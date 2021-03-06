package com.vtradex.wms.server.telnet.receiving.pojo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.lang.StringUtils;

import com.vtradex.thorn.server.exception.BusinessException;
import com.vtradex.thorn.server.service.WorkflowManager;
import com.vtradex.thorn.server.service.pojo.DefaultBaseManager;
import com.vtradex.wms.server.model.base.LotInfo;
import com.vtradex.wms.server.model.organization.WmsItem;
import com.vtradex.wms.server.model.organization.WmsItemState;
import com.vtradex.wms.server.model.organization.WmsLotRule;
import com.vtradex.wms.server.model.organization.WmsPackageUnit;
import com.vtradex.wms.server.model.receiving.JacPalletSerial;
import com.vtradex.wms.server.model.receiving.WmsASN;
import com.vtradex.wms.server.model.receiving.WmsASNDetail;
import com.vtradex.wms.server.model.receiving.WmsASNStatus;
import com.vtradex.wms.server.model.receiving.WmsBooking;
import com.vtradex.wms.server.model.warehouse.WmsDock;
import com.vtradex.wms.server.model.warehouse.WmsLocation;
import com.vtradex.wms.server.service.receiving.WmsASNManager;
import com.vtradex.wms.server.telnet.dto.WmsASNDetailDTO;
import com.vtradex.wms.server.telnet.exception.RFFinishException;
import com.vtradex.wms.server.telnet.receiving.WmsReceivingRFManager;
import com.vtradex.wms.server.utils.JavaTools;
import com.vtradex.wms.server.utils.VtradexDirectPrintJava;
import com.vtradex.wms.server.web.filter.WmsWorkerHolder;

/**
 * 
 *
 * @category Manager
 * @author <a href="brofe.pan@gmail.com">潘宁波</a>
 * @version $Revision: 1.9 $Date: 2016/03/01 08:24:02 $
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class DefaultWmsReceivingRFManager extends DefaultBaseManager implements WmsReceivingRFManager {
	
	private WmsASNManager wmsASNManager;
	
	public DefaultWmsReceivingRFManager(WmsASNManager wmsASNManager) {
		this.wmsASNManager = wmsASNManager;
	}
	
	public WmsASNDetailDTO getWmsASNDetailDTO(Long asnId, String barCode) throws BusinessException {
		String hql = "FROM WmsASNDetail d where d.asn.id = :asnId and d.item.barCode = :barCode and (d.expectedQuantityBU - d.receivedQuantityBU) > 0";
		
		List asnDetailList = commonDao.findByQuery(hql, new String[]{"asnId", "barCode"}, new Object[]{asnId, barCode});
		int detailSize = asnDetailList.size();
		if (detailSize == 0) {
			throw new BusinessException("当前货品条码不存在或不符合收货条件");
		}
		
		WmsASNDetail detail = (WmsASNDetail)asnDetailList.get(0);
		WmsASN asn = commonDao.load(WmsASN.class, detail.getAsn().getId());
		WmsItem item = commonDao.load(WmsItem.class, detail.getItem().getId());
		WmsLotRule lotRule = commonDao.load(WmsLotRule.class, item.getLotRule().getId());
		WmsPackageUnit currentPackageUnit = commonDao.load(WmsPackageUnit.class, detail.getPackageUnit().getId());
		
		WmsBooking booking = detail.getBooking();
		if (booking  == null) {
			throw new BusinessException("未指定收货月台,请预约");
		}
		WmsDock dock = commonDao.load(WmsDock.class, booking.getDock().getId());
		WmsLocation location = commonDao.load(WmsLocation.class, dock.getReceiveLocationId());
		
		WmsASNDetailDTO dto = new WmsASNDetailDTO();
		dto.setAsnId(detail.getAsn().getId());
		dto.setAsnCode(asn.getCode());
		dto.setAsnDetailId(detail.getId());
		dto.setItemCode(item.getCode());
		dto.setItemName(item.getName());
		dto.setLocationId(location.getId());
		dto.setLocationCode(location.getCode());
		dto.setCurrentPackageUnit(detail.getPackageUnit().getId());
		
		String puHql = "FROM WmsPackageUnit pu where pu.item.id = :itemId and pu.id != :currentPackageUnitId order by pu.lineNo ASC ";
		List<WmsPackageUnit> otherPackageUnitList = commonDao.findByQuery(puHql, 
				new String[]{"itemId", "currentPackageUnitId"}, new Object[]{item.getId(), detail.getPackageUnit().getId()});
		dto.getPackageUnitList().add(currentPackageUnit);
		if (!otherPackageUnitList.isEmpty()) {
			dto.getPackageUnitList().addAll(otherPackageUnitList);
		}
		
		dto.setExpectedQuantityBU(detail.getExpectedQuantityBU() - detail.getReceivedQuantityBU());
		String itemStateHql = "FROM WmsItemState s where s.company.id = :companyId and s.status = 'ENABLED' and s.beReceive = true";
		dto.getStatusList().addAll(commonDao.findByQuery(itemStateHql, "companyId", asn.getCompany().getId()));
		dto.setLotRule(lotRule);
		if (detail.getLotInfo() == null) {
			dto.setLotInfo(new LotInfo());
		} else {
			if (detailSize > 1) {
				dto.setLotInfo(new LotInfo());
			} else {
				dto.setLotInfo(detail.getLotInfo());
			}
		}
		return dto;
	}
	
	public void detailReceive(WmsASNDetailDTO dto) throws BusinessException {
		WmsASNDetail detail = commonDao.load(WmsASNDetail.class, dto.getAsnDetailId());
		detail.setItem(commonDao.load(WmsItem.class, detail.getItem().getId()));
		try {
			detail.setLotInfo(dto.getLotInfo());
			wmsASNManager.detailReceive(detail, dto.getCurrentPackageUnit(), dto.getReceivingQuantityBU(), 
					dto.getLocationId(), dto.getItemStateId() == null ? "" : dto.getItemStateId().toString(), WmsWorkerHolder.getWmsWorker().getId());
		} catch (BusinessException be) {
			logger.error("", be);
			throw new BusinessException(be.getMessage());
		}
		
		WmsASN asn = commonDao.load(WmsASN.class, dto.getAsnId());
		if (WmsASNStatus.RECEIVED.equals(asn.getStatus())) {
			throw new RFFinishException("收货完成");
		}
	}
	public Long findAsnId(String asnCode){
		WmsASN asn = (WmsASN) commonDao.findByQueryUniqueResult("FROM WmsASN asn WHERE asn.code =:code", 
				new String[]{"code"}, new Object[]{asnCode});//WmsASNStatus.ACTIVE
		if(asn==null){
			asn = (WmsASN) commonDao.findByQueryUniqueResult("FROM WmsASN asn WHERE asn.relatedBill1 =:relatedBill1", 
					new String[]{"relatedBill1"}, new Object[]{asnCode});
			if(asn==null){
				return 0L;
			}
		}
		if(asn.getStatus().equals(WmsASNStatus.ACTIVE) 
				|| asn.getStatus().equals(WmsASNStatus.RECEIVING)){
			return asn.getId();
		}else if(asn.getStatus().equals(WmsASNStatus.OPEN)){
			wmsASNManager.active(asn);
			WorkflowManager workflowManager = (WorkflowManager) applicationContext.getBean("workflowManager");
			workflowManager.doWorkflow(asn, "wmsASNProcess.active");
		}else{
			return null;
		}
		return asn.getId();
	}
	public Long findConfirmAsnId(String asnCode){
		WmsASN asn = (WmsASN) commonDao.findByQueryUniqueResult("FROM WmsASN asn WHERE 1=1" +
				" AND asn.code =:code" +
				" AND (asn.status =:status1 OR asn.status =:status2) AND asn.confirmAccount = false", 
				new String[]{"code","status1","status2"}, 
				new Object[]{asnCode,WmsASNStatus.RECEIVING,WmsASNStatus.RECEIVED});
		if(asn==null){
			asn = (WmsASN) commonDao.findByQueryUniqueResult("FROM WmsASN asn WHERE 1=1" +
					" AND asn.relatedBill1 =:relatedBill1" +
					" AND (asn.status =:status1 OR asn.status =:status2) AND asn.confirmAccount = false", 
					new String[]{"relatedBill1","status1","status2"}, 
					new Object[]{asnCode,WmsASNStatus.RECEIVING,WmsASNStatus.RECEIVED});
			if(asn==null){
				return 0L;
			}
		}
		return asn.getId();
	}
	public String asnReceiveAll(Long asnId){
		try {
			wmsASNManager.receiveAll(asnId, WmsWorkerHolder.getWmsWorker().getId());
		} catch (Exception e) {
			return e.getLocalizedMessage();
		}
		return "1";
	}
	public String asnConfirmAll(Long asnId){
		try {
			wmsASNManager.confirmAccount(commonDao.load(WmsASN.class, asnId));
		} catch (Exception e) {
			return e.getLocalizedMessage();
		}
		return "1";
	}
	public String driectPrint(Long asnId)  throws HttpException, IOException{
		/*HttpClient client = new HttpClient();
		PostMethod postMethod = new PostMethod("http://localhost:8088/jac_fdj_wms/rf");
		//设置参数
		try {
			postMethod.setParameter("methodName", URLEncoder.encode("xxx.yyy","utf-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return e1.getLocalizedMessage();
		}
		//编码设置
		postMethod.getParams().setHttpElementCharset("utf-8");
		postMethod.getParams().setContentCharset("utf-8");
		client.executeMethod(postMethod);*/
		
		
		String error = "1";
		//打印托盘标签wmsPalletSerial.raq
		List<Long> jps = commonDao.findByQuery("SELECT jps.id FROM JacPalletSerial jps WHERE 1=1"
				+ " AND jps.asnDetail.asn.id =:asnId", 
				new String[]{"asnId"}, new Object[]{asnId});
		if(jps!=null && jps.size()>0){
			try {
				jps.clear();jps.add(asnId);
				List<Object[]> values = new ArrayList<Object[]>();
				values.add(new Object[]{
						StringUtils.substringBetween(jps.toString(), "[", "]")
				});
				String filePath = JavaTools.markDir("D:/logs/biaoqian");
				JavaTools.createTxt(values, 
						values.size(), 
						filePath+File.separator +"asnXg-"+asnId+"-"+JavaTools.format(new Date(), "yyyyMMddHHmmss")+".txt", 
						JavaTools.enter, "\t", "utf-8");
			} catch (Exception e) {
				error = "ASN收货后台打印标签异常:"+e.getLocalizedMessage();
				System.out.println(error);
			}
		}
		/*try {
			List<Long> jps = commonDao.findByQuery("SELECT jps.id FROM JacPalletSerial jps WHERE 1=1"
					+ " AND jps.asnDetail.asn.id =:asnId", 
					new String[]{"asnId"}, new Object[]{asnId});
			if(jps!=null && jps.size()>0){
				//=-=-=-=-=-=-=-=-=-=-=-=-=-=-
				String filePath = JavaTools.markDir("D:/logs/biaoqian");
				JavaTools.markTxt("进入方法体时间:", filePath+"/001.txt");
				//=-=-=-=-=-=-=-=-=-=-=-=-=-=-
				System.setProperty("java.awt.headless", "false");
				String appRoot = null,reportName=null,reportParams="id=0;ids=",printServiceName=null;
				appRoot = GlobalParamUtils.getGloableStringValue("rf_reportURL");//http://localhost:8088/jac_fdj_wms
				reportName = "wmsPalletSerial.raq";
				jps.clear();jps.add(asnId);
				reportParams += jps.toString();//id=0;ids=[172, 173]
				printServiceName = "Zebra ZT410 (203 dpi)";//\\\\WLA00554\\HP LaserJet M1005,Ultra PDF,Zebra ZT410 (203 dpi)
				VtradexDirectPrintJava a = new VtradexDirectPrintJava(appRoot, reportName, reportParams,printServiceName);
				//=-=-=-=-=-=-=-=-=-=-=-=-=-=-
				JavaTools.markTxt("初始化方法时间:", filePath+"/001.txt");
				//=-=-=-=-=-=-=-=-=-=-=-=-=-=-
				a.print();
				error = "1";
			}
		} catch (BusinessException e) {
			error = "ASN收货后台打印标签异常:"+e.getLocalizedMessage();
			System.out.println(error);
		}finally{
//			System.setProperty("java.awt.headless", "true");
			//=-=-=-=-=-=-=-=-=-=-=-=-=-=-
			String filePath = JavaTools.markDir("D:/logs/biaoqian");
			JavaTools.markTxt(error, filePath+"/001.txt");
			JavaTools.markTxt("结束方法体时间:", filePath+"/001.txt");
			
			JavaTools.moveFileToDel(filePath, 5);
			//=-=-=-=-=-=-=-=-=-=-=-=-=-=-
		}*/
		return error;
	}
	public static void main(String[] args) {
		String appRoot = null,reportName=null,reportParams="id=0;ids=",printServiceName=null;
		appRoot = "http://192.168.11.137:8077/jac_fdj_wms";//GlobalParamUtils.getGloableStringValue("rf_reportURL");//http://localhost:8088/jac_fdj_wms
		reportName = "wmsPalletSerial.raq";
		reportParams += "[200795]";//id=0;ids=[172, 173]
		printServiceName = "Ultra PDF";
		VtradexDirectPrintJava a = new VtradexDirectPrintJava(appRoot, reportName, reportParams,printServiceName);
		//=-=-=-=-=-=-=-=-=-=-=-=-=-=-
		String filePath = JavaTools.markDir("D:/logs/biaoqian");
		JavaTools.markTxt("初始化方法时间:", filePath+"/001.txt");
		//=-=-=-=-=-=-=-=-=-=-=-=-=-=-
		a.print();
	}
	public String palletAuto(Long asnId){
		//自动码托
		String message = "自动码托";
		try {
			message = wmsASNManager.palletAuto(asnId);
		} catch (Exception e) {
			System.out.println(message+":"+e.getLocalizedMessage());
			return message;
		}
		return message;
	}
	public Integer palletSerial(String palletNo,Double quantity){
		String hql = "FROM JacPalletSerial jps WHERE jps.palletNo =:palletNo "
				+ "AND jps.toLocationId IS NULL and jps.asnDetail.asn.isPrint='Y'";
		JacPalletSerial jps = (JacPalletSerial) commonDao.findByQueryUniqueResult(hql, 
				new String[]{"palletNo"}, new Object[]{palletNo});
		if(jps==null){
			return 0;
		}
		List<String> values = new ArrayList<String>();
		values.add(quantity.toString());
		wmsASNManager.palletSerial(jps, values);
		return 1;
		
	}

	@Override
	public WmsASNDetailDTO getWmsASNDetailDTO(Long detailId)throws BusinessException {

		WmsASNDetail detail = commonDao.load(WmsASNDetail.class, detailId);
		WmsASN asn = commonDao.load(WmsASN.class, detail.getAsn().getId());
		WmsItem item = commonDao.load(WmsItem.class, detail.getItem().getId());
//		WmsLotRule lotRule = commonDao.load(WmsLotRule.class, item.getLotRule().getId());
		WmsPackageUnit currentPackageUnit = commonDao.load(WmsPackageUnit.class, detail.getPackageUnit().getId());
		
//		WmsBooking booking = detail.getBooking();
//		if (booking  == null) {
//			throw new BusinessException("未指定收货月台,请预约");
//		}
//		WmsDock dock = commonDao.load(WmsDock.class, booking.getDock().getId());
//		WmsLocation location = commonDao.load(WmsLocation.class, dock.getReceiveLocationId());
//		
		WmsASNDetailDTO dto = new WmsASNDetailDTO();
		dto.setAsnId(detail.getAsn().getId());
		dto.setAsnCode(asn.getCode());
		dto.setAsnDetailId(detail.getId());
		dto.setItemCode(item.getCode());
		dto.setItemName(item.getName());
//		dto.setLocationId(location.getId());
//		dto.setLocationCode(location.getCode());
		dto.setCurrentPackageUnit(detail.getPackageUnit().getId());
		dto.setReceivingQuantityBU(detail.getReceivedQuantityBU());
		dto.setExpectedQuantityBU(detail.getExpectedQuantityBU());
		
		String puHql = "FROM WmsPackageUnit pu where pu.item.id = :itemId and pu.id != :currentPackageUnitId order by pu.lineNo ASC ";
		List<WmsPackageUnit> otherPackageUnitList = commonDao.findByQuery(puHql, 
				new String[]{"itemId", "currentPackageUnitId"}, new Object[]{item.getId(), detail.getPackageUnit().getId()});
		dto.getPackageUnitList().add(currentPackageUnit);
		if (!otherPackageUnitList.isEmpty()) {
			dto.getPackageUnitList().addAll(otherPackageUnitList);
		}
		
		dto.setExpectedQuantityBU(detail.getExpectedQuantityBU() - detail.getReceivedQuantityBU());
		String itemStateHql = "FROM WmsItemState s where s.company.id = :companyId and s.status = 'ENABLED' and s.beReceive = true";
		dto.getStatusList().addAll(commonDao.findByQuery(itemStateHql, "companyId", asn.getCompany().getId()));
//		dto.setLotRule(lotRule);
		if (detail.getLotInfo() == null) {
			dto.setLotInfo(new LotInfo());
		} 
		return dto;
	}

	@Override
	public List<WmsItemState> getItemStateStatus(Long detailId) {
		String hql = "from WmsItemState itemState where "
					+ "itemState.company.id=:comId and itemState.beReceive='Y'";
		WmsASNDetail detail = commonDao.load(WmsASNDetail.class, detailId);
		WmsASN asn = commonDao.load(WmsASN.class, detail.getAsn().getId());
		List<WmsItemState> values = commonDao.findByQuery(hql,"comId",asn.getCompany().getId());
		return values;
	}
	
	@Override
	public WmsASNDetail getAsnDetail(Long id){
		WmsASNDetail detail = commonDao.load(WmsASNDetail.class, id);
		return detail ;
	}
	
	public void detailReceive(Long detailId,Long stateId,Double receivingQuantityBU,Long userId,String inventoryState){
		WmsASNDetail detail = commonDao.load(WmsASNDetail.class, detailId);
		WmsPackageUnit u = commonDao.load(WmsPackageUnit.class, detail.getPackageUnit().getId());
		wmsASNManager.detailReceive(detail, u.getId(), receivingQuantityBU, 0L, stateId.toString(), userId,inventoryState);
	}

	@Override
	public WmsASN getAsnById(Long id) {
		WmsASN asn = commonDao.load(WmsASN.class, id);
		return asn;
	}
	
	
}
