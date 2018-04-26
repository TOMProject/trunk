package com.station.moudles.vo.search;

import java.math.BigDecimal;
import java.util.Date;

public class SearchDischargeManualRecordPagingVo extends PageEntity{
	/**
     * @Fields id 
     */
    private Integer id;
    /**
     * @Fields district 区域
     */
    private String district;
    /**
     * @Fields addressCoding 站址编码
     */
    private String addressCoding;
    /**
     * @Fields maintainanceId 运维id
     */
    private String maintainanceId;
    /**
     * @Fields addressName 站名
     */
    private String addressName;
    /**
     * @Fields gprsId 设备id
     */
    private String gprsId;
    /**
     * @Fields dischargeStartTime 开始放电时间
     */
    private Date dischargeStartTime;
    /**
     * @Fields dischargeEndTime 放电结束时间
     */
    private Date dischargeEndTime;
    /**
     * @Fields dischargeForwordVol 放电前电压
     */
    private BigDecimal dischargeForwordVol;
    /**
     * @Fields dischargeForwordCur 放电前电流
     */
    private BigDecimal dischargeForwordCur;
    /**
     * @Fields dischargeForwordSystemCur 放电前系统电流
     */
    private BigDecimal dischargeForwordSystemCur;
    /**
     * @Fields dischargeForwordSystemVol 放电前系统电压
     */
    private BigDecimal dischargeForwordSystemVol;
    /**
     * @Fields dischargeBackVol 放电截止电压
     */
    private BigDecimal dischargeBackVol;
    /**
     * @Fields dischargeBackCur 放电截止电流
     */
    private BigDecimal dischargeBackCur;
    /**
     * @Fields dischargeBackSystemCur 放电截止系统电流
     */
    private BigDecimal dischargeBackSystemCur;
    /**
     * @Fields dischargeBackSystemVol 放电截止系统电压
     */
    private BigDecimal dischargeBackSystemVol;
    /**
     * @Fields dischargeTime 放电时长
     */
    private BigDecimal dischargeTime;
    /**
     * @Fields remark 备注
     */
    private String remark;
    /**
     * @Fields installTime 安装时间
     */
    private Date installTime;
    /**
     * @Fields cellPlant 电池品牌
     */
    private String cellPlant;
    /**
     * @Fields cellType 电池类型
     */
    private String cellType;
    /**
     * @Fields reportHistory 出过历史整治报告
     */
    private String reportHistory;
    /**
     * @Fields isProcessed 是否出具整治报告，1是，0否
     */
    private Integer isProcessed;
    /**
     * @Fields reportFileName 报告文件名称
     */
    private String reportFileName;
    /**
     * @Fields reportRemark 整治备注
     */
    private String reportRemark;
    /**
     * @Fields companyName3 三级公司名称
     */
    private String companyName3;
    /**
     * @Fields 放电人员
     */
    private String dischargePerson;
    /**
     * @Fields companyId3 三级公司id
     */
    private Integer companyId;
	//负载电流
	private BigDecimal loadCurrent;
	//base表的区域
	private String baseDistrict;
    //base表的city
	private String city;
	//base表的省
	private String province;
	
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district == null ? null : district.trim();
    }

    public String getAddressCoding() {
        return addressCoding;
    }

    public void setAddressCoding(String addressCoding) {
        this.addressCoding = addressCoding == null ? null : addressCoding.trim();
    }

    public String getMaintainanceId() {
        return maintainanceId;
    }

    public void setMaintainanceId(String maintainanceId) {
        this.maintainanceId = maintainanceId == null ? null : maintainanceId.trim();
    }

    public String getAddressName() {
        return addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName == null ? null : addressName.trim();
    }

    public String getGprsId() {
        return gprsId;
    }

    public void setGprsId(String gprsId) {
        this.gprsId = gprsId == null ? null : gprsId.trim();
    }

    public Date getDischargeStartTime() {
        return dischargeStartTime;
    }

    public void setDischargeStartTime(Date dischargeStartTime) {
        this.dischargeStartTime = dischargeStartTime;
    }

    public Date getDischargeEndTime() {
        return dischargeEndTime;
    }

    public void setDischargeEndTime(Date dischargeEndTime) {
        this.dischargeEndTime = dischargeEndTime;
    }

    public BigDecimal getDischargeForwordVol() {
        return dischargeForwordVol;
    }

    public void setDischargeForwordVol(BigDecimal dischargeForwordVol) {
        this.dischargeForwordVol = dischargeForwordVol;
    }

    public BigDecimal getDischargeForwordCur() {
        return dischargeForwordCur;
    }

    public void setDischargeForwordCur(BigDecimal dischargeForwordCur) {
        this.dischargeForwordCur = dischargeForwordCur;
    }

    public BigDecimal getDischargeForwordSystemCur() {
        return dischargeForwordSystemCur;
    }

    public void setDischargeForwordSystemCur(BigDecimal dischargeForwordSystemCur) {
        this.dischargeForwordSystemCur = dischargeForwordSystemCur;
    }

    public BigDecimal getDischargeForwordSystemVol() {
        return dischargeForwordSystemVol;
    }

    public void setDischargeForwordSystemVol(BigDecimal dischargeForwordSystemVol) {
        this.dischargeForwordSystemVol = dischargeForwordSystemVol;
    }

    public BigDecimal getDischargeBackVol() {
        return dischargeBackVol;
    }

    public void setDischargeBackVol(BigDecimal dischargeBackVol) {
        this.dischargeBackVol = dischargeBackVol;
    }

    public BigDecimal getDischargeBackCur() {
        return dischargeBackCur;
    }

    public void setDischargeBackCur(BigDecimal dischargeBackCur) {
        this.dischargeBackCur = dischargeBackCur;
    }

    public BigDecimal getDischargeBackSystemCur() {
        return dischargeBackSystemCur;
    }

    public void setDischargeBackSystemCur(BigDecimal dischargeBackSystemCur) {
        this.dischargeBackSystemCur = dischargeBackSystemCur;
    }

    public BigDecimal getDischargeBackSystemVol() {
        return dischargeBackSystemVol;
    }

    public void setDischargeBackSystemVol(BigDecimal dischargeBackSystemVol) {
        this.dischargeBackSystemVol = dischargeBackSystemVol;
    }

    public BigDecimal getDischargeTime() {
        return dischargeTime;
    }

    public void setDischargeTime(BigDecimal dischargeTime) {
        this.dischargeTime = dischargeTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Date getInstallTime() {
        return installTime;
    }

    public void setInstallTime(Date installTime) {
        this.installTime = installTime;
    }

    public String getCellPlant() {
        return cellPlant;
    }

    public void setCellPlant(String cellPlant) {
        this.cellPlant = cellPlant == null ? null : cellPlant.trim();
    }

    public String getCellType() {
        return cellType;
    }

    public void setCellType(String cellType) {
        this.cellType = cellType == null ? null : cellType.trim();
    }

    public String getReportHistory() {
        return reportHistory;
    }

    public void setReportHistory(String reportHistory) {
        this.reportHistory = reportHistory == null ? null : reportHistory.trim();
    }

    public Integer getIsProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(Integer isProcessed) {
        this.isProcessed = isProcessed;
    }

    public String getReportFileName() {
        return reportFileName;
    }

    public void setReportFileName(String reportFileName) {
        this.reportFileName = reportFileName == null ? null : reportFileName.trim();
    }

    public String getReportRemark() {
        return reportRemark;
    }

    public void setReportRemark(String reportRemark) {
        this.reportRemark = reportRemark == null ? null : reportRemark.trim();
    }

    public String getCompanyName3() {
        return companyName3;
    }

    public void setCompanyName3(String companyName3) {
        this.companyName3 = companyName3 == null ? null : companyName3.trim();
    }

	public Integer getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}

	public BigDecimal getLoadCurrent() {
		return loadCurrent;
	}

	public void setLoadCurrent(BigDecimal loadCurrent) {
		this.loadCurrent = loadCurrent;
	}

	public String getDischargePerson() {
		return dischargePerson;
	}

	public void setDischargePerson(String dischargePerson) {
		this.dischargePerson = dischargePerson;
	}

	public String getBaseDistrict() {
		return baseDistrict;
	}

	public void setBaseDistrict(String baseDistrict) {
		this.baseDistrict = baseDistrict;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}
	
	
}
