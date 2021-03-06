package com.station.moudles.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.station.moudles.entity.GprsConfigInfo;
import com.station.moudles.entity.RoutingInspectionStationDetail;
import com.station.moudles.entity.RoutingInspections;
import com.station.moudles.entity.StationDetail;
import com.station.moudles.entity.StationInfo;
import com.station.moudles.vo.AjaxResponse;
import com.station.moudles.vo.CommonSearchVo;
import com.station.moudles.vo.search.SearchStationInfoPagingVo;
import com.station.moudles.vo.search.SearchWarningInfoPagingVo;

public interface StationInfoService extends BaseService<StationInfo, Integer> {
	StationDetail getStationDetailByStationId(Integer stationId);

	boolean parseStationExcelFile(File file, AjaxResponse ajaxResponse, Integer companyId,HttpServletRequest request) throws FileNotFoundException, EncryptedDocumentException, InvalidFormatException, IOException;

	void pulseDischargeAsync(CommonSearchVo commonSearchVo);

	void createStationCells(StationInfo stationInfo,HttpServletRequest request);

	void updateByGprsSelective(StationInfo record);

	void updateByCompanyIdSelective(StationInfo record);

	String exportStationCheckToExcel(StationDetail stationDetail);

	List getStationStatusList(CommonSearchVo commonSearchVo);

	List getStationDurationStatusList(CommonSearchVo commonSearchVo);

	String exportStationStatusToExcel(Integer companyId, Integer companyLevel);

	StationDetail getStationDetailBasicByStationId(Integer stationId);

	StationDetail getStationDetailBasicByGprsId(String gprsId);
	//---add 10/31 统一事务修改
	
	void updateGprsAndCellAndStation(StationInfo stationInfo);
	
	//APP首页
	List<StationInfo>  appSelectListSelectivePaging(SearchStationInfoPagingVo searchStationInfoPagingVo);
	
	void bindingAddRoutingInspections(RoutingInspectionStationDetail routingInspectionsDetail);
	//查询电池组告警信息列表
  	List<StationInfo> appWarnAreaSelectListSelective(SearchWarningInfoPagingVo searchWarningInfoPagingVo);
  	//显示不经常改变的信息
  	StationDetail getStationDetailBasicInfoByStationId(Integer stationId);
  	//显示需要刷新的数据
  	StationDetail getStationDetailpackInfoByStationId(String gprsId);
  	//前台电池出列表
  	List<StationInfo> selectStationInfoList(StationInfo stationIfno);
  	//判断电压平台一致性
  	void judgeCellVolLevel(StationInfo stationInfo);
  	//前台第一个页面
  	List<StationInfo> selectStationInfoListFirst(StationInfo stationIfno);
  	
  	int selectListCountSelective(StationInfo record);
  	//返回昨天的自动特征测试的电池组
  	List<StationInfo> selectBatteryGroupWithYesterdayForIds(List<Integer> ids);
  	
  	//每天自动脉冲特征查询的电池组
  	List<StationInfo> selectBatteryGroupEveryDay(Map map);
}