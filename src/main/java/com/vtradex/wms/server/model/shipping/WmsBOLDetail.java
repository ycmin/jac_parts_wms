package com.vtradex.wms.server.model.shipping;

import java.util.Date;

import com.vtradex.thorn.server.model.Entity;
import com.vtradex.wms.server.model.base.BaseStatus;
import com.vtradex.wms.server.model.inventory.WmsItemKey;
import com.vtradex.wms.server.model.move.WmsTask;
import com.vtradex.wms.server.model.organization.WmsPackageUnit;

public class WmsBOLDetail extends Entity {

	private static final long serialVersionUID = 869113615134440371L;
	/**  行号*/
	private Integer lineNo;
	
	/** 物料批次 */
	private WmsItemKey itemKey;
	
	/** 包装 */
	private WmsPackageUnit packageUnit;
	
	/** 物料总数 */
	private Double quantity=0D;
	
	/** 物料数量BU */
	private Double quantityBU=0D;
	
	/** 已发运数量 */
	private Double shippedQuantity=0D;
	
	/** 托盘号/料架号 */
	private String pallet = BaseStatus.NULLVALUE;
	
	/** 箱号 */
	private String container;
	
	/** 序列号 */
	private String serialNumber;
	
	private String status = WmsBOLStatus.OPEN;
	/** 装车单*/
	private WmsBOL bol;
	
	private String tfd; //投放点
	
	private String slr; //送料人
	
	private String subCode; //子单号
	
	private Boolean beReturn = Boolean.FALSE; //是否回单
	
	private WmsTask task ;
	
	/**子单号生效时间*/
	private Date activeTime;
	
	/**签收数量*/
	private Double signQty = 0D;
	
	/**生产线*/
	private String productionLine;	
	
	/** 要求达到时间 */
	private Date requireArriveDate;
	
	/**器具标签*/
	private String boxTag;
	/**WmsTaskAndStation*/
	private Long wmsTaskAndStationId;
	/**相同子单号下相同物料的配送总量*/
	private Double itemSubQty = 0D;
	
	public Double getItemSubQty() {
		return itemSubQty;
	}

	public void setItemSubQty(Double itemSubQty) {
		this.itemSubQty = itemSubQty;
	}

	public Long getWmsTaskAndStationId() {
		return wmsTaskAndStationId;
	}

	public void setWmsTaskAndStationId(Long wmsTaskAndStationId) {
		this.wmsTaskAndStationId = wmsTaskAndStationId;
	}

	public String getBoxTag() {
		return boxTag;
	}

	public void setBoxTag(String boxTag) {
		this.boxTag = boxTag;
	}

	public Double getSignQty() {
		return signQty;
	}

	public void setSignQty(Double signQty) {
		this.signQty = signQty;
	}

	public WmsTask getTask() {
		return task;
	}

	public void setTask(WmsTask task) {
		this.task = task;
	}

	public Date getActiveTime() {
		return activeTime;
	}

	public void setActiveTime(Date activeTime) {
		this.activeTime = activeTime;
	}

    public Boolean getBeReturn() {
        return beReturn;
    }

    public void setBeReturn(Boolean beReturn) {
        this.beReturn = beReturn;
    }

    public String getTfd() {
		return tfd;
	}

	public void setTfd(String tfd) {
		this.tfd = tfd;
	}

	public Integer getLineNo() {
		return lineNo;
	}

	public void setLineNo(Integer lineNo) {
		this.lineNo = lineNo;
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

	public Double getShippedQuantity() {
		return shippedQuantity;
	}

	public void setShippedQuantity(Double shippedQuantity) {
		this.shippedQuantity = shippedQuantity;
	}

	public String getPallet() {
		return pallet;
	}

	public void setPallet(String pallet) {
		this.pallet = pallet;
	}

	public String getContainer() {
		return container;
	}

	public void setContainer(String container) {
		this.container = container;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public WmsBOL getBol() {
		return bol;
	}

	public void setBol(WmsBOL bol) {
		this.bol = bol;
	}

    public String getSlr() {
        return slr;
    }

    public void setSlr(String slr) {
        this.slr = slr;
    }

    public String getSubCode() {
        return subCode;
    }

    public void setSubCode(String subCode) {
        this.subCode = subCode;
    }

	public String getProductionLine() {
		return productionLine;
	}

	public void setProductionLine(String productionLine) {
		this.productionLine = productionLine;
	}

	public Date getRequireArriveDate() {
		return requireArriveDate;
	}

	public void setRequireArriveDate(Date requireArriveDate) {
		this.requireArriveDate = requireArriveDate;
	}
	/**可签收量*/
	public Double availableSignQty(){
		Double qq = this.signQty == null?0D:this.signQty;
		return this.quantityBU-qq;
	}
}
