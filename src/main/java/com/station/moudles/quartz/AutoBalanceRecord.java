package com.station.moudles.quartz;

import java.math.BigDecimal;
import java.util.Date;

public class AutoBalanceRecord {
	private String gprsId;
	
	private Date dischargeStartTime; 
	
	private Date dischargeEndTime;
	
	private BigDecimal dischargeStartVol;
	
	private BigDecimal dischargeEndVol;
	
	private Integer startRecordID;
	
	private Integer endRecoredID;
	
	//是否需要自动均衡
	private Boolean autoBalance;
	
	private Date balanceStartTime;
	
	private Date balanceEndTime;
	
	private Integer newCellCount;
	
	public String getGprsId() {
		return gprsId;
	}

	public void setGprsId(String gprsId) {
		this.gprsId = gprsId;
	}

	public Date getDischargeStartTime() {
		return dischargeStartTime;
	}

	public void setDischargeStartTime(Date dischargeStartTime) {
		this.dischargeStartTime = dischargeStartTime;
	}

	public Date getBalanceStartTime() {
		return balanceStartTime;
	}

	public void setBalanceStartTime(Date balanceStartTime) {
		this.balanceStartTime = balanceStartTime;
	}

	public Date getBalanceEndTime() {
		return balanceEndTime;
	}

	public void setBalanceEndTime(Date balanceEndTime) {
		this.balanceEndTime = balanceEndTime;
	}

	public Integer getNewCellCount() {
		return newCellCount;
	}

	public void setNewCellCount(Integer newCellCount) {
		this.newCellCount = newCellCount;
	}

	public Date getDischargeEndTime() {
		return dischargeEndTime;
	}

	public void setDischargeEndTime(Date dischargeEndTime) {
		this.dischargeEndTime = dischargeEndTime;
	}

	public Boolean getAutoBalance() {
		return autoBalance;
	}

	public void setAutoBalance(Boolean autoBalance) {
		this.autoBalance = autoBalance;
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

	public Integer getStartRecordID() {
		return startRecordID;
	}

	public void setStartRecordID(Integer startRecordID) {
		this.startRecordID = startRecordID;
	}

	public Integer getEndRecoredID() {
		return endRecoredID;
	}

	public void setEndRecoredID(Integer endRecoredID) {
		this.endRecoredID = endRecoredID;
	}

}
