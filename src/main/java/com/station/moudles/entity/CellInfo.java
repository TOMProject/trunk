package com.station.moudles.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.station.common.utils.MyDateUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * This class was generated by Bill Generator.
 * This class corresponds to the database table cell_info  单体电池信息表
 *
 * @zdmgenerated 2017-48-21 05:48
 */
@ApiModel(value = "单体电池信息表", description = "单体电池信息表描述")
public class CellInfo {
	/**
	 * This field corresponds to the database column cell_info.id  
	 */
	@ApiModelProperty(value = "pk", required = true)
	private Integer id;

	/**
	 * This field corresponds to the database column cell_info.gprs_id  
	 */
	@ApiModelProperty(value = "gprsId", example = "gprsId", required = false)
	private String gprsId;

	/**
	 * This field corresponds to the database column cell_info.cell_index  单体电池编号 1-24
	 */
	@ApiModelProperty(value = "单体电池编号 1-24", required = false)
	private Integer cellIndex;

	/**
	 * This field corresponds to the database column cell_info.cell_type  单体电池类型
	 */
	@ApiModelProperty(value = "单体电池类型", example = "单体电池类型", required = false)
	private String cellType;

	/**
	 * This field corresponds to the database column cell_info.cell_plant  电池厂商
	 */
	@ApiModelProperty(value = "电池厂商", example = "电池厂商", required = false)
	private String cellPlant;

	/**
	 * This field corresponds to the database column cell_info.use_from  开始投入使用时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@ApiModelProperty(value = "开始投入使用时间")
	private Date useFrom;

	/**
	 * This field corresponds to the database column cell_info.remain_time  剩余时长预测，单位月
	 */
	@ApiModelProperty(value = "剩余时长预测，单位月", required = false)
	private Integer remainTime;

	/**
	 * This field corresponds to the database column cell_info.update_time  数据更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@ApiModelProperty(value = "数据更新时间")
	private Date updateTime;

	/**
	 * This field corresponds to the database column cell_info.station_id  
	 */
	@ApiModelProperty(value = "stationId", required = false)
	private Integer stationId;

	/**
	 * This field corresponds to the database column cell_info.pulse_send_done  特征是否成功，0未发送， 1已发送，2成功，3失败，4读取处理中
	 */
	@ApiModelProperty(value = "特征是否成功，0未发送， 1已发送，2成功，3失败，4读取处理中", required = false)
	private Integer pulseSendDone;

    private Integer balanceStatus;
    private String balanceStatusStr;
    private Date balanceTime;
    private String balanceCommand;

    private Date testTime;
    private Integer testStatus;
    private String testStatusStr;

    //故障单体报标记 默认0正常，1故障
    private Integer faultMark;
    //标记故障单体时间
    private Date markTime;


	@Override
    public String toString() {
    	// TODO Auto-generated method stub
    	return super.toString();
    }

    
    //计算使用月份。
    public Integer getUsedMonth(){
    	if(this.getUseFrom() == null)
    		return null;
    	else
    		return MyDateUtils.diffMonths(this.getUseFrom(), new Date());
    }
    
	/**
     * This method returns the value of the database column cell_info.id
     *
     * @return the value of cell_info.id
     */
    public Integer getId() {
        return id;
    }

	/**
	 * This method sets the value of the database column cell_info.id  
	 * @param id the value for cell_info.id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * This method returns the value of the database column cell_info.gprs_id  
	 * @return the value of cell_info.gprs_id
	 */
	public String getGprsId() {
		return gprsId;
	}

	/**
	 * This method sets the value of the database column cell_info.gprs_id  
	 * @param gprsId the value for cell_info.gprs_id
	 */
	public void setGprsId(String gprsId) {
		this.gprsId = gprsId == null ? null : gprsId.trim();
	}

	/**
	 * This method returns the value of the database column cell_info.cell_index  单体电池编号 1-24
	 * @return the value of cell_info.cell_index
	 */
	public Integer getCellIndex() {
		return cellIndex;
	}

	/**
	 * This method sets the value of the database column cell_info.cell_index  单体电池编号 1-24
	 * @param cellIndex the value for cell_info.cell_index
	 */
	public void setCellIndex(Integer cellIndex) {
		this.cellIndex = cellIndex;
	}

	/**
	 * This method returns the value of the database column cell_info.cell_type  单体电池类型
	 * @return the value of cell_info.cell_type
	 */
	public String getCellType() {
		return cellType;
	}

	/**
	 * This method sets the value of the database column cell_info.cell_type  单体电池类型
	 * @param cellType the value for cell_info.cell_type
	 */
	public void setCellType(String cellType) {
		this.cellType = cellType == null ? null : cellType.trim();
	}

	/**
	 * This method returns the value of the database column cell_info.cell_plant  电池厂商
	 * @return the value of cell_info.cell_plant
	 */
	public String getCellPlant() {
		return cellPlant;
	}

	/**
	 * This method sets the value of the database column cell_info.cell_plant  电池厂商
	 * @param cellPlant the value for cell_info.cell_plant
	 */
	public void setCellPlant(String cellPlant) {
		this.cellPlant = cellPlant == null ? null : cellPlant.trim();
	}

	/**
	 * This method returns the value of the database column cell_info.use_from  开始投入使用时间
	 * @return the value of cell_info.use_from
	 */
	public Date getUseFrom() {
		return useFrom;
	}

	/**
	 * This method sets the value of the database column cell_info.use_from  开始投入使用时间
	 * @param useFrom the value for cell_info.use_from
	 */
	public void setUseFrom(Date useFrom) {
		this.useFrom = useFrom;
	}

	/**
	 * This method returns the value of the database column cell_info.remain_time  剩余时长预测，单位月
	 * @return the value of cell_info.remain_time
	 */
	public Integer getRemainTime() {
		return remainTime;
	}

	/**
	 * This method sets the value of the database column cell_info.remain_time  剩余时长预测，单位月
	 * @param remainTime the value for cell_info.remain_time
	 */
	public void setRemainTime(Integer remainTime) {
		this.remainTime = remainTime;
	}

	/**
	 * This method returns the value of the database column cell_info.update_time  数据更新时间
	
	 * @return the value of cell_info.update_time
	 */
	public Date getUpdateTime() {
		return updateTime;
	}

	/**
	 * This method sets the value of the database column cell_info.update_time  数据更新时间
	
	 * @param updateTime the value for cell_info.update_time
	 */
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	/**
	 * This method returns the value of the database column cell_info.station_id  
	 * @return the value of cell_info.station_id
	 */
	public Integer getStationId() {
		return stationId;
	}

	/**
	 * This method sets the value of the database column cell_info.station_id  
	 * @param stationId the value for cell_info.station_id
	 */
	public void setStationId(Integer stationId) {
		this.stationId = stationId;
	}

	/**
	 * This method returns the value of the database column cell_info.pulse_send_done  特征是否成功，0未发送， 1已发送，2成功，3失败，4读取处理中
	 * @return the value of cell_info.pulse_send_done
	 */
	public Integer getPulseSendDone() {
		return pulseSendDone;
	}

    /**
     * This method sets the value of the database column cell_info.pulse_send_done  特征是否成功，0未发送， 1已发送，2成功，3失败，4读取处理中
     *
     * @param pulseSendDone the value for cell_info.pulse_send_done
     */
    public void setPulseSendDone(Integer pulseSendDone) {
        this.pulseSendDone = pulseSendDone;
    }

    public Integer getBalanceStatus() {
        return balanceStatus;
    }

    public void setBalanceStatus(Integer balanceStatus) {
        this.balanceStatus = balanceStatus;
    }

    public String getBalanceStatusStr() {
        return balanceStatusStr;
    }

    public void setBalanceStatusStr(String balanceStatusStr) {
        this.balanceStatusStr = balanceStatusStr;
    }

    public Date getBalanceTime() {
        return balanceTime;
    }

    public void setBalanceTime(Date balanceTime) {
        this.balanceTime = balanceTime;
    }

    public String getBalanceCommand() {
        return balanceCommand;
    }

    public void setBalanceCommand(String balanceCommand) {
        this.balanceCommand = balanceCommand;
    }

    public Date getTestTime() {
        return testTime;
    }

    public void setTestTime(Date testTime) {
        this.testTime = testTime;
    }

    public Integer getTestStatus() {
        return testStatus;
    }

    public void setTestStatus(Integer testStatus) {
        this.testStatus = testStatus;
    }

    public String getTestStatusStr() {
        return testStatusStr;
    }

    public void setTestStatusStr(String testStatusStr) {
        this.testStatusStr = testStatusStr;
    }

	public Integer getFaultMark() {
		return faultMark;
	}

	public void setFaultMark(Integer faultMark) {
		this.faultMark = faultMark;
	}
    public Date getMarkTime() {
		return markTime;
	}

	public void setMarkTime(Date markTime) {
		this.markTime = markTime;
	}

}