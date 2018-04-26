package com.station.common.cache;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.ServletContextAware;

import com.station.common.utils.GetProperty;

public class InitCache implements InitializingBean, ServletContextAware {
	private static final Logger logger = LoggerFactory.getLogger(InitCache.class);
	public static String applicationId;
	public static Map<Integer, Map<String, Integer>> allUserMenuMap = new HashMap<Integer, Map<String, Integer>>();
	public static Map<Integer, Map<String, Integer>> allUserOtherPermissionsMap = new HashMap<Integer, Map<String, Integer>>();

	public static boolean pulseDischargeProgressFlag=false;
	//public static boolean StationInfoFile = true;//电池组导入标志
	public static boolean stationProgressFlag=false;
	
	//上次检查上传数据的时间。
	public static Date lastPackDataCheckTime;
	
	@Value("${fileName}")
	public String fileName;
	
	// @Autowired
	// public RedisClientTemplate redisClient;
	@Override
	public void afterPropertiesSet() throws Exception {
	}

	@Override
	public void setServletContext(ServletContext paramServletContext) {
		// redisClient.set("zdmTest", "test1111");
		// System.out.println(redisClient.get("zdmTest"));
		// System.out.println(redisClient.get("zdmtest"));
		String path = GetProperty.GetConfPath(fileName);
		try {
			path = URLDecoder.decode(path, "UTF-8");
			applicationId = GetProperty.readValue(path, "applicationId");
			paramServletContext.setAttribute("applicationId", applicationId);
			System.out.println("启动ServletContext");
			logger.info("读取配置文件-->" + fileName);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
