package com.station.moudles.vo.report;

import java.math.BigDecimal;

/**
 * Created by Jack on 9/22/2017.
 */
public class SuggestionReportItem {
    private String companyName;
    private String stationName;
    private String carrier;
    private String maintainanceId;
    private String gprsId;
    private String deviceType;
    private String suggestion1;
    private String suggestion2;
    private String address;
    private String remark;
    //放电开始电压
    private BigDecimal dischargeStartVol;
    //放电结束电压
    private BigDecimal dischargeEndVol ;
    // 放电时长
    private BigDecimal dischargeTime;
    //放电开始时间
    private String dischargeStartTime;
    //放电结束时间
    private String dischargeEndTime;
    //负载电流
    private BigDecimal loadCurrent;
	//更换电池数量
	private Integer replaceNum ;
	//巡检标记单体
	private String signCells;
	//内阻过大单体
	private String  resistHighCells;
	//整治方式
	private String rectificationMode;
    

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getMaintainanceId() {
		return maintainanceId;
	}

	public void setMaintainanceId(String maintainanceId) {
		this.maintainanceId = maintainanceId;
	}

	public String getGprsId() {
        return gprsId;
    }

    public void setGprsId(String gprsId) {
        this.gprsId = gprsId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getSuggestion1() {
        return suggestion1;
    }

    public void setSuggestion1(String suggestion1) {
        this.suggestion1 = suggestion1;
    }

    public String getSuggestion2() {
        return suggestion2;
    }

    public void setSuggestion2(String suggestion2) {
        this.suggestion2 = suggestion2;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public BigDecimal getDischargeStartVol() {
		return dischargeStartVol;
	}

	public void setDischargeStartVol(BigDecimal dischargeStartVol) {
		this.dischargeStartVol = dischargeStartVol;
	}

	public BigDecimal getDischargeEndVol() {
		return dischargeEndVol;
	}

	public void setDischargeEndVol(BigDecimal dischargeEndVol) {
		this.dischargeEndVol = dischargeEndVol;
	}

	public BigDecimal getDischargeTime() {
		return dischargeTime;
	}

	public void setDischargeTime(BigDecimal dischargeTime) {
		this.dischargeTime = dischargeTime;
	}

	public String getDischargeStartTime() {
		return dischargeStartTime;
	}

	public void setDischargeStartTime(String dischargeStartTime) {
		this.dischargeStartTime = dischargeStartTime;
	}

	public String getDischargeEndTime() {
		return dischargeEndTime;
	}

	public void setDischargeEndTime(String dischargeEndTime) {
		this.dischargeEndTime = dischargeEndTime;
	}

	public BigDecimal getLoadCurrent() {
		return loadCurrent;
	}

	public void setLoadCurrent(BigDecimal loadCurrent) {
		this.loadCurrent = loadCurrent;
	}

	public Integer getReplaceNum() {
		return replaceNum;
	}

	public void setReplaceNum(Integer replaceNum) {
		this.replaceNum = replaceNum;
	}

	public String getSignCells() {
		return signCells;
	}

	public void setSignCells(String signCells) {
		this.signCells = signCells;
	}

	public String getResistHighCells() {
		return resistHighCells;
	}

	public void setResistHighCells(String resistHighCells) {
		this.resistHighCells = resistHighCells;
	}

	public String getRectificationMode() {
		return rectificationMode;
	}

	public void setRectificationMode(String rectificationMode) {
		this.rectificationMode = rectificationMode;
	}
}
