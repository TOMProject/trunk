package com.station.moudles.vo.search;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;

/**
 * This class was generated by Bill Generator.
 * This class corresponds to the database table pulse_discharge_send  特征放电命令发送列表,每次有插入命令,服务器给gprs发送特征指令，同一电池组必须等一条特征指令执行完成才能执行另一条
 *
 * @zdmgenerated 2017-13-19 07:13
 */
@ApiModel(value="特征放电命令发送列表,每次有插入命令,服务器给gprs发送特征指令，同一电池组必须等一条特征指令执行完成才能执行另一条查询",description="特征放电命令发送列表,每次有插入命令,服务器给gprs发送特征指令，同一电池组必须等一条特征指令执行完成才能执行另一条查询描述")
public class SearchPulseDischargeSendPagingVo extends PageEntity {
    /**
     * This field corresponds to the database column pulse_discharge_send.id  
     */
    @ApiModelProperty(value="pk",required=false)
    private Integer id;

    /**
     * This field corresponds to the database column pulse_discharge_send.gprs_id  
     */
    @ApiModelProperty(value="gprsId",example="gprsId",required=false)
    private String gprsId;

    /**
     * This field corresponds to the database column pulse_discharge_send.insert_time  指令插入表的时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value="指令插入表的时间")
    private Date insertTime;

    /**
     * This field corresponds to the database column pulse_discharge_send.pulse_cell  放电电池序,取值1~24
     */
    @ApiModelProperty(value="放电电池序,取值1~24",required=false)
    private Integer pulseCell;

    /**
     * This field corresponds to the database column pulse_discharge_send.fast_sample_interval  快速采样间隔（区间1和3）， 0：5ms; 1: 10ms; 2: 20ms; 3: 40ms
     */
    @ApiModelProperty(value="快速采样间隔（区间1和3）， 0：5ms; 1: 10ms; 2: 20ms; 3: 40ms",required=false)
    private Integer fastSampleInterval;

    /**
     * This field corresponds to the database column pulse_discharge_send.slow_sample_interval  慢速采样间隔（区间1和3）， 0：1s; 1: 5s; 2: 10s; 3: 20s
     */
    @ApiModelProperty(value="慢速采样间隔（区间1和3）， 0：1s; 1: 5s; 2: 10s; 3: 20s",required=false)
    private Integer slowSampleInterval;

    /**
     * This field corresponds to the database column pulse_discharge_send.discharge_time  区间2时间， 0：1s; 1: 2s; 2: 3s; 3: 4s
     */
    @ApiModelProperty(value="区间2时间， 0：1s; 1: 2s; 2: 3s; 3: 4s",required=false)
    private Integer dischargeTime;

    /**
     * This field corresponds to the database column pulse_discharge_send.slow_sample_time  慢采集采样时间（区间5）0~600s
     */
    @ApiModelProperty(value="慢采集采样时间（区间5）0~600s",required=false)
    private Integer slowSampleTime;

    /**
     * This field corresponds to the database column pulse_discharge_send.send_done  命令状态:0未发送,1发送成功，2特征执行成功，3特征执行失败
     */
    @ApiModelProperty(value="命令状态:0未发送,1发送成功，2特征执行成功，3特征执行失败",required=false)
    private Byte sendDone;

    /**
     * This field corresponds to the database column pulse_discharge_send.end_time  特征结束时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty(value="特征结束时间")
    private Date endTime;

    /**
     * This method returns the value of the database column pulse_discharge_send.id  
     * @return the value of pulse_discharge_send.id
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method sets the value of the database column pulse_discharge_send.id  
     * @param id the value for pulse_discharge_send.id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method returns the value of the database column pulse_discharge_send.gprs_id  
     * @return the value of pulse_discharge_send.gprs_id
     */
    public String getGprsId() {
        return gprsId;
    }

    /**
     * This method sets the value of the database column pulse_discharge_send.gprs_id  
     * @param gprsId the value for pulse_discharge_send.gprs_id
     */
    public void setGprsId(String gprsId) {
        this.gprsId = gprsId == null ? null : gprsId.trim();
    }

    /**
     * This method returns the value of the database column pulse_discharge_send.insert_time  指令插入表的时间
     * @return the value of pulse_discharge_send.insert_time
     */
    public Date getInsertTime() {
        return insertTime;
    }

    /**
     * This method sets the value of the database column pulse_discharge_send.insert_time  指令插入表的时间
     * @param insertTime the value for pulse_discharge_send.insert_time
     */
    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    /**
     * This method returns the value of the database column pulse_discharge_send.pulse_cell  放电电池序,取值1~24
     * @return the value of pulse_discharge_send.pulse_cell
     */
    public Integer getPulseCell() {
        return pulseCell;
    }

    /**
     * This method sets the value of the database column pulse_discharge_send.pulse_cell  放电电池序,取值1~24
     * @param pulseCell the value for pulse_discharge_send.pulse_cell
     */
    public void setPulseCell(Integer pulseCell) {
        this.pulseCell = pulseCell;
    }

    /**
     * This method returns the value of the database column pulse_discharge_send.fast_sample_interval  快速采样间隔（区间1和3）， 0：5ms; 1: 10ms; 2: 20ms; 3: 40ms
     * @return the value of pulse_discharge_send.fast_sample_interval
     */
    public Integer getFastSampleInterval() {
        return fastSampleInterval;
    }

    /**
     * This method sets the value of the database column pulse_discharge_send.fast_sample_interval  快速采样间隔（区间1和3）， 0：5ms; 1: 10ms; 2: 20ms; 3: 40ms
     * @param fastSampleInterval the value for pulse_discharge_send.fast_sample_interval
     */
    public void setFastSampleInterval(Integer fastSampleInterval) {
        this.fastSampleInterval = fastSampleInterval;
    }

    /**
     * This method returns the value of the database column pulse_discharge_send.slow_sample_interval  慢速采样间隔（区间1和3）， 0：1s; 1: 5s; 2: 10s; 3: 20s
     * @return the value of pulse_discharge_send.slow_sample_interval
     */
    public Integer getSlowSampleInterval() {
        return slowSampleInterval;
    }

    /**
     * This method sets the value of the database column pulse_discharge_send.slow_sample_interval  慢速采样间隔（区间1和3）， 0：1s; 1: 5s; 2: 10s; 3: 20s
     * @param slowSampleInterval the value for pulse_discharge_send.slow_sample_interval
     */
    public void setSlowSampleInterval(Integer slowSampleInterval) {
        this.slowSampleInterval = slowSampleInterval;
    }

    /**
     * This method returns the value of the database column pulse_discharge_send.discharge_time  区间2时间， 0：1s; 1: 2s; 2: 3s; 3: 4s
     * @return the value of pulse_discharge_send.discharge_time
     */
    public Integer getDischargeTime() {
        return dischargeTime;
    }

    /**
     * This method sets the value of the database column pulse_discharge_send.discharge_time  区间2时间， 0：1s; 1: 2s; 2: 3s; 3: 4s
     * @param dischargeTime the value for pulse_discharge_send.discharge_time
     */
    public void setDischargeTime(Integer dischargeTime) {
        this.dischargeTime = dischargeTime;
    }

    /**
     * This method returns the value of the database column pulse_discharge_send.slow_sample_time  慢采集采样时间（区间5）0~600s
     * @return the value of pulse_discharge_send.slow_sample_time
     */
    public Integer getSlowSampleTime() {
        return slowSampleTime;
    }

    /**
     * This method sets the value of the database column pulse_discharge_send.slow_sample_time  慢采集采样时间（区间5）0~600s
     * @param slowSampleTime the value for pulse_discharge_send.slow_sample_time
     */
    public void setSlowSampleTime(Integer slowSampleTime) {
        this.slowSampleTime = slowSampleTime;
    }

    /**
     * This method returns the value of the database column pulse_discharge_send.send_done  命令状态:0未发送,1发送成功，2特征执行成功，3特征执行失败
     * @return the value of pulse_discharge_send.send_done
     */
    public Byte getSendDone() {
        return sendDone;
    }

    /**
     * This method sets the value of the database column pulse_discharge_send.send_done  命令状态:0未发送,1发送成功，2特征执行成功，3特征执行失败
     * @param sendDone the value for pulse_discharge_send.send_done
     */
    public void setSendDone(Byte sendDone) {
        this.sendDone = sendDone;
    }

    /**
     * This method returns the value of the database column pulse_discharge_send.end_time  特征结束时间
     * @return the value of pulse_discharge_send.end_time
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * This method sets the value of the database column pulse_discharge_send.end_time  特征结束时间
     * @param endTime the value for pulse_discharge_send.end_time
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}