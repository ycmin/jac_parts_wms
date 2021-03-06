package com.vtradex.wms.server.model.inventory;

import com.vtradex.thorn.server.model.Entity;
import com.vtradex.wms.server.model.base.LotInfo;
import com.vtradex.wms.server.model.organization.WmsBillType;
import com.vtradex.wms.server.model.organization.WmsOrganization;
import com.vtradex.wms.server.model.organization.WmsPackageUnit;
import com.vtradex.wms.server.model.warehouse.WmsLocation;
import com.vtradex.wms.server.model.warehouse.WmsWarehouse;
import com.vtradex.wms.server.utils.PackageUtils;


/**
 * @category 库存日志
 * @author peng.lei
 * @version 1.0
 * @created 15-二月-2011 10:06:58
 */
public class WmsInventoryLog extends Entity{
	private static final long serialVersionUID = 6456489656159652471L;

	/** 仓库*/
	private WmsWarehouse warehouse;
	/** 日志类型*/
	private String logType;
	/** 增减方式 说明(0：不变、1：增加、-1：减少)*/
	private Integer inOrOut;
	/** 相关单据号*/
	private String billCode;
	/** 相关单据类型*/
	private WmsBillType billType;
	/** 库位ID*/
	private Long locationId;
	/** 库位 */
	private String locationCode;
	/** 包装单位*/
	private String packageUnit;
	/** 库存状态*/
	private String inventoryStatus;
	/** 数量*/
	private Double quantity=0D;
	/** 数量BU*/
	private Double quantityBU=0D;
	/**关联日志ID**/
	private Long relatedLogId;
	/**
	 * itemKey id
	 */
	private Long itemKeyId;
	
	/**货品ID*/
	private Long itemId;
	/**货品编码*/
	private String itemCode;
	/**货品名称*/
	private String itemName;
	/**批次号*/
	private String lot;
	/**货主*/
	private Long companyId;
	/**批次属性*/
	private LotInfo lotInfo;
	/**货主名称*/
	private String company;
	/**包装ID*/
	private Long packageUnitId;
	/**备注*/
	private String description;
	public WmsInventoryLog(){

	}
	
	public WmsInventoryLog(String logType, Integer inOrOut, String relatedBill,WmsBillType billType, WmsLocation location, WmsOrganization company,
			WmsItemKey itemKey,Double quantityBU,WmsPackageUnit packageUnit, String status, String description) {
		super();
		this.warehouse = location.getWarehouse();
		this.logType = logType;
		this.inOrOut = inOrOut;
		this.billCode = relatedBill;
		this.billType = billType;
		if(itemKey!=null){
			this.itemKeyId = itemKey.getId();
			this.itemId = itemKey.getItem().getId();
			this.itemCode = itemKey.getItem().getCode();
			this.itemName = itemKey.getItem().getName();
			this.lot = itemKey.getLot();
			this.lotInfo = itemKey.getLotInfo();
		}
		this.quantityBU = quantityBU;
		if(packageUnit!=null){
			this.packageUnit = packageUnit.getUnit();
			this.quantity = PackageUtils.convertPackQuantity(quantityBU, packageUnit);
			this.packageUnitId = packageUnit.getId();
		}
		this.inventoryStatus = status;
		this.locationId = location.getId();
		this.locationCode = location.getCode();
		if(company!=null){
			this.company = company.getName();
			this.companyId = company.getId();
		}
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public WmsWarehouse getWarehouse() {
		return warehouse;
	}

	public void setWarehouse(WmsWarehouse warehouse) {
		this.warehouse = warehouse;
	}

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public Integer getInOrOut() {
		return inOrOut;
	}

	public void setInOrOut(Integer inOrOut) {
		this.inOrOut = inOrOut;
	}

	public String getBillCode() {
		return billCode;
	}

	public void setBillCode(String billCode) {
		this.billCode = billCode;
	}

	public WmsBillType getBillType() {
		return billType;
	}

	public void setBillType(WmsBillType billType) {
		this.billType = billType;
	}

	public Long getLocationId() {
		return locationId;
	}

	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}

	public String getLocationCode() {
		return locationCode;
	}

	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}

	public String getPackageUnit() {
		return packageUnit;
	}

	public void setPackageUnit(String packageUnit) {
		this.packageUnit = packageUnit;
	}

	public String getInventoryStatus() {
		return inventoryStatus;
	}

	public void setInventoryStatus(String inventoryStatus) {
		this.inventoryStatus = inventoryStatus;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	public Double getQuantityBU() {
		return quantityBU;
	}

	public void setQuantityBU(Double quantityBU) {
		this.quantityBU = quantityBU;
	}

	public Long getRelatedLogId() {
		return relatedLogId;
	}

	public void setRelatedLogId(Long relatedLogId) {
		this.relatedLogId = relatedLogId;
	}

	public Long getItemKeyId() {
		return itemKeyId;
	}

	public void setItemKeyId(Long itemKeyId) {
		this.itemKeyId = itemKeyId;
	}
	
	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getLot() {
		return lot;
	}

	public void setLot(String lot) {
		this.lot = lot;
	}

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	public LotInfo getLotInfo() {
		return lotInfo;
	}

	public void setLotInfo(LotInfo lotInfo) {
		this.lotInfo = lotInfo;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public Long getPackageUnitId() {
		return packageUnitId;
	}

	public void setPackageUnitId(Long packageUnitId) {
		this.packageUnitId = packageUnitId;
	}

	public void finalize() throws Throwable {

	}

}