package com.vtradex.wms.server.telnet.shell.quickMove;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import net.wimpi.telnetd.net.Connection;

import com.vtradex.kangaroo.shell.BreakException;
import com.vtradex.kangaroo.shell.ContinueException;
import com.vtradex.kangaroo.shell.Thorn4BaseShell;
import com.vtradex.wms.server.telnet.move.WmsMoveRFManager;

public class WmsQickMoveDoShell extends Thorn4BaseShell{
	public static final String PAGE_ID = "wmsQickMoveDoShell";
	private final WmsMoveRFManager rfWmsMoveManager;

	public WmsQickMoveDoShell(WmsMoveRFManager rfWmsMoveManager) {
		this.rfWmsMoveManager = rfWmsMoveManager;
	}
	protected void mainProcess(Connection connection) throws BreakException,
		ContinueException, IOException, Exception,RuntimeException {
		
		Object[] ix = (Object[])this.getParentContext().get("ix");
		output("源库位",ix[3].toString());
		output("物料",ix[2].toString());
		output("库存可用量",ix[4].toString());
		output("托盘可用量",ix[9].toString());
		output("工艺状态",ix[5] == null ? "" : ix[5].toString());
		String moveQty = this.getTextField("moveQty");
		if (StringUtils.isEmpty(moveQty)) {
			this.setStatusMessage("移位数量必填");
		}
		if(moveQty.contains(".")){
			this.setStatusMessage("移位数量不允许含有小数点");
		}
		Double tempQty = 0D;
		try {
			tempQty = Double.valueOf(moveQty);
		} catch (Exception e) {
			this.setStatusMessage("移位数量录入错误");
		}
		if(tempQty>Double.valueOf(ix[4].toString())){
			this.setStatusMessage("移位数量超出库存可用量");
		}
		if(tempQty>Double.valueOf(ix[9].toString())){
			this.setStatusMessage("移位数量超出托盘可用量");
		}
		if(tempQty<0){
			this.setStatusMessage("移位数量不允许小于0");
		}
		if(tempQty>0){
			String toLocationCode = this.getTextField("toLocationCode");
			//获取到的库位是乱码，下面转编码
			toLocationCode = new String(toLocationCode.getBytes("ISO-8859-1"),"GBK");
			if (StringUtils.isEmpty(toLocationCode)) {
				this.setStatusMessage("目标库位必填");
			}
			String mesg = rfWmsMoveManager.quickMove(ix,tempQty,toLocationCode);
			if(!"-".equals(mesg)){
				this.setStatusMessage(mesg);
			}
		}
		this.forward(WmsQickMoveShell.PAGE_ID, "移位确认成功");
	}
}
