/*
 * @ClassName DischargeManualRecordMapper
 * @Description 
 * @version 1.0
 * @Date 2018-04-10 10:01:22
 */
package com.station.moudles.mapper;

import java.util.List;

import com.station.moudles.entity.DischargeManualRecord;

public interface DischargeManualRecordMapper extends BaseMapper<DischargeManualRecord, Integer>{
	 DischargeManualRecord selectOneLatestSelective (DischargeManualRecord record);
	 
	//放电详情
	DischargeManualRecord selectDischargeDetail(Integer id);
	//通过ids查询对应数据
	List<DischargeManualRecord> findByDischargeManualRecordIds(Integer...ids);
}