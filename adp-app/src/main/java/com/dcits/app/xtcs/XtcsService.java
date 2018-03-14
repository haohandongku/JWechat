package com.dcits.app.xtcs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dcits.app.constant.Constant;
import com.dcits.app.dao.DataWindow;
import com.dcits.app.data.DataObject;
import com.dcits.app.service.BaseService;

public class XtcsService extends BaseService {

	/**
	 * 查询列表
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DataObject getXtcs(DataObject dataObject) {
		Map paramMap = dataObject.getMap();
		paramMap.put("JGBM", getCurrentQybm());
		Map resMap = new HashMap();
		int total = DataWindow.getTotal(
				"app.xtcs.XtcsService_queryXtcsListTotal", paramMap);
		resMap.put("total", total);
		DataWindow data = DataWindow.query(
				"app.xtcs.XtcsService_queryXtcsList", paramMap,
				getPageNumber(paramMap), getPageSize(paramMap));
		resMap.put("rows", data.getList());
		return new DataObject(resMap);
	}

	/**
	 * 检查是否已经存在
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DataObject checkExist(DataObject dataObject) {
		Map parameter = dataObject.getMap();
		Map map = new HashMap();
		int total = DataWindow.getTotal("app.xtcs.XtcsService_checkExist",
				parameter);
		map.put("total", total);
		return new DataObject(map);
	}

	/**
	 * 保存
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DataObject saveXtcs(DataObject dataObject) {
		Map parameter = dataObject.getMap();
		parameter.put("LRR", getCurrentUserbm());
		parameter.put("JGBM", getCurrentQybm());
		boolean isUpdate = (Boolean) parameter.get("ISUPDATE");
		Map map = new HashMap();
		if (isUpdate) {
			DataWindow.update("app.xtcs.XtcsService_updateXtcs", parameter);
		} else {
			DataWindow.insert("app.xtcs.XtcsService_saveXtcs", parameter);
		}
		return new DataObject(map);
	}

	/**
	 * 删除
	 */
	@SuppressWarnings({ "rawtypes" })
	public DataObject deleteXtcs(DataObject dataObject) {
		Map parameter = dataObject.getMap();
		Map map = new HashMap();
		DataWindow.delete("app.xtcs.XtcsService_deleteXtcs", parameter);
		return new DataObject(map);
	}
	/**
	 * 初始化事务所参数
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DataObject initSwsCs(DataObject dataObject) {
	    Map parameter = new HashMap();
	    parameter.put("LRR", getCurrentUserbm());
	    parameter.put("QYBM", getCurrentQybm());
	    
	    Map delMap = new HashMap(parameter);
	    delMap.put("CSXH1", Constant.DEFAULT_JZGLKHDCS);
	    delMap.put("CSXH2", Constant.DEFAULT_JZGLZDGD);
	    DataWindow.delete("app.xtcs.XtcsService_delSwsXtcs", delMap);
	    
	    List<Map> list = new ArrayList<Map>();
	    Map req1 = new HashMap(parameter);
	    req1.put("CSXH", Constant.DEFAULT_JZGLKHDCS);
	    req1.put("CSMC", "是否使用工作底稿客户端工具");
	    req1.put("SYSM", "工作底稿客户端工具使用参数:Y使用,N不使用");
	    req1.put("CSNR", "N");
	    Map req2 = new HashMap(parameter);
	    req2.put("CSXH", Constant.DEFAULT_JZGLZDGD);
	    req2.put("CSMC", "鉴证底稿自动归档天数");
	    req2.put("SYSM", "鉴证底稿自动归档天数，默认‘90’天");
	    req2.put("CSNR", "90");
	    list.add(req1);
	    list.add(req2);
	    parameter.put("CSXH", getSequence());
	    DataWindow.insert("app.xtcs.XtcsService_initSwsXtcs", list);
	    return new DataObject();
	}
	
	

}