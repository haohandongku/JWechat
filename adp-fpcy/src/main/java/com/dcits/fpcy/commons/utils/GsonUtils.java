package com.dcits.fpcy.commons.utils;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtils {
	/** GSON缓存 */
	private static Map<Long, Gson> gsonCache = new HashMap<Long, Gson>();

	public static String tojson(Object obj) {
		Gson gson=GsonUtils.getGson();
		// 将对象编译成json
		String result = gson.toJson(obj);
		return result;
	}

	public static Object toObject(String m, Class<?> beanClass) {
		Gson gson=GsonUtils.getGson();
		m=m.replace("\\", "");//去掉'/'
		// 将json编译成对象
		Object obj = gson.fromJson(m, beanClass);
		return obj;
	}

	/**
	 * 返回每个线程的gson解析器
	 * 
	 * @return
	 */
	public static Gson getGson() {
		long id = Thread.currentThread().getId();
		Gson gson = gsonCache.get(id);
		if (gson == null) {
			GsonBuilder g = new GsonBuilder();
			g = g.disableHtmlEscaping(); // 禁用HTML转义
			gson = g.create();
			gsonCache.put(id, gson);
		}
		return gson;
	}
}
