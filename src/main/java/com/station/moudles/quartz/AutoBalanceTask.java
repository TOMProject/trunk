package com.station.moudles.quartz;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.station.common.utils.MyDateUtils;
import com.station.common.utils.ReflectUtil;
import com.station.moudles.entity.DischargeAbstractRecord;
import com.station.moudles.entity.GprsBalanceSend;
import com.station.moudles.entity.GprsConfigInfo;
import com.station.moudles.entity.ModifyBalanceSend;
import com.station.moudles.entity.PackDataInfo;
import com.station.moudles.entity.PackDataInfoLatest;
import com.station.moudles.entity.RoutingInspectionDetail;
import com.station.moudles.entity.RoutingInspections;
import com.station.moudles.mapper.DischargeAbstractRecordMapper;
import com.station.moudles.mapper.PackDataInfoLatestMapper;
import com.station.moudles.mapper.PackDataInfoMapper;
import com.station.moudles.mapper.RoutingInspectionDetailMapper;
import com.station.moudles.mapper.RoutingInspectionsMapper;
import com.station.moudles.service.GprsBalanceSendService;
import com.station.moudles.service.GprsConfigInfoService;
import com.station.moudles.service.ModifyBalanceInfoService;

public class AutoBalanceTask  {

	private static final Logger logger = LoggerFactory.getLogger(AutoBalanceTask.class);

	// private static final int CHECK_INTERVAL = 10;// 默认检查的时间间隔(分钟)。
	private static final int MIN_DISCHARGE_COUNT = 10;// 检查时间内，最小放电条数。
	private final static String DISCHARGE_NAME = "放电";
	private final static String CELL_PREFIX = "cell";
	private final static String CELL_VOL_PREFIX = "cellVol";
	private final static Double DIFF_VOL = 0.05; // 电压均衡，平均电压  ± 50mv
	private Map<String, AutoBalanceRecord> dischargeRecordMap = new HashMap<>();
	private Map<String, GprsBalanceSend> balanceSendMap = new HashMap<>();

	@Autowired
	GprsConfigInfoService gprsConfigInfoService;
	@Autowired
	PackDataInfoMapper packDataInfoMapper;
	@Autowired
	PackDataInfoLatestMapper packDataInfoLatestMapper;
	@Autowired
	RoutingInspectionsMapper routingInspectionsMapper;
	@Autowired
	RoutingInspectionDetailMapper routingInspectionDetailMapper;
	@Autowired
	GprsBalanceSendService gprsBalanceSendSer;
	@Autowired
	DischargeAbstractRecordMapper dischargeAbstractRecordMapper;
	@Autowired
	ModifyBalanceInfoService modifyBalanceInfoSer;
	
	@Autowired
	@Lazy
	public AutoBalanceTask(
			GprsConfigInfoService gprsConfigInfoService, PackDataInfoMapper packDataInfoMapper,
			PackDataInfoLatestMapper packDataInfoLatestMapper, RoutingInspectionsMapper routingInspectionsMapper,
			RoutingInspectionDetailMapper routingInspectionDetailMapper, GprsBalanceSendService gprsBalanceSendSer,
			DischargeAbstractRecordMapper dischargeAbstractRecordMapper) {
		this.gprsConfigInfoService = gprsConfigInfoService;
		this.packDataInfoMapper = packDataInfoMapper;
		this.packDataInfoLatestMapper = packDataInfoLatestMapper;
		this.routingInspectionsMapper = routingInspectionsMapper;
		this.routingInspectionDetailMapper = routingInspectionDetailMapper;
		this.gprsBalanceSendSer = gprsBalanceSendSer;
		this.dischargeAbstractRecordMapper = dischargeAbstractRecordMapper;
		initConfig();
	}

	private  void initConfig() {
		DischargeAbstractRecord dischargeAbstractRecord = new DischargeAbstractRecord();
		List<DischargeAbstractRecord> discharger = dischargeAbstractRecordMapper
				.selectEndTimeIsNull(dischargeAbstractRecord);
		if (discharger != null && discharger.size() != 0) {
			for (DischargeAbstractRecord dar : discharger) {
				AutoBalanceRecord autoBalanceRecord = new AutoBalanceRecord();
				autoBalanceRecord.setGprsId(dar.getGprsId());
				autoBalanceRecord.setStartRecordID(dar.getRecordStartId());
				autoBalanceRecord.setDischargeStartTime(dar.getStarttime());
				autoBalanceRecord.setDischargeStartVol(dar.getStartVol());
				autoBalanceRecord.setBalanceStartTime(dar.getBalanceStartTime());
				autoBalanceRecord.setBalanceEndTime(dar.getBalanceEndTime());
				autoBalanceRecord.setAutoBalance(dar.getAutoBalance());
				autoBalanceRecord.setNewCellCount(dar.getNewCellCount());
				dischargeRecordMap.put(dar.getGprsId(), autoBalanceRecord);
			}
		}

	}

	public void execute() {

		try {			

 			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			// 查询所有的放电数据
			PackDataInfoLatest queryInfo = new PackDataInfoLatest();
			queryInfo.setState(DISCHARGE_NAME);
			queryInfo.setRcvTime(DateUtils.addHours(new Date(), -2));//2小时内的数据
			List<PackDataInfoLatest> dischargeItems = packDataInfoLatestMapper.selectListSelective(queryInfo);
			Map<String, List<PackDataInfoLatest>> dischargeCellMap = null;
			if (!CollectionUtils.isEmpty(dischargeItems)) {
				dischargeCellMap = dischargeItems.stream()
						.collect(Collectors.groupingBy(PackDataInfoLatest::getGprsId));
				for (String gprsId : dischargeCellMap.keySet()) {
					AutoBalanceRecord record = dischargeRecordMap.get(gprsId);
					if (record != null) {
						//判断endtime或者starteTime是否为null ,若为null做第一次均衡
						if(record.getDischargeStartTime() == null || record.getDischargeEndTime() == null ) {
							firstBalance(gprsId,dischargeCellMap.get(gprsId).get(0), record);// 自动均衡
						// 该电池组还在放电，需要判断是否需要进行均衡
						}else if (record.getAutoBalance() != null && record.getAutoBalance()) {// 再次均衡
							againBalance(gprsId, dischargeCellMap.get(gprsId).get(0));
						}
					} else {// 首次发现放电状态
						firstDischarge(gprsId, dischargeCellMap.get(gprsId).get(0));
					}
				}
			}
			// 处理已经在缓存中，但是不在最新查找放电记录中的gprsid，看是否已经放电结束
			List<String> removeGprsIdList = new ArrayList<>(); // 记录要移除缓存的 gprsId
			for (String gprsId : dischargeRecordMap.keySet()) {
				if (CollectionUtils.isEmpty(dischargeItems) || !dischargeCellMap.containsKey(gprsId)) {// 本轮查询没有放电记录，或者放电记录中不包括这个gprsid。
					// 该电池组，当前不是放电状态；查询是否有10条非放电数据
					List<PackDataInfo> selectListByTime = get10PackDataInfosByTime(gprsId, new Date());
					// 校验是否都为非放电数据
					if (stateVerify(selectListByTime, DISCHARGE_NAME, true)) {
						// 关闭均衡，清除缓存
						if (balanceSendMap.containsKey(gprsId)) {
							GprsBalanceSend send = new GprsBalanceSend();
							for (int i = 1; i < 25; i++) {
								ReflectUtil.setValueByKet(send, CELL_PREFIX + i, (byte) 0);
							}
							send.setDuration(60);// 60分钟
							send.setGprsId(gprsId);
							send.setMode(0);
							gprsBalanceSendSer.send(send);
							balanceSendMap.remove(gprsId);// 移除均衡指令
							removeGprsIdList.add(gprsId);
						} else {
							// 表示从来没发过指令，但是有记录 。eg：k=24 没有满足发送条件，直接放电结束
							removeGprsIdList.add(gprsId);
						}
					} else {
						// 数据乱跳，默认放电，执行再次均衡
						PackDataInfo packDataInfo = selectListByTime.stream()
								.filter(p -> p.getState().equals(DISCHARGE_NAME))
								.max(Comparator.comparing(PackDataInfo::getId)).get();
						PackDataInfoLatest dataInfoLatest = new PackDataInfoLatest();
						BeanUtils.copyProperties(packDataInfo, dataInfoLatest);
						AutoBalanceRecord record = dischargeRecordMap.get(gprsId);
						if (record != null) {
							if(record.getAutoBalance() != null && record.getAutoBalance()) {
							againBalance(gprsId, dataInfoLatest);
							}
						}	
					}
				}
			}
			// 清除缓存，记录放电摘要数据
			for (String gprsId : removeGprsIdList) {
				// 从当前开始往前找，最后一条放电数据，作为放电结束时间，并计算放电结束电压。
				List<PackDataInfo> endDataInfos = forwardLookupDischarge(gprsId, new Date(), 50);
				if(CollectionUtils.isEmpty(endDataInfos)) {
					break;
				}
				// 结束时间
				AutoBalanceRecord dischargeRecord = dischargeRecordMap.get(gprsId);
				dischargeRecord.setDischargeEndTime(endDataInfos.get(0).getRcvTime());
				dischargeRecord.setEndRecoredID(endDataInfos.get(0).getId());
				// 结束电压
				BigDecimal totalGenVol = new BigDecimal(0);
				for (int i = 0; i < 5; i++) {
					totalGenVol = totalGenVol.add(endDataInfos.get(i).getGenVol());
				}
				dischargeRecord.setDischargeEndVol(totalGenVol.divide(new BigDecimal(5), 3 , BigDecimal.ROUND_HALF_UP));
				// 计算总电流
				BigDecimal totalCur = new BigDecimal(0);
				for (int k = 0; k < endDataInfos.size(); k++) {
					totalCur = totalCur.add(endDataInfos.get(k).getGenCur());
				}
				// 写入数据库。
				DischargeAbstractRecord dar = new DischargeAbstractRecord();
				dar.setGprsId(dischargeRecord.getGprsId());
				dar.setRecordStartId(dischargeRecord.getStartRecordID());
				dar.setRecordEndId(dischargeRecord.getEndRecoredID());
				dar.setStarttime(dischargeRecord.getDischargeStartTime());
				dar.setEndtime(dischargeRecord.getDischargeEndTime());
				dar.setStartVol(dischargeRecord.getDischargeStartVol());
				dar.setEndVol(dischargeRecord.getDischargeEndVol());
				// 算平均电流
				dar.setGenCur(totalCur.divide(new BigDecimal(endDataInfos.size()), 3 , BigDecimal.ROUND_HALF_UP));
				// //放电时长
				long time1 = dischargeRecord.getDischargeStartTime().getTime();//开始时间
				long time2 = endDataInfos.get(0).getRcvTime().getTime();//结束时间
				//long dischargeTime = (time2 - time1) / (1000 * 60 * 60);
				BigDecimal dischargeTime = new BigDecimal(time2-time1).divide(new BigDecimal(1000*60*60),3,BigDecimal.ROUND_HALF_UP);
				dar.setDischargeTime(dischargeTime);

				dischargeAbstractRecordMapper.updateByGprsIdSelective(dar);
				// 清除缓存
				dischargeRecordMap.remove(gprsId);
				
				// TODO 硬件BUG，结束时，再发送一个均衡策略默认指令
				GprsConfigInfo record = new GprsConfigInfo();
				record.setGprsId(gprsId);
				List<GprsConfigInfo> gprsConfigInfos = gprsConfigInfoService.selectListSelective(record);
				if (CollectionUtils.isNotEmpty(gprsConfigInfos) && gprsConfigInfos.get(0).getDeviceType() == 1) {
					ModifyBalanceSend send = getDefaultBalanceStrategy(gprsId);
					modifyBalanceInfoSer.insertParaNonNull(send);
				}
			}
			stopWatch.stop();
			logger.info("一轮自动均衡时间：{}" , stopWatch);
		} catch (Exception e) {

		logger.error(this.getClass().getName(), e);

		}
		
	}

	
	private void firstDischarge(String gprsId, PackDataInfoLatest dataInfo) {
		try {
			// 查询 pack_data_info
			List<PackDataInfo> selectListByTime = get10PackDataInfosByTime(dataInfo.getGprsId(), dataInfo.getRcvTime());
			if (CollectionUtils.isEmpty(selectListByTime) || selectListByTime.size() < MIN_DISCHARGE_COUNT) {
				// 查询的数据为空或数量少于10条， 不标记为放电。
				return;
			}
			// 校验 10条数据是否都是 '放电'
			if (stateVerify(selectListByTime, DISCHARGE_NAME, false)) {
				AutoBalanceRecord balanceRecord = new AutoBalanceRecord();
				balanceRecord.setGprsId(gprsId);
				// 设置放电开始时间
				Integer maxId = selectListByTime.stream().max(Comparator.comparing(PackDataInfo::getId)).get().getId();
				List<PackDataInfo> dischargeDatas = forwardLookup(maxId, gprsId, 50, 2);
				dischargeDatas = dischargeDatas.stream().sorted(Comparator.comparing(PackDataInfo::getRcvTime))
						.collect(Collectors.toList());
				PackDataInfo dischargeStart = dischargeDatas.get(0);
				balanceRecord.setDischargeStartTime(dischargeStart.getRcvTime());
				balanceRecord.setStartRecordID(dischargeStart.getId());
				// 开始电压
				BigDecimal totalGenVol = new BigDecimal(0);
				for (int i = 0; i < 5; i++) {
					totalGenVol = totalGenVol.add(dischargeDatas.get(i).getGenVol());
				}
				balanceRecord.setDischargeStartVol(totalGenVol.divide(new BigDecimal(5), 3 , BigDecimal.ROUND_HALF_UP));

				// 检查是否需要放电均衡
				firstBalance(gprsId, dataInfo, balanceRecord);// 自动均衡
				
//				RoutingInspections queryRouting = new RoutingInspections();
//				queryRouting.setGprsId(gprsId);
//				queryRouting.setOperateType(3);// 更换单体
//				queryRouting.setDeviceType(1);// 只查询蓄电池串联复用设备
//				List<RoutingInspections> routings = routingInspectionsMapper.selectListSelective(queryRouting);
//				if (routings != null && routings.size() > 0) {
//					RoutingInspections routing = routings.stream()
//							.max(Comparator.comparing(RoutingInspections::getOperateTime)).get();
//					balanceRecord.setAutoBalance(true);// 需要自动均衡
//					firstBalance(gprsId, dataInfo, balanceRecord, routing);// 自动均衡
//				}else {
//					balanceRecord.setAutoBalance(false);
//				}
				// 加入放电缓存
				dischargeRecordMap.put(gprsId, balanceRecord);
				// 将缓存写入数据库
				DischargeAbstractRecord dar = new DischargeAbstractRecord();
				dar.setGprsId(balanceRecord.getGprsId());
				dar.setRecordStartId(balanceRecord.getStartRecordID());
				dar.setStarttime(balanceRecord.getDischargeStartTime());
				dar.setStartVol(balanceRecord.getDischargeStartVol());
				dar.setBalanceStartTime(balanceRecord.getBalanceStartTime());
				dar.setBalanceEndTime(balanceRecord.getBalanceEndTime());
				dar.setAutoBalance(balanceRecord.getAutoBalance());
				dar.setNewCellCount(balanceRecord.getNewCellCount());
				dischargeAbstractRecordMapper.insertSelective(dar);

			} // 未找到10条连续放电，此轮循环退出。
		} catch (Exception e) {
			logger.error(this.getClass().getName(), e);
		}

	}

	private void firstBalance(String gprsId, PackDataInfoLatest dataInfo, AutoBalanceRecord balanceRecord) {
		RoutingInspections queryRouting = new RoutingInspections();
		queryRouting.setGprsId(gprsId);
		queryRouting.setOperateType(3);// 更换单体
		queryRouting.setDeviceType(1);// 只查询蓄电池串联复用设备
		List<RoutingInspections> routings = routingInspectionsMapper.selectListSelective(queryRouting);
		if (routings != null && routings.size() > 0) {
			RoutingInspections routingInspections = routings.stream().max(Comparator.comparing(RoutingInspections::getOperateTime)).get();
			//balanceRecord.setAutoBalance(true);// 需要自动均衡
			BigDecimal genVol = dataInfo.getGenVol();
			if (genVol.compareTo(new BigDecimal(53)) < 0) { // 判断电压
				balanceRecord.setAutoBalance(true);// 需要自动均衡
				// TODO 硬件BUG，均衡之前必需发送均衡策略指令
				ModifyBalanceSend send = getDefaultBalanceStrategy(gprsId);
				send.setPara1(100);
				send.setPara2(-100);
				modifyBalanceInfoSer.insertParaNonNull(send);
				
				// 判断电压执行 '容量均衡'或'电压均衡'
				if (genVol.compareTo(new BigDecimal(48)) > 0) {
					// 容量均衡 --> 初始指令发送
					Set<Integer> cellIndexs = null; // 新电池的编号
					// 查询 '巡检详情表' 得到新电池的数量和编号
					RoutingInspectionDetail record = new RoutingInspectionDetail();
					record.setRoutingInspectionsId(routingInspections.getRoutingInspectionId());
					record.setDetailOperateType(5);
					record.setDetailOperateValueNew("新电池");
					List<RoutingInspectionDetail> inspectionDetails = routingInspectionDetailMapper
							.selectListSelective(record);
					if (CollectionUtils.isEmpty(inspectionDetails)) {
						balanceRecord.setNewCellCount(0);
					} else {
						balanceRecord.setNewCellCount(inspectionDetails.size());
						cellIndexs = inspectionDetails.stream().map(r -> r.getCellIndex()).collect(Collectors.toSet());
					}
					// 对每个单体设置均衡指令
					if (balanceRecord.getNewCellCount() < 24) {
						if (balanceRecord.getNewCellCount() >= 12) {
							// 新电池个数 >= 12 && < 24
							balanceSendGt12(gprsId, balanceRecord, cellIndexs);
						} else {
							// 新电池个数 < 12
							balanceSendLt12(gprsId, balanceRecord, cellIndexs);
						}
					} else {
						// 24 个都是新电池, 只记录时间，不发指令
						Date startTime = new Date();
						balanceRecord.setBalanceStartTime(startTime);
						balanceRecord.setBalanceEndTime(MyDateUtils.add(startTime, Calendar.MINUTE, 60));
						dischargeRecordMap.put(gprsId, balanceRecord);
					}
				} else {
					// 电压均衡--> 均衡控制
					balanceRecord.setNewCellCount(-1);
					volBalanceCommand(balanceRecord, dataInfo);
				}
			}
		}else {
			balanceRecord.setAutoBalance(false);
		}
	}

	/**
	 * RoutingInspectionPackData 有pack_data_info_latest中的数据 和 巡检记录
	 * 
	 * @param gprsId
	 * @param routingCellMap
	 */
	private void againBalance(String gprsId, PackDataInfoLatest packDataInfo) {
		try {
			AutoBalanceRecord balanceRecord = dischargeRecordMap.get(gprsId);
			// 判断时间
			if (new Date().compareTo(balanceRecord.getBalanceEndTime()) < 0) {
				BigDecimal genVol = packDataInfo.getGenVol();
				if (genVol.compareTo(new BigDecimal(53)) < 0) { // 判断电压
					if (genVol.compareTo(new BigDecimal(48)) > 0) {
						// 容量均衡，均衡控制
						capBalanceCommand(gprsId, balanceRecord, genVol, packDataInfo);
					} else {
						if (balanceRecord.getNewCellCount() >= 0 && balanceRecord.getNewCellCount() < 24) {
							// 电压均衡，由容量均衡进入
							volBalanceFromCap(balanceRecord, packDataInfo);
						} else {
							// 电压均衡，均衡控制
							volBalanceCommand(balanceRecord, packDataInfo);
						}
					}
				}
			}

		} catch (Exception e) {
			logger.error(this.getClass().getName(), e);
		}
		
	}

	private void capBalanceCommand(String gprsId, AutoBalanceRecord balanceRecord, BigDecimal genVol,
			PackDataInfoLatest packDataInfo) {
		if (balanceRecord.getNewCellCount() >= 0 && balanceRecord.getNewCellCount() < 24) {
			GprsBalanceSend gprsBalanceSend = balanceSendMap.get(gprsId);
			if (gprsBalanceSend == null) {
				// 检查是否需要放电均衡
				firstBalance(gprsId, packDataInfo, balanceRecord);// 自动均衡
//				RoutingInspections queryRouting = new RoutingInspections();
//				queryRouting.setGprsId(gprsId);
//				queryRouting.setOperateType(3);// 更换单体
//				List<RoutingInspections> routings = routingInspectionsMapper.selectListSelective(queryRouting);
//				if (routings != null && routings.size() > 0) {
//					RoutingInspections routing = routings.stream()
//							.max(Comparator.comparing(RoutingInspections::getOperateTime)).get();
//					firstBalance(gprsId, packDataInfo, balanceRecord, routing);// 自动均衡
//				}
			} else {
				gprsBalanceSend.setId(null);
				gprsBalanceSend.setDuration(null);
				gprsBalanceSend.setSendDone(null);
				gprsBalanceSend.setSendTime(null);
				if (genVol.compareTo(new BigDecimal(50)) >= 0) {
					gprsBalanceSend.setDuration(60);
				} else if (genVol.compareTo(new BigDecimal(48.5)) >= 0) {
					gprsBalanceSend.setDuration(30);
				} else {
					gprsBalanceSend.setDuration(10);
				}
				gprsBalanceSendSer.send(gprsBalanceSend);

				// 更新缓存
				Date startTime = gprsBalanceSend.getSendTime();
				balanceRecord.setBalanceStartTime(startTime);
				balanceRecord.setBalanceEndTime(MyDateUtils.add(startTime, Calendar.MINUTE, gprsBalanceSend.getDuration()));
			}
		} else if (balanceRecord.getNewCellCount() == 24) {
			int duration = 0;
			if (genVol.compareTo(new BigDecimal(50)) >= 0) {
				duration = 60;
			} else if (genVol.compareTo(new BigDecimal(48.5)) >= 0) {
				duration = 30;
			} else {
				duration = 10;
			}
			// 更新缓存
			Date startTime = new Date();
			balanceRecord.setBalanceStartTime(startTime);
			balanceRecord.setBalanceEndTime(MyDateUtils.add(startTime, Calendar.MINUTE, duration));
		}
	}

	private void balanceSendGt12(String gprsId, AutoBalanceRecord balanceRecord, Set<Integer> cellIndexs) {
		// 1、2#单体设为降压，自3#单体往后按顺序将新电池设为升压，升压个数达到12后，其余设为降压
		// 0: 关闭 2:升压 3:降压
		GprsBalanceSend send = new GprsBalanceSend();
		int volUpCount = 0;
		for (int i = 1; i < 25; i++) {
			if (i <= 2) {
				ReflectUtil.setValueByKet(send, CELL_PREFIX + i, (byte) 3);
			} else {
				if (cellIndexs.contains(i)) {
					if (volUpCount >= 12) {
						ReflectUtil.setValueByKet(send, CELL_PREFIX + i, (byte) 3);
					} else {
						ReflectUtil.setValueByKet(send, CELL_PREFIX + i, (byte) 2);
					}
					volUpCount++;
				} else {
					ReflectUtil.setValueByKet(send, CELL_PREFIX + i, (byte) 3);
				}
			}
		}
		send.setDuration(60); // 60分钟
		send.setGprsId(gprsId);
		send.setMode(0);
		gprsBalanceSendSer.send(send);
		// 缓存 均衡指令
		balanceSendMap.put(gprsId, send);
		// 记录 均衡开始时间、结束时间
		Date startTime = new Date();
		balanceRecord.setBalanceStartTime(startTime);
		balanceRecord.setBalanceEndTime(MyDateUtils.add(startTime, Calendar.MINUTE, 60));
		dischargeRecordMap.put(gprsId, balanceRecord); // 放入缓存
	}

	private void balanceSendLt12(String gprsId, AutoBalanceRecord balanceRecord, Set<Integer> cellIndexs) {
		// 1、2#单体设为降压，自3#单体往后将新电池设为升压，其余设为降压，
		// 如1、2#单体中有新电池,则5分钟后将其设为升压，其余保持原配置，再发一次指令。
		// 0: 关闭 2:升压 3:降压
		GprsBalanceSend send = new GprsBalanceSend();
		for (int i = 1; i < 25; i++) {
			if (i <= 2) {
				ReflectUtil.setValueByKet(send, CELL_PREFIX + i, (byte) 3);
			} else {
				if (cellIndexs.contains(i)) {
					ReflectUtil.setValueByKet(send, CELL_PREFIX + i, (byte) 2);
				} else {
					ReflectUtil.setValueByKet(send, CELL_PREFIX + i, (byte) 3);
				}
			}
		}
		send.setDuration(30); // 30分钟
		send.setGprsId(gprsId);
		send.setMode(0);
		gprsBalanceSendSer.send(send);
		// 缓存 均衡指令
		balanceSendMap.put(gprsId, send);
		// 记录 均衡开始时间、结束时间
		Date startTime = send.getSendTime();
		balanceRecord.setBalanceStartTime(startTime);
		balanceRecord.setBalanceEndTime(MyDateUtils.add(startTime, Calendar.MINUTE, 30));
		dischargeRecordMap.put(gprsId, balanceRecord); // 放入缓存

		if (cellIndexs.contains(1) || cellIndexs.contains(2)) {
			// 如1、2#单体中有新电池 ,5 分钟后，将其设为升压，其余不变，再发一次指令
			Timer timerTask = new Timer();
			timerTask.schedule(new TimerTask() {
				@Override
				public void run() {
					GprsBalanceSend twiceSend = new GprsBalanceSend();
					for (int i = 1; i < 25; i++) {
						if (cellIndexs.contains(i)) {
							ReflectUtil.setValueByKet(twiceSend, CELL_PREFIX + i, (byte) 2);
						} else {
							ReflectUtil.setValueByKet(twiceSend, CELL_PREFIX + i, (byte) 3);
						}
					}
					twiceSend.setDuration(30); // 30分钟
					twiceSend.setGprsId(gprsId);
					twiceSend.setMode(0);
					// 再次发送
					gprsBalanceSendSer.send(twiceSend);
					// 均衡指令 缓存修改
					balanceSendMap.put(gprsId, twiceSend);
					// 记录 均衡开始时间、结束时间、放电开始时间
					Date twiceStartTime = twiceSend.getSendTime();
					balanceRecord.setBalanceStartTime(twiceStartTime);
					balanceRecord.setBalanceEndTime(MyDateUtils.add(twiceStartTime, Calendar.MINUTE, 30));
					dischargeRecordMap.put(gprsId, balanceRecord); // 缓存修改
				}
			}, 300000);
		}
	}

	private void volBalanceFromCap(AutoBalanceRecord balanceRecord, PackDataInfoLatest packDataInfo) {
		int volUpCount = 0;
		int volDownCount = 0;
		List<CellInfoHelper> volUpList = new ArrayList<>();
		BigDecimal averageVol = getAverageVol(packDataInfo);
		GprsBalanceSend gprsBalanceSend = balanceSendMap.get(balanceRecord.getGprsId());

		if (gprsBalanceSend == null) {
			// 检查是否需要放电均衡
			String gprsId = balanceRecord.getGprsId();
			firstBalance(gprsId, packDataInfo, balanceRecord);// 自动均衡
//			RoutingInspections queryRouting = new RoutingInspections();
//			queryRouting.setGprsId(gprsId);
//			queryRouting.setOperateType(3);// 更换单体
//			List<RoutingInspections> routings = routingInspectionsMapper.selectListSelective(queryRouting);
//			if (routings != null && routings.size() > 0) {
//				RoutingInspections routing = routings.stream()
//						.max(Comparator.comparing(RoutingInspections::getOperateTime)).get();
//				firstBalance(gprsId, packDataInfo, balanceRecord, routing);// 自动均衡
//			}
		} else {
			gprsBalanceSend.setId(null);
			gprsBalanceSend.setDuration(null);
			gprsBalanceSend.setSendDone(null);
			gprsBalanceSend.setSendTime(null);
			// 0: 关闭 2:升压 3:降压
			for (int i = 1; i < 25; i++) {
				Object obj = ReflectUtil.getValueByKey(gprsBalanceSend, CELL_PREFIX + i);
				int cell = Integer.valueOf(obj.toString());
				Object objVol = ReflectUtil.getValueByKey(packDataInfo, CELL_VOL_PREFIX + i);
				BigDecimal vol = new BigDecimal(objVol == null ? "0" : objVol.toString());
				if (cell == 2) {
					// 升压变关闭
					if (vol.compareTo(averageVol) <= 0) {
						ReflectUtil.setValueByKet(gprsBalanceSend, CELL_PREFIX + i, (byte) 0);
					} else {
						CellInfoHelper helper = new CellInfoHelper();
						helper.setCellIndex(i);
						helper.setCellVol(vol);
						volUpList.add(helper);
						volUpCount++;
					}
				} else if (cell == 3) {
					volDownCount++;
					// 降压改为升压， VOL_N >AVERAGE(VOL) + 50MV
					if (vol.compareTo(averageVol.add(new BigDecimal(DIFF_VOL))) > 0) {
						ReflectUtil.setValueByKet(gprsBalanceSend, CELL_PREFIX + i, (byte) 2);
						volDownCount--;
						volUpCount++;
						CellInfoHelper helper = new CellInfoHelper();
						helper.setCellIndex(i);
						helper.setCellVol(vol);
						volUpList.add(helper);
					}
				}
			}

			// 如升压个数大于降压个数，则将离平均电压最小升压的依次变为关闭,直到升压个数等于降压个数
			if (volUpCount > volDownCount) {
				int count = volUpCount - volDownCount;
				volUpList.sort(Comparator.comparing(CellInfoHelper::getCellVol));
				for (int i = 0; i < count; i++) {
					Integer index = volUpList.get(i).getCellIndex();
					ReflectUtil.setValueByKet(gprsBalanceSend, CELL_PREFIX + index, (byte) 0);
				}
			}
			gprsBalanceSend.setDuration(30);
			gprsBalanceSendSer.send(gprsBalanceSend);

			// 更新缓存
			Date startTime = gprsBalanceSend.getSendTime();
			balanceRecord.setBalanceStartTime(startTime);
			balanceRecord.setBalanceEndTime(MyDateUtils.add(startTime, Calendar.MINUTE, 30));
		}
	}

	private void volBalanceCommand(AutoBalanceRecord balanceRecord, PackDataInfoLatest packDataInfo) {
		int volUpCount = 0; // 记录升压
		int volDownCount = 0;// 记录降压
		List<CellInfoHelper> volUpList = new ArrayList<>();// 记录升压
		List<CellInfoHelper> volCloseList = new ArrayList<>();// 记录关闭
		List<CellInfoHelper> volDownList = new ArrayList<>();// 记录降压
		BigDecimal averageVol = getAverageVol(packDataInfo);
		GprsBalanceSend send = new GprsBalanceSend();
		for (int i = 1; i < 25; i++) {
			// 0: 关闭 2:升压 3:降压
			/*
			 * avg + 50< vol 升； avg - 50 <= vol <= avg + 50 关； vol < avg - 50 降
			 */
			Object obj = ReflectUtil.getValueByKey(packDataInfo, CELL_VOL_PREFIX + i);
			BigDecimal vol = new BigDecimal(obj == null ? "0" : obj.toString());
			if (vol.compareTo(averageVol.add(new BigDecimal(DIFF_VOL))) > 0) {
				ReflectUtil.setValueByKet(send, CELL_PREFIX + i, (byte) 2);
				volUpCount++;
				CellInfoHelper helper = new CellInfoHelper();
				helper.setCellIndex(i);
				helper.setCellVol(vol);
				volUpList.add(helper);
			} else if (vol.compareTo(averageVol.add(new BigDecimal(-DIFF_VOL))) >= 0) {
				ReflectUtil.setValueByKet(send, CELL_PREFIX + i, (byte) 0);
				CellInfoHelper helper = new CellInfoHelper();
				helper.setCellIndex(i);
				helper.setCellVol(vol);
				volCloseList.add(helper);
			} else {
				ReflectUtil.setValueByKet(send, CELL_PREFIX + i, (byte) 3);
				volDownCount++;
				CellInfoHelper helper = new CellInfoHelper();
				helper.setCellIndex(i);
				helper.setCellVol(vol);
				volDownList.add(helper);
			}
		}
		// 如升压个数大于降压个数，则将关闭中电压小的的依次变为降压,直到升压个数等于降压个数,还不相等则把升压变为关闭
		if (volUpCount > volDownCount) {
			int count = volUpCount - volDownCount;
			volCloseList.sort(Comparator.comparing(CellInfoHelper::getCellVol));
			if (count > volCloseList.size()) {
				for (int i = 0; i < volCloseList.size(); i++) {
					Integer index = volCloseList.get(i).getCellIndex();
					ReflectUtil.setValueByKet(send, CELL_PREFIX + index, (byte) 3);
				}
				// 多出的个数,把升压变为关闭
				count = count - volCloseList.size();
				volUpList.sort(Comparator.comparing(CellInfoHelper::getCellVol));
				for (int i = 0; i < count; i++) {
					Integer index = volUpList.get(i).getCellIndex();
					ReflectUtil.setValueByKet(send, CELL_PREFIX + index, (byte) 0);
				}
			} else {
				for (int i = 0; i < count; i++) {
					Integer index = volCloseList.get(i).getCellIndex();
					ReflectUtil.setValueByKet(send, CELL_PREFIX + index, (byte) 3);
				}
			}
		} else {
			// 升压个数小于降压个数
			int count = volDownCount -volUpCount;
			volCloseList.sort(Comparator.comparing(CellInfoHelper::getCellVol).reversed());
			if (count > volCloseList.size()) {
				for (int i = 0; i < volCloseList.size(); i++) {
					Integer index = volCloseList.get(i).getCellIndex();
					ReflectUtil.setValueByKet(send, CELL_PREFIX + index, (byte) 2);
				}
				// 多出的个数,把降压变为关闭
				count = count - volCloseList.size();
				volDownList.sort(Comparator.comparing(CellInfoHelper::getCellVol).reversed());
				for (int i = 0; i < count; i++) {
					Integer index = volDownList.get(i).getCellIndex();
					ReflectUtil.setValueByKet(send, CELL_PREFIX + index, (byte) 0);
				}
			}else {
				for (int i = 0; i < count; i++) {
					Integer index = volCloseList.get(i).getCellIndex();
					ReflectUtil.setValueByKet(send, CELL_PREFIX + index, (byte) 2);
				}
			}
		}
		send.setGprsId(packDataInfo.getGprsId());
		send.setDuration(10); // 10分钟
		send.setMode(0);
		gprsBalanceSendSer.send(send);
		balanceSendMap.put(packDataInfo.getGprsId(), send);
		Date startTime = send.getSendTime();
		balanceRecord.setBalanceStartTime(startTime);
		balanceRecord.setBalanceEndTime(MyDateUtils.add(startTime, Calendar.MINUTE, 10));
		dischargeRecordMap.put(packDataInfo.getGprsId(), balanceRecord);
	}

	private BigDecimal getAverageVol(PackDataInfoLatest packDataInfo) {
		BigDecimal avg = BigDecimal.ZERO;
		for (int i = 1; i < 25; i++) {
			Object obj = ReflectUtil.getValueByKey(packDataInfo, CELL_VOL_PREFIX + i);
			BigDecimal vol = new BigDecimal(obj == null ? "0" : obj.toString());
			avg = avg.add(vol);
		}
		avg = avg.divide(BigDecimal.valueOf(24), 2, BigDecimal.ROUND_HALF_UP);
		return avg;
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
	private boolean stateVerify(List<PackDataInfo> list, String state, boolean isContrary) {
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
	 * 得到指定时间之前的10条记录
	 * 
	 * @param bean
	 * @return
	 */
	private List<PackDataInfo> get10PackDataInfosByTime(String gprsId, Date endTime) {
		Map<String, Object> param = new HashMap<>();
		param.put("gprsId", gprsId);
		param.put("endTime", endTime);
		param.put("pageNum", 0);
		param.put("pageSize", 10);
		return packDataInfoMapper.getPackDataInfosByTimes(param);
	}

	// 往前找放电数据，作为放电结束的数据。
	private List<PackDataInfo> forwardLookupDischarge(String gprsId, Date endTime, int pageSize) {
		List<PackDataInfo> results = Lists.newArrayList();
		int pageNum = 0;
		int count = 0;
		while (true) {
			Map<String, Object> paramMap = Maps.newHashMap();
			paramMap.put("gprsId", gprsId);
			paramMap.put("endTime", endTime);
			paramMap.put("pageNum", pageNum);
			paramMap.put("pageSize", pageSize);
			List<PackDataInfo> items = packDataInfoMapper.getPackDataInfosByTimes(paramMap);
			if (CollectionUtils.isEmpty(items)) {
				break;
			}
			for (PackDataInfo item : items) {
				if (DISCHARGE_NAME.equals(item.getState())) {
					results.add(item);
					count++;
				}
				if (count >= 10) {
					break;
				}
			}
			if (count >= 10) {
				break;
			}
			if (items.size() < pageSize) {
				break;
			}
			endTime = items.get(items.size() - 1).getRcvTime();
			pageNum += pageSize;
		}
		return results;
	}

	/**
	 * 
	 * @param startId		
	 * @param gprsId
	 * @param pageSize		一次查询的条数
	 * @param forwardCount  向前查找非放电的记录条数
	 * @return
	 */
	private List<PackDataInfo> forwardLookup(Integer startId, String gprsId, int pageSize, int forwardCount) {
		List<PackDataInfo> results = Lists.newArrayList();
		// 向前找非放电态的数据
		int pageNum = 0;
		int count = 0;
		while (true) {
			Map<String, Object> paramMap = Maps.newHashMap();
			paramMap.put("gprsId", gprsId);
			paramMap.put("id", startId);
			paramMap.put("pageNum", pageNum);
			paramMap.put("pageSize", pageSize);
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
				if (count >= forwardCount) {
					break;
				}
			}
			if (count >= forwardCount) {
				break;
			}
			if (items.size() < pageSize) {
				break;
			}
			startId = items.get(items.size() - 1).getId();
			pageNum += pageSize;
		}
		results = results.stream().filter(k -> DISCHARGE_NAME.equals(k.getState())).collect(Collectors.toList());
		return results;
	}

	private List<PackDataInfo> backwardLookup(Integer startId, String gprsId, int pageSize) {
		List<PackDataInfo> results = Lists.newArrayList();
		// 向后找连续非放电态的数据
		int pageNum = 0;
		int count = 0;
		while (true) {
			Map<String, Object> paramMap = Maps.newHashMap();
			paramMap.put("gprsId", gprsId);
			paramMap.put("id", startId);
			paramMap.put("pageNum", pageNum);
			paramMap.put("pageSize", pageSize);
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
				if (count >= 2) {
					break;
				}
			}
			if (count >= 2) {
				break;
			}
			if (items.size() < pageSize) {
				break;
			}
			startId = items.get(items.size() - 1).getId();
			pageNum += pageSize;
		}
		// results = results.stream().filter(k ->
		// DISCHARGE_NAME.equals(k.getState())).collect(Collectors.toList());
		return results;
	}

	private ModifyBalanceSend getDefaultBalanceStrategy(String gprsId) {
		ModifyBalanceSend send = new ModifyBalanceSend();
		send.setGprsId(gprsId);
		send.setSendDone(0);// 未发送
		int [] para = {20, 5, 57, 50, 5, -5, 2350};
		for (int i = 1; i <= 7; i++) {
			ReflectUtil.setValueByKet(send, "para" + i, para[i-1]);
		}
		return send;
	}
	
	class CellInfoHelper {
		private Integer cellIndex;
		private BigDecimal cellVol;

		public Integer getCellIndex() {
			return cellIndex;
		}

		public void setCellIndex(Integer cellIndex) {
			this.cellIndex = cellIndex;
		}

		public BigDecimal getCellVol() {
			return cellVol;
		}

		public void setCellVol(BigDecimal cellVol) {
			this.cellVol = cellVol;
		}
	}
	 
}
