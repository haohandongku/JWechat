package com.dcits.app.util;

import java.io.Serializable;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dcits.app.constant.CacheName;
import com.dcits.app.data.DataObject;

public class CacheUtils {

	private static Cache cache1 = (Cache) ApplicationContextUtils.getContext()
			.getBean(CacheName.lsCache01 + "Factory");
	private static Cache cache2 = (Cache) ApplicationContextUtils.getContext()
			.getBean(CacheName.lsCache02 + "Factory");

	private static final Log LOG = LogFactory.getLog(CacheUtils.class);

	public static Object getCacheValue(String cacheName, String className,
			String methodName, DataObject dataObject) {
		String cacheKey = getCacheKey(className, methodName, dataObject);
		Element element = null;
		if (CacheName.lsCache01.equals(cacheName)) {
			element = cache1.get(cacheKey);
		} else if (CacheName.lsCache02.equals(cacheName)) {
			element = cache2.get(cacheKey);
		}
		if (element == null) {
			return null;
		} else {
			LOG.debug("从缓存：" + cacheName + "中获取：" + cacheKey);
			return element.getValue();
		}
	}

	public static void putCacheValue(String cacheName, String className,
			String methodName, DataObject dataObject, Object object) {
		String cacheKey = getCacheKey(className, methodName, dataObject);
		if (CacheName.lsCache01.equals(cacheName)) {
			Element element = new Element(cacheKey, (Serializable) object);
			LOG.debug(cacheKey + "添加到缓存：" + cacheName);
			cache1.put(element);
		} else if (CacheName.lsCache02.equals(cacheName)) {
			Element element = new Element(cacheKey, (Serializable) object);
			LOG.debug(cacheKey + "添加到缓存：" + cacheName);
			cache2.put(element);
		}
	}

	public static void refreshCache() {
		cache1.removeAll();
		cache2.removeAll();
	}

	private static String getCacheKey(String className, String methodName,
			DataObject dataObject) {
		Object[] arguments = new Object[] { dataObject };
		StringBuffer sb = new StringBuffer();
		sb.append(className).append(".").append(methodName);
		if ((arguments != null) && (arguments.length != 0)) {
			for (int i = 0; i < arguments.length; i++) {
				sb.append(".").append(arguments[i]);
			}
		}
		return sb.toString();
	}

}
