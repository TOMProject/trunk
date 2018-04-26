package com.station.moudles.controller;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.station.common.Constant;
import com.station.common.chart.JfreeChartUtils;
import com.station.common.utils.JxlsUtil;
import com.station.common.utils.StringUtils;
import com.station.common.worldUtils.CustomXWPFDocument;
import com.station.common.worldUtils.WorderToNewWordUtils;
import com.station.moudles.entity.DischargeManualRecord;
import com.station.moudles.entity.PackDataInfo;
import com.station.moudles.helper.ChargeEvent;
import com.station.moudles.helper.DischargeEvent;
import com.station.moudles.helper.LossChargeEvent;
import com.station.moudles.mapper.DischargeManualRecordMapper;
import com.station.moudles.service.ModelCalculationService;
import com.station.moudles.service.PackDataInfoService;
import com.station.moudles.service.ReportService;
import com.station.moudles.vo.AjaxResponse;
import com.station.moudles.vo.report.ChargeDischargeReport;
import com.station.moudles.vo.report.ModelReport;
import com.station.moudles.vo.report.PulseReport;
import com.station.moudles.vo.report.PulseReportItem;
import com.station.moudles.vo.report.StationReport;
import com.station.moudles.vo.report.StationReportItem;
import com.station.moudles.vo.report.SuggestionReport;

import net.sf.jxls.transformer.XLSTransformer;

/**
 * Created by Jack on 9/17/2017.
 */
@Controller
@RequestMapping(value = "/report")
public class ReportController extends BaseController {

	private final ModelCalculationService modelCalculationService;
	private final ReportService reportService;
	
	@Autowired
	DischargeManualRecordMapper dischargeAbstractRecordMapper;
	@Autowired
	PackDataInfoService packDataInfoSer;
	@Autowired
	DischargeManualRecordMapper dischargeManualRecordMapper;
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;
	
	@Autowired
	public ReportController(ModelCalculationService modelCalculationService, ReportService reportService) {
		this.modelCalculationService = modelCalculationService;
		this.reportService = reportService;
	}

	@ResponseBody
	@PostMapping(value = "station_suggestion/{station_id}")
	public AjaxResponse stationSuggestion(@PathVariable("station_id") Integer stationId) {
		if (stationId == null) {
			return new AjaxResponse<>(Constant.RS_CODE_ERROR, "基站编号必须");
		}
		SuggestionReport report = reportService.generateSuggestionReport(stationId);
		if (report == null) {
			return new AjaxResponse<>(Constant.RS_CODE_ERROR, "整治策略建议失败");
		}
		return new AjaxResponse(Constant.RS_CODE_SUCCESS, "整治策略数据生成成功", report);
	}

	@ResponseBody
	@PostMapping(value = "/charge_discharge/{station_id}")
	public AjaxResponse chargetAndDischargeReport(@PathVariable("station_id") Integer stationId,
			@RequestParam("start_time") Date startTime, @RequestParam("end_time") Date endTime) {
		ChargeDischargeReport report = reportService.getChargeDischargeReport(stationId, startTime, endTime, 
				new ChargeEvent(false), new DischargeEvent(false), new LossChargeEvent(false));
		if (report == null) {
			return new AjaxResponse<>(Constant.RS_CODE_ERROR, "获取电池组充放电记录失败");
		}
		return new AjaxResponse(Constant.RS_CODE_SUCCESS, "获取电池组充放电记录成功", report);
	}

	/**
	 * 电池组充放电记录报表
	 *
	 * @param stationId
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws IOException
	 */
	@ResponseBody
	@GetMapping(value = "/charge_discharge/{station_id}")
	public AjaxResponse exportChargeAndDischargeReport(@PathVariable("station_id") Integer stationId,
			@RequestParam("start_time") Date startTime, @RequestParam("end_time") Date endTime)
			throws IOException, InvalidFormatException {
		ChargeDischargeReport report = reportService.getChargeDischargeReport(stationId, startTime, endTime, 
				new ChargeEvent(false), new DischargeEvent(false), new LossChargeEvent(false));
		if (report == null) {
			return new AjaxResponse<>(Constant.RS_CODE_ERROR, "获取电池组充放电记录失败");
		}
		String fileName = report.getCompanyName() + report.getStationName() + "电池组充放电记录表.xlsx";
		File file = new File(generateChargeDischargeTemplateFile(report));
		return downloadAsExcelFile(fileName, file);
	}

	private String generateChargeDischargeTemplateFile(ChargeDischargeReport report)
			throws IOException, InvalidFormatException {
		String srcPath = Constant.TEMPLETE_PATH + "电池组充放电记录表.xlsx";
		srcPath = URLDecoder.decode(srcPath, "UTF-8");
		String destPath = "电池组充放电记录表" + new Date().getTime() + ".xlsx";

		List<String> sheetNames = Lists.newArrayList("电池组数据", "截止电压最低放电详情", "最近一次有效放电详情");
		try (InputStream is = new BufferedInputStream(new FileInputStream(srcPath));
				OutputStream os = new BufferedOutputStream(new FileOutputStream(destPath))) {
			Workbook workbook = new XLSTransformer().transformMultipleSheetsList(
					new BufferedInputStream(new FileInputStream(srcPath)), Lists.newArrayList(report), sheetNames,
					"result", Maps.newHashMap(), 0);
			workbook.write(os);
			os.flush();
		}
		return destPath;
	}

	/**
	 * 生成整治策略数据
	 *
	 * @param companyId
	 * @param province
	 * @param city
	 * @param district
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	@ResponseBody
	@PostMapping(value = "/rectification_suggestion/{company_id}/generation")
	public AjaxResponse generateRectificationSuggestion(@PathVariable("company_id") Integer companyId,
			@RequestParam(value = "province", required = false) String province,
			@RequestParam(value = "city", required = false) String city,
			@RequestParam(value = "district", required = false) String district) {
		if (companyId == null) {
			return new AjaxResponse<>(Constant.RS_CODE_ERROR, "公司编号必须");
		}
		SuggestionReport report = reportService.generateSuggestionReport(companyId, province, city, district);
		if (report == null) {
			return new AjaxResponse<>(Constant.RS_CODE_ERROR, "整治策略建议失败");
		}
		return new AjaxResponse(Constant.RS_CODE_SUCCESS, "整治策略数据生成成功");
	}

	@ResponseBody
	@PostMapping(value = "/rectification_suggestion/{company_id}")
	public AjaxResponse getRectificationSuggestion(@PathVariable("company_id") Integer companyId,
			@RequestParam(value = "province", required = false) String province,
			@RequestParam(value = "city", required = false) String city,
			@RequestParam(value = "district", required = false) String district) {
		if (companyId == null) {
			return new AjaxResponse<>(Constant.RS_CODE_ERROR, "公司编号必须");
		}
		SuggestionReport report = reportService.getSuggestionReportItems(companyId, province, city, district);
		if (report == null) {
			return new AjaxResponse<>(Constant.RS_CODE_ERROR, "整治策略建议失败");
		}
		return new AjaxResponse(Constant.RS_CODE_SUCCESS, "整治策略建议", report.getItems());
	}

	/**
	 * 整治建议报表
	 * @param companyId
	 * @param province
	 * @param city
	 * @param district
	 * @return
	 */
	@ResponseBody
	@GetMapping(value = "/rectification_suggestion/{company_id}")
	public AjaxResponse exportRectificationSuggestion(@PathVariable("company_id") Integer companyId,
			@RequestParam(value = "province", required = false) String province,
			@RequestParam(value = "city", required = false) String city,
			@RequestParam(value = "district", required = false) String district)
			throws IOException, InvalidFormatException {
		if (companyId == null) {
			return new AjaxResponse<>(Constant.RS_CODE_ERROR, "公司编号必须");
		}
		SuggestionReport report = reportService.getSuggestionReportItems(companyId, province, city, district);
		if (report == null) {
			return new AjaxResponse<>(Constant.RS_CODE_ERROR, "整治策略建议失败");
		}
		String fileName = report.getCompanyName() + "电池组整冶建议报表.xlsx";
		File file = new File(generateSuggestionTemplateFile(report));
		return downloadAsExcelFile(fileName, file);
	}
	
	
	/**
	 * 新的整治报告接口，目前只计算容量
	 * @param dischargeManualRecords
	 * @return
	 */
	@RequestMapping(value = "/rectification_suggestion/generation", method = RequestMethod.POST)
	@ResponseBody
	public AjaxResponse generateRectificationSuggestion(@RequestBody List<DischargeManualRecord> dischargeManualRecords) {
		AjaxResponse ajaxResponse = new AjaxResponse<>(Constant.RS_CODE_SUCCESS,"提交成功！");
		if (CollectionUtils.isEmpty(dischargeManualRecords)) {
			ajaxResponse.setCode(Constant.RS_CODE_ERROR);
			ajaxResponse.setMsg("非法参数异常！");
			return ajaxResponse;
		}
		try {
			dischargeManualRecords = dischargeManualRecords.stream()
					.sorted(Comparator.comparing(DischargeManualRecord::getDischargeEndTime))
					.collect(Collectors.toList());
			for (DischargeManualRecord record : dischargeManualRecords) {
				taskExecutor.execute(() -> {
					List<PackDataInfo> dischargeRecords = packDataInfoSer.verifyDischargeRecord(record);
					if (CollectionUtils.isNotEmpty(dischargeRecords)) {
						try {
							reportService.generateSuggestionReport(dischargeRecords, record);
						} catch (Exception e) {
							logger.error(record.getGprsId() + "--->生成报告失败", e);
							DischargeManualRecord dischargeManualRecord = new DischargeManualRecord();
							dischargeManualRecord.setId(record.getId());
							dischargeManualRecord.setIsProcessed(0);
							dischargeManualRecord.setReportRemark("生成报告失败");
							dischargeManualRecordMapper.updateByPrimaryKeySelective(dischargeManualRecord);
						}
					}
				 });
			}
		} catch (Exception e) {
			ajaxResponse.setCode(Constant.RS_CODE_ERROR);
			ajaxResponse.setMsg("生成报告失败！");
		}
		return ajaxResponse;
	}

	private String generateSuggestionTemplateFile(SuggestionReport report) throws IOException, InvalidFormatException {
		String srcPath = Constant.TEMPLETE_PATH + "市区级电池组整冶建议报表.xlsx";
		srcPath = URLDecoder.decode(srcPath, "UTF-8");
		String destPath = "市区级电池组整冶建议报表" + new Date().getTime() + ".xlsx";

		try (InputStream is = new BufferedInputStream(new FileInputStream(srcPath));
				OutputStream os = new BufferedOutputStream(new FileOutputStream(destPath))) {
			Map beanParams = Maps.newHashMap();
			beanParams.put("result", report);
			XSSFWorkbook workbook = (XSSFWorkbook) new XLSTransformer().transformXLS(is, beanParams);
			XSSFSheet sheet = workbook.getSheetAt(0);
			if (CollectionUtils.isNotEmpty(report.getItems())) {
				XSSFCellStyle style = workbook.createCellStyle();
				style.setFillForegroundColor(new XSSFColor(new Color(220, 230, 241)));
				style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				style.setAlignment(HorizontalAlignment.CENTER);
				style.setVerticalAlignment(VerticalAlignment.CENTER);
				for (int i = 4; i < sheet.getLastRowNum(); i++) {
					if (i % 2 == 0) {
						continue;
					}
					XSSFRow row = sheet.getRow(i);
					for (int j = 1; j < 11; j++) {
						XSSFCell cell = row.getCell(j);
						if (cell != null) {
							cell.setCellStyle(style);
						}
					}
				}
			}
			workbook.write(os);
			os.flush();
		}
		return destPath;
	}

	@ResponseBody
	@GetMapping(value = "/pulse_test/{company_id}")
	public AjaxResponse exportPulseTest(@PathVariable("company_id") Integer companyId,
			@RequestParam("start_time") Date startTime, @RequestParam("end_time") Date endTime)
			throws IOException, InvalidFormatException {
		if (companyId == null || startTime == null || endTime == null) {
			return new AjaxResponse<>(Constant.RS_CODE_ERROR, "公司编号和时间范围必须");
		}
		PulseReport pulseReport = reportService.generatePulseReport(companyId, startTime, endTime);
		if (pulseReport == null) {
			return new AjaxResponse<>(Constant.RS_CODE_ERROR, "该时间段内没有数据");
		}

		String fileName = pulseReport.getCompanyName() + "特征测试统计报表.xlsx";
		File file = new File(generatePulseTestTemplateFile(pulseReport));
		return downloadAsExcelFile(fileName, file);
	}

	@ResponseBody
	@GetMapping(value = "/model_calculation/{company_id}")
	public AjaxResponse exportModelCalculation(@PathVariable("company_id") Integer companyId)
			throws IOException, InvalidFormatException {
		if (companyId == null) {
			return new AjaxResponse<>(Constant.RS_CODE_ERROR, "请输入公司编号");
		}
		ModelReport modelReport = modelCalculationService.generateModelReport(companyId);
		if (modelReport == null) {
			return new AjaxResponse<>(Constant.RS_CODE_ERROR, "请输入有效的公司编号");
		}
		String name = "模型计算统计报表";
		String fileName = modelReport.getCompanyName() + name + ".xlsx";
		File file = new File(generateTemplateFile(modelReport, name));
		return downloadAsExcelFile(fileName, file);
	}

	private AjaxResponse downloadAsExcelFile(String fileName, File file) throws IOException {
		// 设置response
		fileName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");
		response.setContentType("application/x-msdownload; charset=UTF-8");
		response.addHeader("content-type", "application/x-msdownload;");
		response.addHeader("content-disposition", "attachment; filename=" + fileName);
		response.setContentLength((int) file.length());
		
		try (OutputStream os = new BufferedOutputStream(response.getOutputStream())) {
			os.write(FileUtils.readFileToByteArray(file));
			os.flush();
			
		} finally {
			FileUtils.deleteQuietly(file);
		}
		return new AjaxResponse();
	}

	private <T> String generateTemplateFile(T t, String name) throws IOException, InvalidFormatException {
		String srcPath = Constant.TEMPLETE_PATH + name + ".xlsx";
		srcPath = URLDecoder.decode(srcPath, "UTF-8");
		String destPath = name + "_" + new Date().getTime() + ".xlsx";
		Map beanParams = Maps.newHashMap();
		beanParams.put("result", t);
		new XLSTransformer().transformXLS(srcPath, beanParams, destPath);
		return destPath;
	}

	//下载world
	private AjaxResponse downloadAsWordFile(String fileName, File file) throws IOException {
		// 设置response
		fileName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");
		response.setContentType("application/x-msdownload; charset=UTF-8");
		response.addHeader("content-type", "application/x-msdownload;");
		response.addHeader("content-disposition", "attachment; filename=" + fileName);
		response.setContentLength((int) file.length());
		
		try (OutputStream os = new BufferedOutputStream(response.getOutputStream())) {
			os.write(FileUtils.readFileToByteArray(file));
			os.flush();
			
		} finally {
		FileUtils.deleteQuietly(file);
		}
		return new AjaxResponse();
	}
	
	
	
	 /**
     * 判断文本中时候包含$
     * @param text 文本
     * @return 包含返回true,不包含返回false
     */
    public static boolean checkText(String text){
        boolean check  =  false;
        if(text.indexOf("$")!= -1){
            check = true;
        }
        return check;

    }

    /**
     * 匹配传入信息集合与模板
     * @param value 模板需要替换的区域
     * @param textMap 传入信息集合
     * @return 模板需要替换区域信息集合对应值
     */
    public static String changeValue(String value, Map<String, String> textMap){
        Set<Entry<String, String>> textSets = textMap.entrySet();
        for (Entry<String, String> textSet : textSets) {
            //匹配模板与替换值 格式${key}
            String key = "${"+textSet.getKey()+"}";
            if(value.indexOf(key)!= -1){
                value = textSet.getValue();
            }
        }
        //模板未匹配到区域替换为空
        if(checkText(value)){
           // value = "";
        }
        return value;
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	private String generatePulseTestTemplateFile(PulseReport pulseReport) throws IOException, InvalidFormatException {
		String srcPath = Constant.TEMPLETE_PATH + "特征测试统计.xlsx";
		srcPath = URLDecoder.decode(srcPath, "UTF-8");
		String destPath = "特征测试统计" + new Date().getTime() + ".xlsx";

		try (InputStream is = new BufferedInputStream(new FileInputStream(srcPath));
				OutputStream os = new BufferedOutputStream(new FileOutputStream(destPath))) {
			Map beanParams = Maps.newHashMap();
			beanParams.put("result", pulseReport);
			XSSFWorkbook workbook = (XSSFWorkbook) new XLSTransformer().transformXLS(is, beanParams);
			XSSFSheet sheet = workbook.getSheetAt(0);

			if (CollectionUtils.isNotEmpty(pulseReport.getItems())) {

				XSSFCellStyle defaultStyle = workbook.createCellStyle();
				defaultStyle.setFillForegroundColor(new XSSFColor(new Color(255, 215, 0)));
				defaultStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				defaultStyle.setBorderRight(BorderStyle.THIN);
				defaultStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
				defaultStyle.setBorderLeft(BorderStyle.THIN);
				defaultStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
				defaultStyle.setBorderTop(BorderStyle.THIN);
				defaultStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
				defaultStyle.setBorderBottom(BorderStyle.THIN);
				defaultStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());

				XSSFCellStyle failedStyle = workbook.createCellStyle();
				failedStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
				failedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				failedStyle.setBorderRight(BorderStyle.THIN);
				failedStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
				failedStyle.setBorderLeft(BorderStyle.THIN);
				failedStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
				failedStyle.setBorderTop(BorderStyle.THIN);
				failedStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
				failedStyle.setBorderBottom(BorderStyle.THIN);
				failedStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());

				XSSFCellStyle successStyle = workbook.createCellStyle();
				successStyle.setFillForegroundColor(new XSSFColor(new Color(245, 245, 245)));
				successStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				successStyle.setBorderRight(BorderStyle.THIN);
				successStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
				successStyle.setBorderLeft(BorderStyle.THIN);
				successStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
				successStyle.setBorderTop(BorderStyle.THIN);
				successStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
				successStyle.setBorderBottom(BorderStyle.THIN);
				successStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());

				int rowIndex = 5;
				for (PulseReportItem item : pulseReport.getItems()) {
					XSSFRow row = sheet.getRow(rowIndex);
					int lastCellNum = row.getLastCellNum();
					for (int j = 2; j < lastCellNum; j++) {
						XSSFCell cell = row.getCell(j);
						XSSFCellStyle style = defaultStyle;
						// 0未发送,1发送成功，2特征执行成功，3特征执行失败
						Byte status = item.getCellStatusMap().get(j - 1);
						if (status == null) {
							status = 0;
						}
						int value = status.intValue();
						if (value == 2) {
							style = successStyle;
						} else if (value == 3) {
							style = failedStyle;
						}
						cell.setCellStyle(style);
					}

					rowIndex++;
				}
			}
			workbook.write(os);
			os.flush();
		}
		return destPath;
	}

	@RequestMapping(value = "/maintainSuggestion", method = RequestMethod.POST)
	@ResponseBody
	public AjaxResponse<StationReport> generateStationVolCurStr(@RequestBody StationReport stationReport) {
		AjaxResponse<StationReport> ajaxResponse = new AjaxResponse<StationReport>(Constant.RS_CODE_ERROR, "请设置公司id！");
		if (stationReport.getCompanyId3() == null) {
			return ajaxResponse;
		}
		try {
			// 需求是，传两天日期，要查询两天的数据。结束时间加一天
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(stationReport.getEndRcvTime());
			calendar.add(Calendar.DATE, 1);
			calendar.add(Calendar.SECOND, -1);
			stationReport.setEndRcvTime(calendar.getTime());
			StationReport report = reportService.generateStationVolCurStr(stationReport);
			ajaxResponse.setCode(Constant.RS_CODE_SUCCESS);
			ajaxResponse.setMsg(Constant.RS_MSG_SUCCESS);
			ajaxResponse.setData(report);
		} catch (Exception e) {
			ajaxResponse.setCode(Constant.RS_CODE_ERROR);
			ajaxResponse.setMsg(Constant.RS_MSG_ERROR);
		}
		return ajaxResponse;
	}
	

	@RequestMapping(value ="/maintainSuggestion/download",method = RequestMethod.GET)
	@ResponseBody
	public AjaxResponse<StationReport> downloadMaintainSuggestion(@RequestParam("companyId3") Integer companyId3,
																@RequestParam("companyName") String companyName,
																@RequestParam("startRcvTime") Date startRcvTime,
																@RequestParam("endRcvTime") Date endRcvTime,
																@RequestParam("linkStatus") Byte linkStatus,
																@RequestParam("state") String state){
		AjaxResponse<StationReport> ajaxResponse = new AjaxResponse<StationReport>(Constant.RS_CODE_ERROR, "请设置公司id！");
		if (companyId3 == null) {
			return ajaxResponse;
		}
		try {
			//设备维护建议如果下载路径乱码就要打开下面的代码
			//companyName = new String(companyName.getBytes("ISO-8859-1"),"UTF-8");
			// 需求是，传两天日期，要查询两天的数据。结束时间加一天
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(endRcvTime);
			calendar.add(Calendar.DATE, 1);
			calendar.add(Calendar.SECOND, -1);
			StationReport stationReport = new StationReport();
			stationReport.setCompanyId3(companyId3);
			stationReport.setCompanyName(companyName);
			stationReport.setStartRcvTime(startRcvTime);
			stationReport.setEndRcvTime(calendar.getTime());
			stationReport.setLinkStatus(linkStatus);
			stationReport.setState(StringUtils.isNull(state) ? null:state);
			StationReport report = reportService.generateStationVolCurStr(stationReport);
			String name = "设备维护数据报表";
			String fileName = report.getCompanyName() + name + ".xlsx";
			File file = new File(generateTemplateFile(report, name));
			return downloadAsExcelFile(fileName, file);
		} catch (Exception e) {
			ajaxResponse.setCode(Constant.RS_CODE_ERROR);
			ajaxResponse.setMsg(Constant.RS_MSG_ERROR);
		}
		return ajaxResponse;
	}
	/**
	 * 获得浮充态异常数据
	 * @return
	 */
	@RequestMapping(value="chargeAbnormalDaga",method = RequestMethod.GET )
	@ResponseBody
	public AjaxResponse<StationReport>  getChargeAbnormalData(@RequestParam("startRcvTime")Date startRcvTime,
																@RequestParam("endRcvTime") Date endRcvTime,
																@RequestParam("vol") BigDecimal vol,
																@RequestParam("minCur") BigDecimal minCur,
																@RequestParam("maxCur") BigDecimal maxCur){
		AjaxResponse<StationReport> ajaxResponse = new AjaxResponse<StationReport>(Constant.RS_CODE_ERROR, "请设置公司id！");
		//根据条件获取浮充异常的总数数据
		try {
			StationReport stationReport = new StationReport();
			stationReport.setStartRcvTimeStr(JxlsUtil.dateFmt(startRcvTime));
			stationReport.setEndRcvTimeStr(JxlsUtil.dateFmt(endRcvTime));
			stationReport.setStartRcvTime(startRcvTime);
			stationReport.setEndRcvTime(endRcvTime);
			stationReport.setVol(vol);
			stationReport.setMinCur(minCur);
			stationReport.setMaxCur(maxCur);
			StationReport report= reportService.getChargeAbnormalData(stationReport);
			//StationReport report= reportService.getAbnormalPdiReport(stationReport);
			if(report != null) {
				String name= "异常浮充数据";
				File file = new File(generateTemplateFile(report, name));
				downloadAsExcelFile(name+".xlsx", file);
				ajaxResponse.setCode(Constant.RS_CODE_SUCCESS);
				ajaxResponse.setMsg(Constant.RS_MSG_SUCCESS);
				ajaxResponse.setData(report);
			}else {
				ajaxResponse.setMsg("没有数据");
			}
		} catch (Exception e) {
			e.printStackTrace();
			ajaxResponse.setMsg(e.getMessage());
		}
		return ajaxResponse;
		
	}
	
	
	/**
	 * 获得电池整治报告word
	 * @return
	 */
	@RequestMapping(value="cellRectification_word",method = RequestMethod.GET)//@RequestBody List<DischargeManualRecord> dischargeList
	@ResponseBody
	public AjaxResponse<List<DischargeManualRecord>>  getCellRectification_word(){
		AjaxResponse<List<DischargeManualRecord>> ajaxResponse = new AjaxResponse<List<DischargeManualRecord>>(Constant.RS_CODE_ERROR, "请设置公司id！");
		
		try {
			StationReportItem stationReport = new StationReportItem();
			StationReportItem report= reportService.getCellRectificationData(stationReport);			
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();			
		//	for (DischargeManualRecord discharge : dischargeList) {
				DischargeManualRecord discharge = new DischargeManualRecord();
				discharge.setGprsId("T0B000208");
				String da ="2018-05-15 18:17:22";
				SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				discharge.setDischargeStartTime(s.parse(da));
				Map<String, Object> parameterMap1 = new HashMap<String, Object>();
				
				parameterMap1.put("type", "png");
				parameterMap1.put("width", "700");
				parameterMap1.put("high", "202");
				parameterMap1.put("gprsId", "T0B000208");
				parameterMap1.put("addressName", "站名");
				parameterMap1.put("time", "2018-03-02 12:11:45"); 
				parameterMap1.put("startTime",discharge.getDischargeStartTime() ); 
//				parameterMap1.put("imgA", "附件A是");
//				parameterMap1.put("imgB", "附件B");
				parameterMap1.put("describe", "附件A描述d水电费是否是的范德萨范德萨发沙发上分散发送发送发送发送辐射服");
			    parameterMap1.put("cells", "1,2,3");
			    parameterMap1.put("text", "地址Y0B124578");
			    parameterMap1.put("textB", "地址");
			    parameterMap1.put("vol_bight","图5-单体电池放电电压曲线");
			    parameterMap1.put("picturePath", JfreeChartUtils.getPicturePath(discharge));//保存图片的路径
			    parameterMap1.put("path", JfreeChartUtils.getParentPath(discharge));//创建保存文件的路径
			    list.add(parameterMap1);
		//	}
				DischargeManualRecord discharge2 = new DischargeManualRecord();
				discharge2.setGprsId("Y0B000208");
				String da2 ="2018-06-15 18:17:22";
				SimpleDateFormat s2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				discharge2.setDischargeStartTime(s2.parse(da2));
				Map<String, Object> parameterMap2 = new HashMap<String, Object>();
				
				parameterMap2.put("type", "png");
				parameterMap2.put("width", "700");
				parameterMap2.put("high", "202");
				parameterMap2.put("gprsId", "Y0B000208");
				parameterMap2.put("addressName", "站名");
				parameterMap2.put("time", "2018-03-02 12:11:45"); 
				parameterMap2.put("startTime",discharge2.getDischargeStartTime() ); 
//				parameterMap1.put("imgA", "附件A是");
//				parameterMap1.put("imgB", "附件B");
				parameterMap2.put("describe", "附件A描述d水电费是否是的范德萨范德萨发沙发上分散发送发送发送发送辐射服");
			    parameterMap2.put("cells", "1,2,3");
			    parameterMap2.put("text", "地址Y0B124578");
			    parameterMap2.put("textB", "地址");
			    parameterMap2.put("vol_bight","图5-单体电池放电电压曲线");
			    parameterMap2.put("picturePath", JfreeChartUtils.getPicturePath(discharge2));//保存图片的路径
			    parameterMap2.put("path", JfreeChartUtils.getParentPath(discharge2));//创建保存文件的路径
			    list.add(parameterMap2);

		    
			List<String[]> testList = new ArrayList<String[]>();
			
			String name= "电池整治报告";
			String srcPath = Constant.TEMPLETE_PATH + name + ".docx";
			srcPath = URLDecoder.decode(srcPath, "UTF-8");
			List<String> outPutUrls = new ArrayList<String>();//保存文件路径
			//解析docx模板并获取document对象
			//XWPFDocument document =   document = new XWPFDocument(POIXMLDocument.openPackage(srcPath));
			CustomXWPFDocument doc = null;
			for(int i = 0 ;i < list.size() ; i++ ) {
				Map<String, Object> parameterMap = list.get(i);
	            OPCPackage pack = POIXMLDocument.openPackage(srcPath);  
	            doc = new CustomXWPFDocument(pack);  
		        //解析图片对象1234
	            parameterMap.put("imgA_1",  list.get(i).get("picturePath")+"\\"+"imgA_1.png");
	            parameterMap.put("imgA_2",  list.get(i).get("picturePath")+"\\"+"imgA_2.png");
	            parameterMap.put("imgB_1",  list.get(i).get("picturePath")+"\\"+"imgB_1.png");
//				parameterMap.put("imgA_1",  Constant.TEMPLETE_PATH+"imgA_1.png");
//				parameterMap.put("imgA_2",  Constant.TEMPLETE_PATH+"imgA_2.png");
//				parameterMap.put("imgB_1",  Constant.TEMPLETE_PATH+"imgB_1.png");
	            FileInputStream in1 = null;
	            FileInputStream in2	= null;
	            FileInputStream in3	= null;
				List<FileInputStream> ins = WorderToNewWordUtils.changePicture(doc, parameterMap,in1,in2,in3);
		        //解析替换文本段落对象
				WorderToNewWordUtils.changeText(doc, parameterMap);
				WorderToNewWordUtils.changeTable(doc, parameterMap,testList );

	            //生成新的word
				String startTime =JfreeChartUtils.dataFormat((Date)parameterMap.get("startTime"));
	            String outputUrl  = list.get(i).get("path")+"\\"+parameterMap.get("addressName")+"_"+parameterMap.get("gprsId")+"_"+startTime+"_电池整治报告.docx";
	           // String outputUrl = "E:\\ABC\\word\\"+parameterMap.get("gprsId")+"电池整治报告.docx";
	            File file = new File(outputUrl);
	            FileOutputStream stream = new FileOutputStream(file);
	            doc.write(stream);
	            stream.close();
	            if(CollectionUtils.isNotEmpty(ins)) {
	            	for (FileInputStream fileInputStream : ins) {
	            		fileInputStream.close();
					}
	            }
	            outPutUrls.add(outputUrl);
			}
			 	String outputUrlZip = "E:\\ABC\\word\\电池整治报告.zip";
	            //写zip文件
//	            File zipFile = new File(outputUrlZip);
//	            FileOutputStream zip = new FileOutputStream(zipFile);
//	            doc.write(zip);
//	            zip.close();
	            //compressZip(response,outPutUrls,outputUrlZip);//压缩文件
	           // downloadAsWordFile("电池整治报告.zip", zipFile);//下载压缩文件
	            deleteDir("D:/整治报告图片");//删除文件
	            outPutUrls.clear();
				ajaxResponse.setCode(Constant.RS_CODE_SUCCESS);
				ajaxResponse.setMsg(Constant.RS_MSG_SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			ajaxResponse.setMsg(e.getMessage());
		}
		return ajaxResponse;
		
	}
	

	
	/**
	 * 下载world-zip https://dell-pc/svn/DEV/station/trunk
	 * @param discharList 传递gprsid,放电结束时间
	 */
	@RequestMapping(value="downloadWordZip",method = RequestMethod.GET)
	@ResponseBody
	public AjaxResponse<List<DischargeManualRecord>> downloadAsWordFileZip(@RequestParam("ids") String ids){
		AjaxResponse<List<DischargeManualRecord>> ajaxResponse = new AjaxResponse<List<DischargeManualRecord>>(Constant.RS_CODE_ERROR,"请选择要下载电池组");
		if(ids.isEmpty()) {
			return ajaxResponse;
		}
		String[] split = ids.split(",|，");
		Integer[] idArray = new Integer[split.length];
		for (int i = 0 ; i<split.length;i++) {
			idArray[i] = Integer.parseInt(split[i]);
		}
		//通过ids查询对应数据
		List<DischargeManualRecord> discharList = dischargeAbstractRecordMapper.findByDischargeManualRecordIds(idArray);
		//获取下载的路径集合paths
		List<String> paths = new ArrayList<String>();
		if(CollectionUtils.isEmpty(discharList)) {
			ajaxResponse.setMsg("没有数据！");
			return ajaxResponse;
		}
		for (DischargeManualRecord dischargeManualRecord : discharList) {
			//循环保存路径
			String path = JfreeChartUtils.getParentPath(dischargeManualRecord);
			//paths.add("E:\\ABC\\word\\T0B124578电池整治报告.docx");
			paths.add(path+"\\"+dischargeManualRecord.getAddressName()+"_"+dischargeManualRecord.getGprsId()+"_"+JfreeChartUtils.dataFormat(dischargeManualRecord.getDischargeStartTime())+"电池整治报告.docx");
	
		}
		try {
			String name= "电池整治报告";
			String srcPath = Constant.TEMPLETE_PATH + name + ".docx";
			srcPath = URLDecoder.decode(srcPath, "UTF-8");
			CustomXWPFDocument doc = null;
	        OPCPackage pack = POIXMLDocument.openPackage(srcPath);  
	        doc = new CustomXWPFDocument(pack); 
		 	String outPutUrlZip = "E:\\ABC\\电池整治报告.zip";
            //写zip文件
            File zipFile = new File(outPutUrlZip);
            FileOutputStream zip = new FileOutputStream(zipFile);
            doc.write(zip);
            zip.close();
			compressZip(response,paths,outPutUrlZip);
			downloadAsWordFile("电池整治报告.zip", zipFile);//下载压缩文件
			ajaxResponse.setMsg("下载电池整治报告成功");
			ajaxResponse.setCode(Constant.RS_CODE_SUCCESS);
		} catch (Exception e) {
			ajaxResponse.setMsg("下载电池整治报告出错");
		}
		return ajaxResponse;
		
	}
	
	
	/**
	 * 打包成zip
	 * @param response
	 * @param paths 需要打包的路径
	 * @param outPutUrlZip 打包后输出的zip文件
	 * @throws IOException
	 */
	public void compressZip(HttpServletResponse response, List<String> paths,String outPutUrlZip) throws IOException {

		File zipFile = new File(outPutUrlZip);
		ZipOutputStream zipStream = null;
		FileInputStream zipSource = null;
		BufferedInputStream bufferStream = null;
		try {
			// 构造最终压缩包的输出流
			zipStream = new ZipOutputStream(new FileOutputStream(zipFile));
			for (int i = 0; i < paths.size(); i++) {
				File file = new File(paths.get(i));
				// 将需要压缩的文件格式化为输入流
				zipSource = new FileInputStream(file);
				// 压缩条目不是具体独立的文件，而是压缩包文件列表中的列表项，称为条目，就像索引一样
				ZipEntry zipEntry = new ZipEntry(file.getName());
				zipStream.putNextEntry(zipEntry);
				// 输入缓冲流
				bufferStream = new BufferedInputStream(zipSource, 1024 * 10);
				int read = 0;
				// 创建读写缓冲区
				byte[] buf = new byte[1024 * 10];
				while ((read = bufferStream.read(buf, 0, 1024 * 10)) != -1) {
					zipStream.write(buf, 0, read);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 关闭流
			try {
				if (null != bufferStream)
					bufferStream.close();
				if (null != zipStream)
					zipStream.close();
				if (null != zipSource)
					zipSource.close();
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * 下载后删除指定目录下面的文件夹
	 * @param path 指定的目录
	 * @return
	 */
	 public boolean deleteDir(String path){  
	        File file = new File(path);  
	        if(!file.exists()){//判断是否待删除目录是否存在  
	            System.err.println("The dir are not exists!");  
	            return false;  
	        }  
	        String[] content = file.list();//取得当前目录下所有文件和文件夹  
	        for(String name : content){  
	            File temp = new File(path, name);  
	            if(temp.isDirectory()){//判断是否是目录  
	                deleteDir(temp.getAbsolutePath());//递归调用，删除目录里的内容  
	                temp.delete();//删除空目录  
	            }else{  
	                if(!temp.delete()){//直接删除文件  
	                	FileUtils.deleteQuietly(file);
	                    System.err.println("Failed to delete " + name);  
	                }  
	            }  
	        }  
	        return true;  
	    }  
	

}
