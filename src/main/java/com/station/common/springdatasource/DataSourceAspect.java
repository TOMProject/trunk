package com.station.common.springdatasource;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.station.moudles.service.impl.GprsConfigInfoServiceImpl;

/**
 * 定义数据源的AOP切面，通过该Service的方法名判断是应该走读库还是写库
 * 
 */
public class DataSourceAspect {

	private static final Logger logger = LoggerFactory.getLogger(GprsConfigInfoServiceImpl.class);

    /**
     * 在进入Service方法之前执行
     * 
     * @param point 切面对象
     */
    public void before(JoinPoint point) {
        // 获取到当前执行的方法名
        String methodName = point.getSignature().getName();
        String declaringTypeName = point.getSignature().getDeclaringTypeName();
        if (isSlave(methodName)) {//只读方法
        	if(StringUtils.isBlank(DBContextHolder.getDBType())) {
	            // 标记为读库
	        	DBContextHolder.setDBType(DBContextHolder.DATA_SOURCE_SLAVER);
	        	logger.info("进入slaver读库：方法是"+declaringTypeName+"-->"+methodName);
        	}
        } else {
            // 标记为写库
        	DBContextHolder.setDBType(DBContextHolder.DATA_SOURCE_MASTER);
        	logger.info("进入master写库：方法是"+declaringTypeName+"-->"+methodName);
        }
    }

    /**
     * 判断是否为读库
     * 
     * @param methodName
     * @return
     */
    private Boolean isSlave(String methodName) {
        // 方法名以query、find、get开头的方法名走从库
        Boolean falg= StringUtils.startsWithAny(methodName, "find", "get","select");
        return falg;
    }

}
