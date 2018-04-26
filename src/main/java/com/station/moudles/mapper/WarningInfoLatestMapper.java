package com.station.moudles.mapper;

import java.util.List;
import java.util.Map;

import com.station.moudles.entity.StationInfo;
import com.station.moudles.entity.StationWarningInfo;
import com.station.moudles.entity.WarnArea;
import com.station.moudles.entity.WarningInfo;
import com.station.moudles.entity.WarningInfoLatest;
import com.station.moudles.vo.search.SearchWarningInfoPagingVo;

public interface WarningInfoLatestMapper extends BaseMapper<WarningInfoLatest, Integer>{
	
	List<StationWarningInfo> selectWarningListByStation(Map m);

	List<WarnArea> selectWarnAreaList(Map m);
	//app的告警信息
	List<WarnArea> appSelectWarnAreaList(Map m);
	
	List<StationInfo> appWarnAreaSelectListSelective(SearchWarningInfoPagingVo searchWarningInfoPagingVo);
}
