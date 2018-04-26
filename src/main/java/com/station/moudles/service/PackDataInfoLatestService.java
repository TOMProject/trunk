package com.station.moudles.service;

import com.station.moudles.entity.PackDataInfoLatest;

public interface PackDataInfoLatestService extends BaseService<PackDataInfoLatest, String> {
	//获得在给定时间之后的总条数数
	int getCountByTime(PackDataInfoLatest packDataInfoLatest);

}