package com.vtradex.wms.server.telnet.putaway.pojo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vtradex.thorn.client.utils.StringUtils;
import com.vtradex.thorn.server.exception.BusinessException;
import com.vtradex.thorn.server.model.EntityFactory;
import com.vtradex.thorn.server.service.WorkflowManager;
import com.vtradex.thorn.server.service.pojo.DefaultBaseManager;
import com.vtradex.thorn.server.util.BeanUtils;
import com.vtradex.wms.server.model.base.BaseStatus;
import com.vtradex.wms.server.model.base.LotInfo;
import com.vtradex.wms.server.model.inventory.WmsInventory;
import com.vtradex.wms.server.model.inventory.WmsInventoryExtend;
import com.vtradex.wms.server.model.move.WmsMoveDoc;
import com.vtradex.wms.server.model.move.WmsMoveDocDetail;
import com.vtradex.wms.server.model.move.WmsMoveDocStatus;
import com.vtradex.wms.server.model.move.WmsMoveDocType;
import com.vtradex.wms.server.model.move.WmsTask;
import com.vtradex.wms.server.model.move.WmsTaskStatus;
import com.vtradex.wms.server.model.organization.WmsBillType;
import com.vtradex.wms.server.model.organization.WmsItem;
import com.vtradex.wms.server.model.organization.WmsLotRule;
import com.vtradex.wms.server.model.organization.WmsOrganization;
import com.vtradex.wms.server.model.organization.WmsPackageUnit;
import com.vtradex.wms.server.model.receiving.JacPalletSerial;
import com.vtradex.wms.server.model.receiving.WmsASN;
import com.vtradex.wms.server.model.receiving.WmsASNDetail;
import com.vtradex.wms.server.model.receiving.WmsASNQualityStauts;
import com.vtradex.wms.server.model.receiving.WmsASNShelvesStauts;
import com.vtradex.wms.server.model.receiving.WmsBooking;
import com.vtradex.wms.server.model.warehouse.WmsDock;
import com.vtradex.wms.server.model.warehouse.WmsLocation;
import com.vtradex.wms.server.model.warehouse.WmsLocationType;
import com.vtradex.wms.server.model.warehouse.WmsWarehouse;
import com.vtradex.wms.server.service.inventory.WmsInventoryExtendManager;
import com.vtradex.wms.server.service.inventory.WmsInventoryManager;
import com.vtradex.wms.server.service.receiving.WmsASNManager;
import com.vtradex.wms.server.service.receiving.WmsMoveDocManager;
import com.vtradex.wms.server.service.rule.WmsRuleManager;
import com.vtradex.wms.server.service.task.WmsTaskManager;
import com.vtradex.wms.server.service.workDoc.WmsWorkDocManager;
import com.vtradex.wms.server.telnet.dto.WmsASNDetailDTO;
import com.vtradex.wms.server.telnet.dto.WmsPutawayDTO;
import com.vtradex.wms.server.telnet.exception.RFFinishException;
import com.vtradex.wms.server.telnet.putaway.WmsPutawayRFManager;
import com.vtradex.wms.server.utils.DoubleUtils;
import com.vtradex.wms.server.utils.MyUtils;
import com.vtradex.wms.server.utils.PackageUtils;
import com.vtradex.wms.server.web.filter.WmsWarehouseHolder;
import com.vtradex.wms.server.web.filter.WmsWorkerHolder;

/**
 * RF 上架
 *
 * @category 
 * @author <a href="brofe.pan@gmail.com">潘宁波</a>
 * @version $Revision: 1.5 $ $Date: 2015/12/07 03:34:57 $
 */
@SuppressWarnings("unchecked")
public class DefaultWmsPutawayRFManager extends DefaultBaseManager implements WmsPutawayRFManager {
	
	private final WmsWorkDocManager wmsWorkDocManager;
	private final WmsRuleManager wmsRuleManager;
	private final WmsInventoryManager wmsInventoryManager;
	private final WorkflowManager workflowManager;
	private final WmsInventoryExtendManager inventoryExtendManager;
	private final WmsMoveDocManager wmsMoveDocManager;
	private final WmsASNManager asnManager;
	private final WmsTaskManager wmsTaskManager;
	
	public DefaultWmsPutawayRFManager(WmsWorkDocManager wmsWorkDocManager, WmsRuleManager wmsRuleManager,
			WmsInventoryManager wmsInventoryManager, WorkflowManager workflowManager, 
			WmsInventoryExtendManager inventoryExtendManager, WmsMoveDocManager wmsMoveDocManager,
			WmsASNManager asnManager,WmsTaskManager wmsTaskManager) {
		this.wmsWorkDocManager = wmsWorkDocManager;
		this.wmsRuleManager = wmsRuleManager;
		this.wmsInventoryManager = wmsInventoryManager;
		this.workflowManager = workflowManager;
		this.inventoryExtendManager = inventoryExtendManager;
		this.wmsMoveDocManager = wmsMoveDocManager;
		this.asnManager = asnManager;
		this.wmsTaskManager = wmsTaskManager;
	}
	
	public WmsPutawayDTO getWmsPutawayDTO(String asnCode, Long moveDocId, String barCode) throws RFFinishException, BusinessException {
		WmsMoveDoc moveDoc = commonDao.load(WmsMoveDoc.class, moveDocId);
		if (WmsMoveDocStatus.FINISHED.equals(moveDoc.getStatus())) {
			throw new RFFinishException("已上架完成");
		}
		
		String hql = "FROM WmsMoveDocDetail mdd where mdd.moveDoc.id = :moveDocId and mdd.item.barCode = :barCode and (mdd.allocatedQuantityBU - movedQuantityBU) > 0";
		List<WmsMoveDocDetail> mddList = commonDao.findByQuery(hql, new String[]{"moveDocId", "barCode"}, new Object[]{moveDocId, barCode});
		if (mddList == null || mddList.isEmpty()) {
			throw new BusinessException("当前ASN中无此条码待上架的任务");
		}
		
		WmsMoveDocDetail mdd = mddList.get(0);
		WmsItem item = commonDao.load(WmsItem.class, mdd.getItem().getId());
		
		String taskHql = "SELECT t FROM WmsTask t, WmsLocation toLocation " +
				" WHERE t.moveDocDetail.id = :moveDocDetailId " +
				" AND toLocation.id = t.toLocationId " +
//				" AND toLocation.zone >= :startZone AND toLocation.zone <= :endZone " +
//				" AND toLocation.line >= :startLine AND toLocation.line <= :endLine " +
				" AND t.status in ('DISPATCHED', 'WORKING') " +
				" AND t.pallet = '-'" +
				" AND (t.worker is null or t.worker.id =:workerID)" +
				" ORDER BY toLocation.routeNo ASC, toLocation.code ASC";
		
//		WmsWorkAreaExtDTO workAreaExt = WmsWorkAreaExtHolder.getWmsWorkAreaExt();
//		List<WmsTask> taskList = commonDao.findByQuery(taskHql, 
//				new String[]{"moveDocDetailId", "startZone", "endZone", "startLine", "endLine"}, 
//				new Object[]{mdd.getId(), workAreaExt.getStartZone(), workAreaExt.getEndZone(), workAreaExt.getStartLine(), workAreaExt.getEndLine()});
		
		List<WmsTask> taskList = commonDao.findByQuery(taskHql, 
				new String[]{"moveDocDetailId","workerID"}
				, new Object[]{mdd.getId() , WmsWorkerHolder.getWmsWorker().getId()});

		if (taskList == null || taskList.isEmpty()) {
			throw new BusinessException("无此条码待上架的任务");
		}
		WmsTask task = taskList.get(0);
		
		task.setWorker(WmsWorkerHolder.getWmsWorker());
		
		WmsPutawayDTO putawayDTO = new WmsPutawayDTO();
		putawayDTO.setMoveDocId(moveDoc.getId());
		putawayDTO.setMoveDocDetailId(mdd.getId());
		putawayDTO.setTaskId(task.getId());
		putawayDTO.setItemCode(item.getCode());
		putawayDTO.setItemName(item.getName());
		putawayDTO.setFromLocationId(task.getFromLocationId());
		putawayDTO.setToLocationId(task.getToLocationId());
		putawayDTO.setToLocationCode(task.getToLocationCode());
		putawayDTO.setStayOnPutawayQuantity(task.getUnmovedQuantityBU());
		
		return putawayDTO;
	}
	
	public void confirmPutaway(WmsPutawayDTO putawayDTO) throws BusinessException {
		String locHql = "FROM WmsLocation loc where loc.code = :code and loc.warehouse.id = :warehouseId";
		
		Object locObj = this.commonDao.findByQueryUniqueResult(locHql, new String[]{"code", "warehouseId"}, 
				new Object[]{putawayDTO.getPutawayLocationCode(), WmsWarehouseHolder.getWmsWarehouse().getId()});
		if (locObj == null) {
			throw new BusinessException("无此上架库位");
		}
		WmsLocation toLocation = (WmsLocation)locObj;
		
		WmsTask task = commonDao.load(WmsTask.class, putawayDTO.getTaskId());
		WmsMoveDocDetail mdd = commonDao.load(WmsMoveDocDetail.class, putawayDTO.getMoveDocDetailId());
		task.setMoveDocDetail(mdd);
		
		wmsWorkDocManager.singleWorkConfirm(task, toLocation.getId(), putawayDTO.getFromLocationId(), 
				putawayDTO.getPutawayQuantity(), WmsWorkerHolder.getWmsWorker().getId());
		
		WmsMoveDoc moveDoc = commonDao.load(WmsMoveDoc.class, putawayDTO.getMoveDocId());
//		if (WmsMoveDocStatus.FINISHED.equals(moveDoc.getStatus())) {
//			throw new RFFinishException("上架完成");
//		}
	}
	
	public Boolean validateLocation(String locationCode) {
		String locHql = "FROM WmsLocation loc where loc.code = :code and loc.warehouse.id = :warehouseId";
		Object locObj = this.commonDao.findByQueryUniqueResult(locHql, new String[]{"code", "warehouseId"}, 
				new Object[]{locationCode, WmsWarehouseHolder.getWmsWarehouse().getId()});
		if (locObj == null) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
	
	public WmsPutawayDTO getWmsPutawayDTOByPalletNo(String palletNo) throws BusinessException {
		
		String taskHql = "SELECT t FROM WmsTask t, WmsLocation toLocation " +
				" WHERE t.pallet = :palletNo " +
				" AND toLocation.id = t.toLocationId " +
//				" AND toLocation.zone >= :startZone AND toLocation.zone <= :endZone " +
//				" AND toLocation.line >= :startLine AND toLocation.line <= :endLine " +
				" AND t.status in ('DISPATCHED', 'WORKING','FINISHED') " +
				" AND (t.worker is null or t.worker.id =:workerID)" +
				" ORDER BY toLocation.routeNo ASC, toLocation.code ASC";
		
//		WmsWorkAreaExtDTO workAreaExt = WmsWorkAreaExtHolder.getWmsWorkAreaExt();
//		List<WmsTask> taskList = commonDao.findByQuery(taskHql, 
//				new String[]{"moveDocDetailId", "startZone", "endZone", "startLine", "endLine"}, 
//				new Object[]{mdd.getId(), workAreaExt.getStartZone(), workAreaExt.getEndZone(), workAreaExt.getStartLine(), workAreaExt.getEndLine()});
		
		List<WmsTask> taskList = commonDao.findByQuery(taskHql, 
				new String[]{"palletNo","workerID"}
			, new Object[]{palletNo , WmsWorkerHolder.getWmsWorker().getId()});
		if (taskList == null || taskList.isEmpty()) {
			throw new BusinessException("无此托盘待上架的任务");
		} 
		WmsTask task = null;
		for (int i = 0; i < taskList.size(); i++) {
			task = taskList.get(i);
			if (!WmsTaskStatus.FINISHED.equals(task.getStatus())) {
				break;
			}
		}
		
		if(task == null){
			throw new RFFinishException("无此当前托盘号的上架任务");
		}
		
		task.setWorker(WmsWorkerHolder.getWmsWorker());
		
		WmsMoveDocDetail mdd = commonDao.load(WmsMoveDocDetail.class, task.getMoveDocDetail().getId());
		WmsMoveDoc moveDoc = commonDao.load(WmsMoveDoc.class, mdd.getMoveDoc().getId());
		WmsItem item = commonDao.load(WmsItem.class, mdd.getItem().getId());
		
		WmsPutawayDTO putawayDTO = new WmsPutawayDTO();
		putawayDTO.setMoveDocId(moveDoc.getId());
		putawayDTO.setMoveDocDetailId(mdd.getId());
		putawayDTO.setTaskId(task.getId());
		putawayDTO.setItemCode(item.getCode());
		putawayDTO.setItemName(item.getName());
		putawayDTO.setFromLocationId(task.getFromLocationId());
		putawayDTO.setToLocationId(task.getToLocationId());
		putawayDTO.setToLocationCode(task.getToLocationCode());
		putawayDTO.setStayOnPutawayQuantity(task.getUnmovedQuantityBU());

		
		return putawayDTO;
	}
	
	public void markExceptionWmsLocation(Long locationId) throws BusinessException {
		try {
			WmsLocation loc = commonDao.load(WmsLocation.class, locationId);
			loc.setExceptionFlag(Boolean.TRUE);
		} catch (BusinessException be) {
			throw new BusinessException("标识异常库位失败，请重试");
		}
	}
	
	private void putawayAllocate(WmsTask task, WmsLocation toLoc, double quantityBU, 
			WmsMoveDocDetail detail, boolean isNew) {
		WmsLocation fromLoc = commonDao.load(WmsLocation.class,
				detail.getFromLocationId());
		WmsInventory dstInventory = wmsInventoryManager.getInventoryWithNew(
				toLoc, detail.getItemKey(), detail.getPackageUnit(),
				detail.getInventoryStatus());
		dstInventory.allocatePutaway(quantityBU);
		
		commonDao.store(dstInventory);
		// 如果是托盘上架，还要更新库位上的托盘占用数，用于计算托盘库位满度(每条明细就是一个托盘)
		if (detail.getBePallet()
				&& WmsLocationType.STORAGE.equals(toLoc.getType())) {
			toLoc.addPallet(1);
		}
		// 调用规则刷新库满度
		wmsInventoryManager.refreshLocationUseRate(toLoc, 0);
		commonDao.store(toLoc);

		if(isNew) {
			WmsTask newTask = EntityFactory.getEntity(WmsTask.class);
			BeanUtils.copyEntity(newTask, task);
			newTask.setId(null);
			newTask.setPlanQuantityBU(quantityBU);
			newTask.setPlanQuantity(PackageUtils.convertPackQuantity(quantityBU,task.getPackageUnit()));
			newTask.setMovedQuantityBU(0D);
			newTask.setStatus(WmsTaskStatus.DISPATCHED);
			newTask.setToLocationId(dstInventory.getLocation().getId());
			newTask.setToLocationCode(dstInventory.getLocation().getCode());
			newTask.setDescInventoryId(dstInventory.getId());
			newTask.calculateLoad();
			detail.getTasks().add(newTask);
			if(newTask.getWorkDoc()!=null) {
				newTask.getWorkDoc().addTask(newTask);
			}
			commonDao.store(newTask);
		} else {
			task.addPlanQuantityBU(quantityBU);
			task.setToLocationId(dstInventory.getLocation().getId());
			task.setToLocationCode(dstInventory.getLocation().getCode());
			task.setDescInventoryId(dstInventory.getId());
			if(task.getWorkDoc()!=null) {
				task.getWorkDoc().addTask(task);
			}
			commonDao.store(task);
		}
			
		// 更新上架单及明细数量
		detail.allocate(quantityBU);
		commonDao.store(detail);
	}
	
	public WmsASNDetail getWmsAsnDetailByBarCode(String asnCode,
			String barCode, Long packageUnitId) throws BusinessException {
		String hql = "FROM WmsASNDetail detail where detail.asn.code = :asnCode " +
				" and detail.receivedQuantityBU - detail.movedQuantityBU > 0 " +
				" and detail.item.barCode=:barCode and detail.packageUnit.id = :packageUnitId";
		List<WmsASNDetail> asnDetailList = commonDao.findByQuery(hql, 
				new String[]{"asnCode","barCode","packageUnitId"}, 
				new Object[]{asnCode , barCode, packageUnitId});
		
		if (asnDetailList == null || asnDetailList.isEmpty()) {
			throw new BusinessException("当前ASN明细中无待上架的货品");
		}
		return asnDetailList.get(0);
	}
	
	public String validatePalletRule(Long asnDetialId){
		String result = "";
		WmsASNDetail wmsASNDetail = load(WmsASNDetail.class,asnDetialId);

		Map<String,Object> ruleTableInfo = wmsRuleManager.getRuleTableDetail(WmsWarehouseHolder.getWmsWarehouse().getName(), 
				new Object[]{"R101_基础数据_码托规则表",wmsASNDetail.getAsn().getCompany().getName(),wmsASNDetail.getItem().getClass3(),wmsASNDetail.getPackageUnit().getLevel()});
		if(ruleTableInfo != null){
			result = ruleTableInfo.get("码托描述")+"";
			return result;
		}
		
		return result;
	}

	public WmsMoveDoc manualPalletAndAutoCreateMoveDoc(Long asnDetialId, String palletNum, String itemCode, String putawayLocationCode) {
		WmsASNDetail wmsASNDetail = load(WmsASNDetail.class,asnDetialId);
		Double quantity = 0D;
		try {
			quantity = new Double(palletNum);
		} catch (Exception ex) {
			throw new BusinessException("每托数量应为正整数！");
		}
		if(quantity <= 0D) {
			throw new BusinessException("每托数量应为正整数！");
		}
		
		if(WmsASNQualityStauts.UNQUALITY.equals(wmsASNDetail.getAsn().getQualityStauts()) && !wmsASNDetail.getAsn().isQualityPutaway()) {			
			if(!isExistUnQuality(wmsASNDetail.getAsn())) {
				throw new BusinessException("存在未质检完成的记录，请先处理！");
			}
		}
		
		Double quantityBU = PackageUtils.convertBUQuantity(quantity, wmsASNDetail.getPackageUnit());
		
		String hql = " FROM WmsInventoryExtend invExtend WHERE invExtend.inventory.itemKey.lotInfo.soi = :soi and invExtend.pallet='-'" +
				"AND invExtend.item.code=:itemCode AND invExtend.quantityBU - invExtend.allocatedQuantityBU > 0 " +
				"AND invExtend.inventory.packageUnit.id =:packageUnitId";
		List<WmsInventoryExtend> invExtends = commonDao.findByQuery(hql, new String[]{"soi","itemCode","packageUnitId"}
		, new Object[]{wmsASNDetail.getAsn().getCode() , itemCode, wmsASNDetail.getPackageUnit().getId()});
		
		if(invExtends.size() == 0) {
			throw new BusinessException("库存数量不足,请重新输入数量！");
		}
		Map problem = new HashMap();
		problem.put("类型", "手工码托");
		Map<String, Object> result = wmsRuleManager.execute(wmsASNDetail.getAsn().getWarehouse().getName(),
				wmsASNDetail.getItem().getCompany().getName(), "码托规则", problem);
		Object pallet = result.get("托盘号");

		for (WmsInventoryExtend inventoryExtend : invExtends) {
			Double tempQuantityBU = quantityBU > inventoryExtend.getQuantityBU() ? inventoryExtend.getQuantityBU() : quantityBU;
			
			// 扣减原序列号数量
			inventoryExtend.removeQuantity(tempQuantityBU);
			if (DoubleUtils.compareByPrecision(inventoryExtend.getQuantityBU(), 0D,
					inventoryExtend.getInventory().getPackageUnit().getPrecision()) == 0) {
				commonDao.delete(inventoryExtend);
			}
			quantityBU -= tempQuantityBU;
			if(quantityBU == 0) {
				// 生成新序列号信息
				inventoryExtendManager.addInventoryExtend(
						inventoryExtend.getInventory(), pallet.toString(),
						inventoryExtend.getCarton(), inventoryExtend.getSerialNo(),
						PackageUtils.convertBUQuantity(quantity, wmsASNDetail.getPackageUnit()));
				break;
			}
		}
		if (quantityBU > 0) {
			throw new BusinessException("库存数量不足,请重新输入数量！");
		}
		
		WmsMoveDoc moveDoc = autoCreateMoveDocByResults(wmsASNDetail.getAsn().getId(), pallet.toString(), 
				PackageUtils.convertBUQuantity(quantity, wmsASNDetail.getPackageUnit()), 
				itemCode , wmsASNDetail.getPackageUnit().getId(), putawayLocationCode);
		return moveDoc;
	}
	
	public boolean isExistUnQuality(WmsASN asn){
		Long unQualityCount = (Long)commonDao.findByQueryUniqueResult("SELECT count(*) FROM WmsInventory inv WHERE " +
				"inv.status = :status AND inv.itemKey.lotInfo.soi = :soi AND inv.quantityBU - inv.allocatedQuantityBU > 0",
				new String[]{"status","soi"},
				new Object[]{WmsASNQualityStauts.UNQUALITY_KEY, asn.getCode()});
		return unQualityCount == null || unQualityCount.longValue() == 0;
	}
	
	private WmsMoveDoc autoCreateMoveDocByResults(Long asnId, String palletNo, Double allocateQuantity,  String itemCode , Long packageUnitId, String putawayLocationCode) {
		WmsASN asn = load(WmsASN.class,asnId);
		WmsMoveDoc moveDoc = wmsMoveDocManager.createWmsMoveDoc(asn);
		commonDao.store(moveDoc);
		
		List<WmsInventory> inventorys = getReceiveInventoryBySoi(asn.getCode(), itemCode, packageUnitId );
		for(WmsInventory inventory : inventorys) {
			//Double moveQuantityBU = inventory.getAvailableQuantityBU();
			inventory.allocatePickup(allocateQuantity);
			
			String hql = " FROM WmsInventoryExtend invExtend WHERE invExtend.inventory.id = :inventoryId " +
					"AND invExtend.item.code=:itemCode AND invExtend.quantityBU - invExtend.allocatedQuantityBU  > 0 " +
					"and invExtend.pallet =:palletNo ";
			List<WmsInventoryExtend> invExtends = commonDao.findByQuery(hql, 
					new String[]{"inventoryId","palletNo","itemCode"},
					new Object[]{inventory.getId(), palletNo, itemCode});

			for(WmsInventoryExtend invExtend : invExtends){
				Double quantityBU = invExtend.getAvailableQuantityBU();
				WmsMoveDocDetail detail = EntityFactory
						.getEntity(WmsMoveDocDetail.class);
				detail.setMoveDoc(moveDoc);
				detail.setItem(inventory.getItemKey().getItem());
				detail.setItemKey(inventory.getItemKey());
				detail.setInventoryStatus(inventory.getStatus());
				detail.setInventoryId(inventory.getId());
				detail.setFromLocationId(inventory.getLocation().getId());
				detail.setFromLocationCode(inventory.getLocation().getCode());
				detail.setPallet(invExtend.getPallet());
				detail.setCarton(invExtend.getCarton());
				detail.setSerialNo(invExtend.getSerialNo());
				detail.setBePallet(Boolean.TRUE);
				detail.setInventoryId(inventory.getId());
				detail.setPackageUnit(inventory.getPackageUnit());
				detail.addPlanQuantityBU(quantityBU);
				detail.calculateLoad();
				moveDoc.addDetail(detail);
				commonDao.store(detail);
				invExtend.allocatePickup(quantityBU);
			}
		}
		
		// 如果当前上架计划明细数为0，则不创建上架计划
		if (moveDoc.getDetails().size() == 0) {
			commonDao.delete(moveDoc);
			return null;
		}
		else{
			workflowManager.doWorkflow(moveDoc, "wmsMoveDocProcess.new");
			workflowManager.doWorkflow(asn, "wmsASNPutawayProcess.createMoveDoc");
		}
		try {
			for (WmsMoveDocDetail detail : moveDoc.getDetails()) {
				if(putawayLocationCode == null) {
					WmsTask task = wmsTaskManager.createWmsTask(detail,detail.getItemKey(),
							detail.getInventoryStatus(), detail.getPlanQuantityBU());
					resetAllocate(task.getId());
					detail.allocate(task.getPlanQuantityBU());
				} else {
					String locHql = "FROM WmsLocation loc where loc.code = :code and loc.warehouse.id = :warehouseId";
					WmsLocation loc = (WmsLocation)this.commonDao.findByQueryUniqueResult(locHql, new String[]{"code", "warehouseId"}, 
							new Object[]{putawayLocationCode, WmsWarehouseHolder.getWmsWarehouse().getId()});
					if (loc == null) {
						throw new BusinessException("无此上架库位");
					}
					wmsMoveDocManager.manualAllocate(detail, loc.getId(), detail.getPlanQuantityBU());
				}
				// 更新上架单及明细数量
				commonDao.store(detail);
			}
			if(putawayLocationCode == null) {
				workflowManager.doWorkflow(moveDoc, "wmsMoveDocProcess.allocate");
			}
			
			moveDoc = load(WmsMoveDoc.class,moveDoc.getId());
			if(WmsMoveDocStatus.ALLOCATED.equals(moveDoc.getStatus())){
				workflowManager.doWorkflow(moveDoc, "wmsMoveDocProcess.active");
			}
		} catch (RFFinishException e) {
			e.printStackTrace();
			throw new RFFinishException(e.getMessage());
		} 
		catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
		}
		return moveDoc;
	}
	
	private List<WmsInventory> getReceiveInventoryBySoi(String soi, String itemCode, Long packageUnitId) {
		String hql = " FROM WmsInventory inv WHERE inv.location.type = 'RECEIVE' AND inv.itemKey.lotInfo.soi = :soi " +
				"AND inv.itemKey.item.code=:itemCode AND inv.quantityBU - inv.allocatedQuantityBU > 0 " +
				"AND inv.packageUnit.id=:packageUnitId";
		return commonDao.findByQuery(hql, new String[]{"soi","itemCode","packageUnitId"}
				, new Object[]{soi , itemCode, packageUnitId});
	}
	
	public WmsASNDetailDTO getWmsASNDetailDTO(String asnCode, String barCode, Long packageUnitId) throws BusinessException {
		String hql = "FROM WmsASNDetail d where d.asn.code = :asnCode and d.item.barCode = :barCode " +
				" and d.receivedQuantityBU - d.movedQuantityBU > 0 and d.packageUnit.id = :packageUnitId";
		
		List asnDetailList = commonDao.findByQuery(hql, new String[]{"asnCode", "barCode", "packageUnitId"}
				, new Object[]{asnCode, barCode, packageUnitId});
		int detailSize = asnDetailList.size();
		if (detailSize == 0) {
			throw new BusinessException("当前ASN明细中无待上架的货品！");
		}
		
		WmsASNDetail detail = (WmsASNDetail)asnDetailList.get(0);
		WmsASN asn = commonDao.load(WmsASN.class, detail.getAsn().getId());
		WmsItem item = commonDao.load(WmsItem.class, detail.getItem().getId());
		WmsLotRule lotRule = commonDao.load(WmsLotRule.class, item.getLotRule().getId());
		WmsPackageUnit currentPackageUnit = commonDao.load(WmsPackageUnit.class, detail.getPackageUnit().getId());
		
		WmsBooking booking = detail.getBooking();
		if (booking == null) {
			throw new BusinessException("未预约收货月台,请预约");
		}
		WmsDock dock = commonDao.load(WmsDock.class, booking.getDock().getId());
		WmsLocation location = commonDao.load(WmsLocation.class, dock.getReceiveLocationId());
		
		WmsASNDetailDTO dto = new WmsASNDetailDTO();
		dto.setAsnId(detail.getAsn().getId());
		dto.setAsnCode(asn.getCode());
		dto.setAsnDetailId(detail.getId());
		dto.setItemCode(item.getCode());
		dto.setItemName(item.getName());
		dto.setLocationId(location.getId());
		dto.setLocationCode(location.getCode());
		dto.setCurrentPackageUnit(detail.getPackageUnit().getId());
		
		dto.setPackageUnitName(currentPackageUnit.getUnit());
		Double unMovedQuantityBU = detail.getReceivedQuantityBU() - detail.getMovedQuantityBU();

		dto.setPackageQuantity(PackageUtils.convertPackQuantity(unMovedQuantityBU, currentPackageUnit.getConvertFigure(), item.getPrecision()));
		
		String puHql = "FROM WmsPackageUnit pu where pu.item.id = :itemId and pu.id != :currentPackageUnitId order by pu.lineNo DESC ";
		List<WmsPackageUnit> otherPackageUnitList = commonDao.findByQuery(puHql, 
				new String[]{"itemId", "currentPackageUnitId"}, new Object[]{item.getId(), detail.getPackageUnit().getId()});
		if (!otherPackageUnitList.isEmpty()) {
			dto.getPackageUnitList().addAll(otherPackageUnitList);
		}
		dto.getPackageUnitList().add(currentPackageUnit);
		
		dto.setExpectedQuantityBU(detail.getExpectedQuantityBU() - detail.getReceivedQuantityBU());
		String itemStateHql = "FROM WmsItemState s where s.company.id = :companyId and s.status = 'ENABLED' and s.beReceive = true";
		dto.getStatusList().addAll(commonDao.findByQuery(itemStateHql, "companyId", asn.getCompany().getId()));
		dto.setLotRule(lotRule);
		if (detail.getLotInfo() == null) {
			dto.setLotInfo(new LotInfo());
		} else {
			if (detailSize > 1) {
				dto.setLotInfo(new LotInfo());
			} else {
				dto.setLotInfo(detail.getLotInfo());
			}
		}
		return dto;
	}
	
	/**
	 * 根据收货单获得最早创建的未完成且无托盘的task
	 * */
	public WmsTask getPutawayTaskByAsnCode(String asnCode, Boolean bePallet){
		String hql = "SELECT task.id FROM WmsTask task WHERE task.itemKey.lotInfo.soi=:asnCode AND task.status<>'FINISHED' " +
				"AND task.type='MV_PUTAWAY' ";
		if(bePallet) {
			hql += "AND task.pallet!='-'";
		} else {
			hql += "AND task.pallet='-'";
		} 
		hql += "ORDER BY task.id";
		List<Long> taskIds = commonDao.findByQuery(hql,"asnCode",asnCode);
		if(taskIds == null || taskIds.size() <= 0){
			return null;
		}
		return load(WmsTask.class,taskIds.get(0));
	}
	
	/**
	 * 根据Task获得上架单
	 * */
	public WmsMoveDoc getWmsMoveDocByTaskId(Long taskId){
		WmsTask task = load(WmsTask.class,taskId);
		return load(WmsMoveDoc.class,task.getMoveDocDetail().getMoveDoc().getId());
	}
	
	/**
	 * 根据task获得上架信息
	 * */
	public Map<String,Object> getInfoByTask(long taskId) {
		Map<String,Object> result = new HashMap<String, Object>();
		WmsTask task = load(WmsTask.class,taskId);
		result.put("barCode", task.getItemKey().getItem().getBarCode());
		result.put("itemCode", task.getItemKey().getItem().getCode()+"-"+task.getItemKey().getItem().getName());
		result.put("packageUnit", task.getPackageUnit().getUnit());
		result.put("putawayQty",PackageUtils.convertPackQuantity(task.getPlanQuantityBU()-task.getMovedQuantityBU(),task.getPackageUnit()));
		result.put("toLocation",task.getToLocationCode());
		result.put("toLocationId",task.getToLocationId());
		result.put("pallet",task.getPallet());
		return result;
	}

	public Map<String, Object> getUnPutawayItemInfoByAsnAndBarCode(
			String asnCode, String barCode, String unit) {
		Map<String,Object> result = new HashMap<String, Object>();
		String hql = "SELECT distinct inventoryExtend.inventory.id," +
				" inventoryExtend.item.code," +
				" inventoryExtend.item.name," +
				" inventoryExtend.inventory.packageUnit.id," +
				" inventoryExtend.inventory.packageUnit.unit," +
				" (inventoryExtend.inventory.quantity-inventoryExtend.inventory.allocatedQuantityBU/inventoryExtend.inventory.packageUnit.convertFigure) " +
				" FROM WmsInventoryExtend inventoryExtend " +
				" WHERE inventoryExtend.inventory.itemKey.lotInfo.soi=:asnCode" +
				" AND inventoryExtend.item.barCode=:barCode " +
				" AND inventoryExtend.pallet='-' " +
				" AND inventoryExtend.inventory.location.type='RECEIVE' " +
				" AND (inventoryExtend.inventory.quantity-inventoryExtend.inventory.allocatedQuantityBU/inventoryExtend.inventory.packageUnit.convertFigure)>0";
		if(!StringUtils.isEmpty(unit)){
			hql += " AND inventoryExtend.inventory.packageUnit.unit='"+unit+"' ";
		}
		hql += " ORDER BY inventoryExtend.inventory.packageUnit.lineNo";
		
		List<Object[]> list = commonDao.findByQuery(hql,new String[]{"asnCode","barCode"},new Object[]{asnCode,barCode});
		if(list == null || list.size() <= 0)
			throw new BusinessException("未找到收货单["+asnCode+"]下待上架物料[条码:"+barCode+"]");
		
		//取一个
		result.put("inventoryId", list.get(0)[0]);
		result.put("itemCode", list.get(0)[1]);
		result.put("itemName", list.get(0)[2]);
		result.put("packageId", list.get(0)[3]);
		result.put("packageUnit", list.get(0)[4]);
		result.put("itemQty", list.get(0)[5]);
		
		return result;
	}

	/**
	 * 获得指定收货单指定条码物料的包装信息
	 * */
	public List<WmsPackageUnit> getUnPutawayItemUnitByAsnAndBarCode(
			String asnCode, String barCode) {
		String hql = "SELECT distinct inventoryExtend.inventory.packageUnit "+
			" FROM WmsInventoryExtend inventoryExtend " +
			" WHERE inventoryExtend.inventory.itemKey.lotInfo.soi=:asnCode" +
			" AND inventoryExtend.item.barCode=:barCode " +
			" AND inventoryExtend.pallet='-' " +
			" AND inventoryExtend.inventory.location.type='RECEIVE' " +
			" AND (inventoryExtend.inventory.quantityBU-inventoryExtend.inventory.allocatedQuantityBU/inventoryExtend.inventory.packageUnit.convertFigure)>0" +
			" ORDER BY inventoryExtend.inventory.packageUnit.lineNo";

		List<WmsPackageUnit> unitList = commonDao.findByQuery(hql,new String[]{"asnCode","barCode"},new Object[]{asnCode,barCode});
		return unitList;
	}

	/**
	 * 判断ASN是否存在并判断是否存在未码托待上架物料
	 * @param code 收货单号
	 * */
	public void findAsnByCode(String code) {
		WmsASN asn = (WmsASN)commonDao.findByQueryUniqueResult("FROM WmsASN asn WHERE asn.code=:code AND asn.shelvesStauts<>:shelvesStauts",
				new String[]{"code","shelvesStauts"},new Object[]{code,WmsASNShelvesStauts.FINISHED});
		if(asn == null)
			throw new BusinessException("收货单["+code+"]不存在或已上架");
		
		if(WmsASNQualityStauts.UNQUALITY.equals(asn.getQualityStauts()) && !asn.isQualityPutaway())
			if(!asnManager.isExistUnQuality(asn))
				throw new BusinessException("存在未质检完成的记录，请先处理！");
		
		String hql = "SELECT COUNT(*) FROM WmsInventoryExtend inventoryExtend " +
				" WHERE inventoryExtend.inventory.itemKey.lotInfo.soi=:asnCode " +
				" AND inventoryExtend.inventory.location.type='RECEIVE' " +
				" AND inventoryExtend.quantityBU-inventoryExtend.allocatedQuantityBU>0" +
				" AND inventoryExtend.pallet='-'";
		
		Long count = (Long)commonDao.findByQueryUniqueResult(hql, "asnCode", code);
		if(count == null || count <=0)
			throw new BusinessException("未找到收货单["+code+"]下未码托待上架的库存");
	}

	/**
	 * 根据收货单号创建上架单
	 * */
	public WmsMoveDoc createWmsMoveDoc(Long moveDocId,String asnCode,Long inventoryId,Long packageId,Double putawayQuantity){
		WmsMoveDoc moveDoc = null;
		WmsASN asn = (WmsASN)commonDao.findByQueryUniqueResult("FROM WmsASN asn WHERE asn.code=:code", "code", asnCode);
		if(moveDocId != null){
			moveDoc = load(WmsMoveDoc.class,moveDocId);
			//如果上架单未分配完则让操作员输入库位分配
			if(moveDoc.getPlanQuantityBU() > moveDoc.getAllocatedQuantityBU()){
				return moveDoc;
			}
		}else{
			if(WmsASNQualityStauts.UNQUALITY.equals(asn.getQualityStauts()) && !asn.isQualityPutaway()) {
				if(!isExistUnQuality(asn)) {
					throw new BusinessException("存在未质检完成的记录，请先处理！");
				}
			}
			moveDoc = wmsMoveDocManager.createWmsMoveDoc(asn);
		}
		workflowManager.doWorkflow(moveDoc, "wmsMoveDocProcess.new");
		
		//明细
		WmsInventory inv = load(WmsInventory.class,inventoryId);
		WmsPackageUnit packageUnit = load(WmsPackageUnit.class,packageId);
		
		Double moveQuantityBU = PackageUtils.convertBUQuantity(putawayQuantity, packageUnit);
		inv.allocatePickup(moveQuantityBU);
		//必须是未码托的物料
		String hql = " FROM WmsInventoryExtend invExtend WHERE invExtend.inventory.id = :inventoryId " +
				" AND invExtend.quantityBU - invExtend.allocatedQuantityBU  > 0 AND invExtend.pallet='-'";
		List<WmsInventoryExtend> invExtends = commonDao.findByQuery(hql, "inventoryId", inv.getId());

		for(WmsInventoryExtend invExtend : invExtends){
			if(moveQuantityBU <= 0.0){
				break;
			}
			boolean f = true;
			Double quantityBU = invExtend.getAvailableQuantityBU();
			Double addQuantity = (moveQuantityBU <= quantityBU) ? moveQuantityBU : quantityBU;
			for (WmsMoveDocDetail wmdd : moveDoc.getDetails()) {
				if (wmdd.getFromLocationId().equals(inv.getLocation().getId())
						&& wmdd.getPallet().equals(invExtend.getPallet())
						&& wmdd.getCarton().equals(invExtend.getCarton())
						&& wmdd.getSerialNo().equals(invExtend.getSerialNo())
						&& wmdd.getItemKey().getId().equals(inv.getItemKey().getId())
						&& wmdd.getInventoryStatus().equals(inv.getStatus())
						&& wmdd.getPackageUnit().getId().equals(inv.getPackageUnit().getId())
						&& wmdd.getInventoryId().equals(inv.getId())
						&& wmdd.getAllocatedQuantityBU()<=0.0) {
					wmdd.addPlanQuantityBU(addQuantity);
					moveDoc.refreshQuantity();
					f = false;
					break;
				}
			}
			if(f){
				// 没找到明细，创建新明细
				WmsMoveDocDetail detail = EntityFactory
						.getEntity(WmsMoveDocDetail.class);
				detail.setMoveDoc(moveDoc);
				detail.setItem(inv.getItemKey().getItem());
				detail.setItemKey(inv.getItemKey());
				detail.setInventoryStatus(inv.getStatus());
				detail.setInventoryId(inv.getId());
				detail.setFromLocationId(inv.getLocation().getId());
				detail.setFromLocationCode(inv.getLocation().getCode());
				detail.setPallet(invExtend.getPallet());
				detail.setCarton(invExtend.getCarton());
				detail.setSerialNo(invExtend.getSerialNo());
				if (BaseStatus.NULLVALUE.equals(detail.getPallet())) {
					detail.setBePallet(Boolean.FALSE);
				} else {
					detail.setBePallet(Boolean.TRUE);
				}
				detail.setInventoryId(inv.getId());
				detail.setPackageUnit(inv.getPackageUnit());
				detail.addPlanQuantityBU(addQuantity);
				detail.calculateLoad();
				moveDoc.addDetail(detail);
			}
			invExtend.allocatePickup(addQuantity);
			moveQuantityBU -= addQuantity;
		}
		
		if(moveDoc.getDetails() == null || moveDoc.getDetails().size() <=0){
			commonDao.delete(moveDoc);
			return null;
		}else{
			workflowManager.doWorkflow(asn, "wmsASNPutawayProcess.createMoveDoc");
			try {
				//明细自动分配
				workflowManager.sendMessage(moveDoc, "wmsMoveDocProcess.autoAllocate");
			} catch (Exception e){
				e.printStackTrace();
			}
			
			return moveDoc;
		}
	}

	/**
	 * 生效上架单
	 * */
	public void activeMoveDoc(Long moveDocId){
		WmsMoveDoc moveDoc = load(WmsMoveDoc.class,moveDocId);
		if(WmsMoveDocStatus.ALLOCATED.equals(moveDoc.getStatus())
				||WmsMoveDocStatus.PARTALLOCATED.equals(moveDoc.getStatus())){
			moveDoc.setWorker(WmsWorkerHolder.getWmsWorker());
			workflowManager.doWorkflow(moveDoc, "wmsMoveDocProcess.active");
		}
	}
	
	/**
	 * 指定库位分配上架单
	 * */
	public WmsMoveDoc createMoveDocWithDescLocation(Long moveDocId,String asnCode,Long inventoryId,Long packageId,Double putawayQuantity,String toLocation){
		WmsMoveDoc moveDoc = null;
		WmsASN asn = (WmsASN)commonDao.findByQueryUniqueResult("FROM WmsASN asn WHERE asn.code=:code", "code", asnCode);
		if(moveDocId != null){
			moveDoc = load(WmsMoveDoc.class,moveDocId);
			//如果上架单未分配完则让操作员输入库位分配
			if(moveDoc.getPlanQuantityBU() > moveDoc.getAllocatedQuantityBU()){
				return moveDoc;
			}
		}else{
			if(WmsASNQualityStauts.UNQUALITY.equals(asn.getQualityStauts()) && !asn.isQualityPutaway()) {
				if(!isExistUnQuality(asn)) {
					throw new BusinessException("存在未质检完成的记录，请先处理！");
				}
			}
			moveDoc = wmsMoveDocManager.createWmsMoveDoc(asn);
		}
		workflowManager.doWorkflow(moveDoc, "wmsMoveDocProcess.new");
		
		//明细
		WmsInventory inv = load(WmsInventory.class,inventoryId);
		WmsPackageUnit packageUnit = load(WmsPackageUnit.class,packageId);
		
		Double moveQuantityBU = PackageUtils.convertBUQuantity(putawayQuantity, packageUnit);
		inv.allocatePickup(moveQuantityBU);
		//必须是未码托的物料
		String hql = " FROM WmsInventoryExtend invExtend WHERE invExtend.inventory.id = :inventoryId " +
				" AND invExtend.quantityBU - invExtend.allocatedQuantityBU  > 0 AND invExtend.pallet='-'";
		List<WmsInventoryExtend> invExtends = commonDao.findByQuery(hql, "inventoryId", inv.getId());

		for(WmsInventoryExtend invExtend : invExtends){
			if(moveQuantityBU <= 0.0){
				break;
			}
			boolean f = true;
			Double quantityBU = invExtend.getAvailableQuantityBU();
			Double addQuantity = (moveQuantityBU <= quantityBU) ? moveQuantityBU : quantityBU;
			for (WmsMoveDocDetail wmdd : moveDoc.getDetails()) {
				if (wmdd.getFromLocationId().equals(inv.getLocation().getId())
						&& wmdd.getPallet().equals(invExtend.getPallet())
						&& wmdd.getCarton().equals(invExtend.getCarton())
						&& wmdd.getSerialNo().equals(invExtend.getSerialNo())
						&& wmdd.getItemKey().getId().equals(inv.getItemKey().getId())
						&& wmdd.getInventoryStatus().equals(inv.getStatus())
						&& wmdd.getPackageUnit().getId().equals(inv.getPackageUnit().getId())
						&& wmdd.getInventoryId().equals(inv.getId())
						&& wmdd.getAllocatedQuantityBU()<=0.0) {
					wmdd.addPlanQuantityBU(addQuantity);
					moveDoc.refreshQuantity();
					f = false;
					break;
				}
			}
			if(f){
				// 没找到明细，创建新明细
				WmsMoveDocDetail detail = EntityFactory
						.getEntity(WmsMoveDocDetail.class);
				detail.setMoveDoc(moveDoc);
				detail.setItem(inv.getItemKey().getItem());
				detail.setItemKey(inv.getItemKey());
				detail.setInventoryStatus(inv.getStatus());
				detail.setInventoryId(inv.getId());
				detail.setFromLocationId(inv.getLocation().getId());
				detail.setFromLocationCode(inv.getLocation().getCode());
				detail.setPallet(invExtend.getPallet());
				detail.setCarton(invExtend.getCarton());
				detail.setSerialNo(invExtend.getSerialNo());
				if (BaseStatus.NULLVALUE.equals(detail.getPallet())) {
					detail.setBePallet(Boolean.FALSE);
				} else {
					detail.setBePallet(Boolean.TRUE);
				}
				detail.setInventoryId(inv.getId());
				detail.setPackageUnit(inv.getPackageUnit());
				detail.addPlanQuantityBU(addQuantity);
				detail.calculateLoad();
				moveDoc.addDetail(detail);
			}
			invExtend.allocatePickup(addQuantity);
			moveQuantityBU -= addQuantity;
		}
		
		if(moveDoc.getDetails() == null || moveDoc.getDetails().size() <=0){
			commonDao.delete(moveDoc);
			return null;
		}else{
			workflowManager.doWorkflow(asn, "wmsASNPutawayProcess.createMoveDoc");
			try {
				//明细自动分配
				workflowManager.sendMessage(moveDoc, "wmsMoveDocProcess.autoAllocate");
			} catch (Exception e){
				e.printStackTrace();
			}
			
			if(moveDoc.getPlanQuantityBU() > moveDoc.getAllocatedQuantityBU()){
				for(WmsMoveDocDetail moveDocDetail : moveDoc.getDetails()){
					if(moveDocDetail.getAllocatedQuantityBU() <= 0.0){
//						commonDao.deleteAll(arg0)
					}
				}
			}
			
			return moveDoc;
		}
	}
	
	/**
	 * 上架确认
	 * */
	public void confirmPutaway(Long taskId,String toLocationCode) throws BusinessException {
		WmsTask task = load(WmsTask.class,taskId);
		WmsMoveDocDetail mdd = commonDao.load(WmsMoveDocDetail.class, task.getMoveDocDetail().getId());
		WmsMoveDoc moveDoc = commonDao.load(WmsMoveDoc.class, mdd.getMoveDoc().getId());
		WmsLocation toLocation = (WmsLocation)commonDao.findByQueryUniqueResult("FROM WmsLocation loc WHERE loc.code=:code","code",toLocationCode);
		Long toLocationId = toLocation.getId();
		if(toLocation.getCode().equals(task.getToLocationCode())){
			toLocationId = null;
		}
		wmsWorkDocManager.singleWorkConfirm(task,toLocationId, task.getFromLocationId(), 
				task.getPlanQuantityBU(), WmsWorkerHolder.getWmsWorker().getId());
	}

	public void resetAllocate(Long taskId) {
		
		WmsTask task = load(WmsTask.class,taskId);
		WmsMoveDocDetail moveDocDetail = commonDao.load(WmsMoveDocDetail.class, task.getMoveDocDetail().getId());
		WmsMoveDoc moveDoc = moveDocDetail.getMoveDoc();
		
		WmsBillType billType = null;
		if (moveDoc.getOriginalBillType() != null) {
			billType = commonDao.load(WmsBillType.class, moveDoc.getOriginalBillType().getId());
		} else {
			billType = commonDao.load(WmsBillType.class, moveDoc.getBillType().getId());
		}
		
		WmsOrganization company = commonDao.load(WmsOrganization.class, moveDoc.getCompany().getId());
		WmsWarehouse warehouse = commonDao.load(WmsWarehouse.class, moveDoc.getWarehouse().getId());				
		String companyName = company.getName();
		if(company.isBeVirtual()){
			companyName = warehouse.getName();
		}
		String warehouseName = warehouse.getName();
		Long warehouseId = warehouse.getId();
		
		Map<String,Object> problem = new HashMap<String, Object>();
		double planQuantity = 0.0;
		String locationType = "";
		if (moveDocDetail.getBePallet()) {
			planQuantity = 1;
			locationType = "是";
		} else {
			planQuantity = PackageUtils.convertPackQuantity(task.getUnmovedQuantityBU(), task.getPackageUnit());
			locationType = "否";
		}
		
		WmsLocation fromLocation = commonDao.load(WmsLocation.class, task.getFromLocationId());
		WmsPackageUnit packageUnit = commonDao.load(WmsPackageUnit.class, task.getPackageUnit().getId());
		
		//每托数量BU 和规则里一致
		Double palletQuantity = moveDocDetail.getPlanQuantity() * packageUnit.getConvertFigure();
		
		problem.put("仓库ID", warehouseId);					
		problem.put("所属仓库", warehouseName);					
		problem.put("货主", companyName);
		problem.put("单据类型", billType.getName());			
		problem.put("是否托盘", locationType);
		problem.put("货品状态", task.getInventoryStatus());
		problem.put("计划移位数量", planQuantity);
		problem.put("每托数量", palletQuantity);	
		problem.put("批次序号", task.getItemKey().getId());
		problem.put("货品代码", task.getItemKey().getItem().getCode());
		problem.put("货品序号", task.getItemKey().getItem().getId());	
		problem.put("货品分类", task.getItemKey().getItem().getClass1());	
		problem.put("包装单位序号", packageUnit.getId());						
		problem.put("货品重量", packageUnit.getWeight());	
		problem.put("货品体积", packageUnit.getVolume());
		problem.put("包装级别", packageUnit.getLevel());
		problem.put("折算系数", packageUnit.getConvertFigure());
		problem.put("月台区号", fromLocation.getZone());			
		problem.put("移出库位ID", fromLocation.getId());			
		problem.put("库区序号", fromLocation.getWarehouseArea().getId());	
		problem.put("越库相关单号", "");
		problem.put("越库已分配数量", 0);
		
		Map<String, Object> result = null;
		try {
			result = wmsRuleManager.execute(warehouseName, companyName, "上架分配规则", problem);				
		} catch (Exception e) {
			throw new RFFinishException(e.getMessage());
		}
		
		//作业任务取消分配
		Double unallocateQty = task.getUnmovedQuantityBU();
		if(task.getToLocationId()!=null) {
			WmsInventory inv = commonDao.load(WmsInventory.class,task.getDescInventoryId());
			inv.unallocatePutaway(unallocateQty);
			wmsInventoryManager.refreshLocationUseRate(inv.getLocation(), 0);
		}
		task.unallocatePick(unallocateQty);
		if(task.getStatus().equals(WmsTaskStatus.WORKING) && task.getMovedQuantityBU()>0) {
			workflowManager.doWorkflow(task, "taskProcess.confirm");
		}
		
		int size = ((List<Map<String, Object>>) result.get("返回列表")).size();
		for (int i = 0; i < size; i++) {
			Map<String, Object> wmsTaskInfos = ((List<Map<String, Object>>) result.get("返回列表")).get(i);
			double quantity = Double.valueOf(String.valueOf(wmsTaskInfos.get("上架数量")));
			if (quantity <= 0) {
				continue;
			}
			
			double quantityBU;
			WmsLocation toLoc = commonDao.load(WmsLocation.class,(Long) wmsTaskInfos.get("库位序号"));
			
			if(moveDocDetail.getBePallet()) {
				quantityBU = palletQuantity;
			} else {
				quantityBU = quantity * packageUnit.getConvertFigure();
			}
			double unAllocateQtyBU = moveDocDetail.getUnAllocateQuantityBU();
			double allocateQuantityBU = (unAllocateQtyBU >= quantityBU) ? quantityBU : unAllocateQtyBU;
			
			//托盘上架
			if(moveDocDetail.getBePallet()) {
				quantity--;
				
			} else {
				if(allocateQuantityBU<=0) {
					break;
				}
			}
			
			if (i == 0 && task.getMovedQuantityBU() == 0) {
				putawayAllocate(task, toLoc,allocateQuantityBU,moveDocDetail,false);
			} else {
				putawayAllocate(task, toLoc,allocateQuantityBU,moveDocDetail,true);
			}
		}
	}

	public List<WmsPackageUnit> getPackageUnitList(String asnCode, String barCode) {		
		String hql = "SELECT distinct inventoryExtend.inventory.packageUnit" +
				" FROM WmsInventoryExtend inventoryExtend " +
				" WHERE inventoryExtend.inventory.itemKey.lotInfo.soi=:asnCode" +
				" AND inventoryExtend.item.barCode=:barCode " +
				" AND inventoryExtend.pallet='-' " +
				" AND inventoryExtend.inventory.location.type='RECEIVE' " +
				" AND (inventoryExtend.inventory.quantity-inventoryExtend.inventory.allocatedQuantityBU/inventoryExtend.inventory.packageUnit.convertFigure)>0 " +
				" ORDER BY inventoryExtend.inventory.packageUnit.lineNo desc";
		
		List<WmsPackageUnit> packageUnitList = commonDao.findByQuery(hql,new String[]{"asnCode","barCode"},new Object[]{asnCode,barCode});
		if(packageUnitList == null || packageUnitList.size() == 0)
			throw new BusinessException("未找到收货单["+asnCode+"]下待上架物料[条码:"+barCode+"]");
		return packageUnitList;
	}
	
	
	
//	public String validatePutawayRule(WmsASNDetail asnDetail){
//		Map<String,Object> ruleTableInfo = wmsRuleManager.getRuleTableDetail(WmsWarehouseHolder.getWmsWarehouse().getName(), 
//				new Object[]{"R101_上架_上架规则表",asnDetail.getAsn().getCompany().getName(),
//			asnDetail.getAsn().getBillType().getName(),"是", asnDetail.getPackageUnit().getLevel(),"-"});
//		if(ruleTableInfo == null){ 
//			ruleTableInfo = wmsRuleManager.getRuleTableDetail(WmsWarehouseHolder.getWmsWarehouse().getName(), 
//					new Object[]{"R101_上架_上架规则表",asnDetail.getAsn().getCompany().getName(),
//				asnDetail.getAsn().getBillType().getName(),"是", asnDetail.getPackageUnit().getLevel(),"待检" });
//		} 
//		if(ruleTableInfo == null){ 
//			ruleTableInfo = wmsRuleManager.getRuleTableDetail(WmsWarehouseHolder.getWmsWarehouse().getName(), 
//					new Object[]{"R101_上架_上架规则表",asnDetail.getAsn().getCompany().getName(),
//				asnDetail.getAsn().getBillType().getName(),"是", "-","-" });
//		} 
//		if(ruleTableInfo == null){ 
//			return "上架规则表未设置！R101_上架_上架规则表["+asnDetail.getAsn().getCompany().getName()+","+asnDetail.getAsn().getBillType().getName()
//			+",是,"+asnDetail.getPackageUnit().getLevel()+"]";
//		} 
//		return "";
//	}

	/**
	 * 分配到指定库位
	 * */
	public void manualAllocate(Long moveDocId,String locationCode) {
		WmsMoveDoc moveDoc = load(WmsMoveDoc.class,moveDocId);
		WmsLocation toLoc = (WmsLocation)commonDao.findByQueryUniqueResult("FROM WmsLocation loc WHERE loc.code=:code","code",locationCode);
		if(toLoc == null){
			throw new BusinessException("库位不存在");
		}
		for(WmsMoveDocDetail moveDocDetail : moveDoc.getDetails()){
			if(moveDocDetail.getPlanQuantityBU() > moveDocDetail.getAllocatedQuantityBU()){
				wmsMoveDocManager.manualAllocate(moveDocDetail, toLoc.getId(), moveDocDetail.getPlanQuantityBU()-moveDocDetail.getAllocatedQuantityBU());
			}
		}
	}
	
	/**
	 * 如果收货单存在打开状态的上架单,直接删除
	 * */
	public void deleteOPENMoveDoc(String asnCode){
		List<WmsMoveDoc> moveDocList = commonDao.findByQuery("FROM WmsMoveDoc moveDoc WHERE moveDoc.asn.code=:asnCode AND moveDoc.status=:status AND moveDoc.type=:type",
				new String[]{"asnCode","status","type"},new Object[]{asnCode,WmsMoveDocStatus.OPEN,WmsMoveDocType.MV_PUTAWAY});
		if(moveDocList != null && moveDocList.size() > 0){
			for(WmsMoveDoc moveDoc : moveDocList){
				wmsMoveDocManager.cancelMoveDoc(moveDoc);
				workflowManager.doWorkflow(moveDoc, "wmsMoveDocProcess.delete");
			}
		}
	}
	
	public Integer putawayAutoAllocate(String asnCode){
		WmsASN asn = (WmsASN) commonDao.findByQueryUniqueResult("FROM WmsASN asn WHERE asn.code =:code"
				+ " AND (asn.autoAllocateStauts =:autoAllocateStauts1 OR asn.autoAllocateStauts =:autoAllocateStauts2)"
				+ " AND (asn.status > 'CANCELED' OR asn.status < 'CANCELED')", 
				new String[]{"code","autoAllocateStauts1","autoAllocateStauts2"}, 
				new Object[]{asnCode,WmsMoveDocStatus.OPEN,WmsMoveDocStatus.PARTALLOCATED});
		if(asn==null){
			asn = (WmsASN) commonDao.findByQueryUniqueResult("FROM WmsASN asn WHERE asn.relatedBill1 =:relatedBill1"
					+ " AND (asn.autoAllocateStauts =:autoAllocateStauts1 OR asn.autoAllocateStauts =:autoAllocateStauts2)"
					+ " AND (asn.status > 'CANCELED' OR asn.status < 'CANCELED')", 
					new String[]{"relatedBill1","autoAllocateStauts1","autoAllocateStauts2"}, 
					new Object[]{asnCode,WmsMoveDocStatus.OPEN,WmsMoveDocStatus.PARTALLOCATED});
			if(asn==null){
				return 0;
			}
		}
		asnManager.putawayAutoAllocate(asn);
		return 1;
		
	}
	public String palletSerial(String palletNo){
		List<Long> jpsIds = commonDao.findByQuery("SELECT jps.id FROM JacPalletSerial jps"
				+ " WHERE jps.palletNo =:palletNo AND jps.bePutawayAuto = true", 
				new String[]{"palletNo"}, new Object[]{palletNo});
		JacPalletSerial jps = null;
		if(jpsIds!=null && jpsIds.size()>0){
			jps = commonDao.load(JacPalletSerial.class, jpsIds.get(0));
		}
		if(jps==null){
			return null;
		}
		return jps.getToLocationCode()+MyUtils.spilt1+jps.getId();
	}
	public String shelvesConfirm(String palletNos,String locationCode){
		String[] plt = palletNos.split(MyUtils.spilt1);
		JacPalletSerial jps = commonDao.load(JacPalletSerial.class, Long.parseLong(plt[1]));
		String hql = "SELECT task.id FROM WmsTask task WHERE task.originalBillCode =:billCode AND task.pallet =:pallet"
				+ " AND task.type =:type AND task.status =:status";
		List<Long> taskIds = commonDao.findByQuery(hql, 
				new String[]{"billCode","pallet","type","status"}, 
				new Object[]{jps.getAsnDetail().getAsn().getCode(),
				jps.getPalletNo(),WmsMoveDocType.MV_PUTAWAY,WmsTaskStatus.OPEN});
		WmsTask task = null;
		if(taskIds!=null && taskIds.size()>0){
			Long locationId = 0L;
			if(!locationCode.equals(jps.getToLocationCode())){//改变目标库位
				WmsLocation loc = (WmsLocation) commonDao.findByQueryUniqueResult("FROM WmsLocation loc WHERE loc.code =:code"
						+ " AND loc.warehouse.id =:warehouseId"
						+ " AND loc.status = 'ENABLED' AND loc.lockCount = false  AND loc.type = 'STORAGE' ", ///AND loc.palletQuantity=0  modified by yuan.sun  for RF 
						new String[]{"code","warehouseId"},  	
						new Object[]{locationCode,WmsWarehouseHolder.getWmsWarehouse().getId()});
				if(loc==null){
					return "loc is null";
				}
				locationId = loc.getId();
			}
			for(Long taskId : taskIds){
				task = commonDao.load(WmsTask.class,taskId);
				asnManager.singleConfirm(task, jps.getAsnDetail().getAsn().getId(), locationId, 
						WmsWorkerHolder.getWmsWorker().getId());
			}
		}else{
			return "task is null";
		}
		return "end";
		
	}
}
