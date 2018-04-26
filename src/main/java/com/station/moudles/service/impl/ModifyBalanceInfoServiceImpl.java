package com.station.moudles.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.station.common.utils.ReflectUtil;
import com.station.moudles.entity.ModifyBalanceSend;
import com.station.moudles.mapper.ModifyBalanceSendMapper;
import com.station.moudles.service.ModifyBalanceInfoService;
@Service
public class ModifyBalanceInfoServiceImpl  extends BaseServiceImpl<ModifyBalanceSend, Integer> 
	implements ModifyBalanceInfoService{
	
	@Autowired
	ModifyBalanceSendMapper modifyBalanceSendMapper;
	
	@Override
	public boolean insertParaNonNull(ModifyBalanceSend balanceSend) {
		if (balanceSend == null) {
			return false;
		}
		List<Object> paraList = ReflectUtil.getValueByStartsWith(balanceSend, "para");
		for (Object object : paraList) {
			if (object == null) {
				return false;
			}
		}
		modifyBalanceSendMapper.insertSelective(balanceSend);
		return true;
	}

}
