package com.vtradex.wms.server.model.organization;

import com.vtradex.thorn.server.annotation.UniqueKey;
import com.vtradex.thorn.server.model.Entity;
import com.vtradex.wms.server.model.warehouse.WmsWorker;
/**
 * 备料工物料关系表
 * @author Administrator
 *
 */
public class WmsBlgItem extends Entity{

	private static final long serialVersionUID = -6162473393557489492L;
	/** 物料 */
	@UniqueKey
	private WmsItem item;
	/**A=true/B=false*/
	@UniqueKey
	private Boolean isA = Boolean.TRUE;
	/** 备料工 */
	private WmsWorker blg;
	/** 备注 */
	private String remark;
	/** 单据类型*/
//	@UniqueKey
//	private WmsBillType billType;
	/** 供应商 去掉不用了暂时*/
//	private WmsOrganization supplier;
	/** 状态 */
	private String status; 
	public Boolean getIsA() {
		return isA;
	}

	public void setIsA(Boolean isA) {
		this.isA = isA;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

//	public WmsBillType getBillType() {
//		return billType;
//	}
//
//	public void setBillType(WmsBillType billType) {
//		this.billType = billType;
//	}

	public WmsItem getItem() {
		return item;
	}

	public void setItem(WmsItem item) {
		this.item = item;
	}

	public WmsWorker getBlg() {
		return blg;
	}

	public void setBlg(WmsWorker blg) {
		this.blg = blg;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public WmsBlgItem(){}
	public WmsBlgItem(WmsItem item, WmsWorker blg, String status) {
		super();
		this.item = item;
		this.blg = blg;
		this.status = status;
	}

}
