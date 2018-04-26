package com.station.moudles.quartz;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.station.common.utils.MyDateUtils;
import com.station.moudles.entity.DischargeAbstractRecord;
import com.station.moudles.entity.PackDataInfo;
import com.station.moudles.entity.StationInfo;
import com.station.moudles.helper.DischargeEvent;
import com.station.moudles.helper.EventParams;
import com.station.moudles.mapper.DischargeAbstractRecordMapper;
import com.station.moudles.mapper.StationInfoMapper;
import com.station.moudles.service.ReportService;
import com.station.moudles.vo.report.ChargeDischargeEvent;

@Component
public class HistoryDischargeRecordTask {
	private static final Logger logger = LoggerFactory.getLogger(HistoryDischargeRecordTask.class);
	private final static String DISCHARGE_NAME = "放电";

	StationInfoMapper stationInfoMapper;
	ReportService reportService;
	DischargeAbstractRecordMapper dischargeAbstractRecordMapper;

	@Autowired
	@Lazy
	public HistoryDischargeRecordTask(StationInfoMapper stationInfoMapper, ReportService reportService,
			DischargeAbstractRecordMapper dischargeAbstractRecordMapper) {
		this.stationInfoMapper = stationInfoMapper;
		this.reportService = reportService;
		this.dischargeAbstractRecordMapper = dischargeAbstractRecordMapper;
//		Executors.newScheduledThreadPool(1).execute(() -> execute()); //只运行一次
	}

	public void execute() {
		logger.info("查找放电记录开始");
		// 得到所有的电池组
		StationInfo record = new StationInfo();
		record.setGprsIdNotNull(true);
		List<StationInfo> stationInfos = stationInfoMapper.selectListSelective(record);
		if (CollectionUtils.isNotEmpty(stationInfos)) {
			List<String> gprsIdList = stationInfos.stream().filter(s -> !s.getGprsId().equals("-1"))
					.map(s -> s.getGprsId()).collect(Collectors.toList());

			for (String gprsId : gprsIdList) {
				try {
					EventParams params = new EventParams();
					params.forwardCount = 2;
					params.backwardCount = 2;
					params.currentCount = 10;
					Date startDate = MyDateUtils.parseDate("2018-02-25 22:43:00");
					Date endDate = MyDateUtils.parseDate("2018-03-07 23:59:59");
					List<ChargeDischargeEvent> chargeDischargeEvent = reportService.getChargeDischargeEvent(gprsId,
							startDate, endDate, new DischargeEvent(false), params);
					// 记录是按照时间降序的
					if (CollectionUtils.isNotEmpty(chargeDischargeEvent)) {
						for (ChargeDischargeEvent discharge : chargeDischargeEvent) {
							List<PackDataInfo> details = discharge.getDetails();
							// 截取，放电
							details = details.subList(params.backwardCount, details.size() - params.forwardCount);
							BigDecimal endVol = BigDecimal.ZERO;
							for (int i = 0; i < 5; i++) {
								endVol = endVol.add(details.get(i).getGenVol());
							}
							endVol = endVol.divide(new BigDecimal(5), 3, BigDecimal.ROUND_HALF_UP);

							BigDecimal startVol = BigDecimal.ZERO;
							for (int i = details.size() - 1; i > details.size() - 6; i--) {
								startVol = startVol.add(details.get(i).getGenVol());
							}
							startVol = startVol.divide(new BigDecimal(5), 3, BigDecimal.ROUND_HALF_UP);

							DischargeAbstractRecord dar = new DischargeAbstractRecord();
							dar.setGprsId(details.get(0).getGprsId());
							// 开始电压、结束电压
							dar.setStartVol(startVol);
							dar.setEndVol(endVol);
							// 电流
							List<PackDataInfo> forwardLookupDischarge = forwardLookupDischarge(details);
							BigDecimal genCur = BigDecimal.ZERO;
							for (PackDataInfo packDataInfo : forwardLookupDischarge) {
								genCur = genCur.add(packDataInfo.getGenCur());
							}
							genCur = genCur.divide(new BigDecimal(forwardLookupDischarge.size()), 3,
									BigDecimal.ROUND_HALF_UP);
							dar.setGenCur(genCur);
							// 开始时间、结束时间、放电时长
							Date startTime = details.get(details.size() - 1).getRcvTime();
							Date endTime = details.get(0).getRcvTime();
							BigDecimal dischargeTime = new BigDecimal(
									(endTime.getTime() - startTime.getTime()) / (1000 * 60 * 60.0));
							dar.setStarttime(startTime);
							dar.setEndtime(endTime);
							dar.setDischargeTime(dischargeTime);
							// 开始id、结束id
							dar.setRecordStartId(details.get(details.size() - 1).getId());
							dar.setRecordEndId(details.get(0).getId());
							// 非均衡
							dar.setAutoBalance(false);
							dischargeAbstractRecordMapper.insertSelective(dar);
							logger.info("放电记录插入成功-->" + gprsId);
						}
					}
				} catch (Exception e) {
					logger.error(gprsId ,e);
				}
			}

		}
		logger.info("查找放电记录结束");
	}

	/**
	 * 
	 * @param dischargeList
	 *            时间降序
	 * @return
	 */
	private List<PackDataInfo> forwardLookupDischarge(List<PackDataInfo> dischargeList) {
		List<PackDataInfo> results = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(dischargeList)) {
			int count = 0;
			for (PackDataInfo item : dischargeList) {
				if (DISCHARGE_NAME.equals(item.getState())) {
					results.add(item);
					count++;
				}
				if (count >= 10) {
					break;
				}
			}
		}
		return results;
	}
}
