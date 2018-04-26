package com.station.moudles.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * This class was generated by Bill Generator.
 * This class corresponds to the database table rcv_gprs_list  
 *
 * @zdmgenerated 2017-13-19 07:13
 */
@ApiModel(value = "RcvGprsList", description = "RcvGprsList描述")
public class RcvGprsList {
	/**
	 * This field corresponds to the database column rcv_gprs_list.id  
	 */
	@ApiModelProperty(value = "pk", required = true)
	private Integer id;

	/**
	 * This field corresponds to the database column rcv_gprs_list.gprs_id  
	 */
	@ApiModelProperty(value = "gprsId", example = "gprsId", required = false)
	private String gprsId;

	/**
	 * This field corresponds to the database column rcv_gprs_list.link_status  1在线，0离线
	 */
	@ApiModelProperty(value = "1在线，0离线", required = false)
	private Byte linkStatus;

	/**
	 * This field corresponds to the database column rcv_gprs_list.last_active_time  
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@ApiModelProperty(value = "lastActiveTime")
	private Date lastActiveTime;

	/**
	 * This field corresponds to the database column rcv_gprs_list.port  
	 */
	@ApiModelProperty(value = "port", required = false)
	private Integer port;

	/**
	 * This method returns the value of the database column rcv_gprs_list.id  
	 * @return the value of rcv_gprs_list.id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * This method sets the value of the database column rcv_gprs_list.id  
	 * @param id the value for rcv_gprs_list.id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * This method returns the value of the database column rcv_gprs_list.gprs_id  
	 * @return the value of rcv_gprs_list.gprs_id
	 */
	public String getGprsId() {
		return gprsId;
	}

	/**
	 * This method sets the value of the database column rcv_gprs_list.gprs_id  
	 * @param gprsId the value for rcv_gprs_list.gprs_id
	 */
	public void setGprsId(String gprsId) {
		this.gprsId = gprsId == null ? null : gprsId.trim();
	}

	/**
	 * This method returns the value of the database column rcv_gprs_list.link_status  1在线，0离线
	 * @return the value of rcv_gprs_list.link_status
	 */
	public Byte getLinkStatus() {
		return linkStatus;
	}

	/**
	 * This method sets the value of the database column rcv_gprs_list.link_status  1在线，0离线
	 * @param linkStatus the value for rcv_gprs_list.link_status
	 */
	public void setLinkStatus(Byte linkStatus) {
		this.linkStatus = linkStatus;
	}

	/**
	 * This method returns the value of the database column rcv_gprs_list.last_active_time  
	 * @return the value of rcv_gprs_list.last_active_time
	 */
	public Date getLastActiveTime() {
		return lastActiveTime;
	}

	/**
	 * This method sets the value of the database column rcv_gprs_list.last_active_time  
	 * @param lastActiveTime the value for rcv_gprs_list.last_active_time
	 */
	public void setLastActiveTime(Date lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}

	/**
	 * This method returns the value of the database column rcv_gprs_list.port  
	 * @return the value of rcv_gprs_list.port
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * This method sets the value of the database column rcv_gprs_list.port  
	 * @param port the value for rcv_gprs_list.port
	 */
	public void setPort(Integer port) {
		this.port = port;
	}
}