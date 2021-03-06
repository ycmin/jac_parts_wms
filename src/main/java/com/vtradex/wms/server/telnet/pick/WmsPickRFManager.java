package com.vtradex.wms.server.telnet.pick;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.vtradex.thorn.server.exception.BusinessException;
import com.vtradex.wms.server.model.carrier.WmsVehicle;
import com.vtradex.wms.server.model.inventory.WmsInventory;
import com.vtradex.wms.server.model.warehouse.WmsWorker;
import com.vtradex.wms.server.telnet.dto.WmsBOLDTO;
import com.vtradex.wms.server.telnet.dto.WmsPickContainerDTO;
import com.vtradex.wms.server.telnet.dto.WmsPickTaskDTO;
import com.vtradex.wms.server.telnet.manager.WmsCommonRFManager;

@Transactional
public interface WmsPickRFManager extends WmsCommonRFManager{
	/**
	 * 通过作业单号获取拣选任务
	 * @param workDocCode
	 * @return
	 */
	@Transactional
	WmsPickTaskDTO findPickTaskById(Long workDocId);
	/**
	 * 校验作业单号是否合法
	 * @param workDocCode
	 */
	@Transactional
	Long checkWorkDocCode(String workDocCode);
	/**
	 * 拣选确认
	 * @param pickTaskDTO
	 * @param locationCode
	 * @param quantity
	 */
	@Transactional
	void confirmPick(WmsPickTaskDTO pickTaskDTO , String locationCode , Double quantity);
	
	@Transactional
	void markExceptionWmsLocation(Long locationId) throws BusinessException;
	
	/**
	 * 重新分配库位
	 */
	@Transactional
	void resetAllocate(WmsPickTaskDTO pickTaskDTO);
	/**拣货扫码*/
	@Transactional
	String pickConfirmAll(String pickNo);
	
	/**发运登记*/
	@Transactional
	String shipRecord(String pickNo,String vehicleNo);
	
	/**
	 * 获取容器信息
	 */
	@Transactional
	WmsPickContainerDTO findPickContainer(WmsWorker blg);
	
	/**
	 * 容器拣选确认
	 */
	@Transactional
	void confimPickByContainer(WmsPickContainerDTO dto,Boolean isFinished);
	
	/**
	 * 根据库位获取库存可用数量
	 */
	@Transactional
	WmsInventory getInventoryQtyByLocation(String location,String itemCode,String supplier);
	
	/**
	 * 获取Task通过moveDocId
	 */
	@Transactional
	WmsPickContainerDTO getWmsTaskByMoveDocId(Long moveDetailId,String picktype);
	
	/**
	 * 器具拣选确认
	 */
	@Transactional
	WmsPickContainerDTO confimPickByPart(WmsPickContainerDTO dto,String picktype);
	
	Double viewQty(Long moveDocDetailId,String boxTag);
	/**
	 * 校验器具编码是否存在
	 */
	@Transactional
	String checkContainerByBoxType(String container,String type,String picktype,Long moveDocId);
	
	/**
	 * 校验车牌号信息
	 */
	@Transactional
	WmsVehicle checkVehicleByLicense(String license);
	
	/**
	 * 校验器具是否拣货
	 * 且完成  yc
	 */
	@Transactional
	Map checkMoveDocByContainer(String container,Map<String,WmsBOLDTO> dtos);
	
	/**
	 * 创建装车单BOL
	 */
	@Transactional
	void createWmsBol(Map<String, WmsBOLDTO> dtos,String license);
	
	List<Object[]> findUnPickMove(Long moveDocId);
	@Transactional
	WmsPickContainerDTO getBindByContainerId(Long moveDetailId,WmsPickContainerDTO dto);
	/**获取时序件容器汇总信息*/
	List<Object[]> findUnAppliance(Long moveDocId);
	/**获取容器拣货信息*/
	Integer sumPickQtys(Long moveDocId,String boxTagNo,String container);
	/**获取时序件容器明细信息*/
	List<Object[]> findUnApplianceItems(Long moveDocId,String boxTagNo);
	/**RF作业相关日志记录*/
	@Transactional
	void saveLogs(String logType,String logTitle,String exception,String message);
	/**容器释放*/
	@Transactional
	void releaseContainer(String container);
	/**查找已拣货但未加入BOL的信息*/
	Map checkContainerPicking(String container);
	/**封装已拣货但未加入BOL明细信息*/
	Map getContainerList(List<Long> ids,String container);
	/**查询箱型器具关系拣货任务是否多条*/
	Map checkContainerList(Long id);
	/**容器退拣*/
	Map containerPickBack(Long id,String supCode,Double pickBackQty,String descLoc);
}
