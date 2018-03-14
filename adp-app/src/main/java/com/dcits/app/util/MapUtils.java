package com.dcits.app.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.dcits.app.data.DataObject;

/**
 * Map操作工具类
 * 
 * @date 2014-12-10
 * @author wangtqa
 * 
 */
public class MapUtils {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void copy(Map fromMap, Map toMap) {
		Iterator<Map.Entry<String, String>> it = fromMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			toMap.put(entry.getKey(), entry.getValue());
		}
	}

	/*
	 * 按照指定的列复制map isAutoCol 为true 自动补齐没有的列 false 不处理 cols 需要处理的列 默认用,返利
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void copy(Map fromMap, Map toMap, String cols,
			boolean isAutoCol) {
		String[] colsp = cols.split(",");
		List<String> colList = Arrays.asList(colsp);
		for (String col : colList) {
			col = col.trim();
			if (fromMap == null || fromMap.get(col) == null) {
				if (isAutoCol) {
					toMap.put(col, null);
				}
			} else {
				toMap.put(col, fromMap.get(col));
			}
		}
	}

	/*
	 * 按照指定的列复制map isAutoCol 为true 自动补齐没有的列 false 不处理 cols 需要处理的列 默认用,返利
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void copy(DataObject fromdataObject, Map toMap, String cols) {
		String[] colsp = cols.split(",");
		List<String> colList = Arrays.asList(colsp);
		for (String col : colList) {
			col = col.trim();
			if (fromdataObject != null
					&& fromdataObject.getItemValue(col) != null) {
				toMap.put(col, fromdataObject.getItemValue(col));
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List add(List toList, String key, String value) {
		List outList = new ArrayList();
		if (CollectionUtils.isNotEmpty(toList)) {
			for (int i = 0; i < toList.size(); i++) {
				Map map = (Map) toList.get(i);
				map.put(key, value);
				outList.add(map);
			}
		}
		return outList;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List add1(List toList, String key, Date date) {
		List outList = new ArrayList();
		if (CollectionUtils.isNotEmpty(toList)) {
			for (int i = 0; i < toList.size(); i++) {
				Map map = (Map) toList.get(i);
				map.put(key, date);
				outList.add(map);
			}
		}
		return outList;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List add(List toList, Map fromMap) {
		List outList = new ArrayList();
		if (CollectionUtils.isNotEmpty(toList)) {
			for (int i = 0; i < toList.size(); i++) {
				Map map = (Map) toList.get(i);
				copy(fromMap, map);
				outList.add(map);
			}
		}
		return outList;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List remove(List toList, String key, String obj) {
		List outList = new ArrayList();
		if (CollectionUtils.isNotEmpty(toList)) {
			for (int i = 0; i < toList.size(); i++) {
				Map map = (Map) toList.get(i);
				if (!StringUtils.equals((String) map.get(key), obj)) {
					outList.add(map);
				}
			}
		}
		return outList;
	}

	@SuppressWarnings({ "rawtypes" })
	public static Map filter(List toList, String key, String obj) {
		Map rtnmap = new HashMap();
		if (CollectionUtils.isNotEmpty(toList)) {
			for (int i = 0; i < toList.size(); i++) {
				Map map = (Map) toList.get(i);
				if (StringUtils.equals((String) map.get(key), obj)) {
					return map;
				}

			}
		}
		return rtnmap;
	}

	@SuppressWarnings({ "rawtypes" })
	public static Object filter(List toList, String key, String obj,
			String filtercol) {
		if (CollectionUtils.isNotEmpty(toList)) {
			for (int i = 0; i < toList.size(); i++) {
				Map map = (Map) toList.get(i);
				if (StringUtils.equals((String) map.get(key), obj)) {
					return map.get(filtercol);
				}

			}
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	public static void check(List toList) {
		if (CollectionUtils.isNotEmpty(toList)) {
			for (int i = 0; i < toList.size(); i++) {
				Map map = (Map) toList.get(i);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fill(Map map, Object obj) {
		Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			if (entry.getValue() == null || entry.getValue() == "null") {
				map.put(entry.getKey(), obj);
			}

		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fill(Map map, Object obj, String filtercol) {
		String[] colsp = filtercol.split(",");
		Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			if (entry.getValue() == null || entry.getValue() == "null") {
				if (!isExists(colsp, (String) entry.getKey())) {
					map.put(entry.getKey(), obj);
				}
			}

		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fill(List list, Object obj) {
		List<Map> allList = list;
		for (Map map : allList) {
			Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> entry = it.next();
				if (entry.getValue() == null || entry.getValue() == "null") {
					map.put(entry.getKey(), obj);
				}

			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fill(List list, Object obj, String filtercol) {
		String[] colsp = filtercol.split(",");
		List<Map> allList = list;
		for (Map map : allList) {
			Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> entry = it.next();
				if (entry.getValue() == null || entry.getValue() == "null") {
					if (!isExists(colsp, (String) entry.getKey())) {
						map.put(entry.getKey(), obj);
					}

				}

			}
		}
	}

	private static boolean isExists(String[] colsp, String filtercol) {
		for (int i = 0; i < colsp.length; i++) {
			if (StringUtils.equals(colsp[i], filtercol)) {
				return true;
			}
		}
		return false;
	}
}