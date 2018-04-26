/**
 * 
 */
package com.station.common;

/**
 * @author zdm
 *
 */
public class Constant {

	// session 标示
	public final static String SESSION_INN_ID = "inn_id";
	public final static String SESSION_USER_CODE = "user_code";
	public final static String SESSION_LOGIN_USER = "loginUser";

	// 返回json格式
	public final static String RS_MSG_SUCCESS = "操作成功";
	public final static String RS_MSG_ERROR = "操作错误";
	public final static String RS_CODE_SUCCESS = "0000";
	public final static String RS_CODE_ERROR = "0001";

	public static final String PREFIX_DELETED_ROOMTYPE = "已删除_";

	public final static String MEDIA_APPLICATION_JSON = "application/json;charset=UTF-8";

	public final static String STATUS_NORMAL = "0";
	public final static String STATUS_FORBIDEN = "1";
	public final static String STATUS_DEL = "2";

	public final static String ERROR_REQUEST = "ajaxResponse";

	public final static String TEMPLETE_PATH = Constant.class.getClassLoader().getResource("").getPath() + "templete/";
	//算平均内阻的保存在redis 中的key+gprsd
	public final static String AVGRESIST_PATH = "srv_stationDetaile_resist_";
	// 放电历史记录redis缓存的key前缀
	public final static String DISHISTORY_KEY_PREFIX = "srv_station_dishistory_";
}
