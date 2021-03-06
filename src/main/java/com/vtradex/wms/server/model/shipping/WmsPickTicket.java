package com.vtradex.wms.server.model.shipping;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.vtradex.thorn.server.annotation.UniqueKey;
import com.vtradex.thorn.server.model.Entity;
import com.vtradex.wms.server.model.base.Contact;
import com.vtradex.wms.server.model.move.WmsTaskLog;
import com.vtradex.wms.server.model.organization.WmsBillType;
import com.vtradex.wms.server.model.organization.WmsOrganization;
import com.vtradex.wms.server.model.receiving.WmsSource;
import com.vtradex.wms.server.model.warehouse.WmsDock;
import com.vtradex.wms.server.model.warehouse.WmsWarehouse;

/**
 * 发货单 
 *
 * @category Entity 
 * @author <a href="brofe.pan@gmail.com">潘宁波</a>
 * @version $Revision: 1.8 $Date: 2015/11/03 02:53:28 $
 */
public class WmsPickTicket extends Entity {
	
	private static final long serialVersionUID = -1334661912887618680L;
	
	/** 所属仓库 */
	@UniqueKey
	private WmsWarehouse warehouse;
	/** 货主 */
	@UniqueKey
	private WmsOrganization company;
	/** 单据类型 */
	@UniqueKey
	private WmsBillType billType;
	
	/** 发货单号 */
	@UniqueKey
	private String code;
	/** 相关单号1 --SAP单据号*/
	private String relatedBill1;
	/** 相关单号2 */
	private String relatedBill2;
	/** 相关单号3 */
	private String relatedBill3;
	
	/** 收货方 */
	private WmsOrganization customer;
	/** 收货人姓名 */
	private String shipToName;
	/** 收货人联系方式 */
	private Contact shipToContact;
	
	/** 
	 * 状态
	 *  
	 * {@link WmsPickTicketStatus} 
	 */
	private String status;
	
	/** 优先级 */
	private Integer priority = 0;
	/** 承运商 */
	private WmsOrganization carrier;
	/** 司机 */
	private String driver;
	/** 车牌号 */
	private String vehicleNo;
	/** 整箱数 */
	private Double boxQuantity = 0D;
	/** 散件数 */
	private Double scatteredQuantity = 0D;
	/** 重量 */
	private Double weight = 0D;
	/** 体积 */
	private Double volume = 0D;
	
	/** 订单日期 */
	private Date orderDate;
	/** 要求达到时间 */
	private Date requireArriveDate;
	/** 预计发货时间 */
	private Date intendShipDate;
	/** 完成时间 */
	private Date finshDate;
	
	/** 计划数量BU */
	private Double expectedQuantityBU = 0D;
	/** 分配数量BU */
	private Double allocatedQuantityBU = 0D;
	/** 拣货数量BU */
	private Double pickedQuantityBU = 0D;
	/** 发货数量BU */
	private Double shippedQuantityBU = 0D;
	
	/** 描述 */
	private String description;
	
	/**  发货月台 */
	private WmsDock dock ;
	/** 包含明细清单 */
	private Set<WmsPickTicketDetail> details = new HashSet<WmsPickTicketDetail>();
	/**======================JAC==================*/
	/**看板编码*/
	private String kbCode;
	/**物料拉动方式:排序件、台套件、机加工标准件、看板件、市场备件、按需件、成品附件*/
	private String ldType;
	/**接口发送方*/
	private String sender;
	/**收货仓库-接口*/
	private String receiveHouse;
	/**收货工厂-接口*/
	private String receiveFactory;
	/**收货道口-接口*/
	private String receiveDoc;
	/** 接口时间 */
	private Date sendTime;
	/** 分配时间 */
	private Date allocatedTime;
	/** 读取标志:0未读取,1已读取.....*/
	private Integer ledRead = 0;
	
	/**
	 * {@link WmsSource}
	 * 来源
	 */
	private String source =WmsSource.MANUAL ;
	
	/**下面是新增字段存接口传过来的数据*/
	//计划需求公司  工作中心
	private String odrSu;
	
	//批次
	private String batch;
	
	//批拣单增加字段,来源于汇总后的发货单明细
	/**生产线*/
	private String productionLine;	
	/**工位*/
	private String station;
		
	
	public WmsPickTicket() {
	}
	
	
	
	
	public WmsPickTicket(WmsWarehouse warehouse, WmsOrganization company,
			WmsBillType billType, String code, String relatedBill2,
			String status, Double expectedQuantityBU,
			Double allocatedQuantityBU, Double pickedQuantityBU,
			Double shippedQuantityBU, String source,Date requireArriveDate) {
		super();
		this.warehouse = warehouse;
		this.company = company;
		this.billType = billType;
		this.code = code;
		this.relatedBill2 = relatedBill2;
		this.status = status;
		this.expectedQuantityBU = expectedQuantityBU;
		this.allocatedQuantityBU = allocatedQuantityBU;
		this.pickedQuantityBU = pickedQuantityBU;
		this.shippedQuantityBU = shippedQuantityBU;
		this.source = source;
		this.requireArriveDate = requireArriveDate;
	}




	public WmsPickTicket(WmsWarehouse warehouse,WmsOrganization company, 
			WmsBillType billType,String code, String relatedBill1,
			String status,Date requireArriveDate, Double expectedQuantityBU,
			Double allocatedQuantityBU, Double pickedQuantityBU,
			Double shippedQuantityBU, String receiveHouse, String receiveDoc,
			String source,Date orderDate,String odrSu,String batch) {
		super();
		this.warehouse = warehouse;
		this.company = company;
		this.billType = billType;
		this.code = code;
		this.relatedBill1 = relatedBill1;
		this.status = status;
		this.requireArriveDate = requireArriveDate;
		this.expectedQuantityBU = expectedQuantityBU;
		this.allocatedQuantityBU = allocatedQuantityBU;
		this.pickedQuantityBU = pickedQuantityBU;
		this.shippedQuantityBU = shippedQuantityBU;
		this.receiveHouse = receiveHouse;
		this.receiveDoc = receiveDoc;
		this.source = source;
		this.orderDate = orderDate;
		this.odrSu = odrSu;
		this.batch = batch;
	}




	public String getOdrSu() {
		return odrSu;
	}


	public String getProductionLine() {
		return productionLine;
	}

	public void setProductionLine(String productionLine) {
		this.productionLine = productionLine;
	}

	public String getStation() {
		return station;
	}

	public void setStation(String station) {
		this.station = station;
	}




	public void setOdrSu(String odrSu) {
		this.odrSu = odrSu;
	}




	public String getBatch() {
		return batch;
	}




	public void setBatch(String batch) {
		this.batch = batch;
	}




	public Integer getLedRead() {
		return ledRead;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setLedRead(Integer ledRead) {
		this.ledRead = ledRead;
	}

	public Date getAllocatedTime() {
		return allocatedTime;
	}

	public void setAllocatedTime(Date allocatedTime) {
		this.allocatedTime = allocatedTime;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public String getReceiveHouse() {
		return receiveHouse;
	}

	public void setReceiveHouse(String receiveHouse) {
		this.receiveHouse = receiveHouse;
	}

	public String getReceiveFactory() {
		return receiveFactory;
	}

	public void setReceiveFactory(String receiveFactory) {
		this.receiveFactory = receiveFactory;
	}

	public String getReceiveDoc() {
		return receiveDoc;
	}

	public void setReceiveDoc(String receiveDoc) {
		this.receiveDoc = receiveDoc;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getKbCode() {
		return kbCode;
	}
	public void setKbCode(String kbCode) {
		this.kbCode = kbCode;
	}
	public String getLdType() {
		return ldType;
	}


	public void setLdType(String ldType) {
		this.ldType = ldType;
	}


	public WmsWarehouse getWarehouse() {
		return warehouse;
	}

	public void setWarehouse(WmsWarehouse warehouse) {
		this.warehouse = warehouse;
	}

	public WmsOrganization getCompany() {
		return company;
	}

	public void setCompany(WmsOrganization company) {
		this.company = company;
	}

	public WmsBillType getBillType() {
		return billType;
	}

	public void setBillType(WmsBillType billType) {
		this.billType = billType;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getRelatedBill1() {
		return relatedBill1;
	}

	public void setRelatedBill1(String relatedBill1) {
		this.relatedBill1 = relatedBill1;
	}

	public String getRelatedBill2() {
		return relatedBill2;
	}

	public void setRelatedBill2(String relatedBill2) {
		this.relatedBill2 = relatedBill2;
	}

	public String getRelatedBill3() {
		return relatedBill3;
	}

	public void setRelatedBill3(String relatedBill3) {
		this.relatedBill3 = relatedBill3;
	}

	public WmsOrganization getCustomer() {
		return customer;
	}

	public void setCustomer(WmsOrganization customer) {
		this.customer = customer;
	}

	public String getShipToName() {
		return shipToName;
	}

	public void setShipToName(String shipToName) {
		this.shipToName = shipToName;
	}

	public Contact getShipToContact() {
		return shipToContact;
	}

	public void setShipToContact(Contact shipToContact) {
		this.shipToContact = shipToContact;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public WmsOrganization getCarrier() {
		return carrier;
	}

	public void setCarrier(WmsOrganization carrier) {
		this.carrier = carrier;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getVehicleNo() {
		return vehicleNo;
	}

	public void setVehicleNo(String vehicleNo) {
		this.vehicleNo = vehicleNo;
	}

	public Double getBoxQuantity() {
		return boxQuantity;
	}

	public void setBoxQuantity(Double boxQuantity) {
		this.boxQuantity = boxQuantity;
	}

	public Double getScatteredQuantity() {
		return scatteredQuantity;
	}

	public void setScatteredQuantity(Double scatteredQuantity) {
		this.scatteredQuantity = scatteredQuantity;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public Double getVolume() {
		return volume;
	}

	public void setVolume(Double volume) {
		this.volume = volume;
	}

	public Date getRequireArriveDate() {
		return requireArriveDate;
	}

	public void setRequireArriveDate(Date requireArriveDate) {
		this.requireArriveDate = requireArriveDate;
	}

	public Date getIntendShipDate() {
		return intendShipDate;
	}

	public void setIntendShipDate(Date intendShipDate) {
		this.intendShipDate = intendShipDate;
	}

	/**
	 * 期待数量BU
	 */
	public Double getExpectedQuantityBU() {
		return expectedQuantityBU;
	}

	public void setExpectedQuantityBU(Double expectedQuantityBU) {
		this.expectedQuantityBU = expectedQuantityBU;
	}

	/**
	 * 分配数量BU
	 */
	public Double getAllocatedQuantityBU() {
		return allocatedQuantityBU;
	}

	public void setAllocatedQuantityBU(Double allocatedQuantityBU) {
		this.allocatedQuantityBU = allocatedQuantityBU;
	}

	/**
	 * 拣货数量BU
	 */
	public Double getPickedQuantityBU() {
		return pickedQuantityBU;
	}

	public void setPickedQuantityBU(Double pickedQuantityBU) {
		this.pickedQuantityBU = pickedQuantityBU;
	}

	/**
	 * 发货数量BU
	 */
	public Double getShippedQuantityBU() {
		return shippedQuantityBU;
	}

	public void setShippedQuantityBU(Double shippedQuantityBU) {
		this.shippedQuantityBU = shippedQuantityBU;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<WmsPickTicketDetail> getDetails() {
		return details;
	}

	public void setDetails(Set<WmsPickTicketDetail> details) {
		this.details = details;
	}

	public Date getFinshDate() {
		return finshDate;
	}

	public void setFinshDate(Date finshDate) {
		this.finshDate = finshDate;
	}

	
	public WmsDock getDock() {
		return dock;
	}

	public void setDock(WmsDock dock) {
		this.dock = dock;
	}

	/**
	 * 发货单新增明细
	 * 
	 * @param pickTicketDetail
	 */
	public void addPickTicketDetail(WmsPickTicketDetail detail) {
		detail.setPickTicket(this);

		this.details.add(detail);

		this.refreshPickTicketQty();
	}

	/**
	 * 发货单删除明细
	 * 
	 * @param detail
	 */
	public void removeDetails(WmsPickTicketDetail detail) {
		detail.setPickTicket(null);

		this.details.remove(detail);

		this.refreshPickTicketQty();
	}

	/**
	 * 刷新发货单期待数量
	 */
	public void refreshPickTicketQty() {
		this.expectedQuantityBU = 0.0D;
		this.shippedQuantityBU = 0.0D;
		
		for (WmsPickTicketDetail detail : this.details) {
			this.expectedQuantityBU += detail.getExpectedQuantityBU();
			this.shippedQuantityBU += detail.getShippedQuantityBU();
		}
	}

	/**
	 * 刷新发货单分配数量
	 * @param allocateQuantity
	 */
	public void refreshAlloatedQtyBU() {
		this.allocatedQuantityBU = 0.0D;
		
		for (WmsPickTicketDetail detail : this.details) {
			this.allocatedQuantityBU += detail.getAllocatedQuantityBU();
		}
	}

	/**
	 * 刷新发货单发运数量
	 */
	public void refreshShippedQty() {
		this.shippedQuantityBU = 0.0D;
		
		for (WmsPickTicketDetail detail : this.details) {
			this.shippedQuantityBU += detail.getShippedQuantityBU();
		}
	}

	/**
	 * 增加分配数量BU
	 * @param quantityBU
	 */
	public void allocate(Double quantityBU){
		this.allocatedQuantityBU += quantityBU;
		this.allocatedTime = new Date();
	}
	
	/**
	 * 取消分配数量BU
	 * @param quantityBU
	 */
	public void unallocate(Double quantityBU){
		this.allocatedQuantityBU -= quantityBU;
	}
	
	/**
	 * 获取未分配数量
	 * @return
	 */
	public Double getUnAllocateQuantityBU() {
		return this.expectedQuantityBU - this.allocatedQuantityBU;
	}
	
	public void pickBack(Double quantity) {
		this.allocatedQuantityBU -= quantity;
		this.pickedQuantityBU -= quantity;
	}

	public void setQuantityByPickConfirm(Double remainingQuantity) {
		this.setAllocatedQuantityBU(remainingQuantity);
	}

	public void addPickedQuantityBU(Double pickedQuantityBU) {
		this.pickedQuantityBU += pickedQuantityBU;
	}
	
	/** 取消拣货数量BU */
	public void cancelPickedQuantityBU(Double pickedQuantityBU) {
		this.pickedQuantityBU -= pickedQuantityBU;
	}

	/**
	 * 发货确认
	 */
	public void ship() {
		this.refreshShippedQty();
		
		this.setFinshDate(new Date());
	}

	/**
	 * 根据拣货记录更新单头的箱件数和重量体积信息
	 * @param taskLog
	 */
	public void updateShipInfo(WmsTaskLog taskLog, int incOrDec) {
//		if (incOrDec == 1) {
//			this.boxQuantity += taskLog.getBoxQuantity();
//			this.scatteredQuantity += taskLog.getScatteredQuantity();
//			this.weight += taskLog.getWeight();
//			this.volume += taskLog.getVolume();
//		} else {
//			this.boxQuantity -= taskLog.getBoxQuantity();
//			this.scatteredQuantity -= taskLog.getScatteredQuantity();
//			this.weight -= taskLog.getWeight();
//			this.volume -= taskLog.getVolume();
//		}
	}

}