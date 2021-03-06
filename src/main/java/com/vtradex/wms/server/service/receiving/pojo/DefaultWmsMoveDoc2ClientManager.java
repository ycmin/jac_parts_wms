package com.vtradex.wms.server.service.receiving.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vtradex.thorn.client.ui.page.IPage;
import com.vtradex.thorn.server.exception.BusinessException;
import com.vtradex.thorn.server.exception.OriginalBusinessException;
import com.vtradex.thorn.server.service.pojo.DefaultBaseManager;
import com.vtradex.wms.client.ui.page.allocate.page.AllocateConstant;
import com.vtradex.wms.client.ui.page.allocate.page.model.CustomInventory;
import com.vtradex.wms.client.ui.page.allocate.page.model.CustomItem;
import com.vtradex.wms.client.ui.page.allocate.page.model.CustomMoveDoc;
import com.vtradex.wms.client.ui.page.allocate.page.model.CustomMoveDocDetail;
import com.vtradex.wms.client.ui.page.allocate.page.model.CustomPackageUnit;
import com.vtradex.wms.client.ui.page.allocate.page.model.CustomTask;
import com.vtradex.wms.server.model.base.LotInfo;
import com.vtradex.wms.server.model.inventory.WmsInventory;
import com.vtradex.wms.server.model.inventory.WmsItemKey;
import com.vtradex.wms.server.model.move.WmsMoveDoc;
import com.vtradex.wms.server.model.move.WmsMoveDocDetail;
import com.vtradex.wms.server.model.move.WmsTask;
import com.vtradex.wms.server.model.organization.WmsItem;
import com.vtradex.wms.server.model.organization.WmsOrganization;
import com.vtradex.wms.server.model.organization.WmsPackageUnit;
import com.vtradex.wms.server.model.warehouse.WmsLocation;
import com.vtradex.wms.server.service.receiving.WmsMoveDoc2ClientManager;
import com.vtradex.wms.server.service.receiving.WmsMoveDocManager;
import com.vtradex.wms.server.utils.NewLotInfoParser;
import com.vtradex.wms.server.web.filter.WmsWarehouseHolder;

@SuppressWarnings({"unchecked", "rawtypes"})
public class DefaultWmsMoveDoc2ClientManager extends DefaultBaseManager implements WmsMoveDoc2ClientManager{
	protected WmsMoveDocManager wmsMoveDocManager;
	
	public DefaultWmsMoveDoc2ClientManager(WmsMoveDocManager wmsMoveDocManager) {
		super();
		this.wmsMoveDocManager = wmsMoveDocManager;
	}
	
	public Map findMoveDocById(Map params) {
		Map result = new HashMap();
		Long moveDocId = (Long)params.get(AllocateConstant.PARENT_ID);
		String hql = "FROM WmsMoveDoc moveDoc WHERE moveDoc.id=:moveDocId";
		WmsMoveDoc moveDoc = (WmsMoveDoc)commonDao.findByQueryUniqueResult(hql, 
			new String[] {"moveDocId"}, new Object[] {moveDocId});
		CustomMoveDoc customMoveDoc = buildCustomMoveDoc(moveDoc);
		
		hql = null;
		hql = "FROM WmsMoveDocDetail detail WHERE detail.moveDoc.id=:moveDocId" +
			" AND detail.moveDoc.warehouse.id=:wareHouseId" +
			" AND (detail.planQuantityBU>detail.allocatedQuantityBU)";
		List<WmsMoveDocDetail> moveDocDetails = commonDao.findByQuery(hql, 
				new String[] {"moveDocId", "wareHouseId"}, 
				new Object[] {moveDocId, WmsWarehouseHolder.getWmsWarehouse().getId()});
		List<CustomMoveDocDetail> customMoveDocDetails = new ArrayList<CustomMoveDocDetail>();
			
		for(WmsMoveDocDetail moveDocDetail : moveDocDetails) {
			CustomMoveDocDetail customMoveDocDetail = bulidCustomMoveDocDetail(customMoveDoc, moveDocDetail);
			customMoveDocDetails.add(customMoveDocDetail);
		}
		result.put(AllocateConstant.CLIENT_ENTITY,customMoveDoc);
		result.put(AllocateConstant.DETAILS_RESULT, customMoveDocDetails);
		return result;
	}

	public Map findTaskById(Map params) {
		Map result = new HashMap();
		Long moveDocId = (Long)params.get(AllocateConstant.PARENT_ID);
		
		String hql = "FROM WmsMoveDoc moveDoc WHERE moveDoc.id=:moveDocId";
		WmsMoveDoc moveDoc = (WmsMoveDoc)commonDao.findByQueryUniqueResult(hql, 
			new String[] {"moveDocId"}, new Object[] {moveDocId});
		CustomMoveDoc customMoveDoc = buildCustomMoveDoc(moveDoc);
		
		StringBuffer hqlBuffer = new StringBuffer();
		hqlBuffer.append("FROM WmsTask task WHERE task.movedQuantityBU=0")
			.append(" AND task.moveDocDetail.moveDoc.id=:moveDocId")
			.append(" AND task.moveDocDetail.moveDoc.warehouse.id=:warehouseId");
		Map<String, String> queryPars = (Map<String, String>)params.get(AllocateConstant.QUERY_PARAMS);
		if(queryPars != null && queryPars.size() > 0) {
			for(String key : queryPars.keySet()) {
				if(key.equals("locationCode")) {
					hqlBuffer.append(" AND task.toLocationCode LIKE '" + queryPars.get(key) + "'");
				} else if(key.contains("item")){
					hqlBuffer.append(" AND task.itemKey." + key + " LIKE '" + queryPars.get(key) + "'");
				}
			}
		}
		final String hql1 = hqlBuffer.toString();
		List<WmsTask> tasks = commonDao.findByQuery(hql1, 
			new String[] {"moveDocId", "warehouseId"}, 
			new Object[] {moveDocId, WmsWarehouseHolder.getWmsWarehouse().getId()});
		List<CustomTask> customTasks = new ArrayList<CustomTask>();
		for(WmsTask task : tasks) {
			CustomTask customTask = buildCustomTask(customMoveDoc, task);
			
			customTasks.add(customTask);
		}
		result.put(AllocateConstant.CLIENT_ENTITY,customMoveDoc);
		result.put(AllocateConstant.ALLOCATED_RESULT, customTasks);
		return result;
	}
	
	public Map findMoveDocDetailsId(Map params) {
		Long parentId = (Long)params.get(AllocateConstant.PARENT_ID);
		StringBuffer hqlBuffer = new StringBuffer();
		hqlBuffer.append("FROM WmsMoveDocDetail detail WHERE detail.moveDoc.id=:moveDocId")
			.append(" AND detail.moveDoc.warehouse.id=:wareHouseId")
			.append(" AND (detail.planQuantityBU>detail.allocatedQuantityBU)");
		Map<String, String> queryPars = (Map<String, String>)params.get(AllocateConstant.QUERY_PARAMS);
		if(queryPars != null && queryPars.size() > 0) {
			for(String key : queryPars.keySet()) {
				hqlBuffer.append(" AND detail." + key + " LIKE '" + queryPars.get(key) + "'");
			}
		}
		final String hql = hqlBuffer.toString();
		List<WmsMoveDocDetail> moveDocDetails = commonDao.findByQuery(hql, 
			new String[] {"moveDocId", "wareHouseId"}, 
			new Object[] {parentId, WmsWarehouseHolder.getWmsWarehouse().getId()});
		
		String hql1 = "FROM WmsMoveDoc moveDoc WHERE moveDoc.id=:moveDocId";
		WmsMoveDoc moveDoc = (WmsMoveDoc)commonDao.findByQueryUniqueResult(hql1, 
			new String[] {"moveDocId"}, new Object[] {parentId});
		CustomMoveDoc customMoveDoc = buildCustomMoveDoc(moveDoc);
		
		List<CustomMoveDocDetail> customMoveDocDetails = new ArrayList<CustomMoveDocDetail>();
		for(WmsMoveDocDetail moveDocDetail : moveDocDetails) {
			CustomMoveDocDetail customMoveDocDetail = bulidCustomMoveDocDetail(customMoveDoc, moveDocDetail);
			customMoveDocDetails.add(customMoveDocDetail);
		}
		
		Map result = new HashMap();
		result.put(AllocateConstant.CLIENT_ENTITY,customMoveDoc);
		result.put(AllocateConstant.DETAILS_RESULT, customMoveDocDetails);
		return result;
	}

	public Map findAvailableInventories(Map params) {
		Map result = new HashMap();
		Boolean isClearInventories = (Boolean)params.get(AllocateConstant.IS_CLEAR_INVENTORIES);
		if(isClearInventories) {
			return result;
		}
		Long moveDocDetailId = (Long)params.get(AllocateConstant.ID);
		WmsMoveDocDetail moveDocDetail = commonDao.load(WmsMoveDocDetail.class, moveDocDetailId);
		Boolean isFitAsLot = (Boolean)params.get(AllocateConstant.IS_FIT_AS_LOT);
		Boolean containLockInv = (Boolean)params.get(AllocateConstant.CONTAIN_LOCK_INV);
		StringBuffer hqlBuffer = new StringBuffer();
		hqlBuffer.append("FROM WmsInventory inventory")
//			.append(" LEFT JOIN inventory.itemKey")
//			.append(" LEFT JOIN inventory.itemKey.item LEFT JOIN inventory.itemKey.lotInfo.supplier")
			.append(" WHERE inventory.itemKey.item.id = :itemId")
			.append(" AND (inventory.quantityBU - inventory.allocatedQuantityBU) > 0")
			.append(" AND inventory.location.warehouse.id = :warehouseId")
			.append(" AND inventory.location.type IN ('STORAGE')");
		Map<String, String> queryPars = (Map<String, String>)params.get(AllocateConstant.QUERY_PARAMS);
		if(queryPars != null && queryPars.size() > 0) {
			for(String key : queryPars.keySet()) {
				if(key.equals("locationCode")) {
					hqlBuffer.append(" AND inventory.location.code LIKE '" + queryPars.get(key) + "'");
				} else if(key.equals("inventoryStatus")){
					hqlBuffer.append(" AND inventory.status LIKE '" + queryPars.get(key) + "'");
				}
			}
		}
		if(!containLockInv) {
			hqlBuffer.append(" AND (inventory.location.lockCount=false)");
			hqlBuffer.append(" AND (inventory.status = '-')");
		}
		if(moveDocDetail.getShipLotInfo()!=null) {
			if(isFitAsLot) {
				hqlBuffer
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("soi", moveDocDetail.getShipLotInfo().getSoi()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("supplier.code", moveDocDetail.getShipLotInfo().getSupplier()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC1", moveDocDetail.getShipLotInfo().getExtendPropC1()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC2", moveDocDetail.getShipLotInfo().getExtendPropC2()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC3", moveDocDetail.getShipLotInfo().getExtendPropC3()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC4", moveDocDetail.getShipLotInfo().getExtendPropC4()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC5", moveDocDetail.getShipLotInfo().getExtendPropC5()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC6", moveDocDetail.getShipLotInfo().getExtendPropC6()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC7", moveDocDetail.getShipLotInfo().getExtendPropC7()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC8", moveDocDetail.getShipLotInfo().getExtendPropC8()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC9", moveDocDetail.getShipLotInfo().getExtendPropC9()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC10", moveDocDetail.getShipLotInfo().getExtendPropC10()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC11", moveDocDetail.getShipLotInfo().getExtendPropC11()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC12", moveDocDetail.getShipLotInfo().getExtendPropC12()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC13", moveDocDetail.getShipLotInfo().getExtendPropC13()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC14", moveDocDetail.getShipLotInfo().getExtendPropC14()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC15", moveDocDetail.getShipLotInfo().getExtendPropC15()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC16", moveDocDetail.getShipLotInfo().getExtendPropC16()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC17", moveDocDetail.getShipLotInfo().getExtendPropC17()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC18", moveDocDetail.getShipLotInfo().getExtendPropC18()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC19", moveDocDetail.getShipLotInfo().getExtendPropC19()))
					.append(" AND ")
					.append(NewLotInfoParser.decryptStringOfLot("extendPropC20", moveDocDetail.getShipLotInfo().getExtendPropC20()));
			}
		}
		
		final String hql = hqlBuffer.toString();
		List<WmsInventory> inventories = commonDao.findByQuery(hql, 
			new String[] {"itemId", "warehouseId"}, 
			new Object[] {moveDocDetail.getItem().getId(), WmsWarehouseHolder.getWmsWarehouse().getId()});
		List<CustomInventory> customInventories = new ArrayList<CustomInventory>();
		for(WmsInventory inventory : inventories) {
			CustomInventory customInventory = buildCustomInventory(inventory);
			customInventories.add(customInventory);
		}
		CustomMoveDocDetail customMoveDocDetail = bulidCustomMoveDocDetail(moveDocDetail);
		customMoveDocDetail.setContainLockInv(containLockInv);
		customMoveDocDetail.setFitAsLot(isFitAsLot);
		result.put(AllocateConstant.CLIENT_ENTITY,customMoveDocDetail);
		result.put(AllocateConstant.AVAILABLE_RESULT, customInventories);
		return result;
	}
	
	public Map autoAllocate(Map params) {
		Map result = new HashMap();
		CustomMoveDoc customMoveDoc = (CustomMoveDoc)params.get(AllocateConstant.CLIENT_ENTITY);
		Long moveDocId = customMoveDoc.getId();
		WmsMoveDoc moveDoc = commonDao.load(WmsMoveDoc.class, moveDocId);
		if (moveDoc.getUnAllocateQuantityBU() <= 0) {
			throw new OriginalBusinessException("移位单已分配！");
		}
		String hql = "SELECT d.id FROM WmsMoveDocDetail d where d.moveDoc.id = :moveDocId and (d.planQuantityBU - d.allocatedQuantityBU) > 0";
		List<Long> moveDocDetailIdList = commonDao.findByQuery(hql, "moveDocId", moveDoc.getId());
		if (moveDocDetailIdList.isEmpty()) {
			throw new BusinessException("wmsmovedoc.detail.not.exist");
		}
		for (Long moveDocDetailId : moveDocDetailIdList) {
			try {
				WmsMoveDocDetail moveDocDetail = commonDao.load(WmsMoveDocDetail.class, moveDocDetailId);
				wmsMoveDocManager.autoAllocate(moveDocDetail);
			} catch (BusinessException be) {
				logger.error("auto allocate error", be);
			}
		}
		result.put(AllocateConstant.PARENT_ID, moveDocId);
		return result;
	}
	
	public Map cancelAllocate(Map params) {
		Map result = new HashMap();
		CustomMoveDoc customMoveDoc = (CustomMoveDoc)params.get(AllocateConstant.CLIENT_ENTITY);
		Long moveDocId = customMoveDoc.getId();
//		WmsMoveDoc moveDoc = commonDao.load(WmsMoveDoc.class, moveDocId);
		wmsMoveDocManager.cancelAllocate(moveDocId);
		result.put(AllocateConstant.PARENT_ID, moveDocId);
		return result;
	}
	
	public Map manuelCancelAllocate(Map params) {
		Map result = new HashMap();
		CustomMoveDoc customMoveDoc = (CustomMoveDoc)params.get(AllocateConstant.CLIENT_ENTITY);
		Map<Long, Double> cancelInfo = (Map<Long, Double>)params.get(IPage.TABLE_INPUT_VALUES);
		wmsMoveDocManager.manualCancelAllocate(customMoveDoc.getId(), cancelInfo);
		result.put(AllocateConstant.PARENT_ID, customMoveDoc.getId());
		return result;
	}
	
	public Map manuelAllocate(Map params) {
		Map result = new HashMap();
		CustomMoveDocDetail customMoveDocDetail = (CustomMoveDocDetail)params.get(AllocateConstant.CLIENT_ENTITY);
		Map<Long, Double> allocateInfo = (Map<Long, Double>)params.get(IPage.TABLE_INPUT_VALUES);
		wmsMoveDocManager.manuelAllocate(customMoveDocDetail.getId(), allocateInfo);
		result.put(AllocateConstant.PARENT_ID, customMoveDocDetail.getCustomMoveDoc().getId());
		return result;
	}
	
	private CustomMoveDoc buildCustomMoveDoc(WmsMoveDoc moveDoc) {
		CustomMoveDoc customMoveDoc = new CustomMoveDoc();
		customMoveDoc.setId(moveDoc.getId());
		customMoveDoc.setCode(moveDoc.getCode());
		customMoveDoc.setStatus(moveDoc.getStatus());
		customMoveDoc.setType(moveDoc.getType());
		customMoveDoc.setPlanQuantityBU(moveDoc.getPlanQuantityBU());
		customMoveDoc.setAllocatedQuantityBU(moveDoc.getAllocatedQuantityBU());
		customMoveDoc.setMovedQuantityBU(moveDoc.getMovedQuantityBU());
		customMoveDoc.setShippedQuantityBU(moveDoc.getShippedQuantityBU());
		return customMoveDoc;
	}
	
	private CustomMoveDocDetail bulidCustomMoveDocDetail(CustomMoveDoc customMoveDoc, WmsMoveDocDetail moveDocDetail) {
		CustomMoveDocDetail customMoveDocDetail = new CustomMoveDocDetail();
		CustomItem customItem = new CustomItem();
		CustomPackageUnit customPackageUnit = new CustomPackageUnit();
		
		WmsItem item = commonDao.load(WmsItem.class, moveDocDetail.getItem().getId());
		customItem.setId(moveDocDetail.getItem().getId());
		customItem.setCode(item.getCode());
		customItem.setName(item.getName());
		
		WmsPackageUnit packageUnit = commonDao.load(WmsPackageUnit.class, moveDocDetail.getPackageUnit().getId());
		customPackageUnit.setId(packageUnit.getId());
		customPackageUnit.setUnit(packageUnit.getUnit());
		customPackageUnit.setConvertFigure(packageUnit.getConvertFigure());
		
		customMoveDocDetail.setId(moveDocDetail.getId());
		customMoveDocDetail.setCustomMoveDoc(customMoveDoc);
		customMoveDocDetail.setCustomItem(customItem);
		customMoveDocDetail.setCustomPackageUnit(customPackageUnit);
		customMoveDocDetail.setPlanQuantity(moveDocDetail.getPlanQuantity());
		customMoveDocDetail.setPlanQuantityBU(moveDocDetail.getPlanQuantityBU());
		customMoveDocDetail.setAllocatedQuantityBU(moveDocDetail.getAllocatedQuantityBU());
		customMoveDocDetail.setMovedQantityBU(moveDocDetail.getMovedQuantityBU());
		customMoveDocDetail.setFitAsLot(Boolean.TRUE);
		customMoveDocDetail.setContainLockInv(Boolean.FALSE);
		String shipLotInfo = "-";
		if(moveDocDetail.getShipLotInfo() != null) {
			shipLotInfo = customMoveDocDetail.toLotInfor(moveDocDetail.getShipLotInfo().getSoi(), moveDocDetail.getShipLotInfo().getSupplier(), 
				moveDocDetail.getShipLotInfo().getExtendPropC1(), moveDocDetail.getShipLotInfo().getExtendPropC2(),
				moveDocDetail.getShipLotInfo().getExtendPropC3(), moveDocDetail.getShipLotInfo().getExtendPropC4(), 
				moveDocDetail.getShipLotInfo().getExtendPropC5(),moveDocDetail.getShipLotInfo().getExtendPropC6(),
				moveDocDetail.getShipLotInfo().getExtendPropC7(),moveDocDetail.getShipLotInfo().getExtendPropC8(),
				moveDocDetail.getShipLotInfo().getExtendPropC9(),moveDocDetail.getShipLotInfo().getExtendPropC10(),
				moveDocDetail.getShipLotInfo().getExtendPropC11(),moveDocDetail.getShipLotInfo().getExtendPropC12(),
				moveDocDetail.getShipLotInfo().getExtendPropC13(),moveDocDetail.getShipLotInfo().getExtendPropC14(),
				moveDocDetail.getShipLotInfo().getExtendPropC15(),moveDocDetail.getShipLotInfo().getExtendPropC16(),
				moveDocDetail.getShipLotInfo().getExtendPropC17(),moveDocDetail.getShipLotInfo().getExtendPropC18(),
				moveDocDetail.getShipLotInfo().getExtendPropC19(),moveDocDetail.getShipLotInfo().getExtendPropC20());
		}
		customMoveDocDetail.setShipLotInfo(shipLotInfo);
		return customMoveDocDetail;
	}
	
	private CustomTask buildCustomTask(CustomMoveDoc customMoveDoc, WmsTask task) {
		CustomTask customTask = new CustomTask();
		CustomItem customItem = new CustomItem();
		CustomPackageUnit customPackageUnit = new CustomPackageUnit();
		
		WmsItemKey itemKey = commonDao.load(WmsItemKey.class, task.getItemKey().getId());
		WmsItem item = commonDao.load(WmsItem.class, itemKey.getItem().getId());
		LotInfo lot = itemKey.getLotInfo();
		String supplierName = "";
		String lotInfo = "-";
		if(lot != null) {
			lotInfo = customTask.toLotInfor(lot.getSoi(), supplierName, 
				lot.getExtendPropC1(), lot.getExtendPropC2(), lot.getExtendPropC3(), 
				lot.getExtendPropC4(), lot.getExtendPropC5(), lot.getExtendPropC6(), 
				lot.getExtendPropC7(), lot.getExtendPropC8(), lot.getExtendPropC9(), 
				lot.getExtendPropC10(), lot.getExtendPropC11(), lot.getExtendPropC12(), 
				lot.getExtendPropC13(), lot.getExtendPropC14(), lot.getExtendPropC15(), 
				lot.getExtendPropC16(), lot.getExtendPropC17(), lot.getExtendPropC18(), 
				lot.getExtendPropC19(), lot.getExtendPropC20());
		}
		if(lot != null && lot.getSupplier() != null) {
			WmsOrganization supplier = commonDao.load(WmsOrganization.class, lot.getSupplier().getId());
			supplierName = supplier.getName();
		}
		WmsPackageUnit packageUnit = commonDao.load(WmsPackageUnit.class, task.getPackageUnit().getId());
		
		customItem.setId(item.getId());
		customItem.setCode(item.getCode());
		customItem.setName(item.getName());
		
		customPackageUnit.setId(packageUnit.getId());
		customPackageUnit.setUnit(packageUnit.getUnit());
		customPackageUnit.setConvertFigure(packageUnit.getConvertFigure());
		
		customTask.setId(task.getId());
		String locationCode = task.getToLocationCode();
		if(locationCode == null) {
			locationCode = "-";
		}
		customTask.setLocationCode(locationCode);
		customTask.setCustomMoveDoc(customMoveDoc);
		customTask.setCustomUnit(customPackageUnit);
		customTask.setCustomItem(customItem);
		customTask.setPlanQuantity(task.getPlanQuantity());
		customTask.setPlanQuantityBU(task.getPlanQuantityBU());
		customTask.setCancelQuantity(task.getPlanQuantityBU());
		customTask.setLotInfo(lotInfo);
		return customTask;
	}
	
	private CustomInventory buildCustomInventory(WmsInventory inventory) {
		CustomInventory customInventory = new CustomInventory();
		CustomItem customItem = new CustomItem();
		CustomPackageUnit customPackageUnit = new CustomPackageUnit();
		
		WmsItemKey itemKey = commonDao.load(WmsItemKey.class, inventory.getItemKey().getId());
		WmsItem item = commonDao.load(WmsItem.class, itemKey.getItem().getId());
		WmsLocation location = commonDao.load(WmsLocation.class, inventory.getLocation().getId());
		LotInfo lot = itemKey.getLotInfo();
		String supplierCode = "",supplierName = "";
		if(lot != null && lot.getSupplier() != null) {
			WmsOrganization supplier = commonDao.load(WmsOrganization.class, lot.getSupplier().getId());
			supplierCode = supplier.getCode();
			supplierName = supplier.getName();
		}
		String lotInfo = "-";
		if(lot != null) {
			lotInfo = customInventory.toLotInfor(supplierCode,supplierName,lot.getSoi(),  
				lot.getExtendPropC1(), lot.getExtendPropC2(), lot.getExtendPropC3(), 
				lot.getExtendPropC4(), lot.getExtendPropC5(), lot.getExtendPropC6(), 
				lot.getExtendPropC7(), lot.getExtendPropC8(), lot.getExtendPropC9(), 
				lot.getExtendPropC10(), lot.getExtendPropC11(), lot.getExtendPropC12(), 
				lot.getExtendPropC13(), lot.getExtendPropC14(), lot.getExtendPropC15(), 
				lot.getExtendPropC16(), lot.getExtendPropC17(), lot.getExtendPropC18(), 
				lot.getExtendPropC19(), lot.getExtendPropC20());
		}
		
		WmsPackageUnit packageUnit = commonDao.load(WmsPackageUnit.class, inventory.getPackageUnit().getId());
		
		customItem.setId(item.getId());
		customItem.setCode(item.getCode());
		customItem.setName(item.getName());
		
		customPackageUnit.setId(packageUnit.getId());
		customPackageUnit.setUnit(packageUnit.getUnit());
		customPackageUnit.setConvertFigure(packageUnit.getConvertFigure());
		
		customInventory.setId(inventory.getId());
		customInventory.setAvailableQuantity(inventory.getAvailableQuantityBU());
		customInventory.setCustomItem(customItem);
		customInventory.setCustomPackageUnit(customPackageUnit);
		customInventory.setLocationCode(location.getCode());
		customInventory.setLotInfo(lotInfo);
		customInventory.setQuantity(inventory.getQuantity());
		customInventory.setStatus(inventory.getStatus());
		customInventory.setSupperCode(supplierCode);
		customInventory.setSupperName(supplierName);
		return customInventory;
	}
	
	private CustomMoveDocDetail bulidCustomMoveDocDetail(WmsMoveDocDetail moveDocDetail) {
		CustomMoveDocDetail customMoveDocDetail = new CustomMoveDocDetail();
		CustomItem customItem = new CustomItem();
		CustomPackageUnit customPackageUnit = new CustomPackageUnit();
		
		WmsItem item = commonDao.load(WmsItem.class, moveDocDetail.getItem().getId());
		customItem.setId(moveDocDetail.getItem().getId());
		customItem.setCode(item.getCode());
		customItem.setName(item.getName());
		
		WmsPackageUnit packageUnit = commonDao.load(WmsPackageUnit.class, moveDocDetail.getPackageUnit().getId());
		customPackageUnit.setId(packageUnit.getId());
		customPackageUnit.setUnit(packageUnit.getUnit());
		customPackageUnit.setConvertFigure(packageUnit.getConvertFigure());
		
		WmsMoveDoc moveDoc = commonDao.load(WmsMoveDoc.class, moveDocDetail.getMoveDoc().getId());
		CustomMoveDoc customMoveDoc = buildCustomMoveDoc(moveDoc);
		
		customMoveDocDetail.setId(moveDocDetail.getId());
		customMoveDocDetail.setCustomMoveDoc(customMoveDoc);
		customMoveDocDetail.setCustomItem(customItem);
		customMoveDocDetail.setCustomPackageUnit(customPackageUnit);
		customMoveDocDetail.setPlanQuantity(moveDocDetail.getPlanQuantity());
		customMoveDocDetail.setPlanQuantityBU(moveDocDetail.getPlanQuantityBU());
		customMoveDocDetail.setAllocatedQuantityBU(moveDocDetail.getAllocatedQuantityBU());
		customMoveDocDetail.setMovedQantityBU(moveDocDetail.getMovedQuantityBU());
		String shipLotInfo = "-";
		if(moveDocDetail.getShipLotInfo() != null) {
			shipLotInfo = customMoveDocDetail.toLotInfor(moveDocDetail.getShipLotInfo().getSoi(), moveDocDetail.getShipLotInfo().getSupplier(), 
				moveDocDetail.getShipLotInfo().getExtendPropC1(), moveDocDetail.getShipLotInfo().getExtendPropC2(),
				moveDocDetail.getShipLotInfo().getExtendPropC3(), moveDocDetail.getShipLotInfo().getExtendPropC4(), 
				moveDocDetail.getShipLotInfo().getExtendPropC5(),moveDocDetail.getShipLotInfo().getExtendPropC6(),
				moveDocDetail.getShipLotInfo().getExtendPropC7(),moveDocDetail.getShipLotInfo().getExtendPropC8(),
				moveDocDetail.getShipLotInfo().getExtendPropC9(),moveDocDetail.getShipLotInfo().getExtendPropC10(),
				moveDocDetail.getShipLotInfo().getExtendPropC11(),moveDocDetail.getShipLotInfo().getExtendPropC12(),
				moveDocDetail.getShipLotInfo().getExtendPropC13(),moveDocDetail.getShipLotInfo().getExtendPropC14(),
				moveDocDetail.getShipLotInfo().getExtendPropC15(),moveDocDetail.getShipLotInfo().getExtendPropC16(),
				moveDocDetail.getShipLotInfo().getExtendPropC17(),moveDocDetail.getShipLotInfo().getExtendPropC18(),
				moveDocDetail.getShipLotInfo().getExtendPropC19(),moveDocDetail.getShipLotInfo().getExtendPropC20());
		}
		customMoveDocDetail.setShipLotInfo(shipLotInfo);
		return customMoveDocDetail;
	}
}