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
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.station.common.utils.BeanValueUtils;
import com.station.common.utils.ConvertUtils;
import com.station.common.utils.JxlsUtil;
import com.station.common.utils.MyDateUtils;
import com.station.common.utils.ReflectUtil;
import com.station.moudles.entity.CellInfo;
import com.station.moudles.entity.Company;
import com.station.moudles.entity.DischargeAbstractRecord;
import com.station.moudles.entity.DischargeManualRecord;
import com.station.moudles.entity.GprsConfigInfo;
import com.station.moudles.entity.PackDataExpandLatest;
import com.station.moudles.entity.PackDataInfo;
import com.station.moudles.entity.Parameter;
import com.station.moudles.entity.PulseDischargeSend;
import com.station.moudles.entity.StationInfo;
import com.station.moudles.entity.SugReplaceCellIndex;
import com.station.moudles.entity.SuggestCellInfo;
import com.station.moudles.helper.AbstractEvent;
import com.station.moudles.helper.ChargeEvent;
import com.station.moudles.helper.DischargeEvent;
import com.station.moudles.helper.EventParams;
import com.station.moudles.helper.LossChargeEvent;
import com.station.moudles.mapper.CellInfoMapper;
import com.station.moudles.mapper.CompanyMapper;
import com.station.moudles.mapper.DischargeManualRecordMapper;
import com.station.moudles.mapper.GprsConfigInfoMapper;
import com.station.moudles.mapper.InspectSignCellIndexMapper;
import com.station.moudles.mapper.PackDataExpandLatestMapper;
import com.station.moudles.mapper.PackDataInfoMapper;
import com.station.moudles.mapper.ParameterMapper;
import com.station.moudles.mapper.PulseDischargeSendMapper;
import com.station.moudles.mapper.RoutingInspectionsMapper;
import com.station.moudles.mapper.StationInfoMapper;
import com.station.moudles.mapper.SugReplaceCellIndexMapper;
import com.station.moudles.service.ModelCalculationService;
import com.station.moudles.service.PackDataExpandService;
import com.station.moudles.service.PackDataInfoService;
import com.station.moudles.service.ParameterService;
import com.station.moudles.service.ReportService;
import com.station.moudles.vo.report.ChargeDischargeEvent;
import com.station.moudles.vo.report.ChargeDischargeReport;
import com.station.moudles.vo.report.ChargeDischargeReportItem;
import com.station.moudles.vo.report.PulseReport;
import com.station.moudles.vo.report.PulseReportItem;
import com.station.moudles.vo.report.StationReport;
import com.station.moudles.vo.report.StationReportFilter;
import com.station.moudles.vo.report.StationReportItem;
import com.station.moudles.vo.report.SuggestionCondition;
import com.station.moudles.vo.report.SuggestionReport;
import com.station.moudles.vo.report.SuggestionReportItem;
import com.station.moudles.vo.search.SearchStationInfoPagingVo;

/**
 * Created by Jack on 9/17/2017.
 */
@Service
public class ReportServiceImpl implements ReportService {

	private final static Logger LOGGER = LoggerFactory.getLogger(ReportServiceImpl.class);
	private final static int PAGESIZE = 2000;
	private final static Map<Integer, String> carrierMap = Maps.newHashMap();
	private final static Map<Integer, String> deviceTypeMap = Maps.newHashMap();
	private final static String CELL_RESIST_PREFIX = "cellResist";
	private final static String CELL_VOL_PREFIX = "cellVol";
	private final static String CELL_TEM_PREFIX = "cellTem";
	private final static String COM_SUC_PREFIX = "comSuc";
	private final static String CELL_STR = "cellStr";
	private final static String CELL_SUC = "cellSuc";
	public final static String DISCHARGE_NAME = "放电";
	public final static String FLOATCHARGE_NAME = "浮充";
	
	static {
		carrierMap.put(1, "移动");
		carrierMap.put(2, "联通");
		carrierMap.put(3, "电信");

		deviceTypeMap.put(1, "蓄电池串联复用设备");
		deviceTypeMap.put(2, "蓄电池串联复用诊断组件");
		deviceTypeMap.put(3, "蓄电池2V监测设备");
		deviceTypeMap.put(4, "蓄电池12V监测设备");
	}

	private final StationInfoMapper stationInfoMapper;
	private final CompanyMapper companyMapper;
	private final PulseDischargeSendMapper pulseDischargeSendMapper;
	private final InspectSignCellIndexMapper inspectSignCellIndexMapper;
	private final SugReplaceCellIndexMapper sugReplaceCellIndexMapper;
	private final GprsConfigInfoMapper gprsConfigInfoMapper;
	private final PackDataExpandLatestMapper packDataExpandLatestMapper;
	private final ModelCalculationService modelCalculationService;
	private final CellInfoMapper cellInfoMapper;
	private final PackDataInfoMapper packDataInfoMapper;
	private final ParameterMapper parameterMapper;
	@Autowired
	RoutingInspectionsMapper routingInspectionsMapper;
	@Autowired
	PackDataInfoService packDataInfoSer;
	@Autowired
	PackDataExpandService packDataExpandSer;
	@Autowired
	ParameterService parameterSer;
	@Autowired
	DischargeManualRecordMapper dischargeManualRecordMapper;

	@Autowired
	public ReportServiceImpl(StationInfoMapper stationInfoMapper, CompanyMapper companyMapper,
			PulseDischargeSendMapper pulseDischargeSendMapper, InspectSignCellIndexMapper inspectSignCellIndexMapper,
			SugReplaceCellIndexMapper sugReplaceCellIndexMapper, GprsConfigInfoMapper gprsConfigInfoMapper,
			PackDataExpandLatestMapper packDataExpandLatestMapper, ModelCalculationService modelCalculationService,
			CellInfoMapper cellInfoMapper, PackDataInfoMapper packDataInfoMapper, ParameterMapper parameterMapper) {
		this.stationInfoMapper = stationInfoMapper;
		this.companyMapper = companyMapper;
		this.pulseDischargeSendMapper = pulseDischargeSendMapper;
		this.inspectSignCellIndexMapper = inspectSignCellIndexMapper;
		this.sugReplaceCellIndexMapper = sugReplaceCellIndexMapper;
		this.gprsConfigInfoMapper = gprsConfigInfoMapper;
		this.packDataExpandLatestMapper = packDataExpandLatestMapper;
		this.modelCalculationService = modelCalculationService;
		this.cellInfoMapper = cellInfoMapper;
		this.packDataInfoMapper = packDataInfoMapper;
		this.parameterMapper = parameterMapper;
	}

	@Override
	public PulseReport generatePulseReport(Integer companyId, Date startTime, Date endTime) {
		Preconditions.checkNotNull(companyId, "公司编号不能为空");
		Preconditions.checkNotNull(startTime, "开始时间不能为空");
		Preconditions.checkNotNull(endTime, "结束时间不能为空");
		if (startTime.compareTo(endTime) > 0) {
			throw new IllegalArgumentException("开始时间大于结束时间");
		}
		Company company = companyMapper.selectByPrimaryKey(companyId);
		if (company == null) {
			return null;
		}
		PulseReport pulseReport = new PulseReport();
		pulseReport.setCompanyName(company.getCompanyName());
		pulseReport.setStartTime(MyDateUtils.getDateString(startTime, "yyyy/MM/dd HH:mm:ss"));
		pulseReport.setEndTime(MyDateUtils.getDateString(endTime, "yyyy/MM/dd HH:mm:ss"));
		List<PulseReportItem> pulseReportItems = Lists.newArrayList();
		List<StationInfo> items = stationInfoMapper.getStationByCompanyId(companyId).stream()
				.filter(item -> !StringUtils.equalsIgnoreCase("-1", item.getGprsId())).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(items)) {
			List<String> gprsIds = items.stream().map(StationInfo::getGprsId).collect(Collectors.toList());
			Map paramMap = Maps.newHashMap();
			paramMap.put("gprsIds", gprsIds);
			paramMap.put("startTime", startTime);
			paramMap.put("endTime", endTime);
			List<PulseDischargeSend> sends = pulseDischargeSendMapper.getPulseDischargeSendsByTimes(paramMap);
			LOGGER.debug("获取{}条特征测试数据，公司编号:{},基站编号:{},时间:{}~{}", new Object[] { sends.size(), companyId,
					StringUtils.join(gprsIds, ","), pulseReport.getStartTime(), pulseReport.getEndTime() });

			Map<String, List<PulseDischargeSend>> sendMap = sends.stream()
					.collect(Collectors.groupingBy(PulseDischargeSend::getGprsId));
			for (StationInfo item : items) {
				List<PulseDischargeSend> list = sendMap.get(item.getGprsId());
				PulseReportItem pulseReportItem = new PulseReportItem();
				pulseReportItem.setGprsId(item.getGprsId());
				pulseReportItem.setStationName(item.getName());
				if (CollectionUtils.isEmpty(list)) {
					pulseReportItem.setCellStatusMap(Maps.newHashMap());
				} else {
					pulseReportItem.setCellStatusMap(list.stream().collect(
							Collectors.toMap(PulseDischargeSend::getPulseCell, PulseDischargeSend::getSendDone)));
				}
				pulseReportItems.add(pulseReportItem);
			}

		}
		pulseReportItems.sort(Comparator.comparing(PulseReportItem::getGprsId));
		pulseReport.setItems(pulseReportItems);
		return pulseReport;
	}

	@Override
	public SuggestionReport getSuggestionReportItems(Integer companyId, String province, String city, String district) {
		return suggestionReportCommon(companyId, province, city, district, OperatorType.Search);
	}

	@Override
	public SuggestionReport generateSuggestionReport(Integer companyId, String province, String city, String district) {
		return suggestionReportCommon(companyId, province, city, district, OperatorType.Create);
	}
	
	/**
	 *  新版整治报告，目前只计算容量
	 */
	@Override
	public void generateSuggestionReport(List<PackDataInfo> dischargeRecords, DischargeManualRecord record) {
		// 重新计算时间，容量
		modelCalculationService.calcDurationAndCapByRealRecord(dischargeRecords, record);
		DischargeManualRecord dischargeManualRecord = new DischargeManualRecord();
		dischargeManualRecord.setId(record.getId());
		dischargeManualRecord.setIsProcessed(1);
		dischargeManualRecordMapper.updateByPrimaryKeySelective(dischargeManualRecord);
	}

	private SuggestionReport suggestionReportCommon(Integer companyId, String province, String city, String district,
			OperatorType operatorType) {
		Preconditions.checkNotNull(companyId, "公司编号不能为空");
		Company company = companyMapper.selectByPrimaryKey(companyId);
		if (company == null) {
			return null;
		}
		SuggestionReport report = new SuggestionReport();
		report.setCompanyName(company.getCompanyName());
		report.setExportTime(MyDateUtils.getDateString(new Date(), "yyyy-MM-dd HH:mm:ss"));
		Map paramMap = Maps.newHashMap();
		paramMap.put("companyId3", companyId);
		paramMap.put("province", province);
		paramMap.put("city", city);
		paramMap.put("district", district);
		paramMap.put("delFlag", 0);
		paramMap.put("isGprsIdNotNull", true);
		paramMap.put("deviceType", 1);
		List<SuggestionReportItem> items = Lists.newArrayList();
		List<StationInfo> stationInfos = stationInfoMapper.getStations(paramMap).stream()
				.filter(item -> !StringUtils.equalsIgnoreCase(item.getGprsId(), "-1")).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(stationInfos)) {
			if (OperatorType.Create.equals(operatorType)) {
				createSuggestionReportHandler(report, items, stationInfos, company);
			} else {
				getSuggestionReportHandler(report, items, stationInfos, company);
			}
		}
		report.setItems(items);
		return report;
	}

	private void getSuggestionReportHandler(SuggestionReport report, List<SuggestionReportItem> items,
			List<StationInfo> stationInfos, Company company) {
		Map<String, GprsConfigInfo> configMap = Maps.newHashMap();
		Map<String, Threshold> thresholdMap = Maps.newHashMap();
		List<String> gprsIds = stationInfos.stream().map(StationInfo::getGprsId).collect(Collectors.toList());
		Map<String, StationInfo> stationInfoMap = stationInfos.stream()
				.collect(Collectors.toMap(StationInfo::getGprsId, s -> s));
		prepareSuggestionReportData(report, gprsIds, configMap, thresholdMap, stationInfoMap);
		Map<String, SugReplaceCellIndex> map = sugReplaceCellIndexMapper.getLatestByGprsIds(gprsIds).stream()
				.collect(Collectors.toMap(SugReplaceCellIndex::getGprsId, item -> item));
		for (StationInfo stationInfo : stationInfos) {
			items.add(generateSuggestionReportItem(stationInfo, company, configMap.get(stationInfo.getGprsId()),
					map.get(stationInfo.getGprsId())));
		}
	}

	private void prepareSuggestionReportData(SuggestionReport report, List<String> gprsIds,
			Map<String, GprsConfigInfo> configMap, Map<String, Threshold> thresholdMap,
			Map<String, StationInfo> stationInfoMap) {
		BigDecimal badCellResist;
		Parameter query = new Parameter();
		query.setParameterCategory("1");// 复用设备才能进行整治
		query.setParameterCode("resistance");
		List<Parameter> parms = parameterMapper.selectListSelective(query);
		if (!parms.isEmpty()) {
			badCellResist = new BigDecimal(parms.get(0).getParameterValue());
		} else {
			badCellResist = BigDecimal.valueOf(6);
		}

		List<GprsConfigInfo> gprsConfigInfos = gprsConfigInfoMapper.selectByGprsIds(gprsIds);
		configMap.putAll(gprsConfigInfos.stream().collect(Collectors.toMap(GprsConfigInfo::getGprsId, item -> item)));
		thresholdMap.putAll(gprsConfigInfos.stream().map(gprsConfigInfo -> {

			BigDecimal suggestTime = gprsConfigInfo != null && StringUtils.isNotBlank(gprsConfigInfo.getSuggestTime())
					? BigDecimal.valueOf(ConvertUtils.toDouble(gprsConfigInfo.getSuggestTime(), 3d))
					: BigDecimal.valueOf(3);

			BigDecimal marginTime = gprsConfigInfo != null && gprsConfigInfo.getMarginTime() != null
					? gprsConfigInfo.getMarginTime()
					: BigDecimal.ONE;

			// BigDecimal badCellResist = gprsConfigInfo != null &&
			// gprsConfigInfo.getBadCellResist() != null ?
			// gprsConfigInfo.getBadCellResist() : BigDecimal.valueOf(6);

			BigDecimal checkDischargeVol = gprsConfigInfo != null && gprsConfigInfo.getCheckDischargeVol() != null
					? gprsConfigInfo.getCheckDischargeVol()
					: BigDecimal.valueOf(47);

			SuggestionCondition suggestionCondition = initSuggestionCondition(gprsConfigInfo, stationInfoMap);
			// 得到内阻平均值，截止电压还未赋值
			List<SuggestCellInfo> suggestCellInfos = packDataExpandSer.getResistanceAverge(gprsConfigInfo,null);

			return new Threshold(gprsConfigInfo.getGprsId(), marginTime, suggestTime, badCellResist, checkDischargeVol,
					suggestionCondition, suggestCellInfos);
		}).collect(Collectors.toMap(Threshold::getGprsId, item -> item)));

		if (MapUtils.isNotEmpty(thresholdMap)) {
			Threshold threshold = thresholdMap.values().iterator().next();
			report.setMarginTime(threshold.getMarginTime().toString());
			report.setSuggestTime(threshold.getSuggestTime().toString());
			report.setBadCellResist(threshold.getBadCellResist().toString());
			report.setCheckDischargeVol(threshold.getCheckDischargeVol().toString());
		}
	}

	/**
	 * 初始化整治判断条件(是否有效核容放电、放电记录、负载电流、截止电压、放电时长、有效核容放电时长、整治支数)
	 */
	private SuggestionCondition initSuggestionCondition(GprsConfigInfo gprsConfigInfo,
			Map<String, StationInfo> stationInfoMap) {
		SuggestionCondition condition = new SuggestionCondition();
		condition.setGprsId(gprsConfigInfo.getGprsId());
		BigDecimal loadCurrent = stationInfoMap.get(gprsConfigInfo.getGprsId()).getLoadCurrent();
		if (loadCurrent == null || loadCurrent.compareTo(new BigDecimal(0)) == 0) {
			loadCurrent = null;
		}
		condition.setLoadCur(loadCurrent);
		GprsConfigInfo record = new GprsConfigInfo();
		BeanUtils.copyProperties(gprsConfigInfo, record);
	
		DischargeAbstractRecord dischargeAbstractRecord = packDataInfoSer.getCheckDischargeList(record);
		List<PackDataInfo> checkPdiRecord = null;
		if(dischargeAbstractRecord != null) {
			checkPdiRecord = dischargeAbstractRecord.getDischargeDetails();
		}
		if (CollectionUtils.isEmpty(checkPdiRecord)) {
			condition.setValidCheck(false);
			dischargeAbstractRecord = packDataInfoSer.getDischargeList(record);
			List<PackDataInfo> dischargeRecord = null;
			if(dischargeAbstractRecord != null) {
				dischargeRecord = dischargeAbstractRecord.getDischargeDetails();
			}
			if (CollectionUtils.isNotEmpty(dischargeRecord)) {
				condition.setDischargeAbstractId(dischargeAbstractRecord.getId());
				conditionHandler(condition, dischargeRecord, gprsConfigInfo.getCheckDischargeVol());
			} else if (condition.getLoadCur() != null) {
				// 没有记录，有负载电流的
				BigDecimal timePred = getTimePred(condition);
				condition.setCheckTime(timePred);
				setSuggestionNumHandler(condition);
			}
		} else {
			condition.setDischargeAbstractId(dischargeAbstractRecord.getId());
			condition.setValidCheck(true);
			conditionHandler(condition, checkPdiRecord, gprsConfigInfo.getCheckDischargeVol());
		}
		return condition;
	}

	/**
	 * 有负载电流，无放电记录时 
	 * 得到模型计算的预测时长
	 */
	private BigDecimal getTimePred(SuggestionCondition condition) {
		PackDataExpandLatest packDataExpandLatest = packDataExpandLatestMapper
				.selectByPrimaryKey(condition.getGprsId());
		BigDecimal timePred = null;
		if (packDataExpandLatest != null) {
			timePred = packDataExpandLatest.getPackDischargeTimePred();
		}
		// double capacity =
		// modelCalculationService.getStandardCapacityFromPackType(stationInfo.getPackType());
		// BigDecimal timePred = new BigDecimal(capacity).divide(condition.getLoadCur(),
		// 2, BigDecimal.ROUND_HALF_UP);
		return timePred;
	}

	private void conditionHandler(SuggestionCondition condition, List<PackDataInfo> dischargeRecord,
			BigDecimal checkDischargeVol) {
		condition.setDischargeRecord(dischargeRecord);
		// 设置负载电流
		if (condition.getLoadCur() == null) {
			BigDecimal averageCurrent = modelCalculationService.get102DischargeAverageCurrent(dischargeRecord);
			condition.setLoadCur(averageCurrent);
		}
		// 截止电压设置
		PackDataInfo pdiMax = dischargeRecord.stream().max(Comparator.comparing(PackDataInfo::getRcvTime)).get();
		condition.setEndVol(pdiMax.getGenVol());
		condition.setEndTime((condition.isValidCheck() ? "核容" : "无核") + MyDateUtils.getDateString(pdiMax.getRcvTime()));
		// 放电时长
		PackDataInfo pdiMin = dischargeRecord.stream().min(Comparator.comparing(PackDataInfo::getRcvTime)).get();
		BigDecimal dischargeTime = new BigDecimal(
				(pdiMax.getRcvTime().getTime() - pdiMin.getRcvTime().getTime()) / (1000 * 60 * 60.0));
		condition.setDischargeTime(dischargeTime);
		condition.setStartVol(pdiMin.getGenVol());
		condition.setStartTime((condition.isValidCheck() ? "核容" : "无核") + MyDateUtils.getDateString(pdiMin.getRcvTime()));
		// 核容时长
		if (condition.isValidCheck()) {
			// 有有效核容放电记录
			if (condition.getEndVol().compareTo(checkDischargeVol) <= 0) {
				condition.setCheckTime(dischargeTime);
			} else if (condition.getEndVol().compareTo(checkDischargeVol.add(new BigDecimal(1))) <= 0) {
				condition.setCheckTime(dischargeTime.multiply(new BigDecimal(1.25)));
			} else if (condition.getEndVol().compareTo(checkDischargeVol.add(new BigDecimal(2))) <= 0) {
				condition.setCheckTime(dischargeTime.multiply(new BigDecimal(1.5)));
			} else if (condition.getEndVol().compareTo(checkDischargeVol.add(new BigDecimal(3))) <= 0) {
				condition.setCheckTime(dischargeTime.multiply(new BigDecimal(1.75)));
			} else {
				condition.setCheckTime(dischargeTime.multiply(new BigDecimal(2)));
			}
		} else {
			// 无有效核容放电记录
			if (condition.getEndVol().compareTo(new BigDecimal(48)) <= 0) {
				condition.setCheckTime(dischargeTime.multiply(new BigDecimal(1.25)));
			} else if (condition.getEndVol().compareTo(new BigDecimal(49)) <= 0) {
				condition.setCheckTime(dischargeTime.multiply(new BigDecimal(1.5)));
			} else if (condition.getEndVol().compareTo(new BigDecimal(50)) <= 0) {
				condition.setCheckTime(dischargeTime.multiply(new BigDecimal(1.75)));
			} else if (condition.getEndVol().compareTo(new BigDecimal(51)) <= 0) {
				condition.setCheckTime(dischargeTime.multiply(new BigDecimal(2)));
			} else {
				condition.setCheckTime(dischargeTime.multiply(new BigDecimal(2.5)));
			}
		}
		// 整治支数
		setSuggestionNumHandler(condition);
	}

	/**
	 * 设置整治支数
	 */
	private void setSuggestionNumHandler(SuggestionCondition condition) {
		if (condition.getLoadCur() == null || condition.getCheckTime() == null) {
			return;
		}
		if (condition.getLoadCur().compareTo(new BigDecimal(15)) <= 0) {
			setSuggestionNum(condition, 7, 6, 5, 5, 5, 2, 1);
		} else if (condition.getLoadCur().compareTo(new BigDecimal(25)) <= 0) {
			setSuggestionNum(condition, 10, 9, 8, 7, 6, 5, 4);
		} else if (condition.getLoadCur().compareTo(new BigDecimal(35)) <= 0) {
			setSuggestionNum(condition, 12, 11, 11, 9, 7, 6, 5);
		} else if (condition.getLoadCur().compareTo(new BigDecimal(45)) <= 0) {
			setSuggestionNum(condition, 14, 13, 12, 10, 9, 7, 5);
		} else if (condition.getLoadCur().compareTo(new BigDecimal(55)) <= 0) {
			setSuggestionNum(condition, 15, 14, 14, 12, 10, 8, 6);
		} else {
			setSuggestionNum(condition, 16, 15, 14, 12, 10, 9, 6);
		}
	}

	private void setSuggestionNum(SuggestionCondition condition, int... num) {
		if (condition.getCheckTime().compareTo(new BigDecimal(1.25)) <= 0) {
			condition.setSuggestNum(num[0]);
		} else if (condition.getCheckTime().compareTo(new BigDecimal(1.75)) <= 0) {
			condition.setSuggestNum(num[1]);
		} else if (condition.getCheckTime().compareTo(new BigDecimal(2.25)) <= 0) {
			condition.setSuggestNum(num[2]);
		} else if (condition.getCheckTime().compareTo(new BigDecimal(2.75)) <= 0) {
			condition.setSuggestNum(num[3]);
		} else if (condition.getCheckTime().compareTo(new BigDecimal(3.25)) <= 0) {
			condition.setSuggestNum(num[4]);
		} else if (condition.getCheckTime().compareTo(new BigDecimal(3.75)) <= 0) {
			condition.setSuggestNum(num[5]);
		} else {
			condition.setSuggestNum(num[6]);
		}
	}

	private void createSuggestionReportHandler(SuggestionReport report, List<SuggestionReportItem> items,
			List<StationInfo> stationInfos, Company company) {
		Map<String, GprsConfigInfo> configMap = Maps.newHashMap();
		Map<String, Threshold> thresholdMap = Maps.newHashMap();
		List<String> gprsIds = stationInfos.stream().map(StationInfo::getGprsId).collect(Collectors.toList());
		Map<String, StationInfo> stationInfoMap = stationInfos.stream()
				.collect(Collectors.toMap(StationInfo::getGprsId, s -> s));
		prepareSuggestionReportData(report, gprsIds, configMap, thresholdMap, stationInfoMap);
		
		Map<String, List<StationInfo>> groupByNameMap = stationInfos.stream()
				.collect(Collectors.groupingBy(StationInfo::getName));
		
		// Map<String, PackDataExpandLatest> expandLatestMap =
		// packDataExpandLatestMapper.getByGprsIds(gprsIds)
		// .stream()
		// .collect(Collectors.toMap(PackDataExpandLatest::getGprsId, item -> item));
		// Map<Integer, Set<Integer>> lCategoryMap =
		// inspectSignCellIndexMapper.getLatestByStationIds(
		// stationInfos.stream().map(StationInfo::getId).collect(Collectors.toList()))
		// .stream()
		// .collect(Collectors.groupingBy(InspectSignCellIndex::getStationId,
		// Collectors.mapping(InspectSignCellIndex::getCellIndex, Collectors.toSet())));
		for (StationInfo stationInfo : stationInfos) {
			SugReplaceCellIndex sugReplaceCellIndex = suggestHandler(stationInfo, thresholdMap,groupByNameMap);
			sugReplaceCellIndexMapper.insert(sugReplaceCellIndex);
			GprsConfigInfo configInfo = configMap.get(stationInfo.getGprsId());
			items.add(generateSuggestionReportItem(stationInfo, company, configInfo, sugReplaceCellIndex));
		}
	}

	@Override
	public ChargeDischargeReport getChargeDischargeReport(Integer stationId, Date startTime, Date endTime,
			ChargeEvent chargeEvent, DischargeEvent dischargeEvent, LossChargeEvent lossChargeEvent) {
		if (stationId == null) {
			return null;
		}
		StationInfo info = new StationInfo();
		info.setId(stationId);
		List<StationInfo> stationInfos = stationInfoMapper.selectListSelective(info);
		if (CollectionUtils.isEmpty(stationInfos)) {
			return null;
		}
		StationInfo stationInfo = stationInfos.get(0);
		if (StringUtils.equalsIgnoreCase(stationInfo.getGprsId(), "-1")) {
			return null;
		}
		ChargeDischargeReport report = new ChargeDischargeReport();
		report.setStartTime(startTime != null ? MyDateUtils.getDateString(startTime, "yyyy/MM/dd") : null);
		report.setEndTime(endTime != null ? MyDateUtils.getDateString(endTime, "yyyy/MM/dd") : null);
		report.setCompanyName(stationInfo.getCompanyName3());
		report.setAddress(stationInfo.getAddress());
		report.setCarrier(carrierMap.get(stationInfo.getOperatorType()));
		report.setDeviceType(stationInfo.getDeviceType());
		report.setDeviceTypeStr(deviceTypeMap.get(stationInfo.getDeviceType()));
		report.setGprsId(stationInfo.getGprsId());
		report.setMaintainanceId(stationInfo.getMaintainanceId());
		report.setStationName(stationInfo.getName().replaceAll(" ", ""));
		report.setPackType(stationInfo.getPackType());
		report.setRoomType(stationInfo.getRoomType());
		report.setLat(stationInfo.getLat() == null ? null : stationInfo.getLat().toString());
		report.setLng(stationInfo.getLng() == null ? null : stationInfo.getLng().toString());
		report.setEvents(Lists.newArrayList());
		report.setDetails(Lists.newArrayList());
		report.setItems(Lists.newArrayList());

		List<PackDataInfo> packDataInfos = getPackDataInfosByRange(stationInfo.getGprsId(), startTime, endTime);
		if (CollectionUtils.isEmpty(packDataInfos)) {
			return report;
		}
		List<GprsConfigInfo> gprsConfigInfos = gprsConfigInfoMapper
				.selectByGprsIds(Lists.newArrayList(stationInfo.getGprsId()));
		GprsConfigInfo gprsConfigInfo = CollectionUtils.isNotEmpty(gprsConfigInfos) ? gprsConfigInfos.get(0) : null;
		if (gprsConfigInfo != null) {
			BigDecimal minCur = gprsConfigInfo.getValidDischargeCur().abs();
			BigDecimal maxCur = gprsConfigInfo.getMaxDischargeCur() == null ? BigDecimal.valueOf(100)
					: gprsConfigInfo.getMaxDischargeCur();
			maxCur = maxCur.abs();
			report.setVol(gprsConfigInfo.getValidDischargeVol().toString());
			report.setMaxCur(maxCur.toString());
			report.setMinCur(minCur.toString());
		}
		if (gprsConfigInfo == null || gprsConfigInfo.getDownCur() == null || gprsConfigInfo.getDownVol() == null) {
			LOGGER.info("不能找到相关的掉电配置，跳过计算掉电事件, 编号:{}", stationInfo.getGprsId());
		} else {
			if (lossChargeEvent != null) {
				lossChargeEvent.setGprsConfigInfo(gprsConfigInfo);
				generateEvents(stationInfo, report, packDataInfos, lossChargeEvent);
			}
		}
		if (chargeEvent != null) {
			chargeEvent.setGprsConfigInfo(gprsConfigInfo);
			generateEvents(stationInfo, report, packDataInfos, chargeEvent);
		}
		if (dischargeEvent != null) {
			dischargeEvent.setGprsConfigInfo(gprsConfigInfo);
			generateEvents(stationInfo, report, packDataInfos, dischargeEvent);
		}

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		List<PackDataInfo> validDischarges = getValidDischarge(gprsConfigInfo, packDataInfos);
		stopWatch.stop();
		LOGGER.debug("获取{}条有效放电,设备编号:{},耗时:{}",
				new Object[] { validDischarges.size(), gprsConfigInfo.getGprsId(), stopWatch });
		if (CollectionUtils.isNotEmpty(validDischarges)) {
			stopWatch.reset();
			stopWatch.start();
			List<ChargeDischargeReportItem> details = getLatestDischarges(
					validDischarges.get(validDischarges.size() - 1), packDataInfos, stationInfo);
			stopWatch.stop();
			LOGGER.debug("获取{}条最近一次有效放电详情, 设备编号:{},耗时:{}",
					new Object[] { details.size(), stationInfo.getGprsId(), stopWatch });
			if (CollectionUtils.isNotEmpty(details)) {
				report.getDetails().addAll(details);
			}
			stopWatch.reset();
			stopWatch.start();
			List<ChargeDischargeReportItem> items = getEndVoltage(validDischarges, packDataInfos, stationInfo);
			stopWatch.stop();
			LOGGER.debug("获取{}条截止电压最低放电详情, 设备编号:{},耗时:{}",
					new Object[] { items.size(), stationInfo.getGprsId(), stopWatch });
			if (CollectionUtils.isNotEmpty(items)) {
				report.getItems().addAll(items);
			}
		}
		return report;
	}

	private List<ChargeDischargeReportItem> getEndVoltage(List<PackDataInfo> validDischarges,
			List<PackDataInfo> packDataInfos, StationInfo stationInfo) {
		packDataInfos = packDataInfos.stream().sorted(Comparator.comparing(PackDataInfo::getRcvTime))
				.collect(Collectors.toList());
		/*
		 * Map<Integer, List<PackDataInfo>> map = Maps.newHashMap(); for (int i = 0; i <
		 * validDischarges.size(); i++) { PackDataInfo packDataInfo =
		 * validDischarges.get(i); List<PackDataInfo> items =
		 * getDischarges(packDataInfo, packDataInfos); if
		 * (CollectionUtils.isEmpty(items)) { continue; } map.put(i, items); }
		 * 
		 * if (MapUtils.isEmpty(map)) { return Collections.emptyList(); }
		 * 
		 * BigDecimal value = BigDecimal.valueOf(Integer.MAX_VALUE); Integer matchedKey
		 * = null; for (Map.Entry<Integer, List<PackDataInfo>> entry : map.entrySet()) {
		 * List<PackDataInfo> list = entry.getValue(); BigDecimal vol =
		 * list.get(list.size() - 1).getGenVol(); if (value.compareTo(vol) > 0) { value
		 * = vol; matchedKey = entry.getKey(); } } return
		 * convertToReportItem(map.get(matchedKey));
		 */
		PackDataInfo packDataInfo = validDischarges.stream().min(Comparator.comparing(PackDataInfo::getGenVol)).get();
		List<PackDataInfo> items = getDischarges(packDataInfo, packDataInfos);
		return convertToReportItem(items, stationInfo);
	}

	private List<PackDataInfo> getDischarges(PackDataInfo packDataInfo, List<PackDataInfo> items) {
		int index = 0;
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getId().equals(packDataInfo.getId())) {
				index = i;
				break;
			}
		}
		if (index < 10) {
			return Collections.emptyList();
		}
		// 向前找连续10条非放电态的数据
		int startIndex = 0;
		int count = 0;
		for (int i = index - 1; i >= 0; i--) {
			PackDataInfo item = items.get(i);
			if (!ModelCalculationServiceImpl.DISCHARGE_NAME.equals(item.getState())) {
				count++;
			} else {
				count = 0;
			}
			if (count >= 10) {
				startIndex = i;
				break;
			}
		}
		if (count < 10) {
			List<PackDataInfo> forwardLookup = forwardLookup10Datas(items.get(0).getId(), packDataInfo.getGprsId());
			if (CollectionUtils.isEmpty(forwardLookup) || forwardLookup.size() < 10) {
				startIndex = 0;
			} else {
				List<PackDataInfo> collect = forwardLookup.stream()
						.sorted(Comparator.comparing(PackDataInfo::getId).reversed()).collect(Collectors.toList());
				collect = collect.subList(0, 10 - count);
				if (stateVerify(collect, "放电", true)) {
					startIndex = 0;
				} else {
					return Collections.emptyList();
				}
			}
		}
		// 向后找连续10条非放电态的数据
		count = 0;
		int endIndex = 0;
		for (int i = index + 1; i < items.size(); i++) {
			PackDataInfo item = items.get(i);
			if (!ModelCalculationServiceImpl.DISCHARGE_NAME.equals(item.getState())) {
				count++;
			} else {
				count = 0;
			}
			if (count >= 10) {
				endIndex = i + 1;
				break;
			}
		}
		if (count < 10) {
			List<PackDataInfo> backwardLookup = backwardLookup10Datas(items.get(items.size() - 1).getId(),
					packDataInfo.getGprsId());
			if (CollectionUtils.isEmpty(backwardLookup) || backwardLookup.size() < 10) {
				endIndex = items.size();
			} else {
				List<PackDataInfo> collect = backwardLookup.stream().sorted(Comparator.comparing(PackDataInfo::getId))
						.collect(Collectors.toList());
				collect = collect.subList(0, 10 - count);
				if (stateVerify(collect, "放电", true)) {
					endIndex = items.size();
				} else {
					return Collections.emptyList();
				}
			}
		}
		return items.subList(startIndex, endIndex);
	}

	private List<ChargeDischargeReportItem> getLatestDischarges(PackDataInfo packDataInfo, List<PackDataInfo> items,
			StationInfo stationInfo) {
		items = items.stream().sorted(Comparator.comparing(PackDataInfo::getRcvTime)).collect(Collectors.toList());
		List<PackDataInfo> packDataInfos = getDischarges(packDataInfo, items);
		if (CollectionUtils.isEmpty(packDataInfos)) {
			return Collections.emptyList();
		}
		return convertToReportItem(packDataInfos, stationInfo);
	}

	private List<ChargeDischargeReportItem> convertToReportItem(List<PackDataInfo> packDataInfos,
			StationInfo stationInfo) {
		return packDataInfos.stream().map(item -> {
			ChargeDischargeReportItem reportItem = new ChargeDischargeReportItem();
			reportItem.setState(item.getState());
			reportItem.setTime(MyDateUtils.getDateString(item.getRcvTime(), "yyyy/MM/dd HH:mm:ss"));
			reportItem.setCur(item.getGenCur().toString());
			reportItem.setVol(item.getGenVol().toString());
			Map<String, String> cellMap = Maps.newHashMap();
			for (int i = 1; i < stationInfo.getCellCount() + 1; i++) {
				BigDecimal bigDecimal = (BigDecimal) BeanValueUtils.getValue("cellVol" + i, item);
				cellMap.put(String.valueOf(i), bigDecimal == null ? StringUtils.EMPTY : bigDecimal.toString());
			}
			reportItem.setCellMap(cellMap);
			return reportItem;
		}).collect(Collectors.toList());
	}

	private List<PackDataInfo> getValidDischarge(GprsConfigInfo gprsConfigInfo, List<PackDataInfo> packDataInfos) {
		if (gprsConfigInfo == null) {
			return Collections.emptyList();
		}
		int counter = 0;
		List<PackDataInfo> items = Lists.newArrayList();
		for (int i = 0; i < packDataInfos.size(); i++) {
			if (isValidDischarge(packDataInfos.get(i), gprsConfigInfo)) {
				counter++;
			} else {
				counter = 0;
			}
			if (counter >= 2) {
				items.add(packDataInfos.get(i));
				counter = 0;
			}
		}

		/*
		 * List<PackDataInfo> items = Lists.newArrayList(); for (PackDataInfo item :
		 * packDataInfos) { boolean res =
		 * ModelCalculationServiceImpl.DISCHARGE_NAME.equals(item.getState()); if (!res)
		 * { continue; } res =
		 * item.getGenVol().compareTo(gprsConfigInfo.getValidDischargeVol()) <= 0; if
		 * (!res) { continue; } BigDecimal minCur =
		 * gprsConfigInfo.getValidDischargeCur().abs(); BigDecimal maxCur =
		 * gprsConfigInfo.getMaxDischargeCur() == null ? BigDecimal.valueOf(100) :
		 * gprsConfigInfo.getMaxDischargeCur(); maxCur = maxCur.abs(); res =
		 * (item.getGenCur().compareTo(minCur) >= 0 &&
		 * item.getGenCur().compareTo(maxCur) <= 0) ||
		 * (item.getGenCur().compareTo(maxCur.multiply(BigDecimal.valueOf(-1))) >= 0 &&
		 * item.getGenCur().compareTo(minCur.multiply(BigDecimal.valueOf(-1))) <= 0); if
		 * (!res) { continue; } items.add(item); }
		 */
		return items.stream().sorted(Comparator.comparing(PackDataInfo::getRcvTime)).collect(Collectors.toList());
	}

	private boolean isValidDischarge(PackDataInfo item, GprsConfigInfo gprsConfigInfo) {
		boolean res = ModelCalculationServiceImpl.DISCHARGE_NAME.equals(item.getState());
		if (!res) {
			return false;
		}
		res = item.getGenVol().compareTo(gprsConfigInfo.getValidDischargeVol()) <= 0;
		if (!res) {
			return false;
		}
		BigDecimal minCur = gprsConfigInfo.getValidDischargeCur().abs();
		BigDecimal maxCur = gprsConfigInfo.getMaxDischargeCur() == null ? BigDecimal.valueOf(100)
				: gprsConfigInfo.getMaxDischargeCur();
		maxCur = maxCur.abs();
		res = (item.getGenCur().compareTo(minCur) >= 0 && item.getGenCur().compareTo(maxCur) <= 0)
				|| (item.getGenCur().compareTo(maxCur.multiply(BigDecimal.valueOf(-1))) >= 0
						&& item.getGenCur().compareTo(minCur.multiply(BigDecimal.valueOf(-1))) <= 0);
		if (!res) {
			return false;
		}
		return true;
	}

	private void generateEvents(StationInfo stationInfo, ChargeDischargeReport report, List<PackDataInfo> packDataInfos,
			AbstractEvent event) {
		List<ChargeDischargeEvent> events = event.generateEvents(stationInfo.getGprsId(), packDataInfos, this);
		if (CollectionUtils.isNotEmpty(events)) {
			// 取出来的数据是按时间降序的，需求要按时间升序
			Collections.reverse(events);
			report.getEvents().addAll(events);
		}
	}

	/**
	 * 得到pack_data_info数据，时间降序
	 * 
	 * @param gprsId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	private List<PackDataInfo> getPackDataInfosByRange(String gprsId, Date startTime, Date endTime) {
		List<PackDataInfo> packDataInfos = Lists.newArrayList();
		int pageNum = 0;
		int pageSize = 5000;
		int queryCount = 0;
		while (true) {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			Map<String, Object> paramMap = Maps.newHashMap();
			paramMap.put("gprsId", gprsId);
			paramMap.put("startTime", startTime);
			paramMap.put("endTime", endTime);
			paramMap.put("pageNum", pageNum);
			paramMap.put("pageSize", pageSize);
			List<PackDataInfo> items = packDataInfoMapper.getPackDataInfosByTimes(paramMap);
			queryCount++;
			LOGGER.debug("第{}次查询---->获取{}条测试数据,耗时:{}", new Object[] { queryCount, items.size(), stopWatch });
			if (CollectionUtils.isEmpty(items)) {
				break;
			}
			packDataInfos.addAll(items);
			if (items.size() < pageSize) {
				break;
			}
			pageNum += pageSize;
		}
		packDataInfos.sort(Comparator.comparing(PackDataInfo::getRcvTime));
		Collections.reverse(packDataInfos);
		LOGGER.debug("获取{}条测试数据, 基站编号:{}, 时间:{}~{}", new Object[] { packDataInfos.size(), gprsId,
				MyDateUtils.getDateString(startTime), MyDateUtils.getDateString(endTime) });
		return packDataInfos;
	}

	@Override
	public SuggestionReport generateSuggestionReport(Integer stationId) {
		if (stationId == null) {
			return null;
		}
		StationInfo stationInfo = stationInfoMapper.selectByPrimaryKey(stationId);
		if (stationInfo == null || StringUtils.equalsIgnoreCase(stationInfo.getGprsId(), "-1")) {
			return null;
		}
		Company company = companyMapper.selectByPrimaryKey(stationInfo.getCompanyId3());
		if (company == null) {
			return null;
		}
		SuggestionReport suggestionReport = new SuggestionReport();
		suggestionReport.setCompanyName(company.getCompanyName());
		suggestionReport.setExportTime(MyDateUtils.getDateString(new Date(), "yyyy-MM-dd HH:mm:ss"));
		List<SuggestionReportItem> items = Lists.newArrayList();
		createSuggestionReportHandler(suggestionReport, items, Lists.newArrayList(stationInfo), company);
		suggestionReport.setItems(items);
		return suggestionReport;
	}

	private SuggestionReportItem generateSuggestionReportItem(StationInfo stationInfo, Company company,
			GprsConfigInfo configInfo, SugReplaceCellIndex sugReplaceCellIndex) {
		SuggestionReportItem item = new SuggestionReportItem();
		item.setAddress(stationInfo.getAddress());
		item.setCarrier(carrierMap.get(stationInfo.getOperatorType()));
		item.setCompanyName(company.getCompanyName());
		item.setDeviceType(configInfo == null ? null : deviceTypeMap.get(configInfo.getDeviceType()));
		item.setGprsId(stationInfo.getGprsId());
		item.setMaintainanceId(stationInfo.getMaintainanceId());
		item.setStationName(stationInfo.getName());
		// 淘汰
		List<Integer> list1 = Lists.newArrayList();
		// 利旧
		List<Integer> list2 = Lists.newArrayList();
		if (sugReplaceCellIndex != null) {
			BeanUtils.copyProperties(sugReplaceCellIndex, item);
			for (int i = 1; i < 25; i++) {
				Integer replaceValue = (Integer) BeanValueUtils.getValue("cellReplace" + i, sugReplaceCellIndex);
				Integer typeValue = (Integer) BeanValueUtils.getValue("cellType" + i, sugReplaceCellIndex);
				if (replaceValue != null && typeValue != null && replaceValue == 1) {
					if (typeValue == 1) {
						list1.add(i);
					} else if (typeValue == 0) {
						list2.add(i);
					}
				}
			}
		}
		if (CollectionUtils.isNotEmpty(list1)) {
			item.setSuggestion1("更换" + StringUtils.join(list1, "、") + "号单体（淘汰)");
		} else {
			item.setSuggestion1("暂不更换(淘汰)");
		}
		if (CollectionUtils.isNotEmpty(list2)) {
			item.setSuggestion2("更换" + StringUtils.join(list2, "、") + "号单体（利旧)");
		} else {
			item.setSuggestion2("暂不更换(利旧)");
		}
		return item;
	}

	private SugReplaceCellIndex suggestHandler(StationInfo stationInfo, Map<String, Threshold> thresholdMap, 
			Map<String, List<StationInfo>> groupByNameMap) {
		final String gprsId = stationInfo.getGprsId();
		List<Integer> processedIndexes = new ArrayList<Integer>();
		SugReplaceCellIndex sugReplaceCellIndex = new SugReplaceCellIndex();
		sugReplaceCellIndex.setGprsId(gprsId);
		processLCategory(stationInfo, processedIndexes, sugReplaceCellIndex);
		// PackDataExpandLatest packDataExpandLatest = expandLatestMap.get(gprsId);
		Threshold threshold = thresholdMap.get(gprsId);
		processKCategory(threshold, processedIndexes, sugReplaceCellIndex);
		threshold.setlkCategoryCount(processedIndexes.size());
		// 增加逻辑，整治支数，最少为巡检标记支数和内阻过大的个数
		SuggestionCondition condition = threshold.getCondition();
		if (condition.getSuggestNum() == null || processedIndexes.size() < condition.getSuggestNum()) {
			processNCategory(processedIndexes, sugReplaceCellIndex);
			processImCategory(stationInfo, threshold, processedIndexes, sugReplaceCellIndex);
		}
		// 剩下未处理的，全部标记为 不更换
		processRemain(processedIndexes, sugReplaceCellIndex);
		// -------------记录该次整治中，需要更换的单体数----
		int replaceNum = 0;
		List<Object> cellReplaceList = ReflectUtil.getValueByStartsWith(sugReplaceCellIndex, "cellReplace");
		for (Object object : cellReplaceList) {
			if (object != null && Integer.valueOf(String.valueOf(object)) == 1) {
				replaceNum++;
			}
		}
		sugReplaceCellIndex.setReplaceNum(replaceNum);
		// 记录该次整治的负载电流（人为设置/自己算的）
		sugReplaceCellIndex.setLoadCurrent(condition.getLoadCur());
		// 记录该次整治的整治模式（有“扩容”，整治模式优先给“扩容”）
		String rectificationMode = sugReplaceCellIndex.getRectificationMode();
		if (replaceNum == 0) {
			if (StringUtils.isEmpty(rectificationMode)) {
				sugReplaceCellIndex.setRectificationMode("均衡整治");
			}
		}else if (replaceNum < 24){
			if (StringUtils.isEmpty(rectificationMode)) {
				sugReplaceCellIndex.setRectificationMode("单体整治");
			}
		}else {
			sugReplaceCellIndex.setRectificationMode("整组整治");
		}
		// 放电记录信息
		sugReplaceCellIndex.setDischargeStartTime(condition.getStartTime());
		sugReplaceCellIndex.setDischargeEndTime(condition.getEndTime());
		sugReplaceCellIndex.setDischargeStartVol(condition.getStartVol());
		sugReplaceCellIndex.setDischargeEndVol(condition.getEndVol());
		sugReplaceCellIndex.setDischargeTime(condition.getDischargeTime());
		sugReplaceCellIndex.setDischargeAbstractRecordId(condition.getDischargeAbstractId());
		// 只有当同一个站点有多个电池组时，才设置remark。格式 1：n
		List<StationInfo> list = groupByNameMap.get(stationInfo.getName());
		if (CollectionUtils.isNotEmpty(list) && list.size() > 1) {
			sugReplaceCellIndex.setRemark("1:" + list.size());
		}
		return sugReplaceCellIndex;
	}

	/**
	 * 
	 * @param stationInfo
	 * @param replaceValue
	 *            1,更换，0,不更换
	 * @param typeValue
	 *            1,淘汰, 0, 利旧
	 * @param processedIndexes
	 * @param sugReplaceCellIndex
	 * @param categoryName
	 */
	private void bindCellValue(Integer replaceValue, Integer typeValue, List<Integer> processedIndexes,
			SugReplaceCellIndex sugReplaceCellIndex, String categoryName) {
		List<Integer> cells = Lists.newArrayList();
		for (int cell = 1; cell < 25; cell++) {
			if (CollectionUtils.isNotEmpty(processedIndexes) && processedIndexes.contains(cell)) {
				continue;
			}
			cells.add(cell);
			bindValue(cell, replaceValue, typeValue, sugReplaceCellIndex);
		}
		if (CollectionUtils.isNotEmpty(cells)) {
			processedIndexes.addAll(cells);
		}
		LOGGER.info("{}个单体({})属于{}类,基站编号:{}", new Object[] { cells.size(), StringUtils.join(cells, ","), categoryName,
				sugReplaceCellIndex.getGprsId() });
	}

	/**
	 * 剩下未处理的，全部标记为 不更换
	 */
	private void processRemain(List<Integer> processedIndexes, SugReplaceCellIndex sugReplaceCellIndex) {
		if (processedIndexes.size() >= 24) {
			return;
		}
		bindCellValue(0, 0, processedIndexes, sugReplaceCellIndex, "剩下未处理的");
	}

	/**
	 * I & M 类别
	 */
	private void processImCategory(StationInfo stationInfo, Threshold threshold, List<Integer> processedIndexes,
			SugReplaceCellIndex sugReplaceCellIndex) {
		if (processedIndexes.size() >= 24) {
			return;
		}
		SuggestionCondition condition = threshold.getCondition();
		if (condition.getLoadCur() != null) { // 有负载电流
			if (condition.getLoadCur().compareTo(new BigDecimal(90)) > 0) {
				// 整治结果，负载过大，建议扩容( condition.getLoadCur() A)
//				ReflectUtil.setValueByKet(sugReplaceCellIndex, "remark", "负载过大，建议扩容(" + condition.getLoadCur() + "A)");
				sugReplaceCellIndex.setRectificationMode("扩容");
			} else {
				if (CollectionUtils.isNotEmpty(condition.getDischargeRecord())) {
					//有放电记录的
					
					iHandler(threshold, processedIndexes, sugReplaceCellIndex);
					mHandler(threshold.getCondition().getCheckTime(), processedIndexes, sugReplaceCellIndex);

					if (condition.isValidCheck()) {
						// 有负载电流、有效核容放电记录
						// 比较整治支数不够的，电压小的 '淘汰、更换'
						mHandlerOfSugNum(threshold, processedIndexes, sugReplaceCellIndex,
								Comparator.comparing(SuggestCellInfo::getEndVol));

					} else {
						// 有负载电流、无效核容放电记录有放电记录
						// 比较整治支数不够的，内阻大的 '淘汰、更换'
						mHandlerOfSugNum(threshold, processedIndexes, sugReplaceCellIndex,
								Comparator.comparing(SuggestCellInfo::getCellResist).reversed());
					}
				} else {
					// 有负载电流、没有放电记录
					mHandlerNotRecord(threshold, processedIndexes, sugReplaceCellIndex);
				}
			}
		} else {
			// 无负载电流、无放电记录
			mHandlerNotCurAndRecord(stationInfo, threshold, processedIndexes, sugReplaceCellIndex);
		}

	}

	/**
	 * 无负载电流、无放电记录 更据 浮冲电压、内阻平均值判断是否淘汰 浮充电压>2.3 && resist_average_n>3 浮充电压<2.3 &&
	 * resist_average_n>2
	 */
	private void mHandlerNotCurAndRecord(StationInfo stationInfo, Threshold threshold, List<Integer> processedIndexes,
			SugReplaceCellIndex sugReplaceCellIndex) {
		if (processedIndexes.size() > 24) {
			return;
		}
		List<SuggestCellInfo> suggestCellInfos = threshold.getSuggestCellInfos();
		if (CollectionUtils.isEmpty(suggestCellInfos)) {
//			ReflectUtil.setValueByKet(sugReplaceCellIndex, "remark", "无负载电流、无放电记录、无平均内阻");
			return;
		}
		// 最近12条的浮冲电压
		Map<Integer, BigDecimal> cellVoltage = modelCalculationService.calculateValidVoltage(stationInfo, new Date());
		if (MapUtils.isEmpty(cellVoltage)) {
//			ReflectUtil.setValueByKet(sugReplaceCellIndex, "remark", "无负载电流、无放电记录、无浮冲电压");
			return;
		}
		for (SuggestCellInfo sugCellInfo : suggestCellInfos) {
			if (processedIndexes.contains(sugCellInfo.getCellIndex())) {
				continue;
			}
			boolean isEliminated = (sugCellInfo.getCellResist().compareTo(new BigDecimal(3)) > 0
					&& cellVoltage.get(sugCellInfo.getCellIndex()).compareTo(new BigDecimal(2.3)) > 0)
					|| (sugCellInfo.getCellResist().compareTo(new BigDecimal(2)) > 0
							&& cellVoltage.get(sugCellInfo.getCellIndex()).compareTo(new BigDecimal(2.3)) < 0);

			if (isEliminated) {
				bindValue(sugCellInfo.getCellIndex(), 1, 1, sugReplaceCellIndex);
				processedIndexes.add(sugCellInfo.getCellIndex());
			}
		}
	}

	/**
	 * 有负载电流无记录
	 */
	private void mHandlerNotRecord(Threshold threshold, List<Integer> processedIndexes,
			SugReplaceCellIndex sugReplaceCellIndex) {
		if (processedIndexes.size() > 24) {
			return;
		}
		SuggestionCondition condition = threshold.getCondition();
		if (condition.getCheckTime() == null) {
//			ReflectUtil.setValueByKet(sugReplaceCellIndex, "remark", "无放电记录、无模型计算记录");
			return;
		}
		if (condition.getLoadCur() != null) {
			if (condition.getCheckTime().compareTo(threshold.getSuggestTime().add(threshold.getMarginTime())) >= 0) {
				bindCellValue(0, 0, processedIndexes, sugReplaceCellIndex, "M类，有负载电流无记录");
			} else if (condition.getCheckTime().compareTo(new BigDecimal(1)) < 0){
				bindCellValue(1, 1, processedIndexes, sugReplaceCellIndex, "M类，有负载电流无记录");
			}
			// 比较整治支数不够的，内阻大的 '淘汰、更换'
			mHandlerOfSugNum(threshold, processedIndexes, sugReplaceCellIndex,
					Comparator.comparing(SuggestCellInfo::getCellResist).reversed());
		}
	}

	/**
	 * 更据整治个数重新整治
	 */
	private void mHandlerOfSugNum(Threshold threshold, List<Integer> processedIndexes,
			SugReplaceCellIndex sugReplaceCellIndex, Comparator<? super SuggestCellInfo> comparator) {
		SuggestionCondition condition = threshold.getCondition();
		if (condition.getCheckTime() == null || processedIndexes.size() >= 24) {
			return;
		}
		Integer suggestNum = condition.getSuggestNum();
		logger.info("gprsId-->{},初始整治支数-->{}", condition.getGprsId(), suggestNum);
		if (suggestNum != null) {
			List<SuggestCellInfo> suggestCellInfos = getSuggestCellInfo(threshold);
			if (CollectionUtils.isEmpty(suggestCellInfos)) {
				return;
			}
			// 按指定规则排序
			suggestCellInfos = suggestCellInfos.stream().sorted(comparator).collect(Collectors.toList());

			// 得到所有已处理的 ‘更换、淘汰’的单体
			List<Integer> eliminatedCellsIndex = m2Handler(threshold, processedIndexes, sugReplaceCellIndex,
					suggestCellInfos);
			if (CollectionUtils.isEmpty(eliminatedCellsIndex)) {
				return;
			}
			
			// 得到非  L、K 类的淘汰单体
			List<Integer> eliminatedCellsIndexNonL = eliminatedCellsIndex
					.subList(threshold.getlkCategoryCount(), eliminatedCellsIndex.size());
			
			// 得到非  L 类的 ‘更换、淘汰’的单体信息
			List<SuggestCellInfo> eliminatedCellInfoNonLK = suggestCellInfos.stream()
					.filter(r -> eliminatedCellsIndexNonL.contains(r.getCellIndex())).collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(eliminatedCellInfoNonLK)) {
				// 重新计算整治 支数
				suggestNum = resetSuggestNum(condition, eliminatedCellInfoNonLK);
				logger.info("gprsId-->{},重新计算后的整治支数-->{}", condition.getGprsId(), suggestNum);
				// 按指定规则反序
				eliminatedCellInfoNonLK = eliminatedCellInfoNonLK.stream().sorted(comparator.reversed()).collect(Collectors.toList());
				// 去除多余的 ‘更换、淘汰’(去除电压大或内阻小的)
				if (eliminatedCellInfoNonLK.size() + threshold.getlkCategoryCount() > suggestNum) {
					int oldNum = eliminatedCellInfoNonLK.size() + threshold.getlkCategoryCount();
					for (int i = 0; i < oldNum - suggestNum && i < eliminatedCellInfoNonLK.size(); i++) {
						bindValue(eliminatedCellInfoNonLK.get(i).getCellIndex(), 0, 0, sugReplaceCellIndex);
					}
				}
			}
		}
	}

	private List<SuggestCellInfo> getSuggestCellInfo(Threshold threshold) {
		List<SuggestCellInfo> suggestCellInfos = threshold.getSuggestCellInfos();
		if (CollectionUtils.isEmpty(suggestCellInfos)) {
			return null;
		}
		List<PackDataInfo> dischargeRecord = threshold.getCondition().getDischargeRecord();
		if (CollectionUtils.isEmpty(dischargeRecord)) {
			return suggestCellInfos;
		}
		PackDataInfo packDataInfo = dischargeRecord.stream().max(Comparator.comparing(PackDataInfo::getRcvTime)).get();
		for (SuggestCellInfo sugCellInfo : suggestCellInfos) {
			BigDecimal endVol = (BigDecimal) ReflectUtil.getValueByKey(packDataInfo,
					CELL_VOL_PREFIX + sugCellInfo.getCellIndex());
			ReflectUtil.setValueByKet(sugCellInfo, "endVol", endVol);
		}
		return suggestCellInfos;
	}

	/**
	 * 重新计算，需整治的支数
	 */
	private Integer resetSuggestNum(SuggestionCondition condition, List<SuggestCellInfo> eliminatedCellInfo) {
		// 判断 '淘汰、更换' 中是否有单体电压为 0 或小于 1的
		if (eliminatedCellInfo.get(0).getEndVol() != null) {
			eliminatedCellInfo = eliminatedCellInfo.stream().sorted(Comparator.comparing(SuggestCellInfo::getEndVol))
					.collect(Collectors.toList());
			if (eliminatedCellInfo.get(0).getEndVol().compareTo(new BigDecimal(0)) == 0) {
				// 少 2个
				int num = condition.getSuggestNum() - 2;
				condition.setSuggestNum(num < 0 ? 0 : num);
			} else if (eliminatedCellInfo.get(0).getEndVol().compareTo(new BigDecimal(1)) < 0) {
				// 少 1个
				int num = condition.getSuggestNum() - 1;
				condition.setSuggestNum(num < 0 ? 0 : num);
			}
		}
		return condition.getSuggestNum();
	}

	/**
	 * 得到所有 更换、淘汰的单体角标 m 类判断中的 1≤pack_discharge_time_pred<suggest_time+Margin_time小时
	 * 比较特殊，当整治个数不够时，该类才添加
	 * 
	 * @param sortedList
	 *            传进来的有两种情况，按电压升序、按内阻降序
	 */
	private List<Integer> m2Handler(Threshold threshold, List<Integer> processedIndexes,
			SugReplaceCellIndex sugReplaceCellIndex, List<SuggestCellInfo> sortedList) {
		List<Integer> cellsIndex = getEliminatedCellList(processedIndexes, sugReplaceCellIndex);
		SuggestionCondition condition = threshold.getCondition();
		if (condition.getCheckTime() == null || processedIndexes.size() >= 24) {
			return cellsIndex;
		}
		Integer suggestNum = threshold.getCondition().getSuggestNum();
		if (cellsIndex.size() < suggestNum) {
			if (condition.getCheckTime().compareTo(new BigDecimal(1)) >= 1 && condition.getCheckTime()
					.compareTo(threshold.getSuggestTime().add(threshold.getMarginTime())) < 0) {
				for (SuggestCellInfo sugCellInfo : sortedList) {
					if (processedIndexes.contains(sugCellInfo.getCellIndex())) {
						continue;
					}
					bindValue(sugCellInfo.getCellIndex(), 1, 1, sugReplaceCellIndex);
					cellsIndex.add(sugCellInfo.getCellIndex());
					processedIndexes.add(sugCellInfo.getCellIndex());
					if (cellsIndex.size() == suggestNum) {
						break;
					}
				}
			}
		}
		return cellsIndex;
	}

	/**
	 * 得到 更换、淘汰的单体
	 */
	private List<Integer> getEliminatedCellList(List<Integer> processedIndexes,
			SugReplaceCellIndex sugReplaceCellIndex) {
		List<Integer> cells = Lists.newArrayList();
		for (Integer index : processedIndexes) {
			Integer cellReplace = (Integer) ReflectUtil.getValueByKey(sugReplaceCellIndex, "cellReplace" + index);
			Integer cellType = (Integer) ReflectUtil.getValueByKey(sugReplaceCellIndex, "cellType" + index);
			if (cellReplace != null && cellType != null && cellReplace == 1 && cellType == 1) {
				cells.add(index);
			}
		}
		return cells;
	}

	/**
	 * m类 pack_discharge_time_pred小于1小时
	 */
	private void mHandler(BigDecimal timePred, List<Integer> processedIndexes, SugReplaceCellIndex sugReplaceCellIndex) {
		if (timePred == null || processedIndexes.size() >= 24) {
			return;
		}
		if (timePred.compareTo(new BigDecimal(1)) < 0) {
			bindCellValue(1, 1, processedIndexes, sugReplaceCellIndex, "M类，有负载电流有记录");
		}
	}

	/**
	 * 电池组放电预测时长大于等于整治目标时长+Margin_time小时 放电截止电压 < 1的单体 记I
	 */
	private void iHandler(Threshold threshold, List<Integer> processedIndexes, SugReplaceCellIndex sugReplaceCellIndex) {
		BigDecimal checkTime = threshold.getCondition().getCheckTime();
		if (checkTime == null || processedIndexes.size() >= 24) {
			return;
		}
		if (checkTime.compareTo(threshold.getSuggestTime().add(threshold.getMarginTime())) >= 0) {
			PackDataInfo packDataInfo = threshold.getCondition().getDischargeRecord().stream()
					.max(Comparator.comparing(PackDataInfo::getRcvTime)).get();
			List<Integer> cells = Lists.newArrayList();
			for (int i = 1; i < 25; i++) {
				if (CollectionUtils.isNotEmpty(processedIndexes) && processedIndexes.contains(i)) {
					continue;
				}
				BigDecimal cellVol = (BigDecimal) ReflectUtil.getValueByKey(packDataInfo, CELL_VOL_PREFIX + i);
				if (cellVol != null && cellVol.compareTo(new BigDecimal(1)) < 0) {
					bindValue(i, 1, 1, sugReplaceCellIndex);
					cells.add(i);
				}
			}
			if (CollectionUtils.isNotEmpty(cells)) {
				processedIndexes.addAll(cells);
			}
			LOGGER.info("{}个单体({})属于I类,基站编号:{}",
					new Object[] { cells.size(), StringUtils.join(cells, ","), sugReplaceCellIndex.getGprsId() });
		}
	}

	/**
	 * N类 使用时间小于一年
	 */
	private void processNCategory(List<Integer> processedIndexes, SugReplaceCellIndex sugReplaceCellIndex) {
		if (processedIndexes.size() >= 24) {
			return;
		}
		CellInfo record = new CellInfo();
		record.setGprsId(sugReplaceCellIndex.getGprsId());
		List<CellInfo> newCellList = cellInfoMapper.selectListSelective(record);
		if (CollectionUtils.isEmpty(newCellList)) {
			return;
		}
		List<Integer> cells = Lists.newArrayList();
		for (CellInfo cellInfo : newCellList) {
			if (CollectionUtils.isNotEmpty(processedIndexes) && processedIndexes.contains(cellInfo.getCellIndex())) {
				continue;
			}
			if (cellInfo.getUseFrom() != null && cellInfo.getUsedMonth() != null && cellInfo.getUsedMonth() < 13) {
				bindValue(cellInfo.getCellIndex(), 0, 0, sugReplaceCellIndex);
				cells.add(cellInfo.getCellIndex());
			}
		}
		if (CollectionUtils.isNotEmpty(cells)) {
			processedIndexes.addAll(cells);
		}
		LOGGER.info("{}个单体({})属于N类,基站编号:{}",
				new Object[] { cells.size(), StringUtils.join(cells, ","), sugReplaceCellIndex.getGprsId() });
	}

	/**
	 * K 类别 内组值avg > bad_cell_resist 淘汰、更换
	 */
	private void processKCategory(Threshold threshold, List<Integer> processedIndexes,
			SugReplaceCellIndex sugReplaceCellIndex) {
		List<SuggestCellInfo> suggestCellInfos = threshold.getSuggestCellInfos();
		if (CollectionUtils.isEmpty(suggestCellInfos) || processedIndexes.size() >= 24) {
			return;
		}
		List<Integer> cells = Lists.newArrayList();
		for (SuggestCellInfo sugCellInfo : suggestCellInfos) {
			if (CollectionUtils.isNotEmpty(processedIndexes) && processedIndexes.contains(sugCellInfo.getCellIndex())) {
				continue;
			}
			if (sugCellInfo.getCellResist().compareTo(threshold.getBadCellResist()) > 0) {
				bindValue(sugCellInfo.getCellIndex(), 1, 1, sugReplaceCellIndex);
				cells.add(sugCellInfo.getCellIndex());
			}
		}
		if (CollectionUtils.isNotEmpty(cells)) {
			sugReplaceCellIndex.setResistHighCells(StringUtils.join(cells,","));
			processedIndexes.addAll(cells);
		}
		LOGGER.info("{}个单体({})属于K类,基站编号:{}",
				new Object[] { cells.size(), StringUtils.join(cells, ","), threshold.getGprsId() });

	}

	/**
	 * L 类别，巡检标记单体编号
	 */
	private void processLCategory(StationInfo stationInfo, List<Integer> processedCells,
			SugReplaceCellIndex sugReplaceCellIndex) {
		// 电池组最近一次整治时间（就是巡检记录 operate_type = 3） > 最近一次巡检标记的异常单体时间 则L类 为 0
		// 否则 L类按巡检人员标记的统计
		/*
		 * RoutingInspections inspections = new RoutingInspections();
		 * inspections.setOperateType(3); inspections.setStationId(stationInfo.getId());
		 * RoutingInspections latestInspections =
		 * routingInspectionsMapper.selectOneLatestSelective(inspections); if
		 * (latestInspections != null) { // 有整治记录信息 // 得到最近的整治时间 Date
		 * latestInspectionTime = latestInspections.getOperateTime(); //
		 * 以最近的整治时间，往后查询；得到有标记异常单体的记录，则 L取标记信息 Map param = new HashMap<>();
		 * param.put("stationId", stationInfo.getId()); param.put("operateType", 2);
		 * param.put("startTime", latestInspectionTime); List<RoutingInspections>
		 * inspectSignCells =
		 * routingInspectionsMapper.selectListHasInspectSignCell(param); if
		 * (CollectionUtils.isEmpty(inspectSignCells)) { return; } }
		 */
		
		List<Integer> cells = Lists.newArrayList();
		CellInfo query = new CellInfo();
		query.setGprsId(stationInfo.getGprsId());
		query.setFaultMark(1);
		List<CellInfo> cellInfos = cellInfoMapper.selectListSelective(query);
		
		if (CollectionUtils.isNotEmpty(cellInfos)) {
			for (CellInfo cellInfo : cellInfos) {
				
				cells.add(cellInfo.getCellIndex());
			}
		}
		if (CollectionUtils.isNotEmpty(cells)) {
			for (Integer cell : cells) {
				bindValue(cell, 1, 1, sugReplaceCellIndex);
			}
			sugReplaceCellIndex.setSignCells(StringUtils.join(cells, ","));
			processedCells.addAll(cells);
		}

		LOGGER.info("{}个单体({})属于L类,基站编号:{}", new Object[] { CollectionUtils.isEmpty(cells) ? 0 : cells.size(),
				StringUtils.join(cells, ","), stationInfo.getGprsId() });
	}

	private void bindValue(Integer cell, Integer replaceValue, Integer typeValue,
			SugReplaceCellIndex sugReplaceCellIndex) {
		BeanValueUtils.bindProperty("cellReplace" + cell, replaceValue, sugReplaceCellIndex);
		BeanValueUtils.bindProperty("cellType" + cell, typeValue, sugReplaceCellIndex);
	}

	private static class Threshold {
		private String gprsId;
		private BigDecimal marginTime;
		private BigDecimal suggestTime;
		private BigDecimal badCellResist;
		private BigDecimal checkDischargeVol;
		private SuggestionCondition condition;
		private List<SuggestCellInfo> suggestCellInfos;
		private int lkCategoryCount;

		public Threshold(String gprsId, BigDecimal marginTime, BigDecimal suggestTime, BigDecimal badCellResist,
				BigDecimal checkDischargeVol, SuggestionCondition condition, List<SuggestCellInfo> suggestCellInfos) {
			this.gprsId = gprsId;
			this.marginTime = marginTime;
			this.suggestTime = suggestTime;
			this.badCellResist = badCellResist;
			this.checkDischargeVol = checkDischargeVol;
			this.condition = condition;
			this.suggestCellInfos = suggestCellInfos;
		}

		public String getGprsId() {
			return gprsId;
		}

		public void setGprsId(String gprsId) {
			this.gprsId = gprsId;
		}

		public BigDecimal getMarginTime() {
			return marginTime;
		}

		public void setMarginTime(BigDecimal marginTime) {
			this.marginTime = marginTime;
		}

		public BigDecimal getSuggestTime() {
			return suggestTime;
		}

		public void setSuggestTime(BigDecimal suggestTime) {
			this.suggestTime = suggestTime;
		}

		public BigDecimal getBadCellResist() {
			return badCellResist;
		}

		public void setBadCellResist(BigDecimal badCellResist) {
			this.badCellResist = badCellResist;
		}

		public BigDecimal getCheckDischargeVol() {
			return checkDischargeVol;
		}

		public void setCheckDischargeVol(BigDecimal checkDischargeVol) {
			this.checkDischargeVol = checkDischargeVol;
		}

		public SuggestionCondition getCondition() {
			return condition;
		}

		public void setCondition(SuggestionCondition condition) {
			this.condition = condition;
		}

		public List<SuggestCellInfo> getSuggestCellInfos() {
			return suggestCellInfos;
		}

		public void setSuggestCellInfos(List<SuggestCellInfo> suggestCellInfos) {
			this.suggestCellInfos = suggestCellInfos;
		}

		public int getlkCategoryCount() {
			return lkCategoryCount;
		}

		public void setlkCategoryCount(int lkCategoryCount) {
			this.lkCategoryCount = lkCategoryCount;
		}
	}

	private enum OperatorType {
		Search, Create
	}

	private enum StationFilter {
		maxGenVol, minGenVol, maxGenCur, minGenCur, maxEnvironTem, minEnvironTem, maxCellVol, minCellVol, maxCellTem, minCellTem, maxComSuc, minComSuc, resistance
	}

	private Logger logger = LoggerFactory.getLogger("TAG");

	@Override
	public StationReport generateStationVolCurStr(StationReport stationReport) {
		StationReport report = new StationReport();
		BeanUtils.copyProperties(stationReport, report);
		report.setStartRcvTimeStr(JxlsUtil.dateFmt(stationReport.getStartRcvTime()));
		report.setEndRcvTimeStr(JxlsUtil.dateFmt(stationReport.getEndRcvTime()));
		List<StationReportItem> items = new ArrayList<>();
		report.setItems(items);
		Map<String, StationReportFilter> filter = new HashMap<>();
		report.setFilter(filter);
		// 过滤条件赋值
		StationFilter[] values = StationFilter.values();
		for (int i = 0; i < values.length; i++) {
			Parameter record = new Parameter();
			record.setParameterCode(values[i].toString());
			List<Parameter> selectListSelective = parameterMapper.selectListSelective(record);
			Map<String, Parameter> parameterMap = selectListSelective.stream()
					.collect(Collectors.toMap(Parameter::getParameterCategory, p -> p));
			StationReportFilter itemFilter = null;
			for (String key : parameterMap.keySet()) {
				if (filter.containsKey(key)) {
					itemFilter = filter.get(key);
				} else {
					itemFilter = new StationReportFilter();
					filter.put(key, itemFilter);
				}
				String parameterValue = parameterMap.get(key).getParameterValue();
				switch (values[i]) {
				case maxGenVol:
				case minGenVol:
				case maxGenCur:
				case minGenCur:
				case maxCellVol:
				case minCellVol:
				case resistance:
					BigDecimal convertDec = new BigDecimal(parameterValue);
					ReflectUtil.setValueByKet(itemFilter, values[i].toString(), convertDec);
					break;
				case maxEnvironTem:
				case minEnvironTem:
				case maxCellTem:
				case minCellTem:
				case maxComSuc:
				case minComSuc:
					Integer convertInt = new Integer(parameterValue);
					ReflectUtil.setValueByKet(itemFilter, values[i].toString(), convertInt);
					break;
				}
			}
		}

		SearchStationInfoPagingVo query = new SearchStationInfoPagingVo();
		query.setCompanyId3(report.getCompanyId3());
		query.setLinkStatus(report.getLinkStatus());
		query.setState(report.getState());
		query.setDelFlag(0);// 只查询未删除的
		query.setPageSize(null);
		query.setPageNo(null);
		StopWatch stopWatch = new StopWatch();
		stopWatch.reset();
		stopWatch.start();
		List<StationInfo> selectListSelective = stationInfoMapper.selectListSelectivePaging(query);
		stopWatch.stop();
		report.setStationTotal(selectListSelective == null ? 0 : selectListSelective.size());
		logger.info("station查询时间-->" + stopWatch.getTime());
		logger.info("station查询条数-->" + (selectListSelective == null ? 0 : selectListSelective.size()));
		for (StationInfo stationInfo : selectListSelective) {
			if (stationInfo.getLinkStatus() == null || stationInfo.getLinkStatus() == 0) {
				StationReportItem item = new StationReportItem();
				BeanUtils.copyProperties(stationInfo, item);
				item.setRemark("设备离线或未绑定设备");
				items.add(item);
				continue;
			}
			exceptionStationCheck(report, items, stationInfo);
			
		}
		return report;
	}

	private void exceptionStationCheck(StationReport report, List<StationReportItem> items, StationInfo stationInfo) {
		Map<String, Object> paramMap = Maps.newHashMap();
		paramMap.put("gprsId", stationInfo.getGprsId());
		paramMap.put("startTime", report.getStartRcvTime());
		paramMap.put("endTime", report.getEndRcvTime());
		StopWatch stopWatch = new StopWatch();
		stopWatch.reset();
		stopWatch.start();
		List<PackDataInfo> pdiByTime = packDataInfoMapper.selectListByTime(paramMap);
		stopWatch.stop();
		logger.info("packData查询时间-->" + stopWatch.getTime());
		logger.info("packData查询条数-->" + (pdiByTime == null ? 0 : pdiByTime.size()));
		if (pdiByTime == null || pdiByTime.size() < 10) {
			StationReportItem item = new StationReportItem();
			BeanUtils.copyProperties(stationInfo, item);
			item.setRemark("数据量过少");
			items.add(item);
			return;
		}
		StationReportFilter filter = report.getFilter().get(stationInfo.getDeviceType() + "");
		if (filter == null || !ReflectUtil.checkBeanNonNullWithoutSup(filter, StationReportFilter.class)) {
			StationReportItem item = new StationReportItem();
			BeanUtils.copyProperties(stationInfo, item);
			item.setRemark("建议维护设备参数没有设置");
			items.add(item);
			return;
		}
		int exceptionCount = 0; // 一个电池组，只取5条异常数据
		// 增加内阻判断
		PackDataExpandLatest expandLatest = packDataExpandLatestMapper.selectByPrimaryKey(stationInfo.getGprsId());
		CellInfo record = new CellInfo();
		record.setGprsId(stationInfo.getGprsId());
		List<CellInfo> cellList = cellInfoMapper.selectListSelective(record);
		for (PackDataInfo packDataInfo : pdiByTime) {
			StationReportItem item = new StationReportItem();
			BeanUtils.copyProperties(stationInfo, item);
			item.setRcvTime(JxlsUtil.dateFmt(packDataInfo.getRcvTime()));
			StringBuffer pdiExce = new StringBuffer();
			if (packDataInfo.getGenVol() == null) {
				pdiExce.append("总电压:").append("无值");
			} else if (packDataInfo.getGenVol().compareTo(filter.getMaxGenVol()) > 0
					|| packDataInfo.getGenVol().compareTo(filter.getMinGenVol()) < 0) {
				pdiExce.append("总电压:").append(packDataInfo.getGenVol()).append("V");
			}

			if (packDataInfo.getGenCur() == null) {
				pdiExce.append(pdiExce.length() > 0 ? ";" : "").append("总电流:").append("无值");
			} else if (packDataInfo.getGenCur().compareTo(filter.getMaxGenCur()) > 0
					|| packDataInfo.getGenCur().compareTo(filter.getMinGenCur()) < 0) {
				pdiExce.append(pdiExce.length() > 0 ? ";" : "").append("总电流:").append(packDataInfo.getGenCur())
						.append("A");
			}

			if (packDataInfo.getEnvironTem() == null) {
				pdiExce.append(pdiExce.length() > 0 ? ";" : "").append("环境温度:").append("无值");
			} else if (packDataInfo.getEnvironTem().compareTo(filter.getMaxEnvironTem()) > 0
					|| packDataInfo.getEnvironTem().compareTo(filter.getMinEnvironTem()) < 0) {
				pdiExce.append(pdiExce.length() > 0 ? ";" : "").append("环境温度:").append(packDataInfo.getEnvironTem())
						.append("℃");
			}

			for (int i = 1; i < stationInfo.getCellCount() + 1; i++) {
				StringBuffer cellExce = new StringBuffer();
				boolean hasSubDeviceExce = false;
				BigDecimal cellVol = (BigDecimal) ReflectUtil.getValueByKey(packDataInfo, CELL_VOL_PREFIX + i);
				Integer cellTem = (Integer) ReflectUtil.getValueByKey(packDataInfo, CELL_TEM_PREFIX + i);
				Integer comSuc = (Integer) ReflectUtil.getValueByKey(packDataInfo, COM_SUC_PREFIX + i);
				BigDecimal cellResist = (BigDecimal) ReflectUtil.getValueByKey(expandLatest, CELL_RESIST_PREFIX + i);

				if (cellVol == null || cellVol.compareTo(filter.getMaxCellVol()) > 0
						|| cellVol.compareTo(filter.getMinCellVol()) < 0) {
					cellExce.append("电压:" + cellVol).append("V");
					hasSubDeviceExce = true;
				}

				if (comSuc == null || comSuc.compareTo(filter.getMaxComSuc()) > 0
						|| comSuc.compareTo(filter.getMinComSuc()) < 0) {
					cellExce.append(cellExce.length() > 0 ? "、" : "").append("通讯成功率:" + comSuc);
					hasSubDeviceExce = true;
				}

				if (cellTem == null || cellTem.compareTo(filter.getMaxCellTem()) > 0
						|| cellTem.compareTo(filter.getMinCellTem()) < 0) {
					cellExce.append(cellExce.length() > 0 ? "、" : "").append("温度:" + cellTem).append("℃");
					hasSubDeviceExce = true;
				}

				// 内阻判断之前先判断，电池投入使用时间，和最近一次模型计算时间比较
				if (expandLatest != null) {
					Date useFrom = cellList.get(i - 1).getUseFrom();
					Date updateTime = expandLatest.getUpdateTime();
					if (useFrom != null) {
						if (useFrom.compareTo(updateTime) < 0) {
							if (cellResist == null || cellResist.compareTo(filter.getResistance()) > 0) {
								cellExce.append(cellExce.length() > 0 ? "、" : "").append("内阻:" + cellResist)
										.append("mΩ");
								hasSubDeviceExce = true;
							}
						}
					} else {
						if (cellResist == null || cellResist.compareTo(filter.getResistance()) > 0) {
							cellExce.append(cellExce.length() > 0 ? "、" : "").append("内阻:" + cellResist).append("mΩ");
							hasSubDeviceExce = true;
						}
					}
				}

				if (hasSubDeviceExce) {
					cellExce.insert(0, i + "号单体:");
					pdiExce.append(pdiExce.length() > 0 ? ";" : "").append(cellExce.toString());
				}
			}

			if (pdiExce.length() > 0) {
				if (exceptionCount >= 5) {
					return;
				}
				exceptionCount++;
				item.setRemark(pdiExce.toString());
				items.add(item);
			}
		}

	}

	@Override
	public List<ChargeDischargeEvent> getChargeDischargeEvent(String gprsId, Date startTime, Date endTime,
			AbstractEvent event, EventParams params) {
		if (event == null) {
			return null;
		}
		event.setParams(params);
		// 得到 PackDataInfo ，时间降序
		List<PackDataInfo> packDataInfos = getPackDataInfosByRange(gprsId, startTime, endTime);
		if (CollectionUtils.isEmpty(packDataInfos)) {
			return null;
		}
		List<GprsConfigInfo> gprsConfigInfos = gprsConfigInfoMapper.selectByGprsIds(Lists.newArrayList(gprsId));
		GprsConfigInfo gprsConfigInfo = CollectionUtils.isNotEmpty(gprsConfigInfos) ? gprsConfigInfos.get(0) : null;
		List<ChargeDischargeEvent> events = null;
		if (gprsConfigInfo == null) {
			LOGGER.info("不能得到设备相关信息,编号:{}", gprsId);
		} else {
			try {
				event.setGprsConfigInfo(gprsConfigInfo);
				events = event.generateEvents(gprsId, packDataInfos, this);
			} catch (Exception e) {
				LOGGER.info("不能找到相关的掉电配置,编号:{}", gprsId);
			}
		}
		return events;
	}

	public List<PackDataInfo> forwardLookup10Datas(Integer startId, String gprsId) {
		Map<String, Object> paramMap = Maps.newHashMap();
		paramMap.put("gprsId", gprsId);
		paramMap.put("id", startId);
		paramMap.put("pageNum", 0);
		paramMap.put("pageSize", 10);
		return packDataInfoMapper.getPackDataInfosWhichIdLessThanGivenValue(paramMap);
	}

	public List<PackDataInfo> backwardLookup10Datas(Integer startId, String gprsId) {
		Map<String, Object> paramMap = Maps.newHashMap();
		paramMap.put("gprsId", gprsId);
		paramMap.put("id", startId);
		paramMap.put("pageNum", 0);
		paramMap.put("pageSize", 10);
		return packDataInfoMapper.getPackDataInfosWhichGreaterThanGivenValue(paramMap);
	}

	/**
	 * 判断集合是否都为指定状态
	 * 
	 * @param list
	 * @param state
	 *            判断状态
	 * @param isContrary
	 *            是否判断相反的状态
	 * @return
	 */
	public boolean stateVerify(List<PackDataInfo> list, String state, boolean isContrary) {
		for (PackDataInfo packDataInfo : list) {
			if (!isContrary && !state.equals(packDataInfo.getState())) {
				return false;
			}
			if (isContrary && state.equals(packDataInfo.getState())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 获得浮充异常数据
	 */
	@Override
	public StationReport getChargeAbnormalData(StationReport stationReport) {
		//申明数据集合
		List<StationReportItem> items = new ArrayList<>();
		//查询所有的电池组
		StationInfo record = new StationInfo();
		record.setGprsIdNotNull(true);
		List<StationInfo> stationInfos = stationInfoMapper.selectListSelective(record);
		if (CollectionUtils.isNotEmpty(stationInfos)) {
			List<String> gprsIdList = stationInfos.stream().filter(s -> !s.getGprsId().equals("-1") && !s.getGprsId().startsWith("Y0A"))
					.map(s -> s.getGprsId()).collect(Collectors.toList());
			
			
		//获取静态的数据
		Parameter parameter = new Parameter();
		parameter.setParameterCode("staticVol");
		List<Parameter> parameterStaticVol = parameterSer.selectListSelective(parameter);
		Parameter staticVol =parameterStaticVol.size() != 0 ? parameterStaticVol.get(0) : null;
		
		parameter.setParameterCode("staticMinCur");
		List<Parameter> parameterStaticMinCur = parameterSer.selectListSelective(parameter);
		Parameter staticMinCur =parameterStaticMinCur.size() != 0 ? parameterStaticMinCur.get(0) : null;
		
		parameter.setParameterCode("staticMaxCur");
		List<Parameter> parameterStaticMaxCur = parameterSer.selectListSelective(parameter);
		Parameter staticMaxCur =parameterStaticMaxCur.size() != 0 ? parameterStaticMaxCur.get(0) : null;
		
		if(staticVol == null || staticMaxCur == null || staticMinCur == null) {
			throw new RuntimeException("静态参数没有设置");
		}
		 BigDecimal staticV = new BigDecimal(staticVol.getParameterValue());
		 BigDecimal staticMinC = new BigDecimal(staticMinCur.getParameterValue());
		 BigDecimal staticMaxC = new BigDecimal(staticMaxCur.getParameterValue());	
		
		//返回的结果
		 List<PackDataInfo> result = new ArrayList<>();
		 
		for (String gprsId : gprsIdList) {
			//String gprsId = "T0B000229";
		//根据时间、电压、电流找到符合条件的数据；取第一条数据
		Map<String, Object> paramMap = Maps.newHashMap();
		paramMap.put("startTime", stationReport.getStartRcvTime());
		paramMap.put("endTime", stationReport.getEndRcvTime());
		paramMap.put("Vol",stationReport.getVol() );
		paramMap.put("MinCur", stationReport.getMinCur());
		paramMap.put("MaxCur", stationReport.getMaxCur());		
		paramMap.put("gprsId", gprsId);	
		List<PackDataInfo> chargeData = packDataInfoMapper.getTotalChargeABnormlData(paramMap);
		if(chargeData.size() != 0) {
			//根据得到的id向后找，在整个pack_data_info 表里
			List<PackDataInfo> exceedTime = new ArrayList<>();
			Integer id = chargeData.get(0).getId();
			Integer index = null;;//截止点
			Date startTime = chargeData.get(0).getRcvTime();
			Date IndexstartTime = null;
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(startTime);
			calendar.add(Calendar.DATE, +3);// 当前时间加3天
			Date time = calendar.getTime();			
			Date endTime;
			while (true) {
			Map<String, Object> map = new HashMap<>();
			map.put("pageNum", 0);
			map.put("pageSize", 5000);
			map.put("gprsId",gprsId);
			map.put("id", id);
			//得到id之后的数据
			List<PackDataInfo>  uncertaintyData= packDataInfoMapper.getPackDataInfosWhichGreaterThanGivenValue(map);
			if(CollectionUtils.isEmpty(uncertaintyData)) {
				break;
			}
			boolean flage = false ;
				int less = 1;
				for (PackDataInfo packDataInfo : uncertaintyData) {
					endTime = packDataInfo.getRcvTime();
					BigDecimal genVol = packDataInfo.getGenVol();
					BigDecimal genCur = packDataInfo.getGenCur();
					if(genVol.compareTo(staticV) < 0 //电压比静态电压小
							&& genCur.compareTo(staticMaxC) < 0 //总电流比最大的静态电流小
							&& genCur.compareTo(staticMinC) > 0 ) {//总电流比小的静态电流大
						
					}else {//不满足这个条件
						IndexstartTime = packDataInfo.getRcvTime();
						index = packDataInfo.getId();//找到截止点;
						break;
					}
					//加载异常数据
					exceedTime.add(packDataInfo);
					
					id = uncertaintyData.get(uncertaintyData.size()-1).getId();
					//时间如果超过三天；
					if(endTime.getTime()-time.getTime() >= 0) {
						result.addAll(exceedTime);//将超过时间的数据加载到异常数据集合中
						flage = true;
						break;
					}
				}
				
				if(flage) {
					break;
				}
				//向前向后找有几个情况：1返回count都为0 前后三天的异常数据，
				//2，向前count=10,向前有10条放电记录，向后count=0；有向后的至少三天的异常数据,
				//3,向后count=0,向后10条充电记录，向前coungt=10,有向前的至少三天的异常数据，
				//4，向前count=10,向前有10条放电记录，向后count=10 ，向后有10条充电记录，获得向后的充电记录的的id,查找截止点
				
				if(index != null) {
					//向前向后找
					//forwardAndBack(index, flage, id, gprsId, result);	
					Map<String, Object> forwardLookupTime = forwardLookupTime(index, gprsId,IndexstartTime);
					Map<String, Object> backwardLookupTime = backwardLookupTime(index, gprsId,IndexstartTime);
					
					int forwarCount = (int)forwardLookupTime.get("count");
					List<PackDataInfo> forwardList = (List<PackDataInfo>) forwardLookupTime.get("forwardResults");
					
					int backCount = (int)backwardLookupTime.get("count");
					List<PackDataInfo> backList = (List<PackDataInfo>) backwardLookupTime.get("backResults");
					
					if(forwarCount == 10 && backCount == 10) {
						id = backList.get(backList.size()-1).getId();
						forwardList.clear();
						backList.clear();
						less = 0;
					}else {
						flage = true;
						less = 0;
						result.addAll(forwardList);
						result.addAll(backList);
					}	
				}

				
				if(less == 1 && uncertaintyData.size() < 5000 ) {
					
					id = uncertaintyData.get(0).getId();
					//forwardAndBack(index, flage, id, gprsId, result);
					Map<String, Object> forwardLookupTime = forwardLookupTime(index, gprsId,IndexstartTime);
					Map<String, Object> backwardLookupTime = backwardLookupTime(index, gprsId,IndexstartTime);
					
					int forwarCount = (int)forwardLookupTime.get("count");
					List<PackDataInfo> forwardList = (List<PackDataInfo>) forwardLookupTime.get("forwardResults");
					
					int backCount = (int)backwardLookupTime.get("count");
					List<PackDataInfo> backList = (List<PackDataInfo>) backwardLookupTime.get("backResults");
					
					if(forwarCount == 10 && backCount == 10) {
						id = backList.get(backList.size()-1).getId();
					}else {
						flage = true;
						result.addAll(forwardList);
						result.addAll(backList);
					}	
					
				}
				if(flage) {
					break;
				}
					
					
			}
			
		
//		GprsConfigInfo query = new GprsConfigInfo();
//		query.setGprsId(gprsId);;
//		List<GprsConfigInfo> gprsConfigInfos = gprsConfigInfoMapper.selectListSelective(query);
//		Integer cellCount=0;
//		if(CollectionUtils.isNotEmpty(gprsConfigInfos)) {
//			cellCount = gprsConfigInfos.get(0).getSubDeviceCount()+1;
//		}
//		if(cellCount == null || cellCount == 0) {
//			System.out.println(gprsId);
//			
//			//throw new RuntimeException("单体个数没有设置");
//		}
		if(CollectionUtils.isNotEmpty(result)) {
			List<PackDataInfo> newCollect = result.stream().sorted(Comparator.comparing(PackDataInfo::getRcvTime)).collect(Collectors.toList());
			List<PackDataInfo> result2 = new ArrayList<>();
			result2.add(newCollect.get(0));
			result2.add(newCollect.get(newCollect.size()-1));

			for (PackDataInfo packDataInfo : result2) {
				StationReportItem item = new StationReportItem();
				item.setGprsId(packDataInfo.getGprsId());
				item.setState(packDataInfo.getState());
				item.setGenVolStr(packDataInfo.getGenVol().toString());
				item.setGenCurStr(packDataInfo.getGenCur().toString());
				item.setRcvTime(JxlsUtil.dateFmt(packDataInfo.getRcvTime()));
				item.setEnvironTemStr(packDataInfo.getEnvironTem().toString());
				for(int i = 1 ;i< 25 ; i++) {
					BigDecimal cellVol = (BigDecimal) ReflectUtil.getValueByKey(packDataInfo, CELL_VOL_PREFIX + i);
					Integer comSuc = (Integer) ReflectUtil.getValueByKey(packDataInfo, COM_SUC_PREFIX + i);
					cellVol =cellVol == null ? new BigDecimal(0):cellVol;
					comSuc	= comSuc == null ? 0:comSuc;
					ReflectUtil.setValueByKet(item, CELL_STR+i, String.valueOf(cellVol));
					ReflectUtil.setValueByKet(item, CELL_SUC+i,  String.valueOf(comSuc));
				}
				items.add(item);
			}
		}
		
		}
		stationReport.setItems(items);
		}
	}
		return stationReport;
	}	
	
//	//向前向后找
//	public void forwardAndBack(Integer index,Boolean flage, Integer id,String gprsId,List<PackDataInfo> result){
//		Map<String, Object> forwardLookupTime = forwardLookupTime(index, gprsId);
//		Map<String, Object> backwardLookupTime = backwardLookupTime(index, gprsId);
//		
//		int forwarCount = (int)forwardLookupTime.get("count");
//		List<PackDataInfo> forwardList = (List<PackDataInfo>) forwardLookupTime.get("forwardResults");
//		
//		int backCount = (int)backwardLookupTime.get("count");
//		List<PackDataInfo> backList = (List<PackDataInfo>) backwardLookupTime.get("backResults");
//		
//		if(forwarCount == 10 && backCount == 10) {
//			id = backList.get(backList.size()-1).getId();
//		}else {
//			flage = true;
//			result.addAll(forwardList);
//			result.addAll(backList);
//		}	
//		
//	}
	
	//向后找
	private  Map<String, Object> backwardLookupTime(Integer startId, String gprsId,Date IndexstartTime) {
		List<PackDataInfo> backResults = Lists.newArrayList();
		int pageNum = 0;
		int count = 0;
		boolean flage = false;
		while (true) {
			Map<String, Object> paramMap = Maps.newHashMap();
			paramMap.put("gprsId", gprsId);
			paramMap.put("id", startId);
			paramMap.put("pageNum", pageNum);
			paramMap.put("pageSize", PAGESIZE);
			List<PackDataInfo> items = packDataInfoMapper.getPackDataInfosWhichGreaterThanGivenValue(paramMap);
			if (CollectionUtils.isEmpty(items)) {
				break;
			}
			//向后找时间超过三天默认为非静态数据
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(IndexstartTime);
			calendar.add(Calendar.DATE, +3);// 当前时间+3天
			Date endtime = calendar.getTime();
			for (PackDataInfo item : items) {
				backResults.add(item);
				Date time = item.getRcvTime();
				if(time.getTime()-endtime.getTime() >= 0) {
					flage = true;
					break;
				}
				if ("充电".equals(item.getState())) {
					count++;
				} else {
					count = 0;
				}
				if (count >= 10) {
					break;
				}
			}
			if(flage) {
				break;
			}
			if (count >= 10) {
				break;
			}
			if (items.size() < PAGESIZE) {
				break;
			}
			startId = items.get(items.size() - 1).getId();
			pageNum += PAGESIZE;
		}
		Map<String, Object> map = new HashMap<>();
		map.put("count", count);
		map.put("backResults",backResults);
		return map;
	}

	//向前找
	private Map<String, Object>  forwardLookupTime(Integer startId, String gprsId,Date IndexstartTime) {
		List<PackDataInfo> forwardResults = Lists.newArrayList();
		int pageNum = 0;
		int count = 0;
		boolean flage = false;
		while (true) {
			Map<String, Object> paramMap = Maps.newHashMap();
			paramMap.put("gprsId", gprsId);
			paramMap.put("id", startId);
			paramMap.put("pageNum", pageNum);
			paramMap.put("pageSize", PAGESIZE);
			List<PackDataInfo> items = packDataInfoMapper.getPackDataInfosWhichIdLessThanGivenValue(paramMap);
			if (CollectionUtils.isEmpty(items)) {
				break;
			}
			//向前找时间超过三天默认为非静态数据
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(IndexstartTime);
			calendar.add(Calendar.DATE, -3);// 当前时间减去3天
			Date endtime = calendar.getTime();
			for (PackDataInfo item : items) {
				forwardResults.add(item);
				Date time = item.getRcvTime();
				if(time.getTime()-endtime.getTime() <= 0) {
					flage = true;
					break;
				}
				if ("放电".equals(item.getState())) {
					count++;
				} else {
					count = 0;
				}
				if (count >= 10) {
					break;
				}
			}
			if(flage) {
				break;
			}
			if (count >= 10) {
				break;
			}
			if (items.size() < PAGESIZE) {
				break;
			}
			startId = items.get(items.size() - 1).getId();
			pageNum += PAGESIZE;
		}
		Map<String, Object> map = new HashMap<>();
		map.put("count", count);
		map.put("forwardResults",forwardResults);
		return map;
	}
	
	@Override
	public StationReport getAbnormalPdiReport(StationReport stationReport) {
		List<StationReportItem> items = new ArrayList<>();
		stationReport.setItems(items);
		// 查询所有的电池组
		StationInfo record = new StationInfo();
		record.setGprsIdNotNull(true);
		List<StationInfo> stationInfos = stationInfoMapper.selectListSelective(record);
		if (CollectionUtils.isNotEmpty(stationInfos)) {
			List<String> gprsIdList = stationInfos.stream().filter(s -> !s.getGprsId().equals("-1") && !s.getGprsId().startsWith("Y0A"))
					.map(s -> s.getGprsId()).collect(Collectors.toList());
			AbnormalStaticParams staticParams = getAbnormalStaticParams();
			List<PackDataInfo> abnormalList = new ArrayList<>();
			
			for (String gprsId : gprsIdList) {
				Integer pdiId = null;
				do {
					pdiId = getAbnormalReportItem(abnormalList, stationReport, items, gprsId, pdiId, staticParams);
				} while (pdiId != null);
			}
		}
		return stationReport;
	}
	
	private AbnormalStaticParams getAbnormalStaticParams() {
		AbnormalStaticParams params = new AbnormalStaticParams();
		try {
			//获取静态的数据
			Parameter parameter = new Parameter();
			parameter.setParameterCode("staticVol");
			List<Parameter> parameterStaticVol = parameterSer.selectListSelective(parameter);
			Parameter staticVol =parameterStaticVol.size() != 0 ? parameterStaticVol.get(0) : null;
			
			parameter.setParameterCode("staticMinCur");
			List<Parameter> parameterStaticMinCur = parameterSer.selectListSelective(parameter);
			Parameter staticMinCur =parameterStaticMinCur.size() != 0 ? parameterStaticMinCur.get(0) : null;
			
			parameter.setParameterCode("staticMaxCur");
			List<Parameter> parameterStaticMaxCur = parameterSer.selectListSelective(parameter);
			Parameter staticMaxCur =parameterStaticMaxCur.size() != 0 ? parameterStaticMaxCur.get(0) : null;
			
			params.staticV = new BigDecimal(staticVol == null ? "53" : staticVol.getParameterValue());
			params.staticMinC = new BigDecimal(staticMinCur == null ? "-5" : staticMinCur.getParameterValue());
			params.staticMaxC = new BigDecimal(staticMaxCur == null ? "5" : staticMaxCur.getParameterValue());	
		} catch (Exception e) {
			params.staticV = new BigDecimal(53);
			params.staticMinC = new BigDecimal(-5);
			params.staticMaxC = new BigDecimal(5);
		}
		return params;
	}
	
	private Integer getAbnormalReportItem(List<PackDataInfo> abnormalList,StationReport stationReport,
			List<StationReportItem> items,String gprsId, Integer id, AbnormalStaticParams staticParams) {
		abnormalList.clear();
		PackDataInfo minPdi = getMinIdPdiWhichGreaterThanGivenValue(stationReport, gprsId, id);
		if (minPdi == null) {
			return null;
		}
		int index = -2;
		List<PackDataInfo> packDataInfoList = new ArrayList<>();
		Integer startId = minPdi.getId();
		int cursor = 0;
		while (true) {
			List<PackDataInfo> backwardLookup = backwardLookup(gprsId, startId, stationReport.getEndRcvTime(),
					0, 5000);
			if (CollectionUtils.isEmpty(backwardLookup)) {
				break;
			}
			packDataInfoList.addAll(backwardLookup);
			if (CollectionUtils.isEmpty(packDataInfoList)) {
				break;
			}
			for (int i = cursor; i < packDataInfoList.size(); i++) {
				PackDataInfo packDataInfo = packDataInfoList.get(i);
				boolean isValid = packDataInfo.getGenVol().compareTo(staticParams.staticV) < 0
						&& packDataInfo.getGenCur().compareTo(staticParams.staticMinC) > 0
						&& packDataInfo.getGenCur().compareTo(staticParams.staticMaxC) < 0;
				if (!isValid) {
					index = i - 1;
					break;
				}
			}
			if (index == -2 && backwardLookup.size() == 5000) {
				startId = packDataInfoList.get(packDataInfoList.size() - 1).getId();
				cursor = packDataInfoList.size();
			} else {
				// 找到了，非有效的数据或者数据找完了还没找到
				break;
			}
		}
		if (CollectionUtils.isEmpty(packDataInfoList)) {
			return null;
		}
		if (index < 0) {
			index = packDataInfoList.size() - 1;
		}
		Date diffDate = MyDateUtils.add(packDataInfoList.get(index).getRcvTime(), Calendar.DATE, -3);
		if (minPdi.getRcvTime().compareTo(diffDate) < 0) {
			// 表示时间大于3天，视为异常数据
			abnormalList.add(minPdi);
			abnormalList.addAll(packDataInfoList.subList(0, index + 1));
			generateAbnormalItemHandler(items, abnormalList);
			return packDataInfoList.get(index).getId();
		} else {
			// 拿到结束点，向后找3天的数据
			Date endTime = MyDateUtils.add(packDataInfoList.get(index).getRcvTime(), Calendar.DATE, 3);
			List<PackDataInfo> backwardLookup = backwardLookup(gprsId, packDataInfoList.get(index).getId(),
					endTime, 0, 20000);
			if (CollectionUtils.isEmpty(backwardLookup)) {
				// 按异常处理
				abnormalList.add(minPdi);
				abnormalList.addAll(packDataInfoList);
				generateAbnormalItemHandler(items, abnormalList);
				return packDataInfoList.get(packDataInfoList.size() -1).getId();
			}
			packDataInfoList = packDataInfoList.subList(0, index + 1);
			packDataInfoList.addAll(backwardLookup);

			int count = 0;
			int endIndex = 0;
			// 向后找 10 条充电数据
			for (int i = index; i < packDataInfoList.size(); i++) {
				if ("充电".equals(packDataInfoList.get(i).getState())) {
					count++;
				} else {
					count = 0;
				}
				if (count >= 10) {
					endIndex = i;
					break;
				}
			}
			if (count == 10) {
				Date rcvTime = packDataInfoList.get(endIndex).getRcvTime();
				if (rcvTime.compareTo(diffDate) < 0) {
					// 表示时间大于三天，异常,不用再找了
					abnormalList.add(minPdi);
					abnormalList.addAll(packDataInfoList.subList(0, endIndex + 1));
					generateAbnormalItemHandler(items, abnormalList);
					return packDataInfoList.get(endIndex).getId();
				}
			} else {
				// 没找到,异常
				abnormalList.add(minPdi);
				abnormalList.addAll(packDataInfoList);
				generateAbnormalItemHandler(items, abnormalList);
				return packDataInfoList.get(packDataInfoList.size() -1).getId();
			}
			// 能走到这来，表示 count = 10
			
			Date startTime = MyDateUtils.add(minPdi.getRcvTime(), Calendar.DATE, -3);
			// 向前找 10 条 放电数据
			List<PackDataInfo> forwardLookup = forwardLookup(gprsId, startId, startTime, 0, 20000);
			if (CollectionUtils.isEmpty(forwardLookup)) {
				// 按异常处理
				abnormalList.add(minPdi);
				abnormalList.addAll(packDataInfoList);
				generateAbnormalItemHandler(items, abnormalList);
				return packDataInfoList.get(endIndex).getId();
			}

			count = 0;
			int startIndex = 0;
			for (int i = 0; i < forwardLookup.size(); i++) {
				if ("放电".equals(forwardLookup.get(i).getState())) {
					count++;
				} else {
					count = 0;
				}
				if (count >= 10) {
					startIndex = i;
					break;
				}
			}
			if (count == 10) {
				diffDate = MyDateUtils.add(forwardLookup.get(startIndex).getRcvTime(), Calendar.DATE, 3);
				Date rcvTime = minPdi.getRcvTime();
				if (rcvTime.compareTo(diffDate) > 0) {
					// 表示超过3天了，异常
					abnormalList.add(minPdi);
					forwardLookup = forwardLookup.subList(0, startIndex + 1).stream()
							.sorted(Comparator.comparing(PackDataInfo::getId)).collect(Collectors.toList());
					abnormalList.addAll(0, forwardLookup);
					abnormalList.addAll(packDataInfoList);
					generateAbnormalItemHandler(items, abnormalList);
				}
			} else {
				abnormalList.add(minPdi);
				forwardLookup = forwardLookup.stream().sorted(Comparator.comparing(PackDataInfo::getId))
						.collect(Collectors.toList());
				abnormalList.addAll(0, forwardLookup);
				abnormalList.addAll(packDataInfoList);
				generateAbnormalItemHandler(items, abnormalList);
			}
			return packDataInfoList.get(endIndex).getId();
		}
	}
	
	private void generateAbnormalItemHandler(List<StationReportItem> items, List<PackDataInfo> abnormalList) {
		if (CollectionUtils.isNotEmpty(abnormalList)) {
			List<PackDataInfo> temp = new ArrayList<>();
			temp.add(abnormalList.get(0));
			temp.add(abnormalList.get(abnormalList.size() -1));
			for (PackDataInfo packDataInfo : temp) {
				StationReportItem item = new StationReportItem();
				item.setGprsId(packDataInfo.getGprsId());
				item.setState(packDataInfo.getState());
				item.setGenVolStr(packDataInfo.getGenVol().toString());
				item.setGenCurStr(packDataInfo.getGenCur().toString());
				item.setRcvTime(JxlsUtil.dateFmt(packDataInfo.getRcvTime()));
				item.setEnvironTemStr(packDataInfo.getEnvironTem().toString());
				for (int i = 1; i < 25; i++) {
					BigDecimal cellVol = (BigDecimal) ReflectUtil.getValueByKey(packDataInfo,
							CELL_VOL_PREFIX + i);
					Integer comSuc = (Integer) ReflectUtil.getValueByKey(packDataInfo, COM_SUC_PREFIX + i);
					cellVol = cellVol == null ? new BigDecimal(0) : cellVol;
					comSuc = comSuc == null ? 0 : comSuc;
					ReflectUtil.setValueByKet(item, CELL_STR + i, String.valueOf(cellVol));
					ReflectUtil.setValueByKet(item, CELL_SUC + i, String.valueOf(comSuc));
				}
				items.add(item);
			}
		}
	}

	private PackDataInfo getMinIdPdiWhichGreaterThanGivenValue(StationReport stationReport, String gprsId, Integer id) {
		// 根据时间、电压、电流找到符合条件的数据
		Map<String, Object> paramMap = Maps.newHashMap();
		paramMap.put("startTime", stationReport.getStartRcvTime());
		paramMap.put("endTime", stationReport.getEndRcvTime());
		paramMap.put("Vol", stationReport.getVol());
		paramMap.put("MinCur", stationReport.getMinCur());
		paramMap.put("MaxCur", stationReport.getMaxCur());
		paramMap.put("gprsId", gprsId);
		paramMap.put("pageNum", 0);
		paramMap.put("pageSize", 1);
		paramMap.put("id", id);// 大于该值
		List<PackDataInfo> pdiAbnormalData = packDataInfoMapper.getPackDateInfoAbnormalData(paramMap);
		if (CollectionUtils.isEmpty(pdiAbnormalData)) {
			return null;
		}
		return pdiAbnormalData.get(0);
	}

	private List<PackDataInfo> backwardLookup(String gprsId, Integer startId, Date endTime, Integer pageNum,
			Integer pageSize) {
		Map<String, Object> paramMap = Maps.newHashMap();
		paramMap.put("gprsId", gprsId);
		paramMap.put("id", startId); // 不包含等于
		paramMap.put("pageNum", pageNum);
		paramMap.put("pageSize", pageSize);
		paramMap.put("endTime", endTime);
		return packDataInfoMapper.getPackDataInfosWhichGreaterThanGivenValue(paramMap);
	}

	private List<PackDataInfo> forwardLookup(String gprsId, Integer startId, Date startTime, Integer pageNum,
			Integer pageSize) {
		Map<String, Object> paramMap = Maps.newHashMap();
		paramMap.put("gprsId", gprsId);
		paramMap.put("id", startId); // 不包含等于
		paramMap.put("pageNum", pageNum);
		paramMap.put("pageSize", pageSize);
		paramMap.put("startTime", startTime);
		return packDataInfoMapper.getPackDataInfosWhichIdLessThanGivenValue(paramMap);
	}
	
	
	class AbnormalStaticParams{
		public BigDecimal staticV;
		public BigDecimal staticMinC;
		public BigDecimal staticMaxC;
	}

	/**
	 * 电池整治报告word
	 */
	@Override
	public StationReportItem getCellRectificationData(StationReportItem stationReport) {
		StationReportItem item = new StationReportItem();
		item.setGprsId("TOB124578");
		item.setGenVolStr("45");
		item.setGenCurStr("12");
		item.setRcvTime("2018-10-12 12:12:12");
		return item;
	}
	
}
