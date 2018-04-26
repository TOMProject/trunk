package com.station.common.interceptor;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.station.common.Constant;
import com.station.common.redis.RedisClientTemplate;
import com.station.common.utils.JsonUtil;
import com.station.common.utils.MD5;
import com.station.moudles.vo.AjaxResponse;

/**
 * 定义拦截进入controller方法前
 * @author 
 *
 */
public class WebcacheInterceptor  implements  HandlerInterceptor {
	
//	@Autowired
	RedisClientTemplate template;
	
	public static final Logger logger = LoggerFactory.getLogger(WebcacheInterceptor.class);
	//执行在方法之前
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		 Base64.Decoder decoder = Base64.getDecoder();//解码
		 Base64.Encoder encoder = Base64.getEncoder();//加密
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String nowTime = sdf.format(new Date());
		
		AjaxResponse<Object> ajaxResponse = new AjaxResponse<Object>(Constant.RS_CODE_SUCCESS, "数据没有改变");
		//获得apiName
		String apiName = request.getRequestURI() ;
		
		//判断是存在header -->Jetag
		List<String> headers = new ArrayList<>();
		Enumeration headerNames = request.getHeaderNames();
		  while (headerNames.hasMoreElements()) {
		        String header = (String) headerNames.nextElement();
		        headers.add(header);
		    }
		  if(!headers.contains("jetag")) {
			  return true;
		  }

		//读取header中的jetag
		String jetag = request.getHeader("Jetag");
		if(jetag == null) {
//			response.setDateHeader("Last-Modified", new Date().getTime());//最後一次访问时间
//			response.setHeader("Cache-Control", "max-age=" + 300);
//			response.setDateHeader("Expires",new Date().getTime()+2000000); //过期时间
//			response.setHeader("Pragma", "Pragma"); //Pragma:设置页面是否缓存，为Pragma则缓
			return true;
		}
		if(jetag != null ) {
			//第一次请求没有(.)
			if(jetag.indexOf(".") == -1) {
				//解码
				byte[] textByte = jetag.getBytes("UTF-8");
				String jetagKey = new String(decoder.decode(textByte), "UTF-8");
				//MD5
				String md5Str = "Apiname."+jetagKey+"."+nowTime;
				String md5 = DigestUtils.md5DigestAsHex(md5Str.getBytes());
				//返回的jetag,不解密
				response.setHeader("Jetag", jetag+"."+md5);
				response.setHeader("Access-Control-Expose-Headers","Jetag");//设置页面拿到Jetag
				Map<String, String> map = new HashMap<String, String>();
				map.put(jetagKey, jetag+"."+md5);
				template.hmset("web.local."+apiName+".jetag",map);
				template.expire("web.local."+apiName+".jetag", 10*60*60*1000);
				logger.info("添加缓存api-->：web.local."+apiName+".jetag：{}",map);
				return true;
			}
			
			String[] jetagStr = jetag.split("\\.");
			byte[] Byte = jetagStr[0].getBytes("UTF-8");
			//解码
			String jetagkey = new String(decoder.decode(Byte), "UTF-8");
			//从redis中读取值(map)
			List<String> hmget = template.hmget("web.local."+apiName+".jetag", jetagkey);
			String reallyValue = null;
			if(hmget.size() != 0) {
				//通过key读取实际的值
				reallyValue = hmget.get(0);
			}

			if(reallyValue != null && reallyValue.equals(jetag)) {
				//返回304
				response.setStatus(response.SC_NOT_MODIFIED);
				response.setHeader("Content-type", "application/json;charset=UTF-8");
				response.setHeader("Access-Control-Allow-Credentials", "*");
				response.setHeader("Access-Control-Allow-Origin", "*");
				response.setHeader("Access-Control-Allow-Headers", "content-type,authorization");
				response.getWriter().write(JsonUtil.writeValueAsString(ajaxResponse));
				return false;
			}else {
				//加密
				byte[] enbyte = jetagkey.getBytes("UTF-8");
				String enjetag = new String(encoder.encode(enbyte),"UTF-8");
				//设置新的jetag返回给web
				String md5Str = "Apiname."+enjetag+"."+nowTime;
				String md5 = DigestUtils.md5DigestAsHex(md5Str.getBytes());
				response.setHeader("Jetag", enjetag+"."+md5);
				response.setHeader("Access-Control-Expose-Headers","Jetag");//设置页面拿到Jetag
				//保存在redis中 
				Map<String, String> map = new HashMap<String, String>();
				map.put(jetagkey, enjetag+"."+md5);
				template.hmset("web.local."+apiName+".jetag",map);
				template.expire("web.local."+apiName+".jetag", 10*60*60*1000);
				logger.info("添加缓存api-->：web.local."+apiName+".jetag：{}",map);
//				return true;
			}
		}
		return true;	
	}

	
	
	//执行在方法之后
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		
	}
	//最终执行的方法
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
