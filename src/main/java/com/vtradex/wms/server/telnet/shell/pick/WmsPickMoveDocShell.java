package com.vtradex.wms.server.telnet.shell.pick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vtradex.wms.server.model.move.WmsMoveDocStatus;
import com.vtradex.wms.server.model.move.WmsMoveDocType;
import com.vtradex.wms.server.telnet.shell.JacPageableBaseShell;
import com.vtradex.wms.server.web.filter.WmsWarehouseHolder;
import com.vtradex.wms.server.web.filter.WmsWorkerHolder;
//RF拣货
public class WmsPickMoveDocShell extends JacPageableBaseShell{
	
	public static final String PAGE_ID = "wmsPickMoveDocShell";
	
	public static final String MOVEDOC_ID = "MOVEDOC_ID";

	@Override
	public String[] getTableHeader() {
		return new String[]{"序号", "拣货单号","需求时间","原始单据类型"};
	}

	@Override
	public String getHql() {
//		String hql = " SELECT doc.id,doc.code FROM WmsMoveDoc doc WHERE 1=1 " +
//				" AND doc.id NOT IN (SELECT DISTINCT mds.moveDocDetail.moveDoc.id FROM WmsMoveDocAndStation mds WHERE mds.isPartPick ='N' )"+ 
//				" /~状态: AND doc.status in  ({状态})~/" +
//				" /~类型: AND doc.type in ({类型})~/" +
//				" /~备料工: AND doc.blg.id = {备料工}~/" +
//				" /~仓库: AND doc.warehouse.id = {仓库}~/";
		String hql = " SELECT DISTINCT mds.moveDocDetail.moveDoc.id,mds.moveDocDetail.moveDoc.code" +
				",to_char(mds.moveDocDetail.moveDoc.pickTicket.requireArriveDate,'yyMMdd hh24mi'),mds.moveDocDetail.moveDoc.originalBillType.name" +
				" FROM WmsMoveDocAndStation mds WHERE 1=1 "+ 
				" /~状态: AND mds.moveDocDetail.moveDoc.status in  ({状态})~/" +
				" /~类型: AND mds.moveDocDetail.moveDoc.type in ({类型})~/" +
				" /~备料工: AND mds.moveDocDetail.moveDoc.blg.id = {备料工}~/" +
				" /~仓库: AND mds.moveDocDetail.moveDoc.warehouse.id = {仓库}~/"+
				" ORDER BY to_char(mds.moveDocDetail.moveDoc.pickTicket.requireArriveDate,'yyMMdd hh24mi')";
		return hql;
	}

	@Override
	public String getNextShell() {
		Object[] rowData = (Object[])get(ROW_DATA_KEY);
		if(rowData != null){
			this.put(MOVEDOC_ID, rowData[0]);
			if("时序件出库单".equals(rowData[3])){
				return WmsPickContainerCodeShell.PAGE_ID;
			}
			return WmsRfMoveDocDetailShell.PAGE_ID;
		}
		return WmsPickMoveDocShell.PAGE_ID;
	}

	@Override
	public Map<String, Object> getParams() {
		Map<String, Object> params = new HashMap<String, Object>();
		List<String> status = new ArrayList<String>();
		List<String> types = new ArrayList<String>();
		status.add(WmsMoveDocStatus.ACTIVE);
		status.add(WmsMoveDocStatus.WORKING);
		types.add(WmsMoveDocType.MV_PICKTICKET_PICKING);
		params.put("状态", status);
		params.put("类型", types);
		params.put("备料工", WmsWorkerHolder.getWmsWorker().getId());
		params.put("仓库", WmsWarehouseHolder.getWmsWarehouse().getId());
		return params;
	}

}
