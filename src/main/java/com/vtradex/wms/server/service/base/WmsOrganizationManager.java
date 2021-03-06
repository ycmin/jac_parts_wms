package com.vtradex.wms.server.service.base;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.vtradex.thorn.server.service.BaseManager;
import com.vtradex.wms.server.model.organization.WmsOrganization;
import com.vtradex.wms.server.model.organization.WmsUserSupplier;
import com.vtradex.wms.server.model.organization.WmsUserSupplierHead;
import com.vtradex.wms.server.model.warehouse.WmsWorker;


public interface WmsOrganizationManager extends BaseManager {
	
	@Transactional
	void saveOrganization(WmsOrganization organization);
	
	@Transactional
	void fixLotRule(Object lotRule);
	
	/**
	 * 根据货主统计货主盘点周期，下库位总数，已盘点数，作业中数，待盘点数
	 * */
	@Transactional
	List<Double> countQuantity(Long wmsCommpanyId);
	
	/**
	 * 作业班组管理-添加作业人员
	 **/
	@Transactional
	void addWorkUser(Long workerGroupId,Long workId,String station);
	
	/**
	 * 作业班组管理-移除作业人员
	 **/
	@Transactional
	void removeWorkUser(WmsWorker worker);
	//导入补充信息
	@Transactional
	void importAdditionalInfo(Map<String,String> map);
	
	/**保管员供应商关系保存明细*/
	@Transactional
	void addUserSupplier(Long usdId,WmsUserSupplier us);
	/**保管员供应商关系删除明细*/
	@Transactional
	void removeUserSupplier(WmsUserSupplier us);
	/**保管员供应商关系删除*/
	@Transactional
	void removeUserSupplierHead(WmsUserSupplierHead usd);
	
	@Transactional
	void importBlgItem(File file);
	
}
