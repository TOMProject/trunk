package com.station.app.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.station.common.Constant;
import com.station.moudles.controller.BaseController;
import com.station.moudles.entity.RoutingInspectionStationDetail;
import com.station.moudles.service.CellInfoService;
import com.station.moudles.service.GprsConfigInfoService;
import com.station.moudles.service.ModifyGprsidSendService;
import com.station.moudles.service.PackDataInfoLatestService;
import com.station.moudles.service.RoutingInspectionDetailService;
import com.station.moudles.service.RoutingInspectionsService;
import com.station.moudles.service.StationInfoService;
import com.station.moudles.service.SubDeviceService;
import com.station.moudles.vo.AjaxResponse;

import io.swagger.annotations.ApiOperation;

@Controller
@RequestMapping(value = "/app/submit")
public class AppSubmitControll extends BaseController {

	@Autowired
	GprsConfigInfoService gprsConfigInfoSer;
	@Autowired
	StationInfoService stationInfoSer;
	@Autowired
	CellInfoService cellInfoSer;
	@Autowired
	PackDataInfoLatestService packDataInfoLatestSer;
	@Autowired
	RoutingInspectionsService routingInspectionsSer;
	@Autowired
	RoutingInspectionDetailService routingInspectionDetailSer;
	@Autowired
	SubDeviceService SubDeviceSer;
	@Autowired
	ModifyGprsidSendService modifyGprsidSendSer;
//	@Autowired
//	RedisClientTemplate template;125422
	/**
	 * app提交信息保存在routing_inspections表和routiing_inspection_deteail表
	 */
	@RequestMapping(value = "/saveInfo", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "保存app提交的信息", notes = "保存app提交的信息")
	public AjaxResponse<RoutingInspectionStationDetail> saveRoutingInspection(@RequestBody RoutingInspectionStationDetail routingInspectionStationDetail) {
		if (routingInspectionStationDetail == null) {
			return new AjaxResponse<RoutingInspectionStationDetail>(Constant.RS_CODE_ERROR, "请选择提交项！");
		}
		AjaxResponse<RoutingInspectionStationDetail> ajaxResponse =  new AjaxResponse<RoutingInspectionStationDetail>(Constant.RS_CODE_SUCCESS, "提交成功！");

		try {
			AjaxResponse<RoutingInspectionStationDetail> Response = routingInspectionDetailSer.appSaveInfo(routingInspectionStationDetail);	
		} catch (Exception e) {
			ajaxResponse.setCode(Constant.RS_CODE_ERROR);
			ajaxResponse.setMsg(e.getMessage());
			return ajaxResponse;
		}
		return ajaxResponse;
	}
	
	//304测试接口
	@RequestMapping(value = "/304Test/{routingId}", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "保存app提交的信息", notes = "保存app提交的信息")
	public AjaxResponse<RoutingInspectionStationDetail> saveRoutingInspection1(@RequestBody RoutingInspectionStationDetail routingInspectionStationDetail) {
		if (routingInspectionStationDetail == null) {
			return new AjaxResponse<RoutingInspectionStationDetail>(Constant.RS_CODE_ERROR, "请选择提交项！");
		}
		AjaxResponse<RoutingInspectionStationDetail> ajaxResponse =  new AjaxResponse<RoutingInspectionStationDetail>(Constant.RS_CODE_SUCCESS, "提交成功！");

		try {
			
			if(routingInspectionStationDetail.getCompanyId1()==6) {
				//JetagUtils.delStationInfoJetage(request,template);
			}
		
			//AjaxResponse<RoutingInspectionStationDetail> Response = routingInspectionDetailSer.appSaveInfo(routingInspectionStationDetail);	
		} catch (Exception e) {
			ajaxResponse.setCode(Constant.RS_CODE_ERROR);
			ajaxResponse.setMsg(e.getMessage());
			return ajaxResponse;
		}
		return ajaxResponse;
	}
	
}