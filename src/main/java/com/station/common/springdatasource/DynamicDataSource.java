package com.station.common.springdatasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
/**
 * 定义数据源这里由spring提高的AbstractRoutingDataSource,实现determineCurrentLookupKey方法
 * 由于DynamicDataSource是单例，线程不安全，所以采用ThreadLocal保证线程安全，由DBContextHolder完成
 *
 */
 public class DynamicDataSource extends AbstractRoutingDataSource{  
  
    @Override  
    protected Object determineCurrentLookupKey() {  
        return DBContextHolder.getDBType(); 
    }  
}