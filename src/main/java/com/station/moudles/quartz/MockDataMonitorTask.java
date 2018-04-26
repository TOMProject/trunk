package com.station.moudles.quartz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("dataMonitorTask")
@Profile({"develop","test","test57"})
public class MockDataMonitorTask implements DataMonitor{
	private static final Logger logger = LoggerFactory.getLogger(MockDataMonitorTask.class);

	@Value("${fileName}")
	public String fileName;
	
	@Override
	public void execute() {
		logger.info("读取配置文件-->" + fileName);
	}
}
