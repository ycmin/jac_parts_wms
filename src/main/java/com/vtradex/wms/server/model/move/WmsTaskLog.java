package com.vtradex.wms.server.model.move;

import java.util.Date;

import com.vtradex.thorn.server.model.Entity;
import com.vtradex.wms.server.model.inventory.WmsItemKey;
import com.vtradex.wms.server.model.organization.WmsPackageUnit;
import com.vtradex.wms.server.model.warehouse.WmsWorker;


/**
 * @category 作业日志
 * @author peng.lei
 * @version 1.0
 * @created 15-二月-2011 10:08:01
 */
public class WmsTaskLog extends Entity{
	private static final long serialVersionUID = -922365314113787866L;
	
	/** 所属任务*/
	private WmsTask task;
	/** 移出库位ID */
	private Long fromLocationId;
	/** 移出库位 */
	private String fromLocationCode;
	/** 移出托盘*/
	private String fromPallet;
	/** 移出箱号 */
	private String fromCarton;
	/** 移出序列号 */
	private String fromSerialNo;
	/** 移入库位ID */
	private Long toLocationId;
	/** 移入库位 */
	private String toLocationCode;
	/** 移入托盘*/
	private String toPallet;
	/** 移入箱号 */
	private String toCarton;
	/** 批次属性*/
	private WmsItemKey itemKey;
	/** 包装单位*/
	private WmsPackageUnit packageUnit;
	/** 库存状态*/
	private String inventoryStatus;
	
	/** 移位数量 */
	private Double movedQuantity = 0D;
	/** 移位数量BU */
	private Double movedQuantityBU = 0D;
	/** 退拣数量BU */
	private Double pickBackQuantityBU = 0D;
	/** 实际操作人员*/
	private WmsWorker worker;
	/** 存货日期 */
	private Date storageDate;
	/** 生产日期 */
	private Date productDate;
	/** 保质日期 */
	private Date expireDate;
	/** 近效期 */
	private Date warnDate;
	/** 收货日期 */
	private Date receivedDate;
	
	/** 关联源库存ID */
	private Long srcInventoryId;
	/** 关联目标库存ID */
	private Long descInventoryId;
	
	private boolean bePickBack = false;
	
	public WmsTaskLog(){

	}
	
	public Double getPickBackQuantityBU() {
		return pickBackQuantityBU;
	}


	public void setPickBackQuantityBU(Double pickBackQuantityBU) {
		this.pickBackQuantityBU = pickBackQuantityBU;
	}


	public boolean isBePickBack() {
		return bePickBack;
	}


	public void setBePickBack(boolean bePickBack) {
		this.bePickBack = bePickBack;
	}


	public WmsTask getTask() {
		return task;
	}

	public void setTask(WmsTask task) {
		this.task = task;
	}

	public Long getFromLocationId() {
		return fromLocationId;
	}

	public void setFromLocationId(Long fromLocationId) {
		this.fromLocationId = fromLocationId;
	}

	public String getFromLocationCode() {
		return fromLocationCode;
	}

	public void setFromLocationCode(String fromLocationCode) {
		this.fromLocationCode = fromLocationCode;
	}

	public String getFromPallet() {
		return fromPallet;
	}

	public void setFromPallet(String fromPallet) {
		this.fromPallet = fromPallet;
	}

	public String getFromCarton() {
		return fromCarton;
	}

	public void setFromCarton(String fromCarton) {
		this.fromCarton = fromCarton;
	}

	public String getFromSerialNo() {
		return fromSerialNo;
	}

	public void setFromSerialNo(String fromSerialNo) {
		this.fromSerialNo = fromSerialNo;
	}

	public Long getToLocationId() {
		return toLocationId;
	}

	public void setToLocationId(Long toLocationId) {
		this.toLocationId = toLocationId;
	}

	public String getToLocationCode() {
		return toLocationCode;
	}

	public void setToLocationCode(String toLocationCode) {
		this.toLocationCode = toLocationCode;
	}

	public String getToPallet() {
		return toPallet;
	}

	public void setToPallet(String toPallet) {
		this.toPallet = toPallet;
	}

	public String getToCarton() {
		return toCarton;
	}

	public void setToCarton(String toCarton) {
		this.toCarton = toCarton;
	}

	public WmsItemKey getItemKey() {
		return itemKey;
	}

	public void setItemKey(WmsItemKey itemKey) {
		this.itemKey = itemKey;
	}

	public WmsPackageUnit getPackageUnit() {
		return packageUnit;
	}

	public void setPackageUnit(WmsPackageUnit packageUnit) {
		this.packageUnit = packageUnit;
	}

	public String getInventoryStatus() {
		return inventoryStatus;
	}

	public void setInventoryStatus(String inventoryStatus) {
		this.inventoryStatus = inventoryStatus;
	}

	public Double getMovedQuantity() {
		return movedQuantity;
	}

	public void setMovedQuantity(Double movedQuantity) {
		this.movedQuantity = movedQuantity;
	}

	public Double getMovedQuantityBU() {
		return movedQuantityBU;
	}

	public void setMovedQuantityBU(Double movedQuantityBU) {
		this.movedQuantityBU = movedQuantityBU;
	}

	public WmsWorker getWorker() {
		return worker;
	}

	public void setWorker(WmsWorker worker) {
		this.worker = worker;
	}

	public Date getStorageDate() {
		return storageDate;
	}

	public void setStorageDate(Date storageDate) {
		this.storageDate = storageDate;
	}

	public Date getProductDate() {
		return productDate;
	}

	public void setProductDate(Date productDate) {
		this.productDate = productDate;
	}

	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}

	public Date getWarnDate() {
		return warnDate;
	}

	public void setWarnDate(Date warnDate) {
		this.warnDate = warnDate;
	}

	public Date getReceivedDate() {
		return receivedDate;
	}

	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}

	public Long getSrcInventoryId() {
		return srcInventoryId;
	}

	public void setSrcInventoryId(Long srcInventoryId) {
		this.srcInventoryId = srcInventoryId;
	}

	public Long getDescInventoryId() {
		return descInventoryId;
	}

	public void setDescInventoryId(Long descInventoryId) {
		this.descInventoryId = descInventoryId;
	}

	public void finalize() throws Throwable {

	}

	
	/**
	 * 退拣
	 * @param quantityBU
	 */
	public void pickBack(Double quantityBU) {
		this.pickBackQuantityBU += quantityBU;
		this.task.pickBak(quantityBU);
	}

	/**
	 * 获取实际的移位数量BU
	 * @return
	 */
	public Double getRealMovedQuantityBU() {
//		return this.movedQuantityBU - this.pickBackQuantityBU;
		return this.movedQuantityBU;
	}

	/**
	 * 更新日志中的箱件数
	 */
	public void refreshBoxQty() {
		if (this.getPackageUnit().getLineNo() > 1) {
			this.movedQuantity = this.getRealMovedQuantityBU() / this.getPackageUnit().getConvertFigure();
		}else{
			this.movedQuantity = this.movedQuantityBU;
		}
	}
}