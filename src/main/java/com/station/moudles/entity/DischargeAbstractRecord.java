/*
 * @ClassName DischargeAbstractRecord
 * @Description 
 * @version 1.0
 * @Date 2018-01-24 13:59:01
 */
package com.station.moudles.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
/**
 * 放电摘要记录
 */
public class DischargeAbstractRecord {
    /**
     * @Fields id 
     */
    private Integer id;
    /**
     * @Fields gprsId 
     */
    private String gprsId;
    /**
     * @Fields starttime 放电开始时间
     */
    private Date starttime;
    /**
     * @Fields endtime 放电结束时间
     */
    private Date endtime;
    /**
     * @Fields startVol 放电开始电压（取前5放电电压平均值）
     */
    private BigDecimal startVol;
    /**
     * @Fields endVol 放电结束电压（取最后5个放电电压平均值）
     */
    private BigDecimal endVol;
    /**
     * @Fields recordStartId 放电开始id,pack_data_info里面的id
     */
    private Integer recordStartId;
    /**
     * @Fields recordEndId 放电结束id,pack_data_info里面的id
     */
    private Integer recordEndId;
    /**
     * 总电流
     */
    private BigDecimal genCur;
    /**
     * 最大电流
     */
    private BigDecimal maxCur;
    /**
     * 放电时长
     */
    private BigDecimal dischargeTime;
    //均衡开始时间
    private Date balanceStartTime;
    //均衡结束时间
    private Date balanceEndTime;
    //是否需要自动均衡
  	private Boolean autoBalance;
  	//单体个数
  	private Integer newCellCount;
  	//核容放电详情
  	List<PackDataInfo> dischargeDetails;

    public BigDecimal getMaxCur() {
		return maxCur;
	}

	public void setMaxCur(BigDecimal maxCur) {
		this.maxCur = maxCur;
	}

	public Integer getNewCellCount() {
		return newCellCount;
	}

	public void setNewCellCount(Integer newCellCount) {
		this.newCellCount = newCellCount;
	}

	public Boolean getAutoBalance() {
		return autoBalance;
	}

	public void setAutoBalance(Boolean autoBalance) {
		this.autoBalance = autoBalance;
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

	public BigDecimal getGenCur() {
		return genCur;
	}

	public void setGenCur(BigDecimal genCur) {
		this.genCur = genCur;
	}

	public BigDecimal getDischargeTime() {
		return dischargeTime;
	}

	public void setDischargeTime(BigDecimal dischargeTime) {
		this.dischargeTime = dischargeTime;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGprsId() {
        return gprsId;
    }

    public void setGprsId(String gprsId) {
        this.gprsId = gprsId == null ? null : gprsId.trim();
    }

    public Date getStarttime() {
        return starttime;
    }

    public void setStarttime(Date starttime) {
        this.starttime = starttime;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }

    public BigDecimal getStartVol() {
        return startVol;
    }

    public void setStartVol(BigDecimal startVol) {
        this.startVol = startVol;
    }

    public BigDecimal getEndVol() {
        return endVol;
    }

    public void setEndVol(BigDecimal endVol) {
        this.endVol = endVol;
    }

    public Integer getRecordStartId() {
        return recordStartId;
    }

    public void setRecordStartId(Integer recordStartId) {
        this.recordStartId = recordStartId;
    }

    public Integer getRecordEndId() {
        return recordEndId;
    }

    public void setRecordEndId(Integer recordEndId) {
        this.recordEndId = recordEndId;
    }

	public List<PackDataInfo> getDischargeDetails() {
		return dischargeDetails;
	}

	public void setDischargeDetails(List<PackDataInfo> dischargeDetails) {
		this.dischargeDetails = dischargeDetails;
	}
    
    
}