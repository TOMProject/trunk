package com.station.moudles.service;

import java.util.List;
import java.util.Map;

import com.station.moudles.entity.Parameter;
import com.station.moudles.vo.AppConfigVo;

public interface ParameterService extends BaseService<Parameter, String> {

	void updateParameterAll(AppConfigVo appConfig,String parameterCategory) throws IllegalArgumentException, IllegalAccessException;
	
	/**
	 * 后期设置参数，都通过前台设置
	 */
	void parameterConsole(List<Parameter> list);
	
	Parameter selectByPrimaryKeys(String parameterCode, String parameterCategory);
	
	List<Parameter> selectByCategroyAndCodes(List<String> codes, String parameterCategory);
}