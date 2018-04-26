package com.station.moudles.mapper;

import java.util.List;
import java.util.Map;

import com.station.moudles.entity.PackDataExpand;

public interface PackDataExpandMapper extends BaseMapper<PackDataExpand, Integer> {
	
	List<PackDataExpand> selectListByTime(Map m);
	
	//查询最新的的10条
	List<PackDataExpand> selectList10(PackDataExpand packDataExpand);
}