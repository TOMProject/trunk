package com.station.moudles.service;

import java.util.List;

import com.station.moudles.entity.GprsConfigInfo;
import com.station.moudles.entity.PackDataExpand;
import com.station.moudles.entity.PackDataExpandLatest;
import com.station.moudles.entity.SuggestCellInfo;

public interface PackDataExpandService extends BaseService<PackDataExpand, Integer> {
	//得到内阻平均值
	List<SuggestCellInfo> getResistanceAverge(GprsConfigInfo gprsConfigInfo,PackDataExpandLatest packDataExpandLatest);
	//查询最新的的10条
	List<PackDataExpand> selectList10(PackDataExpand packDataExpand);
}