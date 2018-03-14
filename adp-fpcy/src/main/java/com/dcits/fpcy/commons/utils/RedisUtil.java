package com.dcits.fpcy.commons.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.dcits.app.util.ApplicationContextUtils;

public class RedisUtil {
	private static JedisPool jedisPool = (JedisPool) ApplicationContextUtils
			.getContext().getBean("jedisPool");
	private static final Log LOG = LogFactory.getLog(RedisUtil.class);
	private static final String redisKey="fpcyRedisCode";
	public static void rpush(String key, String value) {
		try {
			Jedis jedis = jedisPool.getResource();
			jedis.rpush(redisKey+key,value);
			jedis.close();
		} catch (Exception e) {
			LOG.error("放队列时" +
					"key--" +redisKey+key+
					"value--" +value+
					"出错:"+e.getMessage());
		}
	}
	
	public static String lpop(String key) {
		try {
			String value = null;
			Jedis jedis = jedisPool.getResource();
			value = jedis.lpop(redisKey+key);
			jedis.close();
			if (value == null) {
				return null;
			} else {
				LOG.debug("从redis缓存中获取内容，key为：" + key + "值为：" + value);
				return value;
			}
		} catch (Exception e) {
			LOG.error("取队列时"+redisKey+key+"出错:"+e.getMessage());
		}
		return null;
	}
	
	public static Long llen(String key) {
		long length = 0;
		try {
			Jedis jedis = jedisPool.getResource();
			length = jedis.llen(redisKey+key);
			jedis.close();
		} catch (Exception e) {
			LOG.error("获取队列长多中"+redisKey+key+"出错:"+e.getMessage());
		}
		return length;
	}
	public static String rpop(String key) {
		try {
			String value = null;
			Jedis jedis = jedisPool.getResource();
			value = jedis.rpop(key);
			jedis.close();
			if (value == null) {
				return null;
			} else {
				LOG.debug("从redis缓存中获取内容，key为：" + key + "值为：" + value);
				return value;
			}
		} catch (Throwable t) {
			LOG.error(t);
		}
		return null;
	}
	
	public static List<String> lrange(String key,long start,long end){
		List<String> value = new ArrayList<String>();
		Jedis jedis = jedisPool.getResource();
		value = jedis.lrange(key, start, end);
		jedis.close();
		return value;
	}
}
