package com.station.moudles.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.station.common.Constant;
import com.station.common.cache.DischargeCache;
import com.station.common.redis.RedisCacheAgent;
import com.station.common.utils.BeanValueUtils;
import com.station.common.utils.MyDateUtils;
import com.station.common.utils.ReflectUtil;
import com.station.common.utils.WaveFilter;
import com.station.moudles.entity.DischargeAbstractRecord;
import com.station.moudles.entity.DischargeManualRecord;
import com.station.moudles.entity.GprsConfigInfo;
import com.station.moudles.entity.PackDataInfo;
import com.station.moudles.entity.Parameter;
import com.station.moudles.entity.RoutingInspections;
import com.station.moudles.mapper.DischargeAbstractRecordMapper;
import com.station.moudles.mapper.DischargeManualRecordMapper;
import com.station.moudles.mapper.GprsConfigInfoMapper;
import com.station.moudles.mapper.PackDataInfoMapper;
import com.station.moudles.mapper.ParameterMapper;
import com.station.moudles.mapper.RoutingInspectionsMapper;
import com.station.moudles.service.PackDataInfoService;

@Service
public class PackDataInfoServiceImpl extends BaseServiceImpl<PackDataInfo, Integer> implements PackDataInfoService {

    private final PackDataInfoMapper packDataInfoMapper;
    private final DischargeCache dischargeCache;
    private static final String DISCHARGE_STATE="放电";
	public final static String FLOATCHARGE_STATE = "浮充";
    @Autowired
	DischargeAbstractRecordMapper dischargeAbstractRecordMapper;
    @Autowired
    RoutingInspectionsMapper routingInspectionsMapper;
    @Autowired
    GprsConfigInfoMapper gprsConfigInfoMapper;
    @Autowired
    RedisCacheAgent redisCacheAgent;
    @Autowired
    ParameterMapper parameterMapper;
    @Autowired
	DischargeManualRecordMapper dischargeManualRecordMapper;
    
    @Autowired
    @Lazy
    public PackDataInfoServiceImpl(PackDataInfoMapper packDataInfoMapper,
                                   DischargeCache dischargeCache) {
        this.packDataInfoMapper = packDataInfoMapper;
        this.dischargeCache = dischargeCache;
    }

    @Override
    public List<PackDataInfo> selectListByGprsIdTime(String gprsId, Date rcvTime) {
        Map<Object, Object> m = new HashMap<>();
        m.put("gprsId", gprsId);
        m.put("rcvTime", rcvTime);
        return packDataInfoMapper.selectListByGprsIdTime(m);
    }

    @Override
    public List<Map<String, Object>> getSumVolCur(String gprsId) {
        Date rcvTime = MyDateUtils.getDiffTime(-6 * 60 * 60 * 1000);
        List<PackDataInfo> pdiList = selectListByGprsIdTime(gprsId, rcvTime);
//        Function<PackDataInfo, Double> getVol = (PackDataInfo pdi) -> {return Double.valueOf(pdi.getGenVol().doubleValue()); };
//        ObjDoubleConsumer<PackDataInfo> setVol = (PackDataInfo pdi, double val) -> {pdi.setGenVol(BigDecimal.valueOf(val).setScale(3, BigDecimal.ROUND_HALF_UP));};
//        WaveFilter.swingFilter(pdiList, getVol, setVol, 53, 47, 0.1);
//		
//        WaveFilter.avgFilter(pdiList, 
//				(PackDataInfo pdi) -> {return Double.valueOf(pdi.getGenCur().doubleValue());},
//				(PackDataInfo pdi, double val) -> {pdi.setGenCur(BigDecimal.valueOf(val).setScale(3, BigDecimal.ROUND_HALF_UP));}, 
//				100, -100, 0.1);
        List<Map<String, Object>> resultList = new ArrayList();
        for (PackDataInfo pdi : pdiList) {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("genVol", pdi.getGenVol());
            m.put("genCur", pdi.getGenCur());
            m.put("rcvTime", pdi.getRcvTime());
            resultList.add(m);
        }
        return resultList;
    }

    @Override
    public List<Map<String, Object>> getCellVolList(String gprsId, Integer cellIndex) {
        Date rcvTime = MyDateUtils.getDiffTime(-6 * 60 * 60 * 1000);
        List<PackDataInfo> pdiList = selectListByGprsIdTime(gprsId, rcvTime);
        List<Map<String, Object>> resultList = new ArrayList();
        for (PackDataInfo pdi : pdiList) {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("cellVol", ReflectUtil.getValueByKey(pdi, "cellVol" + cellIndex));
            m.put("rcvTime", pdi.getRcvTime());
            resultList.add(m);
        }
        return resultList;
    }


    @Override
//    @Cacheable(value = "station", key="#gprsId")
    public List getDischargeHistory(String gprsId) {
//        List<PackDataInfo> items = dischargeCache.get(gprsId, Collections.emptyList());
//        List<PackDataInfo> items = getRawDischargeHistory(gprsId, 10, 10);
        List<PackDataInfo> items = (List<PackDataInfo>) redisCacheAgent.getCache(Constant.DISHISTORY_KEY_PREFIX + gprsId);
        if (CollectionUtils.isEmpty(items)) {
            return null;
        }
        List<Map> results = Lists.newArrayList();
        items.stream().forEach(item -> {
            Map map = Maps.newHashMap();
            map.put("id", item.getId());
            map.put("state", item.getState());
            map.put("rcvTime", item.getRcvTime());
            map.put("genVol", item.getGenVol());
            map.put("genCur", item.getGenCur());
            results.add(map);
        });
        results.sort(Comparator.comparing(o -> ((Long) ((Date) o.get("rcvTime")).getTime())));
        return results;
    }

    @Override
    public List getCellDischargeHistory(String gprsId, Integer cellIndex) {
//        List<PackDataInfo> items = dischargeCache.get(gprsId, Collections.emptyList());
        List<PackDataInfo> items = (List<PackDataInfo>) redisCacheAgent.getCache(Constant.DISHISTORY_KEY_PREFIX + gprsId);
        if (CollectionUtils.isEmpty(items)) {
            return null;
        }
        List<Map> results = Lists.newArrayList();
        items.stream().forEach(item -> {
            Map map = Maps.newHashMap();
            map.put("id", item.getId());
            map.put("state", item.getState());
            map.put("rcvTime", item.getRcvTime());
            map.put("cenVol", BeanValueUtils.getValue("cellVol" + cellIndex, item));
            results.add(map);
        });
        results.sort(Comparator.comparing(o -> ((Long) ((Date) o.get("rcvTime")).getTime())));
        return results;
    }

    public PackDataInfo selectByOrderSelective(String gprsId, String state, Date rcvTime) {
        try {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("gprsId", gprsId);
            m.put("state", state);
            m.put("rcvTime", rcvTime);
            List<PackDataInfo> resultList = packDataInfoMapper.selectByOrderSelective(m);
            if (resultList.size() > 0) {
                return resultList.get(0);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    List<PackDataInfo> selectListByTime(String gprsId, String state, Date startTime, Date endTime) {
        try {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("gprsId", gprsId);
            m.put("state", state);
            m.put("startTime", startTime);
            m.put("endTime", endTime);
            return packDataInfoMapper.selectListByTime(m);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    /*
	 * 计算核容放电数据 parm:gprsid,validDischargeVol
	 */
	@Override
	public DischargeAbstractRecord getCheckDischargeList(GprsConfigInfo gprsConfigInfo) {
		// 放电时间在90天范围内 and 电流要小于-xx(有效放电电流)，大于-xx(有效放电电流最大值)， and 时长>xxh 或者 结束电压<有效放电电压 order by 时间
		Date endTime = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(endTime);
		Integer validDay = gprsConfigInfo.getValidDay();
		if(validDay == null || validDay == 0) {
			validDay=90;
		}
		calendar.add(Calendar.DATE, -validDay);// 当前时间减去期限天数
		Date time = calendar.getTime();
		double cur =0.0-gprsConfigInfo.getValidDischargeCur().abs().doubleValue();
		double maxCur = 0.0-gprsConfigInfo.getMaxDischargeCur().abs().doubleValue();
		DischargeAbstractRecord dischargeAbstractRecord = new DischargeAbstractRecord();
		dischargeAbstractRecord.setGprsId(gprsConfigInfo.getGprsId());
		dischargeAbstractRecord.setEndtime(time);
		dischargeAbstractRecord.setEndVol(gprsConfigInfo.getCheckDischargeVol());
		dischargeAbstractRecord.setGenCur(new BigDecimal(cur));
		dischargeAbstractRecord.setMaxCur(new BigDecimal(maxCur));
		dischargeAbstractRecord.setDischargeTime(gprsConfigInfo.getCheckDischargeTime());
		List<DischargeAbstractRecord> dischargAbstractList = dischargeAbstractRecordMapper.getDischargAbstract(dischargeAbstractRecord);
		List<PackDataInfo> datas = new ArrayList<PackDataInfo>();
		if (CollectionUtils.isNotEmpty(dischargAbstractList)) {
			// 和整治时间对比，如果有单体更换记录，优先取单体更换后的记录
			RoutingInspections record = new RoutingInspections();
			record.setGprsId(gprsConfigInfo.getGprsId());
			record.setOperateType(3);
			RoutingInspections latestSelective = routingInspectionsMapper.selectOneLatestSelective(record);
			if (latestSelective != null && latestSelective.getOperateTime() != null) {
				List<DischargeAbstractRecord> filtered = dischargAbstractList.stream()
						.filter(d -> d.getEndtime().compareTo(latestSelective.getOperateTime()) > 0)
						.collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(filtered))
					dischargAbstractList = filtered;
			}
			// 在过滤后的记录中，优先取总电压最低的记录。
			DischargeAbstractRecord discharge = dischargAbstractList.stream()
					.min(Comparator.comparing(DischargeAbstractRecord::getEndVol)).get();
			
			// 得到放电的开始id和结束id;在pack_data_info表中的id范围内 ，通过gprs_id 查询返回list
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("gprsId", discharge.getGprsId());
			map.put("startId", discharge.getRecordStartId());
			map.put("endId", discharge.getRecordEndId());
			datas = packDataInfoMapper.getPackDateInfoByIdAndGprsId(map);
			if(CollectionUtils.isNotEmpty(datas)) {
//				discharge.setDischargeDetails(getRealDischargeRecord(datas));
				discharge.setDischargeDetails(datas);
				return discharge;
//				datas = dischargeRecordWaveFilter(gprsConfigInfo, datas);
			}
		}
		return null;
	}

	private List<PackDataInfo> dischargeRecordWaveFilter(GprsConfigInfo gprsConfigInfo, List<PackDataInfo> datas) {
		List<PackDataInfo> after10Datas = Lists.newArrayList();
		if(datas.size() < 10)
			after10Datas.addAll(datas);
		else
			after10Datas.addAll(datas.subList(datas.size()-10, datas.size()));
		// 过滤总电压数据
		Function<PackDataInfo, Double> getVolMethod = (p) -> {return p.getGenVol().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setVolMethod = (p, v) -> {p.setGenVol(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getVolMethod, setVolMethod, 60, 40, 0.5);

		// 过滤单体电压
		Function<PackDataInfo, Double> getCellVol_1 = (p) -> {return p.getCellVol1().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_1 = (p, v) -> {p.setCellVol1(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_1, setCellVol_1, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_2 = (p) -> {return p.getCellVol2().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_2 = (p, v) -> {p.setCellVol2(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_2, setCellVol_2, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_3 = (p) -> {return p.getCellVol3().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_3 = (p, v) -> {p.setCellVol3(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_3, setCellVol_3, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_4 = (p) -> {return p.getCellVol4().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_4 = (p, v) -> {p.setCellVol4(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_4, setCellVol_4, 2.5, 1.5, 0.1);

		if(gprsConfigInfo.getSubDeviceCount() == 24) {
			isCell24(after10Datas);
		}
		if(datas.size() > 10)
			datas=datas.subList(0, datas.size()-10);
		datas.addAll(after10Datas);
		return datas;
	}
	
	public void isCell24(List<PackDataInfo> after10Datas) {
		Function<PackDataInfo, Double> getCellVol_5 = (p) -> {return p.getCellVol5().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_5 = (p, v) -> {p.setCellVol5(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_5, setCellVol_5, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_6 = (p) -> {return p.getCellVol6().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_6 = (p, v) -> {p.setCellVol6(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_6, setCellVol_6, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_7 = (p) -> {return p.getCellVol7().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_7 = (p, v) -> {p.setCellVol7(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_7, setCellVol_7, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_8 = (p) -> {return p.getCellVol8().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_8 = (p, v) -> {p.setCellVol8(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_8, setCellVol_8, 2.5, 1.5, 0.1);
		
		
		Function<PackDataInfo, Double> getCellVol_9 = (p) -> {return p.getCellVol9().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_9 = (p, v) -> {p.setCellVol9(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_9, setCellVol_9, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_10 = (p) -> {return p.getCellVol10().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_10 = (p, v) -> {p.setCellVol10(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_10, setCellVol_10, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_11 = (p) -> {return p.getCellVol11().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_11 = (p, v) -> {p.setCellVol11(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_11, setCellVol_11, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_12 = (p) -> {return p.getCellVol12().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_12 = (p, v) -> {p.setCellVol12(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_12, setCellVol_12, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_13 = (p) -> {return p.getCellVol13().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_13 = (p, v) -> {p.setCellVol13(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_13, setCellVol_13, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_14 = (p) -> {return p.getCellVol14().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_14 = (p, v) -> {p.setCellVol14(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_14, setCellVol_14, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_15 = (p) -> {return p.getCellVol15().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_15 = (p, v) -> {p.setCellVol15(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_15, setCellVol_15, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_16 = (p) -> {return p.getCellVol16().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_16 = (p, v) -> {p.setCellVol16(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_16, setCellVol_16, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_17 = (p) -> {return p.getCellVol17().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_17 = (p, v) -> {p.setCellVol17(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_17, setCellVol_17, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_18 = (p) -> {return p.getCellVol18().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_18 = (p, v) -> {p.setCellVol18(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_18, setCellVol_18, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_19 = (p) -> {return p.getCellVol19().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_19 = (p, v) -> {p.setCellVol19(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_19, setCellVol_19, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_20 = (p) -> {return p.getCellVol20().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_20 = (p, v) -> {p.setCellVol20(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_20, setCellVol_20, 2.5, 1.5, 0.1);
	
		Function<PackDataInfo, Double> getCellVol_21 = (p) -> {return p.getCellVol21().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_21 = (p, v) -> {p.setCellVol21(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_21, setCellVol_21, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_22 = (p) -> {return p.getCellVol22().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_22 = (p, v) -> {p.setCellVol22(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_22, setCellVol_22, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_23 = (p) -> {return p.getCellVol23().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_23 = (p, v) -> {p.setCellVol23(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_23, setCellVol_23, 2.5, 1.5, 0.1);
		
		Function<PackDataInfo, Double> getCellVol_24 = (p) -> {return p.getCellVol24().doubleValue();};
		ObjDoubleConsumer<PackDataInfo> setCellVol_24 = (p, v) -> {p.setCellVol24(BigDecimal.valueOf(v));};
		WaveFilter.swingFilter(after10Datas, getCellVol_24, setCellVol_24, 2.5, 1.5, 0.1);
	}
	
	
	/**
	 * 有效放电数据
	 */
	@Override
	public DischargeAbstractRecord getDischargeList(GprsConfigInfo gprsConfigInfo) {
		// 放电时间在90天范围内 
		Date endTime = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(endTime);
		Integer validDay = gprsConfigInfo.getValidDay();
		calendar.add(Calendar.DATE, -validDay);// 当前时间减去期限天数
		Date time = calendar.getTime();
		
		DischargeAbstractRecord dischargeAbstractRecord = new DischargeAbstractRecord();
		dischargeAbstractRecord.setGprsId(gprsConfigInfo.getGprsId());
		dischargeAbstractRecord.setEndtime(time);
		List<DischargeAbstractRecord> dischargAbstractList = dischargeAbstractRecordMapper.getDischargAbstract(dischargeAbstractRecord);
		List<PackDataInfo> packDateInfoByIdAndGprsId = new ArrayList<PackDataInfo>();
		if (CollectionUtils.isNotEmpty(dischargAbstractList)) {
			//取电压最低的。
			DischargeAbstractRecord discharge = dischargAbstractList.stream().min(Comparator.comparing(DischargeAbstractRecord::getEndVol)).get();
			//通过gprs_id 查询返回最新list
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("gprsId", discharge.getGprsId());
			List<PackDataInfo>	pdi = packDataInfoMapper.getPackDateInfoByIdAndGprsId(map);
			if(pdi.size() == 0) {
				return null;
			}
			PackDataInfo packDateInfo = pdi.stream().max(Comparator.comparing(PackDataInfo::getRcvTime)).get();
			packDateInfoByIdAndGprsId.add(packDateInfo);
			discharge.setDischargeDetails(packDateInfoByIdAndGprsId);
			return discharge;
		}
		return null;
	}

	@Override
	public PackDataInfo getOneLatestNonStateWhichIdLessThanGivenValue(String gprsId, Integer id, String state) {
		Map<String, Object> param = new HashMap<>();
		param.put("gprsId", gprsId);
		param.put("id", id);
		param.put("state", state);
		param.put("pageNum", 0);
		param.put("pageSize", 1);
		List<PackDataInfo> list = packDataInfoMapper.getNonStateWhichIdLessThanGivenValue(param);
		if (CollectionUtils.isNotEmpty(list)) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public PackDataInfo getOneLatestNonStateWhichIdGreaterThanGivenValue(String gprsId, Integer id, String state) {
		Map<String, Object> param = new HashMap<>();
		param.put("gprsId", gprsId);
		param.put("id", id);
		param.put("state", state);
		param.put("pageNum", 0);
		param.put("pageSize", 1);
		List<PackDataInfo> list = packDataInfoMapper.getNonStateWhichIdLessThanGivenValue(param);
		if (CollectionUtils.isNotEmpty(list)) {
			return list.get(0);
		}
		return null;
	}
	
	
	private List<PackDataInfo> getRealDischargeRecord(List<PackDataInfo> dischargeList) {
		if (CollectionUtils.isEmpty(dischargeList)) {
			return null;
		}
		dischargeList = dischargeList.stream().sorted(Comparator.comparing(PackDataInfo::getId)).collect(Collectors.toList());
		int count = 0;
		int startIndex = 0;
		int endIndex = 0;
		for (int i = 0; i < dischargeList.size(); i++) {
			if (DISCHARGE_STATE.equals(dischargeList.get(i).getState())) {
				count ++;
			}else {
				count = 0;
			}
			if (count >= 10) {
				endIndex = i + 1;
				startIndex = i - 9;
				break;
			}
		}
		int temp = endIndex;
		if (count >= 10) {
			for (int i = startIndex; i >= 0; i--) {
				if (!DISCHARGE_STATE.equals(dischargeList.get(i).getState())) {
					startIndex = i + 1;
					break;
				}
			}
			for (int i = endIndex; i < dischargeList.size(); i++) {
				if (!DISCHARGE_STATE.equals(dischargeList.get(i).getState())) {
					endIndex = i;
					break;
				}
			}
		}
		if (temp == endIndex) {
			//说明往后一直没有找到,没有乱跳的数据
			endIndex = dischargeList.size();
		}
		return dischargeList.subList(startIndex, endIndex);
	}
	
	@Override
	public List<PackDataInfo> getRawDischargeHistory(String gprsId, int forwardCount, int backwardCount) {
		GprsConfigInfo record = new GprsConfigInfo();
		record.setGprsId(gprsId);
		List<GprsConfigInfo> selectListSelective = gprsConfigInfoMapper.selectListSelective(record);
		if (CollectionUtils.isEmpty(selectListSelective)) {
			return Collections.emptyList();
		}
		DischargeAbstractRecord dischargeAbstractRecord = getCheckDischargeList(selectListSelective.get(0));
		List<PackDataInfo> checkDischargeList = null;
		if(dischargeAbstractRecord != null) {
			 checkDischargeList = dischargeAbstractRecord.getDischargeDetails();
		}
		
		if (CollectionUtils.isEmpty(checkDischargeList)) {
			return Collections.emptyList();
		}
		checkDischargeList = checkDischargeList.stream()
				.sorted(Comparator.comparing(PackDataInfo::getId))
				.collect(Collectors.toList());
		
		return forwardAndBackwardAddDatas(checkDischargeList, forwardCount, backwardCount);
	}
	
	public List<PackDataInfo> forwardAndBackwardAddDatas(List<PackDataInfo> checkDischargeList, int forwardCount, int backwardCount) {
		String gprsId = checkDischargeList.get(0).getGprsId();
		Integer minId = checkDischargeList.get(0).getId();
		Integer maxId = checkDischargeList.get(checkDischargeList.size() - 1).getId();
		List<PackDataInfo> datas = new ArrayList<>();
		// 向前查找 forwardCount 条
		Map paramLT = new HashMap<>();
		paramLT.put("id", minId);
		paramLT.put("pageNum", 0);
		paramLT.put("pageSize", forwardCount);
		paramLT.put("gprsId", gprsId);
		List<PackDataInfo> forwardList = packDataInfoMapper.getPackDataInfosWhichIdLessThanGivenValue(paramLT);
		// 向后查找 backwardCount 条
		Map paramGT = new HashMap<>();
		paramGT.put("id", maxId);
		paramGT.put("pageNum", 0);
		paramGT.put("pageSize", backwardCount);
		paramGT.put("gprsId", gprsId);
		List<PackDataInfo> backwardList = packDataInfoMapper.getPackDataInfosWhichGreaterThanGivenValue(paramGT);
		
		datas.addAll(forwardList);
		datas.addAll(checkDischargeList);
		datas.addAll(backwardList);
		return datas;
	}
	
	/**
	 * 得到手工放电的时间查询表得到数据，再前后加10条数据返回
	 * 如果存在记录，自动刷新放电曲线缓存
	 * @param manualRecord
	 * @return 不在范围内则返回null
	 */
	@Override
	public List<PackDataInfo> verifyDischargeRecord(DischargeManualRecord manualRecord) {
		if (manualRecord == null || manualRecord.getGprsId() == null) {
			return null;
		}
		manualRecord.setReportRemark(null);
		Date startTime = manualRecord.getDischargeStartTime() == null ? new Date() : manualRecord.getDischargeStartTime();
		Date endTime = manualRecord.getDischargeEndTime() == null ? new Date() : manualRecord.getDischargeEndTime();
		String gprsId = manualRecord.getGprsId();
		BigDecimal startVol = manualRecord.getDischargeForwordVol() == null ? BigDecimal.ZERO : manualRecord.getDischargeForwordVol();
		BigDecimal endVol = manualRecord.getDischargeBackVol() == null ? BigDecimal.ZERO : manualRecord.getDischargeBackVol();
		
		Map<String, Object> record = new HashMap<>();
		record.put("gprsId", gprsId);
//		record.put("startTime", MyDateUtils.add(startTime, Calendar.MINUTE, -30));
//		record.put("endTime", MyDateUtils.add(endTime, Calendar.MINUTE, 30));
		record.put("startTime", startTime);
		record.put("endTime", endTime);
		List<PackDataInfo> packDataInfos = packDataInfoMapper.selectListByTime(record);
		if (CollectionUtils.isEmpty(packDataInfos)) {
			DischargeManualRecord dischargeManualRecord = new DischargeManualRecord();
			dischargeManualRecord.setId(manualRecord.getId());
			dischargeManualRecord.setIsProcessed(0);
			dischargeManualRecord.setReportRemark("该时间段未找到任何记录");
			dischargeManualRecordMapper.updateByPrimaryKeySelective(dischargeManualRecord);
			return null;
		}
		int counter = 0;
		for (int i = 0; i < packDataInfos.size(); i++) {
			if (DISCHARGE_STATE.equals(packDataInfos.get(i).getState())) {
				counter++;
			}else {
				counter = 0;
			}
			if (counter >= 1) {
				break;
			}
		}
		if (counter < 1) {
			manualRecord.setReportRemark("该时间段没有找到放电态记录");
		}
		packDataInfos = packDataInfos.stream().sorted(Comparator.comparing(PackDataInfo::getRcvTime)).collect(Collectors.toList());
		packDataInfos = forwardAndBackwardAddDatas(packDataInfos, 10, 10);
		// 刷新放电缓存
		redisCacheAgent.refreshCache(Constant.DISHISTORY_KEY_PREFIX + gprsId, packDataInfos);
		return packDataInfos;
	}
	
	public List<PackDataInfo> forwardLookup(Integer startId, String gprsId, Date startTime, int size) {
		Map<String, Object> paramMap = Maps.newHashMap();
		paramMap.put("gprsId", gprsId);
		paramMap.put("id", startId);  // < 
		paramMap.put("pageNum", 0);
		paramMap.put("pageSize", size);
		paramMap.put("startTime", startTime);  // >= 
		return packDataInfoMapper.getPackDataInfosWhichIdLessThanGivenValue(paramMap);
	}

	public List<PackDataInfo> backwardLookup(Integer startId, String gprsId, Date endTime, int size) {
		Map<String, Object> paramMap = Maps.newHashMap();
		paramMap.put("gprsId", gprsId);
		paramMap.put("id", startId);   // >
		paramMap.put("pageNum", 0);
		paramMap.put("pageSize", size);
		paramMap.put("endTime", endTime);  // <= 
		return packDataInfoMapper.getPackDataInfosWhichGreaterThanGivenValue(paramMap);
	}
	
	/**
	 * 得到查找放电记录的前后时间差
	 * @return 默认60分钟
	 */
	private int getDiffTime() {
		int diffTime = 60;
		Parameter record = new Parameter();
		record.setParameterCode("diffTime");
		List<Parameter> selectListSelective = parameterMapper.selectListSelective(record);
		if (CollectionUtils.isNotEmpty(selectListSelective)) {
			diffTime = Integer.parseInt(selectListSelective.get(0).getParameterValue());
		}
		return diffTime;
	}

}