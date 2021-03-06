package com.vtradex.wms.server.service.receiving;

import java.io.File;
import java.util.Date;

import org.springframework.transaction.annotation.Transactional;

import com.vtradex.thorn.server.service.BaseManager;
import com.vtradex.wms.server.model.base.LotInfo;
import com.vtradex.wms.server.model.organization.WmsPackageUnit;
import com.vtradex.wms.server.model.receiving.WmsASN;
import com.vtradex.wms.server.model.receiving.WmsASNDetail;
import com.vtradex.wms.server.model.warehouse.WmsLocation;
import com.vtradex.wms.server.model.warehouse.WmsWarehouse;

public interface WmsASNDetailManager extends BaseManager {
	/**
	 * 明细收货确认 web
	 * @param detailId
	 * @param lotInfo
	 * @param packageUnitId
	 * @param quantity
	 * @param status
	 * @param receiveLocId
	 * @param pallet
	 * @param carton
	 * @param serialNo
	 */
	@Transactional
	void receive(Long detailId, LotInfo lotInfo, Long packageUnitId, double quantity,String status, 
			Long receiveLocId, String pallet, String carton, String serialNo,Long workerId);
	
	/**
	 * 明细收货确认 telnet
	 */
	@Transactional
	void receive(Long detailId, LotInfo lotInfo, Long packageUnitId, double quantity,String status, 
			Long receiveLocId, String pallet, String carton, String serialNo,Long workerId,String inventoryState);
	
	/**获取收货库位  yc.min 20150404*/
	WmsLocation findReceiveLocation(WmsWarehouse warehouse, String type
			,String billCode,Boolean bePallet,String supper);
	/**
	 * 收货确认
	 * @param detail
	 * @param receiveQty
	 * @param packageUnit
	 * @param lotInfo
	 * @param recLoc
	 */
	@Transactional
	void receiving(WmsASNDetail detail, double receiveQty, WmsPackageUnit packageUnit, LotInfo lotInfo, 
			String status, WmsLocation recLoc, String pallet, String carton, String serialNo,Long workerId);
	
	/**
	 * 
	 */
	@Transactional
	void asnDetailReservation(Long dockId,Date reserveBeginTime,Date reserveFinishTime,WmsASN asn);
	
	/**导入ASN*/
	void importFile(Long companyId,Long billTypeId,File file);
}
