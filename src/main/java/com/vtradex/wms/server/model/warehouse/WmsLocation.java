package com.vtradex.wms.server.model.warehouse;

import java.util.Date;

import com.vtradex.thorn.server.annotation.UniqueKey;
import com.vtradex.thorn.server.exception.OriginalBusinessException;
import com.vtradex.thorn.server.model.VersionalEntity;
/**
 * @category 库位
 * @author shengpei.zhang
 * @version 1.0
 * @created 15-二月-2011 10:04:38
 */
public class WmsLocation extends VersionalEntity{
	private static final long serialVersionUID = -1045233584854409285L;
	
	public static final String EMPTY = "-";
	public static final String STORE = "存货";
	public static final String PICK = "拣货";
	
	public WmsLocation(){
	}
	/** 仓库*/	
	@UniqueKey
	private WmsWarehouse warehouse;
	/** 库区*/
	private WmsWarehouseArea warehouseArea;
	
	/** 
	 * 库位类型
	 *
	 * {@link WmsLocationType}
	 */
	private String type;
	
	/** 库位代码*/
	@UniqueKey
	private String code;
	/** 检验码*/
	private String verifyCode;
	/** 区*/
	private Integer zone = 0;
	/** 排*/
	private Integer line = 0;
	/** 列*/
	private Integer column = 0;
	/** 层*/
	private Integer layer = 0;
	/**
	 * 说明：动线号，可以重复
	 */
	private Integer routeNo = 0;
	/**
	 * 说明：库位盘点锁
	 */
	private Boolean lockCount = Boolean.FALSE;
	
	/**
	 * 备货后对应退拣库位
	 */
	private WmsLocation pickBackLoc;
	/** 状态 */
	private String status;	
	
	/**库存占用比例*/
	private Double usedRate=0D;
	
    /** 空拣存状态*/
    private String locationStatus= WmsLocationStatus.EMPTY;;
    
    /** 最后盘点日期*/
    private Date cycleDate;
    
    /** 最后动碰日期*/
    private Date touchDate;
    
    /**动碰次数*/
    private Integer touchCount = 0;
    
    /**占用托数*/
	private  Integer palletQuantity=0;
	
	/**
	 * 异常标志(RF上架，拣货在扫描库位输入0时，更新当前推荐库位的异常标志为true)
	 */
	private Boolean exceptionFlag = Boolean.FALSE;
	
	public Boolean getExceptionFlag() {
		return exceptionFlag;
	}

	public void setExceptionFlag(Boolean exceptionFlag) {
		this.exceptionFlag = exceptionFlag;
	}

	public Integer getPalletQuantity() {
		return palletQuantity;
	}

	public void setPalletQuantity(Integer palletQuantity) {
		this.palletQuantity = palletQuantity;
	}

	public Double getUsedRate() {
		return usedRate;
	}

	public void setUsedRate(Double usedRate) {
		this.usedRate = usedRate;
	}

	public String getLocationStatus() {
		return locationStatus;
	}

	public void setLocationStatus(String locationStatus) {
		this.locationStatus = locationStatus;
	}

	public Date getCycleDate() {
		return cycleDate;
	}

	public void setCycleDate(Date cycleDate) {
		this.cycleDate = cycleDate;
	}

	public Date getTouchDate() {
		return touchDate;
	}

	public void setTouchDate(Date touchDate) {
		this.touchDate = touchDate;
	}

	public Integer getTouchCount() {
		return touchCount;
	}

	public void setTouchCount(Integer touchCount) {
		this.touchCount = touchCount;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getColumn() {
		return column;
	}

	public void setColumn(Integer column) {
		this.column = column;
	}

	public Integer getLayer() {
		return layer;
	}

	public void setLayer(Integer layer) {
		this.layer = layer;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public Boolean getLockCount() {
		return lockCount;
	}

	public void setLockCount(Boolean lockCount) {
		this.lockCount = lockCount;
		// 写库存日志
		//WmsInventoryManager wmsInventoryManager = (WmsInventoryManager) applicationContext.getBean("wmsInventoryManager");
		//wmsInventoryManager.addInventoryLog(lockCount?WmsInventoryLogType.LOCK:WmsInventoryLogType.UNLOCK, 0, 
		//		null, null, this, null, 0D, null, locationStatus, "库位盘点锁");
		
		// 写库存日志
//		WmsInventoryLog log = new WmsInventoryLog(
//				lockCount?WmsInventoryLogType.LOCK:WmsInventoryLogType.UNLOCK, 0, null,null,
//				this, null,null,
//				0D, null,
//				locationStatus, "库位盘点锁");
//		messageService.storeEntity(log);
	}


	public WmsLocation getPickBackLoc() {
		return pickBackLoc;
	}

	public void setPickBackLoc(WmsLocation pickBackLoc) {
		this.pickBackLoc = pickBackLoc;
	}

	public Integer getRouteNo() {
		return routeNo;
	}

	public void setRouteNo(Integer routeNo) {
		this.routeNo = routeNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public WmsWarehouse getWarehouse() {
		return warehouse;
	}

	public void setWarehouse(WmsWarehouse warehouse) {
		this.warehouse = warehouse;
	}

	public WmsWarehouseArea getWarehouseArea() {
		return warehouseArea;
	}

	public void setWarehouseArea(WmsWarehouseArea warehouseArea) {
		this.warehouseArea = warehouseArea;
	}

	public Integer getZone() {
		return zone;
	}

	public String getVerifyCode() {
		return verifyCode;
	}

	public void setVerifyCode(String verifyCode) {
		this.verifyCode = verifyCode;
	}

	public void setZone(Integer zone) {
		this.zone = zone;
	}
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void finalize() throws Throwable {

	}

	/**
	 * 盘点锁
	 * */
	public void countLockLocations(){
		if(!this.lockCount){
			this.lockCount = Boolean.TRUE;
		}else{
			throw new OriginalBusinessException("库位【"+this.getCode()+"】已经处于盘点状态");
		}
	}
	/**
	 * 盘点解锁
	 * */
	public void countUnLockLocations(){
		if(this.lockCount){
			this.lockCount = Boolean.FALSE;
		}
		this.exceptionFlag = Boolean.FALSE;
	}
	
	/**
	 * 更新动碰次数，超过1000次后不再累加次数
	 */
	public void addTouchCount() {
		this.touchDate = new Date();
		if (this.touchCount < 1000) {
			this.touchCount ++;
		}
	}
	
	/**
	 * 增加库位托盘数
	 * @param palletQty
	 */
	public void addPallet(Integer palletQty) {
		this.palletQuantity += palletQty;
	}

	/**
	 * 减少库位托盘数
	 * @param palletQty
	 */
	public void removePallet(Integer palletQty) {
		this.palletQuantity -= palletQty;
		if(palletQuantity <= 0){
			this.palletQuantity = 0;
		}
	}
}