package com.vtradex.wms.server.model.interfaces;

import java.util.Date;

import com.vtradex.thorn.server.model.VersionalEntity;
import com.vtradex.wms.server.model.organization.WmsBillType;
import com.vtradex.wms.server.model.organization.WmsItem;
import com.vtradex.wms.server.model.organization.WmsOrganization;
import com.vtradex.wms.server.model.organization.WmsPackageUnit;

/**
 * 存储对应的ASN中间表的数据
 * @author fs
 *
 */
public class MiddleAsnSrmDetail extends VersionalEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7629912821760034331L;

	private String asnNO;//送货单号
	private String poNo;//订单号
	private WmsItem item;//货品编码
	private WmsOrganization supply;//供应商编码
	private WmsOrganization company;//货主
	private WmsPackageUnit packageUnit;//包装单位
	private Double sendQty;//数量
	private Date reDate;//生产日期
	private WmsBillType billType;//单据类型 
	private Boolean isMt;//是否码托
	private Integer trayQty;//托盘总个数
	private String itemName;
	private String supplyName;
	private Integer polineNo;
	private Integer asnLineNo;
	
	private WHead head;

	
	
	public Integer getPolineNo() {
		return polineNo;
	}

	public void setPolineNo(Integer polineNo) {
		this.polineNo = polineNo;
	}

	public Integer getAsnLineNo() {
		return asnLineNo;
	}

	public void setAsnLineNo(Integer asnLineNo) {
		this.asnLineNo = asnLineNo;
	}

	public String getAsnNO() {
		return asnNO;
	}

	public void setAsnNO(String asnNO) {
		this.asnNO = asnNO;
	}

	public String getPoNo() {
		return poNo;
	}

	public void setPoNo(String poNo) {
		this.poNo = poNo;
	}

	public WmsItem getItem() {
		return item;
	}

	public void setItem(WmsItem item) {
		this.item = item;
	}

	public WmsOrganization getSupply() {
		return supply;
	}

	public void setSupply(WmsOrganization supply) {
		this.supply = supply;
	}

	public WmsOrganization getCompany() {
		return company;
	}

	public void setCompany(WmsOrganization company) {
		this.company = company;
	}

	public WmsPackageUnit getPackageUnit() {
		return packageUnit;
	}

	public void setPackageUnit(WmsPackageUnit packageUnit) {
		this.packageUnit = packageUnit;
	}

	public Double getSendQty() {
		return sendQty;
	}

	public void setSendQty(Double sendQty) {
		this.sendQty = sendQty;
	}

	public Date getReDate() {
		return reDate;
	}

	public void setReDate(Date reDate) {
		this.reDate = reDate;
	}

	public WmsBillType getBillType() {
		return billType;
	}

	public void setBillType(WmsBillType billType) {
		this.billType = billType;
	}

	public Boolean getIsMt() {
		return isMt;
	}

	public void setIsMt(Boolean isMt) {
		this.isMt = isMt;
	}

	public Integer getTrayQty() {
		return trayQty;
	}

	public void setTrayQty(Integer trayQty) {
		this.trayQty = trayQty;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getSupplyName() {
		return supplyName;
	}

	public void setSupplyName(String supplyName) {
		this.supplyName = supplyName;
	}

	public WHead getHead() {
		return head;
	}

	public void setHead(WHead head) {
		this.head = head;
	}

	public MiddleAsnSrmDetail(){}
	public MiddleAsnSrmDetail(String asnNO, String poNo, WmsItem item,
			WmsOrganization supply, WmsOrganization company,
			WmsPackageUnit packageUnit, Double sendQty, Date reDate,
			WmsBillType billType, Boolean isMt, Integer trayQty,WHead head) {
		super();
		this.asnNO = asnNO;
		this.poNo = poNo;
		this.item = item;
		this.supply = supply;
		this.company = company;
		this.packageUnit = packageUnit;
		this.sendQty = sendQty;
		this.reDate = reDate;
		this.billType = billType;
		this.isMt = isMt;
		this.trayQty = trayQty;
		this.head = head;
	}
	
	
	
}
