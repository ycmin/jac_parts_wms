package com.vtradex.wms.server.model.shipping;

import java.util.Date;

import com.vtradex.thorn.server.annotation.UniqueKey;
import com.vtradex.thorn.server.model.Entity;
import com.vtradex.wms.server.model.base.ShipLotInfo;
import com.vtradex.wms.server.model.organization.WmsItem;
import com.vtradex.wms.server.model.organization.WmsOrganization;
import com.vtradex.wms.server.model.organization.WmsPackageUnit;
import com.vtradex.wms.server.utils.PackageUtils;

/**
 * 发货单明细 
 *
 * @category Entity 
 * @author <a href="brofe.pan@gmail.com">潘宁波</a>
 * @version $Revision: 1.6 $Date: 2015/06/04 02:15:25 $
 */
public class WmsPickTicketDetail extends Entity {
	
	private static final long serialVersionUID = 2465020137115072863L;
	
	/** 发货单*/
	@UniqueKey
	private WmsPickTicket pickTicket;
	
	/**  行号*/
	@UniqueKey
	private Integer lineNo;
	
	/**  货品*/
	private WmsItem item;
	
	/**  批次属性要求*/
	private ShipLotInfo shipLotInfo = new ShipLotInfo();
	
	/**  包装*/
	private WmsPackageUnit packageUnit;
	
	/**  期待数量*/
	private Double expectedQuantity = 0.0;
	/**  期待数量BU*/
	private Double expectedQuantityBU = 0.0;
	/**  分配数量BU*/
	private Double allocatedQuantityBU = 0.0;
	/**  拣货数量BU*/
	private Double pickedQuantityBU = 0.0;
	/**  发货数量BU*/
	private Double shippedQuantityBU = 0.0;
	
	/** 库存状态要求 */
	private String inventoryStatus;
	/**================JAC============================*/
	/**  验收数量BU*/
	private Double backQuantityBU = 0.0;
	/**扫码人*/
	private String smWorker;
	/**扫码时间*/
	private Date smDate;
	/** 供应商*/
	private WmsOrganization supplier;
	/**确定发货单明细对应接口明细关系*/
	private String hashCode;
	/**明细备注-当明细备注不为空,激活时要人工选择物料工艺状态*/
	private String description;
	
	/**===============XG==============================*/
	/** 需求时间*/
	private Date needTime;
	/** 备料工号*/
	private String pickWorkerCode;
	/** 备料工*/
	private String pickWorker;
	/**生产线*/
	private String productionLine;	
	
	/**工位*/
	private String station;
	/**是否集配*/
	private Boolean isJp;
	/**顺序*/
	private Integer sx;
	/**最小包装量*/
	private Double minQty; 
	/**包装单位*/
	private String pcs;
	/**器具型号,器具容量,器具数量,备注*/
	private String type;
	private Double packageNum;
	private Double 	packageQty;
	private String remark;
	
	//来源
	private String fromSource;
	//========批拣信息
	/**批拣编码*/
	private String lotPickCode;
	
	public WmsPickTicketDetail(WmsPickTicket pickTicket, WmsItem item,
			ShipLotInfo shipLotInfo, WmsPackageUnit packageUnit,
			Double expectedQuantity, Double expectedQuantityBU,
			Double allocatedQuantityBU, Double pickedQuantityBU,
			Double shippedQuantityBU, String pickWorkerCode, String pickWorker,
			String productionLine, String station, Boolean isJp,Integer lineNo,
			Integer sx,Double minQty,String pcs,WmsOrganization supplier,
			String inventoryStatus) {
		super();
		this.pickTicket = pickTicket;
		this.item = item;
		this.shipLotInfo = shipLotInfo;
		this.packageUnit = packageUnit;
		this.expectedQuantity = expectedQuantity;
		this.expectedQuantityBU = expectedQuantityBU;
		this.allocatedQuantityBU = allocatedQuantityBU;
		this.pickedQuantityBU = pickedQuantityBU;
		this.shippedQuantityBU = shippedQuantityBU;
		this.pickWorkerCode = pickWorkerCode;
		this.pickWorker = pickWorker;
		this.productionLine = productionLine;
		this.station = station;
		this.isJp = isJp;
		this.lineNo = lineNo;
		this.sx = sx;
		this.minQty = minQty;
		this.pcs = pcs;
		this.supplier = supplier;
		this.inventoryStatus = inventoryStatus;
	}

	public String getLotPickCode() {
		return lotPickCode;
	}

	public void setLotPickCode(String lotPickCode) {
		this.lotPickCode = lotPickCode;
	}

	public String getFromSource() {
		return fromSource;
	}

	public void setFromSource(String fromSource) {
		this.fromSource = fromSource;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double getPackageNum() {
		return packageNum;
	}

	public void setPackageNum(Double packageNum) {
		this.packageNum = packageNum;
	}

	public Double getPackageQty() {
		return packageQty;
	}

	public void setPackageQty(Double packageQty) {
		this.packageQty = packageQty;
	}

	public String getPcs() {
		return pcs;
	}

	public void setPcs(String pcs) {
		this.pcs = pcs;
	}

	public Double getMinQty() {
		return minQty;
	}

	public void setMinQty(Double minQty) {
		this.minQty = minQty;
	}

	public Integer getSx() {
		return sx;
	}

	public void setSx(Integer sx) {
		this.sx = sx;
	}

	public Boolean getIsJp() {
		return isJp;
	}

	public void setIsJp(Boolean isJp) {
		this.isJp = isJp;
	}

	public String getStation() {
		return station;
	}

	public void setStation(String station) {
		this.station = station;
	}

	public WmsPickTicketDetail() {
	}
	
	public WmsPickTicketDetail(WmsPickTicket pickTicket, Integer lineNo,
			WmsItem item, ShipLotInfo shipLotInfo, WmsPackageUnit packageUnit,
			Double expectedQuantity, Double expectedQuantityBU,
			Double allocatedQuantityBU, Double pickedQuantityBU,
			Double shippedQuantityBU,WmsOrganization supplier,String invStatus) {
		super();
		this.pickTicket = pickTicket;
		this.lineNo = lineNo;
		this.item = item;
		this.shipLotInfo = shipLotInfo;
		this.packageUnit = packageUnit;
		this.expectedQuantity = expectedQuantity;
		this.expectedQuantityBU = expectedQuantityBU;
		this.allocatedQuantityBU = allocatedQuantityBU;
		this.pickedQuantityBU = pickedQuantityBU;
		this.shippedQuantityBU = shippedQuantityBU;
		this.supplier = supplier;
		this.inventoryStatus = invStatus;
	}

	public String getPickWorkerCode() {
		return pickWorkerCode;
	}

	public void setPickWorkerCode(String pickWorkerCode) {
		this.pickWorkerCode = pickWorkerCode;
	}

	public Date getNeedTime() {
		return needTime;
	}


	public void setNeedTime(Date needTime) {
		this.needTime = needTime;
	}

	public String getPickWorker() {
		return pickWorker;
	}

	public void setPickWorker(String pickWorker) {
		this.pickWorker = pickWorker;
	}

	public String getProductionLine() {
		return productionLine;
	}


	public void setProductionLine(String productionLine) {
		this.productionLine = productionLine;
	}


	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setHashCode(String hashCode){
		this.hashCode = hashCode;
	}
	public String getHashCode(){
		return hashCode;
	}
	public void setSupplier(WmsOrganization supplier){
		this.supplier = supplier;
	}
	public WmsOrganization getSupplier(){
		return supplier;
	}
	public void setSmDate(Date smDate){
		this.smDate = smDate;
	}
	public Date getSmDate(){
		return smDate;
	}
	public void setSmWorker(String smWorker){
		this.smWorker = smWorker;
	}
	public String getSmWorker(){
		return smWorker;
	}
	public void setBackQuantityBU(Double backQuantityBU){
		this.backQuantityBU = backQuantityBU;
	}
	public Double getBackQuantityBU(){
		return backQuantityBU;
	}
	public void setInventoryStatus(String inventoryStatus) {
		this.inventoryStatus = inventoryStatus;
	}

	public WmsPickTicket getPickTicket() {
		return pickTicket;
	}

	public void setPickTicket(WmsPickTicket pickTicket) {
		this.pickTicket = pickTicket;
	}

	public Integer getLineNo() {
		return lineNo;
	}

	public void setLineNo(Integer lineNo) {
		this.lineNo = lineNo;
	}

	public WmsItem getItem() {
		return item;
	}

	public void setItem(WmsItem item) {
		this.item = item;
	}

	public ShipLotInfo getShipLotInfo() {
		return shipLotInfo;
	}

	public void setShipLotInfo(ShipLotInfo shipLotInfo) {
		this.shipLotInfo = shipLotInfo;
	}

	public WmsPackageUnit getPackageUnit() {
		return packageUnit;
	}

	public void setPackageUnit(WmsPackageUnit packageUnit) {
		this.packageUnit = packageUnit;
	}

	public Double getExpectedQuantity() {
		return expectedQuantity;
	}

	public void setExpectedQuantity(Double expectedQuantity) {
		this.expectedQuantity = expectedQuantity;
	}

	public Double getExpectedQuantityBU() {
		return expectedQuantityBU;
	}

	public void setExpectedQuantityBU(Double expectedQuantityBU) {
		this.expectedQuantityBU = expectedQuantityBU;
	}

	public Double getAllocatedQuantityBU() {
		return allocatedQuantityBU;
	}

	public void setAllocatedQuantityBU(Double allocatedQuantityBU) {
		this.allocatedQuantityBU = allocatedQuantityBU;
	}

	public Double getPickedQuantityBU() {
		return pickedQuantityBU;
	}

	public void setPickedQuantityBU(Double pickedQuantityBU) {
		this.pickedQuantityBU = pickedQuantityBU;
	}

	public Double getShippedQuantityBU() {
		return shippedQuantityBU;
	}

	public void setShippedQuantityBU(Double shippedQuantityBU) {
		this.shippedQuantityBU = shippedQuantityBU;
	}

	public String getInventoryStatus() {
		return inventoryStatus;
	}
	

	/**
	 * 修改分配数量
	 * @param allocatedQuantityBU
	 */
	public void allocate(Double allocatedQuantityBU) {	
        this.allocatedQuantityBU += allocatedQuantityBU;
        this.pickTicket.allocate(allocatedQuantityBU);
    }
	
	/**
	 * 取消分配数量
	 * @param planQuantityBU
	 */
	public void unallocate(Double quantityBU){
		this.allocatedQuantityBU -= quantityBU;
		this.pickTicket.unallocate(quantityBU);
	}
	/**
	 * 退拣
	 * @param quantity
	 */
	public void pickBack(Double quantity) {	
		this.allocatedQuantityBU -= quantity;
        this.pickedQuantityBU -= quantity;
        this.pickTicket.pickBack(quantity);
        
    }
	
	/**
	 * 回写拣货数量BU
	 * @param quantity
	 */
	public void addPickedQuantityBU(Double quantityBU) {
		this.pickedQuantityBU += quantityBU;
		this.pickTicket.addPickedQuantityBU(quantityBU);
	}
	
	/**
	 * 取消拣货数量BU
	 * @param quantity
	 */
	public void cancelPickedQuantityBU(Double quantityBU) {
		this.pickedQuantityBU -= quantityBU;
		this.pickTicket.cancelPickedQuantityBU(quantityBU);
	}

	/**
	 * 发货确认
	 * @param quantity
	 */
	public void ship(Double quantity) {
		this.shippedQuantityBU += quantity;
		
		this.pickTicket.ship();
	}
	
	/**
	 * 获取未分配数量BU
	 */
	public Double getUnAllocateQuantityBU() {
		return this.expectedQuantityBU - this.allocatedQuantityBU;
	}
	
	/**
	 * 获取未分配数量
	 */
	public Double getUnAllocateQuantity() {
		return PackageUtils.convertPackQuantity(getUnAllocateQuantityBU(), this.packageUnit);
	}
	
	public Double getPackageUnitNumByQuantity(Double quantityBU){
		Double quantity = 0D;
		if (this.getPackageUnit().getLineNo() > 1) {
			quantity = Math.floor(quantityBU / this.getPackageUnit().getConvertFigure());
		}else{
			quantity = quantityBU;
		}
		return quantity;
	}
}
