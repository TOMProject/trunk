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
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.station.common.Constant;
import com.station.common.cache.DischargeCache;
import com.station.common.redis.RedisCacheAgent;
import com.station.common.utils.BeanValueUtils;
import com.station.common.utils.ConvertUtils;
import com.station.common.utils.DoubleUtil;
import com.station.common.utils.ModelCalcHelper;
import com.station.common.utils.MyDateUtils;
import com.station.common.utils.ReflectUtil;
import com.station.moudles.entity.CellInfo;
import com.station.moudles.entity.Company;
import com.station.moudles.entity.DischargeManualRecord;
import com.station.moudles.entity.GprsConfigInfo;
import com.station.moudles.entity.ModifyCapacitySend;
import com.station.moudles.entity.PackDataExpand;
import com.station.moudles.entity.PackDataExpandLatest;
import com.station.moudles.entity.PackDataInfo;
import com.station.moudles.entity.PackDataInfoLatest;
import com.station.moudles.entity.Parameter;
import com.station.moudles.entity.PulseCalculationSend;
import com.station.moudles.entity.PulseDischargeInfo;
import com.station.moudles.entity.PulseDischargeSend;
import com.station.moudles.entity.RoutingInspectionDetail;
import com.station.moudles.entity.RoutingInspections;
import com.station.moudles.entity.StationDurationHistory;
import com.station.moudles.entity.StationInfo;
import com.station.moudles.entity.SuggestCellInfo;
import com.station.moudles.mapper.CellHistoryInfoMapper;
import com.station.moudles.mapper.CellInfoMapper;
import com.station.moudles.mapper.CompanyMapper;
import com.station.moudles.mapper.DischargeManualRecordMapper;
import com.station.moudles.mapper.GprsConfigInfoMapper;
import com.station.moudles.mapper.ModifyCapacitySendMapper;
import com.station.moudles.mapper.PackDataExpandLatestMapper;
import com.station.moudles.mapper.PackDataExpandMapper;
import com.station.moudles.mapper.PackDataInfoLatestMapper;
import com.station.moudles.mapper.PackDataInfoMapper;
import com.station.moudles.mapper.ParameterMapper;
import com.station.moudles.mapper.PulseCalculationSendMapper;
import com.station.moudles.mapper.PulseDischargeInfoMapper;
import com.station.moudles.mapper.PulseDischargeSendMapper;
import com.station.moudles.mapper.RoutingInspectionDetailMapper;
import com.station.moudles.mapper.RoutingInspectionsMapper;
import com.station.moudles.mapper.StationDurationHistoryMapper;
import com.station.moudles.mapper.StationInfoMapper;
import com.station.moudles.service.DischargeManualRecordService;
import com.station.moudles.service.ModelCalculationService;
import com.station.moudles.service.PackDataExpandService;
import com.station.moudles.service.PackDataInfoService;
import com.station.moudles.service.PulseDischargeInfoService;
import com.station.moudles.vo.CellVoltage;
import com.station.moudles.vo.ResponseStatus;
import com.station.moudles.vo.report.ModelReport;
import com.station.moudles.vo.report.ModelReportItem;

@Service
public class ModelCalculationServiceImpl implements ModelCalculationService {

	private final static Logger LOGGER = LoggerFactory.getLogger(ModelCalculationServiceImpl.class);
	private final static int PAGESIZE = 2000;
	private final static String INVALID_GPRS_ID = "-1";
	private final static byte SEND_DONE = 2; // 特征指令成功
	private final static double DEFAULT_AGE = 3;
	private final static double MAX_AGE = 3.5;// 最大放电时长
	private final static double MIN_VOL = 47.00;// 最小放电电压
	public final static String DISCHARGE_NAME = "放电";
	private final static BigDecimal VALID_MIN_VOLTAGE = BigDecimal.valueOf(2.1); // 有效浮充电压最小值
	private final static BigDecimal VALID_MAX_VOLTAGE = BigDecimal.valueOf(2.4); // 有效浮充电压最大值
	private final static Integer CON_THRESHOLD = 52;// 连续放电数量修改为52条，注意，是连续。
	private final static String CELL_RESIST_PREFIX = "cellResist";
	private final static String CELL_CAP_PREFIX = "cellCap";
	private final static String CELL_CAP_SORT_PREFIX = "cellCapSort";
	private final static String CELL_EVALU_PREFIX = "cellEvalu";
	private final static String CELL_VOL_PREFIX = "cellVol";
	private final static BigDecimal DEFAULT_RESISTIANCE = BigDecimal.valueOf(2.001);

	private final StationInfoMapper stationInfoMapper;
	private final PulseDischargeInfoMapper pulseDischargeInfoMapper;
	private final PulseDischargeSendMapper pulseDischargeSendMapper;
	private final PackDataExpandLatestMapper packDataExpandLatestMapper;
	private final PackDataExpandMapper packDataExpandMapper;
	private final GprsConfigInfoMapper gprsConfigInfoMapper;
	private final PackDataInfoMapper packDataInfoMapper;
	private final PackDataInfoLatestMapper packDataInfoLatestMapper;
	private final CellInfoMapper cellInfoMapper;
	private final ModifyCapacitySendMapper modifyCapacitySendMapper;
	private final PulseCalculationSendMapper pulseCalculationSendMapper;
	private final CompanyMapper companyMapper;
	private final StationDurationHistoryMapper stationDurationHistoryMapper;
	private final DischargeCache dischargeCache;
	private final RoutingInspectionsMapper routingInspectionsMapper;

	@Autowired
	private CellHistoryInfoMapper cellHistoryInfoMapper;
	@Autowired
	private PulseDischargeInfoService pulseDischargeInfoService;
	@Autowired
	private PackDataInfoService packDataInfoService;
	@Autowired
	private PackDataExpandService packDataExpandService;
	@Autowired
	private ParameterMapper parameterMapper;
	@Autowired
	DischargeManualRecordService dischargeManualRecordSer;
	@Autowired
	RedisCacheAgent redisCacheAgent;
	@Autowired
	RoutingInspectionDetailMapper routingInspectionDetailMapper;
	@Autowired
	DischargeManualRecordMapper dischargeManualRecordMapper;

	@Autowired
	@Lazy
	public ModelCalculationServiceImpl(StationInfoMapper stationInfoMapper,
			PulseDischargeInfoMapper pulseDischargeInfoMapper, PulseDischargeSendMapper pulseDischargeSendMapper,
			PackDataExpandLatestMapper packDataExpandLatestMapper, PackDataExpandMapper packDataExpandMapper,
			GprsConfigInfoMapper gprsConfigInfoMapper, PackDataInfoMapper packDataInfoMapper,
			PackDataInfoLatestMapper packDataInfoLatestMapper, CellInfoMapper cellInfoMapper,
			ModifyCapacitySendMapper modifyCapacitySendMapper, PulseCalculationSendMapper pulseCalculationSendMapper,
			CompanyMapper companyMapper, StationDurationHistoryMapper stationDurationHistoryMapper,
			DischargeCache dischargeCache,RoutingInspectionsMapper routingInspectionsMapper) {
		this.stationInfoMapper = stationInfoMapper;
		this.pulseDischargeInfoMapper = pulseDischargeInfoMapper;
		this.pulseDischargeSendMapper = pulseDischargeSendMapper;
		this.packDataExpandLatestMapper = packDataExpandLatestMapper;
		this.packDataExpandMapper = packDataExpandMapper;
		this.gprsConfigInfoMapper = gprsConfigInfoMapper;
		this.packDataInfoMapper = packDataInfoMapper;
		this.packDataInfoLatestMapper = packDataInfoLatestMapper;
		this.cellInfoMapper = cellInfoMapper;
		this.modifyCapacitySendMapper = modifyCapacitySendMapper;
		this.pulseCalculationSendMapper = pulseCalculationSendMapper;
		this.companyMapper = companyMapper;
		this.stationDurationHistoryMapper = stationDurationHistoryMapper;
		this.dischargeCache = dischargeCache;
		this.routingInspectionsMapper = routingInspectionsMapper;
	}

	private List<Integer> randomNumbers(int feed) {
		List<Integer> numbers = Lists.newArrayList();
		int number;
		for (int i = 0; i < feed; i++) {
			do {
				number = (int) (Math.random() * feed) + 1;
			} while (numbers.contains(number));
			numbers.add(number);
		}
		return numbers;
	}

	private void resetProperties(GprsConfigInfo gprsConfigInfo, StationInfo stationInfo,
			PackDataExpandLatest packDataExpandLatest) {
		BigDecimal value = BigDecimal.valueOf(getStandardCapacityFromPackType(stationInfo.getPackType()))
				.multiply(BigDecimal.valueOf(0.3)).add(BigDecimal.ONE).abs();
		final int feed = stationInfo.getCellCount();
		List<Integer> numbers = randomNumbers(feed);
		for (int i = 0; i < stationInfo.getCellCount(); i++) {
			int index = i + 1;
			// 默认值 2.001
			String name = CELL_RESIST_PREFIX + index;
			resetProperty(name, DEFAULT_RESISTIANCE, packDataExpandLatest);
			// 默认值 30%标称+1
			name = CELL_CAP_PREFIX + index;
			resetProperty(name, value, packDataExpandLatest);
			// 默认值 随机生成1~stationInfo.getCellCount()
			name = CELL_CAP_SORT_PREFIX + index;
			resetProperty(name, numbers.get(i), packDataExpandLatest);
			// 默认值 0:正常
			name = CELL_EVALU_PREFIX + index;
			resetProperty(name, 0, packDataExpandLatest);
		}
		// 默认值 30%标称+1
		packDataExpandLatest.setPackCapPred(value.intValue());
		// 默认值 目标整冶时长+0.001
		BigDecimal defaultVaue = BigDecimal.valueOf(0.001);
		if (gprsConfigInfo != null && StringUtils.isNotBlank(gprsConfigInfo.getSuggestTime())) {
			defaultVaue = defaultVaue.add(BigDecimal
					.valueOf(ConvertUtils.toDouble(StringUtils.trimToEmpty(gprsConfigInfo.getSuggestTime()), 0d)));
		}
		packDataExpandLatest.setPackDischargeTimePred(defaultVaue);
	}

	private void resetProperty(String name, Object value, PackDataExpandLatest packDataExpandLatest) {
		BeanValueUtils.bindProperty(name, value, packDataExpandLatest);
	}

	@Override
	public List<ResponseStatus> calculate(Integer stationId) {
		LOGGER.debug("基站编码:{}", stationId);
		if (null == stationId) {
			throw new IllegalArgumentException("请输入基站编号");
		}
		StationInfo stationInfo = stationInfoMapper.selectByPrimaryKey(stationId);
		if (null == stationInfo) {
			throw new IllegalArgumentException("请输入有效的基站编号");
		}
		GprsConfigInfo condition = new GprsConfigInfo();
		condition.setGprsId(stationInfo.getGprsId());
		List<GprsConfigInfo> configs = gprsConfigInfoMapper.selectListSelective(condition);
		GprsConfigInfo gprsConfigInfo = CollectionUtils.isEmpty(configs) ? null : configs.get(0);

		boolean newRecord = false;
		PackDataExpandLatest packDataExpandLatest = packDataExpandLatestMapper
				.selectByPrimaryKey(stationInfo.getGprsId());
		if (packDataExpandLatest == null) {
			newRecord = true;
			packDataExpandLatest = new PackDataExpandLatest();
			packDataExpandLatest.setGprsId(stationInfo.getGprsId());
		}
		if (newRecord) {
			resetProperties(gprsConfigInfo, stationInfo, packDataExpandLatest);
		}
		List<ResponseStatus> statuses = new ArrayList<>(2);
		try {
			// 内阻计算
			statuses.add(calculateResistance(stationInfo, packDataExpandLatest));
			//判断是否内阻计算成功删除redis中内阻平均值
			if(statuses.size() != 0) {
			ResponseStatus status=statuses.get(0);
			 if (StringUtils.contains(status.getMessage(), "内阻")) {
                Integer index = status.getStatus();
                if(index == 1 ) {//1成功
                    redisCacheAgent.clear(Constant.AVGRESIST_PATH+stationInfo.getGprsId());
                }
			 }
			}
			
		} catch (Exception e) {
			LOGGER.error("模型计算内阻失败，使用默认值!", e);
		}
		try {
			// 容量计算
			//statuses.add(calculateCapacity(gprsConfigInfo, stationInfo, packDataExpandLatest));
			statuses.add(calculateCapacity2(gprsConfigInfo, stationInfo, packDataExpandLatest));
		} catch (Exception e) {
			LOGGER.error("模型计算容量失败，使用默认值!", e);
		}
		// 单体性能计算，无论模型计算成功与否，都要更新性能数据。
		calcualteEntityPerformance(stationInfo, gprsConfigInfo, packDataExpandLatest);

		packDataExpandLatest.setUpdateTime(new Date());
		// 存储结果
		if (newRecord) {
			packDataExpandLatestMapper.insert(packDataExpandLatest);
		} else {
			packDataExpandLatestMapper.updateByPrimaryKeySelective(packDataExpandLatest);
		}
		PackDataExpand packDataExpand = new PackDataExpand();
		BeanUtils.copyProperties(packDataExpandLatest, packDataExpand);
		packDataExpandMapper.insert(packDataExpand);
		// 保存容量计算结果，等到命令发送
		saveModifyCapacitySend(stationInfo.getGprsId(), packDataExpandLatest.getPackCapPred());
		// 电池组实时时长预测
		updateStationRealDuration(stationInfo, packDataExpandLatest.getPackDischargeTimePred(), gprsConfigInfo);
		// 写入电池组放点时长预测到station_duration_history
		saveStationDurationHistory(stationInfo.getGprsId(), packDataExpandLatest.getPackDischargeTimePred());

		return statuses;
	}
	/**
	 * StationInfo 单体性能统计 和 PackDataExpandLatest 单体性能状态设置
	 * 
	 * @param stationInfo
	 * @param gprsConfigInfo
	 * @param packDataExpandLatest
	 */
	public void calcualteEntityPerformance(StationInfo stationInfo, GprsConfigInfo gprsConfigInfo,
			PackDataExpandLatest packDataExpandLatest) {
		if (gprsConfigInfo == null) {
			LOGGER.warn("跳过单体性能计算，因为没有GprsConfigInfo数据,基站编号：{}", stationInfo.getGprsId());
			return;
		}
		BigDecimal consoleCellCapNormal = gprsConfigInfo.getConsoleCellCapNormal();
		BigDecimal consoleCellCapError = gprsConfigInfo.getConsoleCellCapError();
		// 标称容量
		BigDecimal standardCapacity = BigDecimal.valueOf(getStandardCapacityFromPackType(stationInfo.getPackType()));
		LOGGER.debug("标称容量:{},单体电池故障容量:{},单体电池正常容量:{},基站编号:{}",
				new Object[] { standardCapacity, consoleCellCapError, consoleCellCapNormal, stationInfo.getGprsId() });
		// 性能统计
		int okNum = 0, poorNum = 0, errorNum = 0; // add --- end
		for (int i = 1; i < stationInfo.getCellCount() + 1; i++) {
			String name = CELL_CAP_PREFIX + i;
			BigDecimal value = (BigDecimal) BeanValueUtils.getValue(name, packDataExpandLatest);
			BigDecimal percentage = value.divide(standardCapacity, 3, BigDecimal.ROUND_HALF_UP)
					.multiply(BigDecimal.valueOf(100));
			// 0:正常,1:较差, 2: 故障
			Integer status = 0;
			if (consoleCellCapError != null && percentage.compareTo(consoleCellCapError) < 0) {
				// 当单体容量百分比（suggest_cell_cap_percent）＜console_cell_cap_error时，单体性能为故障
				status = 2;
				errorNum++;
			} else if (consoleCellCapNormal != null && percentage.compareTo(consoleCellCapNormal) > 0) {
				// 当单体容量百分比（suggest_cell_cap_percent）>console_cell_cap_normal时，单体性能为正常
				status = 0;
				okNum++;
			} else if (consoleCellCapError != null && consoleCellCapNormal != null
					&& percentage.compareTo(consoleCellCapError) >= 0
					&& percentage.compareTo(consoleCellCapNormal) <= 0) {
				// 当单体容量百分比（suggest_cell_cap_percent）大于等于console_cell_cap_error且小于等console_cell_cap_normal时，单体性能为较差
				status = 1;
				poorNum++;
			}
			name = CELL_EVALU_PREFIX + i;
			BeanValueUtils.bindProperty(name, status, packDataExpandLatest);
		}
		// --- add ---
		if (errorNum > 0) {
			stationInfo.setStatus(2);
		} else if (poorNum > 0) {
			stationInfo.setStatus(1);
		} else {
			stationInfo.setStatus(0);
		}
		stationInfo.setOkNum(okNum);
		stationInfo.setPoorNum(poorNum);
		stationInfo.setErrorNum(errorNum);
		// --- end ---
	}

	private void saveStationDurationHistory(String gprsId, BigDecimal packDischargeTimePred) {
		StationDurationHistory stationDurationHistory = new StationDurationHistory();
		stationDurationHistory.setGprsId(gprsId);
		stationDurationHistory.setDuration(packDischargeTimePred);
		stationDurationHistoryMapper.insertSelective(stationDurationHistory);
	}
	
	/**
	 * 有核容放电时，电池组时长预测
	 * @param stationInfo
	 * @param packDataExpandLatest
	 * @param gprsConfigInfo
	 */
	private void calculateGroupDischargeTimePrediction1(GprsConfigInfo gprsConfigInfo, StationInfo stationInfo, PackDataExpandLatest packDataExpandLatest, 
			List<PackDataInfo> packDataInfos) {
		PackDataInfo firstRecord = packDataInfos.get(0);
		PackDataInfo endRecord = packDataInfos.get(packDataInfos.size() - 1);
		long dischargeTime = (endRecord.getRcvTime().getTime() - firstRecord.getRcvTime().getTime())/1000;//秒
		BigDecimal value = BigDecimal.valueOf(dischargeTime / 3600.00);
		
		BigDecimal validDischargeVol = gprsConfigInfo.getCheckDischargeVol();//核容放电电压
		BigDecimal endVol = endRecord.getGenVol();//截至电压 
		if(validDischargeVol == null) {
			validDischargeVol = new BigDecimal(47.0);
		}
		if (endVol.compareTo(validDischargeVol) <= 0) {
			value = value;
		}else if (endVol.compareTo(validDischargeVol.add(new BigDecimal(1))) <= 0){
			value = value.multiply(new BigDecimal(1.25));
		}else if (endVol.compareTo(validDischargeVol.add(new BigDecimal(2))) <= 0) {
			value = value.multiply(new BigDecimal(1.5));
		}else if (endVol.compareTo(validDischargeVol.add(new BigDecimal(3))) <= 0) {
			value = value.multiply(new BigDecimal(1.75));
		}else {
			value = value.multiply(new BigDecimal(2));
		}
		
		packDataExpandLatest.setPackDischargeTimePred(value);
	}
	
	/**
	 * 无核容放电时，电池组时长预测
	 * @param stationInfo
	 * @param packDataExpandLatest
	 * @param gprsConfigInfo
	 */
	private void calculateGroupDischargeTimePrediction2(StationInfo stationInfo,
			PackDataExpandLatest packDataExpandLatest, GprsConfigInfo gprsConfigInfo) {
		BigDecimal current  = stationInfo.getLoadCurrent();
		if(current == null || current.compareTo(new BigDecimal(0)) <= 0)
			throw new RuntimeException("无核容放电，无负载电流，时长预测失败。"+gprsConfigInfo.getGprsId());
		
		BigDecimal value = BigDecimal.valueOf(packDataExpandLatest.getPackCapPred());
		LOGGER.debug("计算时长，gprsid:{}，电流:{}，预测容量:{}.", new Object[] { stationInfo.getGprsId(), current, value });
		value = value.divide(current, 2, BigDecimal.ROUND_HALF_UP).abs();
		
		packDataExpandLatest.setPackDischargeTimePred(value);
	}
	
	/**
	 * 电池组放电时长预测: pack_cap_pred / 上一次放电时的放电电流平均值 上一次放电时的放电电流平均值:
	 * 上次放电开始后的连续102个gen_cur去掉最大最小之和除以100
	 *
	 * @param stationInfo
	 * @param packDataExpandLatest
	 */
	private void calculateGroupDischargeTimePrediction(StationInfo stationInfo,
			PackDataExpandLatest packDataExpandLatest, GprsConfigInfo gprsConfigInfo) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		List<PackDataInfo> discharges = Lists.newArrayList();
		List<PackDataInfo> infos = Lists.newArrayList();
		int pageNum = 0;
		int queryCount = 0;
		while (true) {
			long startTime = System.currentTimeMillis();
			Map<String, Object> paramMap = Maps.newHashMap();
			paramMap.put("gprsId", stationInfo.getGprsId());
			paramMap.put("pageNum", pageNum);
			paramMap.put("pageSize", PAGESIZE);
			List<PackDataInfo> items = packDataInfoMapper.getPackDataInfosByTimes(paramMap);
			queryCount++;
			if (CollectionUtils.isEmpty(items)) {
				break;
			}
			if (items.size() < PAGESIZE) {
				break;
			}
			if (queryCount > 10) {
				break;
			}
			LOGGER.debug("第{}次查询---->获取{}/{}条测试数据,耗时:{} ms", new Object[] { queryCount,
					items.stream().filter(item -> DISCHARGE_NAME.equals(item.getState())).mapToInt(item -> 1).sum(),
					items.size(), System.currentTimeMillis() - startTime });

			for (PackDataInfo item : items) {
				if (item.getState().equals(DISCHARGE_NAME)
						&& item.getGenCur().abs().doubleValue() >= gprsConfigInfo.getValidDischargeCur().abs()
								.doubleValue()
						&& item.getGenCur().abs().doubleValue() <= gprsConfigInfo.getMaxDischargeCur().abs()
								.doubleValue()) {
					infos.add(item);
					discharges.add(item);
					if (infos.size() >= CON_THRESHOLD) {
						break;
					}
				} else {
					// infos = Lists.newArrayList();
				}
			}
			if (infos.size() >= CON_THRESHOLD) {
				break;
			}

			pageNum += PAGESIZE;
		}
		if (CollectionUtils.isEmpty(infos)) {
			infos = discharges;
		}
		stopWatch.stop();
		LOGGER.debug("获取{}条放电测试数据--->最终查询了{}次---->最终获取{}条非连续放电测试数据---->基站编号:{}--->耗时:{}",
				new Object[] { infos.size(), queryCount, discharges.size(), stationInfo.getGprsId(), stopWatch });
		infos.sort(Comparator.comparing(PackDataInfo::getGenCur));
		BigDecimal avg = BigDecimal.ZERO;
		if (infos.size() < 3) {
			LOGGER.warn("容量预测，计算时长时，获得放电数据条数{}小于3条，设置默认放电电流为50a。", infos.size());
			avg = BigDecimal.valueOf(50);
		} else {
			infos.remove(0);
			infos.remove(infos.size() - 1);

			for (PackDataInfo info : infos) {
				avg = avg.add(info.getGenCur());
			}
			avg = avg.divide(BigDecimal.valueOf(infos.size()), 2, BigDecimal.ROUND_HALF_UP);
		}
		BigDecimal value = BigDecimal.valueOf(packDataExpandLatest.getPackCapPred());
		LOGGER.debug("计算时长，gprsid:{}，平均电流:{}，预测容量:{}.", new Object[] { stationInfo.getGprsId(), avg, value });
		value = value.divide(avg, 2, BigDecimal.ROUND_HALF_UP).abs();
		if (value.compareTo(BigDecimal.valueOf(5)) > 0) {
			LOGGER.debug("计算时长，gprsid:{}，预测容量:{}，时长超过5小时，用功率来计算时长.", new Object[] { stationInfo.getGprsId(), value });
			BigDecimal power = stationInfo.getLoadPower() == null ? BigDecimal.valueOf(2400)
					: stationInfo.getLoadPower();
			value = BigDecimal.valueOf(packDataExpandLatest.getPackCapPred())
					.divide(power.divide(BigDecimal.valueOf(48)), 2, BigDecimal.ROUND_HALF_UP);
			if (value.compareTo(BigDecimal.TEN) >= 0) {
				value = BigDecimal.valueOf(9.99);
			}
		}
		packDataExpandLatest.setPackDischargeTimePred(value);
	}

	private List<PackDataInfo> get102PackDataInfos(List<PackDataInfo> items) {
		if (items.size() < CON_THRESHOLD) {
			return Collections.emptyList();
		}
		List<PackDataInfo> infos = new ArrayList<>(102);
		for (PackDataInfo item : items) {
			if (item.getState().equals(DISCHARGE_NAME)) {
				infos.add(item);
				if (infos.size() >= CON_THRESHOLD) {
					break;
				}
			} else {
				infos = Lists.newArrayList();
			}
		}
		return infos.size() >= CON_THRESHOLD ? infos.subList(0, CON_THRESHOLD) : Collections.emptyList();
	}

	@Override
	public ResponseStatus generateResponseStatus(boolean status, long startTime, String type, String extMessage) {
		ResponseStatus responseStatus = new ResponseStatus();
		responseStatus.setStatus(status ? 1 : 2);
		responseStatus.setStatusText(status ? "成功" : "失败");
		responseStatus.setMessage(type + responseStatus.getStatusText());
		responseStatus.setExtMessage(extMessage);
		responseStatus.setTookTime((System.currentTimeMillis() - startTime) + " ms");
		return responseStatus;
	}

	/**
	 * 内阻计算
	 * 
	 * <pre>
	 *     cell_resist_n = (Max(voltage)-Min(voltage))/n号单体最近一次成功的特征测试数据区间1第10个电流点开始连续取12个点，去掉最大最小后的10个点平均值
	 *     Max(Voltage)为区间1前20个Voltage中最大的值，Min(Voltage)为区间1前20个Voltage中最小的值
	 * </pre>
	 *
	 * @param gprsId
	 * @param packDataExpandLatest
	 * @return
	 */
	private ResponseStatus calculateResistance(StationInfo stationInfo, PackDataExpandLatest packDataExpandLatest) {
		String gprsId = stationInfo.getGprsId();
		long startTime = System.currentTimeMillis();
		Map<Integer, PulseDischargeInfo> infoMap = getLatestPulseDischargeInfo(gprsId);
		if (MapUtils.isEmpty(infoMap)) {
			return generateResponseStatus(false, startTime, "内阻", "失败，没有特征测试成功数据");
		}
		Map<String, BigDecimal> resistances = new HashMap<String, BigDecimal>();
		for (Map.Entry<Integer, PulseDischargeInfo> entry : infoMap.entrySet()) {
			BigDecimal value;
			String name = CELL_RESIST_PREFIX + entry.getKey();
			// -----10/14 add 如果电池内阻有就直接用，没有就重新计算
			Integer number = entry.getKey();
			PulseDischargeInfo pulseDischargeInfo = infoMap.get(number);
			if (pulseDischargeInfo != null && pulseDischargeInfo.getImpendance() != null 
					&& pulseDischargeInfo.getImpendance().doubleValue() > 0
					&& pulseDischargeInfo.getImpendance().doubleValue() < 100) {
				value = pulseDischargeInfo.getImpendance();
				LOGGER.info("读取主机上传内阻 。设备编号{}， 单体编号{}。",new Object[] { gprsId,entry.getKey()});
			} else {
				value = BigDecimal.valueOf(0.0);
				try {
					value = calculateSingleEntityResistance(entry.getValue(), gprsId);
				} catch (Exception ex) {
					LOGGER.error(String.format("计算单体内阻失败，使用默认值0.0。设备编号{}， 单体编号{}。", gprsId, entry.getKey()), ex);
				}
			}
			if (value != null)
				resistances.put(name, value);
			// BeanValueUtils.bindProperty(name, value == null ? BigDecimal.valueOf(2) :
			// value, packDataExpandLatest);
		}
		int count = 0;
		double sumVal = 0;
		double avgVal = 0;
		for (BigDecimal val : resistances.values()) {
			if (val != null && val.doubleValue() > 0.0) {
				count++;
				sumVal += val.doubleValue();
			}
		}
		if (count > 0) {
			avgVal = sumVal / count;
		}
		for (int i = 1; i < stationInfo.getCellCount() + 1; i++) {
			String name = CELL_RESIST_PREFIX + i;
			BigDecimal value = resistances.get(name);
			if (value != null && value.doubleValue() > 0.0) {

			} else {
				double val = avgVal + new Random().nextDouble();
				value = BigDecimal.valueOf(val).setScale(4, BigDecimal.ROUND_HALF_UP);
			}
			BeanValueUtils.bindProperty(name, value, packDataExpandLatest);
		}
		return generateResponseStatus(true, startTime, "内阻", "成功");
	}

	/**
	 * 容量模型计算
	 *
	 * @param gprsConfigInfo
	 * @param stationInfo
	 * @param packDataExpandLatest
	 */
	private ResponseStatus calculateCapacity(GprsConfigInfo gprsConfigInfo, StationInfo stationInfo,
			PackDataExpandLatest packDataExpandLatest) {
		long startTime = System.currentTimeMillis();
		try {
			if (gprsConfigInfo == null) {
				LOGGER.warn("找不到基站配置信息--->基站编号:{}", stationInfo.getGprsId());
				return generateResponseStatus(false, startTime, "容量", "失败，找不到基站配置信息");
			}
			Date currentDate = new Date();
			Date dischargeEndTime = null;// 最近一次有效放电，放电结束时间。
			// 单体浮充电压
			Map<Integer, BigDecimal> cellVoltage = calculateValidVoltage(stationInfo, currentDate);
			if (MapUtils.isEmpty(cellVoltage)) {
				return generateResponseStatus(false, startTime, "容量", "失败，找不到最近一周12次浮充态电压数据");
			}

			// 获取最近一次有效放电记录
			PackDataInfo latestDischargeRecord = getLatestDischargeRecord(gprsConfigInfo, currentDate);
			List<PackDataInfo> discharges = null;
			if (null != latestDischargeRecord) {
				// 计算电池组预测容量+有最近一次有效放电记录
				discharges = getContinuousDischargeRecords(latestDischargeRecord, stationInfo.getGprsId());
				dischargeEndTime = calculateGroupPredictionCapacityInLatestDischargeMode(discharges,
						packDataExpandLatest);
			}
			// 单体预测容量排序计算
			if (CollectionUtils.isNotEmpty(discharges)) {
				// 按照时间倒序排
				discharges.sort(Comparator.comparing(PackDataInfo::getRcvTime).reversed());
			}
			calculateEntityPredictionCapacitySort(latestDischargeRecord, cellVoltage,
					calculateValidVoltageHandler(stationInfo, discharges), packDataExpandLatest, stationInfo);
			// 单体预测容量计算
			calculateEntityPredictionCapacity(latestDischargeRecord, cellVoltage, stationInfo, currentDate,
					packDataExpandLatest);
			// 根据电池单体更换历史记录，更正电池容量预测。
			updateEntityPredictionCapacity(packDataExpandLatest, stationInfo, dischargeEndTime, currentDate);

			//查找最近一次该电池组的单体更换时间（整治时间）。
			RoutingInspections query = new RoutingInspections();
//			query.setGprsId(stationInfo.getGprsId());
			query.setStationId(stationInfo.getId());
			query.setOperateType(3);// 更换单体
			RoutingInspections latestRouting = routingInspectionsMapper.selectOneLatestSelective(query);
			Date routingTime = MyDateUtils.parseDate("1970-01-01 00:00:00");
			if (latestRouting != null) {
				routingTime = latestRouting.getOperateTime() != null ? latestRouting.getOperateTime() : routingTime ;
			}
			//无最近一次有效放电记录或这个记录早于整治时间，那么也需要从新计算放电时长和电池组预测容量
			if (null == latestDischargeRecord || latestDischargeRecord.getRcvTime().compareTo(routingTime) < 0 ) {
				// 计算电池组预测容量
				calculateGroupPredictionCapacityUnLatestDischargeMode(cellVoltage, packDataExpandLatest, stationInfo);
				// 电池组放电时长预测
				calculateGroupDischargeTimePrediction(stationInfo, packDataExpandLatest, gprsConfigInfo);
			}
			return generateResponseStatus(true, startTime, "容量", "成功");
		} catch (Exception ex) {
			LOGGER.error("容量预测失败", ex);
			return generateResponseStatus(false, startTime, "容量", ex.getMessage());
		}
	}

	/**
	 * 容量计算
	 * @param gprsConfigInfo
	 * @param stationInfo
	 * @param packDataExpandLatest
	 * @return
	 */
	private ResponseStatus calculateCapacity2(GprsConfigInfo gprsConfigInfo, StationInfo stationInfo,
			PackDataExpandLatest packDataExpandLatest) {
		long startTime = System.currentTimeMillis();
		try {
			if (gprsConfigInfo == null) {
				LOGGER.warn("找不到基站配置信息--->基站编号:{}", stationInfo.getGprsId());
				return generateResponseStatus(false, startTime, "容量", "失败，找不到基站配置信息");
			}
			//电池组标称容量
			double stationStandardCap = getStandardCapacityFromPackType(stationInfo.getPackType());
			//当前时间
			Date currentDate = new Date();
			//获得所有的单体
			CellInfo query = new CellInfo();
			query.setGprsId(gprsConfigInfo.getGprsId());
			List<CellInfo> cells = cellInfoMapper.selectListSelective(query);
			//获取核容放电数据
			//DischargeAbstractRecord dischargeAbstractRecord = packDataInfoService.getCheckDischargeList(gprsConfigInfo);
			//获取手动放电记录
//			List<PackDataInfo> packDataInfoList = dischargeManualRecordSer.getOneLatestRealDischargeRecord(gprsConfigInfo.getGprsId());
			DischargeManualRecord record = new DischargeManualRecord();
			record.setGprsId(gprsConfigInfo.getGprsId());
			DischargeManualRecord dischargeManualRecord = dischargeManualRecordMapper.selectOneLatestSelective(record);
			List<PackDataInfo> packDataInfos  = packDataInfoService.verifyDischargeRecord(dischargeManualRecord);
			if(packDataInfos != null && packDataInfos.size() > 10) {
				LOGGER.debug("模型计算，找到数据，gprsid:{}, startid:{}", stationInfo.getGprsId(), packDataInfos.get(0).getId() );
				/*// 电池组放电时长预测
				calculateGroupDischargeTimePrediction1(gprsConfigInfo, stationInfo, packDataExpandLatest,  packDataInfos);
				//电池组容量预测
				calculateGroupPredictionCapacity1(packDataExpandLatest, packDataInfos, stationStandardCap);
				//单体容量排序
				int badCount = calculateEntityPredictionCapacitySort1(gprsConfigInfo, cells, packDataExpandLatest, packDataInfos, currentDate);
				//单体容量预测
				calculateEntityPredictionCapacity1(gprsConfigInfo, stationInfo, cells, packDataExpandLatest, currentDate, stationStandardCap,badCount);
				*/
				clacDurationAndCapHandler(packDataInfos, stationInfo, gprsConfigInfo, cells, packDataExpandLatest, dischargeManualRecord);
			}else {//无核容放电。
				LOGGER.debug("模型计算，无找到有效核容放电数据，gprsid:{}。", stationInfo.getGprsId());
				//单体容量排序
				int badCount = calculateEntityPredictionCapacitySort2(gprsConfigInfo, cells, packDataExpandLatest, currentDate);
				//单体容量计算
				calculateEntityPredictionCapacity2(gprsConfigInfo, stationInfo, cells, packDataExpandLatest, currentDate,badCount);
				//最近12条浮充电压
				Map<Integer, BigDecimal> cellVoltage = calculateValidVoltage(stationInfo, currentDate);
				if (MapUtils.isEmpty(cellVoltage)) {
					return generateResponseStatus(false, startTime, "容量", "失败，找不到最近一周12次浮充态电压数据");
				}
				//电池组容量预测
				calculateGroupPredictionCapacity2(cellVoltage, packDataExpandLatest, stationInfo, stationStandardCap);
				// 电池组放电时长预测
				calculateGroupDischargeTimePrediction2(stationInfo, packDataExpandLatest, gprsConfigInfo);
			}
			return generateResponseStatus(true, startTime, "容量", "成功");
		} catch (Exception ex) {
			LOGGER.error("容量预测失败", ex);
			return generateResponseStatus(false, startTime, "容量", ex.getMessage());
		}
	}
	
	/**
	 * @param gprsId
	 * @param packCapPred
	 */
	private void saveModifyCapacitySend(String gprsId, Integer packCapPred) {
		ModifyCapacitySend modifyCapacitySend = new ModifyCapacitySend();
		modifyCapacitySend.setGprsId(gprsId);
		modifyCapacitySend.setCapacity(packCapPred);
		modifyCapacitySend.setSendTime(new Date());
		modifyCapacitySend.setSendDone(0);
		modifyCapacitySendMapper.insertSelective(modifyCapacitySend);
	}

	/**
	 * 更新基站电池组实时时长预测
	 *
	 * @param stationInfo
	 * @param packDischargeTimePred
	 */
	private void updateStationRealDuration(StationInfo stationInfo, BigDecimal packDischargeTimePred,
			GprsConfigInfo gprsConfigInfo) {
		if (packDischargeTimePred == null) {
			LOGGER.warn("没有电池组放电时长预测数据放弃计算电池组实时时长预测，基站编号:{}", stationInfo.getGprsId());
			return;
		}
		Integer soc = 100;
		PackDataInfoLatest latest = packDataInfoLatestMapper.selectByPrimaryKey(stationInfo.getGprsId());
		if (latest != null && latest.getSoc() != null) {
			soc = latest.getSoc();
		}
		stationInfo.setDuration(packDischargeTimePred);
		stationInfo.setRealDuration(
				packDischargeTimePred.multiply(BigDecimal.valueOf(soc)).divide(BigDecimal.valueOf(100)));
		setStationDurationStatus(stationInfo, gprsConfigInfo, packDischargeTimePred);
		stationInfoMapper.updateByPrimaryKeySelective(stationInfo);
	}

	/**
	 * 设置电池组预测时长的状态
	 * 
	 * @param stationInfo
	 * @param gprsConfigInfo
	 * @param packDischargeTimePred
	 *            电池组放点时长预测
	 */
	public void setStationDurationStatus(StationInfo stationInfo, GprsConfigInfo gprsConfigInfo,
			BigDecimal packDischargeTimePred) {
		if (gprsConfigInfo == null) {
			LOGGER.warn("设置电池组预测时长的状态，因为没有GprsConfigInfo数据,基站编号：{}", stationInfo.getGprsId());
			return;
		}
		// 转换为分钟数
		BigDecimal duration = new BigDecimal(60).multiply(packDischargeTimePred);
		Integer durationStatus = null;
		if (duration.compareTo(new BigDecimal(gprsConfigInfo.getDurationMinExcellent())) >= 0) {
			durationStatus = 1;
		} else if (duration.compareTo(new BigDecimal(gprsConfigInfo.getDurationMinGood())) >= 0
				&& duration.compareTo(new BigDecimal(gprsConfigInfo.getDurationMaxGood())) < 0) {
			durationStatus = 2;
		} else if (duration.compareTo(new BigDecimal(gprsConfigInfo.getDurationMinMedium())) >= 0
				&& duration.compareTo(new BigDecimal(gprsConfigInfo.getDurationMaxMedium())) < 0) {
			durationStatus = 3;
		} else {
			durationStatus = 4;
		}
		stationInfo.setDurationStatus(durationStatus);
	}
	/**
	 * 有核容放电时，单体预测容量
	 * 1.新单体，1年内，每个月减少10.---为适应不同容量，改为减去2%
	 * 2.其余的，电池组预测容量pack_cap_pred* [1+(16-单体电池容量排序序号cellcap_sort_n)/240],其中n=1~24
	 * @param gprsConfigInfo
	 * @param stationInfo
	 * @param cells
	 * @param packDataExpandLatest
	 * @param currentDate
	 */
	private void calculateEntityPredictionCapacity1(GprsConfigInfo gprsConfigInfo, StationInfo stationInfo, List<CellInfo> cells, 
			PackDataExpandLatest packDataExpandLatest, Date currentDate, double stationStandardCap,int badCount) {
		
		Integer groupPredictionCap = packDataExpandLatest.getPackCapPred();//电池组预测容量
		int cellCount = cells.size();
		
		BigDecimal value;
		for(CellInfo cell : cells) {
			Integer cellCapSort = (Integer) BeanValueUtils.getValue("cellCapSort" + cell.getCellIndex(), packDataExpandLatest);
			if(cell.getUsedMonth() != null && cell.getUsedMonth() < 13) {
				//每个月减去2%
				value = BigDecimal.valueOf(stationStandardCap * (1 - 0.02 * cell.getUsedMonth()));
			}else {
				// 电池组预测容量pack_cap_pred* [1+(16-单体电池容量排序序号cellcap_sort_n)/240],其中n=1~24
				// 除于240让单体容量的变化范围，低于6%
				value = BigDecimal.valueOf(groupPredictionCap * (1 + ((cellCount*2/3.0 - cellCapSort) / (cellCount*10))));
			}
			
			if(cellCapSort > (cellCount-badCount)) {
				//(电池组容量预测*故障单体容量百分比)*（100 - 单体性能编号）/100
				value = new BigDecimal(groupPredictionCap).multiply(gprsConfigInfo.getConsoleCellCapError().divide(new BigDecimal(100)))
						.multiply(new BigDecimal(100 -cellCapSort)).divide(new BigDecimal(100));
			}
			String name = CELL_CAP_PREFIX + cell.getCellIndex();
			BeanValueUtils.bindProperty(name, value, packDataExpandLatest);
		}
	}
	
	/**
	 * 无核容放电时，单体容量计算
	 * 1.新单体，1年内，每个月减少10.---为适应不同容量，改为减去2%
	 * 2.其余的，ccn*[1+(12-排序)/24]
	 * 3.对于故障单体，其最大值必须在系统设定的故障百分比下。其余故障单体的值也必须小于系统设定的故障百分比，且值的排序不能改变。因此用百分比进行单体容量矫正。
	 * 矫正单体容量=原来单体容量*[1-(最大值-故障标准值)/最大值]
	 * @param stationInfo
	 * @param currentDate
	 * @param packDataExpandLatest
	 * @param cells
	 */
	private void calculateEntityPredictionCapacity2(GprsConfigInfo gprsConfigInfo, StationInfo stationInfo, List<CellInfo> cells, 
			PackDataExpandLatest packDataExpandLatest, Date currentDate,int badCount) {
		double stationStandardCap = getStandardCapacityFromPackType(stationInfo.getPackType());//电池组标称容量
		Map<Integer, BigDecimal> ccnMap = calculateCCN2(stationInfo, currentDate, cells);
		if (MapUtils.isEmpty(ccnMap)) {
			throw new RuntimeException(String.format("计算CCN失败，因为没有单体标称容量数据，基站编号:"+stationInfo.getGprsId()));
		}
		//故障单体排序开始值，即，所有故障单体中，容量最高值。
		int badCellIdx = cells.size()-badCount + 1;
		//故障单体容量允许的最高值。
		BigDecimal standardValue = BigDecimal.valueOf(getStandardCapacityFromPackType(stationInfo.getPackType()));
		double badCellStandCap = standardValue.multiply(gprsConfigInfo.getConsoleCellCapError()).divide(BigDecimal.valueOf(100)).doubleValue();
		//计算出来的故障单体差值超过允许值之间的部分。
		double diffBetweenMaxAndStand = 0;
		//故障单体的容量值需要同步矫正的百分比
		double bedCellSubstractPercent = 0;
		
		BigDecimal value;
		for(CellInfo cell : cells) {
			Integer cellCapSort = (Integer) BeanValueUtils.getValue("cellCapSort" + cell.getCellIndex(), packDataExpandLatest);
			if(cell.getUsedMonth() != null && cell.getUsedMonth() < 13) {
				//每个月减去2%
				value = BigDecimal.valueOf(stationStandardCap * (1 - 0.02 * cell.getUsedMonth()));
			}else {
				BigDecimal ccn = ccnMap.get(cell.getCellIndex());
				if (ccn == null) {
					ccn = BigDecimal.ZERO;
				}
				value = ccn.multiply(BigDecimal.valueOf((1 + ((cells.size()/2.0 - cellCapSort) / cells.size()))));
			}
			
			if(cellCapSort == badCellIdx) {
				diffBetweenMaxAndStand = value.doubleValue() - badCellStandCap;
				if(diffBetweenMaxAndStand > 0) {//故障单体最大值超过标准值，计算矫正百分比。
					bedCellSubstractPercent = (diffBetweenMaxAndStand + 1) / value.doubleValue();
				}
			}
			
			String name = CELL_CAP_PREFIX + cell.getCellIndex();
			BeanValueUtils.bindProperty(name, value, packDataExpandLatest);
		}

		if(bedCellSubstractPercent > 0) {//需要矫正故障单体的容量值。
			for(CellInfo cell : cells) {
				Integer cellCapSort = (Integer) BeanValueUtils.getValue("cellCapSort" + cell.getCellIndex(), packDataExpandLatest);
				if(cellCapSort >= badCellIdx) {
					String name = CELL_CAP_PREFIX + cell.getCellIndex();
					value = (BigDecimal) BeanValueUtils.getValue(name, packDataExpandLatest);
					value = new BigDecimal(value.doubleValue() * (1- bedCellSubstractPercent));
					BeanValueUtils.bindProperty(name, value, packDataExpandLatest);
				}
			}
		}
	}
	
	/**
	 * 单体预测容量计算:
	 * 
	 * <pre>
	 *     1. 浮充电压范围:(2.1V≤X≤2.35V) & 有最近一次有效放电记录 --》电池组预测容量pack_cap_pred* [1+(16-单体电池容量排序序号cellcap_sort_n)/240],其中n=1~24
	 *     除于240让单体容量的变化范围，低于6%
	 *     2. 浮充电压范围:(2.1V≤X≤2.35V) & 无最近一次有效放电记录 --》CCn* [1+(12-单体电池容量排序序号cellcap_sort_n)/24]，其中n=1~24
	 *     3. 浮充电压范围:小于2.1V 或 大于2.35 --》 单体标称容量*9.999%
	 * </pre>
	 *
	 * @param latestDischargeRecord
	 *            最近一次有效放电记录
	 * @param cellVoltage
	 *            单体浮充电压
	 * @param stationInfo
	 *            基站
	 * @param date
	 *            时间
	 * @param packDataExpandLatest
	 *            需要更新的结果
	 */
	private void calculateEntityPredictionCapacity(PackDataInfo latestDischargeRecord,
			Map<Integer, BigDecimal> cellVoltage, StationInfo stationInfo, Date date,
			PackDataExpandLatest packDataExpandLatest) {
		Map<Integer, BigDecimal> ccnMap = calculateCCN(stationInfo, date);
		if (MapUtils.isEmpty(ccnMap)) {
			LOGGER.debug("计算CCN失败，因为没有单体标称容量数据，基站编号:{}", stationInfo.getGprsId());
			return;
		}
		int cellCount = stationInfo.getCellCount();
		for (Map.Entry<Integer, BigDecimal> entry : cellVoltage.entrySet()) {
			Integer cellNum = entry.getKey();
			BigDecimal voltage = entry.getValue();
			BigDecimal value;
			boolean match = voltage.compareTo(VALID_MAX_VOLTAGE) <= 0 && voltage.compareTo(VALID_MIN_VOLTAGE) >= 0;
			if (match) {
				Integer cellCapSort = (Integer) BeanValueUtils.getValue("cellCapSort" + cellNum, packDataExpandLatest);
				if (null == latestDischargeRecord) {
					// 浮充电压范围:(2.1V≤X≤2.4V) & 无最近一次有效放电记录
					// CCn* [1+(12-单体电池容量排序序号cellcap_sort_n)/24]，其中n=1~24
					BigDecimal ccn = ccnMap.get(cellNum);
					if (ccn == null) {
						ccn = BigDecimal.ZERO;
					}
					value = ccn.multiply(BigDecimal.valueOf((1 + ((cellCount/2.0 - cellCapSort) / cellCount))));
				} else {
					// 浮充电压范围:(2.1V≤X≤2.35V) & 有最近一次有效放电记录
					// 电池组预测容量pack_cap_pred* [1+(16-单体电池容量排序序号cellcap_sort_n)/240],其中n=1~24
					// 除于240让单体容量的变化范围，低于6%
					value = BigDecimal
							.valueOf(packDataExpandLatest.getPackCapPred() * (1 + ((cellCount*2/3.0 - cellCapSort) / (cellCount*10))));
				}
			} else {
				// 浮充电压范围:小于2.1V 或 大于2.35， 单体标称容量*9.999%
				value = BigDecimal.valueOf(getStandardCapacityFromPackType(stationInfo.getPackType()) * 9.999 / 100);
			}
			String name = CELL_CAP_PREFIX + cellNum;
			BeanValueUtils.bindProperty(name, value, packDataExpandLatest);
		}

	}

	private void singleEntitySortHandler(Map<Integer, BigDecimal> cellVoltageMap, List<CellVoltage> inners,
			List<CellVoltage> outers) {
		for (Map.Entry<Integer, BigDecimal> entry : cellVoltageMap.entrySet()) {
			Integer cellNum = entry.getKey();
			BigDecimal voltage = entry.getValue();
			CellVoltage cellVoltage = new CellVoltage(cellNum, voltage);
			if (voltage.compareTo(VALID_MIN_VOLTAGE) < 0 || voltage.compareTo(VALID_MAX_VOLTAGE) > 0) {
				// 有效浮充电压2.1V~2.35V外
				outers.add(cellVoltage);
			} else {
				// 有效浮充电压2.1V~2.35V内
				inners.add(cellVoltage);
			}
		}
	}
	/**
	 * 有核容放电时，单体容量排序。
	 * 1.故障单体，按内阻从小到大，排最后。
	 * 2.内阻值超过标准值的，按从小到大，排故障单体前面
	 * 3.新电池，按截至电压，从高到底，排最前面。
	 * 4.其余电池，按截至电压，从高到底，排新电池后面。
	 * @param gprsConfigInfo
	 * @param cells
	 * @param packDataExpandLatest
	 * @param currentDate
	 */
	private int calculateEntityPredictionCapacitySort1(GprsConfigInfo gprsConfigInfo, List<CellInfo> cells, PackDataExpandLatest packDataExpandLatest, 
			List<PackDataInfo> packDataInfos, Date currentDate) {
		//内阻最大标准值
		double stardBadResist = 6.0;
		Parameter query = new Parameter();
		query.setParameterCategory(String.valueOf(gprsConfigInfo.getDeviceType()));
		query.setParameterCode("resistance");
		List<Parameter> parms = parameterMapper.selectListSelective(query);
		if(!parms.isEmpty()) {
			stardBadResist = Double.parseDouble(parms.get(0).getParameterValue());
		}
		
		//获得内阻平均值
		List<SuggestCellInfo> resistAvgs = packDataExpandService.getResistanceAverge(gprsConfigInfo,packDataExpandLatest);
		if(resistAvgs == null || resistAvgs.size() < cells.size()) {
			if(packDataExpandLatest != null) {
				for (int i = 1; i <= cells.size(); i++) {
					Object resist = ReflectUtil.getValueByKey(packDataExpandLatest, CELL_RESIST_PREFIX + i);
					BigDecimal number = new BigDecimal(resist == null ? "0" : resist.toString());
					SuggestCellInfo suggestCellInfo = new SuggestCellInfo();
					
					suggestCellInfo.setCellIndex(i);
					suggestCellInfo.setCellResist(number);
					resistAvgs.add(suggestCellInfo);
				}
				if (CollectionUtils.isNotEmpty(resistAvgs)) {
					resistAvgs = resistAvgs.stream()
							.sorted(Comparator.comparing(SuggestCellInfo::getCellResist))
							.collect(Collectors.toList());
				}
			}else {
				throw new RuntimeException("无内阻平均值：" + gprsConfigInfo.getGprsId());
			}
		}

		//标记为故障的电池单体编号
		List<Integer> badCellIndex = new ArrayList<Integer>();
		for(CellInfo cell : cells) {
			if(cell.getFaultMark() > 0) {
				badCellIndex.add(cell.getCellIndex());
			}
		}
		//内阻值超过标准值的单体编号
		List<Integer> exceedCellIndex = new ArrayList<Integer>();
		for(SuggestCellInfo sCellInfo : resistAvgs) {
			if(sCellInfo.getCellResist().doubleValue() >= stardBadResist) {
				if(!badCellIndex.contains(sCellInfo.getCellIndex())){
					exceedCellIndex.add(sCellInfo.getCellIndex());
				}
			}
		}
	
		
		int bIndex = cells.size() + 1 - badCellIndex.size();//故障单体中，内阻最小的排序值。
		int eindex = bIndex - exceedCellIndex.size();//内阻超过标准值单体排序的起始值
		for(SuggestCellInfo resist : resistAvgs ) {
			if(badCellIndex.contains(resist.getCellIndex())) {
				String name = CELL_CAP_SORT_PREFIX + resist.getCellIndex();
				BeanValueUtils.bindProperty(name, bIndex, packDataExpandLatest);
				bIndex ++;
			}else if(exceedCellIndex.contains(resist.getCellIndex())){
				String name = CELL_CAP_SORT_PREFIX + resist.getCellIndex();
				BeanValueUtils.bindProperty(name, eindex, packDataExpandLatest);
				eindex ++;
			}
		}
		
		
		//新电池单体编号
		List<Integer> newCellIndex = new ArrayList<Integer>();
		for(CellInfo cell : cells) {
			if(cell.getUseFrom() != null && MyDateUtils.diffMonths(cell.getUseFrom(), currentDate) < 13){
				 if(!badCellIndex.contains(cell.getCellIndex()) && ! exceedCellIndex.contains(cell.getCellIndex())){
					 newCellIndex.add(cell.getCellIndex());
				 }
			}
		}
		//放电截至时的放电数据
		PackDataInfo endRecord = packDataInfos.get(packDataInfos.size() - 1);
		//截至电压
		for (SuggestCellInfo sugCellInfo : resistAvgs) {
			BigDecimal endVol = (BigDecimal) ReflectUtil.getValueByKey(endRecord, CELL_VOL_PREFIX + sugCellInfo.getCellIndex());
			ReflectUtil.setValueByKet(sugCellInfo, "endVol", endVol);
		}
		//按电压从高到底排列
		resistAvgs = resistAvgs.stream().sorted(Comparator.comparing(SuggestCellInfo::getEndVol).reversed()).collect(Collectors.toList());
		
		int nIndex = 1;//新电池起始编号
		int oIndex = newCellIndex.size() + 1;//其余电池起始编号
		for(SuggestCellInfo sCell : resistAvgs) {
			if(newCellIndex.contains(sCell.getCellIndex())){
				String name = CELL_CAP_SORT_PREFIX + sCell.getCellIndex();
				BeanValueUtils.bindProperty(name, nIndex, packDataExpandLatest);
				nIndex ++;
			}else if(!badCellIndex.contains(sCell.getCellIndex()) && ! exceedCellIndex.contains(sCell.getCellIndex())) {//非新，非故障，非超过标准值的单体
				String name = CELL_CAP_SORT_PREFIX + sCell.getCellIndex();
				BeanValueUtils.bindProperty(name, oIndex, packDataExpandLatest);
				oIndex ++;
			}
		}
		
		//返回总的异常单体数量
		return exceedCellIndex.size()+badCellIndex.size();
	}
	
	/** 无有效核容放电时，单体容量排序。
	 * 1.故障单体，按内阻从小到大，排最后。
	 * 2.内阻超过设定值的，按从小到大，排故障单体前面
	 * 3.新电池，按内阻从小到大，排最前。
	 * 4.其余电池，按内阻从小到大，排中间。
	 * @param gprsConfigInfo
	 * @param cells
	 * @param packDataExpandLatest
	 * @param currentDate
	 */
	private int calculateEntityPredictionCapacitySort2(GprsConfigInfo gprsConfigInfo, List<CellInfo> cells, PackDataExpandLatest packDataExpandLatest, Date currentDate) {
		//获得内阻平均值
		List<SuggestCellInfo> resistAvgs = packDataExpandService.getResistanceAverge(gprsConfigInfo,packDataExpandLatest);
		if(resistAvgs == null || resistAvgs.size() < cells.size()) {
			if(packDataExpandLatest != null) {
				for (int i = 1; i <= cells.size(); i++) {
					Object resist = ReflectUtil.getValueByKey(packDataExpandLatest, CELL_RESIST_PREFIX + i);
					BigDecimal number = new BigDecimal(resist == null ? "0" : resist.toString());
					SuggestCellInfo suggestCellInfo = new SuggestCellInfo();
					
					suggestCellInfo.setCellIndex(i);
					suggestCellInfo.setCellResist(number);
					resistAvgs.add(suggestCellInfo);
				}
				if (CollectionUtils.isNotEmpty(resistAvgs)) {
					resistAvgs = resistAvgs.stream()
							.sorted(Comparator.comparing(SuggestCellInfo::getCellResist))
							.collect(Collectors.toList());
				}
			}else {
				throw new RuntimeException("无内阻平均值：" + gprsConfigInfo.getGprsId());
			}
		
		}
		//内阻最大标准值
		double stardBadResist = 6.0;
		Parameter query = new Parameter();
		query.setParameterCategory(String.valueOf(gprsConfigInfo.getDeviceType()));
		query.setParameterCode("resistance");
		List<Parameter> parms = parameterMapper.selectListSelective(query);
		if(!parms.isEmpty()) {
			stardBadResist = Double.parseDouble(parms.get(0).getParameterValue());
		}
		
//		//新电池单体编号
//		List<Integer> newCellIndex = new ArrayList<Integer>();
		//标记为故障的电池单体编号
		//List<Integer> badCellIndex = new ArrayList<Integer>();
//		for(CellInfo cell : cells) {
//			if(cell.getFaultMark() > 0) {
//				badCellIndex.add(cell.getCellIndex());
//			}else if(cell.getUseFrom() != null){
//				 if(MyDateUtils.diffMonths(cell.getUseFrom(), currentDate) < 13){
//					 newCellIndex.add(cell.getCellIndex());
//				 }
//			}
//		}
		
		//标记为故障的电池单体编号
		List<Integer> badCellIndex = new ArrayList<Integer>();
		for(CellInfo cell : cells) {
			if(cell.getFaultMark() > 0) {
				badCellIndex.add(cell.getCellIndex());
			}
		}
		//内阻值超过标准值的单体编号
		List<Integer> exceedCellIndex = new ArrayList<Integer>();
		for(SuggestCellInfo sCellInfo : resistAvgs) {
			if(sCellInfo.getCellResist().doubleValue() >= stardBadResist) {
				if(!badCellIndex.contains(sCellInfo.getCellIndex())){
					exceedCellIndex.add(sCellInfo.getCellIndex());
				}
			}
		}
		int bIndex = cells.size() + 1 - badCellIndex.size();//故障单体中，内阻最小的排序值。
		int eindex = bIndex - exceedCellIndex.size();//内阻超过标准值单体排序的起始值
		for(SuggestCellInfo resist : resistAvgs ) {
			if(badCellIndex.contains(resist.getCellIndex())) {
				String name = CELL_CAP_SORT_PREFIX + resist.getCellIndex();
				BeanValueUtils.bindProperty(name, bIndex, packDataExpandLatest);
				bIndex ++;
			}else if(exceedCellIndex.contains(resist.getCellIndex())){
				String name = CELL_CAP_SORT_PREFIX + resist.getCellIndex();
				BeanValueUtils.bindProperty(name, eindex, packDataExpandLatest);
				eindex ++;
			}
		}
		//新电池单体编号
		List<Integer> newCellIndex = new ArrayList<Integer>();
		for(CellInfo cell : cells) {
			if(cell.getUseFrom() != null && MyDateUtils.diffMonths(cell.getUseFrom(), currentDate) < 13){
				 if(!badCellIndex.contains(cell.getCellIndex()) && ! exceedCellIndex.contains(cell.getCellIndex())){
					 newCellIndex.add(cell.getCellIndex());
				 }
			}
		}

		int nIndex = 1;//新电池起始编号
		int oIndex = newCellIndex.size() + 1;//其余电池起始编号
		for(SuggestCellInfo sCell : resistAvgs) {
			if(newCellIndex.contains(sCell.getCellIndex())){
				String name = CELL_CAP_SORT_PREFIX + sCell.getCellIndex();
				BeanValueUtils.bindProperty(name, nIndex, packDataExpandLatest);
				nIndex ++;
			}else if(!badCellIndex.contains(sCell.getCellIndex()) && ! exceedCellIndex.contains(sCell.getCellIndex())) {//非新，非故障，非超过标准值的单体
				String name = CELL_CAP_SORT_PREFIX + sCell.getCellIndex();
				BeanValueUtils.bindProperty(name, oIndex, packDataExpandLatest);
				oIndex ++;
			}
		}
		
		
//		int nIndex = 0;//新电池排序起始位
//		int oIndex = cells.size() + 1 - badCellIndex.size();//故障单体中，内阻最小的排序值。
//		int index = newCellIndex.size() + 1;//其余单体排序的起始值
//		for(SuggestCellInfo resist : resistAvgs ) {
//			if(newCellIndex.contains(resist.getCellIndex())){
//				nIndex++;
//				String name = CELL_CAP_SORT_PREFIX + resist.getCellIndex();
//				BeanValueUtils.bindProperty(name, nIndex, packDataExpandLatest);
//			}else if(badCellIndex.contains(resist.getCellIndex())) {
//				String name = CELL_CAP_SORT_PREFIX + resist.getCellIndex();
//				BeanValueUtils.bindProperty(name, oIndex, packDataExpandLatest);
//				oIndex ++;
//			}else {
//				String name = CELL_CAP_SORT_PREFIX + resist.getCellIndex();
//				BeanValueUtils.bindProperty(name, index, packDataExpandLatest);
//				index ++;
//			}
//		}
		
		return exceedCellIndex.size()+badCellIndex.size();
	}
	
	/**
	 * 单体预测容量排序计算
	 * 
	 * <pre>
	 *     1. 有最近一次有效放电记录
	 *     <p>
	 *         1、有效浮充电压在2.1V≤X≤2.35V范围内的24-Num个单体：排序规则：按照单体容量排序根据有效截止电压从大到小依次排为1、2、3、4…、24-Num。
	 *         2、有效浮充电压在2.1V≤X≤2.35V范围外的Num个单体：排序规则：随机排列为（24-Num+1）~24
	 *
	 *         单体放电有效截止电压：{单体本次放电记录最后12条cell_vol_X值之和，减去其中最高及最低的值}/10
	 *         (同‘单体有效浮充电压’计算方式一致)
	 *         Num:有效浮充电压在2.1V≤X≤2.35V范围外的单体个数为Num
	 *     </p>
	 *     2. 无最近一次有效放电记录
	 *     <p>
	 *         1、有效浮充电压在2.1V≤X≤2.35V范围内的24-Num个单体：排序规则：按照单体容量排序根据有效浮充电压从小到大依次排为1、2、3、4…、24-Num。
	 *         2、有效浮充电压在2.1V≤X≤2.35V范围外的Num个单体：排序规则：随机排列为（24-Num+1）~24
	 *
	 *         浮充电压排序： 是从小到大依次为1、2、3…、24-Num
	 *         Num:有效浮充电压在2.1V≤X≤2.35V范围外的单体个数为Num
	 *     </p>
	 * </pre>
	 *
	 * @param latestDischargeRecord
	 *            最近一次有效放电记录
	 * @param cellVoltageMap
	 *            单体浮充电压
	 * @param cellVoltageMap2
	 *            单体放电有效截止电压
	 * @param packDataExpandLatest
	 *            需要更新的结果
	 */
	private void calculateEntityPredictionCapacitySort(PackDataInfo latestDischargeRecord,
			Map<Integer, BigDecimal> cellVoltageMap, Map<Integer, BigDecimal> cellVoltageMap2,
			PackDataExpandLatest packDataExpandLatest, StationInfo stationInfo) {
		List<CellVoltage> inners = Lists.newArrayList();
		List<CellVoltage> outers = Lists.newArrayList();

		if (null == latestDischargeRecord) {
			// 无最近一次有效放电记录
			singleEntitySortHandler(cellVoltageMap, inners, outers);
			// 按照单体容量排序有效浮充电压从小到大排序
			inners.sort(Comparator.comparing(CellVoltage::getVoltage));
		} else {
			// 有最近一次有效放电记录
			singleEntitySortHandler(cellVoltageMap2, inners, outers);
			// 按照单体容量排序有效截止电压从大到小排序
			inners.sort(Comparator.comparing(CellVoltage::getVoltage).reversed());
		}
		// Num为有效浮充电压或有效截止电压范围外的单体个数
		int value = 0;
		setCellCapSort(inners, value, packDataExpandLatest);
		value = stationInfo.getCellCount() - outers.size();
		if (value == 0) {
			outers.sort(Comparator.comparing(CellVoltage::getVoltage).reversed());
		}
		setCellCapSort(outers, value, packDataExpandLatest);
	}

	private void setCellCapSort(List<CellVoltage> items, int value, PackDataExpandLatest packDataExpandLatest) {
		for (CellVoltage item : items) {
			value++;
			String name = CELL_CAP_SORT_PREFIX + item.getNum();
			BeanValueUtils.bindProperty(name, value, packDataExpandLatest);
		}
	}
	/**
	 * 电池组容量预测，有核容放电的情况,平均电流*时长
	 * @param items
	 * @param packDataExpandLatest
	 * @return
	 */
	private void calculateGroupPredictionCapacity1(PackDataExpandLatest packDataExpandLatest, List<PackDataInfo> discharges, double stationStandardCap) {
		//平均电流
		BigDecimal avg = getAllDischargeAverageCurrent(discharges);
		BigDecimal timePred = packDataExpandLatest.getPackDischargeTimePred();
		BigDecimal capPred = avg.multiply(timePred).abs();
		//和标称容量对比。
		if(capPred.doubleValue() > stationStandardCap * 1.1)
			capPred = new BigDecimal(stationStandardCap * 1.1);
		packDataExpandLatest.setPackCapPred(capPred.intValue());
	}
	/**
	 * 有最近有效放电记录模式
	 * 
	 * <pre>
	 *    电池组预测容量 = 放电电流平均值*时间
	 *    放电电流平均值 = 放电开始后的连续102个gen_cur去掉最大最下后的和除以100
	 *    时间 = 放电结束时间 - 放电开始时间 （小时）
	 * </pre>
	 *
	 * @param items
	 * @param packDataExpandLatest
	 */
	private Date calculateGroupPredictionCapacityInLatestDischargeMode(List<PackDataInfo> items,
			PackDataExpandLatest packDataExpandLatest) {
		List<PackDataInfo> discharges = items.stream().filter(item -> item.getState().equals(DISCHARGE_NAME))
				.sorted(Comparator.comparing(PackDataInfo::getRcvTime)).collect(Collectors.toList());

		int dischargeSize = discharges.size();
		// 获取放电开始，放电结束时间
		Date startTime = discharges.get(0).getRcvTime();
		Date endTime = discharges.get(dischargeSize - 1).getRcvTime();
		BigDecimal diff = BigDecimal.valueOf((endTime.getTime() - startTime.getTime()) / 1000.0 / 3600);

//		BigDecimal avg = get102DischargeAverageCurrent(items);
		BigDecimal avg = getAllDischargeAverageCurrent(items);
		
		LOGGER.debug("计算电池容量，编号：{},平均电流：{},放电时长：{}。", new Object[] { packDataExpandLatest.getGprsId(), avg, diff });
		packDataExpandLatest.setPackCapPred(avg.multiply(diff).abs().intValue());
		packDataExpandLatest.setPackDischargeTimePred(diff);

		return endTime;
	}
	
	public BigDecimal getAllDischargeAverageCurrent(List<PackDataInfo> items){
		List<PackDataInfo> discharges = items.stream().filter(item -> item.getState().equals(DISCHARGE_NAME))
				.collect(Collectors.toList());
		BigDecimal avg = BigDecimal.ZERO;
		if (discharges == null || discharges.size() < 3) {
			LOGGER.warn("未取得放电记录，目前只取得{}条放电数据，返回预测容量0。设备编号{}", discharges.size(), items.get(0).getGprsId());
			return avg;
		}
		discharges.sort(Comparator.comparing(PackDataInfo::getGenCur));
		discharges.remove(0);
		discharges.remove(discharges.size() - 1);
		for (PackDataInfo discharge : discharges) {
			avg = avg.add(discharge.getGenCur());
		}
		avg = avg.divide(BigDecimal.valueOf(discharges.size()), 2, BigDecimal.ROUND_HALF_UP);
		return avg;
	}

	@Override
	public BigDecimal get102DischargeAverageCurrent(List<PackDataInfo> items) {
		List<PackDataInfo> discharges = items.stream().filter(item -> item.getState().equals(DISCHARGE_NAME))
				.sorted(Comparator.comparing(PackDataInfo::getRcvTime)).collect(Collectors.toList());

		List<PackDataInfo> finalList = Lists.newArrayList();
		// 获取放电开始后的连续102个放电记录
		List<PackDataInfo> results = get102PackDataInfos(items);
		if (CollectionUtils.isEmpty(results)) {
			finalList.addAll(discharges);
		} else {
			finalList.addAll(results);
		}
		BigDecimal avg = BigDecimal.ZERO;
		// finalList = finalList.stream().filter(k -> k.getGenCur().abs().doubleValue()
		// <= 100.00 && k.getGenCur().abs().doubleValue() >=
		// 20.00).collect(Collectors.toList());
		if (finalList.size() < 3) {
			LOGGER.warn("未取得放电开始后连续有效的50个放电记录，目前只取得{}条放电数据，返回预测容量0。设备编号{}", finalList.size(), items.get(0).getGprsId());
			return avg;
		}

		finalList.sort(Comparator.comparing(PackDataInfo::getGenCur));
		finalList.remove(0);
		if (finalList.size() > 0) {
			finalList.remove(finalList.size() - 1);
		}
		for (PackDataInfo discharge : finalList) {
			avg = avg.add(discharge.getGenCur());
		}
		avg = avg.divide(BigDecimal.valueOf(finalList.size()), 2, BigDecimal.ROUND_HALF_UP);
		LOGGER.debug("获得有效放电电流数据{}条，平均电流{}，设备编号{}", new Object[] { finalList.size(), avg, items.get(0).getGprsId() });
		return avg;
	}

	private void calculateGroupPredictionCapacity2(Map<Integer, BigDecimal> cellVoltage,
			PackDataExpandLatest packDataExpandLatest, StationInfo stationInfo, double stationStandardCap) {
		BigDecimal sum = BigDecimal.ZERO;
		for (int i = 1; i < stationInfo.getCellCount() + 1; i++) {
			BigDecimal value = (BigDecimal) BeanValueUtils.getValue(CELL_CAP_PREFIX + i, packDataExpandLatest);
			if (value != null) {
				sum = sum.add(value);
			}
		}

		List<BigDecimal> values = cellVoltage.values().stream().sorted(Comparator.comparing(BigDecimal::doubleValue))
				.collect(Collectors.toList());
		double minCellVol = values.get(0).doubleValue();
		double maxCellVol = values.get(values.size() - 1).doubleValue();
		double diff = maxCellVol - minCellVol;
		final double threshold = 0.03;
		if (diff >= threshold) {
			diff = threshold;
		}
		// 容量计算修改
		BigDecimal value = sum.divide(BigDecimal.valueOf(stationInfo.getCellCount()), 2, BigDecimal.ROUND_HALF_UP);
		value = value.multiply(BigDecimal.valueOf(1 + (diff - 0.01) / 0.06)).abs();
		if(value.doubleValue() > stationStandardCap * 1.1) {
			value = new BigDecimal(stationStandardCap * 1.1);
		}
		packDataExpandLatest.setPackCapPred(value.intValue());
	}
	
	/**
	 * 无最近有效放电记录模式
	 * 
	 * <pre>
	 *      先计算单体预测容量，后计算电池组预测容量
	 *      (cell_cap_1+cell_cap_2+…+cell_cap_24)/24*[1+(δ-0.01)/0.06]
	 *      cell_cap_1...cell_cap_24表示单体容量预测；δ=MAX（cell_vol）-MIN（cell_vol）即是1~24号位置的单体浮充电压最大最小电压差,当δ≥0.03时，则δ=0.03。
	 * </pre>
	 */
	private void calculateGroupPredictionCapacityUnLatestDischargeMode(Map<Integer, BigDecimal> cellVoltage,
			PackDataExpandLatest packDataExpandLatest, StationInfo stationInfo) {
		BigDecimal sum = BigDecimal.ZERO;
		for (int i = 1; i < stationInfo.getCellCount() + 1; i++) {
			BigDecimal value = (BigDecimal) BeanValueUtils.getValue(CELL_CAP_PREFIX + i, packDataExpandLatest);
			if (value != null) {
				sum = sum.add(value);
			}
		}

		List<BigDecimal> values = cellVoltage.values().stream().sorted(Comparator.comparing(BigDecimal::doubleValue))
				.collect(Collectors.toList());
		double minCellVol = values.get(0).doubleValue();
		double maxCellVol = values.get(values.size() - 1).doubleValue();
		double diff = maxCellVol - minCellVol;
		final double threshold = 0.03;
		if (diff >= threshold) {
			diff = threshold;
		}
		// 容量计算修改
		BigDecimal value = sum.divide(BigDecimal.valueOf(stationInfo.getCellCount()), 2, BigDecimal.ROUND_HALF_UP);
		value = value.multiply(BigDecimal.valueOf(1 + (diff - 0.01) / 0.06)).abs();
		if(value.doubleValue() > 500 * 1.1) {
			value = new BigDecimal(500 * 1.1);
		}
		packDataExpandLatest.setPackCapPred(value.intValue());
	}

	/**
	 * 判断单体更换时间，如果在12个月内，则设置容量为标称容量-10*使用月份。
	 * 
	 * @param packDataExpandLatest
	 * @param dischargeEndTime
	 */
	private void updateEntityPredictionCapacity(PackDataExpandLatest packDataExpandLatest, StationInfo stationInfo,
			Date dischargeEndTime, Date currentDate) {
		CellInfo info = new CellInfo();
		info.setGprsId(packDataExpandLatest.getGprsId());
		List<CellInfo> items = cellInfoMapper.selectListSelective(info);
		BigDecimal value = BigDecimal.valueOf(getStandardCapacityFromPackType(stationInfo.getPackType()));

		List<CellVoltage> cells = new ArrayList<CellVoltage>();

		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTime(currentDate);
		for (CellInfo item : items) {
			String name = CELL_CAP_PREFIX + item.getCellIndex();

			if (item.getUseFrom() != null) {
				Calendar startCalendar = Calendar.getInstance();
				startCalendar.setTime(item.getUseFrom());
				int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
				int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);

				if (diffMonth <= 12 && diffMonth >= 0) {
					double newValue = value.doubleValue() - 10.0 * diffMonth;
					BeanValueUtils.bindProperty(name, new BigDecimal(newValue), packDataExpandLatest);
					if (LOGGER.isDebugEnabled())
						LOGGER.debug("电池：{}，单体：{}，有更换记录，更新容量：{}。",
								new Object[] { packDataExpandLatest.getGprsId(), item.getCellIndex(), newValue });
				}
			}
			CellVoltage cell = new CellVoltage(item.getCellIndex(),
					((BigDecimal) BeanValueUtils.getValue(name, packDataExpandLatest)).doubleValue());
			cells.add(cell);
		}
		// 重新计算单体容量排序。
		cells.sort(Comparator.comparing(CellVoltage::getCapacity).reversed());
		setCellCapSort(cells, 0, packDataExpandLatest);
	}

	@Override
	public List<PackDataInfo> getContinuousDischargeRecords(PackDataInfo latestDischargeRecord, String gprsId) {
		List<PackDataInfo> packDataInfos = Lists.newArrayList();
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		List<PackDataInfo> forwards = forwardLookup(latestDischargeRecord.getId(), gprsId);
		stopWatch.stop();
		LOGGER.debug("获取{}条向前测试数据相对于最近一次有效放电记录,耗时:{}，设备编号：{}",
				new Object[] { CollectionUtils.isEmpty(forwards) ? 0 : forwards.size(), stopWatch, gprsId });
		if (CollectionUtils.isNotEmpty(forwards)) {
			packDataInfos.addAll(forwards);
		}
		// 最近一次有效放电记录放入容器
		packDataInfos.add(latestDischargeRecord);
		stopWatch.reset();
		stopWatch.start();
		List<PackDataInfo> backwards = backwardLookup(latestDischargeRecord.getId(), gprsId);
		stopWatch.stop();
		LOGGER.debug("获取{}条向后测试数据相对于最近一次有效放电记录,耗时:{}，设备编号：{}",
				new Object[] { CollectionUtils.isEmpty(backwards) ? 0 : backwards.size(), stopWatch, gprsId });
		if (CollectionUtils.isNotEmpty(backwards)) {
			packDataInfos.addAll(backwards);
		}
		packDataInfos.sort(Comparator.comparing(PackDataInfo::getId));

		LOGGER.debug("获取总{}/前{}后/{}条测试数据相对于最近一次有效放电记录-->基站编号:{}",
				new Object[] { packDataInfos.size(), forwards.size(), backwards.size(), gprsId });
		// 计算完成后，刷新有效放电数据缓存。
		dischargeCache.resetContent(gprsId, packDataInfos);
		return packDataInfos;
	}

	public List<PackDataInfo> getCheckedContinuousDischargeRecords(String gprsId) {
		// 获取从现在最近一次有效放电记录
		Date startDate = new Date();
		if (StringUtils.isBlank(gprsId)) {
			return null;
		}
		GprsConfigInfo condition = new GprsConfigInfo();
		condition.setGprsId(gprsId);
		List<GprsConfigInfo> configs = gprsConfigInfoMapper.selectListSelective(condition);
		if (CollectionUtils.isEmpty(configs)) {
			return null;
		}
		GprsConfigInfo gprsConfigInfo = configs.get(0);
		while (true) {
			PackDataInfo latestDischargeRecord = getLatestDischargeRecord(gprsConfigInfo, startDate);
			if (latestDischargeRecord == null) {
				return null;
			}
			List<PackDataInfo> discharges = getContinuousDischargeRecords(latestDischargeRecord, gprsId);
			if (discharges.isEmpty())
				return null;
			List<PackDataInfo> pureDischarges = discharges.stream()
					.filter(item -> DISCHARGE_NAME.equals(item.getState())).collect(Collectors.toList());
			PackDataInfo firstDischarge = pureDischarges.get(0);
			PackDataInfo lastDischarge = pureDischarges.get(pureDischarges.size() - 1);
			long timeDiff = lastDischarge.getRcvTime().getTime() - firstDischarge.getRcvTime().getTime();
			if (timeDiff >= MAX_AGE * 3600 * 1000) {// 放电间隔大于最大放电时间
				return discharges;
			} else {
				if (lastDischarge.getGenVol().doubleValue() < MIN_VOL)
					return discharges;
			}
			startDate = lastDischarge.getRcvTime();
		}
	}

	private List<PackDataInfo> backwardLookup(Integer startId, String gprsId) {
		List<PackDataInfo> results = Lists.newArrayList();
		// 向后找连续10条非放电态的数据
		int pageNum = 0;
		int count = 0;
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
			for (PackDataInfo item : items) {
				results.add(item);
				if (!DISCHARGE_NAME.equals(item.getState())) {
					count++;
				} else {
					count = 0;
				}
				if (count >= 10) {
					break;
				}
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
		// 向后找连续10条非放电态的数据没找到返回空
//		if (count < 5) {
//			return Collections.emptyList();
//		}
		return results;
	}

	private List<PackDataInfo> getLatestNormalData(String gprsId, Date endTime) {
		List<PackDataInfo> results = Lists.newArrayList();
		Map<String, Object> paramMap = Maps.newHashMap();
		paramMap.put("gprsId", gprsId);
		paramMap.put("state", "浮充");
		paramMap.put("rcvTime", endTime);
		results = packDataInfoMapper.selectByOrderSelective(paramMap);
		return results;
	}

	private List<PackDataInfo> forwardLookup(Integer startId, String gprsId) {
		List<PackDataInfo> results = Lists.newArrayList();
		// 向前找连续5条非放电态的数据
		int pageNum = 0;
		int count = 0;
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
			for (PackDataInfo item : items) {
				results.add(item);
				if (!DISCHARGE_NAME.equals(item.getState())) {
					count++;
				} else {
					count = 0;
				}
				if (count >= 10) {
					break;
				}
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
		// 向前找连续10条非放电态的数据没找到返回空
//		if (count < 10) {
//			return Collections.emptyList();
//		}
		return results;
	}

	@Override
	public PackDataInfo getLatestDischargeRecord(String gprsId, Date date) {
		if (StringUtils.isBlank(gprsId)) {
			return null;
		}
		GprsConfigInfo condition = new GprsConfigInfo();
		condition.setGprsId(gprsId);
		List<GprsConfigInfo> configs = gprsConfigInfoMapper.selectListSelective(condition);
		if (CollectionUtils.isEmpty(configs)) {
			return null;
		}
		return getLatestDischargeRecord(configs.get(0), date);
	}

	private PackDataInfo getLatestDischargeRecord(GprsConfigInfo gprsConfigInfo, Date date) {
		if (null == gprsConfigInfo) {
			return null;
		}
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		// 如该电池组历史数据在valid_day期限内，有数据同时满足gen_vol小于等于valid_discharge_vol及gen_cur大于等于valid_discharge_cur两个条件，且有5条连续数据，则电池组有最近一次放电记录。
		// 否则，继续往前找，直到超过时间有效期。
		Date rcvTimeEnd = null;
		Date rcvTimeStart = null;
		PackDataInfo packDataInfo = null;
		if (date != null) {
			rcvTimeEnd = date;
			rcvTimeStart = DateUtils.addDays(date, -gprsConfigInfo.getValidDay());
		}
        int pageNum = 0;
        int pageSize = 20;
		while(true) {
			Map<String, Object> paramMap1 = Maps.newHashMap();
			paramMap1.put("gprsId", gprsConfigInfo.getGprsId());
	        paramMap1.put("endTime", date);
	        paramMap1.put("pageNum", pageNum);
	        paramMap1.put("pageSize", pageSize);
	        List<PackDataInfo> items = packDataInfoMapper.getPackDataInfosByTimes(paramMap1);
	        if (CollectionUtils.isEmpty(items)) {
				break;
			}
	        boolean findNotDischarge = false;
			for(PackDataInfo info : items) {
				if(!DISCHARGE_NAME.equals(info.getState())){//非放电数据
					date = info.getRcvTime();
					findNotDischarge = true;
					break;
				}
			}
			if(findNotDischarge)
				break;
			else {
				pageNum += pageSize;
				date = items.get(items.size() - 1).getRcvTime();
			}
		}
		
		while (true) {
			Map<String, Object> paramMap = Maps.newHashMap();
			paramMap.put("gprsId", gprsConfigInfo.getGprsId());
			paramMap.put("state", DISCHARGE_NAME);
			paramMap.put("maxVol", gprsConfigInfo.getValidDischargeVol());
			paramMap.put("minVol", 20.00);
			// (>= abs(minCur) && <= abs(maxCur)) or (>= maxCur && <=minCur)
			BigDecimal minCur = gprsConfigInfo.getValidDischargeCur().abs();
			paramMap.put("minCur", minCur.multiply(BigDecimal.valueOf(-1)));
			BigDecimal maxCur = gprsConfigInfo.getMaxDischargeCur() == null ? BigDecimal.valueOf(100)
					: gprsConfigInfo.getMaxDischargeCur();
			maxCur = maxCur.abs();
			paramMap.put("maxCur", maxCur.multiply(BigDecimal.valueOf(-1)));
			if (rcvTimeStart != null) {
				paramMap.put("rcvTime1", rcvTimeStart);
			}
			if (rcvTimeEnd != null) {
				paramMap.put("rcvTime2", rcvTimeEnd);
			}
			// 读取最近5条放电记录，然后判断时间是否连续。
			List<PackDataInfo> packDataInfos = packDataInfoMapper.getLatestValidDischargeRecord(paramMap);

			if (packDataInfos == null || packDataInfos.size() < 2) {// 没找到数据，或者少于2条数据，则无效。
				LOGGER.debug("查找到放电记录失败。gprsid:{},符合条件的数据条数：{}",
						new Object[] { gprsConfigInfo.getGprsId(), packDataInfos == null ? "" : packDataInfos.size() });
				packDataInfo = null;
				break;
			} else {
				packDataInfos = packDataInfos.stream().sorted(Comparator.comparing(PackDataInfo::getId).reversed()).collect(Collectors.toList());
				PackDataInfo packDataInfo1 = packDataInfos.get(0);
				PackDataInfo packDataInfo5 = packDataInfos.get(1);
				if ((packDataInfo1.getRcvTime().getTime() - packDataInfo5.getRcvTime().getTime()) < 11 * 60 * 1000) {// 2条记录时间间隔在10分钟内。
					packDataInfo = packDataInfo1;
					break;
				} else {
					LOGGER.debug("查找到放电记录，但是时间间隔超过标准值。gprsid:{},startid:{}, endId:{}",
							new Object[] { gprsConfigInfo.getGprsId(), packDataInfo1.getId(), packDataInfo5.getId() });
					rcvTimeEnd = packDataInfo5.getRcvTime();// 以这次找到的最前一条记录往前继续查找。
				}
			}
		}
		stopWatch.stop();
		LOGGER.debug("{}最近一次有效放电记录:{},设备编号:{},耗时:{}", new Object[] { packDataInfo == null ? "无" : "有",
				packDataInfo == null ? "" : packDataInfo.getId(), gprsConfigInfo.getGprsId(), stopWatch });
		return packDataInfo;
	}

	/**
	 * 单体标称容量：电池出厂容量，由pack_type获取，如pack_type是48V500ah，则标称容量为500。
	 *
	 * @param packType
	 * @return
	 */
	@Override
	public double getStandardCapacityFromPackType(String packType) {
		// 48V500AH
		packType = StringUtils.trimToEmpty(packType).toUpperCase();
		return ConvertUtils.toDouble(StringUtils.substring(packType, packType.indexOf("V") + 1, packType.indexOf("AH")),
				500d);
	}

	/**
	 * 计算单体容量。
	 * 1.使用时间在1年内，标称容量-10*使用月份
	 * 2.使用时间超过1年，标称容量*（1-使用年限*20%)
	 * 3.使用时间超过3.5年，用3.5代替。
	 * 4.不知道使用时间，用3代替。
	 * @param stationInfo
	 * @param currentDate
	 * @return
	 */
	private Map<Integer, BigDecimal> calculateCCN2(StationInfo stationInfo, Date currentDate, List<CellInfo> cells) {
		if (null == stationInfo) {
			return MapUtils.EMPTY_MAP;
		}

		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTime(currentDate);

		if (CollectionUtils.isEmpty(cells)) {
			return MapUtils.EMPTY_MAP;
		}
		// CCn=单体标称容量乘以[1-已投入使用年限（精确到1位小数）*20%](n=1~24)
		Map<Integer, BigDecimal> map = Maps.newTreeMap();
		for (CellInfo item : cells) {
			// 已投入使用年限默认为3，实际使用年限小于等于3年时，则使用实际年限，实际使用年限大于3年时统一设置为3.5年；
			double age = DEFAULT_AGE;
			if (item.getUseFrom() != null) {
				Calendar startCalendar = Calendar.getInstance();
				startCalendar.setTime(item.getUseFrom());
				int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
				int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);

				diffMonth = diffMonth == 0 ? 1 : diffMonth;
				age = diffMonth / 12.0;
			}
			if (age > DEFAULT_AGE) {
				age = MAX_AGE;
			}
			BigDecimal value = BigDecimal.valueOf(getStandardCapacityFromPackType(stationInfo.getPackType()));
			if(age <= 1.0) {
				value = value.subtract(BigDecimal.valueOf(1.0 - 0.02 * age * 12));//使用时间在1年内，标称容量*（1-0.02*使用月份）
			}else {
				value = value.multiply(BigDecimal.valueOf(1 - age * 0.2));
			}
			map.put(item.getCellIndex(), value.setScale(1, BigDecimal.ROUND_HALF_UP));
		}
		return map;
	}
	
	@Override
	public Map<Integer, BigDecimal> calculateCCN(StationInfo stationInfo, Date currentDate) {
		if (null == stationInfo) {
			return MapUtils.EMPTY_MAP;
		}

		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTime(currentDate);

		CellInfo info = new CellInfo();
		info.setGprsId(stationInfo.getGprsId());

		List<CellInfo> items = cellInfoMapper.selectListSelective(info);
		if (CollectionUtils.isEmpty(items)) {
			return MapUtils.EMPTY_MAP;
		}
		// CCn=单体标称容量乘以[1-已投入使用年限（精确到1位小数）*20%](n=1~24)
		Map<Integer, BigDecimal> map = Maps.newTreeMap();
		for (CellInfo item : items) {
			// 已投入使用年限默认为3，实际使用年限小于等于3年时，则使用实际年限，实际使用年限大于3年时统一设置为3.5年；
			double age = DEFAULT_AGE;
			if (item.getUseFrom() != null) {
				Calendar startCalendar = Calendar.getInstance();
				startCalendar.setTime(item.getUseFrom());
				int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
				int diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);

				diffMonth = diffMonth == 0 ? 1 : diffMonth;
				age = diffMonth / 12.0;
			}
			if (age > DEFAULT_AGE) {
				age = MAX_AGE;
			}
			BigDecimal value = BigDecimal.valueOf(getStandardCapacityFromPackType(stationInfo.getPackType()));
			value = value.multiply(BigDecimal.valueOf(1 - age * 0.2));
			map.put(item.getCellIndex(), value.setScale(1, BigDecimal.ROUND_HALF_UP));
		}
		return map;
	}

	private Map<Integer, BigDecimal> calculateValidVoltageHandler(StationInfo stationInfo, List<PackDataInfo> packDataInfos) {
		if (CollectionUtils.isEmpty(packDataInfos) || packDataInfos.size() < 12) {
			return MapUtils.EMPTY_MAP;
		}
		// 最近12次浮充态的单体电压数据
		List<PackDataInfo> top12 = new ArrayList<>(12);
		top12.addAll(packDataInfos.subList(0, 12));
		Map<Integer, List<BigDecimal>> map = Maps.newHashMap();
		for (PackDataInfo packDataInfo : top12) {
			for (int i = 1; i <= stationInfo.getCellCount(); i++) {
				map.computeIfAbsent(i, key -> Lists.newArrayList())
						.add((BigDecimal) BeanValueUtils.getValue("cellVol" + i, packDataInfo));
			}
		}
		Map<Integer, BigDecimal> finalMap = Maps.newTreeMap();
		for (Map.Entry<Integer, List<BigDecimal>> entry : map.entrySet()) {
			List<BigDecimal> values = entry.getValue();
			values.sort(Comparator.comparing(BigDecimal::doubleValue).reversed());
			// 移除最大
			values.remove(0);
			// 移除最小
			values.remove(10);
			BigDecimal sum = BigDecimal.ZERO;
			for (BigDecimal value : values) {
				sum = sum.add(value);
			}
			finalMap.put(entry.getKey(), sum.divide(BigDecimal.TEN, 3, BigDecimal.ROUND_HALF_UP));
		}
		return finalMap;
	}

	@Override
	public List<PackDataInfo> getValidVoltage(String gprsId, Date currentDate) {
		final int pageSize = 12;
		Date startTime = DateUtils.addDays(currentDate, -7);
		Map<String, Object> paramMap = Maps.newHashMap();
		paramMap.put("gprsId", gprsId);
		paramMap.put("state", "浮充");
//		paramMap.put("startTime", startTime);
		paramMap.put("endTime", currentDate);
		paramMap.put("pageNum", 0);
		paramMap.put("pageSize", pageSize);
		return packDataInfoMapper.getPackDataInfosByTimes(paramMap);
	}

	@Override
	public Map<Integer, BigDecimal> calculateValidVoltage(StationInfo stationInfo, Date currentDate) {
		List<PackDataInfo> items = getValidVoltage(stationInfo.getGprsId(), currentDate);
		if (items.size() < 12) {
			return MapUtils.EMPTY_MAP;
		}
		return calculateValidVoltageHandler(stationInfo, items);
	}

	@Override
	public Map<Integer, PulseDischargeInfo> getLatestPulseDischargeInfo(String gprsId) {
		LOGGER.debug("设备编号:{}", gprsId);
		if (StringUtils.isBlank(gprsId) || StringUtils.equals(gprsId, INVALID_GPRS_ID)) {
			return MapUtils.EMPTY_MAP;
		}
		PulseDischargeSend pulseDischargeSend = new PulseDischargeSend();
		pulseDischargeSend.setGprsId(gprsId);
		pulseDischargeSend.setSendDone(SEND_DONE);
		List<PulseDischargeSend> sends = pulseDischargeSendMapper.selectListSelective(pulseDischargeSend);
		LOGGER.debug("获得{}条特征执行成功数据-->设备编号:{}", CollectionUtils.isEmpty(sends) ? 0 : sends.size(), gprsId);
		if (CollectionUtils.isEmpty(sends)) {
			return MapUtils.EMPTY_MAP;
		}
		Map<Integer, List<PulseDischargeSend>> cellMap = sends.stream()
				.collect(Collectors.groupingBy(PulseDischargeSend::getPulseCell));

		Map<Integer,Date> mapCell = new HashMap<Integer,Date>();
		RoutingInspections  routing = new RoutingInspections();
		routing.setGprsId(gprsId);
		routing.setOperateType(3);//跟换单体
		List<RoutingInspections> routingList = routingInspectionsMapper.selectListSelective(routing);
		if(CollectionUtils.isNotEmpty(routingList)) {
			//获取最近的一条跟换单体记录
			RoutingInspections routingInspection = routingList.stream().max(Comparator.comparing(RoutingInspections::getRoutingInspectionId)).get();
			//跟换的操作时间
			Date operateTime = routingInspection.getOperateTime();
			//通过routingInspectionId查询跟换的单体
			RoutingInspectionDetail routingDetail = new RoutingInspectionDetail();
			routingDetail.setRoutingInspectionsId(routingInspection.getRoutingInspectionId());
			List<RoutingInspectionDetail> routingDetailList = routingInspectionDetailMapper.selectListSelective(routingDetail);
			if(CollectionUtils.isNotEmpty(routingDetailList)) {
				List<RoutingInspectionDetail> collect = routingDetailList.stream().filter(RoutingInspectionDetail -> RoutingInspectionDetail.getDetailOperateType() != null
						&& (RoutingInspectionDetail.getDetailOperateType() == 5 
						|| RoutingInspectionDetail.getDetailOperateType() == 6)).collect(Collectors.toList());
				for (RoutingInspectionDetail detail : collect) {
					mapCell.put(detail.getCellIndex(), operateTime);
				}
			}
		
		}
		// 获取单体最近一次执行成功的特征指令
		BiMap<Integer, PulseDischargeSend> cellSendMap = HashBiMap.create();
		for (Map.Entry<Integer, List<PulseDischargeSend>> entry : cellMap.entrySet()) {
			List<PulseDischargeSend> items = entry.getValue();
			// 按照结束时间降序排，取最近一条
			items.sort(Comparator.comparing(PulseDischargeSend::getEndTime).reversed());
			PulseDischargeSend item = items.get(0);
			//只有更新电池时间在最后一次脉冲测试时间之前
			if(mapCell.isEmpty()) {
				cellSendMap.put(item.getId(), item);
			}else if(mapCell.get(item.getPulseCell()) == null) {
				cellSendMap.put(item.getId(), item);
			}else if(mapCell.get(item.getPulseCell()).getTime() - item.getEndTime().getTime() < 0){//只有更换电池时间在最后一次脉冲测试时间之前
				cellSendMap.put(item.getId(), item);
			}
			LOGGER.debug("单体编号：{}，特征命令编号：{}，设备编号：{}", new Object[] { entry.getKey(), item.getId(), gprsId });
		}
		List<Integer> sendIds = new ArrayList<Integer>();
		if (cellSendMap.isEmpty()) {
			return MapUtils.EMPTY_MAP;
		}
		cellSendMap.forEach((k, v) -> sendIds.add(v.getId()));
		List<PulseDischargeInfo> items = pulseDischargeInfoMapper
				.findByPulseDischargeSendIds(sendIds.toArray(new Integer[sendIds.size()]));
		if (CollectionUtils.isEmpty(items)) {
			return MapUtils.EMPTY_MAP;
		}

		// Map<Integer, PulseDischargeSend> sendCellMap = cellSendMap.inverse();
		Map<Integer, PulseDischargeInfo> infoMap = Maps.newTreeMap();
		for (PulseDischargeInfo item : items) {
			PulseDischargeSend send = cellSendMap.get(item.getPulseDischargeSendId());
			item.setFastSampleInterval(send.getFastSampleInterval());
			item.setSlowSampleInterval(send.getSlowSampleInterval());
			item.setSlowSampleTime(send.getSlowSampleTime());
			item.setDischargeTime(send.getDischargeTime());
			item.setPulseCell(send.getPulseCell());
			item.setEndTime(send.getEndTime());
			infoMap.putIfAbsent(send.getPulseCell(), item);
		}
		return infoMap;
	}

	@Override
	public BigDecimal calculateSingleEntityResistance(PulseDischargeInfo pulseDischargeInfo, String gprsId) {
		if (null == pulseDischargeInfo || StringUtils.isBlank(pulseDischargeInfo.getVoltage())
				|| StringUtils.isBlank(pulseDischargeInfo.getCurrent())) {
			LOGGER.warn("计算单体内阻，未找到返回结果，或返货结果电流/电压为空。设备编号：{}", gprsId);
			return null;
		}
		// 区间1（1000/采样间隔），2（采样时间*1000/100），3（1000/采样间隔）的总采样数据量
		int sampleCount1 = 0;
		int sampleCount2 = 0;
		int sampleCount3 = 0;
		int sampleCount4 = 50;// 固定值
		int sampleCount5 = -1;// sampleCount5 = 总长度  -（sampleCount1 + sampleCount2 + sampleCount3 + sampleCount4）
		switch (pulseDischargeInfo.getFastSampleInterval()) {
		case 0:
			sampleCount1 += 1000 / 5;
			sampleCount3 += 1000 / 5;
			break;
		case 1:
			sampleCount1 += 1000 / 10;
			sampleCount3 += 1000 / 10;
			break;
		case 2:
			sampleCount1 += 1000 / 20;
			sampleCount3 += 1000 / 20;
			break;
		case 3:
			sampleCount1 += 1000 / 40;
			sampleCount3 += 1000 / 40;
			break;
		default:
			sampleCount1 += 20;
		}
		switch (pulseDischargeInfo.getDischargeTime()) {
			case 0:
				sampleCount2 += 1 * 1000 / 100;
				break;
			case 1:
				sampleCount2 += 2 * 1000 / 100;
				break;
			case 2:
				sampleCount2 += 3 * 1000 / 100;
				break;
			case 3:
				sampleCount2 += 4 * 1000 / 100;
				break;
			case 4:
				sampleCount2 += 5 * 1000 / 100;
				break;
			case 9:
				sampleCount2 += 10 * 1000 / 100;
				break;
			default:
				throw new IllegalArgumentException("区间2采样参数错误! -->" + pulseDischargeInfo.getDischargeTime());
		}

		int sampleCount = sampleCount1 + sampleCount2 + sampleCount3 + sampleCount4;
		// 判断filteredvoltage，filteredcurrent是否为空，如果为空，则需要先计算出该值。
		if (pulseDischargeInfo.getFilterCurrent() == null || pulseDischargeInfo.getFilterVoltage() == null
				|| pulseDischargeInfo.getFilterVoltage().length() < 1
				|| pulseDischargeInfo.getFilterCurrent().length() < 1) {

			// 读取voltage，过滤掉范围值不在1.5-2.5之间的值。
			List<Double> vol = ConvertUtils.separateStringIntoDoubleList(pulseDischargeInfo.getVoltage());
			// 判断取区间1，区间2，3，4的数据量是否够。且区间5的数量不少于10条   11/29
			if (vol.size() - sampleCount < 10) {
				LOGGER.debug("特征测试电压返回值数量不够。特征命令编号：{}，结果编号：{}",
						new Object[] { pulseDischargeInfo.getPulseDischargeSendId(), pulseDischargeInfo.getId() });
				return null;
			}

			// 从第三位起循环，读取它的前2位，后2两个的值 ，算平均值，如果当前值超过最大值+0.01，或者小于最小值-0.01.则用平均值替代当期值。
			// 取2条浮充状态的电压数据。
			List<PackDataInfo> datas = getLatestNormalData(gprsId, pulseDischargeInfo.getEndTime());
			double normalVoltage = 0;
			if (!datas.isEmpty()) {
				String name = "cellVol" + pulseDischargeInfo.getPulseCell();
				PackDataInfo firstPackDataInfo = datas.get(0);
				PackDataInfo lastPackDataInfo = datas.get(datas.size() - 1);
				BigDecimal firstNormalVoltage = (BigDecimal) BeanValueUtils.getValue(name, firstPackDataInfo);
				BigDecimal lastNormalVoltage = (BigDecimal) BeanValueUtils.getValue(name, lastPackDataInfo);
				normalVoltage = (firstNormalVoltage.doubleValue() + lastNormalVoltage.doubleValue()) / 2.0;
			}
			if (normalVoltage < 2.0 || normalVoltage > 2.5)
				normalVoltage = 2.24;
			List<Double> newVol = Lists.newArrayList();
			newVol.add(normalVoltage);
			newVol.addAll(vol);
			vol = DoubleUtil.filter(newVol, 2.5, 1.5, 0.01);
			vol.remove(0);

			List<Double> current = ConvertUtils.separateStringIntoDoubleList(pulseDischargeInfo.getCurrent());
			// 对Y0A的设备，特殊处理。主机为处理传送数据，解析器做了解析，需要反转回去，然后再除以30
			if (gprsId != null && gprsId.toUpperCase().startsWith("Y0A")) {
				current = current.stream().map(k -> (k + 300.00) * 100).map(k -> k > 32768 ? 65536 - k : k)
						.map(k -> new BigDecimal(k / 30.00).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue())
						// .sorted(Comparator.comparing(Double::doubleValue))
						.collect(Collectors.toList());
			}
			if (current.size() < sampleCount) {
				LOGGER.debug("特征测试电流返回值数量不够。特征命令编号：{}，结果编号：{}",
						new Object[] { pulseDischargeInfo.getPulseDischargeSendId(), pulseDischargeInfo.getId() });
				return null;
			}

			// 电流同样的逻辑。范围值在0-20安，差值在正负2a之内。
			current = DoubleUtil.filter(current, 20.0, 0.0, 2.0);
			// 转换成字符字符串
			String filterVoltage = "";
			for (Double filterVol : vol) {
				filterVoltage += filterVol + "-";
			}
			filterVoltage = filterVoltage.substring(0, filterVoltage.length() - 2);
			String filterCurrent = "";
			for (Double filterCur : current) {
				filterCurrent += filterCur + "-";
			}
			filterCurrent = filterCurrent.substring(0, filterCurrent.length() - 2);
			// 将结果保持到数据库中。
			PulseDischargeInfo pulse = new PulseDischargeInfo();
			pulse.setId(pulseDischargeInfo.getId());
			pulse.setFilterVoltage(filterVoltage);
			pulse.setFilterCurrent(filterCurrent);
			pulseDischargeInfoService.updateByPrimaryKeySelective(pulse);

			pulseDischargeInfo.setFilterVoltage(filterVoltage);
			pulseDischargeInfo.setFilterCurrent(filterCurrent);
		}

		// 电压读取区间1，区间2的数据。
		List<Double> values = ConvertUtils.separateStringIntoDoubleList(pulseDischargeInfo.getFilterVoltage());
		if (values.size() - sampleCount < 10) {
			LOGGER.debug("特征测试电压返回值过滤后数量不够，数据库被修改。结果编号：{},结果数量{},期望数量{}", new Object[] { pulseDischargeInfo.getId(),
					values.size(), sampleCount + 10});
			return null;
		}
		/* 改 11/29
		 * 最大值=区间5的前60个值的平均值，不足60个，按获得的数据个数平均，但是不能少于10个
		 * 最小值为区间2的最小值。具体逻辑可参见pm里面的文档说明
		 * */
		// 走到这来了，说明sampleCount5肯定是不小于10的
		sampleCount5 = values.size() - sampleCount;
		List<Double> minVols = values.subList(sampleCount1, sampleCount1 + sampleCount2);
		double minVoltage = minVols.stream().mapToDouble(m -> m.doubleValue()).average().getAsDouble();
		List<Double> maxVols = values.subList(sampleCount, sampleCount5 > 10 ? sampleCount + 10 : sampleCount + sampleCount5);
		double maxVoltage = maxVols.stream().mapToDouble(m -> m.doubleValue()).average().getAsDouble();
		
		values = ConvertUtils.separateStringIntoDoubleList(pulseDischargeInfo.getFilterCurrent());
		if (values.size() - sampleCount < 10) {
			LOGGER.debug("特征测试电流返回值过滤后数量不够，数据库被修改。结果编号：{},结果数量{},期望数量{}", new Object[] { pulseDischargeInfo.getId(),
					values.size(), sampleCount + 10});
			return null;
		}
		// n号单体最近一次成功的特征测试数据区间2的全部数据的平均值
		List<Double> currents = values.subList(sampleCount1, sampleCount1 + sampleCount2);
		double totalCurrent = 0;
		for (int i = 0; i < currents.size(); i++) {//
			totalCurrent += currents.get(i);
		}
		totalCurrent = totalCurrent / (currents.size());
		BigDecimal decimal = new BigDecimal(maxVoltage - minVoltage);
		decimal = decimal.multiply(BigDecimal.valueOf(1000));// 乘以1000，转化为毫伏，
		LOGGER.debug("计算单体内阻，设备编号:{}，单体:{}，结果编号:{},最大电压:{}，最小电压:{}，平均电流:{}", new Object[] { gprsId,
				pulseDischargeInfo.getPulseCell(), pulseDischargeInfo.getId(), maxVoltage, minVoltage, totalCurrent });
		// 欧姆转毫欧取绝对值
		return decimal.divide(BigDecimal.valueOf(totalCurrent), 4, BigDecimal.ROUND_HALF_UP).abs();
	}


	@Override
	public ModelReport generateModelReport(Integer companyId) {
		Preconditions.checkNotNull(companyId, "公司编号不能为空");
		Company company = companyMapper.selectByPrimaryKey(companyId);
		if (company == null) {
			return null;
		}
		ModelReport modelReport = new ModelReport();
		modelReport.setCompanyName(company.getCompanyName());
		modelReport.setExportDate(MyDateUtils.getDateString(new Date(), "yyyy/MM/dd"));
		List<StationInfo> items = stationInfoMapper.getStationByCompanyId(companyId);
		List<ModelReportItem> reportItems = Lists.newArrayList();
		if (CollectionUtils.isNotEmpty(items)) {
			List<String> gprsIds = items.stream().filter(s -> !s.getGprsId().equals("-1"))
					.map(StationInfo::getGprsId).collect(Collectors.toList());
			List<PulseCalculationSend> sends = pulseCalculationSendMapper.getLatestRecords(gprsIds);
			if (CollectionUtils.isNotEmpty(sends)) {
				Map<String, ModelReportItem> map = sends.stream()
						.collect(Collectors.toMap(PulseCalculationSend::getGprsId, item -> {
							ModelReportItem reportItem = new ModelReportItem();
							reportItem.setGprsId(item.getGprsId());
							reportItem.setStationName(item.getName());
							reportItem.setCalculationTime(
									MyDateUtils.getDateString(item.getSendTime(), "yyyy/MM/dd HH:mm"));
							reportItem.setCapacityStatus(getStatusName(item.getCapacityStatus()));
							reportItem.setResistanceStatus(getStatusName(item.getResistanceStatus()));
							return reportItem;
						}));

				List<Integer> exclusionIds = sends.stream().map(PulseCalculationSend::getId)
						.collect(Collectors.toList());
				exclusionIds.clear();
				exclusionIds.add(-1);
				Map paramMap = Maps.newHashMap();
				paramMap.put("exclusionIds", exclusionIds);
				paramMap.put("gprsIds", gprsIds);
				for (PulseCalculationSend send : pulseCalculationSendMapper.getLatestCapacitySuccessRecords(paramMap)) {
					ModelReportItem reportItem = map.get(send.getGprsId());
					if (reportItem != null) {
						reportItem.setLastCapacitySuccessTime(
								MyDateUtils.getDateString(send.getEndTime(), "yyyy/MM/dd HH:mm"));
					}
				}
				for (PulseCalculationSend send : pulseCalculationSendMapper
						.getLatestResistanceSuccessRecords(paramMap)) {
					ModelReportItem reportItem = map.get(send.getGprsId());
					if (reportItem != null) {
						reportItem.setLastResistanceSuccessTime(
								MyDateUtils.getDateString(send.getEndTime(), "yyyy/MM/dd HH:mm"));
					}
				}
				reportItems.addAll(map.values());
			}
		}
		reportItems.sort(Comparator.comparing(ModelReportItem::getGprsId));
		modelReport.setItems(reportItems);
		return modelReport;
	}

	private String getStatusName(Integer status) {
		String name = "未知";
		if (status == null) {
			return name;
		}
		switch (status) {
		case 1:
			name = "成功";
			break;
		case 2:
			name = "失败";
			break;
		}
		return name;
	}
	
	@Override
	public void calcDurationAndCapByRealRecord(List<PackDataInfo> packDataInfos, DischargeManualRecord dischargeManualRecord) {
		if (CollectionUtils.isEmpty(packDataInfos)) {
			return;
		}
		packDataInfos = packDataInfos.stream().sorted(Comparator.comparing(PackDataInfo::getRcvTime)).collect(Collectors.toList());
		
		String gprsId = packDataInfos.get(0).getGprsId();
		// 得到station信息
		StationInfo stationQuery = new StationInfo();
		stationQuery.setGprsId(gprsId);
		List<StationInfo> stationList = stationInfoMapper.selectListSelective(stationQuery);
		if (CollectionUtils.isEmpty(stationList)) {
			return;
		}
		StationInfo stationInfo = stationList.get(0);
		// 得到 gprs信息
		GprsConfigInfo gprsQuery = new GprsConfigInfo();
		gprsQuery.setGprsId(gprsId);
		List<GprsConfigInfo> gprsList = gprsConfigInfoMapper.selectListSelective(gprsQuery);
		if (CollectionUtils.isEmpty(gprsList)) {
			return;
		}
		GprsConfigInfo gprsConfigInfo = gprsList.get(0);
		//获得所有的单体
		CellInfo query = new CellInfo();
		query.setGprsId(gprsConfigInfo.getGprsId());
		List<CellInfo> cells = cellInfoMapper.selectListSelective(query);
		if (CollectionUtils.isEmpty(cells)) {
			LOGGER.warn("时长预测和容量计算失败，因为cell_info表没有数据,基站编号：{}", gprsConfigInfo.getGprsId());
			return;
		}
		boolean newRecord = false;
		PackDataExpandLatest packDataExpandLatest = packDataExpandLatestMapper.selectByPrimaryKey(gprsId);
		if (packDataExpandLatest == null) {
			newRecord = true;
			packDataExpandLatest = new PackDataExpandLatest();
			packDataExpandLatest.setGprsId(stationInfo.getGprsId());
		}
		if (newRecord) {
			resetProperties(gprsConfigInfo, stationInfo, packDataExpandLatest);
		}

		clacDurationAndCapHandler(packDataInfos, stationInfo, gprsConfigInfo, cells, packDataExpandLatest, dischargeManualRecord);

		// 单体性能计算，无论模型计算成功与否，都要更新性能数据。
		calcualteEntityPerformance(stationInfo, gprsConfigInfo, packDataExpandLatest);

		packDataExpandLatest.setUpdateTime(new Date());
		// 存储结果
		if (newRecord) {
			packDataExpandLatestMapper.insert(packDataExpandLatest);
		} else {
			packDataExpandLatestMapper.updateByPrimaryKeySelective(packDataExpandLatest);
		}
		PackDataExpand packDataExpand = new PackDataExpand();
		BeanUtils.copyProperties(packDataExpandLatest, packDataExpand);
		packDataExpandMapper.insert(packDataExpand);
		// 保存容量计算结果，等到命令发送
		saveModifyCapacitySend(stationInfo.getGprsId(), packDataExpandLatest.getPackCapPred());
		// 电池组实时时长预测
		updateStationRealDuration(stationInfo, packDataExpandLatest.getPackDischargeTimePred(), gprsConfigInfo);
		// 写入电池组放点时长预测到station_duration_history
		saveStationDurationHistory(stationInfo.getGprsId(), packDataExpandLatest.getPackDischargeTimePred());
		
	}

	/**
	 * 计算容量和时长
	 */
	private void clacDurationAndCapHandler(List<PackDataInfo> packDataInfos, StationInfo stationInfo, GprsConfigInfo gprsConfigInfo,
			List<CellInfo> cells, PackDataExpandLatest packDataExpandLatest, DischargeManualRecord dischargeManualRecord) {
		//电池组标称容量
		double stationStandardCap = getStandardCapacityFromPackType(stationInfo.getPackType());
		ModelCalcHelper calcHelper = ModelCalcHelper.newInstance(packDataInfos, stationInfo.getCellCount(), stationStandardCap);
		// 电池组放电时长预测
		int[] startEndPoint = calculateGroupDischargeTimePrediction(packDataInfos, packDataExpandLatest, calcHelper);
		
		// 修改手工放电记录的report_remark
		updateManualRecordReportRemark(packDataExpandLatest.getPackDischargeTimePred(), dischargeManualRecord);
		
		// 容量预测（电池组和单体）、容量排序
		calculatePredictionCapacityAndSort(calcHelper, startEndPoint, packDataExpandLatest);
	}

	private void updateManualRecordReportRemark(BigDecimal packDischargeTimePred,
			DischargeManualRecord dischargeManualRecord) {
		// 比较放电时间，如果误差超过10%，则给提示
		BigDecimal dischargeTime = dischargeManualRecord.getDischargeTime() == null? BigDecimal.ZERO : dischargeManualRecord.getDischargeTime();
		double scale = Math.abs(dischargeTime.doubleValue() - packDischargeTimePred.doubleValue())/packDischargeTimePred.doubleValue();
		String reportRemark = dischargeManualRecord.getReportRemark();
		if (scale > 0.1) {
			reportRemark = reportRemark != null? reportRemark + ",放电时长存在较大误差":"放电时长存在较大误差";
			dischargeManualRecord.setReportRemark(reportRemark);
		}
		reportRemark = reportRemark == null? "" : reportRemark;
		DischargeManualRecord record = new DischargeManualRecord();
		record.setId(dischargeManualRecord.getId());
		record.setReportRemark(reportRemark);
		dischargeManualRecordMapper.updateByPrimaryKeySelective(record);
	}

	/**
	 * 最新算法，计算电池组容量和单体容量
	 * @param calcHelper
	 * @param startEndPoint
	 * @param packDataExpandLatest
	 */
	private void calculatePredictionCapacityAndSort(ModelCalcHelper calcHelper, int[] startEndPoint, PackDataExpandLatest packDataExpandLatest) {
		// 得到容量数组中有25个元素，第一个是电池组的容量，后面是单体的容量（按编号顺序）
		double[] capacity = calcHelper.estimateCellCapacity(startEndPoint[0], startEndPoint[1]);
		
		// 容量预测赋值
		packDataExpandLatest.setPackCapPred((int)(capacity[0] + 0.5));
		
		List<SuggestCellInfo> infos = new ArrayList<>();
		for (int i = 1; i < capacity.length; i++) {
			SuggestCellInfo info = new SuggestCellInfo();
			info.setCellIndex(i);
			info.setCellCap(new BigDecimal(capacity[i]).setScale(3, BigDecimal.ROUND_HALF_UP));
			infos.add(info);
		}
		infos = infos.stream().sorted(Comparator.comparing(SuggestCellInfo::getCellCap).reversed())
				.collect(Collectors.toList());
		
		// 单体容量和排序赋值
		for (int j = 0; j < infos.size(); j++) {
			SuggestCellInfo info = infos.get(j);
			ReflectUtil.setValueByKet(packDataExpandLatest, CELL_CAP_PREFIX + info.getCellIndex(), info.getCellCap());
			ReflectUtil.setValueByKet(packDataExpandLatest, CELL_CAP_SORT_PREFIX + info.getCellIndex(), j + 1);
		}
	}

	/**
	 * 最新算法，计算放电时长
	 * @param packDataInfos
	 * @param packDataExpandLatest
	 * @return 放电开始点和结束点index
	 */
	private int[] calculateGroupDischargeTimePrediction(List<PackDataInfo> packDataInfos,
			PackDataExpandLatest packDataExpandLatest, ModelCalcHelper calcHelper) {
		int[] dischargeStartAndEnd = calcHelper.estimateDischargeStartAndEnd();
		PackDataInfo startPdi = packDataInfos.get(dischargeStartAndEnd[0]);
		PackDataInfo endPdi = packDataInfos.get(dischargeStartAndEnd[1]);
		BigDecimal dischargeTime = new BigDecimal(endPdi.getRcvTime().getTime() - startPdi.getRcvTime().getTime())
				.divide(new BigDecimal(1000*60*60), 3, BigDecimal.ROUND_HALF_UP);
		packDataExpandLatest.setPackDischargeTimePred(dischargeTime);
		return dischargeStartAndEnd;
	}

}
