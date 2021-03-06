package com.vtradex.wms.server.service.interfaces.pojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.vtradex.thorn.server.exception.BusinessException;
import com.vtradex.thorn.server.service.pojo.DefaultBaseManager;
import com.vtradex.wms.server.model.base.LotInfo;
import com.vtradex.wms.server.model.base.ShipLotInfo;
import com.vtradex.wms.server.model.interfaces.MiddleAsnSrmDetail;
import com.vtradex.wms.server.model.interfaces.MiddleOrderJh;
import com.vtradex.wms.server.model.interfaces.MiddleOrderKb;
import com.vtradex.wms.server.model.interfaces.MiddleOrderSps;
import com.vtradex.wms.server.model.interfaces.WHead;
import com.vtradex.wms.server.model.move.WmsMoveDocStatus;
import com.vtradex.wms.server.model.organization.WmsOrganization;
import com.vtradex.wms.server.model.receiving.WmsASN;
import com.vtradex.wms.server.model.receiving.WmsASNDetail;
import com.vtradex.wms.server.model.receiving.WmsASNShelvesStauts;
import com.vtradex.wms.server.model.receiving.WmsASNStatus;
import com.vtradex.wms.server.model.receiving.WmsSource;
import com.vtradex.wms.server.model.shipping.WmsPickTicket;
import com.vtradex.wms.server.model.shipping.WmsPickTicketDetail;
import com.vtradex.wms.server.model.shipping.WmsPickTicketStatus;
import com.vtradex.wms.server.model.warehouse.WmsWarehouse;
import com.vtradex.wms.server.service.interfaces.WmsDealTaskManager;
import com.vtradex.wms.server.service.sequence.WmsBussinessCodeManager;

public class DefaultWmsDealTaskManager 
			extends DefaultBaseManager implements WmsDealTaskManager{

	private WmsBussinessCodeManager codeManager;
	
	
	public WmsBussinessCodeManager getCodeManager() {
		return codeManager;
	}

	public void setCodeManager(WmsBussinessCodeManager codeManager) {
		this.codeManager = codeManager;
	}

	@SuppressWarnings("unchecked")
	public void dealAsn(Long id){
		WHead w = commonDao.load(WHead.class, id);
		if(w==null){
			return;
		}
		List<MiddleAsnSrmDetail> list = commonDao.findByQuery
				("from MiddleAsnSrmDetail a where a.head.id=:id", "id",w.getId());
		WmsWarehouse warehouse = getwareHouse();//仓库
		Date date = new Date();
		int i = 1;//标记生成ASN还是明细,以及生成行号
		Integer lineNo = 10;//行号
		WmsASN asn = null;
		List<Long> ids = new ArrayList<Long>();//中间表数据id
		Double totalQty = 0D;//ASN总数
		
		for(MiddleAsnSrmDetail detail : list){
			
			String asnCode = codeManager.generateCodeByRule(warehouse, detail.getCompany().getName(), "ASN", detail.getBillType().getName());
			
			if(i == 1){
				asn = new WmsASN(warehouse, detail.getCompany(),detail.getBillType(), asnCode,WmsASNStatus.OPEN, detail.getAsnNO(),
						detail.getPoNo(),date,WmsASNShelvesStauts.UNPUTAWAY,detail.getSupply(),WmsMoveDocStatus.OPEN,
						0d, WmsSource.INTERFACE,0d,Boolean.FALSE);
				commonDao.store(asn);
			}
			
			WmsASNDetail asnDetail = new WmsASNDetail(asn, lineNo*i, detail.getItem(), detail.getSendQty(), 
								detail.getPackageUnit(), detail.getSendQty(), 0d, 0d, detail.getTrayQty(), 0.0,detail.getIsMt());
			LotInfo lotInfo = new LotInfo(detail.getReDate(), asn.getCode(), detail.getSupply(), "-");
			asnDetail.setLotInfo(lotInfo);
			asnDetail.setPolineNo(detail.getPolineNo());
			asnDetail.setAsnLineNo(detail.getAsnLineNo());
			commonDao.store(asnDetail);
			i += 1;
			ids.add(id);
			totalQty += detail.getSendQty();
		}	
		updateTotalQty(null, asn, totalQty);//更新单头数量
	}

	WmsWarehouse getwareHouse(){
		WmsWarehouse warehouse = (WmsWarehouse) commonDao.findByQueryUniqueResult
							("from WmsWarehouse where name='新港仓库' and status='ENABLED'","","");
		if(null == warehouse){
			throw new BusinessException("未找到新港仓库");
		}
		return warehouse;
	}
	
	//更新 发货单/ASN 单头数量
	void updateTotalQty(WmsPickTicket pickTicket,WmsASN asn,Double qty){
		String updateHql = "";
		if(null == asn){
			updateHql = "update WmsPickTicket set "
							+ "expectedQuantityBU=:qtyParam where id="+pickTicket.getId();//更新整单数量
		}else{
			updateHql = "update WmsASN set expectedQuantityBU=:qtyParam where id="+asn.getId();
		}	
		commonDao.executeByHql(updateHql,"qtyParam" , qty);
	}
	
	@SuppressWarnings("unchecked")
	public void dealKbPickData(Long id){
		int i = 1;
		Integer lineNo = 10;
		Double totalQty = 0D;
		WmsPickTicket pickTicket = null;
		Date date = new Date();
		WmsWarehouse warehouse = getwareHouse();//仓库
		
		WHead w = commonDao.load(WHead.class, id);
		List<MiddleOrderKb> list = commonDao.findByQuery
				("from MiddleOrderKb a where a.head.id=:id", "id",w.getId());
		
		for(MiddleOrderKb kb : list){
			
			if(i == 1){
				String code = codeManager.generateCodeByRule(warehouse, kb.getCompany().getName(), "发货单", kb.getBillType().getName());
				pickTicket = new WmsPickTicket(warehouse,kb.getCompany(), kb.getBillType(), code, kb.getOdrNo(), 
								WmsPickTicketStatus.OPEN, kb.getDemandDate(), kb.getQty(), 0d, 0d, 0d,
								kb.getDware(), kb.getShdk(),WmsSource.INTERFACE,date,kb.getOdrSu(),kb.getBatch());
				commonDao.store(pickTicket);
			}
			WmsOrganization supplier = kb.getSupply();
			ShipLotInfo shipLotInfo = new ShipLotInfo
						(supplier == null ? null : supplier.getCode(),
								supplier == null ? null : supplier.getName());
			Double qty = kb.getQty().doubleValue();
			WmsPickTicketDetail detail = new WmsPickTicketDetail(pickTicket, kb.getItem(), 
									shipLotInfo,kb.getPackageUnit(),qty, qty, 0d, 0d, 0d, null, 
									null, kb.getProductLine(), kb.getStation(), kb.getIsJp(),lineNo*i,kb.getSx(),
									kb.getSmallQty().doubleValue(),kb.getPcs(),supplier,"-");
			commonDao.store(detail);
			
			i += 1;
			totalQty += qty;
		}
		updateTotalQty(pickTicket, null, totalQty);//更新单头数量
	}
	/**计划件*/
	public void dealJhPickData(Long id){
		int i = 1;
		Integer lineNo = 10;
		Double totalQty = 0D;
		WmsPickTicket pickTicket = null;
		Date date = new Date();
		WmsWarehouse warehouse = getwareHouse();//仓库
		
		WHead w = commonDao.load(WHead.class, id);
		List<MiddleOrderJh> list = commonDao.findByQuery
				("from MiddleOrderJh a where a.head.id=:id", "id",w.getId());
		
		for(MiddleOrderJh jh : list){
			
			if(i == 1){
				String code = codeManager.generateCodeByRule(warehouse, jh.getCompany().getName(), "发货单", jh.getBillType().getName());
				pickTicket = new WmsPickTicket(warehouse,jh.getCompany(), jh.getBillType(), code, jh.getOdrNo(), 
								WmsPickTicketStatus.OPEN, jh.getDemandDate(), jh.getQty(), 0d, 0d, 0d,
								jh.getDware(), jh.getShdk(),WmsSource.INTERFACE,date,jh.getOdrSu(),jh.getBatch());
				commonDao.store(pickTicket);
			}
			WmsOrganization supplier = jh.getSupply();
			ShipLotInfo shipLotInfo = new ShipLotInfo
						(supplier == null ? null : supplier.getCode(),
								supplier == null ? null : supplier.getName());
			Double qty = jh.getQty().doubleValue();
			WmsPickTicketDetail detail = new WmsPickTicketDetail(pickTicket, jh.getItem(), 
									shipLotInfo,jh.getPackageUnit(),qty, qty, 0d, 0d, 0d, jh.getSlr(), 
									jh.getSlr(), jh.getProductLine(), jh.getStation(), jh.getIsJp(),lineNo*i,0,
									0d,jh.getPackageUnit().getUnit(),supplier,"-");
			detail.setFromSource(jh.getFromSource());
			commonDao.store(detail);
			
			i += 1;
			totalQty += qty;
		}
		updateTotalQty(pickTicket, null, totalQty);//更新单头数量
	}
	
	/**时序件*/
	@SuppressWarnings("unchecked")
	public void dealSpsPickData(Long id){
		int i = 1;
		Integer lineNo = 10;
		Double totalQty = 0D;
		WmsPickTicket pickTicket = null;
		Date date = new Date();
		WmsWarehouse warehouse = getwareHouse();//仓库
		
		WHead w = commonDao.load(WHead.class, id);
		List<MiddleOrderSps> list = commonDao.findByQuery
				("from MiddleOrderSps a where a.head.id=:id", "id",w.getId());
		
		for(MiddleOrderSps sps : list){
			WmsOrganization company = commonDao.load(WmsOrganization.class, sps.getCompany().getId());
			if(i == 1){
				String code = codeManager.generateCodeByRule(warehouse, company.getName(), "发货单", sps.getBillType().getName());
				pickTicket = new WmsPickTicket(warehouse,company, sps.getBillType(), code, sps.getOdrNo(), 
								WmsPickTicketStatus.OPEN, sps.getDemandDate(), sps.getQty(), 0d, 0d, 0d,
								sps.getDware(), sps.getShdk(),WmsSource.INTERFACE,date,sps.getOdrSu(),sps.getBatch());
				commonDao.store(pickTicket);
			}
			WmsOrganization supplier = sps.getSupply();
			ShipLotInfo shipLotInfo = new ShipLotInfo
						(supplier == null ? null : supplier.getCode(),
								supplier == null ? null : supplier.getName());
			Double qty = sps.getQty().doubleValue();
			WmsPickTicketDetail detail = new WmsPickTicketDetail(pickTicket, sps.getItem(), 
									shipLotInfo,sps.getPackageUnit(),qty, qty, 0d, 0d, 0d, sps.getSlr(), 
									sps.getSlr(), sps.getProductLine(), sps.getStation(), sps.getIsJp(),
									lineNo*i,sps.getSx(),0d,sps.getPackageUnit().getUnit(),supplier,"-");
			detail.setType(sps.getPackageNo());
			detail.setPackageNum(sps.getPackageNum());
			detail.setPackageQty(sps.getPackageQty());
			detail.setRemark(sps.getRemark());
			detail.setFromSource(sps.getFromSource());
			commonDao.store(detail);
			
			i += 1;
			totalQty += qty;
		}
		updateTotalQty(pickTicket, null, totalQty);//更新单头数量
	}
}
