package com.dcits.invoice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.dcits.app.dao.DataWindow;
import com.dcits.app.data.DataObject;
import com.dcits.app.service.BaseService;

public class RegionService extends BaseService {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DataObject updateEnableQuery(DataObject dataObject) {
		Map map = dataObject.getMap();
		Map map1 = new HashMap<String, String>();
		String swjg_dm = map.get("swjg_dm").toString();
		String fp_zl = map.get("fp_zl").toString();
		String poolid = map.get("poolid").toString();
		String query = map.get("query").toString();
		Map parameter = new HashMap<String, String>();
		parameter.put("swjg_dm", swjg_dm);
		parameter.put("fp_zl", fp_zl);
		parameter.put("poolid", poolid);
		String SQL = "invoice.service.invoiceService_queryEnableQuery";
		int total = DataWindow.getTotal(SQL, parameter);
		parameter.put("query", query);
		if (total == 1) {
			String SQL3 = "invoice.service.invoiceService_updateEnableQuery";
			DataWindow.insert(SQL3, parameter);
			map1.put("czjg", "更改成功");
		} else {
			String SQL1 = "invoice.service.invoiceService_addEnableQuery";
			DataWindow.insert(SQL1, parameter);
			map1.put("czjg", "插入成功");
		}
		return new DataObject(map1);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DataObject getEnableRegion(DataObject dataObject) throws JSONException {
		Map parameter = dataObject.getMap();
		Map rtnMap = new HashMap();
		DataWindow result = DataWindow.query("invoice.service.invoiceService_queryEnableQueryRegion", parameter);
		String qy = "qy";
		String query = "";
		String sz = "";
		List list = result.getList();
		List list1 = new ArrayList();
		List list2 = new ArrayList();
		JSONObject json1 = new JSONObject();
		for (int i = 0; i < list.size() + 1; i++) {
			if (i == list.size()) {
				qy = "qy";
			} else {
				JSONObject json = new JSONObject(list.get(i).toString());
				sz = json.getString("swjg_mc").toString();
				query = json.getString("query").toString();
			}
			/*
			 * if(sz.contains("深圳")){ System.out.println(1); }
			 */
			if (!sz.contains(qy)) {
				if (list1.size() != 0) {
					json1.put("list", list1);
					list2.add(json1);
					list1.clear();
					json1 = new JSONObject();
				}
				if (sz.contains("国税")) {
					qy = sz.substring(0, sz.indexOf("国"));
				} else if (sz.contains("地税")) {
					qy = sz.substring(0, sz.indexOf("地"));
				} else if (sz.contains("增值税")) {
					qy = sz.substring(0, sz.indexOf("增"));
				} else if (sz.contains("定额")) {
					qy = sz.substring(0, sz.indexOf("定"));
				}
				json1.put("key", qy);
			}
			list1.add(sz);
			list1.add(query);
		}
		rtnMap.put("rows", new JSONArray(list2).toString());
		return new DataObject(rtnMap);
	}
}
