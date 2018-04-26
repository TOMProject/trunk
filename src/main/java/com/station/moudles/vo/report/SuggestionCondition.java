package com.station.moudles.vo.report;

import java.math.BigDecimal;
import java.util.List;

import com.station.moudles.entity.PackDataInfo;

public class SuggestionCondition {
	private String gprsId;
	private boolean isValidCheck; // 是否为有效核容记录
	private BigDecimal loadCur; // 负载电流
	private BigDecimal endVol; // 截止电压
	private BigDecimal checkTime; // 核容放电时长
	private List<PackDataInfo> dischargeRecord; // 放电记录（此放电记录是过滤之后的）
	private BigDecimal dischargeTime; // 放电时长
	private Integer suggestNum; // 整治支数

	private String startTime; // 放电开始时间
	private String endTime; // 放电结束时间
	private BigDecimal startVol;
	private Integer dischargeAbstractId;

	public String getGprsId() {
		return gprsId;
	}

	public void setGprsId(String gprsId) {
		this.gprsId = gprsId;
	}

	public boolean isValidCheck() {
		return isValidCheck;
	}

	public void setValidCheck(boolean isValidCheck) {
		this.isValidCheck = isValidCheck;
	}

	public BigDecimal getLoadCur() {
		return loadCur;
	}

	public void setLoadCur(BigDecimal loadCur) {
		this.loadCur = loadCur;
	}

	public BigDecimal getEndVol() {
		return endVol;
	}

	public void setEndVol(BigDecimal endVol) {
		this.endVol = endVol;
	}

	public BigDecimal getCheckTime() {
		return checkTime;
	}

	public void setCheckTime(BigDecimal checkTime) {
		this.checkTime = checkTime;
	}

	public List<PackDataInfo> getDischargeRecord() {
		return dischargeRecord;
	}

	public void setDischargeRecord(List<PackDataInfo> dischargeRecord) {
		this.dischargeRecord = dischargeRecord;
	}

	public BigDecimal getDischargeTime() {
		return dischargeTime;
	}

	public void setDischargeTime(BigDecimal dischargeTime) {
		this.dischargeTime = dischargeTime;
	}

	public Integer getSuggestNum() {
		return suggestNum;
	}

	public void setSuggestNum(Integer suggestNum) {
		this.suggestNum = suggestNum;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public BigDecimal getStartVol() {
		return startVol;
	}

	public void setStartVol(BigDecimal startVol) {
		this.startVol = startVol;
	}

	public Integer getDischargeAbstractId() {
		return dischargeAbstractId;
	}

	public void setDischargeAbstractId(Integer dischargeAbstractId) {
		this.dischargeAbstractId = dischargeAbstractId;
	}
}
