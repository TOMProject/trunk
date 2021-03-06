package com.station.moudles.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.station.moudles.entity.User;
import com.station.moudles.vo.search.PageEntity;

public interface UserMapper extends BaseMapper<User, Integer> {
	//通过公司id修改公司信息
	void updateByCompanyIdSelective(User user);

}