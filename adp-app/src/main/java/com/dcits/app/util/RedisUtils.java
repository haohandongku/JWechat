package com.dcits.app.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.dcits.app.data.DataObject;

public class RedisUtils {

	private static final Log LOG = LogFactory.getLog(RedisUtils.class);
	private static JedisPool jedisPool = (JedisPool) ApplicationContextUtils
			.getContext().getBean("jedisPool");

	public static void putValue(String className, String methodName,
			DataObject dataObject, String value) {
		try {
			String cacheKey = getCacheKey(className, methodName, dataObject);
			Jedis jedis = jedisPool.getResource();
			LOG.debug("内容添加到redis缓存，key为：" + cacheKey);
			jedis.set(cacheKey, value);
			jedis.close();
		} catch (Throwable t) {
			LOG.error(t);
		}
	}

	public static void putValue(String className, String methodName,
			DataObject dataObject, String value, int surviveTime) {
		try {
			String cacheKey = getCacheKey(className, methodName, dataObject);
			Jedis jedis = jedisPool.getResource();
			LOG.debug("内容添加到redis缓存，key为：" + cacheKey);
			jedis.setex(cacheKey, surviveTime, value);
			jedis.close();
		} catch (Throwable t) {
			LOG.error(t);
		}

	}

	public static String getValue(String className, String methodName,
			DataObject dataObject) {
		try {
			String cacheKey = getCacheKey(className, methodName, dataObject);
			Jedis jedis = jedisPool.getResource();
			String res = null;
			res = jedis.get(cacheKey);
			jedis.close();
			if (res == null) {
				return null;
			} else {
				LOG.debug("从redis缓存中获取内容，key为：" + cacheKey + "值为：" + res);
				return res;
			}
		} catch (Throwable t) {
			LOG.error(t);
		}
		return null;
	}

	public static void delValue(String className, String methodName,
			DataObject dataObject) {
		try {
			String cacheKey = getCacheKey(className, methodName, dataObject);
			Jedis jedis = jedisPool.getResource();
			jedis.del(cacheKey);
			jedis.close();
		} catch (Throwable t) {
			LOG.error(t);
		}
	}

	public static void delValueByPatternKey(String pattern) {
		try {
			Set<String> keys = getPatternKeys(pattern);
			for (String key : keys) {
				delValueByKey(key);
			}
		} catch (Throwable t) {
			LOG.error(t);
		}
	}

	private static Set<String> getPatternKeys(String pattern) {
		Jedis jedis = jedisPool.getResource();
		Set<String> keys = jedis.keys(pattern);
		jedis.close();
		return keys;
	}

	public static void delValueByKey(String key) {
		try {
			Jedis jedis = jedisPool.getResource();
			jedis.del(key);
			jedis.close();
		} catch (Throwable t) {
			LOG.error(t);
		}
	}

	public static void refreshCache() {
		try {
			Jedis jedis = jedisPool.getResource();
			jedis.flushDB();
			jedis.close();
		} catch (Throwable t) {
			LOG.error(t);
		}
	}

	@SuppressWarnings("rawtypes")
	private static String getCacheKey(String className, String methodName,
			DataObject dataObject) {
		Map cacheMap = dataObject.getMap();
		StringBuffer sb = new StringBuffer();
		sb.append(className).append(".").append(methodName);
		Collection values = cacheMap.values();
		for (Object obj : values) {
			if (obj != null) {
				String str = obj.toString();
				sb.append(".").append(str);
			}
		}
		return sb.toString();
	}
	public static Jedis getJedis() {
		if(jedisPool == null) {
			System.out.println("jedisPool为空");
		}
		Jedis jedis = jedisPool.getResource();
		return jedis;
	}
	

}