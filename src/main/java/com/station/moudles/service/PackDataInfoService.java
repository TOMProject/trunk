package com.station.moudles.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.station.moudles.entity.DischargeAbstractRecord;
import com.station.moudles.entity.DischargeManualRecord;
import com.station.moudles.entity.GprsConfigInfo;
import com.station.moudles.entity.PackDataInfo;

public interface PackDataInfoService extends BaseService<PackDataInfo, Integer> {

	List<PackDataInfo> selectListByGprsIdTime(String gprsId, Date rcvTime);

	List<Map<String, Object>> getSumVolCur(String gprsId);

	List getDischargeHistory(String gprsId);

	List<Map<String, Object>> getCellVolList(String gprsId, Integer cellIndex);

	List getCellDischargeHistory(String gprsId, Integer cellIndex);
	//获得核容放电时长记录
	DischargeAbstractRecord  getCheckDischargeList(GprsConfigInfo gprsConfigInfo) ;
	//获得放电时长记录
	DischargeAbstractRecord  getDischargeList(GprsConfigInfo gprsConfigInfo) ;
	
	// 得到非该状态，id小于指定id的最近一条数据
	PackDataInfo getOneLatestNonStateWhichIdLessThanGivenValue(String gprsId, Integer id, String state);
	
	// 得到非该状态，id大于指定id的最近一条数据
	PackDataInfo getOneLatestNonStateWhichIdGreaterThanGivenValue(String gprsId, Integer id, String state);
	
	/**
	 * 
	 * @param gprsId
	 * @param forwardCount   向前取数据条数
	 * @param backwardCount  向后取数据条数
	 */
	List<PackDataInfo> getRawDischargeHistory(String gprsId, int forwardCount, int backwardCount);
	
	/**
	 * 在集合中前后各添加指定条数的数据
	 */
	List<PackDataInfo> forwardAndBackwardAddDatas(List<PackDataInfo> checkDischargeList, int forwardCount, int backwardCount);
	
	/**
	 * 现场放电记录和系统中的数据进行校验，不符合规则的在discharge_manual_record表中备注
	 * @param manualRecord
	 * @return 得到放电记录，不管在不在误差范围内都返回
	 */
	List<PackDataInfo> verifyDischargeRecord(DischargeManualRecord manualRecord);
}