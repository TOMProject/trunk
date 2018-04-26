package com.station.moudles.quartz;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.station.common.redis.RedisCacheAgent;
import com.station.common.utils.MyDateUtils;
import com.station.moudles.entity.Parameter;
import com.station.moudles.entity.PulseCalculationSend;
import com.station.moudles.entity.StationInfo;
import com.station.moudles.mapper.GprsConfigInfoMapper;
import com.station.moudles.mapper.PulseDischargeSendMapper;
import com.station.moudles.service.CellInfoService;
import com.station.moudles.service.ModelCalculationService;
import com.station.moudles.service.ParameterService;
import com.station.moudles.service.PulseCalculationSendService;
import com.station.moudles.service.PulseDischargeSendService;
import com.station.moudles.service.StationInfoService;
import com.station.moudles.service.impl.PulseServiceImpl;
import com.station.moudles.vo.PulseVo;
import com.station.moudles.vo.ResponseStatus;


/**
 * 自动发送特征测试
 * @author 
 *
 */
public class AutoPulseDischageSendTask {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(PulseServiceImpl.class);
    private static Integer batteryGroupNum  = 150;
    private static String KEY = "srv_autoPulseSend_ids";
  
    @Autowired
	StationInfoService stationInfoSer;
	@Autowired
	PulseDischargeSendMapper pulseDischargeSendMapper;
	@Autowired
	CellInfoService cellInfoService;
	@Autowired
	PulseDischargeSendService PulseDischargeSendSer;
	@Autowired
	GprsConfigInfoMapper gprsConfigInfoMapper ;
	@Autowired
	PulseServiceImpl pulseServiceImpl;
	@Autowired
	ModelCalculationService modelCalculationService;
	@Autowired
	PulseCalculationSendService pulseCalculationSendSer;
	@Autowired
	ParameterService parameterSer;
	@Autowired
	RedisCacheAgent redisCacheAgent;
	@Autowired
	ThreadPoolTaskExecutor taskExecutor;
	
	
	//晚上22::30自动特征测试
	public void autoPulseDischageSend() {
	   StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String parameterCode = "battery_group_num";
        String parameterCategory = "1";
        Parameter parameter = parameterSer.selectByPrimaryKeys(parameterCode, parameterCategory);
       
        if(parameter != null) {
        	batteryGroupNum = Integer.parseInt(parameter.getParameterValue());
		}
		
        //或取当前是周几
		Date date = new Date();
		Integer week = getWeekOfDate(date);//当天
		Integer start = 0;
		Integer maxId = null;
		if(week == 1) {
			maxId = 0;
			redisCacheAgent.clear(KEY);
		}
		//如果存在缓存获取昨天的最大id、
		  Object idsCache = redisCacheAgent.getCache(KEY);
		 if(idsCache != null) {
			 List<Integer> idList  = (List<Integer>) idsCache;
			 if(idList.get(0) == -1) {
				 LOGGER.info(new Date()+"本周特征测试的电池已经检索完毕！");
				 return ;
			 }
			maxId = idList.stream().max(new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					return o1.compareTo(o2);
				}
			}).get(); 
		 }else {
			 maxId = 0;
			LOGGER.info(new Date()+"自动特征测试的电池组缓存中没有数据！");
			
		 }
		//通过周判断要做特征测试的电池组序号
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("start",start);
		map.put("count",batteryGroupNum);
		map.put("maxId",maxId);
		List<StationInfo> stationInfos = stationInfoSer.selectBatteryGroupEveryDay(map);
		PulseVo pulseVo = new PulseVo();
		pulseVo.setDischargeTime(0);//区间2时间， 0：1s; 1: 2s; 2: 3s; 3: 4s
		pulseVo.setFastSampleInterval(0);//"快速采样间隔（区间1和3）， 0：5ms; 1: 10ms; 2: 20ms; 3: 40ms"
		pulseVo.setSlowSampleInterval(0);//"慢速采样间隔（区间1和3）， 0：1s; 1: 5s; 2: 10s; 3: 20s"
		pulseVo.setSlowSampleTime(60);//区间5的持续时间填写
		//装载gprsid--cells
		Map<String,List<Integer>> mapCells = new HashMap<String,List<Integer>>();
		//装载cells
		List<Integer> cells = new ArrayList<Integer>();
		//转载当前id
		List<Integer> ids = new ArrayList<Integer>();
		if(CollectionUtils.isNotEmpty(stationInfos)) {
			for (StationInfo stationInfo : stationInfos) {
				for(int i = 1 ;i <= stationInfo.getCellCount();i++ ) {
					cells.add(i);
				}
				ids.add(stationInfo.getId());
				mapCells.put(stationInfo.getGprsId(), cells);				
				pulseServiceImpl.sendCommand(stationInfo.getGprsId(), mapCells.get(stationInfo.getGprsId()), pulseVo);
				cells.clear();
			} 
			//将当前特征测试的电池的id保存在redis中的；
			redisCacheAgent.refreshCache(KEY, ids);
			ids.clear();
		}else {//没有电池组将ids 设置为0
			ids.add(-1);
			redisCacheAgent.refreshCache(KEY, ids);
		}
		stopWatch.stop();
		LOGGER.info(new Date()+" 自动特征测试完成耗时:{}", stopWatch);
		
	}

	/**
	 * 获取当前日期是星期几，当传递的参数为null时获得当前时间的星期几
	 * @param date
	 * @return
	 */
	public static Integer getWeekOfDate(Date date) {      

	    Integer[] weekOfDays = {7, 1, 2, 3, 4, 5, 6};        
	    Calendar calendar = Calendar.getInstance();      
	    if(date != null){        
	         calendar.setTime(date);      
	    }        
	    int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;      
	    if (w < 0){        
	        w = 0;      
	    }      
	    return weekOfDays[w];    
	}

	
	
	//零晨5:00对昨天22:30自动特征测试的电池组设备进行检索
	public void searchBatteryGroup() {
	   StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        //从缓存读取昨天的自动特征测试的ids
        Object idsCache = redisCacheAgent.getCache(KEY);
        if(idsCache == null) {
        	LOGGER.info(new Date()+"自动特征测试的电池组设备进行检索没有电池组！");
        	return;
        }
        List<Integer> ids = (List<Integer>) idsCache;
		 if(ids.get(0) == -1) {
			 LOGGER.info(new Date()+"本周特征测试的电池已经检索完毕！");
			 return ;
		 }
		 List<StationInfo> stations = stationInfoSer.selectBatteryGroupWithYesterdayForIds(ids);
		if(CollectionUtils.isNotEmpty(stations)) {
			for (StationInfo stationInfo : stations) {
				Map<String,Object> updateSendDone = new HashMap<String,Object>();
				updateSendDone.put("gprsId", stationInfo.getGprsId());
				updateSendDone.put("sendDone3", 3);
				updateSendDone.put("sendDone6", 6);
				pulseDischargeSendMapper.updatebyGprsIdAndpulseSendDone(updateSendDone);
			}
		}
		stopWatch.stop();
		LOGGER.info(new Date()+"自动特征测试的电池组设备进行检索:{}", stopWatch);
	}
	
	//早上7:00对昨天22:30自动特征测试的电池组设备进行模型计算
	public void batteryGroupModelCalculate() {
        //从缓存读取昨天的自动特征测试的ids
        Object idsCache = redisCacheAgent.getCache(KEY);
        if(idsCache == null) {
        	LOGGER.info(new Date()+"自动特征测试的电池组设备进行检索没有电池组！");
        	return;
        }
        List<Integer> ids = (List<Integer>) idsCache;
		 if(ids.get(0) == -1) {
			 LOGGER.info(new Date()+"本周特征测试的电池已经检索完毕！");
			 return ;
		 }
		 List<StationInfo> stations = stationInfoSer.selectBatteryGroupWithYesterdayForIds(ids);
		taskExecutor.execute(() -> {
			if(CollectionUtils.isNotEmpty(stations)) {
	            for (StationInfo stationInfo : stations) {
	            	calculateHandler(stationInfo);
	            }
	            stations.clear();
        	}
        });
	}
	
	/**
	 * 模型计算处理器
	 * @param stationInfo
	 * @return
	 */
	 public List<ResponseStatus> calculateHandler(StationInfo stationInfo) {
	        StopWatch stopWatch = new StopWatch();
	        stopWatch.start();
	        List<ResponseStatus> statuses = null;
	        try {
	    		
	            PulseCalculationSend pulseCalculationSend = new PulseCalculationSend();
	            pulseCalculationSend.setSendTime(new Date());
	            pulseCalculationSend.setGprsId(stationInfo.getGprsId());
	            statuses = modelCalculationService.calculate(stationInfo.getId());
	            StringBuilder sb = new StringBuilder();
	            boolean resistanceStatus = false;
	            boolean capacityStatus = false;
	            for (ResponseStatus status : statuses) {
	                sb.append(status.getMessage()).append(", ");
	                if (StringUtils.contains(status.getMessage(), "内阻")) {
	                    resistanceStatus = Integer.valueOf(1).equals(status.getStatus());
	                    pulseCalculationSend.setResistanceStatus(status.getStatus());
	                    pulseCalculationSend.setResistanceStatusMessage(StringUtils.isBlank(status.getExtMessage())
	                            ? status.getMessage() : status.getExtMessage());
	                } else if (StringUtils.contains(status.getMessage(), "容量")) {
	                    capacityStatus = Integer.valueOf(1).equals(status.getStatus());
	                    pulseCalculationSend.setCapacityStatus(status.getStatus());
	                    pulseCalculationSend.setCapacityStatusMessage(StringUtils.isBlank(status.getExtMessage()) ? status
	                            .getMessage() : status.getExtMessage());
	                }
	            }
	            // 0:内阻容量都成功, 1:内阻成功容量失败, 2:内阻失败容量成功, 3:内阻容量都失败
	            Integer sendDone = 3;
	            if (resistanceStatus && capacityStatus) {
	                sendDone = 0;
	            } else if (resistanceStatus && !capacityStatus) {
	                sendDone = 1;
	            } else if (!resistanceStatus && capacityStatus) {
	                sendDone = 2;
	            }
	            pulseCalculationSend.setSendDone(sendDone);
	            sb.setLength(sb.length() - 2);
	            pulseCalculationSend.setSendDoneMessage(sb.toString());
	            pulseCalculationSend.setEndTime(new Date());
	            pulseCalculationSendSer.insert(pulseCalculationSend);
	        } catch (Exception e) {
	        	LOGGER.error("模型计算失败--->基站编号:{}", stationInfo.getId(), e);
	        } finally {
	            stopWatch.stop();
	            LOGGER.info("模型计算耗时:{},基站编号:{}", stopWatch, stationInfo.getId());
	        }
	        return statuses;
	    }
	
	
    public static void main(String[] args) {
    	String da= "2018-04-22";
    	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");  
    	
    	try {
    		//Date date = sf.parse(da);
    		Date date =MyDateUtils.getDiffTime(-1 * 1000 * 60 * 60 * 24);
			int week = getWeekOfDate(date);
			Integer start = (week-1) * 150;
			Integer end = start+150 ;
			System.out.println(week);
			System.out.println(start);
			System.out.println(end);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
