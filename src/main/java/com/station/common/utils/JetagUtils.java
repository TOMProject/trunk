package com.station.common.utils;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.station.common.redis.RedisClientTemplate;

/**
 * 修改业务数据掉用的方法
 *
 */
public class JetagUtils{
	
	public static final Logger logger = LoggerFactory.getLogger(JetagUtils.class);
	//删除电池组redis--key
	public static void delStationInfoJetage1(HttpServletRequest request,RedisClientTemplate template){
		//要删除的key
		String key1 = "web.local./station/stationInfo/stationList.jetag";
		template.del(key1);
		logger.info("删除redis中的key-->：{}",key1);
		
	}

}
