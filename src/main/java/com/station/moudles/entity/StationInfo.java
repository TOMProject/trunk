package com.station.moudles.entity;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * This class was generated by Bill Generator. This class corresponds to the
 * database table base_station_info
 *
 * @zdmgenerated 2017-18-29 03:18
 */
@ApiModel(value = "StationInfo", description = "StationInfo描述")
public class StationInfo {
	/**
	 * This field corresponds to the database column base_station_info.id
	 */
	@ApiModelProperty(value = "pk", required = true)
	private Integer id;
	/**
	 * This field corresponds to the database column base_station_info.gprs_id
	 * 基站gprs ID
	 */
	@ApiModelProperty(value = "基站gprs ID", example = "基站gprs ID", required = false)
	private String gprsId;
	/**
	 * This field corresponds to the database column base_station_info.gprs_id_out
	 */
	@ApiModelProperty(value = "gprsIdOut", example = "gprsIdOut", required = false)
	private String gprsIdOut;
	/**
	 * This field corresponds to the database column base_station_info.name 基站名称
	 */
	@ApiModelProperty(value = "基站名称", example = "基站名称", required = false)
	private String name;
	/**
	 * This field corresponds to the database column base_station_info.address 基站地址
	 */
	@ApiModelProperty(value = "基站地址", example = "基站地址", required = false)
	private String address;
	/**
	 * This field corresponds to the database column base_station_info.province 省
	 */
	@ApiModelProperty(value = "省", example = "省", required = false)
	private String province;
	/**
	 * This field corresponds to the database column base_station_info.city 市
	 */
	@ApiModelProperty(value = "市", example = "市", required = false)
	private String city;
	/**
	 * This field corresponds to the database column base_station_info.district 区
	 */
	@ApiModelProperty(value = "区", example = "区", required = false)
	private String district;
	/**
	 * This field corresponds to the database column base_station_info.lat 纬度 -90 to
	 * +90 (degrees)
	 */
	@ApiModelProperty(value = "纬度 -90 to +90 (degrees)", required = false)
	private BigDecimal lat;
	/**
	 * This field corresponds to the database column base_station_info.lng 经度 -180
	 * to +180 (degrees)
	 */
	@ApiModelProperty(value = "经度  -180 to +180 (degrees)", required = false)
	private BigDecimal lng;
	/**
	 * This field corresponds to the database column
	 * base_station_info.maintainance_id 运维ID
	 */
	@ApiModelProperty(value = "运维ID", example = "运维ID", required = false)
	private String maintainanceId;
	/**
	 * This field corresponds to the database column base_station_info.pack_type
	 * 电池组类型
	 */
	@ApiModelProperty(value = "电池组类型", example = "电池组类型", required = false)
	private String packType;
	/**
	 * This field corresponds to the database column base_station_info.room_type
	 * 机房类型
	 */
	@ApiModelProperty(value = "机房类型", example = "机房类型", required = false)
	private String roomType;
	/**
	 * This field corresponds to the database column base_station_info.duration
	 * 预测时长，单位分钟
	 */
	@ApiModelProperty(value = "预测时长，单位分钟", required = false)
	private BigDecimal duration;
	/**
	 * This field corresponds to the database column base_station_info.real_duration
	 * 剩余时长实时预测，单位分钟
	 */
	@ApiModelProperty(value = "剩余时长实时预测，单位分钟", required = false)
	private BigDecimal realDuration;
	/**
	 * This field corresponds to the database column base_station_info.ok_num 正常数量
	 */
	@ApiModelProperty(value = "正常数量", required = false)
	private Integer okNum;
	/**
	 * This field corresponds to the database column base_station_info.poor_num 较差数量
	 */
	@ApiModelProperty(value = "较差数量", required = false)
	private Integer poorNum;
	/**
	 * This field corresponds to the database column base_station_info.error_num
	 * 故障数量
	 */
	@ApiModelProperty(value = "故障数量", required = false)
	private Integer errorNum;
	/**
	 * This field corresponds to the database column base_station_info.status 电池组性能
	 * 0:正常,1:较差, 2: 故障
	 */
	@ApiModelProperty(value = "电池组性能 0:正常,1:较差, 2: 故障", required = false)
	private Integer status;
	/**
	 * This field corresponds to the database column base_station_info.company_id1
	 * 一级公司id
	 */
	@ApiModelProperty(value = "一级公司id", required = true)
	private Integer companyId1;
	/**
	 * This field corresponds to the database column base_station_info.company_id2
	 * 二级公司id
	 */
	@ApiModelProperty(value = "二级公司id", required = false)
	private Integer companyId2;
	/**
	 * This field corresponds to the database column base_station_info.company_id3
	 * 三级公司id
	 */
	@ApiModelProperty(value = "三级公司id", required = false)
	private Integer companyId3;
	/**
	 * This field corresponds to the database column base_station_info.del_flag
	 * 0未删除，1删除
	 */
	@ApiModelProperty(value = "0未删除，1删除", required = false)
	private Integer delFlag;
	/**
	 * This field corresponds to the database column base_station_info.company_name3
	 */
	@ApiModelProperty(value = "companyName3", example = "companyName3", required = false)
	private String companyName3;
	/**
	 * This field corresponds to the database column base_station_info.vol_level
	 * 电压级别
	 */
	@ApiModelProperty(value = "电压级别", required = false)
	private Integer volLevel;
	/**
	 * This field corresponds to the database column base_station_info.operator_type
	 * 运营商类型,1移动，2联通，3电信
	 */
	@ApiModelProperty(value = "运营商类型,1移动，2联通，3电信", required = false)
	private Integer operatorType;
	/**
	 * This field corresponds to the database column
	 * base_station_info.duration_status 1优2良3中4差
	 */
	@ApiModelProperty(value = "1优2良3中4差", required = false)
	private Integer durationStatus;
	@ApiModelProperty(value = "电池单体品牌，冗余", required = false)
	private String cellPlant;
	@ApiModelProperty(value = "设备在线状态,0离线,1在线", required = false)
	private Byte linkStatus;
	@ApiModelProperty(value = "设备类型，1蓄电池串联复用设备,2蓄电池串联复用诊断组件", required = false)
	private Integer deviceType;
	@ApiModelProperty(value = "负载功率，默认值2400W", required = false)
	private BigDecimal loadPower;
	@ApiModelProperty(value = "安装、维护流程，电池组状态，默认值0", required = false)
	private Integer inspectStatus;
	// ----10/22 add app端的新主机id
	private String newGprsIdOut;
	/**
	 * 巡检人员id
	 */
	@ApiModelProperty(value = "operateId", required = false)
	private Integer operateId;
	/**
	 * 巡检人员姓名
	 */
	@ApiModelProperty(value = "operateName", example = "operateName", required = false)
	private String operateName;

	// ----10/17 add 添加电话卡号属性
	@ApiModelProperty(value = "电话卡号", required = false)
	private String devicePhone;

	@ApiModelProperty(value = "主机端口", required = false)
	private String gprsPort;

	@ApiModelProperty(value = "主机规格", required = false)
	private String gprsSpec;
	// 站址编码
	@ApiModelProperty(value = "站址编码", required = false)
	private String addressCoding;

	@ApiModelProperty(value = "电池状态： 充电,放电,浮充", example = "电池状态： 充电,放电,浮充", required = false)
	private String state;
	// 总电压
	private BigDecimal genVol;
	// 总电流
	private BigDecimal genCur;
	//总单体数量
	private Integer cellCount;
	//负载电流
	private BigDecimal loadCurrent;

	public BigDecimal getLoadCurrent() {
		return loadCurrent;
	}

	public void setLoadCurrent(BigDecimal loadCurrent) {
		this.loadCurrent = loadCurrent;
	}

	public Integer getCellCount() {
		return cellCount;
	}

	public void setCellCount(Integer cellCount) {
		this.cellCount = cellCount;
	}

	public BigDecimal getGenVol() {
		return genVol;
	}

	public void setGenVol(BigDecimal genVol) {
		this.genVol = genVol;
	}

	public BigDecimal getGenCur() {
		return genCur;
	}

	public void setGenCur(BigDecimal genCur) {
		this.genCur = genCur;
	}

	public String getAddressCoding() {
		return addressCoding;
	}

	public void setAddressCoding(String addressCoding) {
		this.addressCoding = addressCoding;
	}

	public Integer getOperateId() {
		return operateId;
	}

	public void setOperateId(Integer operateId) {
		this.operateId = operateId;
	}

	public String getOperateName() {
		return operateName;
	}

	public void setOperateName(String operateName) {
		this.operateName = operateName;
	}

	public String getNewGprsIdOut() {
		return newGprsIdOut;
	}

	public void setNewGprsIdOut(String newGprsIdOut) {
		this.newGprsIdOut = newGprsIdOut;
	}

	public String getGprsPort() {
		return gprsPort;
	}

	public void setGprsPort(String gprsPort) {
		this.gprsPort = gprsPort;
	}

	public String getGprsSpec() {
		return gprsSpec;
	}

	public void setGprsSpec(String gprsSpec) {
		this.gprsSpec = gprsSpec;
	}

	public String getDevicePhone() {
		return devicePhone;
	}

	public void setDevicePhone(String devicePhone) {
		this.devicePhone = devicePhone;
	}

	// -----end
	private boolean isGprsIdNotNull = false;

	/**
	 * @return the isGprsIdNotNull
	 */
	public boolean isGprsIdNotNull() {
		return isGprsIdNotNull;
	}

	/**
	 * @param isGprsIdNotNull
	 *            the isGprsIdNotNull to set
	 */
	public void setGprsIdNotNull(boolean isGprsIdNotNull) {
		this.isGprsIdNotNull = isGprsIdNotNull;
	}

	/**
	 * @return the cellPlant
	 */
	public String getCellPlant() {
		return cellPlant;
	}

	/**
	 * @param cellPlant
	 *            the cellPlant to set
	 */
	public void setCellPlant(String cellPlant) {
		this.cellPlant = cellPlant;
	}

	/**
	 * @return the linkStatus
	 */
	public Byte getLinkStatus() {
		return linkStatus;
	}

	/**
	 * @param linkStatus
	 *            the linkStatus to set
	 */
	public void setLinkStatus(Byte linkStatus) {
		this.linkStatus = linkStatus;
	}

	/**
	 * @return the deviceType
	 */
	public Integer getDeviceType() {
		return deviceType;
	}

	/**
	 * @param deviceType
	 *            the deviceType to set
	 */
	public void setDeviceType(Integer deviceType) {
		this.deviceType = deviceType;
	}

	/**
	 * This method returns the value of the database column base_station_info.id
	 * 
	 * @return the value of base_station_info.id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * This method sets the value of the database column base_station_info.id
	 * 
	 * @param id
	 *            the value for base_station_info.id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.gprs_id 基站gprs ID
	 * 
	 * @return the value of base_station_info.gprs_id
	 */
	public String getGprsId() {
		return gprsId;
	}

	/**
	 * This method sets the value of the database column base_station_info.gprs_id
	 * 基站gprs ID
	 * 
	 * @param gprsId
	 *            the value for base_station_info.gprs_id
	 */
	public void setGprsId(String gprsId) {
		this.gprsId = gprsId == null ? null : gprsId.trim();
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.gprs_id_out
	 * 
	 * @return the value of base_station_info.gprs_id_out
	 */
	public String getGprsIdOut() {
		return gprsIdOut;
	}

	/**
	 * This method sets the value of the database column
	 * base_station_info.gprs_id_out
	 * 
	 * @param gprsIdOut
	 *            the value for base_station_info.gprs_id_out
	 */
	public void setGprsIdOut(String gprsIdOut) {
		this.gprsIdOut = gprsIdOut == null ? null : gprsIdOut.trim();
	}

	/**
	 * This method returns the value of the database column base_station_info.name
	 * 基站名称
	 * 
	 * @return the value of base_station_info.name
	 */
	public String getName() {
		return name;
	}

	/**
	 * This method sets the value of the database column base_station_info.name 基站名称
	 * 
	 * @param name
	 *            the value for base_station_info.name
	 */
	public void setName(String name) {
		this.name = name == null ? null : name.trim();
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.address 基站地址
	 * 
	 * @return the value of base_station_info.address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * This method sets the value of the database column base_station_info.address
	 * 基站地址
	 * 
	 * @param address
	 *            the value for base_station_info.address
	 */
	public void setAddress(String address) {
		this.address = address == null ? null : address.trim();
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.province 省
	 * 
	 * @return the value of base_station_info.province
	 */
	public String getProvince() {
		return province;
	}

	/**
	 * This method sets the value of the database column base_station_info.province
	 * 省
	 * 
	 * @param province
	 *            the value for base_station_info.province
	 */
	public void setProvince(String province) {
		this.province = province == null ? null : province.trim();
	}

	/**
	 * This method returns the value of the database column base_station_info.city 市
	 * 
	 * @return the value of base_station_info.city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * This method sets the value of the database column base_station_info.city 市
	 * 
	 * @param city
	 *            the value for base_station_info.city
	 */
	public void setCity(String city) {
		this.city = city == null ? null : city.trim();
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.district 区
	 * 
	 * @return the value of base_station_info.district
	 */
	public String getDistrict() {
		return district;
	}

	/**
	 * This method sets the value of the database column base_station_info.district
	 * 区
	 * 
	 * @param district
	 *            the value for base_station_info.district
	 */
	public void setDistrict(String district) {
		this.district = district == null ? null : district.trim();
	}

	/**
	 * This method returns the value of the database column base_station_info.lat 纬度
	 * -90 to +90 (degrees)
	 * 
	 * @return the value of base_station_info.lat
	 */
	public BigDecimal getLat() {
		return lat;
	}

	/**
	 * This method sets the value of the database column base_station_info.lat 纬度
	 * -90 to +90 (degrees)
	 * 
	 * @param lat
	 *            the value for base_station_info.lat
	 */
	public void setLat(BigDecimal lat) {
		this.lat = lat;
	}

	/**
	 * This method returns the value of the database column base_station_info.lng 经度
	 * -180 to +180 (degrees)
	 * 
	 * @return the value of base_station_info.lng
	 */
	public BigDecimal getLng() {
		return lng;
	}

	/**
	 * This method sets the value of the database column base_station_info.lng 经度
	 * -180 to +180 (degrees)
	 * 
	 * @param lng
	 *            the value for base_station_info.lng
	 */
	public void setLng(BigDecimal lng) {
		this.lng = lng;
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.maintainance_id 运维ID
	 * 
	 * @return the value of base_station_info.maintainance_id
	 */
	public String getMaintainanceId() {
		return maintainanceId;
	}

	/**
	 * This method sets the value of the database column
	 * base_station_info.maintainance_id 运维ID
	 * 
	 * @param maintainanceId
	 *            the value for base_station_info.maintainance_id
	 */
	public void setMaintainanceId(String maintainanceId) {
		this.maintainanceId = maintainanceId == null ? null : maintainanceId.trim();
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.pack_type 电池组类型
	 * 
	 * @return the value of base_station_info.pack_type
	 */
	public String getPackType() {
		return packType;
	}

	/**
	 * This method sets the value of the database column base_station_info.pack_type
	 * 电池组类型
	 * 
	 * @param packType
	 *            the value for base_station_info.pack_type
	 */
	public void setPackType(String packType) {
		this.packType = packType == null ? null : packType.trim();
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.room_type 机房类型
	 * 
	 * @return the value of base_station_info.room_type
	 */
	public String getRoomType() {
		return roomType;
	}

	/**
	 * This method sets the value of the database column base_station_info.room_type
	 * 机房类型
	 * 
	 * @param roomType
	 *            the value for base_station_info.room_type
	 */
	public void setRoomType(String roomType) {
		this.roomType = roomType == null ? null : roomType.trim();
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.duration 预测时长，单位分钟
	 * 
	 * @return the value of base_station_info.duration
	 */
	public BigDecimal getDuration() {
		return duration;
	}

	/**
	 * This method sets the value of the database column base_station_info.duration
	 * 预测时长，单位分钟
	 * 
	 * @param duration
	 *            the value for base_station_info.duration
	 */
	public void setDuration(BigDecimal duration) {
		this.duration = duration;
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.real_duration 剩余时长实时预测，单位分钟
	 * 
	 * @return the value of base_station_info.real_duration
	 */
	public BigDecimal getRealDuration() {
		return realDuration;
	}

	/**
	 * This method sets the value of the database column
	 * base_station_info.real_duration 剩余时长实时预测，单位分钟
	 * 
	 * @param realDuration
	 *            the value for base_station_info.real_duration
	 */
	public void setRealDuration(BigDecimal realDuration) {
		this.realDuration = realDuration;
	}

	/**
	 * This method returns the value of the database column base_station_info.ok_num
	 * 正常数量
	 * 
	 * @return the value of base_station_info.ok_num
	 */
	public Integer getOkNum() {
		return okNum;
	}

	/**
	 * This method sets the value of the database column base_station_info.ok_num
	 * 正常数量
	 * 
	 * @param okNum
	 *            the value for base_station_info.ok_num
	 */
	public void setOkNum(Integer okNum) {
		this.okNum = okNum;
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.poor_num 较差数量
	 * 
	 * @return the value of base_station_info.poor_num
	 */
	public Integer getPoorNum() {
		return poorNum;
	}

	/**
	 * This method sets the value of the database column base_station_info.poor_num
	 * 较差数量
	 * 
	 * @param poorNum
	 *            the value for base_station_info.poor_num
	 */
	public void setPoorNum(Integer poorNum) {
		this.poorNum = poorNum;
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.error_num 故障数量
	 * 
	 * @return the value of base_station_info.error_num
	 */
	public Integer getErrorNum() {
		return errorNum;
	}

	/**
	 * This method sets the value of the database column base_station_info.error_num
	 * 故障数量
	 * 
	 * @param errorNum
	 *            the value for base_station_info.error_num
	 */
	public void setErrorNum(Integer errorNum) {
		this.errorNum = errorNum;
	}

	/**
	 * This method returns the value of the database column base_station_info.status
	 * 电池组性能 0:正常,1:较差, 2: 故障
	 * 
	 * @return the value of base_station_info.status
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * This method sets the value of the database column base_station_info.status
	 * 电池组性能 0:正常,1:较差, 2: 故障
	 * 
	 * @param status
	 *            the value for base_station_info.status
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.company_id1 一级公司id
	 * 
	 * @return the value of base_station_info.company_id1
	 */
	public Integer getCompanyId1() {
		return companyId1;
	}

	/**
	 * This method sets the value of the database column
	 * base_station_info.company_id1 一级公司id
	 * 
	 * @param companyId1
	 *            the value for base_station_info.company_id1
	 */
	public void setCompanyId1(Integer companyId1) {
		this.companyId1 = companyId1;
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.company_id2 二级公司id
	 * 
	 * @return the value of base_station_info.company_id2
	 */
	public Integer getCompanyId2() {
		return companyId2;
	}

	/**
	 * This method sets the value of the database column
	 * base_station_info.company_id2 二级公司id
	 * 
	 * @param companyId2
	 *            the value for base_station_info.company_id2
	 */
	public void setCompanyId2(Integer companyId2) {
		this.companyId2 = companyId2;
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.company_id3 三级公司id
	 * 
	 * @return the value of base_station_info.company_id3
	 */
	public Integer getCompanyId3() {
		return companyId3;
	}

	/**
	 * This method sets the value of the database column
	 * base_station_info.company_id3 三级公司id
	 * 
	 * @param companyId3
	 *            the value for base_station_info.company_id3
	 */
	public void setCompanyId3(Integer companyId3) {
		this.companyId3 = companyId3;
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.del_flag 0未删除，1删除
	 * 
	 * @return the value of base_station_info.del_flag
	 */
	public Integer getDelFlag() {
		return delFlag;
	}

	/**
	 * This method sets the value of the database column base_station_info.del_flag
	 * 0未删除，1删除
	 * 
	 * @param delFlag
	 *            the value for base_station_info.del_flag
	 */
	public void setDelFlag(Integer delFlag) {
		this.delFlag = delFlag;
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.company_name3
	 * 
	 * @return the value of base_station_info.company_name3
	 */
	public String getCompanyName3() {
		return companyName3;
	}

	/**
	 * This method sets the value of the database column
	 * base_station_info.company_name3
	 * 
	 * @param companyName3
	 *            the value for base_station_info.company_name3
	 */
	public void setCompanyName3(String companyName3) {
		this.companyName3 = companyName3 == null ? null : companyName3.trim();
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.vol_level 电压级别
	 * 
	 * @return the value of base_station_info.vol_level
	 */
	public Integer getVolLevel() {
		return volLevel;
	}

	/**
	 * This method sets the value of the database column base_station_info.vol_level
	 * 电压级别
	 * 
	 * @param volLevel
	 *            the value for base_station_info.vol_level
	 */
	public void setVolLevel(Integer volLevel) {
		this.volLevel = volLevel;
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.operator_type 运营商类型,1移动，2联通，3电信
	 * 
	 * @return the value of base_station_info.operator_type
	 */
	public Integer getOperatorType() {
		return operatorType;
	}

	/**
	 * This method sets the value of the database column
	 * base_station_info.operator_type 运营商类型,1移动，2联通，3电信
	 * 
	 * @param operatorType
	 *            the value for base_station_info.operator_type
	 */
	public void setOperatorType(Integer operatorType) {
		this.operatorType = operatorType;
	}

	/**
	 * This method returns the value of the database column
	 * base_station_info.duration_status 1优2良3中4差
	 * 
	 * @return the value of base_station_info.duration_status
	 */
	public Integer getDurationStatus() {
		return durationStatus;
	}

	/**
	 * This method sets the value of the database column
	 * base_station_info.duration_status 1优2良3中4差
	 * 
	 * @param durationStatus
	 *            the value for base_station_info.duration_status
	 */
	public void setDurationStatus(Integer durationStatus) {
		this.durationStatus = durationStatus;
	}

	public BigDecimal getLoadPower() {
		return loadPower;
	}

	public void setLoadPower(BigDecimal loadPower) {
		this.loadPower = loadPower;
	}

	public Integer getInspectStatus() {
		return inspectStatus;
	}

	public void setInspectStatus(Integer inspectStatus) {
		this.inspectStatus = inspectStatus;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

}