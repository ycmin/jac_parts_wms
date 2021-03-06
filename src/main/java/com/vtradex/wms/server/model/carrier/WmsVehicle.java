package com.vtradex.wms.server.model.carrier;

import java.util.Date;
import java.util.Set;

import com.vtradex.thorn.server.model.Entity;
import com.vtradex.wms.server.model.base.BaseStatus;

public class WmsVehicle extends Entity{

	private static final long serialVersionUID = 4147980837661001757L;
	
	/**
	 * 车牌号
	 */
	private String license;
	/**
	 * 挂车
	 */
	private WmsVehicle trailerLicense;
	/**
	 * 车型类别
	 */
	private WmsVehicleType vehicleType;
	/**
	 * 车籍
	 */
	private String origin;
	/**
	 * 品牌
	 */
	private String brands;
	/**
	 * 底盘号
	 */
	private String chassis;
	/**
	 * 车架号
	 */
	private String underpanNo;
	/**
	 * 长
	 */
	private double length;
	/**
	 * 宽
	 */
	private double width;
	/**
	 * 高
	 */
	private double height;
	/**
	 * GPS设备号
	 */
	private String gps;
	/**
	 * 百公里油耗
	 */
	private double fuelConsumption=0.0;
	/**
	 * 主司机
	 */
	private WmsDriver masterDriver;
	/**
	 * 副司机
	 */
	private WmsDriver minorDriver;
	/**
	 * 制造日期
	 */
	private Date produceDate;
	/**
	 * 购买日期
	 */
	private Date purchaseDate;
	/**
	 * 注册日期
	 */
	private Date registrationDate;
	/**
	 * 强制报销日期
	 */
	private Date reimbursementDate;
	/**
	 * 常驻城市
	 */
	private WmsCity cityResident;
	/**
	 * 当前位置
	 */
	private String location;
	/**
	 * 可用城市
	 */
	private WmsCity avaliableCity;
	/**
	 * 预期可用时间
	 */
	private Date expectAvaliableTime;
	/**
	 * 车辆状态
     * 可用、在途、维修、其他
     * {@link TmsVehicleStatus}
	 */
	private String vehicleStatus = WmsVehicleStatus.ENABLE;
	
	/**
	 * 任务状态
	 */
	private Boolean beShipment=false;
	/**
	 * 是否挂车
	 */
	private Boolean beTrailer=false;
	
	/**
	 * 挂车轴数
	 */
	private Integer axles;
	/**
	 * 行驶证号
	 */
	private String drivingLicense;
	/**
	 * 车龄
	 */
	private String carAge;
	/**
	 * 整备质量(T)
	 */
	private double curbWeight;
	/**
	 * 核载质量(T)
	 */
	private double coreSetTotalMass;
	/**
	 * 挂型(T)
	 */
	private String hangingType;
	/**
	 * 合作起始时间
	 */
	private Date copStartDate;
	/**
	 * 发动机号
	 */
	private String engineNumber;
	/**
	 * 车主姓名
	 */
	private String carOwnersName;
	/**
	 * 车主身份证号
	 */
	private String carOwnersId;
	/**
	 * 挂靠单位
	 */
	private String carOwnersCompany;
	/**
	 * 家庭住址
	 */
	private String carOwnersAdd;
	/**
	 * 住宅电话
	 */
	private String carOwnersTel;
	/**
	 * 移动电话
	 */
	private String carOwnersMob;
	/**
	  * {@link BaseStatus}
	 * modify:此字段更改为状态
	 */
	private String status = BaseStatus.ENABLED;
	/**
	 * 描述
	 */
	private String description;
	
	/**明细
	 *  附件*/
	private Set<WmsVehicleDetail> details;

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public WmsVehicle getTrailerLicense() {
		return trailerLicense;
	}

	public void setTrailerLicense(WmsVehicle trailerLicense) {
		this.trailerLicense = trailerLicense;
	}

	public WmsVehicleType getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(WmsVehicleType vehicleType) {
		this.vehicleType = vehicleType;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getBrands() {
		return brands;
	}

	public void setBrands(String brands) {
		this.brands = brands;
	}

	public String getChassis() {
		return chassis;
	}

	public void setChassis(String chassis) {
		this.chassis = chassis;
	}

	public String getUnderpanNo() {
		return underpanNo;
	}

	public void setUnderpanNo(String underpanNo) {
		this.underpanNo = underpanNo;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public String getGps() {
		return gps;
	}

	public void setGps(String gps) {
		this.gps = gps;
	}

	public double getFuelConsumption() {
		return fuelConsumption;
	}

	public void setFuelConsumption(double fuelConsumption) {
		this.fuelConsumption = fuelConsumption;
	}

	public WmsDriver getMasterDriver() {
		return masterDriver;
	}

	public void setMasterDriver(WmsDriver masterDriver) {
		this.masterDriver = masterDriver;
	}

	public WmsDriver getMinorDriver() {
		return minorDriver;
	}

	public void setMinorDriver(WmsDriver minorDriver) {
		this.minorDriver = minorDriver;
	}

	public Date getProduceDate() {
		return produceDate;
	}

	public void setProduceDate(Date produceDate) {
		this.produceDate = produceDate;
	}

	public Date getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(Date purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public Date getReimbursementDate() {
		return reimbursementDate;
	}

	public void setReimbursementDate(Date reimbursementDate) {
		this.reimbursementDate = reimbursementDate;
	}

	public WmsCity getCityResident() {
		return cityResident;
	}

	public void setCityResident(WmsCity cityResident) {
		this.cityResident = cityResident;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public WmsCity getAvaliableCity() {
		return avaliableCity;
	}

	public void setAvaliableCity(WmsCity avaliableCity) {
		this.avaliableCity = avaliableCity;
	}

	public Date getExpectAvaliableTime() {
		return expectAvaliableTime;
	}

	public void setExpectAvaliableTime(Date expectAvaliableTime) {
		this.expectAvaliableTime = expectAvaliableTime;
	}

	public String getVehicleStatus() {
		return vehicleStatus;
	}

	public void setVehicleStatus(String vehicleStatus) {
		this.vehicleStatus = vehicleStatus;
	}

	public Boolean getBeShipment() {
		return beShipment;
	}

	public void setBeShipment(Boolean beShipment) {
		this.beShipment = beShipment;
	}

	public Boolean getBeTrailer() {
		return beTrailer;
	}

	public void setBeTrailer(Boolean beTrailer) {
		this.beTrailer = beTrailer;
	}

	public Integer getAxles() {
		return axles;
	}

	public void setAxles(Integer axles) {
		this.axles = axles;
	}

	public String getDrivingLicense() {
		return drivingLicense;
	}

	public void setDrivingLicense(String drivingLicense) {
		this.drivingLicense = drivingLicense;
	}

	public String getCarAge() {
		return carAge;
	}

	public void setCarAge(String carAge) {
		this.carAge = carAge;
	}

	public double getCurbWeight() {
		return curbWeight;
	}

	public void setCurbWeight(double curbWeight) {
		this.curbWeight = curbWeight;
	}

	public double getCoreSetTotalMass() {
		return coreSetTotalMass;
	}

	public void setCoreSetTotalMass(double coreSetTotalMass) {
		this.coreSetTotalMass = coreSetTotalMass;
	}

	public String getHangingType() {
		return hangingType;
	}

	public void setHangingType(String hangingType) {
		this.hangingType = hangingType;
	}

	public Date getCopStartDate() {
		return copStartDate;
	}

	public void setCopStartDate(Date copStartDate) {
		this.copStartDate = copStartDate;
	}

	public String getEngineNumber() {
		return engineNumber;
	}

	public void setEngineNumber(String engineNumber) {
		this.engineNumber = engineNumber;
	}

	public String getCarOwnersName() {
		return carOwnersName;
	}

	public void setCarOwnersName(String carOwnersName) {
		this.carOwnersName = carOwnersName;
	}

	public String getCarOwnersId() {
		return carOwnersId;
	}

	public void setCarOwnersId(String carOwnersId) {
		this.carOwnersId = carOwnersId;
	}

	public String getCarOwnersCompany() {
		return carOwnersCompany;
	}

	public void setCarOwnersCompany(String carOwnersCompany) {
		this.carOwnersCompany = carOwnersCompany;
	}

	public String getCarOwnersAdd() {
		return carOwnersAdd;
	}

	public void setCarOwnersAdd(String carOwnersAdd) {
		this.carOwnersAdd = carOwnersAdd;
	}

	public String getCarOwnersTel() {
		return carOwnersTel;
	}

	public void setCarOwnersTel(String carOwnersTel) {
		this.carOwnersTel = carOwnersTel;
	}

	public String getCarOwnersMob() {
		return carOwnersMob;
	}

	public void setCarOwnersMob(String carOwnersMob) {
		this.carOwnersMob = carOwnersMob;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<WmsVehicleDetail> getDetails() {
		return details;
	}

	public void setDetails(Set<WmsVehicleDetail> details) {
		this.details = details;
	}
	
}
