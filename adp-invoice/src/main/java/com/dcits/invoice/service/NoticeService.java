package com.dcits.invoice.service;

import java.util.HashMap;
import java.util.Map;

import com.dcits.app.dao.DataWindow;
import com.dcits.app.data.DataObject;

public class NoticeService {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DataObject getNotice(DataObject dataObject) {
		Map map = dataObject.getMap();

		String mname = map.get("mname").toString();
		Map parameter = new HashMap<String, String>();
		parameter.put("mname", mname);
		String SQL = "invoice.service.invoiceService_selectMessage";
		return new DataObject(DataWindow.queryOne(SQL, parameter));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DataObject updateNotice(DataObject dataObject) {
		Map map = dataObject.getMap();
		Map map1 = new HashMap<String, String>();

		String mname = map.get("mname").toString();
		String mbody = map.get("mbody").toString();
		Map parameter = new HashMap<String, String>();
		parameter.put("mname", mname);
		int total = DataWindow.getTotal("invoice.service.invoiceService_queryMessage", parameter);
		parameter.put("mbody", mbody);
		if (total == 1) {
			DataWindow.insert("invoice.service.invoiceService_updateMessage", parameter);
			map1.put("czjg", "更改成功");
		} else if (total > 1) {
			DataWindow.delete("invoice.service.invoiceService_deleteMessage", parameter);
			DataWindow.insert("invoice.service.invoiceService_addMessage", parameter);
			map1.put("czjg", "更改成功");
		} else {
			DataWindow.insert("invoice.service.invoiceService_addMessage", parameter);
			map1.put("czjg", "插入成功");
		}
		return new DataObject(map1);
	}

}
