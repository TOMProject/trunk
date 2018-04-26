package com.station.moudles.quartz;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.station.common.utils.MyDateUtils;
import com.station.moudles.entity.PackDataInfoLatest;
import com.station.moudles.service.PackDataInfoLatestService;

@Component("dataMonitorTask")
@Profile({"product201","product215"})
public class DataMonitorTask implements DataMonitor{
	private static final Logger logger = LoggerFactory.getLogger(DataMonitorTask.class);
	@Autowired
	PackDataInfoLatestService packDataInfoLatestService;
	@Value("${host.ip}")
	public String host;
	@Value("${fileName}")
	public String fileName;
	
	private int times = 0;
	private PackDataInfoLatest record = new PackDataInfoLatest();

	@Override
	public void execute() {
		logger.info("读取配置文件-->" + fileName);
		logger.info(host + "开始检测数据");
		try {
			record.setRcvTime(MyDateUtils.add(new Date(), Calendar.MINUTE, -30));
			int count = packDataInfoLatestService.getCountByTime(record);
			if (count < 1) {
				times += 30;
				send("18215599792");
				send("18081089935");
			} else {
				times = 0;
			}
		} catch (Exception e) {
			String temp = host;
			host = host + "-database";
			send("18215599792");
			send("18081089935");
			logger.error("MsgSendTask执行失败-->", e);
			host = temp;
		}
	}

	private void send(String phone) {
		String format = host + "系统%1$d分钟无数据更新，请检查数据网关。";
		String msg = String.format(format, times);
//		String msg = "智慧巡检找回密码验证码：" + 666 + times + "，请于1分钟内填写。如非本人操作，请忽略本短信。";
 		int appid = 1400057108;
		SmsSingleSender sender;
		try {
			sender = new SmsSingleSender(appid, "1c1018076746ae9a8f4951ef8d49b535");
			SmsSingleSenderResult result = sender.send(0, "86", phone, msg , "", "");
			int code = result.result;
		} catch (Exception e) {
			logger.error("MsgSendTask检测数据短信发送失败-->", e);
		}

	}

}
