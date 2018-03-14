package com.dcits.app.util;

import java.util.HashMap;
import java.util.Map;

import com.dcits.app.dao.DataWindow;
import com.dcits.app.resource.RegexPropertyMessageResources;

public class DateUtils {

	public static final RegexPropertyMessageResources regexPropertyMessageResources = (RegexPropertyMessageResources) ApplicationContextUtils
	.getContext().getBean("propertyMessageResources");
	public static final String DBTYPE = regexPropertyMessageResources.getMessage("adp.dbType").toString()
			.toUpperCase();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String getDBTime() {
		Map map = new HashMap();
		map.put("DBTEYP", DBTYPE);
		return (String) DataWindow.queryOne("app.DateUtils_getDBtime", map);
	}

	public static String getDBdate() {
		return getDBTime().substring(0, 10);
	}

	public static int getYear(String date) {
		return Integer.parseInt(date.substring(0, 4));
	}

	public static int getMonth(String date) {
		return Integer.parseInt(date.substring(5, 7));
	}

	public static int getDay(String date) {
		return Integer.parseInt(date.substring(8, 10));
	}

}