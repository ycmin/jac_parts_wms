package com.vtradex.wms.server.telnet.shell.receiving;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.wimpi.telnetd.net.Connection;

import com.vtradex.kangaroo.component.support.ObjectOptionDisplayer;
import com.vtradex.kangaroo.shell.BreakException;
import com.vtradex.kangaroo.shell.ContinueException;
import com.vtradex.kangaroo.shell.ShellFactory;
import com.vtradex.kangaroo.shell.Thorn4BaseShell;
import com.vtradex.thorn.server.exception.BusinessException;
import com.vtradex.thorn.server.web.security.UserHolder;
import com.vtradex.wms.server.model.organization.WmsItemState;
import com.vtradex.wms.server.model.receiving.WmsASN;
import com.vtradex.wms.server.model.receiving.WmsASNDetail;
import com.vtradex.wms.server.service.receiving.WmsASNManager;
import com.vtradex.wms.server.telnet.dto.WmsASNDetailDTO;
import com.vtradex.wms.server.telnet.receiving.WmsReceivingRFManager;

public class WmsAsnDetailReceiveShell  extends Thorn4BaseShell{

	public static final String PAGE_ID = "wmsAsnDetailReceiveShell";
	private WmsReceivingRFManager wmsReceivingRFManager;
	public WmsAsnDetailReceiveShell(WmsReceivingRFManager wmsReceivingRFManager) {
		this.wmsReceivingRFManager = wmsReceivingRFManager;
	}
	
	@Override
	protected void mainProcess(Connection connection) throws BreakException,
			ContinueException, IOException, Exception {
		
		String detailIdStr = this.getParentContext().get("detail.id").toString();
		String asnCode = (String) this.getParentContext().get("lotRule.soi"); //asn单据号
		Long detailId = Long.valueOf(detailIdStr);
		WmsASNDetailDTO dto = null;
		try {
			dto = wmsReceivingRFManager.getWmsASNDetailDTO(detailId);
		} catch (BusinessException be) {
			be.printStackTrace();
			this.forward(WmsASNListShell.PAGE_ID, be.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			this.forward(WmsASNListShell.PAGE_ID, "请重新选择ASN");
		}
		WmsASNDetail detail = wmsReceivingRFManager.getAsnDetail(detailId);
		this.output("货品", dto.getItemCode() + "-" + dto.getItemName());
		this.output("期待数量", detail.getExpectedQuantityBU().toString());
//		this.output("收货数量", detail.getReceivedQuantityBU().toString());
		
		
		List<String> inventoryStatus = new ArrayList<String>();
		inventoryStatus.add("-");
		inventoryStatus.add("待检");
		
		String inventoryState = (String) this.getListField("库存状态", inventoryStatus,  new ObjectOptionDisplayer());
		Double receivingQuantityBU = this.getNumberField
							("收货数量", (detail.getExpectedQuantityBU()-detail.getReceivedQuantityBU()));
		
		Long stateId = 0l;
		List<WmsItemState> itemState = wmsReceivingRFManager.getItemStateStatus(detailId);
		for(WmsItemState s : itemState){
			if(s.getName().equals("-")){
				stateId = s.getId();
			}
		}
		List<String> goList =new ArrayList<String>();
		goList.add("是");
		goList.add("否");
		String beGo = (String)getListField("是否确认", goList, new ObjectOptionDisplayer());
		
		if(beGo.equals("是")){
			try {
				wmsReceivingRFManager.detailReceive(detailId,stateId,receivingQuantityBU,UserHolder.getUser().getId(),inventoryState);
			} catch (Exception e) {
				e.printStackTrace();
				this.reset(e.getMessage());
			}
			WmsASN asn = wmsReceivingRFManager.getAsnById(detail.getAsn().getId());
			if(asn.getExpectedQuantityBU() - asn.getReceivedQuantityBU() <= 0){
				this.forward(WmsASNDetailShell.PAGE_ID, "收货成功");
			}else{
				this.put("asnId", asn.getId());
				this.put("lotRule.soi", asnCode);
				this.forward(WmsASNListShell.PAGE_ID, "收货成功,请继续选择ASN明细");
			}
			
		}else{
			this.put("lotRule.soi",asnCode);
			this.forward(WmsASNListShell.PAGE_ID, "继续选择ASN明细");
		}
	}

}
