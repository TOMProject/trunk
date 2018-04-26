package com.station.common.springdatasource;

public class DBContextHolder{  
	//写库对应的key
    public static final String DATA_SOURCE_MASTER = "master";  
    //读库对应的key
    public static final String DATA_SOURCE_SLAVER = "slaver";  
    //使用threadLocal记录当前线程的数据源key  
    private static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<String>();  
    //设置数据源key  
    public static void setDBType(String dbType) {  
    	THREAD_LOCAL.set(dbType);  
    }  
    //获取数据源key  
    public static String getDBType() {  
        return THREAD_LOCAL.get();  
    }  
      
    public static void clearDBType() {  
    	THREAD_LOCAL.remove();  
    }  
}  