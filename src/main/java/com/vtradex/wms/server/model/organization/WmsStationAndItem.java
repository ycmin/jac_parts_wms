package com.vtradex.wms.server.model.organization;

import com.vtradex.thorn.server.annotation.UniqueKey;
import com.vtradex.thorn.server.model.Entity;
/**器具物料对应关系表*/
public class WmsStationAndItem extends Entity{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6659529526152149190L;

	/**器具型号*/
	@UniqueKey
	private String type;
	
	/**货品*/
	@UniqueKey
	private WmsItem item;
	
	/**装载量*/
	private Integer loadage;
	
	/**尺寸 */
	private String size;//考虑23*3*12类型数据改为String
	/**器具名称*/
	private String name;
	

	public WmsStationAndItem(){}
	
	public WmsStationAndItem(String type, WmsItem item, Integer loadage,
			String size,String name) {
		super();
		this.type = type;
		this.item = item;
		this.loadage = loadage;
		this.size = size;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public WmsItem getItem() {
		return item;
	}

	public void setItem(WmsItem item) {
		this.item = item;
	}

	public Integer getLoadage() {
		return loadage;
	}

	public void setLoadage(Integer loadage) {
		this.loadage = loadage;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
}
