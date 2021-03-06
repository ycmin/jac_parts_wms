package com.vtradex.wms.client.ui.page.allocate.page.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CustomTask implements IsSerializable{
	private Long id;
	private String locationCode;
	private CustomMoveDoc customMoveDoc;
	private CustomItem customItem;
	private CustomPackageUnit customUnit;
	private double planQuantity;
	private double planQuantityBU;
	private double cancelQuantity;
	private String lotInfo;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getLocationCode() {
		return locationCode;
	}
	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}
	public CustomItem getCustomItem() {
		return customItem;
	}
	public void setCustomItem(CustomItem customItem) {
		this.customItem = customItem;
	}
	public CustomPackageUnit getCustomUnit() {
		return customUnit;
	}
	public void setCustomUnit(CustomPackageUnit customUnit) {
		this.customUnit = customUnit;
	}
	public double getPlanQuantity() {
		return planQuantity;
	}
	public void setPlanQuantity(double planQuantity) {
		this.planQuantity = planQuantity;
	}
	public double getPlanQuantityBU() {
		return planQuantityBU;
	}
	public void setPlanQuantityBU(double planQuantityBU) {
		this.planQuantityBU = planQuantityBU;
	}
	public double getCancelQuantity() {
		return cancelQuantity;
	}
	public void setCancelQuantity(double cancelQuantity) {
		this.cancelQuantity = cancelQuantity;
	}
	public String getLotInfo() {
		return lotInfo;
	}
	public void setLotInfo(String lotInfo) {
		this.lotInfo = lotInfo;
	}
	public CustomMoveDoc getCustomMoveDoc() {
		return customMoveDoc;
	}
	public void setCustomMoveDoc(CustomMoveDoc customMoveDoc) {
		this.customMoveDoc = customMoveDoc;
	}
	
	public Object[] toArray() {
		return new Object[] { this.id, this.customItem.getId(),
			this.customUnit.getId(), this.locationCode, this.customItem.getCode(), 
			this.customItem.getName(), this.customUnit.getUnit(), 
			this.customUnit.getConvertFigure(), this.planQuantity, 
			this.planQuantityBU, this.cancelQuantity, this.lotInfo};
	}
	
	public String toLotInfor(String soi, String supplier, String extendPropC1, String extendPropC2, 
			String extendPropC3, String extendPropC4, String extendPropC5, String extendPropC6,
			String extendPropC7, String extendPropC8, String extendPropC9, String extendPropC10,
			String extendPropC11, String extendPropC12, String extendPropC13, String extendPropC14,
			String extendPropC15, String extendPropC16, String extendPropC17, String extendPropC18,
			String extendPropC19, String extendPropC20) {
		String result = "";
		if (soi != null && !"".equals(soi)) {
			result += "#" + soi;
		}
		if (supplier != null && !"".equals(supplier)) {
			result += "#" + supplier;
		}
		if (extendPropC1 != null && !"".equals(extendPropC1)) {
			result += "#" + extendPropC1;
		}
		if (extendPropC2 != null && !"".equals(extendPropC2)) {
			result += "#" + extendPropC2;
		}
		if (extendPropC3 != null && !"".equals(extendPropC3)) {
			result += "#" + extendPropC3;
		}
		if (extendPropC4 != null && !"".equals(extendPropC4)) {
			result += "#" + extendPropC4;
		}
		if (extendPropC5 != null && !"".equals(extendPropC5)) {
			result += "#" + extendPropC5;
		}
		if (extendPropC6 != null && !"".equals(extendPropC6)) {
			result += "#" + extendPropC6;
		}
		if (extendPropC7 != null && !"".equals(extendPropC7)) {
			result += "#" + extendPropC7;
		}
		if (extendPropC8 != null && !"".equals(extendPropC8)) {
			result += "#" + extendPropC8;
		}
		if (extendPropC9 != null && !"".equals(extendPropC9)) {
			result += "#" + extendPropC9;
		}
		if (extendPropC10 != null && !"".equals(extendPropC10)) {
			result += "#" + extendPropC10;
		}
		if (extendPropC11 != null && !"".equals(extendPropC11)) {
			result += "#" + extendPropC11;
		}
		if (extendPropC12 != null && !"".equals(extendPropC12)) {
			result += "#" + extendPropC12;
		}
		if (extendPropC13 != null && !"".equals(extendPropC13)) {
			result += "#" + extendPropC13;
		}
		if (extendPropC14 != null && !"".equals(extendPropC14)) {
			result += "#" + extendPropC14;
		}
		if (extendPropC15 != null && !"".equals(extendPropC15)) {
			result += "#" + extendPropC15;
		}
		if (extendPropC16 != null && !"".equals(extendPropC16)) {
			result += "#" + extendPropC16;
		}
		if (extendPropC17 != null && !"".equals(extendPropC17)) {
			result += "#" + extendPropC17;
		}
		if (extendPropC18 != null && !"".equals(extendPropC18)) {
			result += "#" + extendPropC18;
		}
		if (extendPropC19 != null && !"".equals(extendPropC19)) {
			result += "#" + extendPropC19;
		}
		if (extendPropC20 != null && !"".equals(extendPropC20)) {
			result += "#" + extendPropC20;
		}
		
		if (result != null) {
			result = result.replaceFirst("#", "");
		}
		
		return result;
	}
}
