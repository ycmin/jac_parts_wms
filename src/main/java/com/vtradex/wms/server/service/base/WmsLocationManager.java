package com.vtradex.wms.server.service.base;

import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.vtradex.thorn.server.service.BaseManager;
import com.vtradex.wms.server.model.warehouse.WmsDock;
import com.vtradex.wms.server.model.warehouse.WmsLocation;
import com.vtradex.wms.server.model.warehouse.WmsWorkArea;

public interface WmsLocationManager extends BaseManager {
	
	
	/**
	 * 批量创建库位
	 */
	void storeBatchLocation(Integer locationZone,Integer zoneEnd,Integer locationLine,Integer lineEnd,Integer locationColumn,
			Integer columnEnd,Integer locationLayer,Integer layerEnd,Long warehouseAreaId,String locationType);
	/**
	 * 保存库位
	 */
	@Transactional
	void storeLocation(WmsLocation location);
	
	/**
	 * 自动产生动线号
	 * @param obj
	 */
	@Transactional
	void autoCreateRouteNo(Long warehouseAreaId,Integer zone, String channelLines, String startLines,Integer startRouteNo);
	
	/**
	 * 刷新动线号
	 * @param obj
	 */
	@Transactional
	void refreshRouteNo(Long id, String channelLines, String startLines);
	
	/**
	 * 删除库位
	 * @param location
	 */
	@Transactional
	void deleteLocations(WmsLocation location);
	
	/**
	 * 判断库位是否可用
	 * @param location
	 * @param type
	 * @return
	 */
	@Transactional
	Boolean verify(WmsLocation location, String type);

	/**
	 * 保存月台
	 * @param dock
	 */
	@Transactional
	void storeDock(WmsDock dock);
	/**
	 * 保存工作区
	 * @param dock
	 */
	@Transactional
	void storeWorkArea(WmsWorkArea workArea);
	
	@Transactional
	void lockLocationtesting(String str);
	/**
	 * 直接打印库位标签
	 * @author fs
	 */
	Map printLocation(WmsLocation location);
}