  package com.station.app.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.station.common.Constant;
import com.station.common.utils.ReflectUtil;
import com.station.moudles.controller.BaseController;
import com.station.moudles.entity.GprsConfigInfo;
import com.station.moudles.entity.GprsDeviceType;
import com.station.moudles.entity.PackDataInfoLatest;
import com.station.moudles.entity.Parameter;
import com.station.moudles.entity.RoutingInspectionStationDetail;
import com.station.moudles.service.GprsConfigInfoService;
import com.station.moudles.service.GprsDeviceTypeService;
import com.station.moudles.service.ModifyGprsidSendService;
import com.station.moudles.service.PackDataInfoLatestService;
import com.station.moudles.service.ParameterService;
import com.station.moudles.service.SubDeviceService;
import com.station.moudles.vo.AjaxResponse;

import io.swagger.annotations.ApiOperation;

@Controller
@RequestMapping(value = "/app/subdevice")
public class AppSubdeviceController extends BaseController {
	@Autowired
	PackDataInfoLatestService packDataInfoLatestSer;
	@Autowired
	SubDeviceService subDeviceSer;
	@Autowired
	ModifyGprsidSendService modifyGprsidSendSer;
	@Autowired
	ParameterService paramterSer;
	@Autowired
	GprsConfigInfoService gprsConfigInfoSer;
	@Autowired
	GprsDeviceTypeService gprsDeviceTypeSer;

	@RequestMapping(value = "/subDeviceAndCellInfo/{gprsId}", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "根据gprsid得到从机信息(目前只能得到电压)", notes = "返回数据")
	public AjaxResponse<Map> getSubdeviceInfo(@PathVariable String gprsId) {
		AjaxResponse<Map> ajaxResponse = new AjaxResponse<Map>(Constant.RS_CODE_ERROR, "获取失败！");
		PackDataInfoLatest queryPdi = new PackDataInfoLatest();
		queryPdi.setGprsId(gprsId);
		GprsConfigInfo gprsConfigInfo = new GprsConfigInfo();
		gprsConfigInfo.setGprsId(gprsId);
		List<GprsConfigInfo> gprsList = gprsConfigInfoSer.selectListSelective(gprsConfigInfo);
		if(gprsList.size() == 0) {
			return ajaxResponse;
		}
		Parameter  par = new Parameter();
		par.setParameterCategory(gprsList.get(0).getDeviceType().toString());
		
		par.setParameterCode("subMaxVol");
		List<Parameter> subMaxVol = paramterSer.selectListSelective(par);
		String maxVol =subMaxVol.size() == 0 ? null : subMaxVol.get(0).getParameterValue();
		
		par.setParameterCode("subMinVol");
		List<Parameter> subMinVol = paramterSer.selectListSelective(par);
		String minVol = subMinVol.size() == 0 ? null :subMinVol.get(0).getParameterValue();
		if(maxVol == null || minVol == null) {
			ajaxResponse.setMsg("巡检app下，从机异常参数没有设置！");
			return ajaxResponse;
		}
		BigDecimal maxV = new BigDecimal(maxVol);
		BigDecimal minV = new BigDecimal(minVol);
		
		//查询单体的电压范围
//		GprsDeviceType gprsDeviceType = new GprsDeviceType();
//		gprsDeviceType.setTypeCode(gprsList.get(0).getDeviceType());
//		List<GprsDeviceType> deviceType = gprsDeviceTypeSer.selectListSelective(gprsDeviceType);
		//通过电压平台赋值个parameterCategory
		par.setParameterCategory(gprsList.get(0).getVolLevel().toString());
		par.setParameterCode("chargeMaxVol");
		List<Parameter> cellMaxVol = paramterSer.selectListSelective(par);
		String cellMaxV  = cellMaxVol.size() == 0 ? null : cellMaxVol.get(0).getParameterValue();
		
		par.setParameterCode("chargeMinVol");
		List<Parameter> cellMixVol = paramterSer.selectListSelective(par);
		String cellMixV = cellMixVol.size() == 0 ? null : cellMixVol.get(0).getParameterValue();
		if(cellMixV == null || cellMaxV == null ) {
			ajaxResponse.setMsg("巡检app下，浮充下电池异常值没有设置！");
			return ajaxResponse;
		}
		//查询出从机的电压
		List<PackDataInfoLatest> pdiList = packDataInfoLatestSer.selectListSelective(queryPdi);
		PackDataInfoLatest pdi = null;
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("linkStatus", gprsList.get(0).getLinkStatus());
		map.put("minVol", cellMixV);
		map.put("maxVol", cellMaxV);
		List<Map> volList = new ArrayList<>();
		//兼容检测设备更改
		Integer subCount = gprsList.get(0).getSubDeviceCount();
		if (pdiList.size() > 0) {
			pdi = pdiList.get(0);
			for (int i = 1; i < subCount+1; i++) {
				Map<String,Object> entity = new HashMap<>();
				entity.put("isNormal", true);
				Object obj = ReflectUtil.getValueByKey(pdi, "cellVol" + i);
				String cellVol = obj == null ? "0.000" : obj.toString();
				entity.put("vol", cellVol);
				//判断是否再正常电压范围内
				BigDecimal vol = new BigDecimal(cellVol);
				if(vol.compareTo(maxV) == 1 || minV.compareTo(vol) == 1) {
					entity.put("isNormal", false);
				}
				volList.add(entity);
			}
		}else {
			for (int i = 1; i < subCount+1; i++) {
				Map<String,Object> entity = new HashMap<>();
				entity.put("vol", null);
				entity.put("isNormal", false);
				volList.add(entity);
			}
		}
		map.put("vols", volList);
		ajaxResponse.setData(map);
		ajaxResponse.setCode(Constant.RS_CODE_SUCCESS);
		ajaxResponse.setMsg("获取成功！");
		return ajaxResponse;
	}
	/**
	 * 修改从机id  参数 station_id、 gprs_id、 operate_type、 old、 new 、OperateId、operateName
	 * @param appSubdevice
	 * @return
	 */
	@RequestMapping(value = "/updateSubDeviceId", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation(value = "根据pk更新从机设备", notes = "根据pk更新从机设备")
	public AjaxResponse<Object> updateSubDeviceId(@RequestBody RoutingInspectionStationDetail routingInspectionStationDetail) {
		AjaxResponse<Object> ajaxResponse = new AjaxResponse<Object>(Constant.RS_CODE_ERROR, "修改从机设备出错！");
		try {
			subDeviceSer.updateSubDevice(routingInspectionStationDetail,0);
			ajaxResponse.setCode(Constant.RS_CODE_SUCCESS);
			ajaxResponse.setMsg("修改从机指令发送成功！");
			
		} catch (Exception e) {
			ajaxResponse.setCode(Constant.RS_CODE_ERROR);
			ajaxResponse.setMsg(e.getMessage());
		}
		return ajaxResponse;
	}

}
