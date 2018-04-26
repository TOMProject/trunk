package com.station.moudles.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.util.HSSFColor.INDIGO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.station.common.utils.RowParseHelper;
import com.station.common.utils.StringUtils;
import com.station.moudles.entity.DischargeManualRecord;
import com.station.moudles.entity.PackDataInfo;
import com.station.moudles.mapper.DischargeManualRecordMapper;
import com.station.moudles.service.DischargeManualRecordService;
import com.station.moudles.service.PackDataInfoService;

@Service
public class DischargeManualRecordServiceImpl extends BaseServiceImpl<DischargeManualRecord, Integer> implements DischargeManualRecordService {

	@Autowired
	DischargeManualRecordMapper dischargeManualRecordMapper;
	@Autowired
	PackDataInfoService packDataInfoSer;
	
	@Override
	public void dischargeExcelFile(Row row,Workbook wb) {
		
		DischargeManualRecord dischargeManualRecord = new DischargeManualRecord();
		//地市
		if(!StringUtils.isNull(row.getCell(1))) {
			if(isOverlength(row.getCell(1).toString(), 45)) {
				dischargeManualRecord.setCompanyName3(row.getCell(1).toString().trim());
				}else {
					throw new RuntimeException("地市填写超过45个字符串！");
			}
		}
		
		//区域
		if (!StringUtils.isNull(row.getCell(2))) {
			if(isOverlength(row.getCell(2).toString(), 45)) {
			dischargeManualRecord.setDistrict(row.getCell(2).toString().trim());
			}else {
				throw new RuntimeException("区域填写超过45个字符串！");
			}
		}
		//站名
		if (!StringUtils.isNull(row.getCell(3))) {
			if(isOverlength(row.getCell(3).toString(), 45)) {
				dischargeManualRecord.setAddressName(row.getCell(3).toString().trim());
			}else {
				throw new RuntimeException("站名填写超过45个字符串！");
			}
		}
		//站址编码
		if (!StringUtils.isNull(row.getCell(4))) {
			String addressCoding = RowParseHelper.getCell(row, 4);
			if(isOverlength(addressCoding, 32)) {
				dischargeManualRecord.setAddressCoding(addressCoding.trim());
			}else {
				throw new RuntimeException("站址编码填写超过32个字符串！");
			}
		}
		
		//运维id模板没有
//		if (!StringUtils.isNull(row.getCell(3))) {
//			String maintainanceId = RowParseHelper.getCell(row, 3);
//			if(isOverlength(maintainanceId, 45)) {
//				dischargeManualRecord.setMaintainanceId(maintainanceId.trim());
//			}else {
//				throw new RuntimeException("运维id填写超过45个字符串！");
//			}
//		}

		//设备id必填
		if (StringUtils.isNull(row.getCell(5))) {
			throw new RuntimeException("设备id必填项！");
		}else {
			if(isOverlength(row.getCell(5).toString(), 32)) {
				dischargeManualRecord.setGprsId(row.getCell(5).toString().trim());
			}else {
				throw new RuntimeException("设备编号填写超过32个字符串！");
			}

		}
		
		//放电人员
		if(!StringUtils.isNull(row.getCell(6))) {
			if(isOverlength(row.getCell(6).toString(), 32)) {
				dischargeManualRecord.setDischargePerson(row.getCell(6).toString().trim());
			}else {
				throw new RuntimeException("放电人员填写超过32个字符串！");
			}
		}
		
		//放电开始时间必填
		if (StringUtils.isNull(row.getCell(7))) {
			throw new RuntimeException("放电开始时间必填项！");
		}
		// 判断是否为日期类型
		if (0 == row.getCell(7).getCellType()) {
			if (DateUtil.isCellDateFormatted(row.getCell(7))) {
				// 用于转化为日期格式
				Date startDate = row.getCell(7).getDateCellValue();
				if(startDate.getTime() > new Date().getTime()) {
					throw new RuntimeException("放电开始时间填写超过了当前时间！");
				}
				dischargeManualRecord.setDischargeStartTime(startDate);
			}
		} else {
			throw new RuntimeException("放电开始时间填写格式不正确，正确的格式是：年/月/日  或者：年/月/日  时:分:秒 ");
		}
		//放电结束时间必填
		if (StringUtils.isNull(row.getCell(8))) {
			throw new RuntimeException("放电结束时间必填项！");
		}
		// 判断是否为日期类型
		if (0 == row.getCell(8).getCellType()) {
			if (DateUtil.isCellDateFormatted(row.getCell(8))) {
				// 用于转化为日期格式
				Date endDate = row.getCell(8).getDateCellValue();
				if(endDate.getTime() > new Date().getTime()) {
					throw new RuntimeException("放电结束时间填写超过了当前时间！");
				}
				dischargeManualRecord.setDischargeEndTime(endDate);
			}
		} else {
			throw new RuntimeException("放电结束时间填写格式不正确，正确的格式是：年/月/日  或者：年/月/日  时:分:秒 ");
		}
		
		//放电前电压
		if (StringUtils.isNull(row.getCell(9))) {
			throw new RuntimeException("放电前电压必须填写！");
		}
		if (!StringUtils.isNull(row.getCell(9))) {
			String temp = row.getCell(9).toString().trim().toUpperCase();
			String value_vol = "";
			if (temp.contains("V")) {
				value_vol = temp.replace("V", "");
			} else {
				value_vol = temp;
			}
			if(isWithinDecimal(value_vol)) {
				dischargeManualRecord.setDischargeForwordVol( new BigDecimal(value_vol));
			}else{
				throw new RuntimeException("输入的放电前电压超过数值范围，标准是：整数部位不超过6位小数部份不超过3位");
			}
		}
		//放电前电流
		if (!StringUtils.isNull(row.getCell(10))) {
			String temp = row.getCell(10).toString().trim().toUpperCase();
			String value_cur = "";
			if (temp.contains("A")) {
				value_cur = temp.replace("A", "");
			} else {
				value_cur = temp;
			}
			if(isWithinDecimal(value_cur)) {
				dischargeManualRecord.setDischargeForwordCur( new BigDecimal(value_cur));
			}else{
				throw new RuntimeException("输入的放电前电流超过数值范围，标准是：整数部位不超过6位小数部份不超过3位");
			}
		}
		//放电前系统电压
//		if (!StringUtils.isNull(row.getCell(10))) {
//			if(isWithinDecimal(row.getCell(10).toString())) {
//				dischargeManualRecord.setDischargeForwordSystemVol( new BigDecimal((row.getCell(10).toString().trim())));
//			}else{
//				throw new RuntimeException("输入的放电前系统电压超过数值范围，标准是：整数部位不超过6位小数部份不超过3位");
//			}
//		}
		//放电前系统电流
//		if (!StringUtils.isNull(row.getCell(11))) {
//			if(isWithinDecimal(row.getCell(11).toString())) {
//				dischargeManualRecord.setDischargeForwordSystemCur( new BigDecimal((row.getCell(11).toString().trim())));
//			}else{
//				throw new RuntimeException("输入的放电前系统电流超过数值范围，标准是：整数部位不超过6位小数部份不超过3位");
//			}
//		}
		//放电后电压
		if (StringUtils.isNull(row.getCell(11))) {
			throw new RuntimeException("放电截止电压必须填写！");
		}
		if (!StringUtils.isNull(row.getCell(11))) {
			String temp = row.getCell(11).toString().trim().toUpperCase();
			String value_vol = "";
			if (temp.contains("V")) {
				value_vol = temp.replace("V", "");
			} else {
				value_vol = temp;
			}
			
			if(isWithinDecimal(value_vol)) {
				dischargeManualRecord.setDischargeBackVol( new BigDecimal(value_vol));
			}else{
				throw new RuntimeException("输入的放电后电压超过数值范围，标准是：整数部位不超过6位小数部份不超过3位");
			}
		}
		//放电后电流
		if (!StringUtils.isNull(row.getCell(12))) {
			String temp = row.getCell(12).toString().trim().toUpperCase();
			String value_cur = "";
			if (temp.contains("A")) {
				value_cur = temp.replace("A", "");
			} else {
				value_cur = temp;
			}
			if(isWithinDecimal(value_cur)) {
				dischargeManualRecord.setDischargeBackCur( new BigDecimal(value_cur));
			}else{
				throw new RuntimeException("输入的放电后电流超过数值范围，标准是：整数部位不超过6位小数部份不超过3位");
			}
		}
		//放电后系统电压
//		if (!StringUtils.isNull(row.getCell(14))) {
//			if(isWithinDecimal(row.getCell(14).toString())) {
//				dischargeManualRecord.setDischargeBackSystemVol(new BigDecimal((row.getCell(14).toString().trim())));
//			}else{
//				throw new RuntimeException("输入的放电后系统电压超过数值范围，标准是：整数部位不超过6位小数部份不超过3位");
//			}
//		}
		//放电后系统电流
//		if (!StringUtils.isNull(row.getCell(15))) {
//			if(isWithinDecimal(row.getCell(15).toString())) {
//				dischargeManualRecord.setDischargeBackSystemCur(new BigDecimal((row.getCell(15).toString().trim())));
//			}else{
//				throw new RuntimeException("输入的放电后系统电流超过数值范围，标准是：整数部位不超过6位小数部份不超过3位");
//			}
//		}
//         

		//放电时长
		if(!StringUtils.isNull(row.getCell(13))) {
			String dischargeTime = getDischargeTime(row, wb);
			//按:拆分字符串
			String[] arr = dischargeTime.split(":|：");
			BigDecimal hour = new BigDecimal(arr[0]);
			BigDecimal big = new BigDecimal(Double.parseDouble(arr[1].toString())/60);
			String disTime = (hour.add(big)).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
			//String disTime = hour+"."+hour_end;
			if(isWithinDecimal(disTime)) {
			dischargeManualRecord.setDischargeTime(new BigDecimal(disTime.trim()));
			}else{
				throw new RuntimeException("输入的放电时长超过数值范围，标准是：整数部位不超过6位小数部份不超过3位");
			}
		}
		//备注
		if(!StringUtils.isNull(row.getCell(14))) {
			if(isOverlength(row.getCell(14).toString().trim(),100)) {
				dischargeManualRecord.setRemark(row.getCell(14).toString().trim());
			}else {
				throw new RuntimeException("备注输入的字符超过100位");
			}
		}
		//安装时间
		if(!StringUtils.isNull(row.getCell(15))) {
			// 判断是否为日期类型
			if (0 == row.getCell(15).getCellType()) {
				if (DateUtil.isCellDateFormatted(row.getCell(15))) {
					// 用于转化为日期格式
					Date installDate = row.getCell(15).getDateCellValue();
					if(installDate.getTime() > new Date().getTime()) {
						throw new RuntimeException("安装时间填写超过了当前时间！");
					}
					dischargeManualRecord.setInstallTime(installDate);
				}
			} else {
				throw new RuntimeException("安装时间填写格式不正确，正确的格式是：年/月/日  或者：年/月/日  时:分:秒 ");
			}
			
		}
		//电池品牌
		if(!StringUtils.isNull(row.getCell(16))) {
			if(isOverlength(row.getCell(16).toString().trim(),32)) {
				dischargeManualRecord.setCellPlant(row.getCell(16).toString().trim());
			}else {
				throw new RuntimeException("电池品牌输入的字符超过32位");
			}
		}
		//电池类型
//		if(!StringUtils.isNull(row.getCell(20))) {
//			if(isOverlength(row.getCell(20).toString().trim(),45)) {
//			dischargeManualRecord.setCellType(row.getCell(20).toString().trim());
//			}else {
//				throw new RuntimeException("电池类型输入的字符超过45位");
//			}
//		}
		//出过历史整治报告
//		if(!StringUtils.isNull(row.getCell(21))) {
//			if(isOverlength(row.getCell(21).toString().trim(),32)) {
//				dischargeManualRecord.setReportHistory(row.getCell(21).toString().trim());
//			} else {
//				throw new RuntimeException("出过历史整治报告输入的字符超过32位");
//			}
//		}
		//是否出具历史整治报告-默认是否
		dischargeManualRecord.setIsProcessed(0);

		//根据gprsid,开始放电时间，结束放电时间查询放电记录，如果有就更新没有就新增
		DischargeManualRecord query = new DischargeManualRecord();
		query.setGprsId(dischargeManualRecord.getGprsId());
		query.setDischargeStartTime(dischargeManualRecord.getDischargeStartTime());
		query.setDischargeEndTime(dischargeManualRecord.getDischargeEndTime());
		List<DischargeManualRecord> discharge = dischargeManualRecordMapper.selectListSelective(query);
		if(discharge.size() != 0) {
			dischargeManualRecord.setId(discharge.get(0).getId());
			dischargeManualRecordMapper.updateByPrimaryKeySelective(dischargeManualRecord);
		}else {
		dischargeManualRecordMapper.insertSelective(dischargeManualRecord);
		}
	}
	
	/**
	 * 判断字符串长度
	 * @param Str
	 * @param length
	 * @return
	 */
	public boolean isOverlength(String Str,int length) {
		if(Str.length() < length) {
			return true;
		}
		return false;
		
	}
	
	/**
	 * 是否再规定的数值范围内
	 * @param str
	 * @return
	 */
	public boolean isWithinDecimal(String str) {
		boolean flag = false;
		Pattern regex = Pattern.compile("^(-)?\\d{0,6}(\\.\\d{0,3})?$");
		Matcher matcher = regex.matcher(str);
		flag = matcher.matches();
		return flag;
		
		
	}
	//读取公式的计算值
	public String getDischargeTime(Row row,Workbook wb) {
		//读取公式计算的值
		FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();   
		// suppose your formula is in B3  
		CellReference cellReference = new CellReference("N5");   
		Cell cell = row.getCell(cellReference.getCol());   
		CellValue cellValue = evaluator.evaluate(cell);  
		String dischargeTime = null; 
		switch (cellValue.getCellType()) {  
		    case Cell.CELL_TYPE_BOOLEAN:  
		        //System.out.println(cellValue.getBooleanValue());  
		        break;  
		    case Cell.CELL_TYPE_NUMERIC:  
		    	   dischargeTime = String.valueOf(cellValue.getNumberValue());  
		        break;  
		    case Cell.CELL_TYPE_STRING:  
		        dischargeTime = cellValue.getStringValue();  
		        break;  
		    case Cell.CELL_TYPE_BLANK:  
		        break;  
		    case Cell.CELL_TYPE_ERROR:  
		        break;  
		  
		    // CELL_TYPE_FORMULA will never happen  
		    case Cell.CELL_TYPE_FORMULA:   
		        break;  
		} 
		return dischargeTime;
		
	}
	
	@Override
	public List<PackDataInfo> getOneLatestRealDischargeRecord(String gprsId) {
		DischargeManualRecord record = new DischargeManualRecord();
		record.setGprsId(gprsId);
		DischargeManualRecord dischargeManualRecord = dischargeManualRecordMapper.selectOneLatestSelective(record);
		return packDataInfoSer.verifyDischargeRecord(dischargeManualRecord);
	}

	public static void main(String[] args) {
		String str = "54.4";
		boolean flag = false;
		Pattern regex = Pattern.compile("^(-)?\\d{0,6}(\\.\\d{0,3})?$");
		Matcher matcher = regex.matcher(str);
		flag = matcher.matches();
		System.out.println(flag); 
	}

	@Override
	public DischargeManualRecord selectDischargeDetail(Integer id) {

		return dischargeManualRecordMapper.selectDischargeDetail(id);
	}
	
	
	

}




