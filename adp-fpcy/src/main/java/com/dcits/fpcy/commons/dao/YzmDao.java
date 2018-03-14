package com.dcits.fpcy.commons.dao;

import java.util.HashMap;
import java.util.Map;

import com.dcits.app.dao.DataWindow;

/**
 * 超级鹰等服务商
 * @author wuche
 *
 */
public class YzmDao {
	/**
	 * 根据swjg_dm和fp_zl查询单条数据
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Map saveYzmLog(Map paramter) {
		Map map = (Map) DataWindow.queryOne(
				"fpcy.common.dao.YzmDao_saveYzmLog", paramter);
		return map;
	}
	
	/**
	 * 保存请求日志
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Map saveRequestLog(Map paramter) {
	   Map resMap=new HashMap();	
	   DataWindow.insert(
				"fpcy.common.dao.YzmDao_saveRequestLog", paramter);
		return resMap;
	}
	
	/**
	 * 保存请求日志
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Map saveOcrRequestLog(Map paramter) {
	   Map resMap=new HashMap();	
	   DataWindow.insert(
				"fpcy.common.dao.YzmDao_saveOcrRequestLog", paramter);
		return resMap;
	}
	/**
	 * 更新请求日志
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Map updateOcrRequestLog(Map paramter) {
	   Map resMap=new HashMap();	
	   DataWindow.insert(
				"fpcy.common.dao.YzmDao_updateOcrRequestLog", paramter);
		return resMap;
	}
}
