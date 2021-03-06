package com.vtradex.wms.server.service.middle;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.vtradex.thorn.server.service.BaseManager;
import com.vtradex.wms.server.model.move.WmsMoveDocDetail;
import com.vtradex.wms.server.model.organization.WmsBillType;
import com.vtradex.wms.server.model.organization.WmsItem;
import com.vtradex.wms.server.model.organization.WmsOrganization;
import com.vtradex.wms.server.model.organization.WmsPackageUnit;
import com.vtradex.wms.server.model.warehouse.WmsWarehouse;

public interface MilldleSessionManager extends BaseManager {
	
	@Transactional
	void sysMiddleSupplier(Object[] obj);
	@Transactional
	void sysMiddleMaterial(Object[] obj);
	@Transactional
	Object[] sysMiddleDeliverydoc(Object[] obj);
	@Transactional
	void sysPickShip(Object[] obj);
	@Transactional
	Object[] readMidQuality(List<Object[]> objs,List<WmsMoveDocDetail> mdds,
			Map<String,String[]> midStatus,List<String> itemStatus);
	@Transactional
	Object[] sysASNSrm(String key,List<Object[]> objs,WmsBillType billType,WmsOrganization company,
			WmsWarehouse warehouse,WmsOrganization supplier,Map<String,WmsItem> itemMaps,Map<String,WmsPackageUnit> puMaps);
	@Transactional
	Map<String , List<Object>>  getFeedIdsAndInsertBillDetail(List<Object[]> feeDataList);
	@Transactional
	void updateHashCode(List<Object> ret);
	@Transactional
	void initMesMisInventory(List<Object[]> objs,Integer lot);
	@Transactional
	void saveAsnDataToWms(List<Object[]> tempDatas,WmsWarehouse warehouse,
			String tableName);
}
