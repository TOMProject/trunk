package com.station.moudles.service.impl;

import com.station.common.utils.ReflectUtil;
import com.station.moudles.entity.GprsConfigInfo;
import com.station.moudles.entity.PackDataExpand;
import com.station.moudles.entity.PackDataExpandLatest;
import com.station.moudles.entity.SuggestCellInfo;
import com.station.moudles.entity.RoutingInspections;
import com.station.moudles.mapper.PackDataExpandMapper;
import com.station.moudles.mapper.RoutingInspectionsMapper;
import com.station.moudles.service.PackDataExpandService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PackDataExpandServiceImpl extends BaseServiceImpl<PackDataExpand, Integer> implements PackDataExpandService {
	// 内阻值
	private final static String cell_resist_Index = "cellResist";
	@Autowired
	RoutingInspectionsMapper routingInspectionsMapper;
	@Autowired
    PackDataExpandMapper packDataExpandMapper;
    
    /**
	 * 获得内阻的平均值
	 */
	@Override
	public List<SuggestCellInfo> getResistanceAverge(GprsConfigInfo gprsConfigInfo,PackDataExpandLatest packDataExpandLatest) {
		String gprsId = gprsConfigInfo.getGprsId();
		Integer subCount = gprsConfigInfo.getSubDeviceCount();
		//查找内阻最近记录的开始时间。
		Date startTime = new Date("1970/01/01 00:00:00");
		// 查询巡检记录主表是否有更换单体的记录，
		RoutingInspections queryRouting = new RoutingInspections();
		queryRouting.setGprsId(gprsId);
		queryRouting.setOperateType(3);// 更换单体
		List<RoutingInspections> routings = routingInspectionsMapper.selectListSelective(queryRouting);
		if (routings != null && routings.size() > 0) {
			RoutingInspections routing = routings.stream().max(Comparator.comparing(RoutingInspections::getOperateTime)).get();
			// 如果有就取更换单体以后的记录从pack_data_expend 表中
			startTime = routing.getOperateTime();
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("gprsId", gprsId);
		map.put("startTime", startTime);
		List<PackDataExpand> packDataExpandList = packDataExpandMapper.selectListByTime(map);
		//将最新内阻计算的值加入到内阻计算平均值中
		PackDataExpand packDataExpand = new PackDataExpand();
		if(packDataExpandLatest != null) {
			for(int i = 1 ;i <= gprsConfigInfo.getSubDeviceCount(); i++) {
			Object resistValue = ReflectUtil.getValueByKey(packDataExpandLatest, cell_resist_Index + i);
			ReflectUtil.setValueByKet(packDataExpand, cell_resist_Index+i, resistValue);
			}
			packDataExpandList.add(0, packDataExpand);
		}
		List<SuggestCellInfo> resistLists = getResistList(packDataExpandList,subCount);
		//按内阻值排序
		if (CollectionUtils.isNotEmpty(resistLists)) {
			resistLists = resistLists.stream()
					.sorted(Comparator.comparing(SuggestCellInfo::getCellResist))
					.collect(Collectors.toList());
		}
		return resistLists;
	}
	

	public List<SuggestCellInfo> getResistList(List<PackDataExpand> packDataExpandLsit,Integer subCount){
		List<SuggestCellInfo> list = new ArrayList<SuggestCellInfo>();
		if(packDataExpandLsit == null || packDataExpandLsit.size() < 1 || subCount == null) {
			return list;
		}
		for (int i = 1; i <= subCount; i++) {
			SuggestCellInfo resistAvg = new SuggestCellInfo();
			BigDecimal resistCount = getResistValue(packDataExpandLsit,i);
			resistAvg.setCellIndex(i);
			resistAvg.setCellResist(resistCount);
			list.add(resistAvg);
		}
		return list;
	}
	
	//计算单个电池内阻平均值
	public BigDecimal getResistValue(List<PackDataExpand> packDataExpandLsit,Integer index) {
		BigDecimal resistCount = new BigDecimal(0.0);
		int i = 0;
		for (i = 0; i < packDataExpandLsit.size() && i < 5; i++) {
			Object resist = ReflectUtil.getValueByKey(packDataExpandLsit.get(i), cell_resist_Index + index);
			BigDecimal number = new BigDecimal(resist == null ? "0" : resist.toString());
			resistCount=resistCount.add(number);
		}
		resistCount = resistCount.divide(new BigDecimal(i), 3 , BigDecimal.ROUND_HALF_UP);
		return resistCount;
	}


	@Override
	public List<PackDataExpand> selectList10(PackDataExpand packDataExpand) {
		
		return packDataExpandMapper.selectList10(packDataExpand);
	}
}