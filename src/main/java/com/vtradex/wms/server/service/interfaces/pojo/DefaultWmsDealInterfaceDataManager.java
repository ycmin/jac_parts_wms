package com.vtradex.wms.server.service.interfaces.pojo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import com.vtradex.thorn.server.exception.BusinessException;
import com.vtradex.thorn.server.model.UpdateInfo;
import com.vtradex.thorn.server.model.message.Task;
import com.vtradex.thorn.server.service.pojo.DefaultBaseManager;
import com.vtradex.wms.server.model.base.BaseStatus;
import com.vtradex.wms.server.model.base.Contact;
import com.vtradex.wms.server.model.base.ShipLotInfo;
import com.vtradex.wms.server.model.interfaces.HeadType;
import com.vtradex.wms.server.model.interfaces.MiddleAsnSrmDetail;
import com.vtradex.wms.server.model.interfaces.MiddleOrderJh;
import com.vtradex.wms.server.model.interfaces.MiddleOrderKb;
import com.vtradex.wms.server.model.interfaces.MiddleOrderSps;
import com.vtradex.wms.server.model.interfaces.MiddleSupplyItemErpA;
import com.vtradex.wms.server.model.interfaces.MiddleSupplyItemErpB;
import com.vtradex.wms.server.model.interfaces.WHead;
import com.vtradex.wms.server.model.move.QisPlan;
import com.vtradex.wms.server.model.move.WmsMoveDoc;
import com.vtradex.wms.server.model.move.WmsMoveDocDetail;
import com.vtradex.wms.server.model.move.WmsMoveDocType;
import com.vtradex.wms.server.model.move.WmsTask;
import com.vtradex.wms.server.model.organization.WmsBillType;
import com.vtradex.wms.server.model.organization.WmsEnumType;
import com.vtradex.wms.server.model.organization.WmsItem;
import com.vtradex.wms.server.model.organization.WmsLotRule;
import com.vtradex.wms.server.model.organization.WmsOrganization;
import com.vtradex.wms.server.model.organization.WmsPackageUnit;
import com.vtradex.wms.server.model.organization.WmsStationAndItem;
import com.vtradex.wms.server.model.receiving.WmsASN;
import com.vtradex.wms.server.model.receiving.WmsASNDetail;
import com.vtradex.wms.server.model.receiving.WmsSource;
import com.vtradex.wms.server.model.shipping.WmsBOL;
import com.vtradex.wms.server.model.shipping.WmsBOLDetail;
import com.vtradex.wms.server.model.shipping.WmsMoveDocAndStation;
import com.vtradex.wms.server.model.shipping.WmsPickTicket;
import com.vtradex.wms.server.model.shipping.WmsPickTicketAndAppliance;
import com.vtradex.wms.server.model.shipping.WmsPickTicketDetail;
import com.vtradex.wms.server.model.shipping.WmsPickTicketStatus;
import com.vtradex.wms.server.model.warehouse.WmsWarehouse;
import com.vtradex.wms.server.service.interfaces.WmsDealInterfaceDataManager;
import com.vtradex.wms.server.service.middle.MiddleTableName;
import com.vtradex.wms.server.service.middle.MilldleSessionManager;
import com.vtradex.wms.server.service.sequence.WmsBussinessCodeManager;
import com.vtradex.wms.server.utils.DateUtil;
import com.vtradex.wms.server.utils.EdiTaskType;
import com.vtradex.wms.server.utils.JavaTools;
import com.vtradex.wms.server.utils.MyUtils;
import com.vtradex.wms.server.utils.StringHelper;
import com.vtradex.wms.server.utils.WmsTables;
import com.vtradex.wms.server.web.filter.WmsWarehouseHolder;

public class DefaultWmsDealInterfaceDataManager  
				extends DefaultBaseManager implements WmsDealInterfaceDataManager{

	public static Integer PAGE_NUMBER = 500;
	private WmsBussinessCodeManager codeManager;
	private DataSource dataSource;//中间表数据源
	private JdbcTemplate jdbcTemplateExt1;//中间表数据源
	private JdbcTemplate jdbcTemplate;//WMS数据源
	
	public WmsBussinessCodeManager getCodeManager() {
		return codeManager;
	}
	public void setCodeManager(WmsBussinessCodeManager codeManager) {
		this.codeManager = codeManager;
	}
	public DataSource getDataSource() {
		return dataSource;
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	public JdbcTemplate getJdbcTemplateExt1() {
		return jdbcTemplateExt1;
	}
	public void setJdbcTemplateExt1(JdbcTemplate jdbcTemplateExt1) {
		this.jdbcTemplateExt1 = jdbcTemplateExt1;
	}
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public DefaultWmsDealInterfaceDataManager() {
	}
	//wmsDealInterfaceDataManager.executeT
	public void executeT(){
		int i = 1;
		while(true){
			System.out.println(i);
			dealErpItemData();
			i++;
			try {
				Thread.sleep(1000*2);//2'
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
	public void mesReturnOrderFor(){
		while(true){
			mesReturnOrder();
			try {
				Thread.sleep(1000*60*2);//2min
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
	//wmsDealInterfaceDataManager.mesReturnOrder
	public void mesReturnOrder(){
		System.out.println("-----------开始处理MES退料单数据-----------");
		WmsWarehouse warehouse = getwareHouse();//仓库
		//处理MES退料单数据
		String mesOrderSql = "select id,ASNNO,PONO,ITEMCODE,SUPPLY_NO,"
				+ "STORECODE,UNIT,SENDQTY,REQDATE,ORD_TYPE,IS_MT,TRAYQTY,ITEMNAME, "
				+ "SUPPLY_NAME from "+MiddleTableName.W_ASN_TL+" where status=1"
				+ " ORDER BY upper(ASNNO) desc";
		dealMesAndSrmData(warehouse,mesOrderSql,MiddleTableName.W_ASN_TL);
		System.out.println("-----------结束处理MES退料单数据-----------");
	}
	
	public void srmOrderFor(){
		while(true){
			srmOrder();
			try {
				Thread.sleep(1000*60*2);//2min
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
	//wmsDealInterfaceDataManager.srmOrder
	public void srmOrder(){
		System.out.println("-----------开始处理SRM中间表数据-----------");
		WmsWarehouse warehouse = getwareHouse();//仓库
		//处理SRM数据
		String srmOrderSql = "select id,ASNNO,PONO,ITEMCODE,SUPPLY_NO,STORECODE,UNIT,"
								+ "SENDQTY,REQDATE,ORD_TYPE,IS_MT,TRAYQTY,ITEMNAME,SUPPLY_NAME,"
								+ "POLINENO,ASNLINENO "
								+ "from "+MiddleTableName.W_ASN_SRM
								+" where status = 1 "
//								+" where asnno='SH2898263' "//test
								+ "ORDER BY upper(ASNNO) desc";
			
		dealMesAndSrmData(warehouse,srmOrderSql,MiddleTableName.W_ASN_SRM);
		System.out.println("-----------结束处理SRM中间表数据-----------");
	}
	//临采的不取数了
	public void dealErpOrder() {
		WmsWarehouse warehouse = getwareHouse();//仓库
//		logger.error("-----------开始处理临采件入库数据-----------");
//		//处理ERP数据
//		String erpSql = "select id,ASNNO,PONO,ITEMCODE,SUPPLY_NO,STORECODE,"
//							+ "UNIT,SENDQTY,REQDATE,ORD_TYPE,IS_MT,TRAYQTY "
//							+ "from "+MiddleTableName.W_ASN_ERP+" where status = 1 "
//							+ "ORDER BY upper(ASNNO) desc";
//		dealMesAndSrmData(warehouse,erpSql,MiddleTableName.W_ASN_ERP);
		logger.error("-----------结束处理临采件入库数据-----------");
	}
	//公用处理接口数据的方法  edit yc.min
	@SuppressWarnings("unchecked")
	void dealMesAndSrmData(WmsWarehouse warehouse,String sql,String tableName){
		Set<String> billCodes = new TreeSet<String>();
		int num = 1,count=0;
		List<Object[]> tempDatas = new ArrayList<Object[]>();//存储每批接口(一个送货单号为一批)数据
		List<Long> ids = new ArrayList<Long>();//中间表数据id
		List<String> errorLog = new ArrayList<String>();//记录每条错误日志
		Boolean isAutoError = false;
		Map<String,Boolean> isErrors = new HashMap<String, Boolean>();
		Map<String,List<String>> errorsLogs = new HashMap<String, List<String>>();
		List<String> errors = null;//记录每条错误日志
		
		String hql = "FROM WmsLotRule WHERE code='默认批次规则' AND status='ENABLED'";
		WmsLotRule lotRule = (WmsLotRule) commonDao.findByQueryUniqueResult(hql, "", "");
		if(null == lotRule){
			errorLog.add("WMS没有找到[默认批次规则];");
			isAutoError = true;
		}
		Map<String,WmsOrganization> companys = new HashMap<String, WmsOrganization>();//如果存在了的货主,不要再次查询数据
		Map<String,WmsOrganization> suppliers = new HashMap<String, WmsOrganization>();
		Map<String,WmsBillType> types = new HashMap<String, WmsBillType>();
		
		List list = jdbcTemplateExt1.queryForList(sql);
		Iterator asnList = list.iterator();
		while(asnList.hasNext()){
			Long id = null;
			String relateBill1 = null;
			String poNo = null;
			String itemCode = null;
			String supplyNo = null;
			String fware = null;
			String unit = null;
			Double qty =  null;
			Date demandDate = null;
			String billCode = null;
			Boolean isMt = null;
			Integer palletNo = null;
			String itemName = null;
			String supplyName = null;
			WmsBillType bType = null;//validateBillType(errorLog, billCode,fware);//单据类型
			WmsOrganization company = null;//validateOrg(errorLog, fware,"hz",Boolean.TRUE);//货主
			WmsOrganization supplier = null;//validateOrg(errorLog, supplyNo,"gys",Boolean.FALSE);//供应商
			WmsItem item = null;
			WmsPackageUnit packageUnit = null;//validatePackageUnit(errorLog, item,unit);//包装单位
			Integer polineNo = null;
			Integer asnLineNo = null;
			try {
				Map map = (Map) asnList.next();//行数据
				id = ((BigDecimal) map.get("ID")).longValue();//id 0
				relateBill1 = map.get("ASNNO").toString();//送货单号 1
				poNo = map.get("PONO") == null ? null : map.get("PONO").toString();//订单号 2
				itemCode = map.get("ITEMCODE").toString().toUpperCase();//货品编码 3
				supplyNo = map.get("SUPPLY_NO").toString().toUpperCase();//供应商编码 4
				fware = map.get("STORECODE").toString().toUpperCase();//货主 5
				unit = map.get("UNIT").toString().toUpperCase();//包装单位 6
				qty =  ((BigDecimal) map.get("SENDQTY")).doubleValue();//数量 7
				demandDate = (Date) map.get("REQDATE");//生产日期 8
				billCode = map.get("ORD_TYPE").toString().toUpperCase();//单据类型 9
				isMt = map.get("IS_MT").toString().equals("1") ? Boolean.TRUE : Boolean.FALSE;
				palletNo = ((BigDecimal) map.get("TRAYQTY")).intValue();//托盘总个数
				itemName = (String) map.get("ITEMNAME");//物料名称
				supplyName = (String) map.get("SUPPLY_NAME");//供应商名称
				if(tableName.equals(MiddleTableName.W_ASN_SRM)){
					polineNo = ((BigDecimal) map.get("POLINENO")).intValue();
					asnLineNo = ((BigDecimal) map.get("ASNLINENO")).intValue();
				}
				
				if(isAutoError){//全局的错误,那么将导致所有的数据都失败
					updateMiddleStatus(tableName, errorLog, id);
					continue;
				}
				/**
				 * 数据校验
				 * 货品和供应商如果在wms没有找到对应的数据,则根据接口中的物料名称和供应商名称新建,如果接口中的物料名称和供应商名称为空,则报错
				 * */
				if(companys.containsKey(fware)){
					company = companys.get(fware);
				}else{
					hql = "FROM WmsOrganization o WHERE upper(o.code)=:code";
					company = (WmsOrganization) commonDao.findByQueryUniqueResult(hql, "code", fware);
					companys.put(fware, company);
				}
				if(null == company){//校验货主在WMS里是否存在
					errorLog.add("货主("+fware+")WMS不存在;");
				}else{
					if(types.containsKey(billCode)){
						bType = types.get(billCode);
					}else{
						hql = "FROM WmsBillType WHERE upper(code)=:code AND status='ENABLED' AND upper(company.code)=:companyCode";
						bType = (WmsBillType) commonDao.findByQueryUniqueResult(hql, 
														new String[]{"code","companyCode"},
																new Object[]{billCode,fware});
						types.put(billCode, bType);
					}
					if(null == bType){
						errorLog.add("单据类型WMS不存在;");
					}
				}
				
				if(suppliers.containsKey(supplyNo)){
					supplier = suppliers.get(supplyNo);
				}else{
					hql = "FROM WmsOrganization o WHERE upper(o.code)=:code";
					supplier = (WmsOrganization) commonDao.findByQueryUniqueResult(hql, "code", supplyNo);
					suppliers.put(supplyNo, supplier);
				}
				if(null == supplier){
					if(null == supplyName){
						errorLog.add("供应商名称不能为空;");//因为要新建,所以名称不可以为空
					}else{
						supplier = new WmsOrganization(supplyNo, 
								supplyName, null, BaseStatus.ENABLED);
						supplier.setUpdateInfo(newUpdateInfo());
						commonDao.store(supplier);
					}
				}
				
				if(null != company){
					item = validateItem(errorLog, itemCode,fware,null);//物料
					if(null == item){
						if(null == itemName){
							errorLog.add("物料名称不能为空;");//因为要新建,所以名称不可以为空
						}else{
							item = new WmsItem(company, itemCode, itemName, 
									unit, BaseStatus.ENABLED,lotRule);//货品
							item.setUpdateInfo(newUpdateInfo());
							commonDao.store(item);
							packageUnit = new WmsPackageUnit(item, 
								1, unit, BaseStatus.DEFAULT_PACKAGE_LEVEL, 1);//包装
							packageUnit.setUpdateInfo(newUpdateInfo());
							commonDao.store(packageUnit);
						}
					}else{
						if(StringUtils.isEmpty(unit)){
							errorLog.add("单位不能为空;");//因为要新建,所以名称不可以为空
						}
						else{
							List<WmsPackageUnit> pus = commonDao.findByQuery("FROM WmsPackageUnit pu WHERE pu.item.id =:id" +
									" AND upper(pu.unit) =:unit",new String[]{"id","unit"},new Object[]{item.getId(),unit});
							if(pus==null || pus.size()<=0){
								pus = commonDao.findByQuery("FROM WmsPackageUnit pu WHERE pu.item.id =:id" +
										" AND pu.lineNo = 1",new String[]{"id"},new Object[]{item.getId()});
								if(pus==null || pus.size()<=0){
									packageUnit = new WmsPackageUnit(item, 
											1, unit, BaseStatus.DEFAULT_PACKAGE_LEVEL, 1);//包装
										packageUnit.setUpdateInfo(newUpdateInfo());
										commonDao.store(packageUnit);
								}else{
									packageUnit = pus.get(0);
								}
							}else{
								packageUnit = pus.get(0);
							}
						}
					}
				}
				//数据校验完毕*******************************
			} catch (Exception e) {
				errorLog.add(e.getMessage());//添加到错误列表,保存到数据库
			}
			//sql一定要order by relateBill1
			billCodes.add(relateBill1);
			if(billCodes.size()>num){//新的code数据来了
				dealRelateBill1(isErrors, errorsLogs, tempDatas, warehouse, tableName, ids);
				num++;
				//初始化
				tempDatas.clear();
				ids.clear();
				isErrors.clear();
				errorsLogs.clear();
			}
			if(errorLog.size()>0){//同一个单号下的任何一条数据有问题,那么将视为整个单号的数据有问题
				isErrors.put(relateBill1, true);
				errors = new ArrayList<String>();
				errors.addAll(errorLog);
				errorsLogs.put(relateBill1, errors);
			}
			tempDatas.add(new Object[]{id,relateBill1,poNo,
					item,supplier,company,packageUnit,qty,demandDate,
					bType,isMt,palletNo,polineNo,asnLineNo});//单条数据校验通过,存入临时list
			ids.add(id);
			
			errorLog.clear();
			count++;
			if(count==list.size()){//最后有一批
				dealRelateBill1(isErrors, errorsLogs, tempDatas, warehouse, tableName, ids);
			}
		}
	}
	private void dealRelateBill1(Map<String,Boolean> isErrors,Map<String,List<String>> errorsLogs,List<Object[]> tempDatas,
			WmsWarehouse warehouse,String tableName,List<Long> ids){
		List<String> errorSaveLog = new ArrayList<String>();
		if(isErrors!=null && isErrors.size()>0){//判断上一批单号是否有错误
			Iterator<Entry<String, Boolean>> itera = isErrors.entrySet().iterator();
			while(itera.hasNext()){
				Entry<String, Boolean> entry = itera.next();
				if(entry.getValue()){//此单号下明细数据有错误
					tempDatas.clear();
					errorSaveLog = errorsLogs.get(entry.getKey());
				}
			}
		}
		if(tempDatas.size()>0){
			MilldleSessionManager milldleSessionManager = (MilldleSessionManager) applicationContext.getBean("milldleSessionManager");
			try {
				milldleSessionManager.saveAsnDataToWms(tempDatas, warehouse, tableName);
			} catch (Exception e) {
				errorSaveLog.add(e.getMessage());//添加到错误列表,保存到数据库
			}
		}
		updateMiddleStatus(tableName, errorSaveLog, ids);
	}
	/**
	 * @param objs = 临时中间表数据
	 * 新建ASN
	 * 此方法优化为先把数据存入Task表,然后由edi去保存到wms
	 */
	/*void saveAsnData(List<Object[]> tempDatas,WmsWarehouse warehouse,
			String tableName,List<String> errorLog){
		Date date = new Date();
		int i = 1;//标记生成ASN还是明细,以及生成行号
		Integer lineNo = 10;//行号
		WmsASN asn = null;
		List<Long> ids = new ArrayList<Long>();//中间表数据id
		Double totalQty = 0D;//ASN总数
		
		for(Object[] obj : tempDatas){
			Long id = (Long) obj[0];
			String relateBill1 = (String) obj[1];//送货单号
			String orderNo = obj[2].toString().toUpperCase();//订单号
			Double qty =  (Double) obj[7];//数量
			Date demandDate = (Date) obj[8];//生产日期
			Boolean isMt = obj[10].equals("1") ? Boolean.TRUE : Boolean.FALSE;//是否码托
			Integer palletNo = (Integer) obj[11];//托盘总个数
			WmsOrganization company = (WmsOrganization) obj[5] ;//货主
			WmsItem item = (WmsItem)obj[3] ;//货品
			WmsOrganization supplier = (WmsOrganization) obj[4] ;//供应商
			WmsBillType type = (WmsBillType) obj[9] ;//单据类型
			WmsPackageUnit packageUnit = (WmsPackageUnit) obj[6];//包装
			String asnCode = getCodeByRule(errorLog, company, type, warehouse, "ASN");//获取单据编码
			
			if(i == 1){
				asn = new WmsASN(warehouse, company, type, asnCode,WmsASNStatus.OPEN, relateBill1,
						orderNo,date,WmsASNShelvesStauts.UNPUTAWAY,supplier,WmsMoveDocStatus.OPEN,
						0d, WmsSource.INTERFACE,qty,Boolean.FALSE);
				commonDao.store(asn);
			}
			
			WmsASNDetail detail = new WmsASNDetail(asn, lineNo*i, item, qty, packageUnit, qty, 0d, 0d, palletNo, 0.0,isMt);
			LotInfo lotInfo = new LotInfo(demandDate, asn.getCode(), supplier, "-");
			detail.setLotInfo(lotInfo);
			commonDao.store(detail);
			i += 1;
			ids.add(id);
			totalQty += qty;
		}	
		//更新状态
		updateMiddleStatusByBatch(tableName,ids,Boolean.FALSE);
		updateTotalQty(null, asn, totalQty);//更新单头数量
	}*/
	
	/**通过校验的数据存入WMS临时表*/
	void saveAsnDataToWms(List<Object[]> tempDatas,WmsWarehouse warehouse,
			String tableName){
		
		List<Long> ids = new ArrayList<Long>();//中间表数据id
		/***/
		WHead w = new WHead(1, new Date(), HeadType.ASN);
		commonDao.store(w);
		for(Object[] obj : tempDatas){
			Long id = (Long) obj[0];
			String relateBill1 = (String) obj[1];//送货单号
			String orderNo = obj[2].toString().toUpperCase();//订单号
			Double qty =  (Double) obj[7];//数量
			Date demandDate = (Date) obj[8];//生产日期
			Boolean isMt = obj[10].equals("1") ? Boolean.TRUE : Boolean.FALSE;//是否码托
			Integer palletNo = (Integer) obj[11];//托盘总个数
			WmsOrganization company = (WmsOrganization) obj[5] ;//货主
			WmsItem item = (WmsItem)obj[3] ;//货品
			WmsOrganization supplier = (WmsOrganization) obj[4] ;//供应商
			WmsBillType type = (WmsBillType) obj[9] ;//单据类型
			WmsPackageUnit packageUnit = (WmsPackageUnit) obj[6];//包装
			
			MiddleAsnSrmDetail mas = new MiddleAsnSrmDetail(relateBill1, orderNo, 
							item, supplier, company, packageUnit,
					qty, demandDate, type, isMt, palletNo,w);
			commonDao.store(mas);
			
			ids.add(id);
		}	
		
		Task task = new Task(HeadType.ASN, 
				"wmsDealTaskManager"+MyUtils.spiltDot+"dealAsn", w.getId());
		commonDao.store(task);
		//更新状态
		updateMiddleStatusByBatch(tableName,ids,Boolean.FALSE);
	}
	
	/**
	 * @param errorLog
	 * @param fware
	 * @param type = 供应商/货主
	 * @param notNull = true 不能为空 false=可为空,不记错误信息
	 * @return
	 */
	WmsOrganization validateOrg(List<String> errorLog,String fware,String type,Boolean notNull){
		String hql = "from WmsOrganization o where o.code=:code";
		WmsOrganization org = (WmsOrganization) commonDao.findByQueryUniqueResult(hql, "code", fware);
		if(null == org && notNull){
			if("hz".equals(type)){
				errorLog.add("根据编码("+fware+")未找到对应的货主!\n");
			}else{
				errorLog.add("根据编码("+fware+")未找到对应的供应商!\n");
			}
		}
		hql = null;
		return org;
	}
	
	/**
	 * @param errorLog
	 * @param itemCode
	 * @param companyCode
	 * @param isNull == true 如果没有找到货品,不记录错误信息
	 * @return
	 */
	WmsItem validateItem(List<String> errorLog,String itemCode,String companyCode,Boolean isNull){
		String hql = "from WmsItem item where upper(item.code)=:code and status='ENABLED' AND upper(company.code)=:companyCode";
		WmsItem item = (WmsItem) commonDao.findByQueryUniqueResult(hql, 
										new String[]{"code","companyCode"}, 
											new Object[]{itemCode,companyCode});
		if(null == item && null != isNull && !isNull){
			errorLog.add("根据编码("+itemCode+")+货主("+companyCode+")未找到对应的货品!\n");
		}
		if(null != item && null != isNull && isNull){//货品已经存在,记录错误信息
			errorLog.add("根据编码("+itemCode+")+货主("+companyCode+")已经找到了对应货品!\n");
		}
		hql = null;
		return item;
	}
	
	WmsBillType validateBillType(List<String> errorLog,String billCode,String companyCode){
		String hql = "from WmsBillType where code=:code and status='ENABLED' and company.code=:companyCode";
		WmsBillType bType = (WmsBillType) commonDao.findByQueryUniqueResult(hql, 
										new String[]{"code","companyCode"},
												new Object[]{billCode,companyCode});
		if(null == bType){
			errorLog.add("根据编码("+billCode+")+货主("+companyCode+")未找到对应的单据类型!\n");
		}
		hql = null;
		return bType;
	}
	
	/**
	 * 校验接口表中包装单位与wms货品对应的包装单位是否一致
	 * @param errorLog
	 * @param item
	 * @param unit
	 * @return
	 */
	WmsPackageUnit validatePackageUnit(List<String> errorLog, WmsItem item,String unit){
		if(item == null){
			return null;
		}
		@SuppressWarnings("unchecked")
		List<WmsPackageUnit> list = commonDao.
				findByQuery("from WmsPackageUnit where item_id=:id","id",
						item.getId());//包装单位
		if(list.size() <= 0){
			errorLog.add("物料编码("+item.getCode()+")未找到包装单位;");
			return null;
		}
		if(null == unit || "".equals(unit)){//如果PCS为空,说明当前表里没有PCS字段,不校验包装单位
			return list.get(0);
		}
//		if(!unit.equals(list.get(0).getUnit())){
//			errorLog.add("物料编码的包装单位与接口表中的包装单位不符,请检查!!\n");
//		}
		return list.get(0);
	}
	
	WmsWarehouse getwareHouse(){
		WmsWarehouse warehouse = (WmsWarehouse) commonDao.findByQueryUniqueResult
							("from WmsWarehouse where name='新港仓库' and status='ENABLED'","","");
		if(null == warehouse){
			throw new BusinessException("未找到新港仓库");
		}
		return warehouse;
	}
	
	/**
	 * 获取数据库连接
	 * @return
	 */
	private Connection getConnection() {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return connection;
	}
	
	/**
	 * 关闭连接
	 * @param rs
	 * @param ps
	 * @param connection
	 */
	private void closeConnection(ResultSet rs ,PreparedStatement ps,Connection connection){
		try {
			if (rs != null) { // 关闭记录集
				rs.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) { // 关闭声明
					ps.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (connection != null) { // 关闭连接对象
					try {
						connection.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}
	public void dealAsnData(WmsASN asn) {
		String hql = "from WmsASNDetail d where d.receivedQuantityBU > 0 and d.asn.id=:id";
		@SuppressWarnings("unchecked")
		List<WmsASNDetail> details = commonDao.findByQuery(hql,"id",asn.getId());
		Date date = new Date();
		int i = 0;//行号
		Connection connection = getConnection();
		if(null == connection){
			throw new BusinessException("数据库连接失败!!!");
		}
		WmsWarehouse w = commonDao.load(WmsWarehouse.class, asn.getWarehouse().getId());
		WmsBillType bill = commonDao.load(WmsBillType.class,asn.getBillType().getId());
		PreparedStatement pre = null;
		ResultSet rs = null;
		for(WmsASNDetail detail : details){
			i += 1;
//			Long maxId = getMaxId(pre,rs,connection,MiddleTableName.W_RECEIVE_ERP);
			String sql = "insert into "+MiddleTableName.W_RECEIVE_ERP+" (ASNNO,"
					+ "SHDH,LINE_NO,ORDER_CODE,ORDER_LINE_NO, REC_QTY, INV_LOC, UNIT, "
					+ "ITEMCODE, SUPPLY_CODE,ORDER_TYPE, RECEIVE_DATE, STATUS,ID,detail_id)"
					+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,WSEQ_W_RECEIVE_ERP.Nextval,?)";
			try {
				WmsPackageUnit pu = commonDao.load(WmsPackageUnit.class, detail.getPackageUnit().getId());
				WmsItem item = commonDao.load(WmsItem.class, detail.getItem().getId());
				WmsOrganization supp = commonDao.load(WmsOrganization.class,detail.getLotInfo().getSupplier().getId());
				WmsOrganization company = commonDao.load(WmsOrganization.class,asn.getCompany().getId());
				
				
				pre = connection.prepareStatement(sql);
				pre.setString(1,asn.getCode());//收货单号
				pre.setString(2, asn.getRelatedBill1());//SRM收货单号
				pre.setInt(3,StringHelper.replaceNullToZero(detail.getAsnLineNo()));//收货单行号
				pre.setString(4, asn.getRelatedBill2());//订单号
				pre.setInt(5, StringHelper.replaceNullToZero(detail.getPolineNo()));//订单行号 空值就给0
				pre.setDouble(6, detail.getReceivedQuantityBU());//收货数量
				pre.setString(7, company.getCode());//货主代码
				pre.setString(8, pu.getUnit());//包装单位
				pre.setString(9, item.getCode());//货品代码
				pre.setString(10, supp.getCode());//供应商
				pre.setString(11, bill.getCode());//单据类型
				pre.setDate(12, new java.sql.Date(date.getTime()));//收货日期
//				pre.setTimestamp(12, new java.sql.Timestamp(date.getTime()));//收货日期,以实际中间表字段为主
				pre.setInt(13, 1);//状态
//				pre.setLong(14, maxId);//状态
				pre.setLong(14, detail.getId());//明细ID
				pre.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
				closeConnection(rs, pre, connection);
				throw new BusinessException("处理失败："+e.getMessage());
			}
		}
		closeConnection(rs, pre, connection);
	}
	
	public Long getMaxId(PreparedStatement pre,ResultSet rs,
						Connection connection,String tableName){
		Long maxId = 1l;
		String sql = "select MAX(id) from "+tableName;
		try {
			pre = connection.prepareStatement(sql);
			rs = pre.executeQuery();// 执行查询，注意括号中不需要再加参数
			if(rs.next()){
				maxId = rs.getLong(1) + 1;
			}
		}catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return maxId;
	}
	public void dealOrderJhFor(){
		while(true){
			dealOrderJh();
			try {
				Thread.sleep(1000*60*2);//2min
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
	//wmsDealInterfaceDataManager.dealOrderJh
	public void dealOrderJh(){
		logger.error("-----------开始处理计划件料单中间表数据-----------");
		WmsWarehouse warehouse = getwareHouse();//新港仓库
		//MES料单
		String orderJhSql = "select id,ODR_NO,ODR_TYPE,DEMAND_DATE,SUPPLY_NO,ITEM,"
								+ "QTY,FWARE,ODR_SU,DWARE,PRODUCT_LINE,SHDK,IS_JP,"
								+ "BATCH,STATION,SLR,FROM_SOURCE from "+MiddleTableName.W_ORDER_JH
								+" WHERE status  = 1"
//								+" WHERE upper(ODR_NO) = 'JH170929000004'"
								+" ORDER BY upper(ODR_NO) desc";
		
		createPickTicketByMiddleData(warehouse,orderJhSql,MiddleTableName.W_ORDER_JH);
		logger.error("-----------结束处理计划件料单中间表数据-----------");
	}
	
	public void dealOrderSpsFor(){
		while(true){
			dealOrderSps();
			try {
				Thread.sleep(1000*60*2);//2min
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
	//wmsDealInterfaceDataManager.dealOrderSps
	public void dealOrderSps(){
		logger.error("-----------开始处理时序件料单中间表数据-----------");
		WmsWarehouse warehouse = getwareHouse();//新港仓库
		//时序件料单
		String orderSxSql = "select id,ODR_NO,ODR_TYPE,DEMAND_DATE,SUPPLY_NO,"
						+ "ITEM,QTY,FWARE,ODR_SU,DWARE,PRODUCT_LINE,SHDK,IS_JP,"
						+ "BATCH,STATION,SLR,SX,PACKAGE_NO,PACKAGE_NUM,PACKAGE_QTY, "
						+ "REMARK,FROM_SOURCE from "+MiddleTableName.W_ORDER_SPS
//						+" where ODR_NO = 'SXJ171106000013'"//test
						+" where status = 1"
						+ " ORDER BY upper(ODR_NO) desc";
		createPickTicketByMiddleData(warehouse,orderSxSql,MiddleTableName.W_ORDER_SPS);
		logger.error("-----------结束处理时序件料单中间表数据-----------");
	}
	public void dealOrderKbFor(){
		while(true){
			dealOrderKb();
			try {
				Thread.sleep(1000*60*2);//2min
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
	//wmsDealInterfaceDataManager.dealOrderKb
	public void dealOrderKb(){
		WmsWarehouse warehouse = getwareHouse();//新港仓库
		logger.error("-----------开始处理看板件料单中间表数据-----------");
		String orderKbSql = "select id,ODR_NO,ODR_TYPE,DEMAND_DATE,SUPPLY_NO,"
				+ "ITEM,QTY,FWARE,ODR_SU,DWARE,PRODUCT_LINE,SHDK,IS_JP,"
				+ "BATCH,STATION,SX,SMALL_QTY,PCS,FROM_SOURCE from "+MiddleTableName.W_ORDER_KB
				+" where status = 1  ORDER BY upper(ODR_NO) desc";
		createPickTicketByMiddleData(warehouse,orderKbSql,MiddleTableName.W_ORDER_KB);
		logger.error("-----------结束处理看板件料单中间表数据-----------");
	}
	
	public void dealPicketTicketDataFor() {
		while(true){
			dealPicketTicketData();
			try {
				Thread.sleep(1000*60*2);//2min
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
	//wmsDealInterfaceDataManager.dealPicketTicketData
	public void dealPicketTicketData() {
		logger.error("-----------开始处理临采件调整中间表数据-----------");
		WmsWarehouse warehouse = getwareHouse();//新港仓库
		//临采件调整出库发货单
		String erpSql = "select ID,ODR_NO,ODR_TYPE,DEMAND_DATE,SUPPLY_NO,"
							+ " ITEM,QTY,FWARE,PCS,FROM_SOURCE from "+MiddleTableName.W_ORDER_ERP
							+ " where status = 1 ORDER BY upper(ODR_NO) desc";
		createPickTicketByMiddleData(warehouse, erpSql, MiddleTableName.W_ORDER_ERP);
		logger.error("-----------结束处理临采件调整中间表数据-----------");
	}	
	
	void createPickTicketByMiddleData(WmsWarehouse warehouse,String sql,String tableName){
		
		List<String> errorLog = new ArrayList<String>();//记录每条错误日志
		List<String> lastError = new ArrayList<String>();//记录每批错误日志,只有当这批错误日志为空,才可以保存到数据库
		List<Long> ids = new ArrayList<Long>();//每批数据里成功的数据,这一批为为失败的一批
		List<Long> spsApplianceId = new ArrayList<Long>();//存储时序件器具关系表的ID
		
		List<String> odrNos = new ArrayList<String>();//记录送货单号,size一直等于1
		List<Object[]> batchData = new ArrayList<Object[]>();//记录每批数据,每个送货单号为一批
		Boolean batchFlag = Boolean.FALSE;//batchFlag=false=没检验,true代表每批的送货单号已经校验过了
		Long count = 0l;//count>0 代表送货单号已经存在
		
		List list = jdbcTemplateExt1.queryForList(sql);
		Iterator iter = list.iterator();
		
		String odrNo = null;//相关单号 1
		String odrType = null;//单据类型 2
		Date arriveDate = null;//要求到达日期 3
		String supplyNo = null;//供应商编码 4
		String itemCode = null;//货品编码 5
		Double qty = null;//数量 6
		String fware = null;//货主 7
		String odrSu = null;//计划需求公司8
		String dware = null;//目的仓库9
		String productLine = null;//生产线10
		String shdk = null;//收货道口11
		String isJpStr = null;//是否集配12
		String batch = null;//批次 13
		String station = null;//工位 14
		String fromSource = null;//来源 15
		while(iter.hasNext()){
			Map map = (Map) iter.next();
			Long id = ((BigDecimal) map.get("ID")).longValue();// 0 
			try{
				odrNo = map.get("ODR_NO").toString().toUpperCase();//相关单号 1
				odrType = map.get("ODR_TYPE").toString().toUpperCase();//单据类型 2
				arriveDate = (Date) map.get("DEMAND_DATE");//要求到达日期 3
				supplyNo = map.get("SUPPLY_NO").toString().toUpperCase();//供应商编码 4
				itemCode = map.get("ITEM").toString().toUpperCase();//货品编码 5
				qty = ((BigDecimal) map.get("QTY")).doubleValue();//数量 6
				fware = map.get("FWARE").toString().toUpperCase();//货主 7
				odrSu = getMayNullData(map.get("ODR_SU"));//计划需求公司8
				dware = getMayNullData(map.get("DWARE"));//目的仓库9
				productLine = getMayNullData(map.get("PRODUCT_LINE"));//生产线10
				shdk = getMayNullData(map.get("SHDK"));//收货道口11
				isJpStr = null;//是否集配12
				batch = getMayNullData(map.get("BATCH"));//批次 13
				station = getMayNullData(map.get("STATION"));//工位 14
				fromSource = getMayNullData(map.get("FROM_SOURCE"));//来源 15
				/**四个表的差异字段*/
				String pcs = null;
				String slr = null;
				Integer sx = 0;
				Double minQty = 0d;
				String packageNo = null;
				Double packageNum = 0d;
				Double packageQty = 0d;
				String remark = null;
				WmsPackageUnit packageUnit = null;//包装单位
				if(!tableName.equals(MiddleTableName.W_ORDER_KB)){
					slr = getMayNullData(map.get("SLR"));//备料工&备料工号 15
				}else{//看板件字段
					sx = map.get("SX") == null ? 0 : ((BigDecimal) map.get("SX")).intValue();//顺序 15
					minQty = map.get("SMALL_QTY") == null 
							? null : ((BigDecimal) map.get("SMALL_QTY")).doubleValue();//16
				}
				if(tableName.equals(MiddleTableName.W_ORDER_SPS)){//时序件料单
					sx = map.get("SX") == null ? 
							null : ((BigDecimal) map.get("SX")).intValue();//顺序 16
					packageNo = getMayNullData(map.get("PACKAGE_NO"));//器具型号 17
					packageNum = map.get("PACKAGE_NUM") == null 
									? null : ((BigDecimal) map.get("PACKAGE_NUM")).doubleValue();//器具容量18
					packageQty = map.get("PACKAGE_QTY") == null 
							? null : ((BigDecimal) map.get("PACKAGE_QTY")).doubleValue();//器具数量19
					remark = getMayNullData(map.get("REMARK"));//备注
				}
				
				if(!odrNos.contains(odrNo)){
					odrNos.add(odrNo);
				}
				
				//数据校验
				WmsOrganization company = validateOrg(errorLog, fware,"hz",Boolean.TRUE);//货主
				WmsItem item = validateItem(errorLog, itemCode,fware,Boolean.FALSE);//物料
				WmsOrganization supplier = validateOrg(errorLog, supplyNo,"gys",Boolean.FALSE);//供应商
				if(supplier==null){
					errorLog.add("供应商为空:"+odrNo+"supplyNo;");
//					throw new BusinessException("计划件-供应商为空:"+odrNo+"-"+supplyNo);
				}
				WmsBillType type = validateBillType(errorLog, odrType,fware);//单据类型
				
				if(tableName.equals(MiddleTableName.W_ORDER_ERP)){//临采件
					if(odrNos.size() > 1){//ERP
						if(lastError.size() == 0 ){
							saveErpData(lastError, warehouse, tableName, batchData);
						}else if(ids.size()>0 && lastError.size() > 0){//更新失败批里面的部分成功数据状态
							updateMiddleStatusByBatch(tableName, ids, Boolean.TRUE);
						}
						/**清空存储批数据的list*/
						lastError.clear();
						batchData.clear();
						odrNos.clear();
						ids.clear();
						batchFlag = Boolean.FALSE;//重置flag
						count = 0l;//清空count
						odrNos.add(odrNo);
						spsApplianceId.clear();
					}
					
					if(!batchFlag){//batchFlag=true 避免重复校验  需要batchFlag重置后再校验
						count = isExistRelateBill(odrNo,Boolean.FALSE,errorLog);//判断送货单号是否存在
						batchFlag = Boolean.TRUE;
					}
					if(null != count && count > 0){
						errorLog.add("送货单号:"+odrNo+"已经存在,请检查!!\n");
					}
					
					if(errorLog.size() == 0){
						batchData.add(new Object[]{id,odrNo,type,//保存通过校验的数据
								arriveDate,supplier,item,qty,company,packageUnit});
						ids.add(id);
					}else{//记录错误信息,将每条的错误信息加到每批错误信息里,清空每条错误信息
						updateMiddleStatus(tableName, errorLog, id);
						lastError.add(errorLog.toString());
						errorLog.clear();
					}
					continue;
				}else{
					if(map.get("IS_JP") != null){//是否集配Y/N
						isJpStr = map.get("IS_JP").toString().toUpperCase();
						validateYesOrNo(errorLog,isJpStr,"是否集配只能填Y/N!!\n");//校验是否集配值是否是Y/N
					}else{
						isJpStr = "N";
					}
				}
				//可以优化,使用键值对存储
				List saIds = null;
				if(tableName.equals(MiddleTableName.W_ORDER_SPS)){//时序件料单
					saIds = validateOdrNo(errorLog,odrNo);//校验送货单号在时序件器具明细表里有没有
				}
				//yc.min
				pcs = map.get("PCS") == null ? null : map.get("PCS").toString();
				packageUnit = validatePackageUnit(errorLog,item,pcs);//包装单位
				if(odrNos.size() > 1){
					if(lastError.size() == 0){
						savePickTicketData(lastError, warehouse, tableName, batchData,spsApplianceId);//保存数据
					}else if(ids.size() > 0 && lastError.size() > 0){//更新失败批里面的部分成功数据状态
						updateMiddleStatusByBatch(tableName,ids, Boolean.TRUE);
					}
					/**清空存储批数据的list*/
					lastError.clear();//存储每批错误信息
					batchData.clear();//存储每批正确数据
					ids.clear();//存储每批正确数据ID
					odrNos.clear();//存储每批的送货单号
					batchFlag = Boolean.FALSE;//重置每批送货单号是否校验标识符
					count = 0l;//清空count
					odrNos.add(odrNo);
					spsApplianceId.clear();
				}
				
				if(!batchFlag){//batchFlag=true 避免重复校验  需要batchFlag重置后再校验
					count = isExistRelateBill(odrNo,Boolean.FALSE,errorLog);//判断送货单号是否存在
					batchFlag = Boolean.TRUE;
				}
				if(null != count && count > 0){
					errorLog.add("送货单号:"+odrNo+"已经存在,请检查!!\n");
				}
				
				/**数据校验通过,保存到临时每批list*/
				if(errorLog.size() == 0){
					if(null != saIds && saIds.size() > 0){
						Iterator idIter = saIds.iterator();
						while(idIter.hasNext()){
							Map m = (Map) idIter.next();
							Long saId = ((BigDecimal) m.get("ID")).longValue();
							if(!spsApplianceId.contains(saId)){
								spsApplianceId.add(saId);//如果这批数据通过校验,则将时序件器具关系表对应MES单号的数据存储到WMS
							}
						}
					}
					
					Object[] objArray = null;
					if(tableName.equals(MiddleTableName.W_ORDER_KB)){//看板件数据
						objArray = new Object[]{id,odrNo,type,arriveDate,supplier,//保存通过校验的数据
								item,qty,company,odrSu,dware,productLine,shdk,isJpStr,
								batch,station,sx,minQty,packageUnit};
					}else if(tableName.equals(MiddleTableName.W_ORDER_JH)){//计划件
						objArray = new Object[]{id,odrNo,type,arriveDate,supplier,
								item,qty,company,odrSu,dware,productLine,shdk,isJpStr,
								batch,station,slr,fromSource,packageUnit};
					}else{//时序件数据
						objArray = new Object[]{id,odrNo,type,arriveDate,supplier,
								item,qty,company,odrSu,dware,productLine,shdk,isJpStr,
								batch,station,slr,sx,packageNo,packageNum,packageQty,remark,fromSource,packageUnit};
					}
					batchData.add(objArray);
					ids.add(id);
				}else{//记录错误信息,将每条的错误信息加到每批错误信息里,清空每条错误信息
					updateMiddleStatus(tableName,errorLog, id);
					lastError.add(errorLog.toString());
					errorLog.clear();
				}
			}catch(Exception e){
				e.printStackTrace();
				errorLog.add(e.getMessage());//保存异常信息
				updateMiddleStatus(tableName, errorLog, id);//捕捉到异常后存入数据错误信息子弹
				lastError.add(errorLog.toString());
				errorLog.clear();
				continue;
			}
		}
		/**处理最后一批数据,上面逻辑是只有到了下一批才会去处理上一批的数据,这样会导致最后一批的数据搁置*/
		
		if(lastError.size() == 0 && batchData.size() > 0){
			if(tableName.equals(MiddleTableName.W_ORDER_ERP)){//临采件
				saveErpData(lastError, warehouse, tableName, batchData);
			}else{
				savePickTicketData(lastError, warehouse, tableName, batchData,spsApplianceId);//保存数据
			}	
		}
		
		if(ids.size() > 0 && lastError.size() > 0){//更新失败批里面的部分成功数据状态
			updateMiddleStatusByBatch(tableName,ids, Boolean.TRUE);
			ids.clear();
		}
		
		lastError.clear();//清空每批错误信息
		batchData.clear();
		ids.clear();
		odrNos.clear();
	}
	
	/**
	 * 
	 * @param odrNo
	 * @param isAsn = true 收货单,FALSE = 发货单
	 * @return
	 */
	Long isExistRelateBill(String odrNo,Boolean isAsn,List<String> errorLog){
		String hql = "";
		if(isAsn){
			hql = "select count(*) from WmsASN where relatedBill1 = :relatedBill1";
		}else{
			hql = "select count(*) from WmsPickTicket where relatedBill1 = :relatedBill1";
		}
		Long count = (Long) commonDao.findByQueryUniqueResult(hql, "relatedBill1", odrNo);
		return count;
	}
	
	void savePickTicketData(List<String> errorLog,WmsWarehouse warehouse, 
								String tableName,List<Object[]> batchData,List<Long> appId){
		/**看板件先保存到wms临时表,然后交给task处理*/
		if(tableName.equals(MiddleTableName.W_ORDER_KB)){
			savePickDataToWms(batchData,errorLog,tableName,HeadType.PICK_KB);
		}else if(tableName.equals(MiddleTableName.W_ORDER_JH)){
			savePickDataToWms(batchData,errorLog,tableName,HeadType.PICK_JH);
		}else if(tableName.equals(MiddleTableName.W_ORDER_SPS)){
			savePickDataToWms(batchData,errorLog,tableName,HeadType.PICK_SPS);
			/**保存时序件数据*/
			saveSpsApplianceData(appId);
		}
	}
	
	/**看板件数据先记录到WMS临时表,最后由task处理生成发货单*/
	void savePickDataToWms(List<Object[]> batchData,List<String> errorLog,
			String tableName,String headType){
		Date date = new Date();
		List<Long> ids = new ArrayList<Long>();//记录中间表数据ID,更新数据状态
		
		WHead head = new WHead(1, date, headType);
		commonDao.store(head);
		String taskType = "";
		for(Object[] obj : batchData){
			
			String odrNo = obj[1].toString().toUpperCase();//相关单号
			String odrSu = (String) obj[8];//计划需求公司
			String dware = (String) obj[9];//目的仓库
			String productLine = (String) obj[10];//生产线
			String shdk = (String) obj[11];//收货道口
			String isJpStr = obj[12].toString().toUpperCase();//是否集配Y/N
			String batch = (String) obj[13];//批次
			String station = (String) obj[14];//工位
			Date arriveDate = (Date) obj[3];//要求到达日期
			Double qty = (Double) obj[6];//数量
			Long id = (Long) obj[0];
			Boolean isjp = validateYesOrNo(errorLog,isJpStr,"");
			
			WmsOrganization company = (WmsOrganization) obj[7];//货主
			WmsItem item = (WmsItem) obj[5];//物料
			WmsOrganization supplier = (WmsOrganization) obj[4];//供应商
//			if(supplier==null){
//				throw new BusinessException("时序件-供应商为空:"+odrNo);
//			}
			WmsBillType type = (WmsBillType) obj[2];//单据类型
			
			WmsPackageUnit packageUnit = validatePackageUnit(errorLog, item, null);//包装以wms货品关联的为准
			if(packageUnit==null){
				throw new BusinessException("时序件/包装为空:"+item.getCode());
			}
			if(tableName.equals(MiddleTableName.W_ORDER_KB)){//看板件
				Double minQty = (Double) obj[16];
				Integer sx = (Integer) obj[15];
				MiddleOrderKb mok = new MiddleOrderKb(odrNo, type, 0, arriveDate, supplier,
						 item, minQty.intValue(), packageUnit.getUnit(), qty, odrSu, company, dware, productLine, 
						shdk, isjp, batch, station, head,packageUnit,sx);
				commonDao.store(mok);
				taskType = "wmsDealTaskManager"+MyUtils.spiltDot+"dealKbPickData";
			}else if(tableName.equals(MiddleTableName.W_ORDER_JH)){//计划件
				String slr = (String) obj[15];
				String fromSource = (String) obj[16];
				MiddleOrderJh jh = new MiddleOrderJh(odrNo, type, arriveDate, supplier, item, qty, 
						company, odrSu, dware, productLine, shdk, isjp, batch, station, slr, packageUnit,head);
				jh.setFromSource(fromSource);
				commonDao.store(jh);
				taskType = "wmsDealTaskManager"+MyUtils.spiltDot+"dealJhPickData";
			}else if(tableName.equals(MiddleTableName.W_ORDER_SPS)){
				String slr = (String) obj[15];
				Integer sx = (Integer) obj[16] ;
				String packageNo = (String) obj[17];//器具型号
				Double packageNum = obj[18] == null ? 0d : (Double) obj[18];//器具容量
				Double packageQty = obj[18] == null ? 0d : (Double) obj[19];;//器具数量
				String remark = (String) obj[20];//备注
				String fromSource = (String) obj[21];
				
				MiddleOrderSps sps = new MiddleOrderSps(odrNo, type, arriveDate, supplier, item, 
								qty, company, odrSu, dware, productLine, shdk, isjp, batch, station, slr, 
								head, packageUnit, sx, packageNo, packageNum, packageQty,remark);
				sps.setFromSource(fromSource);
				commonDao.store(sps);
				taskType = "wmsDealTaskManager"+MyUtils.spiltDot+"dealSpsPickData";
			}
			
			ids.add(id);
		}
		Task task = new Task(headType,taskType, head.getId());
		commonDao.store(task);
		updateMiddleStatusByBatch(tableName,ids,Boolean.FALSE);//更新中间表状态
	}
	
	//存储器具明细表
	void saveSpsApplianceData(List<Long> ids) {
		if(ids.size() > 0){
			int PAGE_NUMBER = 500;
			int size = ids.size();
			int j = JavaTools.getSize(size, PAGE_NUMBER);
			for(int k=0;k<j;k++){
				int toIndex = JavaTools.getIndex(k, size, PAGE_NUMBER);
				List<Long> ret = JavaTools.getListLong(ids, k, toIndex, PAGE_NUMBER);
				
				String idStr = "";
				for(Long id : ret){
					idStr += id+",";
				}
				idStr = idStr.substring(0, idStr.length()-1);
				String sql = "SELECT ID,ODR_NO,APPLIANCE_NO,DEMAND_DATE,FWARE,"
						+ "DWARE,SUPPLY_NO,SUPPLY_NAME,APPLIANCE_TYPE,APPLIANCE_NAME,"
						+ "APPLIANCE_AMOUNT,ITEM_CODE,ITEM_NAME,ORDER_QTY,QTY,SEQ,END_SEQ,IS_JP,"
						+ "PRODUCT_LINE,SHDK,CUR_PAGE,TOTAL_PAGE,STATION,REMARK,SX FROM "
						+ MiddleTableName.W_SPS_APPLIANCE+" WHERE 1=1" 
						+ " AND status = 1"
						+ " AND id in("+idStr+")";
				List list = jdbcTemplateExt1.queryForList(sql);
				Iterator iter = list.iterator();
				while(iter.hasNext()){
					Map map = (Map) iter.next();
					Long id = ((BigDecimal) map.get("ID")).longValue();
					String odrNo = (String) map.get("ODR_NO");
					String applianceNo = (String) map.get("APPLIANCE_NO");
					Date demandDate = (Date) map.get("DEMAND_DATE");
					String fware = (String) map.get("FWARE");
					String dware = (String) map.get("DWARE");
					String supplyNo = (String) map.get("SUPPLY_NO");
					String supplyName = (String) map.get("SUPPLY_NAME");
					String applianceType = (String) map.get("APPLIANCE_TYPE");
					String applianceName = (String) map.get("APPLIANCE_NAME");
					Double applianceAmount = map.get("APPLIANCE_AMOUNT") == null ? 0D : 
									((BigDecimal) map.get("APPLIANCE_AMOUNT")).doubleValue();
					String itemCode = (String) map.get("ITEM_CODE");
					String itemName = (String) map.get("ITEM_NAME");
					Double orderQty = map.get("ORDER_QTY") == null ? 0D : 
									((BigDecimal) map.get("ORDER_QTY")).doubleValue();
					Double qty = map.get("QTY") == null ? 
										0d : ((BigDecimal) map.get("QTY")).doubleValue();
					Double seq = map.get("SEQ") == null ? 0d : 
									((BigDecimal) map.get("SEQ")).doubleValue();
					Double endseq = map.get("END_SEQ") == null ? 0d : 
						((BigDecimal) map.get("END_SEQ")).doubleValue();
					String isJpStr = (String) map.get("IS_JP");
					String productLine = (String) map.get("PRODUCT_LINE");
					String shdk = (String) map.get("SHDK");
					Integer curPage = map.get("CUR_PAGE") == null 
										? 0 : ((BigDecimal)map.get("CUR_PAGE")).intValue();
					Integer totalPage = map.get("TOTAL_PAGE") == null 
										? 0 : ((BigDecimal) map.get("TOTAL_PAGE")).intValue();
					String station = (String) map.get("STATION");
					String remark = (String) map.get("REMARK");
					Integer sx = map.get("SX") == null ? 0 : ((BigDecimal)map.get("SX")).intValue();
					
					Boolean isJp = Boolean.FALSE;
					if(null != isJpStr){
						isJp = isJpStr.equals("Y") ? Boolean.TRUE : Boolean.FALSE;
					}
					
					WmsPickTicketAndAppliance wp = new WmsPickTicketAndAppliance(odrNo,
							applianceNo, demandDate, fware, dware, supplyNo, supplyName, applianceType,
							applianceName, applianceAmount, itemCode, itemName, orderQty, qty, seq,endseq,isJp, 
							productLine, shdk, curPage, totalPage, station, remark,sx);
					commonDao.store(wp);
					
				}
			}
			updateMiddleStatusByBatch(MiddleTableName.W_SPS_APPLIANCE, ids, Boolean.FALSE);
			return;
		}
	}
	List validateOdrNo(List<String> errorLog,String odrNo){
		
		String sql = "select id from "+MiddleTableName.W_SPS_APPLIANCE+" where ODR_NO = '"+odrNo+"'";
		List id = jdbcTemplateExt1.queryForList(sql);
		if(id == null){
			errorLog.add("根据送货单号("+odrNo+")没有在时序件器具明细表里找到对应数据,请检查!!\n");
		}
		return id;
	}
	/**
	 * 更新数据状态
	 * @param tableName
	 * @param errorLog
	 * @param id
	 */
	String batchUpdateMiddleStatus(String tableName,List<String> errorLog,Long id){
		String sql = "";
		String date = DateUtil.formatDateToStr(new Date());//更新状态时候加上时间
		if(errorLog.size() > 0){
			errorLog.add(date);
			sql = "update "+tableName+" set status=0,"
					+ "EXCEPTION_MESS='"+keepLogLength(errorLog)+"' where id="+id;
		}else{
			sql = "update "+tableName+" set status=3,"
					+ "EXCEPTION_MESS='' where id="+id;
		}
		return sql;
	}
	
	void updateMiddleStatus(String tableName,List<String> errorLog,Long id){
		String sql = "";
		String date = DateUtil.formatDateToStr(new Date());//更新状态时候加上时间
		if(errorLog.size() == 0){
			sql = "update "+tableName+" set status=3,"
					+ "EXCEPTION_MESS='成功时间："+date+"' where id ="+id;
		}else{
			errorLog.add("失败时间："+date);
			sql = "update "+tableName+" set status=0,"
					+ "EXCEPTION_MESS='"+keepLogLength(errorLog)+"' where id="+id;
		}
		jdbcTemplateExt1.update(sql);
	}
	void updateMiddleStatus(String tableName,List<String> errorLog,List<Long> ids){
		if(ids==null || ids.size()<=0){
			return;
		}
		String sql = "";
		String date = DateUtil.formatDateToStr(new Date());//更新状态时候加上时间
		if(errorLog.size() == 0){
			sql = "update "+tableName+" set status=3,"
					+ "EXCEPTION_MESS='成功时间："+date+"' where id in ("+StringUtils.substringBetween(ids.toString(), "[", "]")+")";
		}else{
			errorLog.add("失败时间："+date);
			sql = "update "+tableName+" set status=0,"
					+ "EXCEPTION_MESS='"+keepLogLength(errorLog)+"' where id in ("+StringUtils.substringBetween(ids.toString(), "[", "]")+")";
		}
		jdbcTemplateExt1.update(sql);
	}
	
	/**
	 * 
	 * @param tableName
	 * @param errorLog
	 * @param ids
	 * @param isBatchFail = true 
	 * 	那么单条数据没问题,但是这个送货单号里有其它数据是失败的
	 */
	void updateMiddleStatusByBatch(String tableName,List<Long> ids,Boolean isBatchFail){
		String date = DateUtil.formatDateToStr(new Date());//更新状态时候加上时间
		String[] sqlArray = new String[ids.size()];//存sql
		
		if(!isBatchFail){
			for(int i = 0; i < ids.size(); i++){
				String sql = "update "+tableName+" set status=3,"
						+ "EXCEPTION_MESS='成功时间："+date+"' where id = "+ids.get(i);
				sqlArray[i] = sql;
			}
		}else{
			for(int i = 0; i < ids.size(); i++){
				String sql = "update "+tableName+" set status=0,"
						+ "EXCEPTION_MESS='批次失败,处理结束时间："+date+"' where id = "+ids.get(i);
				sqlArray[i] = sql;
			}
		}
		
		jdbcTemplateExt1.batchUpdate(sqlArray);
	}
	
	Boolean validateYesOrNo(List<String> errorLog,String value,String msg){
		if(!value.equals("Y") && !value.equals("N")){
			errorLog.add(msg);
			return Boolean.FALSE;
		}else if(value.equals("Y")){
			return Boolean.TRUE;
		}else{
			return Boolean.FALSE;
		}
	}
	
	//保存MES看板件料单
	void saveErpData(List<String> errorLog,WmsWarehouse warehouse,
								String tableName,List<Object[]> objs){
		Integer lineNo = 10;
		int i = 1;
		WmsPickTicket pickTicket = null;
		List<Long> ids = new ArrayList<Long>();
		Double totalQty = 0D;//发货单总数量
		for(Object[] obj : objs){
			
			WmsOrganization company = (WmsOrganization) obj[7];//货主
			WmsItem item = (WmsItem) obj[5];//物料
			WmsOrganization supplier = (WmsOrganization) obj[4];//供应商
			WmsBillType type = (WmsBillType) obj[2];//单据类型
			WmsPackageUnit packageUnit = (WmsPackageUnit) obj[8];//包装单位
			
			Long id = ((BigDecimal) obj[0]).longValue();
			String odrNo = obj[1].toString().toUpperCase();//相关单号
			Date arriveDate = (Date) obj[3];//要求到达日期
			Double qty = ((BigDecimal) obj[6]).doubleValue();//数量
			
			String code = getCodeByRule(errorLog, company, type, warehouse, "发货单") ;//获取单据编码
			
			if(i == 1){
				pickTicket = new WmsPickTicket(warehouse, company, type, code, odrNo, 
						WmsPickTicketStatus.OPEN, qty, 0d, 0d, 0d, WmsSource.INTERFACE,arriveDate);
				commonDao.store(pickTicket);
			}
			
			ShipLotInfo shipLotInfo = new ShipLotInfo
					(supplier == null ? null : supplier.getCode(),
							supplier == null ? null : supplier.getName());
			
			WmsPickTicketDetail detail = new WmsPickTicketDetail(pickTicket, lineNo * i, item, shipLotInfo, 
														packageUnit, qty, qty, 0d, 0d, 0d,supplier,"-");
			commonDao.store(detail);
			i += 1;
			ids.add(id);
			totalQty += qty;
		}
		//更新状态
		updateMiddleStatusByBatch(tableName,ids,Boolean.FALSE);
		updateTotalQty(pickTicket, null, totalQty);//更新单头数量
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
	
	/**
	 * 获取单据编码,有异常(例如数据未在规则表维护)则保存错误信息
	 * @param errorLog
	 * @param company
	 * @param billType
	 * @param warehouse
	 * @param type
	 * @return
	 */
	String getCodeByRule(List<String> errorLog,WmsOrganization company,
							WmsBillType billType,WmsWarehouse warehouse,String type){
		if(null == warehouse || null == billType || null == billType){
			return null;
		}
		String code = "";
		if(null != company && null != billType){
			try{
				code = codeManager.generateCodeByRule(warehouse, company.getName(),type, billType.getName());
			}catch(Exception e){
				errorLog.add(e.getMessage());
			}
		}
		return code;
	}
	public void dealStationAndItemDataFor() {
		while(true){
			dealStationAndItemData();
			try {
				Thread.sleep(1000*60*2);//2min
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
	//wmsDealInterfaceDataManager.dealStationAndItemData
	@SuppressWarnings("unchecked")
	public void dealStationAndItemData() {
		System.out.println("-----------开始处理器具物料对应关系表数据-----------");
		/**器具物料对应关系*/
		String sql = "select ID,APPLIANCE_TYPE,ITEMCODE,FULLAMOUNT,"
							+ " SIZES,APPLIANCE_NAME from "+MiddleTableName.W_APPLIANCE_ITEM_MES
							+ " where STATUS = 1 AND ROWNUM < 296";
		List list = jdbcTemplateExt1.queryForList(sql);
		Iterator iter = list.iterator();
		List<String> errorLog = new ArrayList<String>();//记录错误信息
		Set<String> typs = new HashSet<String>();
		while(iter.hasNext()){
			Long id = null;//
			String type = null;//器具型号
			String itemCode = null;//货品编码
			Integer loadage = null;//装载量
			String size = null;//尺寸
			String applianceName = null;//器具名称
			try {
				errorLog.clear();
				Map m = (Map) iter.next();
				id = ((BigDecimal)m.get("ID")).longValue();;//
				type = (String) m.get("APPLIANCE_TYPE");//器具型号
				itemCode = (String) m.get("ITEMCODE");//货品编码
				loadage = m.get("FULLAMOUNT")==null?0:((BigDecimal)m.get("FULLAMOUNT")).intValue();//装载量
				size = (String) m.get("SIZES");//尺寸
				applianceName = (String) m.get("APPLIANCE_NAME");//器具名称
				WmsItem item = validateItem(itemCode);
				if(item==null){
					errorLog.add("根据货品编码："+itemCode+"未找到对应的货品;");
					sql = batchUpdateMiddleStatus(MiddleTableName.W_APPLIANCE_ITEM_MES, errorLog, id);
					jdbcTemplateExt1.execute(sql);
					continue;
				}
				/**根据物料和器具型号去找物料器具关系表数据，找到则更新，没找到则新增*/
				WmsStationAndItem sai = getApplianceByItemAndType(item,type);
				if(null == sai){
					sai = new WmsStationAndItem(type, item, loadage, size,applianceName);
					commonDao.store(sai);
					if(!typs.contains(type)){
						String hql = "SELECT e.id FROM WmsEnumType e WHERE e.enumValue =:enumValue";
						List<Long> eids = commonDao.findByQuery(hql, "enumValue", type);
						if(eids==null || eids.size()<=0){
							WmsEnumType enumType = new WmsEnumType("xx", type,applianceName);
							commonDao.store(enumType);
						}
						typs.add(type);
					}
				}else{
					sai.setSize(size);
					sai.setLoadage(loadage);
					commonDao.store(sai);
				}
				
				sql = batchUpdateMiddleStatus(MiddleTableName.W_APPLIANCE_ITEM_MES, errorLog, id);
				jdbcTemplateExt1.execute(sql);
			} catch (Exception e) {
				errorLog.add(e.getMessage());
				sql = batchUpdateMiddleStatus(MiddleTableName.W_APPLIANCE_ITEM_MES, errorLog, id);
				jdbcTemplateExt1.execute(sql);
			}
		}
		System.out.println("-----------结束处理器具物料对应关系表数据--------"+list.size());
	}
	
	/**根据货品和器具型号去找器具物料对应关系表数据*/
	WmsStationAndItem getApplianceByItemAndType(WmsItem item,String type){
		String hql = "from WmsStationAndItem w where w.item.id=:itemId and w.type=:type";
		
		WmsStationAndItem sai = (WmsStationAndItem) commonDao.findByQueryUniqueResult(hql, 
									new String[]{"itemId","type"}, new Object[]{item.getId(),type});
		return sai;
	}
	
	@SuppressWarnings("unchecked")
	WmsItem validateItem(String itemCode){
		String hql = "from WmsItem item where item.code=:itemCode and status='ENABLED'";
		List<WmsItem> items = commonDao.findByQuery(hql, "itemCode", itemCode);
		if(items==null || items.size()<=0){
//			errorLog.add("根据货品编码："+itemCode+"未找到对应的货品;");
			return null;
		}else{
			if(items.size()>1){//如果找到两个的话默认寄存
				for(WmsItem i : items){
					WmsOrganization company = commonDao.load(WmsOrganization.class,i.getCompany().getId());
					if(company.getCode().equals("XG100")){
						return i;
					}
				}
			}else{
				return items.get(0);
			}
		}
		return null;
	}
	public void dealSupplierAndItemDataFor() {
		while(true){
			dealSupplierAndItemData();
			try {
				Thread.sleep(1000*60*60);//1h
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
	//wmsDealInterfaceDataManager.dealSupplierAndItemData
	public void dealSupplierAndItemData() {
		System.out.println("----开始处理供应商物料关系对应表数据:"+JavaTools.format(new Date(), JavaTools.hms));
		String sql = "SELECT ID FROM "+MiddleTableName.M_MESSAGE+
				" WHERE TYPE = '"+EdiTaskType.MIDDLE_SUPPLY_ITEM_ERP+"' AND STATUS = 1";
		List list = jdbcTemplateExt1.queryForList(sql);
		if(list!=null && list.size()>0){
			/***********(M1)**********************/
			dealSupplierAndItemDataM1();
			/***********(M2)**********************/
			dealSupplierAndItemDataM2();
			/***********(M3)**********************/
			dealSupplierAndItemDataM3();
			
			Iterator iter = list.iterator();
			while(iter.hasNext()){
				Map m = (Map) iter.next();
				Long id = ((BigDecimal)m.get("ID")).longValue();
				sql = "UPDATE "+MiddleTableName.M_MESSAGE+" SET STATUS = 3,READ_TIME = systimestamp WHERE ID = "+id;
				jdbcTemplateExt1.execute(sql);
			}
		}
		System.out.println("----结束处理供应商物料关系对应表数据:"+JavaTools.format(new Date(), JavaTools.hms)+"-"+list.size());
	}
	/**将接口表信息初始化到A表*/
	private void dealSupplierAndItemDataM1(){
		String sql = "SELECT ID,COMPANY,ITEMCODE,INV_LOC FROM "+MiddleTableName.W_SUPPLY_ITEM_ERP+
				" WHERE STATUS = 1 AND COMPANY is not null AND ITEMCODE is not null AND INV_LOC is not null";
		
		List l1 = jdbcTemplateExt1.queryForList(sql);
		if(l1!=null && l1.size()>0){
			int size = l1.size();
			int j = JavaTools.getSize(size, PAGE_NUMBER);
			for(int k = 0 ; k < j ; k++){
				System.out.println(j+"."+(k+1));
				int index = JavaTools.getIndex(k, size, PAGE_NUMBER);
				List objs = JavaTools.getLists(l1, k, index, PAGE_NUMBER);
				//@1:写入MiddleSupplyItemErpA
				Iterator iter = objs.iterator();
				while(iter.hasNext()){
					Map m = (Map) iter.next();
					Long id = ((BigDecimal)m.get("ID")).longValue();
					String company = (String) m.get("COMPANY");//仓库
					String itemCode = (String) m.get("ITEMCODE");//物料
					String invLoc = (String) m.get("INV_LOC");//货主
					MiddleSupplyItemErpA a = new MiddleSupplyItemErpA(company, itemCode, invLoc, "1");
					commonDao.store(a);
					//@2:回写状态3
					sql = "update  "+MiddleTableName.W_SUPPLY_ITEM_ERP+" set STATUS = 3 where id = "+id;
					jdbcTemplateExt1.execute(sql);
				}
				try {
					Thread.sleep(1000*2);//2'
				} catch (InterruptedException e) {
					logger.error("", e);
				}
			}
		}
	}
	/**A与B对比,获取仓库+物料对应货主不同的数据,并更新相应的物料接口表状态=1,*/
	private void dealSupplierAndItemDataM2(){
		String sql = "SELECT a.id as ID,a.company as COMPANY,a.item_code as ITEMCODE,a.inv_loc as INV_LOC" +
				" FROM "+WmsTables.MIDDLESUPPLYITEMERPA+" a WHERE a.status = 1";
		List l2 = jdbcTemplate.queryForList(sql);
		if(l2!=null && l2.size()>0){
			String key = "";
			int size = l2.size();
			int j = JavaTools.getSize(size, PAGE_NUMBER);
			for(int k = 0 ; k < j ; k++){
				System.out.println(j+".."+(k+1));
				int index = JavaTools.getIndex(k, size, PAGE_NUMBER);
				List objs = JavaTools.getLists(l2, k, index, PAGE_NUMBER);
				
				List<String> upItem = null;
				Map<String,List<String>> upComp = new HashMap<String, List<String>>();//找B的集合
				List<Long> backids = new ArrayList<Long>();
				Map<String,String> midds = new HashMap<String, String>();
				
				Iterator iter = objs.iterator();
				while(iter.hasNext()){
					Map m = (Map) iter.next();
					Long id = ((BigDecimal)m.get("ID")).longValue();
					String company = (String) m.get("COMPANY");//仓库
					String itemCode = (String) m.get("ITEMCODE");//物料
					String invLoc = (String) m.get("INV_LOC");//货主
					//为了找B的map
					if(upComp.containsKey(company)){
						upItem = upComp.get(company);
					}else{
						upItem = new ArrayList<String>();
					}
					upItem.add("'"+itemCode+"'");
					upComp.put(company, upItem);
					//为了和B匹配的map
					key = company+MyUtils.spilt1+itemCode;
					midds.put(key, invLoc);
					//为了回写A的list
					backids.add(id);
				}
				//找B(根据仓库+物料)
				if(upComp.size()>0){
					Iterator<Entry<String, List<String>>> i = upComp.entrySet().iterator();
					while(i.hasNext()){
						Entry<String, List<String>> entry = i.next();
						sql = "SELECT b.id as ID,b.company as COMPANY,b.item_code as ITEMCODE,b.inv_loc as INV_LOC" +
								" FROM "+WmsTables.MIDDLESUPPLYITEMERPB+" b WHERE b.company = '"+entry.getKey()+"' AND b.item_code IN("+
								StringUtils.substringBetween(entry.getValue().toString(), "[", "]")+")";
						List lb = jdbcTemplate.queryForList(sql);
						if(lb!=null && lb.size()>0){
							Map<String,List<String>> upErpItem = new HashMap<String, List<String>>();//和B匹配差异的map
							Iterator ib = lb.iterator();
							while(ib.hasNext()){
								Map m = (Map) ib.next();
								Long id = ((BigDecimal)m.get("ID")).longValue();
								String company = (String) m.get("COMPANY");//仓库
								String itemCode = (String) m.get("ITEMCODE");//物料
								String invLoc = (String) m.get("INV_LOC");//货主
								//@1:MiddleSupplyItemErpA与MiddleSupplyItemErpB匹配
								key = company+MyUtils.spilt1+itemCode;
								if(midds.containsKey(key)){
									if(!midds.get(key).equals(invLoc)){
										//记录差异key
										if(upErpItem.containsKey(company)){
											upItem = upErpItem.get(company);
										}else{
											upItem = new ArrayList<String>();
										}
										upItem.add("'"+itemCode+"'");
										upErpItem.put(company, upItem);
									}
								}
								midds.remove(key);
							}
							//@2:A和B差异物料信息状态变为1(仓库+物料对应的货主不同)
							if(upErpItem.size()>0){
								Iterator<Entry<String, List<String>>> ii = upErpItem.entrySet().iterator();
								while(ii.hasNext()){
									Entry<String, List<String>> entryErp = ii.next();
									sql = "UPDATE "+MiddleTableName.W_ITEM_ERP+" w SET w.status = 1,w.exception_mess ='货主更新',w.deal_time = systimestamp" +
											" WHERE w.company = '"+entryErp.getKey()+"'" +
											" and w.code IN ("+StringUtils.substringBetween(entryErp.getValue().toString(), "[", "]")+")";
									jdbcTemplateExt1.execute(sql);
								}
							}
						}
					}
				}
				//@3:回写A状态=3
				if(backids!=null && backids.size()>0){
					sql = "UPDATE "+WmsTables.MIDDLESUPPLYITEMERPA+" a SET a.status = 3 WHERE a.id IN ("+
							StringUtils.substringBetween(backids.toString(), "[", "]")+")";
					jdbcTemplate.execute(sql);
				}
			}
			try {
				Thread.sleep(1000*2);//2'
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
	/**B表信息清空,A表信息转移给B,A表信息清空*/
	private void dealSupplierAndItemDataM3(){
		//辞旧迎新
		String sql = "truncate table "+WmsTables.MIDDLESUPPLYITEMERPB;
		jdbcTemplate.execute(sql);
		
		sql = "SELECT a.id as ID,a.company as COMPANY,a.item_code as ITEMCODE,a.inv_loc as INV_LOC" +
				" FROM "+WmsTables.MIDDLESUPPLYITEMERPA+" a WHERE a.status = 3";
		List l3 = jdbcTemplate.queryForList(sql);
		if(l3!=null && l3.size()>0){
			int size = l3.size();
			int j = JavaTools.getSize(size, PAGE_NUMBER);
			for(int k = 0 ; k < j ; k++){
				System.out.println(j+"..."+(k+1));
				int index = JavaTools.getIndex(k, size, PAGE_NUMBER);
				List objs = JavaTools.getLists(l3, k, index, PAGE_NUMBER);
				
				Iterator itern = objs.iterator();
				while(itern.hasNext()){
					Map m = (Map) itern.next();
					Long id = ((BigDecimal)m.get("ID")).longValue();
					String company = (String) m.get("COMPANY");//仓库
					String itemCode = (String) m.get("ITEMCODE");//物料
					String invLoc = (String) m.get("INV_LOC");//货主
					MiddleSupplyItemErpB b = new MiddleSupplyItemErpB(company, itemCode, invLoc, "1");
					commonDao.store(b);
				}
				
				try {
					Thread.sleep(1000*2);//2'
				} catch (InterruptedException e) {
					logger.error("", e);
				}
			}
			l3.clear();
		}
		
		sql = "truncate table "+WmsTables.MIDDLESUPPLYITEMERPA;
		jdbcTemplate.execute(sql);
	}
	public void dealErpItemDataFor() {
		while(true){
			dealErpItemData();
			try {
				Thread.sleep(1000*60*2);//2min
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
	//edit yc.min  wmsDealInterfaceDataManager.dealErpItemData
	public void dealErpItemData() {
		List<String> errorLog = new ArrayList<String>();//记录错误信息
		Boolean isAutoError = false;
		
		System.out.println("-----------开始处理物料关系中间表数据--------");
		String hql = "FROM WmsLotRule WHERE code='默认批次规则' AND status='ENABLED'";
		WmsLotRule lotRule = (WmsLotRule) commonDao.findByQueryUniqueResult(hql, "", "");
		if(null == lotRule){
			errorLog.add("WMS没有找到[默认批次规则];");
			isAutoError = true;
		}
		
		Map<String,WmsOrganization> companys = new HashMap<String, WmsOrganization>();//如果存在了的货主,不要再次查询数据
		List<WmsOrganization> company = new ArrayList<WmsOrganization>();
		/**物料基础信息数据*/
		String sql = "SELECT ID,COMPANY,CODE,NAME,UNIT "
				+ "FROM "+MiddleTableName.W_ITEM_ERP+" WHERE STATUS = 1 AND ROWNUM < 296";
		List list = jdbcTemplateExt1.queryForList(sql);
		Iterator iter = list.iterator();
		while(iter.hasNext()){
			Long id = null;
			String itemCode = null;
			String itemName = null;
			String unit = null;
			try {
				Map m = (Map) iter.next();
				id = ((BigDecimal)m.get("ID")).longValue();
				itemCode = (String) m.get("CODE");
				itemName = (String) m.get("NAME");
				unit = (String) m.get("UNIT");
				
				sql = "SELECT INV_LOC FROM "
						+ MiddleTableName.W_SUPPLY_ITEM_ERP + " WHERE ITEMCODE='"+itemCode+"' GROUP BY INV_LOC";
				List clist = jdbcTemplateExt1.queryForList(sql);
				if(clist!=null && clist.size()>0){
					WmsOrganization org = null;
					Iterator citer = clist.iterator();
					while(citer.hasNext()){
						Map map = (Map) citer.next();
						String companyCode = map.get("INV_LOC")==null?"":String.valueOf(map.get("INV_LOC")).trim();
						if(companys.containsKey(companyCode)){
							org = companys.get(companyCode);
						}else{
							hql = "from WmsOrganization o where o.code=:code";
							org = (WmsOrganization) commonDao.findByQueryUniqueResult(hql, "code", companyCode);
							companys.put(companyCode, org);
						}
						if(null == org){//校验货主在WMS里是否存在
							errorLog.add("根据物料编码("+itemCode+")在"
									+ "供应商物料关系表里找到的货主("+companyCode+")在WMS系统里不存在;");
						}else{
							//如果货主已经存在,再校验此货品是否已经存在
							hql = "FROM WmsItem item WHERE item.code=:code AND item.company.code=:companyCode";
							WmsItem item = (WmsItem) commonDao.findByQueryUniqueResult(hql, 
															new String[]{"code","companyCode"}, 
																new Object[]{itemCode,companyCode});
							if(null != item){
								errorLog.add("编码("+itemCode+")和货主("+companyCode+")"
										+ "对应的货品在WMS已存在;");
							}else{
								company.add(org);
							}
						}
					}
				}else{
					errorLog.add("根据物料("+itemCode+")在供应商物料关系表中未找到对应数据;");
				}
			} catch (Exception e) {
				String exception = e.getMessage();
				exception = JavaTools.spiltLast(exception, ":");
				errorLog.add(exception);
			}
			
			if(errorLog.size() == 0){
				for(WmsOrganization com : company){
					WmsItem item = new WmsItem(com, itemCode, itemName, 
											unit, BaseStatus.ENABLED,lotRule);//货品
					item.setUpdateInfo(newUpdateInfo());
					commonDao.store(item);
					
					WmsPackageUnit packageUnit = new WmsPackageUnit(item, 
										1, unit, BaseStatus.DEFAULT_PACKAGE_LEVEL, 1);//包装
					packageUnit.setUpdateInfo(newUpdateInfo());
					commonDao.store(packageUnit);
				}
			}
			sql = batchUpdateMiddleStatus(MiddleTableName.W_ITEM_ERP, errorLog, id);
			jdbcTemplateExt1.execute(sql);
			errorLog.clear();company.clear();
			if(isAutoError){
				errorLog.add("WMS没有找到[默认批次规则];");
			}
		}
		System.out.println("-----------结束处理物料关系中间表数据---------"+list.size());
	}
	/**
	 * 根据接口表里的物料编码
	 * 		去供应商物料关系表里查货主,没查到则记录报错信息
	 * @param connection
	 * @param pre
	 * @param itemCode
	 * @param errorLog
	 * @param result
	 * @return
	 */
	List<WmsOrganization> getcompanyByItemCode(String itemCode,List<String> errorLog){
		List<WmsOrganization> orgs = new ArrayList<WmsOrganization>();//记录货主
		String sql = "select ID,INV_LOC from "
					+ MiddleTableName.W_SUPPLY_ITEM_ERP + " where ITEMCODE='"+itemCode+"'";
		List list = jdbcTemplateExt1.queryForList(sql);
		Iterator iter = list.iterator();
		while(iter.hasNext()){
			Map map = (Map) iter.next();
			String companyCode = (String) map.get("INV_LOC");
			WmsOrganization org = validateOrg(errorLog, companyCode, null, Boolean.FALSE);
			if(null == org){//校验货主在WMS里是否存在
				errorLog.add("根据物料编码("+itemCode+")在"
						+ "供应商物料关系表里找到的货主("+companyCode+")在WMS系统里不存在!\n");
			}else{
				WmsItem item = validateItem(errorLog,itemCode,
						companyCode,Boolean.TRUE);//如果货主已经存在,再校验此货品是否已经存在
				if(null != item){
					errorLog.add("编码("+itemCode+")和货主("+companyCode+")"
							+ "对应的货品在WMS已经存在,请检查!!");
				}
				orgs.add(org);
			}
		}
		if(orgs.size() == 0){
			errorLog.add("根据物料编码("+itemCode+")在供应商物料关系表中未找到对应数据,请检查!!");
		}
		
		return orgs;
	}
	public void dealErpSupplierDataFor(){
		while(true){
			dealErpSupplierData();
			try {
				Thread.sleep(1000*60*2);//2min
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
	//edit yc.min  wmsDealInterfaceDataManager.dealErpSupplierData
	public void dealErpSupplierData() {
		System.out.println("-----------开始处理供应商中间表数据-----------");
		List<String> errorLog = new ArrayList<String>();//记录错误信息
		
		String hql = "FROM WmsOrganization o WHERE o.code=:code";
		/**供应商基础信息数据*/
		String sql = "SELECT ID,COMPANY,SUPPLY_NO,SUPPLY_NAME,"
							+ " PROVINCE,CITY,CONTACT_USER,TEL,MOBILE,FAX,EMAIL,"
							+ " ADDRESS FROM "+MiddleTableName.W_SUPPLY_ERP
							+ " WHERE STATUS = 1 AND ROWNUM < 296";
		List list = jdbcTemplateExt1.queryForList(sql);
		Iterator iter = list.iterator();
		while(iter.hasNext()){
			Map m = (Map) iter.next();
			Long id = ((BigDecimal)m.get("ID")).longValue();
			String supplierNo = (String) m.get("SUPPLY_NO");//供应商编码
			String supplierName = (String) m.get("SUPPLY_NAME");//供应商名称
			String province = (String) m.get("PROVINCE");//省份
			String city = (String) m.get("CITY");//城市
			String contactUser = (String) m.get("CONTACT_USER");//联系人
			String tel = (String) m.get("TEL");//电话
			String mobile = (String) m.get("MOBILE");//手机
			String fax = (String) m.get("FAX");//传真
			String email = (String) m.get("EMAIL");//邮箱
			String address = (String) m.get("ADDRESS");//地址
			
			WmsOrganization supplier = (WmsOrganization) commonDao.findByQueryUniqueResult(hql, "code", supplierNo);
			if(null != supplier){
				errorLog.add("编码"+supplierNo+"的供应商在WMS已存在;");
			}else{
				Contact contact = new Contact(contactUser, tel, 
									mobile, fax, email, province, city, address);
				supplier = new WmsOrganization(supplierNo, 
									supplierName, contact, BaseStatus.ENABLED);
				supplier.setUpdateInfo(newUpdateInfo());
				commonDao.store(supplier);
			}
			sql = batchUpdateMiddleStatus(MiddleTableName.W_SUPPLY_ERP, errorLog, id);
			jdbcTemplateExt1.execute(sql);
			errorLog.clear();
		}
		System.out.println("-----------结束处理供应商中间表数据---------"+list.size());
	}
	
	public void dealQisResultDataFor() {
		while(true){
			dealQisResultData();
			try {
				Thread.sleep(1000*60*2);//2min
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
	//wmsDealInterfaceDataManager.dealQisResultData
	@SuppressWarnings("unchecked")
	public void dealQisResultData() {
		logger.error("-----------开始QIS质检结果数据-----------");
		Map<String,String[]> senderMap = senderMap();
		try {
			String sql="";
			List<Object[]> objs = getQualityData();
			//按发送方汇总
			Map<String,List<Object[]>> detailSenders = senders(objs,new int[]{
					12
			});
			//查询质检状态映射表
			List<Object[]> status = getQualityStatusData();
			//按发送方汇总(匹配所属的客户,以便正确的获取质检状态)
			Map<String,List<Object[]>> statusSenders = senders(status,new int[]{
					1
			});
			
			List<String> nullSenders = new ArrayList<String>();//未匹配到发送方-12
			List<String> nullmidStatus = new ArrayList<String>();//质检状态映射表未维护发送方数据-14
			List<String> nullItemStatus = new ArrayList<String>();//WMS未维护货品状态信息-13
			
			String sender = null,moveCode=null;
			String[] wearhouseCompany=null;
			Iterator<Entry<String, List<Object[]>>> itertor = detailSenders.entrySet().iterator();
			while(itertor.hasNext()){
				List<Long> nullMoves = new ArrayList<Long>();//无法匹配上质检单的数据-11
				List<Long> errStatus = new ArrayList<Long>();//质检状态不能正确匹配WMS-15
				List<Long> errBackQty = new ArrayList<Long>();//返回数量有误-16
				List<Long> succIds = new ArrayList<Long>();//返回正常执行完毕数据-3
				
				Entry<String, List<Object[]>> entry = itertor.next();
				sender = entry.getKey();
				if(!senderMap.containsKey(sender)){
					nullSenders.add(sender);//未匹配到发送方
					continue;
				}
				wearhouseCompany = senderMap.get(sender);//[warehouse,company]
				List<String> itemStatus = commonDao.findByQuery("SELECT t.name FROM WmsItemState t"
						+ " WHERE t.company.id =:companyId GROUP BY t.name", "companyId", Long.parseLong(wearhouseCompany[1]));
				if(itemStatus==null || itemStatus.size()<=0){
					nullItemStatus.add(sender);//WMS未维护货品状态信息
					continue;
				}
				if(!statusSenders.containsKey(sender)){
					nullmidStatus.add(sender);//质检状态映射表未维护发送方数据
					continue;
				}
				Map<String,String[]> midStatus = new HashMap<String, String[]>();
				for(Object[] o : statusSenders.get(sender)){
					midStatus.put(o[3].toString(), 
							new String[]{
						o[2].toString(),o[4].toString()
					});//INTER_FACE_NAME,[WMS_NAME,SCRAP_NAME] 
					//MES回写状态,[wms维护状态(对应BACK_QTY>0回写wms),wms维护报废状态(对应SCRAP_QTY>0回写wms)]
				}
				
				objs = entry.getValue();
				//按质检单号汇总CODE
				Map<String,List<Object[]>> movelKeys = senders(objs,new int[]{
						2
				});
				Iterator<Entry<String, List<Object[]>>> itertor1 = movelKeys.entrySet().iterator();
				while(itertor1.hasNext()){
					Entry<String, List<Object[]>> entry1 = itertor1.next();
					moveCode = entry1.getKey();
					objs = entry1.getValue();
					//mes质检是一条物料返回一次,同一个质检单号下的明细会存在多次返回
					List<WmsMoveDocDetail> mdds = commonDao.findByQuery("FROM WmsMoveDocDetail mm"
							+ " WHERE mm.moveDoc.code =:moveDocCode"
//							+ " AND (mm.moveDoc.transStatus >:transStatus OR mm.moveDoc.transStatus <:transStatus)", 
							+ " AND mm.processQuantityBU=0",
							new String[]{"moveDocCode"}, new Object[]{moveCode});
					if(mdds.size()>0){
						MilldleSessionManager milldleSessionManager = (MilldleSessionManager) 
													applicationContext.getBean("milldleSessionManager");
						Object[] backObj = milldleSessionManager.readMidQuality(objs, mdds, midStatus, itemStatus);
						
						errStatus.addAll((List<Long>) backObj[0]);
						errBackQty.addAll((List<Long>) backObj[1]);
						succIds.addAll((List<Long>) backObj[2]);
					}else{
						for(Object[] o : objs){
							nullMoves.add(Long.parseLong(o[0].toString()));
						}
						continue;
					}
				}
				//回写-WMS读取完毕,状态置为3
				if(succIds!=null&&succIds.size()>0){
					int size = succIds.size();
					int j = JavaTools.getSize(size, PAGE_NUMBER);
					for(int k = 0 ; k<j ; k++){
						int index = JavaTools.getIndex(k, size, PAGE_NUMBER);
						List<Long> ids = JavaTools.getListLong(succIds, k, index, PAGE_NUMBER);
						sql = "update "+MiddleTableName.W_QUALITY_TESTING_QIS+" set flag = 3,wms_memo = 'WMS读取完毕',DEAL_TIME = systimestamp"
								+ " where id in ("+StringUtils.substringBetween(ids.toString(), "[", "]")+")";
						jdbcTemplateExt1.update(sql);
					}
				}
				//回写-无法匹配上质检单的数据,状态置为11-nullMoves
				if(nullMoves!=null&&nullMoves.size()>0){
					int size = nullMoves.size();
					int j = JavaTools.getSize(size, PAGE_NUMBER);
					for(int k = 0 ; k < j ; k++){
						int index = JavaTools.getIndex(k, size, PAGE_NUMBER);
						List<Long> ids = JavaTools.getListLong(nullMoves, k, index, PAGE_NUMBER);
						sql = "update "+MiddleTableName.W_QUALITY_TESTING_QIS+" set flag = 11,wms_memo = '无法匹配质检单号',DEAL_TIME = systimestamp"
								+ " where id in ("+StringUtils.substringBetween(ids.toString(), "[", "]")+")";
						jdbcTemplateExt1.update(sql);
					}
					nullMoves.clear();
				}
				//回写-质检状态不能正确匹配WMS-15
				if(errStatus!=null&&errStatus.size()>0){
					int size = errStatus.size();
					int j = JavaTools.getSize(size, PAGE_NUMBER);
					for(int k = 0 ; k < j ; k++){
						int index = JavaTools.getIndex(k, size, PAGE_NUMBER);
						List<Long> ids = JavaTools.getListLong(errStatus, k, index, PAGE_NUMBER);
						sql = "update "+MiddleTableName.W_QUALITY_TESTING_QIS+" set flag = 15,wms_memo = '质检状态不能正确匹配WMS',DEAL_TIME = systimestamp"
								+ " where id in ("+StringUtils.substringBetween(ids.toString(), "[", "]")+")";
						jdbcTemplateExt1.update(sql);
					}
				}
				//回写-返回数量有误-16
				if(errBackQty!=null&&errBackQty.size()>0){
					int size = errBackQty.size();
					int j = JavaTools.getSize(size, PAGE_NUMBER);
					for(int k = 0 ; k < j ; k++){
						int index = JavaTools.getIndex(k, size, PAGE_NUMBER);
						List<Long> ids = JavaTools.getListLong(errBackQty, k, index, PAGE_NUMBER);
						sql = "update "+MiddleTableName.W_QUALITY_TESTING_QIS+" set flag = 16,wms_memo = '返回数量有误'"
								+ " where id in ("+StringUtils.substringBetween(ids.toString(), "[", "]")+")";
						jdbcTemplateExt1.update(sql);
					}
				}
			}
			
			//回写-质检状态映射表未维护发送方数据,状态置为14-nullmidStatus
			if(nullmidStatus!=null&&nullmidStatus.size()>0){
				List<String> ss = JavaTools.charList(nullmidStatus);
				sql = "update "+MiddleTableName.W_QUALITY_TESTING_QIS+" set flag = 14,wms_memo = '质检状态映射表未维护发送方数据',DEAL_TIME = systimestamp"
						+ " where SENDER in ("+StringUtils.substringBetween(ss.toString(), "[", "]")+") AND flag = 2";
				jdbcTemplateExt1.update(sql);
			}
			//回写-WMS未维护货品状态信息,状态置为13-nullItemStatus
			if(nullItemStatus!=null&&nullItemStatus.size()>0){
				List<String> ss = JavaTools.charList(nullItemStatus);
				sql = "update "+MiddleTableName.W_QUALITY_TESTING_QIS+" set flag = 13,wms_memo = 'WMS未维护货品状态信息',DEAL_TIME = systimestamp"
						+ " where SENDER in ("+StringUtils.substringBetween(ss.toString(), "[", "]")+") AND flag = 2";
				jdbcTemplateExt1.update(sql);
			}
			//回写-无法匹配发送方,状态置为12-nullSenders
			if(nullSenders!=null&&nullSenders.size()>0){
				List<String> ss = JavaTools.charList(nullSenders);
				sql = "update "+MiddleTableName.W_QUALITY_TESTING_QIS+" set flag = 12,wms_memo = '无法匹配发送方',DEAL_TIME = systimestamp"
						+ " where SENDER in ("+StringUtils.substringBetween(ss.toString(), "[", "]")+") AND flag = 2";
				jdbcTemplateExt1.update(sql);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException(e.getLocalizedMessage());
		}finally{
//			if(session!=null && session.isOpen()){
//				session.close();
//			}
		}
		logger.error("-----------结束QIS质检结果数据-----------");
	}
	
	List<Object[]> getQualityData(){
		List<Object[]> objs = new ArrayList<Object[]>();
		//CODE-质检单CODE,QUALITY_STATUS-质检状态,PROCESS_STATE-工艺状态,SCRAP_QTY-报损数量,MES_QUALITY_CODE-MES质检单号
		String sql = "select ID,COMPANY,CODE,ASN_CODE,"
					+ "SUPPLIER_CODE,MATERIAL_CODE,QUALITY_STATUS,PROCESS_STATE,"
					+ "SEND_QTY,BACK_QTY,SCRAP_QTY,MES_MEMO,SENDER,MES_QUALITY_CODE"
					+ " from "+MiddleTableName.W_QUALITY_TESTING_QIS
					+ " where FLAG = 2 AND SENDER IS NOT NULL";
		List list = jdbcTemplateExt1.queryForList(sql);
		Iterator iter = list.iterator();
		while(iter.hasNext()){
			Map m = (Map) iter.next();
			Long id = ((BigDecimal)m.get("ID")).longValue();
			String company = (String) m.get("COMPANY");
			String code = (String) m.get("CODE");
			String asnCode = (String) m.get("ASN_CODE");
			String supplyCode = (String) m.get("SUPPLIER_CODE");
			String itemCode = (String) m.get("MATERIAL_CODE");
			String qualityStatus = (String) m.get("QUALITY_STATUS");
			String proState = (String) m.get("PROCESS_STATE");
			Double sendQty = m.get("SEND_QTY")==null?0D:((BigDecimal)m.get("SEND_QTY")).doubleValue();
			Double backQty = m.get("BACK_QTY")==null?0D:((BigDecimal)m.get("BACK_QTY")).doubleValue();
			Double scrapQty = m.get("SCRAP_QTY")==null?0D:((BigDecimal)m.get("SCRAP_QTY")).doubleValue();
			String mesMemo = (String) m.get("MES_MEMO");
			String sender = (String) m.get("SENDER");
			String mesQualityCode = (String) m.get("MES_QUALITY_CODE");
			Object[] obj = new Object[]{id,company,code,asnCode,supplyCode,itemCode,qualityStatus,
					proState,sendQty,backQty,scrapQty,mesMemo,sender,mesQualityCode};
			objs.add(obj);
		}
		return objs;
	}
	
	List<Object[]> getQualityStatusData(){
		List<Object[]> objs = new ArrayList<Object[]>();
		
		String sql = "select COMPANY,SENDER,WMS_NAME,INTER_FACE_NAME,"
				+ "SCRAP_NAME,MEMO from "+MiddleTableName.MIDDLE_QUALITY_STATUS;
		List list = jdbcTemplateExt1.queryForList(sql);
		Iterator iter = list.iterator();
		while(iter.hasNext()){
			Map m = (Map) iter.next();
			String company = (String) m.get("COMPANY");
			String sender = (String) m.get("SENDER");
			String wmsName = (String) m.get("WMS_NAME");
			String interfaceName = (String) m.get("INTER_FACE_NAME");
			String scrapName = (String) m.get("SCRAP_NAME");
			String memo = (String) m.get("MEMO");
			
			Object[] obj = new Object[]{company,sender,wmsName,interfaceName,scrapName,memo};
			objs.add(obj);
		}
		return objs;
	}
	
	//质检表数据
	void saveDataToWms(List list){
		String qualitySql = "select ID,CODE,COMPANY,ASN_CODE,SUPPLIER_CODE,"
				+ "MATERIAL_CODE,QUALITY_STATUS,PROCESS_STATE,SEND_QTY,BACK_QTY,"
				+ "SCRAP_QTY,FLAG,WMS_WRITE_TIME,MES_READ_TIME,MES_BACK_TIME,"
				+ "WMS_READ_TIME,WMS_MEMO,MES_MEMO,MES_QUALITY_CODE,WMS_CANCEL_FLAG,"
				+ "SENDER,CREATE_TIME,DEAL_TIME,RECEIVED_QUANTITY_BU,SUPPLY_NAME,"
				+ "ITEM_NAME,SJR,SHDH,AVAILABLE_QTY from "
				+MiddleTableName.W_QUALITY_TESTING_QIS+" where FLAG=2";
		Iterator iter = list.iterator();
		while(iter.hasNext()){
			Map map = (Map) iter.next();
			Long id = (Long) map.get("ID");
			String code = (String) map.get("CODE");
			String company = (String) map.get("COMPANY");
			String asnCode = (String) map.get("ASN_CODE");
			String supplyCode = (String) map.get("SUPPLIER_CODE");
			String itemCode = (String) map.get("MATERIAL_CODE");
			String qualityStatus = (String) map.get("QUALITY_STATUS");
			String proState = (String) map.get("PROCESS_STATE");
			Double sendQty = (Double) map.get("SEND_QTY");
			Double backQty = (Double) map.get("BACK_QTY");
			Double scrapQty = (Double) map.get("SCRAP_QTY");
			Double flag = (Double) map.get("FLAG");
			Date wmsWriteTime = (Date) map.get("WMS_WRITE_TIME");
			Date wmsReadTime = (Date) map.get("MES_READ_TIME");
			String wmsMemo = (String) map.get("WMS_MEMO");
			String mesMemo = (String) map.get("MES_MEMO");
			String mesQualityCode = (String) map.get("MES_QUALITY_CODE");
			Double cancelflag = (Double) map.get("WMS_CANCEL_FLAG");
			String sender = (String) map.get("SENDER");
		}
	}
	
	@SuppressWarnings("unchecked")
	private Map<String,String[]> senderMap(){
		List<Object[]> mces = commonDao.findByQuery("SELECT m.sender,m.warehouse.id,m.company.id"
				+ " FROM MiddleCompanyExtends m WHERE 1=1");
		if(mces==null || mces.size()<=0){
			throw new BusinessException("仓库货主接口映射关系表未维护信息");
		}
		Map<String,String[]> senderMap = new HashMap<String, String[]>();//sender,[warehouse,company]
		for(Object[] mce : mces){
			senderMap.put(mce[0].toString(), new String[]{
				mce[1].toString(),mce[2].toString()
			});
		}
		mces.clear();
		return senderMap;
	}
	
	private Map<String,List<Object[]>> senders(List<Object[]> objs,int[] a){
		String key = null;
		Map<String,List<Object[]>> senders = new HashMap<String, List<Object[]>>();
		List<Object[]> tempObj = null;
		for(Object[] o : objs){
			key = "";
			for (int i = 0; i < a.length; i++) {
				key += o[a[i]]+MyUtils.spilt1;
			}
			key = StringUtils.substringBeforeLast(key,MyUtils.spilt1);
			if(senders.containsKey(key)){
				tempObj = senders.get(key);
			}else{
				tempObj = new ArrayList<Object[]>();
			}
			tempObj.add(o);
			senders.put(key, tempObj);
		}
		return senders;
	}
	
	public void insertMiddleTable(String moveDocCode,String asnCode,String supplierCode,
			String itemCode,String itemState,Double sendQty,String replenishmentArea,
			Double receivedQuantityBU,String itemName,String supplyName,String userName,
			String relateBill1,Double totalQty,String company,String qualityType){
		Connection connection = getConnection();
		PreparedStatement pre = null;
		ResultSet rs = null;
		if(null == itemState || "null".equals(itemState) || "".equals(itemState)){
			itemState = "-";
		}
		/**等等用序列*/
		Long maxId = getMaxId(pre, rs, connection, MiddleTableName.W_QUALITY_TESTING_QIS);
		
		String insertSql = "insert into "+MiddleTableName.W_QUALITY_TESTING_QIS+"(code,"
				+ "company,asn_Code,supplier_code,material_code,process_state,send_qty,"
				+ "MES_QUALITY_CODE,RECEIVED_QUANTITY_BU,SENDER,SUPPLY_NAME,ITEM_NAME,SJR,ID,"
				+ "SHDH,AVAILABLE_QTY,QUALITY_TYPE) "
				+ "values(?,?,?,?,?,?,?,?,?,'JQWLXG',?,?,?,?,?,?,?)";
		try {
			pre = connection.prepareStatement(insertSql);
			pre.setString(1,moveDocCode);//质检单号
			pre.setString(2,company);//货主
			pre.setString(3, asnCode);//ASN单号
			pre.setString(4,supplierCode);//供应商编码
			pre.setString(5, itemCode);//货品编码
			pre.setString(6, itemState);//工艺状态
			pre.setDouble(7, sendQty);//送检数量
			pre.setString(8, replenishmentArea);//JAC质检唯一单号
			pre.setDouble(9, receivedQuantityBU);//加工数量
			pre.setString(10, itemName);//货品名称
			pre.setString(11, supplyName);//供应商名称
			pre.setString(12, userName);//送检人
			pre.setLong(13, maxId);//id
			pre.setString(14, relateBill1);//送货单号
			pre.setDouble(15, totalQty);//总数
			pre.setString(16, qualityType);//送检分类
			pre.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new BusinessException("处理失败："+e.getMessage());
		}finally{
			closeConnection(rs, pre, connection);
		}
	}
	/**
	 * 判断是否为空,返回字符串
	 * @param obj
	 * @return
	 */
	String getMayNullData(Object obj){
		String str = null;
		if(null != obj && !"".equals(obj)){
			str = obj.toString();
		}
		return str;
	}
	/**
	 * 判断是否为空，空就返回-
	 * @param obj
	 * @return
	 */
	String getMayNullData1(Object obj){
		String str = "-";
		if(null != obj && !"".equals(obj)){
			str = obj.toString();
		}
		return str;
	}
	//按照子单号签收
	public void receiveBolNo() {
		String sql = "SELECT ID,trim(BOL_NO) as BOL_NO,SUPPLY_NO,ITEMCODE,"+
				 "SIGNQTY FROM "+MiddleTableName.W_RECEIVE_MES+" WHERE STATUS=1 AND trim(BOL_NO) IS NOT NULL" +
				 " AND SIGNQTY>0 ORDER BY BOL_NO,SUPPLY_NO,ITEMCODE";
		List list = jdbcTemplateExt1.queryForList(sql);
		if(list!=null && list.size()>0){
			int size = list.size();
			int j = JavaTools.getSize(size, PAGE_NUMBER);
			Double quantity = 0D;
			String message = "";
			for(int k = 0 ; k < j ; k++){
				int index = JavaTools.getIndex(k, size, PAGE_NUMBER);
				List objs = JavaTools.getLists(list, k, index, PAGE_NUMBER);
				Iterator iter = objs.iterator();
				Long id = null;//id
				String bolNo = null;//装车子单号
				String supplyNo = null;//供应商编码
				String itemCode = null;//物料编码
				Double signQty = 0D;//签收数量
				while(iter.hasNext()){
					Map map = (Map) iter.next();
					try {
						id = ((BigDecimal)map.get("ID")).longValue();//id
						bolNo = (String) map.get("BOL_NO");//装车子单号
						supplyNo = (String) map.get("SUPPLY_NO");//供应商编码
						itemCode = (String) map.get("ITEMCODE");//物料编码
						signQty = Double.parseDouble(map.get("SIGNQTY").toString());//签收数量
						String hql = "FROM WmsBOLDetail bb WHERE bb.subCode =:subCode AND bb.itemKey.lotInfo.supplier.code =:supplier" +
								" AND bb.itemKey.item.code =:item AND bb.signQty<bb.quantityBU";
						List<WmsBOLDetail> bbs = commonDao.findByQuery(hql, new String[]{"subCode","supplier","item"}, 
								new Object[]{bolNo,supplyNo,itemCode});
						bolSignQty(bbs, signQty, id);
					} catch (Exception e) {
						String exception = JavaTools.spiltLast(e.getMessage(), ":");
						message = exception+":"+JavaTools.format(new Date(), JavaTools.ymd_Hms);
						sql = "update "+MiddleTableName.W_RECEIVE_MES+" set status=0,"
								+ "EXCEPTION_MESS='"+message+"' where id="+id;
						jdbcTemplateExt1.execute(sql);
					}
				}
				try {
					Thread.sleep(1000*2);//2'
				} catch (InterruptedException e) {
					logger.error("", e);
				}
			}
		}
	}
	private void bolSignQty(List<WmsBOLDetail> bbs,Double signQty,Long id){
		String message = "",sql = "";
		Double quantity = 0D;
		if(bbs==null || bbs.size()<=0){
			message = "未匹配到子单号"+JavaTools.format(new Date(), JavaTools.ymd_Hms);
			sql = "update "+MiddleTableName.W_RECEIVE_MES+" set status=0,"
					+ "EXCEPTION_MESS='"+message+"' where id="+id;
			jdbcTemplateExt1.execute(sql);
			return;
		}
		for(WmsBOLDetail bb : bbs){
			if(signQty==0){
				continue;
			}
			quantity = bb.availableSignQty()<signQty?bb.availableSignQty():signQty;
			bb.setSignQty(quantity);
			commonDao.store(bb);
			signQty -= quantity;
		}
		if(signQty>0){
			message = "警告:签收量大于发运量"+JavaTools.format(new Date(), JavaTools.ymd_Hms);
			sql = "update "+MiddleTableName.W_RECEIVE_MES+" set status=2,"
					+ "EXCEPTION_MESS='"+message+"' where id="+id;
			jdbcTemplateExt1.execute(sql);
		}else{
			message = JavaTools.format(new Date(), JavaTools.ymd_Hms);
			sql = "update "+MiddleTableName.W_RECEIVE_MES+" set status=3,"
					+ "EXCEPTION_MESS='"+message+"' where id="+id;
			jdbcTemplateExt1.execute(sql);
		}
	}
	//根据mes单号签收
	public void receiveMesNo() {
		String sql = "SELECT ID,trim(ODR_NO) as ODR_NO,SUPPLY_NO,ITEMCODE,"+
				 "SIGNQTY FROM "+MiddleTableName.W_RECEIVE_MES+
				 " WHERE STATUS=1" +
//				 " WHERE ID=93" +//test
				 " AND trim(ODR_NO) IS NOT NULL" +
				 " AND SIGNQTY>0 ORDER BY ODR_NO,SUPPLY_NO,ITEMCODE";
		List list = jdbcTemplateExt1.queryForList(sql);
		if(list!=null && list.size()>0){
			Map<String,List<String>> mesTags = new HashMap<String, List<String>>();//mes单号下关联的标签号
			List<String> tags = null;
			int size = list.size();
			int j = JavaTools.getSize(size, PAGE_NUMBER);
			Double quantity = 0D;
			String message = "";
			for(int k = 0 ; k < j ; k++){
				int index = JavaTools.getIndex(k, size, PAGE_NUMBER);
				List objs = JavaTools.getLists(list, k, index, PAGE_NUMBER);
				Iterator iter = objs.iterator();
				Long id = null;//id
				String mesNo = null;//mes单号
				String supplyNo = null;//供应商编码
				String itemCode = null;//物料编码
				Double signQty = 0D;//签收数量
				while(iter.hasNext()){
					Map map = (Map) iter.next();
					try {
						id = ((BigDecimal)map.get("ID")).longValue();//id
						mesNo = (String) map.get("ODR_NO");//mes单号
						supplyNo = (String) map.get("SUPPLY_NO");//供应商编码
						itemCode = (String) map.get("ITEMCODE");//物料编码
						signQty = Double.parseDouble(map.get("SIGNQTY").toString());//签收数量
						if(!mesTags.containsKey(mesNo)){
							tags = new ArrayList<String>();
							String hql = "SELECT DISTINCT wds.boxTag FROM WmsMoveDocAndStation wds" +
									" WHERE wds.moveDocDetail.moveDoc.relatedBill1 =:relatedBill1";//.pickTicket
							tags = commonDao.findByQuery(hql, new String[]{"relatedBill1"}, new Object[]{mesNo});
							mesTags.put(mesNo, tags);
						}else{
							tags = mesTags.get(mesNo);
						}
						if(tags==null || tags.size()<=0){
							message = "未匹配到拣货明细与器具关系"+JavaTools.format(new Date(), JavaTools.ymd_Hms);
							sql = "update "+MiddleTableName.W_RECEIVE_MES+" set status=0,"
									+ "EXCEPTION_MESS='"+message+"' where id="+id;
							jdbcTemplateExt1.execute(sql);
							continue;
						}
						String hql = "FROM WmsBOLDetail bb WHERE bb.boxTag in(:tags) AND bb.itemKey.lotInfo.supplier.code =:supplier" +
								" AND bb.itemKey.item.code =:item AND bb.signQty<bb.quantityBU";
						List<WmsBOLDetail> bbs = commonDao.findByQuery(hql, new String[]{"tags","supplier","item"}, 
								new Object[]{tags,supplyNo,itemCode});
						
						bolSignQty(bbs, signQty, id);
						
					} catch (Exception e) {
						String exception = JavaTools.spiltLast(e.getMessage(), ":");
						message = exception+":"+JavaTools.format(new Date(), JavaTools.ymd_Hms);
						sql = "update "+MiddleTableName.W_RECEIVE_MES+" set status=0,"
								+ "EXCEPTION_MESS='"+message+"' where id="+id;
						jdbcTemplateExt1.execute(sql);
					}
				}
			}
			try {
				Thread.sleep(1000*2);//2'
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
	public void receiveMesFor() {
		while(true){
			receiveMes();
			try {
				Thread.sleep(1000*60*2);//2min
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
	//wmsDealInterfaceDataManager.receiveMes
	public void receiveMes() {
		receiveBolNo();
		receiveMesNo();
	}
	/**找到唯一装车单明细*/
	Long findBolDetail(List<String> errorLog,String bolNo,String itemCode){
		String hql = "from WmsBOLDetail b where "
				+ "b.item.code=:itemCode and b.bol.code=:bolCode";
		Long id = 0l;
		try {
			id = (Long) commonDao.
					findByQueryUniqueResult(hql, 
						new String[]{"itemCode","bolCode"}, 
							new Object[]{bolNo,itemCode});
		} catch (Exception e) {
			e.printStackTrace();
			errorLog.add("根据装车子单号和物料编码找到了两条数据,请检查!!");
		}
		if(null == id){
			errorLog.add("根据装车子单号和物料编码没有找到对应数据,请检查!!");
		}
		return id;
	}
	public void readMonthQisPlanFor() {
		while(true){
			readMonthQisPlan();
			try {
				Thread.sleep(1000*60*2);//2min
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
	//wmsDealInterfaceDataManager.readMonthQisPlan
	public void readMonthQisPlan() {
		System.out.println("----开始处理质检基础信息----");
		String sql = "select PLAN_YEAR,PLAN_MONTH,FACTORY,PART_NO,"
				+ "SUPPLIER_CODE,PART_NAME,SUPPLIER_NAME,NOTE,QUANTITY,STORECODE "
				+ "from "+MiddleTableName.W_QISPLAN+" where status = 1 AND ROWNUM < 296";
		List list = jdbcTemplateExt1.queryForList(sql);
		Iterator iter = list.iterator();
		String message = "";
		String planYear = null;//计划年份
		String planMonth = null;//计划月份
		String factory = null;//工厂
		String partNo = null;//零件号
		String supplyNo = null;//供应商编码
		String partName = null;//零件名称
		String supplyName = null;//供应商名称
		String note = null;//备注
		String qty = null;//数量
		String companyCode = null;//货主
		while(iter.hasNext()){
			Map map = (Map) iter.next();
			try {
				planYear = getMayNullData1(map.get("PLAN_YEAR"));//计划年份
				planMonth = getMayNullData1(map.get("PLAN_MONTH"));//计划月份
				factory = getMayNullData1(map.get("FACTORY"));//工厂
				partNo = getMayNullData1(map.get("PART_NO"));//零件号
				supplyNo = getMayNullData1(map.get("SUPPLIER_CODE"));//供应商编码
				partName = getMayNullData1(map.get("PART_NAME"));//零件名称
				supplyName = getMayNullData1(map.get("SUPPLIER_NAME"));//供应商名称
				note = getMayNullData1(map.get("NOTE"));//备注
				qty = getMayNullData1(map.get("QUANTITY"));//数量
				companyCode = getMayNullData1(map.get("STORECODE"));//货主
				
				QisPlan qisP = new QisPlan(planYear, planMonth, factory, partNo, supplyNo, partName, supplyName, note, 
						qty, new Date(), companyCode);
				commonDao.store(qisP);
				
				sql = "update "+MiddleTableName.W_QISPLAN+" set status=3" +
						" where PLAN_YEAR = ? and PLAN_MONTH = ? and FACTORY = ? and PART_NO = ? and SUPPLIER_CODE = ? and STORECODE = ? and status = 1";
				jdbcTemplateExt1.update(sql, new Object[]{planYear,planMonth,factory,partNo,supplyNo,companyCode});
			} catch (Exception e) {
				String exception = JavaTools.spiltLast(e.getMessage(), ":");
				message = exception+":"+JavaTools.format(new Date(), JavaTools.ymd_Hms);
				try {
					sql = "update "+MiddleTableName.W_QISPLAN+" set status=0,"
							+ "EXCEPTION_MESS='"+message
							+"' where PLAN_YEAR = ? and PLAN_MONTH = ? and FACTORY = ? and PART_NO = ? and SUPPLIER_CODE = ? and STORECODE = ? and status = 1";
					jdbcTemplateExt1.update(sql, new Object[]{planYear,planMonth,factory,partNo,supplyNo,companyCode});
				} catch (Exception e2) {
					//发消息提示接口无法终止的错误
				}
			}
		}
		System.out.println("----结束处理质检基础信息--"+list.size());
	}
	
	/**出库数据传erp*/
	public void outBoundToErp(WmsBOLDetail detail,WmsMoveDoc moveDoc,
				int i,WmsTask task,String billCode) {
		Connection connection = getConnection();
		if(null == connection){//处理装车单第一条数据的时候获取连接,处理第二条数据的时候就不用再获取连接了
			throw new BusinessException("数据库连接失败,请检查!!");
		}
		String insertSql = "insert into "+MiddleTableName.W_DELIVER_ERP
						+ "(ID,DELIVERY_CODE,LINE_NO,DEV_QTY,INV_LOC,UNIT,ITEMCODE,"
						+ "SUPPLY_CODE,ORDER_TYPE,CKID,DELIVER_DATE,STATUS,BOL_ID) "
						+ "values(wseq_W_DELIVER_ERP.nextval,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement pre = null;
		WmsMoveDocDetail moveDocDetail = commonDao.load(WmsMoveDocDetail.class, task.getMoveDocDetail().getId());
		try {
			pre = connection.prepareStatement(insertSql);
			
			pre.setString(1,detail.getBol().getCode());//装车单号
			pre.setInt(2,10 * i);//行号
			pre.setDouble(3, detail.getQuantityBU());//送检数量
			pre.setString(4, moveDoc.getCompany().getCode());//货主
			pre.setString(5, moveDocDetail.getPackageUnit().getUnit());//包装
			pre.setString(6, detail.getItemKey().getItem().getCode());//货品编码
			pre.setString(7, moveDocDetail.getShipLotInfo().getSupplier());//供应商编码
			pre.setString(8, billCode);//单据类型
			pre.setString(9, detail.getId().toString());//子单号ID
			pre.setTimestamp(10,Timestamp.valueOf(DateUtil.formatDateToStr(new Date())));//发货日期
			pre.setString(11,"1");//状态
			pre.setString(12,detail.getSubCode());//子单号
			
			pre.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
		}finally{
			closeConnection(null, pre, connection);
		}
		logger.error("-----出库数据传ERP结束-----");
	}
	
	/**出库数据传MES*/
	public void outBoundToMes(WmsBOLDetail detail,WmsMoveDoc moveDoc,
						int i,WmsTask task,String billCode){
		Connection connection = getConnection();
		if(null == connection){//处理装车单第一条数据的时候获取连接,处理第二条数据的时候就不用再获取连接了
			throw new BusinessException("数据库连接失败,请检查!!");
		}
		String insertSql = insertMesSql();
		PreparedStatement pre = null;
		
		WmsPickTicket pickTicket = commonDao.load(WmsPickTicket.class,moveDoc.getPickTicket().getId());
		WmsMoveDocDetail moveDocDetail = commonDao.load(WmsMoveDocDetail.class,task.getMoveDocDetail().getId());
		WmsPickTicketDetail pickTicketDetail = commonDao.load(WmsPickTicketDetail.class,moveDocDetail.getRelatedId());
		
		List<WmsMoveDocAndStation> mas = getmoveDocAndStation(moveDocDetail.getId());
		String applianceType = null,applianceName = null;//器具型号,器具名称
		Integer applianceAmount=0;
		if(mas.size() > 0){
			applianceType = mas.get(0).getType() == null ? null : mas.get(0).getType().toString();
			applianceAmount = mas.get(0).getLoadage() == null ? 0 : mas.get(0).getLoadage();
		}
		String subCode = detail.getSubCode();//子单号
		String mesCode = pickTicket.getRelatedBill1();//mes料单号
		String supplyCode = moveDocDetail.getShipLotInfo().getSupplier();//供应商编码
		String productionLine = getMayNullData1(pickTicketDetail.getProductionLine());//生产线
		String station = pickTicketDetail.getStation();//工位
		String remark = pickTicketDetail.getRemark();//备注
		String itemCode = detail.getItemKey().getItem().getCode();//货品编码
		try {
			pre =connection.prepareStatement(insertSql);
			
			insertMes(pre, subCode, mesCode, billCode, i, pickTicket.getRequireArriveDate(), supplyCode, itemCode, 
					moveDocDetail.getPlanQuantityBU(),detail.getQuantityBU(), moveDoc.getCompany().getCode(), 
					pickTicket.getReceiveHouse(), productionLine, pickTicket.getReceiveDoc(), moveDoc.getBlg().getName(), 
					detail.getBol().getVehicleNo(), pickTicketDetail.getIsJp(), pickTicket.getBatch(), station, applianceType, 
					applianceName, applianceAmount, mas.size(), remark);
//			pre.setString(1, subCode);//子单号
//			pre.setString(2, getMayNullData1(mesCode));//MES料单号
//			pre.setString(3, billCode);//单据类型
//			pre.setInt(4, 10 * i);//行号
//			pre.setTimestamp(5, Timestamp.valueOf
//					(DateUtil.formatDateToStr
//							(pickTicket.getRequireArriveDate() == null 
//								? new Date() : pickTicket.getRequireArriveDate())));//生产日期
//			pre.setTimestamp(6, Timestamp.valueOf(DateUtil.formatDateToStr(new Date())));
//			pre.setString(7, getMayNullData1(supplyCode));//供应商
//			pre.setString(8, detail.getItemKey().getItem().getCode());//货品编码
//			pre.setDouble(9, moveDocDetail.getPlanQuantityBU());//订单数量
//			pre.setDouble(10, detail.getQuantityBU());//发运数量
//			pre.setString(11, moveDoc.getCompany().getCode());//货主
//			pre.setString(12, getMayNullData1(pickTicket.getReceiveHouse()));//目的仓库
//			pre.setString(13, productionLine);//生产线
//			pre.setString(14, getMayNullData1(pickTicket.getReceiveDoc()));//收货道口
//			pre.setString(15, moveDoc.getBlg().getName());//备料工
//			pre.setString(16, detail.getBol().getVehicleNo());//车牌号
//			if(null == pickTicketDetail.getIsJp()){//是否集配
//				pre.setString(17,null);
//			}else{
//				pre.setString(17,pickTicketDetail.getIsJp() == true ? "Y" : "N" );
//			}
//			pre.setString(18, pickTicket.getBatch());//批次
//			pre.setString(19, station);//工位
//			pre.setString(20, applianceType);//器具类型
//			pre.setString(21, applianceName);//器具名称
//			pre.setInt(22, applianceAmount);//满载量
//			pre.setInt(23, mas.size());//器具个数
//			pre.setString(24, remark);//备注
//			pre.setString(25, "1");//状态
//			pre.executeUpdate();
		
			outBoundApplianceToMes( moveDocDetail, subCode, mesCode, billCode, 
					connection, pre, supplyCode,  itemCode, detail.getQuantityBU(), 
					productionLine, station, remark, mas);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
		}finally{
			closeConnection(null, pre, connection);
		}
		logger.error("-----出库数据传MES结束-----");
	}
	private String insertMesSql(){
		String insertSql = "INSERT INTO "+MiddleTableName.W_DELIVER_MES
				+ "(ID,ODR_NO,MESODR_NO,ODR_TYPE,LINE_NO,DEMAND_DATE,DELIVER_DATE,"
				+ "SUPPLY_NO,ITEM,ODR_QTY,DEL_QTY,FWARE,DWARE,PRODUCT_LINE,SHDK,SLR,"
				+ "LICENSE,IS_JP,BATCH,STATION,APPLIANCE_TYPE,APPLIANCE_NAME,"
				+ "APPLIANCE_AMOUNT,APPLIANCE_SUM,REMARK,STATUS) values"
				+ "(wseq_W_DELIVER_MES.nextval,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		return insertSql;
	}
	private void insertMes(PreparedStatement pre,String subCode,String mesCode,String billCode,int i,Date requireArriveDate
			,String supplyCode,String itemCode,Double planQuantityBU,Double shipQuantityBU,String companyCode,String receiveHouse,String productionLine
			,String receiveDoc,String blgName,String vehicleNo,Boolean isJp,String batch,String station,String applianceType
			,String applianceName,int applianceAmount,int size,String remark) throws SQLException{
		pre.setString(1, subCode);//子单号
		pre.setString(2, getMayNullData1(mesCode));//MES料单号
		pre.setString(3, billCode);//单据类型
		pre.setInt(4, 10 * i);//行号
		pre.setTimestamp(5, Timestamp.valueOf
				(DateUtil.formatDateToStr
						(requireArriveDate == null ? new Date() : requireArriveDate)));//生产日期
//		pre.setDate(6, new java.sql.Date(new Date().getTime()));//发运日期
		pre.setTimestamp(6, Timestamp.valueOf(DateUtil.formatDateToStr(new Date())));
		pre.setString(7, getMayNullData1(supplyCode));//供应商
		pre.setString(8, itemCode);//货品编码
		pre.setDouble(9, planQuantityBU);//订单数量
		pre.setDouble(10, shipQuantityBU);//发运数量
		pre.setString(11, companyCode);//货主
		pre.setString(12, getMayNullData1(receiveHouse));//目的仓库
		pre.setString(13, productionLine);//生产线
		pre.setString(14, getMayNullData1(receiveDoc));//收货道口
		pre.setString(15, blgName);//备料工
		pre.setString(16, vehicleNo);//车牌号
		if(null == isJp){//是否集配
			pre.setString(17,null);
		}else{
			pre.setString(17,isJp == true ? "Y" : "N" );
		}
		pre.setString(18, batch);//批次
		pre.setString(19, station);//工位
		pre.setString(20, applianceType);//器具类型
		pre.setString(21, applianceName);//器具名称
		pre.setInt(22, applianceAmount);//满载量
		pre.setInt(23, size);//器具个数
		pre.setString(24, remark);//备注
		pre.setString(25, "1");//状态
		pre.executeUpdate();
	}
	public void outBoundLotToMes(Long id){
		WmsBOL bol = commonDao.load(WmsBOL.class, id);
		if(bol!=null){
			Connection connection = null;
			PreparedStatement pre = null;
			try {
				connection = getConnection();
				if(null == connection){//处理装车单第一条数据的时候获取连接,处理第二条数据的时候就不用再获取连接了
					throw new BusinessException("数据库连接失败,请检查!!");
				}
				String insertSql = insertMesSql();
				String hqlPP = "FROM WmsPickTicketDetail pp WHERE pp.lotPickCode =:lotPickCode" +
						" AND pp.item.code =:item AND pp.supplier.code =:supplier";
				//根据当前发运明细数据,通过获取发货单号+物料编码+供应商编码,找到原始的发货单信息
				String hql = "FROM WmsBOLDetail bd WHERE bd.bol.id =:bolId ";
				List<WmsBOLDetail> details = commonDao.findByQuery(hql, "bolId", bol.getId());
				int i = 1; //数据条数标识
				String supplyCode = "",itemCode="";
				Double avaliableQty = 0D,ppQty = 0D,shipQuantityBU = 0D;
				for(WmsBOLDetail detail : details){
					avaliableQty = detail.getQuantityBU();
					supplyCode = detail.getItemKey().getLotInfo().getSupplier().getCode();
					itemCode = detail.getItemKey().getItem().getCode();
					List<WmsPickTicketDetail> pps = commonDao.findByQuery(hqlPP,new String[]{"lotPickCode","item","supplier"},
							new Object[]{bol.getPickCode(),itemCode,supplyCode});

					pre =connection.prepareStatement(insertSql);
					for(WmsPickTicketDetail pp : pps){
						ppQty = pp.getExpectedQuantityBU();
						shipQuantityBU = avaliableQty>ppQty?ppQty:avaliableQty;
						insertMes(pre, detail.getSubCode(), pp.getPickTicket().getRelatedBill1(), WmsMoveDocType.LOT_PICKING, i, 
								detail.getRequireArriveDate(), supplyCode, itemCode, ppQty, shipQuantityBU, 
								pp.getPickTicket().getCompany().getCode(), pp.getPickTicket().getReceiveHouse(), 
								pp.getProductionLine(), pp.getPickTicket().getReceiveDoc(), "SL011", 
								detail.getBol().getVehicleNo(), pp.getIsJp(), pp.getPickTicket().getBatch(),  
								pp.getStation(),"散件","散件", 2147483647, 1, pp.getRemark());
						pp.ship(shipQuantityBU);
						commonDao.store(pp);
						avaliableQty -= shipQuantityBU;
						if(avaliableQty<=0){
							break;
						}
					}
					i++;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
				throw new BusinessException(e.getMessage());
			}finally{
				closeConnection(null, pre, connection);
			}
		}else{
			System.out.println("WmsBOL is null id = "+ id);
		}
	}
	List<WmsMoveDocAndStation> getmoveDocAndStation(Long moveDocDetailId){
		String hql = "from WmsMoveDocAndStation s where s.moveDocDetail.id=:id";
		List<WmsMoveDocAndStation> list = commonDao.findByQuery(hql,"id",moveDocDetailId);
		if(list.size() > 0){
			return list;
		}
		return null;
	}
	
	public void outBoundApplianceToMes(WmsMoveDocDetail moveDocDetail,String subCode,
							String mesCode,String billCode,Connection connection,
							PreparedStatement pre,String supplyCode,String itemCode,
							Double qty,String productionLine,String station,String remark,
							List<WmsMoveDocAndStation> mas) {
		String insertSql = "insert into "+MiddleTableName.W_APPLIANCE_MES +" (ID,BOL_NO,LINE_NO,"
							+ "ODR_NO,APPLIANCE_NO,DELIVER_DATE,SUPPLY_NO,APPLIANCE_TYPE,APPLIANCE_NAME,"
							+ "APPLIANCE_AMOUNT,ITEM_CODE,ORDER_QTY,QTY,SEQ,PRODUCT_LINE,STATION,CUR_PAGE,"
							+ "TOTAL_PAGE,REMARK,ODR_TYPE) values(wseq_W_APPLIANCE_MES.nextval,?,?,?,?,?,?,?,?,"
							+ "?,?,?,?,?,?,?,?,?,?,?)";
		try {
			int j = 1;
			for(WmsMoveDocAndStation w : mas){
				pre = connection.prepareStatement(insertSql);
				pre.setString(1, subCode);//子单号
				pre.setInt(2, 10 * j);//行号
				pre.setString(3, mesCode);//MES料单号
				pre.setString(4,w.getBoxTag());//器具标签
//				pre.setDate(5, new java.sql.Date(new Date().getTime()));//发运日期
				pre.setTimestamp(5, Timestamp.valueOf(DateUtil.formatDateToStr(new Date())));
				pre.setString(6, supplyCode);//供应商编码
				pre.setString(7, w.getType());//器具型号
				pre.setString(8, null);//器具名称
				pre.setInt(9, w.getLoadage());//满载量
				pre.setString(10,itemCode);//货品编码
				pre.setDouble(11,qty);//发货单数量
				pre.setDouble(12,w.getPickQuantity());//QTY
				pre.setDouble(13,w.getSeq() == null ? 0 : w.getSeq());//排序号
				pre.setString(14,productionLine);//生产线
				pre.setString(15,station);//工位
				pre.setInt(16, w.getCurPag() == null ? 0 : w.getCurPag());//当前页数
				pre.setInt(17, w.getTotalPage() == null ? 0 : w.getTotalPage());//总页数
				pre.setString(18,getMayNullData1(remark));//备注
				pre.setString(19,billCode);//单据类型
				pre.executeUpdate();
				j += 1;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		logger.error("-----出库器具"+mas.size()+"数据传MES结束-----");
	}
	UpdateInfo newUpdateInfo(){
		UpdateInfo ui = new UpdateInfo();
		ui.setCreatedTime(new Date());
		ui.setCreator("admin");
		ui.setLastOperator("admin");
		return ui;
	}
	
	String keepLogLength(List<String> log){
		String logStr = "";
		if(log.toString().length() > 1000){
			logStr = log.toString().substring(0, 1000);
		}else{
			return log.toString();
		}
		return logStr;
	}
	@Override
	public void cancelQuality(String sql) {
		int line = jdbcTemplateExt1.update(sql);
		if(line <= 0){
			throw new BusinessException("当前质检单已开始作业或找不到数据,无法取消");
		}
	}
}
