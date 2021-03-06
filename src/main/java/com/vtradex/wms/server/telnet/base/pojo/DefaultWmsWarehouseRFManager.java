package com.vtradex.wms.server.telnet.base.pojo;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.vtradex.kangaroo.shell.RFBusinessException;
import com.vtradex.rule.server.loader.IRuleTableLoader;
import com.vtradex.thorn.server.exception.BusinessException;
import com.vtradex.thorn.server.security.acegi.holder.SecurityContextHolder;
import com.vtradex.thorn.server.service.pojo.DefaultBaseManager;
import com.vtradex.thorn.server.util.Constant;
import com.vtradex.wms.server.model.warehouse.WmsWarehouse;
import com.vtradex.wms.server.model.warehouse.WmsWarehouseArea;
import com.vtradex.wms.server.model.warehouse.WmsWorkArea;
import com.vtradex.wms.server.model.warehouse.WmsWorker;
import com.vtradex.wms.server.telnet.base.WmsWarehouseRFManager;
import com.vtradex.wms.server.telnet.dto.WmsWorkAreaExtDTO;
import com.vtradex.wms.server.telnet.exception.RFFinishException;
import com.vtradex.wms.server.web.filter.WmsWarehouseHolder;

/**
 * @author: 李炎
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DefaultWmsWarehouseRFManager extends DefaultBaseManager implements
		WmsWarehouseRFManager {

	protected IRuleTableLoader ruleTableLoader;
	
	public DefaultWmsWarehouseRFManager(IRuleTableLoader ruleTableLoader) {
		this.ruleTableLoader = ruleTableLoader;
	}
	
	public List<WmsWarehouse> getWmsAvailableWarehousesByUserId() {
//		String hql = SecurityContextHolder.getRFSecurityFilter().getHql("switchWareHousePage.loadWarehouseDefault");
		String hql = "";
		if (StringUtils.isEmpty(hql)) {
			hql = "FROM WmsWarehouse wh WHERE 1=1 AND wh.status = 'ENABLED'";
		}
		
		return commonDao.findByQuery(hql);
	}

	
	public List<WmsWorkArea> getWorkAreaByDefaultWarehouse(){
		String hql = "from WmsWorkArea wwa where wwa.warehouseArea.warehouse.id = :wareHouseId ORDER BY id";
		return commonDao.findByQuery(hql, "wareHouseId", WmsWarehouseHolder.getWmsWarehouse().getId());
	}
	
	public List<WmsWorker> getWmsWorker(Long userId, Long warehouseId) {
		String workerHql = "from WmsWorker worker where worker.user.id=:userId and worker.warehouse.id = :warehouseId" +
				" and worker.status = 'ENABLED'";
		List<WmsWorker> workerList = commonDao.findByQuery(workerHql, new String[]{"userId", "warehouseId"}, new Object[]{userId, warehouseId});
		
		return workerList;
	}
	
	public WmsWarehouseArea getWmsWarehouseArea(Long warehouseAreaId) {
		return commonDao.load(WmsWarehouseArea.class, warehouseAreaId);
	}
	
	public WmsWorkAreaExtDTO getWmsWorkAreaExt(WmsWorkArea workArea) {
		String[] binds = new String[]{WmsWarehouseHolder.getWmsWarehouse().getName(),
				Constant.NULL, Constant.NULL,Constant.NULL, Constant.NULL};
		
		WmsWarehouseArea warehouseArea = getWmsWarehouseArea(workArea.getWarehouseArea().getId());
		Object[] objects = new Object[]{"R101_基础数据_工作区定位表", warehouseArea.getCode(), workArea.getCode()};
		Map<String,String> values = (Map)ruleTableLoader.getRuleTableDetail(new Date(), binds, objects);
		String areaBetweens = values.get("区区间");
		String lineBetweens = values.get("排区间");
		final int startZone = Integer.valueOf(StringUtils.substringBetween(areaBetweens, "(", ",").trim());
		final int endZone = Integer.valueOf(StringUtils.substringBetween(areaBetweens, ",", ")").trim());
		final int startLine = Integer.valueOf(StringUtils.substringBetween(lineBetweens, "(", ",").trim());
		final int endLine = Integer.valueOf(StringUtils.substringBetween(lineBetweens, ",", ")").trim());
		WmsWorkAreaExtDTO workAreaExt = new WmsWorkAreaExtDTO(startZone, endZone, startLine, endLine);
		
		return workAreaExt;
	}
}
