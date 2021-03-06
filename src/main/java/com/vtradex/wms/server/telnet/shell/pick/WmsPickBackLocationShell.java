package com.vtradex.wms.server.telnet.shell.pick;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;


import net.wimpi.telnetd.net.Connection;

import com.vtradex.kangaroo.shell.BreakException;
import com.vtradex.kangaroo.shell.ContinueException;
import com.vtradex.kangaroo.shell.Thorn4BaseShell;
import com.vtradex.wms.server.telnet.pick.WmsPickRFManager;
import com.vtradex.wms.server.telnet.shell.ShellExceptions;
import com.vtradex.wms.server.utils.MyUtils;
/**容器退拣->退拣库位*/
public class WmsPickBackLocationShell extends Thorn4BaseShell{
	
	public static final String PAGE_ID = "wmsPickBackLocationShell";
	
	private final WmsPickRFManager pickRFManager;
	
	public WmsPickBackLocationShell(WmsPickRFManager pickRFManager) {
		this.pickRFManager = pickRFManager;
	}
	@Override
	protected void mainProcess(Connection connection) throws BreakException,
			ContinueException, IOException, Exception {
		String messge = "";
		Long mdsId = (Long) this.getParentValue(WmsPickBackMoveDocShell.CURRENT_MDID);//WmsMoveDocAndStation.id
		Object[] value = (Object[]) this.getParentValue(WmsPickBackMoveDocShell.PICK_OBJ);//itemcode,itemname,supcode,qty
		String container = (String) this.getParentValue(WmsPickBackMoveDocShell.CONTAINER);
		String[] keys = new String[]{
				"itemcode","itemname","supcode","pickQty"
		};
		for(int i = 0 ; i< value.length ; i++){
			output(keys[i],value[i].toString());
		}
		String locCode = this.getTextField("退拣库位(00结束)");
		if(StringUtils.isEmpty(locCode)){
			this.setStatusMessage(ShellExceptions.PICK_LOC_IS_NULL);
		}
		locCode = locCode.trim();
		if(MyUtils.OVER.equals(locCode)){
			messge = "请重新扫描器具退拣";
			this.forward(WmsPickBackMoveDocShell.PAGE_ID,messge);
		}
		if(MyUtils.THIS.equals(locCode)){//自动全部退拣到原拣货库位--将来实现
			
		}
		
		messge = "输入退拣量<="+value[3];
		this.put(WmsPickBackMoveDocShell.CURRENT_MDID, mdsId);//WmsMoveDocAndStation.id
		this.put(WmsPickBackMoveDocShell.PICK_OBJ, new Object[]{//itemcode,itemname,supcode,locCode,qty
				value[0],value[1],value[2],locCode,value[3]
		});
		this.put(WmsPickBackMoveDocShell.CONTAINER, container);
		this.forward(WmsPickBackPickingShell.PAGE_ID,messge);
	}

}
