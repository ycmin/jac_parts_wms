package com.vtradex.wms.server.service.base.pojo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;

import com.vtradex.thorn.client.ui.page.IPage;
import com.vtradex.thorn.server.exception.BusinessException;
import com.vtradex.thorn.server.model.EntityFactory;
import com.vtradex.thorn.server.service.WorkflowManager;
import com.vtradex.thorn.server.service.pojo.DefaultBaseManager;
import com.vtradex.thorn.server.util.LocalizedMessage;
import com.vtradex.wms.server.model.warehouse.NumberList;
import com.vtradex.wms.server.model.warehouse.WmsDock;
import com.vtradex.wms.server.model.warehouse.WmsLocation;
import com.vtradex.wms.server.model.warehouse.WmsLocationType;
import com.vtradex.wms.server.model.warehouse.WmsWarehouse;
import com.vtradex.wms.server.model.warehouse.WmsWarehouseArea;
import com.vtradex.wms.server.model.warehouse.WmsWorkArea;
import com.vtradex.wms.server.service.base.WmsLocationManager;
import com.vtradex.wms.server.service.rule.WmsRuleManager;
import com.vtradex.wms.server.service.sequence.WmsBussinessCodeManager;
import com.vtradex.wms.server.web.filter.WmsWarehouseHolder;

@SuppressWarnings("unchecked")
public class DefaultWmsLocationManager extends DefaultBaseManager implements WmsLocationManager {
	
	private WmsBussinessCodeManager bussinessCodeManager;
	private WorkflowManager workFlowManager;
	private WmsRuleManager wmsRuleManager;
	
	public DefaultWmsLocationManager(WmsBussinessCodeManager bussinessCodeManager, WorkflowManager workFlowManager,
			WmsRuleManager wmsRuleManager) {
		this.bussinessCodeManager = bussinessCodeManager;
		this.workFlowManager = workFlowManager;
		this.wmsRuleManager = wmsRuleManager;
	}
	
	
	
	/**批量创建库位*/
	public void storeBatchLocation(Integer locationZone,Integer zoneEnd,Integer locationLine,Integer lineEnd,Integer locationColumn,
			Integer columnEnd,Integer locationLayer,Integer layerEnd,Long warehouseAreaId,String locationType){	
		//算出要创建的库位
		int aisleStart = locationZone;
		int lineStart = locationLine;
		int colStart = locationColumn;
		int rowStart = locationLayer;
		String type = locationType;
		int aisleEnd = zoneEnd;
		int colEnd = columnEnd;
		int rowEnd = layerEnd;
		
		if(aisleStart > aisleEnd || lineStart > lineEnd || colStart > colEnd || rowStart > rowEnd){
			throw new BusinessException("startNumber.gt.endNumber");
		}
		
		WmsWarehouseArea wa = commonDao.load(WmsWarehouseArea.class, warehouseAreaId);
		WmsWarehouse warehouse = commonDao.load(WmsWarehouse.class, wa.getWarehouse().getId());
		for(int aisleSum = aisleStart;aisleSum <= aisleEnd;aisleSum++){
			for(int lineSum = lineStart;lineSum <= lineEnd;lineSum++){
				for(int colSum = colStart;colSum <= colEnd;colSum++){
					for(int rowSum = rowStart;rowSum <= rowEnd;rowSum++){				
						//根据库区编码-区排列层生成库区编码
						
						Map<String,Object> map = bussinessCodeManager.generateLocationCodeByRule(warehouse.getName(), 
								wa.getCode(), aisleSum, lineSum,colSum, rowSum);
						String verifyCode = map.get("校验码").toString();
						String code = map.get("流水号").toString();
						
						WmsLocation newLoc = EntityFactory.getEntity(WmsLocation.class);
						newLoc.setWarehouse(warehouse);
						newLoc.setWarehouseArea(wa);
						newLoc.setCode(code);
						newLoc.setVerifyCode(verifyCode);
						newLoc.setType(type);
						newLoc.setZone(aisleSum);
						newLoc.setLine(lineSum);
						newLoc.setColumn(colSum);
						newLoc.setLayer(rowSum);
						workFlowManager.doWorkflow(newLoc, "wmsLocationBaseProcess.new");			
						commonDao.store(newLoc);
					}
				}
			}
		}
	}

	public void storeLocation(WmsLocation location) {
		location.setWarehouse(WmsWarehouseHolder.getWmsWarehouse());		
		Map<String,Object> map=bussinessCodeManager.generateLocationCodeByRule(location.getWarehouse().getName(), 
				location.getWarehouseArea().getCode(), location.getZone(), location.getLine(),location.getColumn(), location.getLayer());
		location.setVerifyCode(map.get("校验码").toString());
		if (StringUtils.isEmpty(location.getCode())) {
			location.setCode(map.get("流水号").toString());
		}
		try {
			workFlowManager.doWorkflow(location, "wmsLocationBaseProcess.new");
		}catch (DataIntegrityViolationException e) {
			throw new BusinessException("库位代码重复,请检查！");
		} 
		catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
		
		commonDao.store(location);

		
	}
	
	public Map getLineColumnMap(Long warehouseAreaId,Integer zone,Integer line) {
		String lineColumnSQl = "SELECT DISTINCT location.column FROM WmsLocation location WHERE location.line = :line AND location.zone = :zone AND location.warehouseArea.id = :warehouseAreaId ORDER BY location.line";
		List<Integer> lineColumnObj = (List<Integer>)commonDao.findByQuery(lineColumnSQl, new String[]{"line","zone","warehouseAreaId"}, new Object[]{line,zone,warehouseAreaId});
		Map lineMap = new HashMap();
		List columnList = new ArrayList();
		Map columnMap;
		for (Integer col : lineColumnObj) {
			columnMap = new HashMap();
			columnMap.put("列号", col);
			
			columnList.add(columnMap);
		}
		lineMap.put("列", columnList);
		return lineMap;
	}
	
	public void autoCreateRouteNo(Long warehouseAreaId,Integer zone, String channelLines, String startLines,Integer startRouteNo) {
		String lineSQl = "SELECT DISTINCT location.line FROM WmsLocation location WHERE location.zone = :zone AND location.warehouseArea.id = :warehouseAreaId";
		List<Integer> lineColumnObj = (List<Integer>)commonDao.findByQuery(lineSQl, new String[]{"zone","warehouseAreaId"}, new Object[]{zone,warehouseAreaId});
		Map zoneMap = new HashMap();
		List lineList = new ArrayList();
		Map lineMap;
		Integer col;
		for (Integer lineCol : lineColumnObj) {
			col = lineCol;
			lineMap = new HashMap();
			lineMap.put("排号", col);
			lineMap.put("排", getLineColumnMap(warehouseAreaId,zone,col));
			
			lineList.add(lineMap);
		}
		zoneMap.put("排", lineList);
		zoneMap.put("排序号列表", lineColumnObj);
		
		Map problem = new HashMap();
		problem.put("区", zoneMap);
		problem.put("库区ID", warehouseAreaId);
		problem.put("区号", zone);
		problem.put("开始动线号", startRouteNo);
		if (NumberList.ONE.equals(channelLines)) {
			problem.put("通道列数", "单列");
		} else {
			problem.put("通道列数", "双列");
		}
		if (NumberList.ONE.equals(startLines)) {
			problem.put("起始排数", "单排");
		} else {
			problem.put("起始排数", "双排");
		}
		
		try {
			WmsWarehouseArea warehouseArea = commonDao.load(WmsWarehouseArea.class, warehouseAreaId);
			Map<String, Object> result = wmsRuleManager.execute(warehouseArea.getWarehouse().getName(), warehouseArea.getWarehouse().getName(), "动线号生成规则", problem);
			
			List<Map> lineColumns = (List<Map>)result.get("返回列表");
			
			String updateRouteNoHQL = "UPDATE WmsLocation location SET location.routeNo = :routeNo WHERE location.zone = :zone AND location.line = :line AND location.column = :column AND location.type = :type AND location.warehouseArea.id = :warehouseAreaId";
			for (Map lineColumn : lineColumns) {
				
				commonDao.executeByHql(updateRouteNoHQL,
					new String[]{"routeNo","zone","line","column","type","warehouseAreaId"},
					new Object[]{
						Integer.valueOf(lineColumn.get("值").toString()),
						zone,
						Integer.valueOf(lineColumn.get("排").toString()),
						Integer.valueOf(lineColumn.get("列").toString()),
						WmsLocationType.STORAGE,
						warehouseAreaId});
			}
			String endRouteNo = ((List<Map>)result.get("动线号列表")).get(0).get("结束").toString();
			LocalizedMessage.addLocalizedMessage("本次生成的动线从【" + startRouteNo + "】开始到【" + endRouteNo + "】结束！");
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * 刷新动线号
	 * @see com.vtradex.wms.server.service.base.WmsLocationManager#refreshRouteNo(java.lang.Long, java.lang.Long)
	 */
	public void refreshRouteNo(Long id, String channelLines1, String startLines1){
		Long channelLines = channelLines1.equals(NumberList.ONE)?1L:2L;
		Long startLines = startLines1.equals(NumberList.ONE)?1L:2L;
		WmsWarehouseArea area = commonDao.load(WmsWarehouseArea.class, id);
		//查找该库区的库位
		List<WmsLocation> locations = commonDao.findByQuery("from WmsLocation location where location.status='ENABLED' and location.warehouseArea.id=:areaId " +
				" order by location.line,location.column", 
										new String[]{"areaId"}, new Object[]{area.getId()});
		Set<Integer> zoneIds = new HashSet<Integer>();
		for(WmsLocation wl:locations){
			zoneIds.add(wl.getZone());
		}
		for(Integer zoneId:zoneIds){
			//查找同一zone的location
			List<WmsLocation> locationsOfSameZone = new ArrayList<WmsLocation>();
			for(WmsLocation wl:locations){
				if(wl.getZone().equals(zoneId)){
					locationsOfSameZone.add(wl);
				}
			}
			refreshLocationInSameZone(locationsOfSameZone,channelLines,startLines);
		}
		
	
	}
	
	
	/**
	 * 对同一zone的库位进行动线号刷新
	 * @param locationsOfSameZone
	 * @param channelLines
	 * @param startLines
	 */
	private void refreshLocationInSameZone(List<WmsLocation> locationsOfSameZone,Long channelLines,Long startLines){
		Long channelLinesTemp = channelLines;
		Long startLinesTemp = startLines;
		/**
		 * 通道列数分为：单列(1),双列(2)
		 * 起始通道排数分为：单排(1),双排(2)
		 * */
		//单列
		if(channelLinesTemp.equals(1L)){
			refreshSingleChannelLine(locationsOfSameZone);
		}
		else{//双列
			refreshDoubleChannelLine(startLinesTemp,locationsOfSameZone);
		}
	}
	
	/**
	 * 通道单列，不分排数
	 * @param locationsOfSameZone
	 */
	private void refreshSingleChannelLine(List<WmsLocation> locationsOfSameZone){
		int directionFlag=1;
		Set<Integer> locationOfRows = new HashSet<Integer>();//同一个区中包含的排数
		Integer routeNo=0;
		for(WmsLocation wl:locationsOfSameZone){
			locationOfRows.add(wl.getLine());
		}
		for(Integer rowNumber:locationOfRows){
			List<WmsLocation> wlInSameRows=new ArrayList<WmsLocation>();
			for(WmsLocation wlInSameRow:locationsOfSameZone){
				if(wlInSameRow.getLine().equals(rowNumber)){
					wlInSameRows.add(wlInSameRow);
				}
			}
			if(directionFlag==-1){
				Collections.reverse(wlInSameRows);
			}
			Set<Integer> cols = new HashSet<Integer>();
			for(WmsLocation wmsLocation:wlInSameRows){
				if(cols.contains(wmsLocation.getColumn())){
					wmsLocation.setRouteNo(routeNo);
				}else{
					routeNo++;
					wmsLocation.setRouteNo(routeNo);
					cols.add(wmsLocation.getColumn());
				}
			}
			directionFlag*=-1;
		}
	}
	
	/**
	 * 通道双列，需分排数
	 * @param startLinesTemp
	 * @param locationsOfSameZone
	 */
	private void refreshDoubleChannelLine(Long startLinesTemp,List<WmsLocation> locationsOfSameZone){
		
		if(startLinesTemp.equals(1L)){//双列，单排
			refreshSingleStartLine(locationsOfSameZone);
		}else{//双列，双排
			refreshDoubleStartLine(locationsOfSameZone);
		}
	}
	
	/**
	 * 通道双列，单排
	 * @param locationsOfSameZone
	 */
	private void refreshSingleStartLine(List<WmsLocation> locationsOfSameZone){
		Set<Integer> locationOfRows = new HashSet<Integer>();//同一个区中包含的排数
		Integer routeNo=0;//动线号
		for(WmsLocation wl:locationsOfSameZone){
			locationOfRows.add(wl.getLine());
		}
		int directionFlag=1;
		Set<Integer> checkInteger = new HashSet<Integer>();
		for(Integer rowNumber:locationOfRows){
			if(checkInteger.contains(rowNumber)){continue;}
			List<WmsLocation> wlInOneOrTwoRows = new ArrayList<WmsLocation>();
					if(checkInteger.isEmpty()){//第一排
						for(WmsLocation wlInSameRow:locationsOfSameZone){
							if(wlInSameRow.getLine().equals(rowNumber)){
								wlInOneOrTwoRows.add(wlInSameRow);
							}
							checkInteger.add(rowNumber);
						}
					}else{//其余排
						List<WmsLocation> wlInBeforeRows = new ArrayList<WmsLocation>();
						List<WmsLocation> wlInAfterRows = new ArrayList<WmsLocation>();
						//根据本排和下一排先后顺序和走向加入到同一List
							for(WmsLocation wlInSameRow:locationsOfSameZone){
								if(wlInSameRow.getLine().equals(rowNumber)){
									wlInBeforeRows.add(wlInSameRow);
								}
								if(wlInSameRow.getLine().equals(rowNumber+1)){
									wlInAfterRows.add(wlInSameRow);
								}
							}
							if(wlInAfterRows==null||wlInAfterRows.isEmpty()){
								if(directionFlag==-1){
									Collections.reverse(wlInBeforeRows);
								}
								wlInOneOrTwoRows.addAll(wlInBeforeRows);
								checkInteger.add(rowNumber);
							}
							else{
								if(directionFlag==-1){//反向
									Collections.reverse(wlInBeforeRows);
									Collections.reverse(wlInAfterRows);
								}
								wlInOneOrTwoRows.addAll(generateRows(wlInBeforeRows,wlInAfterRows));
								checkInteger.add(rowNumber);
								checkInteger.add(rowNumber+1);
							}
							
					}
			//刷入动线号，并改变方向
			List<String> cols = new ArrayList<String>();
			String colFlag ="";
			for(WmsLocation wmsLocation:wlInOneOrTwoRows){
				colFlag=wmsLocation.getLine()+":"+wmsLocation.getColumn();
				if(cols.contains(colFlag)){
					wmsLocation.setRouteNo(routeNo);
				}else{
					routeNo++;
					wmsLocation.setRouteNo(routeNo);
					cols.add(colFlag);
				}
			}
			directionFlag*=-1;
		}
	}
	
	/**
	 * 通道双列，双排
	 * @param locationsOfSameZone
	 */
	private void refreshDoubleStartLine(List<WmsLocation> locationsOfSameZone){
		Set<Integer> locationOfRows = new HashSet<Integer>();//同一个区中包含的排数
		Integer routeNo=0;//动线号
		for(WmsLocation wl:locationsOfSameZone){
			locationOfRows.add(wl.getLine());
		}
		int directionFlag=1;
		Set<Integer> checkInteger = new HashSet<Integer>();
		for(Integer rowNumber:locationOfRows){
			if(checkInteger.contains(rowNumber)){continue;}
			List<WmsLocation> wlInOneOrTwoRows = new ArrayList<WmsLocation>();
			List<WmsLocation> wlInBeforeRows = new ArrayList<WmsLocation>();
			List<WmsLocation> wlInAfterRows = new ArrayList<WmsLocation>();
			//根据本排List和下一排List先后顺序和走向加入到同一List
				for(WmsLocation wlInSameRow:locationsOfSameZone){
					if(wlInSameRow.getLine().equals(rowNumber)){
						wlInBeforeRows.add(wlInSameRow);
					}
					if(wlInSameRow.getLine().equals(rowNumber+1)){
						wlInAfterRows.add(wlInSameRow);
					}
				}
				if(wlInAfterRows==null||wlInAfterRows.isEmpty()){
					if(directionFlag==-1){
						Collections.reverse(wlInBeforeRows);
					}
					wlInOneOrTwoRows.addAll(wlInBeforeRows);
					checkInteger.add(rowNumber);
				}
				else{
					if(directionFlag==-1){//反向
						Collections.reverse(wlInBeforeRows);
						Collections.reverse(wlInAfterRows);
					}
					wlInOneOrTwoRows.addAll(generateRows(wlInBeforeRows,wlInAfterRows));
					checkInteger.add(rowNumber);
					checkInteger.add(rowNumber+1);
				}
			//刷入动线号，并改变方向	
				List<String> cols = new ArrayList<String>();
				String colFlag ="";
				for(WmsLocation wmsLocation:wlInOneOrTwoRows){
					colFlag=wmsLocation.getLine()+":"+wmsLocation.getColumn();
					if(cols.contains(colFlag)){
						wmsLocation.setRouteNo(routeNo);
					}else{
						routeNo++;
						wmsLocation.setRouteNo(routeNo);
						cols.add(colFlag);
					}
				}
			directionFlag*=-1;
		}
	}
	/**
	 * 以前一排先排的顺利，连接两个List
	 * @param wlInBeforeRows
	 * @param wlInAfterRows
	 * @return
	 */
	private List<WmsLocation> generateRows(List<WmsLocation> wlInBeforeRows,List<WmsLocation> wlInAfterRows){
		List<WmsLocation> wlInOneOrTwoRows = new ArrayList<WmsLocation>();
		List<Integer> beforeCol = new ArrayList<Integer>();//前一排列数
		List<Integer> afterCol = new ArrayList<Integer>();//后一排列数
		for(WmsLocation wl:wlInBeforeRows){
			if(beforeCol.contains(wl.getColumn())){
				continue;
			}
			beforeCol.add(wl.getColumn());
		}
		for(WmsLocation wl:wlInAfterRows){
			if(afterCol.contains(wl.getColumn())){
				continue;
			}
			afterCol.add(wl.getColumn());
		}
		int beforeColSize = beforeCol.size();
		int afterColSize = afterCol.size();
		int i=0;
		for(i=0;i<(beforeColSize>=afterColSize?afterColSize:beforeColSize);i++){
			//前一排中与当前列数相同的location先加入
			for(WmsLocation wl:wlInBeforeRows){
				if(wl.getColumn().equals(beforeCol.get(i))){
					wlInOneOrTwoRows.add(wl);
				}
			}
			//后一排中与当前列数相同的location后加入
			for(WmsLocation wl:wlInAfterRows){
				if(wl.getColumn().equals(afterCol.get(i))){
					wlInOneOrTwoRows.add(wl);
				}
			}
		}
		if(beforeColSize==afterColSize){
			return wlInOneOrTwoRows;
		}else if(beforeColSize>afterColSize){
			for(int j=i;j<beforeColSize;j++){
				for(WmsLocation wl:wlInBeforeRows){
					if(wl.getColumn().equals(beforeCol.get(j))){
						wlInOneOrTwoRows.add(wl);
					}
				}
			}
		}else{
			for(int j=i;j<afterColSize;j++){
				for(WmsLocation wl:wlInAfterRows){
					if(wl.getColumn().equals(afterCol.get(j))){
						wlInOneOrTwoRows.add(wl);
					}
				}
			}
		}
		return wlInOneOrTwoRows;
	}
	
	
	public void deleteLocations(WmsLocation location) {
		//删除未关联库位所对应的库位扩展信息
		commonDao.delete(location);
	}

	/**
	 * 库位出入锁校验
	 * @param location
	 * @param inOrOut
	 * @return
	 */
	public Boolean verify(WmsLocation location, String type) {
		Map<String,Object> problem = new HashMap<String, Object>();
		problem.put("出入锁类型", type);
		problem.put("库区", location.getWarehouseArea().getCode());
		problem.put("区", location.getZone());
		problem.put("排", location.getLine());
		problem.put("列", location.getColumn());
		problem.put("层", location.getLayer());
		
		Map<String, Object> result = wmsRuleManager.execute(location.getWarehouse().getName(), 
				location.getWarehouse().getName(), "库位出入锁校验规则", problem);
		
		Integer isLock = Integer.valueOf(result.get("是否锁定").toString());
		if (isLock.intValue() == 0) {
			return false;
		}
		return true;
	}
	
	public void storeDock(WmsDock dock) {
		try {
			commonDao.store(dock);
		}catch (DataIntegrityViolationException e) {
			throw new BusinessException("月台编码重复,请检查！");
		} 
		catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
		
	}

	public void storeWorkArea(WmsWorkArea workArea) {
		commonDao.store(workArea);
	}

	public void lockLocationtesting(String str){
		WmsLocation location = load(WmsLocation.class,13892L);
		location.addTouchCount();
		commonDao.store(location);
	}
	
	public Map printLocation(WmsLocation location){
		Map result = new HashMap();
		Map<Long,String> reportValue = new HashMap<Long, String>();
		reportValue.put(location.getId(), "XG_JCSJ.raq");
		
		result.put(IPage.REPORT_VALUES, reportValue);
		result.put(IPage.REPORT_PRINT_NUM, 1);
		
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("parentIds", location.getId());
		result.put(IPage.REPORT_PARAMS, params);
		
		return result;
	}
}