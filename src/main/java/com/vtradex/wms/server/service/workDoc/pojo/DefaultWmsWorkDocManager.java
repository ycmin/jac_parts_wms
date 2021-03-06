package com.vtradex.wms.server.service.workDoc.pojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import com.vtradex.thorn.client.ui.table.RowData;
import com.vtradex.thorn.server.exception.BusinessException;
import com.vtradex.thorn.server.model.EntityFactory;
import com.vtradex.thorn.server.model.message.Task;
import com.vtradex.thorn.server.model.message.TaskStatus;
import com.vtradex.thorn.server.service.WorkflowManager;
import com.vtradex.thorn.server.service.pojo.DefaultBaseManager;
import com.vtradex.thorn.server.util.BeanUtils;
import com.vtradex.thorn.server.util.LocalizedMessage;
import com.vtradex.wms.client.scanBol.EditWmsScanBol;
import com.vtradex.wms.client.scanBol.businessObject.BusinessNode;
import com.vtradex.wms.server.action.PickTicketBaseShipDecision;
import com.vtradex.wms.server.model.base.BaseStatus;
import com.vtradex.wms.server.model.inventory.WmsInventory;
import com.vtradex.wms.server.model.inventory.WmsInventoryExtend;
import com.vtradex.wms.server.model.inventory.WmsInventoryLogType;
import com.vtradex.wms.server.model.inventory.WmsQualityMoveSoiLog;
import com.vtradex.wms.server.model.move.WmsBoxDetail;
import com.vtradex.wms.server.model.move.WmsMoveDoc;
import com.vtradex.wms.server.model.move.WmsMoveDocDetail;
import com.vtradex.wms.server.model.move.WmsMoveDocShipStatus;
import com.vtradex.wms.server.model.move.WmsMoveDocType;
import com.vtradex.wms.server.model.move.WmsTask;
import com.vtradex.wms.server.model.move.WmsTaskStatus;
import com.vtradex.wms.server.model.move.WmsWorkDoc;
import com.vtradex.wms.server.model.move.WmsWorkDocStatus;
import com.vtradex.wms.server.model.organization.WmsBillType;
import com.vtradex.wms.server.model.organization.WmsStationAndItem;
import com.vtradex.wms.server.model.shipping.WmsBOL;
import com.vtradex.wms.server.model.shipping.WmsBOLStateLog;
import com.vtradex.wms.server.model.shipping.WmsMoveDocAndStation;
import com.vtradex.wms.server.model.shipping.WmsPickContainer;
import com.vtradex.wms.server.model.shipping.WmsPickTicket;
import com.vtradex.wms.server.model.shipping.WmsPickTicketAndAppliance;
import com.vtradex.wms.server.model.shipping.WmsPickTicketDetail;
import com.vtradex.wms.server.model.shipping.WmsTaskAndStation;
import com.vtradex.wms.server.model.shipping.WmsWaveDoc;
import com.vtradex.wms.server.model.shipping.WmsWaveDocDetail;
import com.vtradex.wms.server.model.shipping.WmsWaveDocWorkMode;
import com.vtradex.wms.server.model.warehouse.WmsBoxType;
import com.vtradex.wms.server.model.warehouse.WmsLocation;
import com.vtradex.wms.server.model.warehouse.WmsLocationType;
import com.vtradex.wms.server.model.warehouse.WmsWarehouse;
import com.vtradex.wms.server.model.warehouse.WmsWarehouseArea;
import com.vtradex.wms.server.model.warehouse.WmsWorkArea;
import com.vtradex.wms.server.service.inventory.WmsInventoryExtendManager;
import com.vtradex.wms.server.service.inventory.WmsInventoryManager;
import com.vtradex.wms.server.service.rule.WmsRuleManager;
import com.vtradex.wms.server.service.rule.WmsTransactionalManager;
import com.vtradex.wms.server.service.sequence.WmsBussinessCodeManager;
import com.vtradex.wms.server.service.task.WmsTaskManager;
import com.vtradex.wms.server.service.workDoc.WmsWorkDocManager;
import com.vtradex.wms.server.utils.JavaTools;
import com.vtradex.wms.server.utils.MyUtils;
import com.vtradex.wms.server.utils.WmsTables;
import com.vtradex.wms.server.web.filter.WmsWarehouseHolder;

/**
 * 作业单管理
 *
 * @category Manager
 * @author <a href="brofe.pan@gmail.com">潘宁波</a>
 * @version $Revision: 1.39 $Date: 2016/10/25 01:31:27 $
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class DefaultWmsWorkDocManager extends DefaultBaseManager implements WmsWorkDocManager {
	
	protected WorkflowManager workflowManager;
	protected WmsBussinessCodeManager wmsBussinessCodeManager;
	protected WmsRuleManager wmsRuleManager;
	protected WmsTransactionalManager transactionalManager;
	protected WmsInventoryManager wmsInventoryManager;
	protected WmsInventoryExtendManager inventoryExtendManager;
	private JdbcTemplate jdbcTemplate;
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public DefaultWmsWorkDocManager(WorkflowManager workflowManager, 
			WmsBussinessCodeManager wmsBussinessCodeManager, WmsRuleManager wmsRuleManager,
			WmsTransactionalManager transactionalManager, WmsInventoryManager wmsInventoryManager
			,WmsInventoryExtendManager inventoryExtendManager) {
		this.workflowManager = workflowManager;
		this.wmsBussinessCodeManager = wmsBussinessCodeManager;
		this.wmsRuleManager = wmsRuleManager;
		this.transactionalManager = transactionalManager;
		this.wmsInventoryManager = wmsInventoryManager;
		this.inventoryExtendManager = inventoryExtendManager;
	}
	//wmsWorkDocManager.invokeMethod
	public void invokeMethod(){
		this.addPickTask(commonDao.load(WmsMoveDoc.class, 328293L));
//		this.clearWmsPickContainer(327924L);
	}
	/**
	 * 1.消息驱动；
	 * 2.根据上架分配结果自动创建库内作业单，作业单作业人员随机挑选一个（未来通过调用策略配置）；
	 * 3.暂时一张上架单对应一张作业单,该作业单里包含该发货单产生的所有拣货任务
	 * */
	public WmsMoveDoc presubscriberCreateWorkDocByWmsMoveDoc(List<WmsMoveDoc> wmsMoveDocs){
		return wmsMoveDocs.get(0);
	}
	
	public void confirmAll(Long workDocId, Long workerId) {
		transactionalManager.confirmAll(workDocId, workerId);
	}
	
	public void singleWorkConfirm(WmsTask task, Long toLocationId, Long fromLocationId, Double quantityBU, Long workerId) {
		transactionalManager.singleWorkConfirm(task, toLocationId, fromLocationId, quantityBU, workerId);
	}
	
	public Map<String,Object> getTaskListByMoveDoc(Long moveDocId, boolean isGroup) {
		String hql = "SELECT task FROM WmsTask task, WmsLocation location WHERE task.fromLocationId = location.id and task.status = :status " +
				"AND task.moveDocDetail.moveDoc.id = :moveDocId ORDER BY location.routeNo ASC";
		Map<String,Object> groupMap = new HashMap<String, Object>();
		List<WmsTask> tasks = commonDao.findByQuery(hql,new String[]{"status","moveDocId"},new Object[]{WmsTaskStatus.OPEN,moveDocId});
		if(isGroup) {
			for (WmsTask task : tasks) {
				Map taskMap = new HashMap();
				taskMap.put("任务ID", task.getId());
				WmsLocation fromLoc = commonDao.load(WmsLocation.class, task.getFromLocationId());
				WmsWarehouseArea fromArea = fromLoc.getWarehouseArea();
				taskMap.put("移出库位ID", task.getFromLocationId());
				taskMap.put("移出库位编码", task.getFromLocationCode());
				taskMap.put("移出区", fromLoc.getZone());
				taskMap.put("移出排", fromLoc.getLine());
				taskMap.put("移出库区ID", fromArea.getId());
				taskMap.put("移出库区编码", fromArea.getCode());
				taskMap.put("移出库区名称", fromArea.getName());
				
				if (task.getToLocationId() != null) {
					WmsLocation toLoc = commonDao.load(WmsLocation.class, task.getToLocationId());
					WmsWarehouseArea toArea = toLoc.getWarehouseArea();
					taskMap.put("移入库位ID", task.getToLocationId());
					taskMap.put("移入库位编码", task.getToLocationCode());
					taskMap.put("移入区", toLoc.getZone());
					taskMap.put("移入排", toLoc.getLine());
					taskMap.put("移入库区ID", toArea.getId());
					taskMap.put("移入库区编码", toArea.getCode());
					taskMap.put("移入库区名称", toArea.getName());
				}
				taskMap.put("件数", task.getPlanQuantityBU());
				taskMap.put("重量", task.getPlanWeight());
				taskMap.put("体积", task.getPlanVolume());
				
				Object orderObj = groupMap.get("订单");
				if (orderObj == null) {
					Map orderMap = new HashMap();
					orderMap.put("单号", task.getRelatedBill());
					
					List taskList = new ArrayList();
					taskList.add(taskMap);
					orderMap.put("任务", taskList);
					
					List orderList = new ArrayList();
					orderList.add(orderMap);
					
					groupMap.put("组号", task.getStatus());
					groupMap.put("订单", orderList);
				} else {
					List orderList = (List)orderObj;
					Map orderMap = null;
					Boolean flag = Boolean.FALSE;
					for (int i = 0; i < orderList.size(); i++) {
						orderMap = (Map)orderList.get(i);
						if(orderMap.get("单号").equals(task.getRelatedBill())) {
							flag = Boolean.TRUE;
							break;
						}
					}
					if (flag) {
						List taskList = (List)orderMap.get("任务");
						taskList.add(taskMap);
					} else {
						Map newOrderMap = new HashMap();
						newOrderMap.put("单号", task.getRelatedBill());
						
						List taskList = new ArrayList();
						taskList.add(taskMap);
						newOrderMap.put("任务", taskList);
						
						orderList.add(newOrderMap);
					}
				}
			}
		} else {
			for (WmsTask task : tasks) {
				groupMap.put("组号", task.getStatus());
				
				Map taskMap = new HashMap();
				taskMap.put("任务ID", task.getId());
				
				WmsLocation fromLoc = commonDao.load(WmsLocation.class, task.getFromLocationId());
				WmsWarehouseArea fromArea = fromLoc.getWarehouseArea();
				taskMap.put("移出库位ID", task.getFromLocationId());
				taskMap.put("移出库位编码", task.getFromLocationCode());
				taskMap.put("移出区", fromLoc.getZone());
				taskMap.put("移出排", fromLoc.getLine());
				taskMap.put("移出库区ID", fromArea.getId());
				taskMap.put("移出库区编码", fromArea.getCode());
				taskMap.put("移出库区名称", fromArea.getName());
				
				if (task.getToLocationId() != null) {
					WmsLocation toLoc = commonDao.load(WmsLocation.class, task.getToLocationId());
					WmsWarehouseArea toArea = toLoc.getWarehouseArea();
					taskMap.put("移入库位ID", task.getToLocationId());
					taskMap.put("移入库位编码", task.getToLocationCode());
					taskMap.put("移入区", toLoc.getZone());
					taskMap.put("移入排", toLoc.getLine());
					taskMap.put("移入库区ID", toArea.getId());
					taskMap.put("移入库区编码", toArea.getCode());
					taskMap.put("移入库区名称", toArea.getName());
				}
				
				Object taskObj = groupMap.get("任务");
				if (taskObj == null) {
					List taskList = new ArrayList();
					taskList.add(taskMap);
					groupMap.put("任务", taskList);
				} else {
					List taskList = (List)taskObj;
					taskList.add(taskMap);
				}
			}
		}
		return groupMap;
	}
	
	/**
	 * 创建作业单
	 * 
	 * @param moveDoc 移位单
	 * @exception BusinessException
	 */
	public void createWorkDocByRule(WmsMoveDoc moveDoc) throws BusinessException {
		moveDoc = commonDao.load(WmsMoveDoc.class, moveDoc.getId());
		
		Map<String, Object> problem = new HashMap<String, Object>();
		problem.put("仓库", moveDoc.getWarehouse().getName());
		problem.put("移位单序号", moveDoc.getId());
		problem.put("移位单号", moveDoc.getCode());
		String type = "收货上架";
		Boolean isGroup = Boolean.FALSE;
		if (moveDoc.isPutawayType()) {
			type = "收货上架";
		} else if (moveDoc.isPickTicketType()) {
			type = "发货拣货";
			workByDocProblem(problem,moveDoc);
			isGroup = Boolean.TRUE;
		} else if (moveDoc.isWaveType()) {
			type = "波次拣货";
			WmsWaveDoc waveDoc = load(WmsWaveDoc.class,moveDoc.getWaveDoc().getId());
			if(WmsWaveDocWorkMode.WORK_BY_DOC.equals(waveDoc.getWorkMode())){
				problem.put("作业模式", WmsWaveDocWorkMode.WORK_BY_DOC_KEY);
				workByDocProblem(problem,moveDoc);
				isGroup = Boolean.TRUE;
			}
			else{
				problem.put("作业模式", WmsWaveDocWorkMode.WORK_BY_WAVE_KEY);
			}
		} else if (moveDoc.isProcessType()) {
			type = "加工拣货";
		} else if (moveDoc.isMoveType()) {
			type = "库内移位";
		} else if (moveDoc.isReplenishmentType()) {
			type = "补货移位";
		}
		problem.put("类型", type);
		problem.put("作业任务集", this.getTaskListByMoveDoc(moveDoc.getId(), isGroup));
		
		Map<String, Object> resultMap = wmsRuleManager.execute(moveDoc.getWarehouse().getName(), moveDoc.getWarehouse().getName(), "作业单生成规则", problem);
		
		doCreateWorkDocResult(moveDoc, resultMap);
	}
	
	private void workByDocProblem(Map<String, Object> problem,WmsMoveDoc moveDoc){
		List<Map<String,Object>> workDocList = new ArrayList<Map<String,Object>>();
		String hql = "FROM WmsWorkDoc wwd WHERE wwd.warehouse.id = :warehouseId  AND wwd.status = :status AND wwd.type = :type";
		List<WmsWorkDoc> workDocs = commonDao.findByQuery(hql, new String[]{"warehouseId","status","type"}, new Object[]{moveDoc.getWarehouse().getId(),WmsWorkDocStatus.OPEN,moveDoc.getType()});
		for(WmsWorkDoc wwd : workDocs){
			Map<String,Object> workDocMap = new HashMap<String, Object>();
			workDocMap.put("作业单序号", wwd.getId());
			workDocMap.put("作业单编码", wwd.getCode());
			workDocMap.put("工作区序号", wwd.getWorkArea().getId());
			workDocMap.put("工作区编码", wwd.getWorkArea().getCode());
			
			hql = "SELECT COUNT(DISTINCT task.relatedBill) FROM WmsTask task WHERE task.workDoc.id = :workDocId";
			Long orderCount = (Long)commonDao.findByQueryUniqueResult(hql, "workDocId", wwd.getId());
			workDocMap.put("单数", orderCount == null ? 0L : orderCount);
			workDocMap.put("件数", wwd.getExpectedQuantityBU());
			
			hql = "SELECT SUM(task.planWeight),SUM(task.planVolume) FROM WmsTask task WHERE task.workDoc.id = :workDocId";
			Object[] loadQuantity = (Object[])commonDao.findByQueryUniqueResult(hql, "workDocId", wwd.getId());
			workDocMap.put("重量", loadQuantity[0] == null ? 0D : loadQuantity[0]);
			workDocMap.put("体积", loadQuantity[1] == null ? 0D : loadQuantity[1]);
			workDocList.add(workDocMap);
		}
		problem.put("作业单集", workDocList);
	}
	
	/**
	 * 处理作业单生成规则返回结果
	 * @param resultMap
	 */
	private void doCreateWorkDocResult(WmsMoveDoc moveDoc, Map<String, Object> resultMap) throws BusinessException {
		List <Map<String, Object>> resultList = (List <Map<String, Object>>) resultMap.get("返回列表");
//		if(resultList.isEmpty()){
//			throw new BusinessException("未找到源库位对应的工作区,请检查【工作区定位表】！");
//		}
		for (Map<String, Object> result : resultList) {
			List<Map<String,Object>> taskList = (List<Map<String,Object>>) result.get("任务集");
			if(taskList == null || taskList.isEmpty()){
				continue;
			}
			Object workDocKey = result.get("作业单序号");
			
			WmsWorkDoc workDoc = null;
			if(workDocKey != null){
				workDoc = load(WmsWorkDoc.class,new Long(workDocKey.toString()));
			}
			if(workDoc == null){
				workDoc = EntityFactory.getEntity(WmsWorkDoc.class);
				workDoc.setWarehouse(moveDoc.getWarehouse());
				workDoc.setCompany(moveDoc.getCompany());
				workDoc.setCode(wmsBussinessCodeManager.generateCodeByRule(moveDoc.getWarehouse(), moveDoc.getWarehouse().getName(), "作业单", ""));
				workDoc.setOriginalBillCode(moveDoc.getCode());
				workDoc.setType(moveDoc.getType());
				Long workAreaId = (Long)result.get("工作区序号");
				if (workAreaId == null) {
					throw new BusinessException("工作区无法找到！");
				}
				WmsWorkArea workArea = load(WmsWorkArea.class,workAreaId);
				if (workArea == null) {
					throw new BusinessException("workarea.isnull");
				}
				workDoc.setWorkArea(workArea);
				commonDao.store(workDoc);
			}
			else{
				if(!moveDoc.getCode().equals(workDoc.getOriginalBillCode())){
					workDoc.setOriginalBillCode("");
				}
			}
			
			//FIXME 只有发货单拣货和波次拣货需要产生目标库存
			if (moveDoc.isPickTicketType() || moveDoc.isWaveType()) {
					WmsLocation toLocation = null;
					if(!workDoc.getTasks().isEmpty()){
						WmsTask task = workDoc.getTasks().iterator().next();
						toLocation = load(WmsLocation.class,task.getToLocationId());
					}
					else{
						toLocation = getStockUpLocationByWorkArea(moveDoc.getWarehouse(), moveDoc,workDoc);
					}
					for(Map<String,Object> taskMap : taskList){
						WmsTask task = load(WmsTask.class,(Long)taskMap.get("任务ID"));
						
						WmsInventory dstInv = wmsInventoryManager.getInventoryWithNew(toLocation, task.getItemKey(), task.getPackageUnit(), task.getInventoryStatus());
						dstInv.allocatePutaway(task.getPlanQuantityBU());
						task.setDescInventoryId(dstInv.getId());
						task.setToLocationId(toLocation.getId());
						task.setToLocationCode(toLocation.getCode());
						workDoc.addTask(task);
						commonDao.store(workDoc);
						workflowManager.doWorkflow(task, "taskProcess.dispatch");
					}
				
			}// 加入加工拣货
			else if (moveDoc.isProcessType()) {
				WmsLocation toLocation = commonDao.load(WmsLocation.class, moveDoc.getShipLocation().getId());
					for(Map<String,Object> taskMap : taskList){
						WmsTask task = load(WmsTask.class,(Long)taskMap.get("任务ID"));
						
						WmsInventory dstInv = wmsInventoryManager.getInventoryWithNew(toLocation, task.getItemKey(), task.getPackageUnit(), moveDoc.getProcessPlan().getInventoryStatus());
						dstInv.allocatePutaway(task.getPlanQuantityBU());
						task.setDescInventoryId(dstInv.getId());
						task.setToLocationId(toLocation.getId());
						task.setToLocationCode(toLocation.getCode());
							
						workDoc.addTask(task);
						commonDao.store(workDoc);
						workflowManager.doWorkflow(task, "taskProcess.dispatch");
					}
			} else {
					for(Map<String,Object> taskMap : taskList){
						WmsTask task = load(WmsTask.class,(Long)taskMap.get("任务ID"));
						workDoc.addTask(task);
						commonDao.store(workDoc);
						workflowManager.doWorkflow(task, "taskProcess.dispatch");
					}
			}
			workflowManager.doWorkflow(workDoc, "workDocProcess.new");
		}
	}
	public void activePickByJac(WmsMoveDoc moveDoc){
		//产生目标库存、task更新目标库位信息/状态、拣货单状态改为生效
		List<WmsTask> tasks = commonDao.findByQuery(MyUtils.getTask(), 
				new String[]{"billCode","type","status"},
				new Object[]{moveDoc.getCode(),moveDoc.getType(),WmsTaskStatus.OPEN});
		String type = MyUtils.getMoveType(moveDoc.getType());
		WmsWarehouse warehouse = commonDao.load(WmsWarehouse.class,moveDoc.getWarehouse().getId());
		WmsBillType billType = commonDao.load(WmsBillType.class, moveDoc.getBillType().getId());
		if("补货单".equals(type)){
			//补货分配规则
			WmsLocation toLocation = null;
			/*List<Map<String, Object>> details = wmsRuleManager.getAllRuleTableDetail(WmsWarehouseHolder.getWmsWarehouse().getName(),
					"R101_备货库位定位表",WmsWarehouseHolder.getWmsWarehouse().getName());
			String supplier = null,itemCode = null,extendPropC1 = null,locationCode = "nodatebefound";*/
			for(WmsTask task : tasks){
				/*for(Map<String, Object> dd : details){
					//{工艺状态=-, 货主=发动机, 货品代码=1023060GAZC, 补货上限=180.0, 供应商=L21016, 库位=A-03010101, 备料工工号=test}
					supplier = dd.get("供应商").toString();
					itemCode = dd.get("货品代码").toString();
					extendPropC1 = dd.get("工艺状态").toString();
					if(task.getItemKey().getLotInfo().getSupplier().getCode().equals(supplier)
							&& task.getItemKey().getItem().getCode().equals(itemCode)
							&& task.getItemKey().getLotInfo().getExtendPropC1().equals(extendPropC1)){
						locationCode = dd.get("库位").toString().trim();
						break;
					}
				}
				
				if(locationCode.equals("nodatebefound")){
					throw new BusinessException("R101_备货库位定位表,无数据维护");
				}*/
				WmsMoveDocDetail detail = commonDao.load(WmsMoveDocDetail.class, task.getMoveDocDetail().getId());
				toLocation = commonDao.load(WmsLocation.class, detail.getToLocationId());
				/*toLocation = (WmsLocation) commonDao.findByQueryUniqueResult("FROM WmsLocation l WHERE l.code =:code AND l.status =:status", 
						new String[]{"code","status"}, new Object[]{locationCode,BaseStatus.ENABLED});//ENABLED
				if(toLocation==null){
					throw new BusinessException("备料库位不存在:"+locationCode);
				}*/
				activePickByJac(task, toLocation,findToInv(task, toLocation));
			}
		}else if("质检单".equals(type)){
			Boolean beSameASN = Boolean.FALSE;
			if(billType.getBeSameASN()){
				beSameASN = Boolean.TRUE;//说明当前质检单回写源库存状态
			}
			WmsLocation toLocation = null;
			Boolean isSameLoc = false;
			for(WmsTask task : tasks){
				//是否同一库位
				isSameLoc = task.getFromLocationCode().equals(task.getToLocationCode());

				toLocation = commonDao.load(WmsLocation.class, task.getToLocationId());
				if(beSameASN){
					if(isSameLoc){
						activePickByJac(task,toLocation,findToInvQuality(moveDoc,task, toLocation));
					}else{
						activePickByJac(task,toLocation,findToInv(task, toLocation));
					}
				}else{
					//是否调整库存状态了
					if(isSameLoc){
						activePickByJac(task,toLocation,findToInvSame(task, toLocation));
					}else{
						activePickByJac(task,toLocation,findToInv(task, toLocation));
					}
				}
			
//				if(task.getBeManual()){}else{//不需要返库的只改变状态
//					task.setStatus(WmsTaskStatus.DISPATCHED);
//					commonDao.store(task);
//				}
			}
		}else{
			splitMoveDoc(moveDoc,type);
			WmsLocation toLocation = getStockUpLocationByWorkArea(warehouse, type,billType.getCode()
					,moveDoc.getPlanQuantityBU(),WmsLocationType.SHIP,BaseStatus.NULLVALUE,Boolean.FALSE,moveDoc.getCompany().getCode());
			for(WmsTask task : tasks){
				activePickByJac(task, toLocation,findToInv(task, toLocation));
			}
		}
		
		if("发货单".equals(type) || "波次单".equals(type)){
			WmsMoveDoc wmsMoveDoc = commonDao.load(WmsMoveDoc.class, moveDoc.getId());
			Set<WmsMoveDocDetail> moveDocDetails = wmsMoveDoc.getDetails();
			Iterator<WmsMoveDocDetail> it = moveDocDetails.iterator();  
			
			//每次激活前,清空下关系表  yc.min 20170921
			//删除对应的容器信息
//			clearWmsMoveDocAndStation(moveDoc.getId());
			
			Double quantity = 0D;//容器需要装载的量
			if("SPS_PICKING".equals(wmsMoveDoc.getOriginalBillType().getCode())){//时序件出库单(器具关系接口传)
				Map<String,Double> boxTagNums = new HashMap<String, Double>();//汇总相同标签器具的总装载量
				Double boxTagNum = 0D;
				List<WmsMoveDocAndStation> wmdas = new ArrayList<WmsMoveDocAndStation>();
				String relatedBill1 = wmsMoveDoc.getPickTicket().getRelatedBill1();
				while(it.hasNext()){
					WmsMoveDocDetail detail = it.next();
					if(detail.getAllocatedQuantityBU()<=0){
						continue;
					}
					WmsPickTicketDetail pickDetail = commonDao.load(WmsPickTicketDetail.class, detail.getRelatedId());
					String hql = "FROM WmsPickTicketAndAppliance appl WHERE appl.sheetNo =:sheetNo" +
							" AND appl.supplierNo =:supplierNo AND appl.partNo =:partNo" +
							" AND appl.qty > appl.activeQty" +
							" ORDER BY appl.no ASC,appl.seq DESC";//ORDER BY appl.no ASC,appl.seq ASC
					List<WmsPickTicketAndAppliance> appls = commonDao.findByQuery(hql, new String[]{"sheetNo","supplierNo","partNo"}, 
							new Object[]{relatedBill1,pickDetail.getSupplier().getCode(),detail.getItem().getCode()});
					if(appls==null || appls.size()<=0){
						throw new BusinessException("时序件器具关系找不到数据:"+relatedBill1+","+pickDetail.getSupplier().getCode()+","+detail.getItem().getCode());
					}
					Double allocatedQuantityBU = detail.getAllocatedQuantityBU();
					for(WmsPickTicketAndAppliance appl : appls){
						quantity = appl.getQty()-appl.getActiveQty();
						if(quantity<=0){//防止已经激活过的还被继续使用
							continue;
						}
						if(allocatedQuantityBU<=0){//防止分配量部分分配导致所有都加入
							break;
						}
						quantity = quantity<=allocatedQuantityBU?quantity:allocatedQuantityBU;
//						quantity = appl.getQty();//appl.getQty() <= detail.getPlanQuantityBU() ? appl.getQty() : detail.getPlanQuantityBU();
						WmsMoveDocAndStation was = new WmsMoveDocAndStation(detail.getItem(), detail, 
								quantity,quantity.intValue(),appl.getPackageNo(),appl.getPackageName(),appl.getSeq(),appl.getEndseq()
								,appl.getCurPag(),appl.getTotalPage());
						was.setBoxTag(appl.getNo());
						was.setFromStorage(appl.getFromStorage());
						was.setToStorage(appl.getToStorage());
						was.setDockNo(appl.getDockNo());
						was.setSx(appl.getSx());
						was.setSpsId(appl.getId());
						commonDao.store(was);
						wmdas.add(was);
						if(boxTagNums.containsKey(appl.getNo())){
							boxTagNum = boxTagNums.get(appl.getNo()) + quantity;
						}else{
							boxTagNum = quantity;
						}
						boxTagNums.put(appl.getNo(), boxTagNum);
						appl.setActiveQty(quantity+appl.getActiveQty());
						commonDao.store(appl);
						
						allocatedQuantityBU -= quantity;
					}
				}
				for(WmsMoveDocAndStation was : wmdas){
					was.setBoxTagQty(boxTagNums.get(was.getBoxTag()));
					commonDao.store(was);
				}
			}else{
				String receiveDoc = wmsMoveDoc.getPickTicket().getReceiveDoc();//收货道口
				while(it.hasNext()){
					WmsMoveDocDetail detail = it.next();
					if(detail.getAllocatedQuantityBU()<=0){
						continue;
					}
					List<WmsStationAndItem> stationList = null;
					if(!"KD01K1".equals(detail.getProductionLine())){//KD件走散件
						String hql = "FROM WmsStationAndItem w WHERE w.item.company.id =:companyId AND w.item.id="+detail.getItem().getId();
						stationList = commonDao.findByQuery(hql,"companyId",moveDoc.getCompany().getId());
					}
					if(null == stationList || stationList.size() <= 0){//视为散件,也生成WmsPickTicketAndAppliance,器具型号默认散件
						WmsMoveDocAndStation was = new WmsMoveDocAndStation(detail.getItem(), detail, 
								detail.getAllocatedQuantityBU(),Integer.MAX_VALUE,MyUtils.PARTS,MyUtils.PARTS,0d,0d,0,0);
						String boxTag =wmsBussinessCodeManager.generateCodeByRule(WmsWarehouseHolder.getWmsWarehouse(), wmsMoveDoc.getCompany().getName(), "RF", "SJ");
						was.setBoxTag(boxTag);
						was.setIsPartPick(true);
						was.setDockNo(receiveDoc);
						quantity = (double) Integer.MAX_VALUE;
						was.setBoxTagQty(quantity);
						commonDao.store(was);
					}else{
						WmsStationAndItem stations = stationList.get(0);
						//stationNum = 需要容器的个数 = 分配数量/装载量  向上取整
						double stationNum = Math.ceil(detail.getAllocatedQuantityBU() / stations.getLoadage());
//						WmsLocation location = commonDao.load(WmsLocation.class, detail.getFromLocationId());
						for(int j = 1; j <= stationNum; j++){
							if(stationNum == j){
								quantity = detail.getAllocatedQuantityBU() % stations.getLoadage() == 0 ? stations.getLoadage() : detail.getAllocatedQuantityBU() % stations.getLoadage();
							}else{
								quantity = stations.getLoadage().doubleValue();
							}
							WmsMoveDocAndStation was = new WmsMoveDocAndStation(detail.getItem(), detail, 
														quantity,stations.getLoadage(),stations.getType(),stations.getName(),
														0d,0d,0,0);
							String boxTag =wmsBussinessCodeManager.generateCodeByRule(WmsWarehouseHolder.getWmsWarehouse(), wmsMoveDoc.getCompany().getName(), "RF", stations.getType());
								//wmsBussinessCodeManager.getWmsBillDetailCode("", "");
							was.setBoxTag(boxTag);
							was.setDockNo(receiveDoc);
							was.setBoxTagQty(quantity);
							commonDao.store(was);
						}
					}
				}
			}
			addPickTask(wmsMoveDoc);
		}
	}
	private void addPickTask(WmsMoveDoc wmsMoveDoc){
		Task task = new Task("initPickContainer", 
				"wmsWorkDocManager.initPickContainer", wmsMoveDoc.getId());
		//task.setStatus(TaskStatus.STAT_FINISH);//test
		commonDao.store(task);
		wmsMoveDoc.setLineNo(task.getId().intValue());
		commonDao.store(wmsMoveDoc);
	}
	//wmsWorkDocManager.initPickContainer
	public void initPickContainer(Long id){
		WmsMoveDoc wmsMoveDoc = commonDao.load(WmsMoveDoc.class, id);
		if(wmsMoveDoc!=null){
			WmsPickTicket pickTicket = wmsMoveDoc.getPickTicket();
			Set<WmsMoveDocDetail> moveDocDetails = wmsMoveDoc.getDetails();
			Iterator<WmsMoveDocDetail> it = moveDocDetails.iterator();  
			while(it.hasNext()){
				WmsMoveDocDetail detail = it.next();
				if(detail.getAllocatedQuantityBU()<=0){
					continue;
				}
				String hql = "FROM WmsTask task WHERE task.moveDocDetail.id =:moveDetailId" +
						" ORDER BY task.id ASC ";
				List<WmsTask> tasks = commonDao.findByQuery(hql, new String[]{"moveDetailId"}, new Object[]{detail.getId()});
				if(tasks==null || tasks.size()<=0){
					continue;
				}
				List<WmsMoveDocAndStation> was = commonDao.findByQuery("FROM WmsMoveDocAndStation w WHERE w.moveDocDetail.id =:moveDetailId",
						new String[]{"moveDetailId"}, new Object[]{detail.getId()});
				if(was==null || was.size()<=0){
					continue;
				}
				Map<Long,Double> msAvailableQty = new HashMap<Long, Double>();//WmsMoveDocAndStationd的可用量
				Double qty = 0D;
				for(WmsTask task : tasks){
					Double taskQty = task.getPlanQuantityBU();
					for(WmsMoveDocAndStation ws : was){
						Double wsQty = msAvailableQty.containsKey(ws.getId())?msAvailableQty.get(ws.getId()):ws.getQuantity();
						if(wsQty<=0){
							continue;
						}
						qty = taskQty>=wsQty?wsQty:taskQty;
						taskQty -= qty;
						wsQty -= qty;
						
						msAvailableQty.put(ws.getId(), wsQty);//更新WmsMoveDocAndStationd的可用量
						WmsPickContainer pc = new WmsPickContainer(wmsMoveDoc.getId(), wmsMoveDoc.getCode(), 
								JavaTools.format(pickTicket.getRequireArriveDate(), JavaTools.dmy_hms), wmsMoveDoc.getBillType().getName(), 
								detail.getProductionLine(), wmsMoveDoc.getBlg().getName(), ws.getBoxTag(), ws.getType(), ws.getTypeName(),
								task.getItemKey().getLotInfo().getSupplier().getCode(), task.getItemKey().getLotInfo().getSupplier().getName(), 
								detail.getItem().getCode(), detail.getItem().getName(), task.getFromLocationCode(), wmsMoveDoc.getStation(), 
								ws.getDockNo(), ws.getSeq(), ws.getEndseq(), qty,pickTicket.getOdrSu());
						commonDao.store(pc);
//						System.out.println(qty+","+task.getFromLocationCode()+","+detail.getItem().getCode());
						if(taskQty<=0){
							break;
						}
					}
//					System.out.println("--------------------------------");
				}
				msAvailableQty.clear();
			}
		}
	}
	/**
	 *  拆分移位单
	 *  1.获取移位单未分配数量
	 *  2.对未分配数量小于等于0的移位单不做拆分
	 *  3.如果未分配数量大于0，则将未做分配的数量和已做分配的数量拆分成两张移位单
	 * */
	public void splitMoveDoc(WmsMoveDoc moveDoc,String type) {
		//获取移位单未分配数量
		double unallocateQuantity = moveDoc.getUnAllocateQuantityBU();
		//因为底层包是先调用工作流修改状态，再调用消息，
		//因此此处只能用未分配数量来判断，而不能通过移位单的状态来判断
		if(unallocateQuantity <= 0) {
			return;
		}
		WmsMoveDoc unAllocateMoveDoc = EntityFactory.getEntity(WmsMoveDoc.class);
		BeanUtils.copyEntity(unAllocateMoveDoc, moveDoc);
		unAllocateMoveDoc.setId(null);
		
		/*String type = "移位单";
		if(WmsMoveDocType.MV_PICKTICKET_PICKING.equals(moveDoc.getType())) {
			type = "拣货单";
		} else if(WmsMoveDocType.MV_MOVE.equals(moveDoc.getType())) {
			type = "移位单";
		} else if(WmsMoveDocType.MV_WAVE_PICKING.equals(moveDoc.getType())) {
			type = "波次拣货单";
		} else if(WmsMoveDocType.MV_REPLENISHMENT_MOVE.equals(moveDoc.getType())) {
			type = "补货单";
		} else if(WmsMoveDocType.MV_PROCESS_PICKING.equals(moveDoc.getType())) {
			type = "加工单";
		} else if(WmsMoveDocType.MV_PUTAWAY.equals(moveDoc.getType())) {
			type = "上架单";
		}*/
		
		WmsBillType billType = commonDao.load(WmsBillType.class, moveDoc.getBillType().getId());
		String companyName = moveDoc.getCompany().getName();
		if(moveDoc.getCompany().isBeVirtual()){
			companyName = moveDoc.getWarehouse().getName();
		}
		String code = wmsBussinessCodeManager.generateCodeByRule(moveDoc.getWarehouse(), 
				companyName, type, billType.getName());
		unAllocateMoveDoc.setCode(code);
		unAllocateMoveDoc.setPlanQuantityBU(moveDoc.getUnAllocateQuantityBU());
		unAllocateMoveDoc.setAllocatedQuantityBU(0D);
		unAllocateMoveDoc.setMovedQuantityBU(0D);
		unAllocateMoveDoc.setShippedQuantityBU(0D);
		unAllocateMoveDoc.setIsOweProduct(Boolean.TRUE);//是否欠品
		unAllocateMoveDoc.setDetails(new HashSet<WmsMoveDocDetail>());
		unAllocateMoveDoc.setBoxDetails(new HashSet<WmsBoxDetail>());
		unAllocateMoveDoc.setStateLogs(new HashSet<WmsBOLStateLog>());
		workflowManager.doWorkflow(unAllocateMoveDoc, "wmsMoveDocProcess.new");
		commonDao.store(unAllocateMoveDoc);
		
		for(WmsMoveDocDetail detail : moveDoc.getDetails()) {
			if(detail.getUnAllocateQuantityBU() > 0 
				&& detail.getUnAllocateQuantityBU().doubleValue() < detail.getPlanQuantityBU().doubleValue()) {
				WmsMoveDocDetail unallocateDetail = EntityFactory.getEntity(WmsMoveDocDetail.class);
				BeanUtils.copyEntity(unallocateDetail, detail);
				unallocateDetail.setId(null);
				unallocateDetail.setMovedQuantityBU(0D);
				unallocateDetail.setPlanQuantityBU(detail.getUnAllocateQuantityBU());
				unallocateDetail.setPlanQuantity(unallocateDetail.getPlanQuantityBU() / unallocateDetail.getPackageUnit().getConvertFigure());
				unallocateDetail.setShippedQuantityBU(0D);
				unallocateDetail.setAllocatedQuantityBU(0D);
				unallocateDetail.setMoveDoc(unAllocateMoveDoc);
				unallocateDetail.calculateLoad();
				unallocateDetail.setTasks(new HashSet<WmsTask>());
				unallocateDetail.setWaveDocDetails(new HashSet<WmsWaveDocDetail>());
				unAllocateMoveDoc.addDetail(unallocateDetail);
				commonDao.store(unallocateDetail);
				for(WmsWaveDocDetail waveDocDetail : detail.getWaveDocDetails()){
					waveDocDetail.addWmsMoveDocDetail(unallocateDetail);
				}
				detail.setPlanQuantityBU(detail.getAllocatedQuantityBU());
				detail.setPlanQuantity(detail.getPlanQuantityBU() / detail.getPackageUnit().getConvertFigure());
				detail.calculateLoad();
				commonDao.store(detail);
			} else if(detail.getUnAllocateQuantityBU() > 0 
				&& detail.getUnAllocateQuantityBU().doubleValue() == detail.getPlanQuantityBU().doubleValue()) {
				detail.setMoveDoc(unAllocateMoveDoc);
				unAllocateMoveDoc.addDetail(detail);
				commonDao.store(detail);
			}
		}
		moveDoc.setPlanQuantityBU(moveDoc.getAllocatedQuantityBU());
		commonDao.store(moveDoc);
	}
	private WmsInventory findToInv(WmsTask task,WmsLocation toLocation){
		WmsInventory dstInv = wmsInventoryManager.getInventoryWithNew(toLocation, task.getItemKey(), 
				task.getPackageUnit(), task.getInventoryStatus());
		dstInv.allocatePutaway(task.getPlanQuantityBU());
		return dstInv;
	}
	private WmsInventory findToInvSame(WmsTask task,WmsLocation toLocation){
		WmsInventory dstInv = commonDao.load(WmsInventory.class, task.getSrcInventoryId());
		if(dstInv==null){
			dstInv = wmsInventoryManager.getInventoryWithNew(toLocation, task.getItemKey(), 
					task.getPackageUnit(), task.getInventoryStatus());
		}
		dstInv.allocatePutaway(task.getPlanQuantityBU());
		return dstInv;
	}
	/**质检返回源库位且回写相同ASN号库位物料的,匹配库存不考虑状态,且回写源库存状态为质检后状态*/
	private WmsInventory findToInvQuality(WmsMoveDoc moveDoc,WmsTask task,WmsLocation toLocation){
		WmsInventory dstInv = commonDao.load(WmsInventory.class, task.getSrcInventoryId());
//		WmsMoveDocDetail moveDocDetail = task.getMoveDocDetail();
//		List<WmsBOLStateLog> logs = commonDao.findByQuery("FROM WmsBOLStateLog log"
//				+ " WHERE log.moveDoc.id =:moveDocId AND log.type =:detailId AND log.vehicleNo =:pallet", 
//				new String[]{"moveDocId","detailId","pallet"}, 
//				new Object[]{moveDoc.getId(),moveDocDetail.getId()+"",moveDocDetail.getPallet()});
//		WmsInventoryExtend ix = null;
//		for(WmsBOLStateLog log : logs){
//			try {
//				ix = commonDao.load(WmsInventoryExtend.class, Long.parseLong(log.getDriver()));
//				if(ix!=null){
//					dstInv = ix.getInventory();
//				}
//			} catch (Exception e) {
//				logger.error(e);
//			}
//		}
		if(dstInv==null){
			dstInv = wmsInventoryManager.getInventoryWithNew(toLocation, task.getItemKey(),task.getPackageUnit());
		}
		dstInv.allocatePutaway(task.getPlanQuantityBU());
		return dstInv;
	}
	private void activePickByJac(WmsTask task,WmsLocation toLocation,WmsInventory dstInv){
		task.setDescInventoryId(dstInv.getId());
		task.setToLocationId(toLocation.getId());
		task.setToLocationCode(toLocation.getCode());
		task.setStatus(WmsTaskStatus.DISPATCHED);
		commonDao.store(task);
	}
	public void unActivePickByJac(WmsMoveDoc moveDoc){
		//目标库存取消上架分配、task清除目标库位信息/状态、拣货单状态改为整单分配
		List<WmsTask> tasks = commonDao.findByQuery(MyUtils.getTask(), 
				new String[]{"billCode","type","status"},
				new Object[]{moveDoc.getCode(),moveDoc.getType(),WmsTaskStatus.DISPATCHED});
		String type = MyUtils.getMoveType(moveDoc.getType());
		if("质检单".equals(type)){
			for(WmsTask task : tasks){
				if(task.getBeManual()){//不需要返库的激活不分配目标库存
					WmsInventory dstInv = commonDao.load(WmsInventory.class, task.getDescInventoryId());
					dstInv.unallocatePutaway(task.getUnmovedQuantityBU());
					commonDao.store(dstInv);
				}
				
				task.setDescInventoryId(null);
				task.setStatus(WmsTaskStatus.OPEN);
				commonDao.store(task);
			}
		}else{
			for(WmsTask task : tasks){
				WmsInventory dstInv = commonDao.load(WmsInventory.class, task.getDescInventoryId());
				dstInv.unallocatePutaway(task.getUnmovedQuantityBU());
				commonDao.store(dstInv);
				
				task.setDescInventoryId(null);
				task.setToLocationId(null);
				task.setToLocationCode(null);
				task.setStatus(WmsTaskStatus.OPEN);
				commonDao.store(task);
			}
		}
		//删除对应的容器信息
		clearWmsMoveDocAndStation(moveDoc.getId());
		clearWmsPickContainer(moveDoc.getId());
	}
	private void clearWmsMoveDocAndStation(Long moveDocId){
		//删除对应的容器信息
		List<String> containers = new ArrayList<String>();
		String conteiner = "";
		String hql = "FROM WmsMoveDocAndStation w WHERE w.moveDocDetail.moveDoc.id=:id";
		List<WmsMoveDocAndStation> mdds = commonDao.findByQuery(hql ,"id", moveDocId);
		if(mdds!=null && mdds.size()>0){
			for(WmsMoveDocAndStation md : mdds){
				//增加时序件反向修改激活量  yc.min
				if(md.getSpsId()!=null){//时序件不为空
					WmsPickTicketAndAppliance appl = commonDao.load(WmsPickTicketAndAppliance.class, md.getSpsId());
					if(appl!=null){
						Double quantity = appl.getActiveQty()>md.getQuantity()?appl.getActiveQty()-md.getQuantity():0D;
						appl.setActiveQty(quantity);
						commonDao.store(appl);
					}
				}
				hql = "FROM WmsTaskAndStation wt WHERE wt.station.id =:id";
				List<WmsTaskAndStation> wtss = commonDao.findByQuery(hql, "id", md.getId());
				if(wtss!=null && wtss.size()>0){
					for(WmsTaskAndStation wts : wtss){
						
						if(!StringUtils.isEmpty(wts.getContainer())){
							conteiner = wts.getContainer();//"'"+md.getContainer()+"'"; 
							if(!containers.contains(conteiner)){
								containers.add(conteiner);
							}
						}
						
						commonDao.delete(wts);
					}
				}
				commonDao.delete(md);
			}
			if(containers.size()>0){
				hql = "UPDATE WmsBoxType bt SET bt.isPicking = false WHERE bt.code in(:codes)";
				commonDao.executeByHql(hql, "codes", containers);
			}
		}
	}
	private void clearWmsPickContainer(Long moveDocId){
		String hql = "FROM WmsPickContainer pc WHERE pc.pickId =:pickId";
		List<WmsPickContainer> pcs = commonDao.findByQuery(hql, new String[]{"pickId"}, new Object[]{moveDocId});
		if(pcs!=null && pcs.size()>0){
			commonDao.deleteAll(pcs);
		}
	}
	/**质检单移位确认*/
	private void pickConfirmQuality(WmsMoveDoc moveDoc,List<WmsTask> tasks){
		WmsBillType billType = commonDao.load(WmsBillType.class, moveDoc.getBillType().getId());
		if(billType.getBeSameASN()){//回写质检明细对应SOI的库存记录
			//同一个ASN下的同一个明细,库存状态要么合格要么不合格,两者不同时存在
			//允许存在的组合(合格+报废合格,不合格+报废不合格,报废合格,报废不合格,合格,不合格)
			//如果是否返库=false却没维护回写状态则系统报错
			List<Object[]> backInventoryStates = commonDao.findByQuery("SELECT s.itemState.name,s.backInventoryState.name"
					+ " FROM WmsQualityBillStatus s"
					+ " WHERE s.itemState.beBackInv=false AND s.billType.id =:billTypeId", 
					new String[]{"billTypeId"}, 
					new Object[]{billType.getId()});
			Map<String,String> backStates = new HashMap<String, String>();
			for(Object[] repel : backInventoryStates){
				if(repel[1]==null){
					throw new BusinessException(billType.getName()+"-"+repel[0]+":不返库但没维护回写状态");
				}
				backStates.put(repel[0].toString(), repel[1].toString());//{报废-不合格=质检不合格, 报废-合格=-}
			}
			//存放task源库存参与质检量,生成反馈日志时减去参与量得出真实的回写量
			//不考虑不返库的
			Map<Long,Double> srcInventoryQty = new HashMap<Long,Double>();
			Double srcQty = 0D;
			
			List<String> sois = new ArrayList<String>();
			Map<String,Map<Long,String>> soiStates = new HashMap<String, Map<Long,String>>();
			Map<Long,String> itemStates = null;//物料维护状态
			Map<Long,String> invStatus = new HashMap<Long, String>();//物料质检前状态
			for(WmsTask task : tasks){
				if(!sois.contains("'"+task.getItemKey().getLotInfo().getSoi()+"'")){
					sois.add("'"+task.getItemKey().getLotInfo().getSoi()+"'");
				}
				if(soiStates.containsKey(task.getItemKey().getLotInfo().getSoi())){
					itemStates = soiStates.get(task.getItemKey().getLotInfo().getSoi());
				}else{
					itemStates = new HashMap<Long, String>();
				}
				if(backStates.containsKey(task.getInventoryStatus())){//维护状态是否属于不返库的,是的话取回写状态
					itemStates.put(task.getItemKey().getItem().getId(), backStates.get(task.getInventoryStatus()));
				}else{//不属于不返库的,直接取维护状态
					itemStates.put(task.getItemKey().getItem().getId(), task.getInventoryStatus());
					//只有不返库的才累加
					if(srcInventoryQty.containsKey(task.getSrcInventoryId())){
						srcQty = srcInventoryQty.get(task.getSrcInventoryId());
					}else{
						srcQty = 0D;
					}
					srcQty += task.getMovedQuantityBU();
					srcInventoryQty.put(task.getSrcInventoryId(), srcQty);
				}
				soiStates.put(task.getItemKey().getLotInfo().getSoi(), itemStates);//{JAC_FDJIN150512000001={200000=-}}
				invStatus.put(task.getItemKey().getItem().getId(), task.getMoveDocDetail().getInventoryStatus());
				
			}
			String hql = "FROM WmsInventory i WHERE i.itemKey.lotInfo.soi"
					+ " in ("+StringUtils.substringBetween(sois.toString(), "[", "]")+")"
					+ " AND i.quantityBU>i.allocatedQuantityBU"
					+ " AND i.location.type =:type";
			List<WmsInventory> inventorys = commonDao.findByQuery(hql, 
					new String[]{"type"}, new Object[]{WmsLocationType.STORAGE});
			if(inventorys!=null && inventorys.size()>0){
				Long key = null;
				for(WmsInventory i : inventorys){
					key = i.getItemKey().getItem().getId();
					itemStates = soiStates.get(i.getItemKey().getLotInfo().getSoi());
					if(itemStates.containsKey(key)){
						if(!invStatus.get(key).equals(i.getStatus())){//允许参与送检的才批量调整状态
							continue;
						}
						i.setStatus(itemStates.get(key));
						commonDao.store(i);
						//20160530 用于库存日结统计质检状态调整日志
						srcQty = srcInventoryQty.containsKey(i.getId())?srcInventoryQty.get(i.getId()):0D;
						Object[] obj = new Object[]{
								i.getItemKey().getLotInfo().getExtendPropC1(),invStatus.get(key),
								i.getItemKey().getItem().getCode(),i.getItemKey().getItem().getName(),
								i.getAvailableQuantityBU()-srcQty,i.getItemKey().getLotInfo().getStorageDate(),
								i.getItemKey().getLotInfo().getSupplier().getCode(),i.getItemKey().getLotInfo().getSupplier().getName(),
								billType.getName(),moveDoc.getWarehouse().getId(),WmsQualityMoveSoiLog.QUALITY_MOVE_DOC_SOI,i.getStatus(),
								i.getLocation().getCode(),moveDoc.getCode()
						};
						transactionalManager.saveQualityMoveSoiLog(obj);
					}
				}
			}
		}
	}
	public List<Object[]> findTaskByMoveCode(String moveCode){
		String hql = "SELECT task.id,task.fromLocationCode,task.itemKey.item.code,"
				+ "task.planQuantityBU-task.movedQuantityBU AS unMoveQty,task.toLocationCode"
				+ " FROM WmsTask task WHERE 1=1 AND (task.planQuantityBU-task.movedQuantityBU) > 0"
				+ " AND task.moveDocDetail.moveDoc.code =:moveCode"
				+ " ORDER BY task.toLocationCode";
		List<Object[]> tasks = commonDao.findByQuery(hql, 
				new String[]{"moveCode"}, new Object[]{moveCode});
		return tasks;
	}
	
	/**拣货单整单拣货确认*/
	public void pickConfirmAll(WmsMoveDoc moveDoc){
		List<WmsTask> tasks = commonDao.findByQuery(MyUtils.getTaskByType(), 
				new String[]{"billCode","type"},
				new Object[]{moveDoc.getCode(),moveDoc.getType()});
		if(tasks.size()>0){
			String description = null;
			String type = MyUtils.getMoveType(moveDoc.getType());
			if("移位单".equals(type)){
				description = "库内移位单移位确认";
			}else if("发货单".equals(type)){
				description = "拣货单整单拣货确认";
			}else if("补货单".equals(type)){
				description = "补货单整单拣货确认";
			}else if("质检单".equals(type)){
				description = WmsInventoryLogType.QUALITY_MOVE_DOC_SOI;//质检单整单拣货确认
			}
			if("发货单".equals(type) || "质检单".equals(type)
					||"补货单".equals(type)){//只允许测试通过的业务使用
				for(WmsTask task : tasks){
					if(task.getUnmovedQuantityBU()<=0){
						continue;
					}
					if(!task.getStatus().equals(WmsTaskStatus.DISPATCHED)){
						continue;
					}
					pickConfirmStep(moveDoc, task, task.getUnmovedQuantityBU(), 
							description, task.getMoveDocDetail(),moveDoc.getType(),null);
				}
				if("质检单".equals(type)){
					pickConfirmQuality(moveDoc, tasks);
				}
			}
//			if("移位单".equals(type)){
//				List<Object[]> taskValues = findTaskByMoveCode(moveDoc.getCode());
//				for(Object[] obj : taskValues){
//					WmsTask task = commonDao.load(WmsTask.class, Long.parseLong(obj[0].toString()));
//					Double unMoveQty = Double.valueOf(obj[3].toString());
//					pickConfirmStep(moveDoc, task, unMoveQty, description, task.getMoveDocDetail(), type,null);
//					workflowManager.doWorkflow(moveDoc, "wmsMoveDocProcess.confirm");
//				}
//			}
		}else{
			LocalizedMessage.addLocalizedMessage(MyUtils.font("失败!不存在可移位任务"));
		}
	}
	private void pickConfirmStep(WmsMoveDoc moveDoc,WmsTask task,Double moveQty,String description
			,WmsMoveDocDetail moveDocDetail,String type,Long inventoryId){
		task = commonDao.load(WmsTask.class, task.getId());
		Long srcInventoryId = task.getSrcInventoryId();
		if(inventoryId != null && inventoryId !=0 && srcInventoryId != inventoryId){//不是按系统分配的库位拣的货
			srcInventoryId = inventoryId;
			WmsInventory srcInv = commonDao.load(WmsInventory.class, inventoryId);//拣货库位的库存写入拣货分配数量
			srcInv.allocatePickup(task.getUnmovedQuantityBU());
			WmsInventory taskSrcInv = commonDao.load(WmsInventory.class, task.getSrcInventoryId());//task记录的原库存取消拣货分配
			taskSrcInv.unallocatePickup(task.getUnmovedQuantityBU());
			task.setFromLocationId(srcInv.getLocation().getId());
			task.setFromLocationCode(srcInv.getLocation().getCode());
			task.setSrcInventoryId(inventoryId);//task记录拣货库位与拣货的库存
			commonDao.store(srcInv);
		}
		
		//原库存扣减库存量/取消拣货分配
		WmsInventory srcInventory = wmsInventoryManager.moveSrcInv(srcInventoryId,
				moveQty);
		srcInventory.setLockLot(false);
//		srcInventory.getLocation().setLockCount(false);
		
		wmsInventoryManager.addInventoryLog(WmsInventoryLogType.UNLOCK, 0, 
				moveDoc.getCode(), moveDoc.getBillType(), srcInventory.getLocation(), srcInventory.getItemKey(), 0D,   
				srcInventory.getPackageUnit(),srcInventory.getStatus(),"批次锁");
		wmsInventoryManager.addInventoryLog(MyUtils.getInventoryLogType(type), -1, moveDoc.getCode(),
				moveDoc.getBillType(), srcInventory.getLocation(), srcInventory.getItemKey(),
				moveQty, srcInventory.getPackageUnit(),
				srcInventory.getStatus(), description);
		//目标库存增加库存量/取消上架分配
		WmsInventory dstInventory = wmsInventoryManager.moveDescInv(task.getDescInventoryId()==null?0L:task.getDescInventoryId(),
				moveQty);
		if(dstInventory!=null){//不返库的task激活时不产生目标库位,源库位释放掉锁定量,等同于口减掉库存
			wmsInventoryManager.addInventoryLog(MyUtils.getInventoryLogType(type), 1, moveDoc.getCode(),
					moveDoc.getBillType(), dstInventory.getLocation(), dstInventory.getItemKey(),
					moveQty, dstInventory.getPackageUnit(),
					task.getInventoryStatus(), description);
		}
		String pallet = task.getPallet() == null ? BaseStatus.NULLVALUE : task.getPallet();
		Boolean beChangeLoc = !task.getToLocationId().equals(task.getFromLocationId());
		List<WmsInventoryExtend> wsns = null;//transactionalManager.getWmsInventoryExtendByTask(task);
		if(WmsMoveDocType.MV_QUALITY_MOVE.equals(type) 
				|| WmsMoveDocType.MV_REPLENISHMENT_MOVE.equals(type)){//质检/补货子表ID已保存在明细表字段relatedId
			WmsInventoryExtend ix = commonDao.load(WmsInventoryExtend.class, moveDocDetail.getRelatedId());
			ix.unallocatePickup(moveQty);
			ix.removeQuantity(moveQty);
			if (ix.getQuantityBU() <= 0 && beChangeLoc) {
				commonDao.delete(ix);
			}else{
				pallet = BaseStatus.NULLVALUE;//没有整托移位,托盘信息不允许带走
			}
		}else{//发货只会扣减源序列号,不会增加目标序列号,因为签收区不属于存货区,也就不存在托盘号会不会带走的问题^^
			// 扣减原序列号库存信息
			wsns = commonDao.findByQuery(
							"FROM WmsInventoryExtend wsn WHERE wsn.inventory.id = :inventoryId AND wsn.quantityBU > 0 ",
							new String[] { "inventoryId" },new Object[] { srcInventoryId });
			if(wsns!=null && wsns.size()>0){
				Double m = moveQty,d = 0D;
				while(m>0){
					for(WmsInventoryExtend wsn : wsns){
						d = m<=wsn.getQuantityBU()?m:wsn.getQuantityBU();
						wsn.removeQuantity(d);
						if (wsn.getQuantityBU() <= 0) {
							commonDao.delete(wsn);
						}
						m -= d;
						if(m<=0){
							break;
						}
					}
				}
			}
		}
		WmsLocation toLocation = commonDao.load(WmsLocation.class, task.getToLocationId());
		wmsInventoryManager.addDescWie(dstInventory, pallet, toLocation, moveQty);
		
		WmsLocation srcLoc = commonDao.load(WmsLocation.class,task.getFromLocationId());
		wmsInventoryManager.refreshLocationUseRate(srcLoc, 0);
		commonDao.store(srcLoc);
		
		moveDocDetail = commonDao.load(WmsMoveDocDetail.class, moveDocDetail.getId());
		if(WmsMoveDocType.MV_PICKTICKET_PICKING.equals(type)){//"发货单"
			transactionalManager.updatePickTicketMovedQuantity(moveDocDetail, 
					moveQty);
		}
		moveDocDetail.move(moveQty);
		
		task.editMovedQuantityBU(moveQty);
		commonDao.store(task);
	}
	public void singleConfirm(WmsTask task,Long moveDocId,Double moveQty,Long wsnId,Long workerId){
		if(task.getUnmovedQuantityBU()>0 && task.getUnmovedQuantityBU()>=moveQty){
			WmsMoveDoc moveDoc = commonDao.load(WmsMoveDoc.class, moveDocId);
			String description = null;
			String type = MyUtils.getMoveType(moveDoc.getType());
			if("移位单".equals(type)){
				description = "库内移位单移位确认";
			}else if("发货单".equals(type)){
				description = "拣货单整单拣货确认";
			}else if("补货单".equals(type)){
				description = "补货单整单拣货确认";
			}
			pickConfirmStep(moveDoc, task, moveQty, description, task.getMoveDocDetail(),moveDoc.getType(),wsnId);
			workflowManager.doWorkflow(moveDoc, "wmsMoveDocProcess.confirm");
		}else{
			LocalizedMessage.addLocalizedMessage(MyUtils.font("失败!不存在可移位任务"));
		}
		
	}
	//库内退拣,退拣后的料还要继续能被拣货
	public void unPickConfirm(WmsTaskAndStation ts,String supCode,Double pickBackQty,Long toLocId){
		WmsTask task = commonDao.load(WmsTask.class, ts.getTask().getId());
		WmsMoveDocDetail moveDocDetail = commonDao.load(WmsMoveDocDetail.class, task.getMoveDocDetail().getId());
		WmsMoveDoc moveDoc = commonDao.load(WmsMoveDoc.class, moveDocDetail.getMoveDoc().getId());
		WmsLocation toLocation = commonDao.load(WmsLocation.class, toLocId);
		/**========移位========*/
		//源目标库位
		WmsInventory srcInventory = load(WmsInventory.class,task.getDescInventoryId());
		srcInventory.removeQuantityBU(pickBackQty);
//		srcInventory.addAllocatedQuantityBU(pickBackQty);
		srcInventory.allocatePutaway(pickBackQty);
		commonDao.store(srcInventory);
		wmsInventoryManager.addInventoryLog(WmsInventoryLogType.MOVE, -1, moveDoc.getCode(),
				moveDoc.getBillType(), srcInventory.getLocation(), srcInventory.getItemKey(),
				pickBackQty, srcInventory.getPackageUnit(),
				srcInventory.getStatus(), "退拣");
		//退拣库位
		WmsInventory dstInventory = wmsInventoryManager.getInventoryWithNew(
				toLocation, srcInventory.getItemKey(), srcInventory.getPackageUnit(),srcInventory.getStatus());
		dstInventory.addQuantityBU(pickBackQty);
		dstInventory.addAllocatedQuantityBU(pickBackQty);
		commonDao.store(dstInventory);
		wmsInventoryManager.addInventoryLog(WmsInventoryLogType.MOVE, 1, moveDoc.getCode(),
				moveDoc.getBillType(), dstInventory.getLocation(), dstInventory.getItemKey(),
				pickBackQty, dstInventory.getPackageUnit(),
				dstInventory.getStatus(), "退拣");
		// 增加目标库位序列号信息
		inventoryExtendManager.addInventoryExtend(dstInventory, task.getPallet()
				, BaseStatus.NULLVALUE,BaseStatus.NULLVALUE,pickBackQty);
		WmsPickTicketDetail pickTicketDetail = this.commonDao.load(WmsPickTicketDetail.class, moveDocDetail.getRelatedId());
		pickTicketDetail.cancelPickedQuantityBU(pickBackQty);
		moveDocDetail.cancelMove(pickBackQty);
		moveDoc.pickBackStatus();
		commonDao.store(moveDoc);
		task.setMovedQuantityBU(task.getMovedQuantityBU()-pickBackQty);
		if(!task.getSrcInventoryId().equals(dstInventory.getId())){//退拣库位与task分配库位不一致,new
			WmsTaskManager wmsTaskManager = (WmsTaskManager) applicationContext.getBean("wmsTaskManager");
			WmsTask newTask = wmsTaskManager.createWmsTask(moveDocDetail, dstInventory.getItemKey(),
					dstInventory.getStatus(), pickBackQty);
			newTask.setToLocationId(task.getToLocationId());
			newTask.setToLocationCode(task.getToLocationCode());
			newTask.setDescInventoryId(task.getDescInventoryId());
			newTask.setStatus(WmsTaskStatus.DISPATCHED);
			newTask.setFromLocationId(toLocation.getId());
			newTask.setFromLocationCode(toLocation.getCode());
			newTask.setSrcInventoryId(dstInventory.getId());
			commonDao.store(newTask);
			task.removePlanQuantityBU(pickBackQty);
		}else{
			task.setSrcInventoryId(dstInventory.getId());
			task.setFromLocationCode(toLocation.getCode());
			task.setFromLocationId(toLocation.getId());
		}
		task.setStatus(WmsTaskStatus.DISPATCHED);
		commonDao.store(task);
		
	}
	public void unPickConfirm(WmsTask task,List<String> values){
		Double inputQty = Double.valueOf(values.get(0));
		if(inputQty>0 && inputQty<= task.getUnShipQuantityBU()){
			//FDJ_退拣单
			WmsMoveDocDetail moveDocDetail = commonDao.load(WmsMoveDocDetail.class, task.getMoveDocDetail().getId());
			WmsMoveDoc moveDoc = commonDao.load(WmsMoveDoc.class, moveDocDetail.getMoveDoc().getId());
			String type = MyUtils.getMoveType(moveDoc.getType());
			/**========分配========*/
			//源目标库位
			WmsInventory srcInventory = load(WmsInventory.class,task.getDescInventoryId());
			srcInventory.allocatePickup(inputQty);
			commonDao.store(srcInventory);
			//将来如果备货库位需要按照供应商编码分开可以扩展,目前都在一个库位
			String supper = srcInventory.getItemKey().getLotInfo().getSupplier().getCode();
			WmsLocation toLocation = getStockUpLocationByWorkArea(moveDoc.getWarehouse(), type,moveDoc.getBillType().getCode()
					,inputQty,WmsLocationType.STORAGE,supper,Boolean.TRUE,moveDoc.getCompany().getCode());
			
			WmsInventory dstInventory = wmsInventoryManager.getInventoryWithNew(
					toLocation, srcInventory.getItemKey(), srcInventory.getPackageUnit(),srcInventory.getStatus());
			dstInventory.allocatePutaway(inputQty);
			commonDao.store(dstInventory);
			/**========移位========*/
			//原库存扣减库存量/取消拣货分配
			wmsInventoryManager.moveSrcInv(task.getDescInventoryId(),inputQty);
			wmsInventoryManager.addInventoryLog(WmsInventoryLogType.MOVE, -1, moveDoc.getCode(),
					moveDoc.getBillType(), srcInventory.getLocation(), srcInventory.getItemKey(),
					inputQty, srcInventory.getPackageUnit(),
					srcInventory.getStatus(), "退拣");
			//目标库存增加库存量/取消上架分配
			wmsInventoryManager.moveDescInv(dstInventory.getId(),inputQty);
			wmsInventoryManager.addInventoryLog(WmsInventoryLogType.MOVE, 1, moveDoc.getCode(),
					moveDoc.getBillType(), dstInventory.getLocation(), dstInventory.getItemKey(),
					inputQty, dstInventory.getPackageUnit(),
					dstInventory.getStatus(), "退拣");
			WmsLocation fromLocation = commonDao.load(WmsLocation.class, task.getFromLocationId());
			if(fromLocation.getType().equals(WmsLocationType.STORAGE)){
				// 增加目标库位序列号信息
				inventoryExtendManager.addInventoryExtend(dstInventory, task.getPallet()
						, BaseStatus.NULLVALUE,BaseStatus.NULLVALUE,inputQty);
			}
			
			if(moveDoc.isPickTicketType()){
				WmsPickTicketDetail pickTicketDetail = this.commonDao.load(WmsPickTicketDetail.class, moveDocDetail.getRelatedId());
				pickTicketDetail.pickBack(inputQty);
			}
			moveDocDetail.unallocate(inputQty);
			moveDocDetail.cancelMove(inputQty);
			task.editMovedQuantityBU(-inputQty);
			if(task.getMovedQuantityBU()<=0){
				task.removeMove();
			}
			commonDao.store(task);
			if(moveDoc.getUnMoveQuantityBU()>0 && moveDoc.getMovedQuantityBU()>0){
				splitMoveDoc(moveDoc,type);
			}
			workflowManager.doWorkflow(moveDoc, "wmsMoveDocProcess.pickBack");
		}else{
			LocalizedMessage.addLocalizedMessage(MyUtils.font("失败!数量不满足"));
		}
	}
	public Map getWmsScanBolGwt(Map param){
		Map result  = new HashMap();
		String relatedBill1 = param.get(EditWmsScanBol.PARAM_CODE).toString().trim();	// 获得录入的发货单号
		String message = "核单成功"+ "\n" +relatedBill1;
		List<WmsMoveDoc> ms = commonDao.findByQuery("FROM WmsMoveDoc m WHERE (m.pickTicket.relatedBill1 =:relatedBill1" +
				" OR m.pickTicket.code =:relatedBill1)", 
				new String[]{"relatedBill1"}, new Object[]{relatedBill1});
		if(ms!=null && ms.size()>0){
			for(WmsMoveDoc moveDoc : ms){
				List<WmsTask> tasks = commonDao.findByQuery(MyUtils.getUnShipTask(), 
						new String[]{"billCode","type"},
						new Object[]{moveDoc.getCode(),moveDoc.getType()});
				if(tasks.size()>0){
					Double shipQty = 0.0;
					for(WmsTask task : tasks){
						shipQty = task.getUnShipQuantityBU();
						pickShipByTask(task, shipQty, moveDoc);
					}
					moveDoc.setBeScanBol(true);
					moveDoc.setScanBolTime(new Date());
					moveDoc.setShipStatus(WmsMoveDocShipStatus.SHIPPED);
					commonDao.store(moveDoc);
					
					result.put(BusinessNode.MSG, BusinessNode.ON_SUCCESS);
				}else{
					if(moveDoc.isBeScanBol()){
						message = "失败!不允许重复核单:"+ "\n" +relatedBill1;//MyUtils.font2("失败!不允许重复核单:"+relatedBill1);
					}else{
						message = "失败!不存在可发运任务:"+ "\n" +relatedBill1;//MyUtils.font2("失败!不存在可发运任务:"+relatedBill1);
					}
					result.put(BusinessNode.MSG, BusinessNode.SYS_ERROR);
				}
			}
		}else{
			message = "WMS系统找不到对应发货单:"+ "\n" +relatedBill1;//MyUtils.font2("WMS系统找不到对应发货单:"+relatedBill1);
			result.put(BusinessNode.MSG, BusinessNode.SYS_ERROR);
		}
		result.put(EditWmsScanBol.RETURN_NAME, message);
		return result;
	}
	public void pickShipAll(WmsMoveDoc moveDoc){
		List<WmsTask> tasks = commonDao.findByQuery(MyUtils.getUnShipTask(), 
				new String[]{"billCode","type"},
				new Object[]{moveDoc.getCode(),moveDoc.getType()});
		if(tasks.size()>0){
			Double shipQty = 0.0;
			for(WmsTask task : tasks){
				shipQty = task.getUnShipQuantityBU();
				pickShipByTask(task, shipQty, moveDoc);
			}
		}else{
			LocalizedMessage.addLocalizedMessage(MyUtils.font("失败!不存在可发运任务"));
		}
	}
	
	public void pickShipByTask(WmsTask task,Double shipQty,WmsMoveDoc moveDoc){
		WmsMoveDocDetail moveDocDetail = null;
		WmsPickTicketDetail pickTicketDetail =null;
		if(shipQty<=0){
			return;
		}
		WmsInventory dstInventory = load(WmsInventory.class,task.getDescInventoryId());
		dstInventory.removeQuantityBU(shipQty);
		commonDao.store(dstInventory);
		wmsInventoryManager.addInventoryLog(WmsInventoryLogType.SHIPPING, -1, moveDoc.getCode(),
				moveDoc.getBillType(), dstInventory.getLocation(), dstInventory.getItemKey(),
				shipQty, dstInventory.getPackageUnit(),
				dstInventory.getStatus(), "发运确认");
		
		moveDocDetail = commonDao.load(WmsMoveDocDetail.class, task.getMoveDocDetail().getId());
		moveDocDetail.ship(shipQty);
		if(moveDoc.getShippedQuantityBU() >= moveDoc.getMovedQuantityBU() && moveDoc.getShippedQuantityBU() >= moveDoc.getPlanQuantityBU()){
			moveDoc.setShipStatus(WmsMoveDocShipStatus.SHIPPED);
			commonDao.store(moveDoc);
		}
		
		pickTicketDetail = this.commonDao.load(WmsPickTicketDetail.class, 
				moveDocDetail.getRelatedId());
		pickTicketDetail.ship(shipQty);
		
		task.addTiredMovedQuantityBU(shipQty);
		commonDao.store(task);
		
		WmsPickTicket pic = pickTicketDetail.getPickTicket();
		PickTicketBaseShipDecision p = new PickTicketBaseShipDecision();
		String status = p.processAction(pic);
		pic.setStatus(status);
		commonDao.store(pic);
		String hql = "FROM WmsTaskAndStation t WHERE t.task.id =:taskId ";
		List<WmsTaskAndStation> wts = commonDao.findByQuery(hql, "taskId", task.getId());
		for(WmsTaskAndStation wt : wts){
			hql = "FROM WmsBoxType bt WHERE bt.code =:code ";
			WmsBoxType bt = (WmsBoxType) commonDao.findByQueryUniqueResult(hql, new String[]{"code"}, new Object[]{wt.getContainer()});
			if(bt!=null){
				bt.setIsBol(Boolean.FALSE);
				bt.setIsPicking(false);
				commonDao.store(bt);
			}
		}
	}
	
	public void shipRecord(WmsMoveDoc moveDoc){
		WmsBOLStateLog bol = EntityFactory.getEntity(WmsBOLStateLog.class);
		bol.setInputTime(new Date());
		bol.setMoveDoc(moveDoc);
		bol.setType("装车扫描");
		bol.setDriver(moveDoc.getDriver());
		bol.setVehicleNo(moveDoc.getVehicleNo());
		commonDao.store(bol);
	}
	/**
	 * 根据工作区获取备货库位
	 * @return
	 */
	private WmsLocation getStockUpLocationByWorkArea(WmsWarehouse warehouse, WmsMoveDoc moveDoc,WmsWorkDoc workDoc) throws BusinessException {
		Map<String, Object> problem = new HashMap<String, Object>();
		String type = "发货单";
		if(moveDoc.isPickTicketType()){
			type = "发货单";
		}
		if(moveDoc.isWaveType()){
			type = "波次单";
		}
		problem.put("类型", type);
		problem.put("工作区序号", workDoc.getWorkArea().getId());
		problem.put("工作区编码", workDoc.getWorkArea().getCode());
		problem.put("库区序号", workDoc.getWorkArea().getWarehouseArea().getId());
		problem.put("库区编码", workDoc.getWorkArea().getWarehouseArea().getCode());
		
		
		Map<String, Object> resultMap = wmsRuleManager.execute(warehouse.getName(), warehouse.getName(), "备货库位分配规则", problem);
		
		Map<String, Object> result = (Map<String, Object>)resultMap.get("返回对象");
		String locCode = (String)result.get("备货库位");
		if (locCode == null) {
			throw new BusinessException("无法找到对应的备货库位!");
		}
		String hql = "FROM WmsLocation loc WHERE loc.warehouseArea.id = :warehouseAreaId AND loc.code = :code";
		WmsLocation loc = (WmsLocation)commonDao.findByQueryUniqueResult(hql, new String[]{"warehouseAreaId","code"}, new Object[]{workDoc.getWorkArea().getWarehouseArea().getId(),locCode});
		if (loc == null) {
			throw new BusinessException("无法找到对应的备货库位!");
		}
		return loc;
	}
	/**
	 * 根据工作区获取备货库位 yc.min
	 * @return
	 */
	private WmsLocation getStockUpLocationByWorkArea(WmsWarehouse warehouse, String type
			,String billCode,Double quantity,String locationType,String supper
			,Boolean isBack,String company) throws BusinessException {
		
		List<Map<String, Object>> resultObjs = getStockUpLocationByJac(warehouse, type, 
				billCode, quantity, locationType, supper, isBack,company);
		Long locId = null;
		if(resultObjs!=null && resultObjs.size()>0){
			locId = (Long) resultObjs.get(0).get("库位序号");
		}else{
			throw new BusinessException("无法找到对应的备货库位!");
		}
		/*Map<String, Object> result = (Map<String, Object>)resultMap.get("返回对象");
		Long locId = (Long)result.get("备货库位序号");*/
		if (locId == null) {
			throw new BusinessException("无法找到对应的备货库位!");
		}
		WmsLocation loc = commonDao.load(WmsLocation.class, locId);
		if (loc == null) {
			throw new BusinessException("备货库位未新建!");
		}
		return loc;
	}
	
	public List<Map<String, Object>> getStockUpLocationByJac(WmsWarehouse warehouse, String type
			,String billCode,Double quantity,String locationType,String supper
			,Boolean isBack,String company){
		Map<String, Object> problem = new HashMap<String, Object>();
		problem.put("仓库ID", warehouse.getId());
		problem.put("类型", type);
		problem.put("单据类型编码",billCode);
		problem.put("供应商",supper);
		problem.put("数量", quantity);
		problem.put("库位类型", locationType);
		problem.put("是否退拣", isBack?"是":"否");
		problem.put("货主编码", company);
		
		Map<String, Object> resultMap = null;
		List<Map<String, Object>> resultObjs = null;//[{库位序号=21233, 库位代码=1号包装台}]
		try {
			resultMap = wmsRuleManager.execute(warehouse.getName(), warehouse.getName(), "备货库位分配规则", problem);
			resultObjs = (List<Map<String, Object>>) resultMap.get("备货策略库位列表");
		} catch (Exception e) {
			throw new BusinessException(e.getLocalizedMessage());
		}
		return resultObjs;
	}
	public void addTaskForWorkDoc(Long workDocId, WmsTask wmsTask){
		WmsWorkDoc workDoc = commonDao.load(WmsWorkDoc.class, workDocId);

		workDoc.addTask(wmsTask);
		wmsTask.setWorkDoc(workDoc);
		
		workDoc.refreshQuantity();
		
		commonDao.store(workDoc);
		
		workflowManager.doWorkflow(wmsTask, "taskProcess.dispatch");
	}
	
	public void delTaskForWorkDoc(WmsWorkDoc workDoc , WmsTask wmsTask){
		workDoc.getTasks().remove(wmsTask);
		wmsTask.setWorkDoc(null);
		commonDao.store(wmsTask);
		
		workDoc.refreshQuantity();
		commonDao.store(workDoc);
		
		workflowManager.doWorkflow(wmsTask, "taskProcess.undispatch");			
	}

	public RowData getToLocationByTask(Map map) {
		Long taskId = (Long) ((List) map.get("parentIds")).get(0);
		
		WmsTask task = commonDao.load(WmsTask.class, taskId);
		WmsLocation location = commonDao.load(WmsLocation.class, task.getToLocationId());
		
		RowData rowData = new RowData();
		
		rowData.addColumnValue(location.getId());
		rowData.addColumnValue(location.getCode());
		
		return rowData;
	}
	
	public Double getUnWorkQuantityByPageMap(Map map) {
		Long taskId = (Long) map.get("task.id");
		WmsTask task = commonDao.load(WmsTask.class, taskId);
		return task.getUnmovedQuantityBU();
	}
	
	public 	void upBolTagsNum(WmsBOL bol){
//		System.out.println("001::"+JavaTools.format(new Date(), JavaTools.dmy_hms));
		String hql = "SELECT count(distinct detail.boxTag) as boxTag FROM WmsBOLDetail detail WHERE detail.bol.id =:bolId ";
		Long tags = (Long) commonDao.findByQueryUniqueResult(hql, "bolId",bol.getId());
		bol.setBoxTagNums(Integer.parseInt(tags==null?"0":tags.toString()));
		commonDao.store(bol);
//		System.out.println("002::"+JavaTools.format(new Date(), JavaTools.dmy_hms));
	}
	//更新标签状态为发运
	public void upBolTagsShip(WmsBOL bol,List<String> boxTags,List<String> containers,List<Long> wmsTaskAndStationIds){
		if(wmsTaskAndStationIds!=null && wmsTaskAndStationIds.size()>0){
			for(Long id : wmsTaskAndStationIds){
				WmsTaskAndStation wt = commonDao.load(WmsTaskAndStation.class, id);
				wt.setShipStatus(WmsMoveDocShipStatus.SHIPPED);
				commonDao.store(wt);
			}
		}else{//业务正常此步骤可注释
			String hql = "FROM WmsTaskAndStation "+WmsTaskAndStation.short_name+
					" WHERE "+WmsTaskAndStation.short_name+".station.boxTag in(:boxTags)" +
					" AND "+WmsTaskAndStation.short_name+".container in (:containers)";
			List<WmsTaskAndStation> wts = commonDao.findByQuery(hql, 
					new String[]{"boxTags","containers"}, new Object[]{boxTags,containers});
			for(WmsTaskAndStation wt : wts){
				wt.setShipStatus(WmsMoveDocShipStatus.SHIPPED);
				commonDao.store(wt);
			}
		}
	}
}