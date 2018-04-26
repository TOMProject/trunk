package com.station.moudles.entity;

import java.util.Date;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * This class was generated by Bill Generator.
 * This class corresponds to the database table modify_gprsid_send  修改主从机ID发送表
 *
 * @zdmgenerated 2017-47-24 07:47
 */
@ApiModel(value = "修改主从机ID发送表", description = "修改主从机ID发送表描述")
public class ModifyGprsidSend {
	/**
	 * This field corresponds to the database column modify_gprsid_send.id  
	 */
	@ApiModelProperty(value = "pk", required = true)
	private Integer id;

	/**
	 * This field corresponds to the database column modify_gprsid_send.type  修改类型,  1位主机,2位从机
	 */
	@NotNull
	@ApiModelProperty(value = "修改类型,  1位主机,2位从机", required = false)
	private Integer type;

	/**
	 * This field corresponds to the database column modify_gprsid_send.gprs_id  当type==1时，gprs_id = outer_id；当type == 2时，gprs_id为从机的主机ID
	 */
	@ApiModelProperty(value = "当type==1时，gprs_id = outer_id；当type == 2时，gprs_id为从机的主机ID", example = "当type==1时，gprs_id = outer_id；当type == 2时，gprs_id为从机的主机ID", required = false)
	private String gprsId;

	/**
	 * This field corresponds to the database column modify_gprsid_send.inner_id  内部ID,即原ID
	 */
	@NotNull
	@ApiModelProperty(value = "内部ID,即原ID", example = "内部ID,即原ID", required = false)
	private String innerId;

	/**
	 * This field corresponds to the database column modify_gprsid_send.outer_id  外部ID,即外壳的新ID
	 */
	@NotNull
	@ApiModelProperty(value = "外部ID,即外壳的新ID", example = "外部ID,即外壳的新ID", required = false)
	private String outerId;

	/**
	 * This field corresponds to the database column modify_gprsid_send.send_done  0为待发送,1为发送,2为修改成功,3为修改失败
	 */
	@ApiModelProperty(value = "0未发送,1发送成功，2执行成功，3执行失败或超时，4发送失败", required = false)
	private Byte sendDone;

	/**
	 * This field corresponds to the database column modify_gprsid_send.time  修改完成时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@ApiModelProperty(value = "修改完成时间")
	private Date time;

	private Integer state;

	/**
	 * This method returns the value of the database column modify_gprsid_send.id  
	 * @return the value of modify_gprsid_send.id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * This method sets the value of the database column modify_gprsid_send.id  
	 * @param id the value for modify_gprsid_send.id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * This method returns the value of the database column modify_gprsid_send.type  修改类型,  1位主机,2位从机
	 * @return the value of modify_gprsid_send.type
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * This method sets the value of the database column modify_gprsid_send.type  修改类型,  1位主机,2位从机
	 * @param type the value for modify_gprsid_send.type
	 */
	public void setType(Integer type) {
		this.type = type;
	}

	/**
	 * This method returns the value of the database column modify_gprsid_send.gprs_id  当type==1时，gprs_id = outer_id；当type == 2时，gprs_id为从机的主机ID
	 * @return the value of modify_gprsid_send.gprs_id
	 */
	public String getGprsId() {
		return gprsId;
	}

	/**
	 * This method sets the value of the database column modify_gprsid_send.gprs_id  当type==1时，gprs_id = outer_id；当type == 2时，gprs_id为从机的主机ID
	 * @param gprsId the value for modify_gprsid_send.gprs_id
	 */
	public void setGprsId(String gprsId) {
		this.gprsId = gprsId == null ? null : gprsId.trim();
	}

	/**
	 * This method returns the value of the database column modify_gprsid_send.inner_id  内部ID,即原ID
	 * @return the value of modify_gprsid_send.inner_id
	 */
	public String getInnerId() {
		return innerId;
	}

	/**
	 * This method sets the value of the database column modify_gprsid_send.inner_id  内部ID,即原ID
	 * @param innerId the value for modify_gprsid_send.inner_id
	 */
	public void setInnerId(String innerId) {
		this.innerId = innerId == null ? null : innerId.trim();
	}

	/**
	 * This method returns the value of the database column modify_gprsid_send.outer_id  外部ID,即外壳的新ID
	 * @return the value of modify_gprsid_send.outer_id
	 */
	public String getOuterId() {
		return outerId;
	}

	/**
	 * This method sets the value of the database column modify_gprsid_send.outer_id  外部ID,即外壳的新ID
	 * @param outerId the value for modify_gprsid_send.outer_id
	 */
	public void setOuterId(String outerId) {
		this.outerId = outerId == null ? null : outerId.trim();
	}

	/**
	 * This method returns the value of the database column modify_gprsid_send.send_done  0为待发送,1为发送,2为修改成功,3为修改失败
	 * @return the value of modify_gprsid_send.send_done
	 */
	public Byte getSendDone() {
		return sendDone;
	}

	/**
	 * This method sets the value of the database column modify_gprsid_send.send_done  0为待发送,1为发送,2为修改成功,3为修改失败
	 * @param sendDone the value for modify_gprsid_send.send_done
	 */
	public void setSendDone(Byte sendDone) {
		this.sendDone = sendDone;
	}

	/**
	 * This method returns the value of the database column modify_gprsid_send.time  修改完成时间
	 * @return the value of modify_gprsid_send.time
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * This method sets the value of the database column modify_gprsid_send.time  修改完成时间
	 * @param time the value for modify_gprsid_send.time
	 */
	public void setTime(Date time) {
		this.time = time;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}
}