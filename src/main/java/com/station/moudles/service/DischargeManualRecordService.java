package com.station.moudles.service;

import com.station.moudles.entity.DischargeManualRecord;
import com.station.moudles.entity.PackDataInfo;

import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
public interface DischargeManualRecordService extends BaseService<DischargeManualRecord, Integer> {
	
	//手动放电摘数据要导入文件
	void dischargeExcelFile(Row row,Workbook wb);
	
	List<PackDataInfo> getOneLatestRealDischargeRecord(String gprsId);
	//放电详情
	DischargeManualRecord selectDischargeDetail(Integer id);
}