/*
 * @ClassName DischargeAbstractRecordMapper
 * @Description 
 * @version 1.0
 * @Date 2018-01-24 13:59:01
 */
package com.station.moudles.mapper;

import java.util.List;

import com.station.moudles.entity.DischargeAbstractRecord;

public interface DischargeAbstractRecordMapper extends BaseMapper<DischargeAbstractRecord, Integer>{

	//根据条件返回放电记录
	List<DischargeAbstractRecord> getDischargAbstract(DischargeAbstractRecord dischargeAbstractRecord);
	//通过gprsId更新
	void updateByGprsIdSelective(DischargeAbstractRecord dischargeAbstractRecord);
	//查询出endTime为null
	List<DischargeAbstractRecord> selectEndTimeIsNull(DischargeAbstractRecord dischargeAbstractRecord);
}