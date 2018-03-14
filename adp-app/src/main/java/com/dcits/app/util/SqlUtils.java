package com.dcits.app.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;

import com.dcits.app.dao.DataWindowBasic;

public class SqlUtils {

	public static Map<String, String> getResultColumnNamesMap(String key) {
		List<String> list = getResultColumnNames(key);
		Map<String, String> map = new HashMap<String, String>();
		if (CollectionUtils.isNotEmpty(list)) {
			for (String str : list) {
				map.put(str, "");
			}
		}
		return map;
	}

	@SuppressWarnings("rawtypes")
	public static List<Map> getResultColumnNamesList(String key) {
		List<Map> list = new ArrayList<Map>();
		list.add(getResultColumnNamesMap(key));
		return list;
	}

	public static List<String> getResultColumnNames(String key) {
		List<String> list = new ArrayList<String>();
		String sql = getSql(key).replaceAll("[\\t\\n\\r]", "");
		String reg = "\\s*select\\s+(.+)from\\s+";
		Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sql);
		while (matcher.find()) {
			String str = matcher.group(1);
			if (str != null) {
				String[] columnArray = str.split(",");
				if (columnArray != null && columnArray.length > 0) {
					for (String column : columnArray) {
						String temp = column.trim().toUpperCase();
						if (temp.indexOf("AS") != -1) {
							list.add(temp.substring(temp.indexOf("AS") + 2)
									.trim());
						} else if (temp.indexOf(" ") != -1) {
							list.add(temp.substring(temp.indexOf(" ") + 1)
									.trim());
						} else if (temp.indexOf(".") != -1) {
							list.add(temp.substring(temp.indexOf(".") + 1)
									.trim());
						} else {
							list.add(temp);
						}
					}
				}
			}
		}
		return list;
	}

	private static String getSql(String key) {
		DataWindowBasic dataWindowBasic = new DataWindowBasic();
		return dataWindowBasic.getSql(key);
	}

}