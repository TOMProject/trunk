package com.station.moudles.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TreeMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.station.common.Constant;
import com.station.common.utils.RowParseHelper;
import com.station.common.utils.StringUtils;
import com.station.moudles.entity.DischargeManualRecord;
import com.station.moudles.service.DischargeManualRecordService;
import com.station.moudles.vo.AjaxResponse;
import com.station.moudles.vo.ShowPage;
import com.station.moudles.vo.search.SearchDischargeManualRecordPagingVo;

/**
 * 
 * 手动放电摘要数据
 * @author ywg
 *
 */
@Controller
@RequestMapping(value = "/dischargeManualRecord")
public class DischargeManualRecordController extends BaseController{

	@Autowired
	DischargeManualRecordService DischargeManualRecordSer;
	
	/**
	 * 手动放电记录分页查询
	 * @param searchDischargeManualRecordPagingVo
	 * @return
	 */
	@RequestMapping(value="listPage",method = RequestMethod.POST)
	@ResponseBody
	public AjaxResponse<ShowPage<DischargeManualRecord>> getDischargeManualListPage(@RequestBody SearchDischargeManualRecordPagingVo searchDischargeManualRecordPagingVo){
		if(StringUtils.isNotEmpty(searchDischargeManualRecordPagingVo.getAddressName())) {
			searchDischargeManualRecordPagingVo.setAddressName("%"+searchDischargeManualRecordPagingVo.getAddressName()+"%");
		}else {
			searchDischargeManualRecordPagingVo.setAddressName(null);
		}
		List<DischargeManualRecord> dischargeManualRecordList = DischargeManualRecordSer.selectListSelectivePaging(searchDischargeManualRecordPagingVo);

		ShowPage<DischargeManualRecord> page = new ShowPage<>(searchDischargeManualRecordPagingVo, dischargeManualRecordList);
		AjaxResponse<ShowPage<DischargeManualRecord>> ajaxResponse = new AjaxResponse<ShowPage<DischargeManualRecord>>(page);
		return ajaxResponse;
		
	}
	/**
	 * 获取手动放电数详情
	 * @param id
	 * @return
	 */
	
	@RequestMapping(value="dischargeDetail/{id}",method = RequestMethod.POST)
	@ResponseBody
	public AjaxResponse<DischargeManualRecord> getDischargeManualDetail(@PathVariable Integer id){
		AjaxResponse<DischargeManualRecord> ajaxResponse = new AjaxResponse<DischargeManualRecord>(Constant.RS_CODE_ERROR,"没有设置id");
		if(StringUtils.isNull(id)) {
			return ajaxResponse;
		}
		DischargeManualRecord dischargeManualRecor = DischargeManualRecordSer.selectDischargeDetail(id);
		ajaxResponse.setCode(Constant.RS_CODE_SUCCESS);
		ajaxResponse.setMsg("获取放电详情成功");
		ajaxResponse.setData(dischargeManualRecor);
		return ajaxResponse;
		
	}

	@RequestMapping(value="list",method = RequestMethod.POST)
	@ResponseBody
	public AjaxResponse<List<DischargeManualRecord>> getDischargeManualList(@RequestBody DischargeManualRecord dischargeManualRecord){
		List<DischargeManualRecord> dischargeManualRecordList = DischargeManualRecordSer.selectListSelective(dischargeManualRecord);
		AjaxResponse<List<DischargeManualRecord>> ajaxResponse = new AjaxResponse<List<DischargeManualRecord>>(Constant.RS_CODE_SUCCESS,"查询成功", dischargeManualRecordList);
		return ajaxResponse;
		
	}

	/**
	 * 手动放电摘要文件导入
	 * 
	 */
	@ResponseBody
	@RequestMapping(value = "/fileImportDischarge", method = RequestMethod.POST)
	public AjaxResponse<Object> fileImportCell(@RequestParam MultipartFile file, Integer companyId)
			throws IOException, EncryptedDocumentException, InvalidFormatException {
		AjaxResponse<Object> ajaxResponse = new AjaxResponse<Object>(Constant.RS_CODE_ERROR, "文件导入失败");
		request.setAttribute(Constant.ERROR_REQUEST, ajaxResponse);
		if (!file.isEmpty()) {
			InputStream in = null;
			OutputStream out = null;
			File dir = new File("tmpFiles");
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File serverFile = new File(dir.getAbsolutePath() + File.separator + System.currentTimeMillis());
			in = file.getInputStream();
			out = new FileOutputStream(serverFile);
			byte[] b = new byte[1024];
			int len = 0;
			while ((len = in.read(b)) > 0) {
				out.write(b, 0, len);
			}
			out.close();
			in.close();
			logger.info("Server File Location=" + serverFile.getAbsolutePath());
			dischargeFile(serverFile, ajaxResponse, companyId);
			return ajaxResponse;
		} else {
			ajaxResponse.setMsg("文件为空！");
			return ajaxResponse;
		}
	}
	
	public boolean dischargeFile(File file, @SuppressWarnings("rawtypes") AjaxResponse ajaxResponse, Integer companyId)
			throws EncryptedDocumentException, FileNotFoundException, InvalidFormatException, IOException {
		// 导入巡检记录，问题行详细记录信息
		TreeMap<Integer, String> errorRow = new TreeMap<>();
		try {
			int rowNum = 1;
			int successCount = 0;
			InputStream inp = new FileInputStream(file);
			Workbook wb = WorkbookFactory.create(inp);
			Sheet sheet = wb.getSheetAt(0);
			for (Row row : sheet) {
				rowNum = row.getRowNum();
				if (rowNum >= 4) {
					if (!RowParseHelper.hasData(row, 18)) {
						// 解析当前行有没有数据
						break;
					}
					try {
						DischargeManualRecordSer.dischargeExcelFile(row,wb);
						successCount++;
					} catch (Exception e) {
						
						errorRow.put(rowNum + 1, e.getMessage());
					}
				}
			}
			StringBuffer sb = new StringBuffer();
			if (errorRow.size() > 0) {
				for (Entry<Integer, String> et : errorRow.entrySet()) {
					String error = "第" + et.getKey() + "行，" + et.getValue() + "\r\n";
					sb.append(error);
				}
			}
			ajaxResponse.setCode(Constant.RS_CODE_SUCCESS);
			ajaxResponse.setMsg("记录导入处理完成，共导入" + (successCount) + "行数据\r\n" + sb.toString());
			logger.info("parse DischargeManualRecord excel file over!");
		} catch (Exception e) {
			ajaxResponse.setCode(Constant.RS_CODE_ERROR);
			StringBuffer sb = new StringBuffer();
			if (errorRow.size() > 0) {
				for (Entry<Integer, String> et : errorRow.entrySet()) {
					String error = "第" + et.getKey() + "行，" + et.getValue() + "\r\n";
					sb.append(error);
				}
			}
			ajaxResponse.setMsg("记录导入失败\r\n" + sb.toString());
			ajaxResponse.getMsg();
			logger.error("记录导入出错-->", e.getMessage());
		}
		return true;
	}
	
}
