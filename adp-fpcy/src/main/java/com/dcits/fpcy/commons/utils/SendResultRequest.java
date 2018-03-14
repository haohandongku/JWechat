package com.dcits.fpcy.commons.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;

import com.dcits.app.data.DataObject;
import com.dcits.fpcy.commons.bean.IpAddress;
import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;

/**
 * 发送get post 请求
 * 
 */
public class SendResultRequest {

	private static Log logger = LogFactory.getLog(SendResultRequest.class);

	/**
	 * 
	 * @param requestHeaderMap
	 *            请求头信息
	 * @param ipAddress
	 *            ip地址
	 * @param attributeMap
	 *            请求数据
	 * @param requestAddress
	 *            数据库请求地址
	 * @param method
	 *            请求方法 get post
	 * @return
	 */
	public static InputStream sendRequestIn(
			Map<String, String> requestHeaderMap, IpAddress ipAddress,
			Map<String, String> attributeMap, String requestAddress,
			String method) {
		TrustAllHosts.trustAllHosts();
		InputStream in = null;
		HttpURLConnection conn = null;
		URL url = null;
		ipAddress = null;
		String addr = "";
		if (StringUtils.isEmpty(method)) {
			return null;
		}
		if (attributeMap != null) {
			addr = gene(requestAddress, attributeMap);
		} else {
			addr = requestAddress;
		}
		logger.debug("地址：" + addr);
		if (StringUtils.isEmpty(addr)) {
			return null;
		}
		try {
			url = new URL(addr);
			if (requestHeaderMap != null
					&& requestHeaderMap.get("localid") != null) {
				conn = (HttpURLConnection) url.openConnection();
				requestHeaderMap.remove("localid");
			} else {
				conn = HttpUtils.getConnection(url, ipAddress);
			}
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod(method.toUpperCase());
			conn.setUseCaches(false);
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(12000);
			if (null != requestHeaderMap) {
				Iterator<String> iterator = requestHeaderMap.keySet()
						.iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = (String) requestHeaderMap.get(key);
					conn.setRequestProperty(key, value);
				}
			} else {
				conn.setRequestProperty(HeaderType.USERAGENT, BrowerType.firfox);
			}
			logger.debug(conn.getRequestProperties());
			in = conn.getInputStream();// 获取发票验真返回信息
		} catch (Exception e) {
			logger.error("发票查询异常" + e.getMessage());
			if ("Connection reset".equals(e.getMessage())) {
				logger.error("Connection reset 发票查询进入");
				in = sendRequestIn(requestHeaderMap, ipAddress, attributeMap,
						requestAddress, method);
			}
		}
		return in;
	}

	/**
	 * 
	 * @param requestHeaderMap
	 *            请求头信息
	 * @param ipAddress
	 *            ip地址
	 * @param attributeMap
	 *            请求数据
	 * @param requestAddress
	 *            数据库请求地址
	 * @param method
	 *            请求方法 get post
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map sendRequestInToVat(Map<String, String> requestHeaderMap,
			IpAddress ipAddress, Map<String, String> attributeMap,
			String requestAddress, String method, Map map) {
		TrustAllHosts.trustAllHosts();
		InputStream in = null;
		HttpURLConnection conn = null;
		URL url = null;
		String addr = "";
		ipAddress = null;
		if (StringUtils.isEmpty(method)) {
			return null;
		}
		if (attributeMap != null) {
			addr = gene(requestAddress, attributeMap);
		} else {
			addr = requestAddress;
		}
		logger.debug("地址：" + addr);
		if (StringUtils.isEmpty(addr)) {
			return null;
		}
		try {
			url = new URL(addr);
			if (requestHeaderMap != null
					&& requestHeaderMap.get("localid") != null) {
				conn = (HttpURLConnection) url.openConnection();
				requestHeaderMap.remove("localid");
			} else {
				conn = HttpUtils.getConnection(url, ipAddress);
			}
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod(method.toUpperCase());
			conn.setUseCaches(false);
			conn.setConnectTimeout(1000);
			conn.setReadTimeout(15000);
			if (null != requestHeaderMap) {
				Iterator<String> iterator = requestHeaderMap.keySet()
						.iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = (String) requestHeaderMap.get(key);
					conn.setRequestProperty(key, value);
				}
			} else {
				conn.setRequestProperty(HeaderType.USERAGENT, BrowerType.firfox);
			}
			logger.debug(conn.getRequestProperties());
			in = conn.getInputStream();// 获取发票验真返回信息
			long connLength = conn.getContentLength();
			map.put("connLength", connLength);
			map.put("in", in);
			/*if(connLength>0){
			
			}else{
				if (checkNum < 4) {
					checkNum++;
					map.put("checkNum", checkNum);
					map = sendRequestInToVat(requestHeaderMap, ipAddress,
							attributeMap, requestAddress, method, map);

				}
			}**/
		} catch (Exception e) {
			logger.error("发票查询异常" + e.getMessage());
			/*if ("Connection reset".equals(e.getMessage())) {
				if (checkNum < 4) {
					checkNum++;
					map.put("checkNum", checkNum);
					map = sendRequestInToVat(requestHeaderMap, ipAddress,
							attributeMap, requestAddress, method, map);

				}
			} else if ("Read timed out".equals(e.getMessage())) {
				if (checkNum < 4) {
					checkNum++;
					map.put("checkNum", checkNum);
					map = sendRequestInToVat(requestHeaderMap, ipAddress,
							attributeMap, requestAddress, method, map);
				}
			} else if ("connect timed out".equals(e.getMessage())) {
				if (checkNum < 4) {
					checkNum++;
					map.put("checkNum", checkNum);
					map = sendRequestInToVat(requestHeaderMap, ipAddress,
							attributeMap, requestAddress, method, map);
				}
			}*/
		}
		return map;
	}

	/**
	 * 
	 * @param requestHeaderMap
	 *            请求头信息
	 * @param ipAddress
	 *            ip地址
	 * @param attributeMap
	 *            请求数据
	 * @param requestAddress
	 *            数据库请求地址
	 * @param method
	 *            请求方法 get post
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map sendRequestInToYzm(Map<String, String> requestHeaderMap,
			IpAddress ipAddress, Map<String, String> attributeMap,
			String requestAddress, String method, Map map) {
		int checkNum = (Integer) map.get("checkNum");
		ipAddress = null;
		TrustAllHosts.trustAllHosts();
		InputStream in = null;
		HttpURLConnection conn = null;
		URL url = null;
		String addr = "";
		if (StringUtils.isEmpty(method)) {
			return null;
		}
		if (attributeMap != null) {
			addr = gene(requestAddress, attributeMap);
		} else {
			addr = requestAddress;
		}
		if (StringUtils.isEmpty(addr)) {
			return null;
		}
		try {
			url = new URL(addr);
			if (requestHeaderMap != null
					&& requestHeaderMap.get("localid") != null) {
				conn = (HttpURLConnection) url.openConnection();
				requestHeaderMap.remove("localid");
			} else {
				conn = HttpUtils.getConnection(url, ipAddress);
			}
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod(method.toUpperCase());
			conn.setUseCaches(false);
			conn.setConnectTimeout(500);
			conn.setReadTimeout(100);
			if (null != requestHeaderMap) {
				Iterator<String> iterator = requestHeaderMap.keySet()
						.iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = (String) requestHeaderMap.get(key);
					conn.setRequestProperty(key, value);
				}
			} else {
				conn.setRequestProperty(HeaderType.USERAGENT, BrowerType.firfox);
			}
			in = conn.getInputStream();// 获取发票验真返回信息
			long connLength = conn.getContentLength();
			if(connLength==0){
				if (checkNum < 4) {
					checkNum++;
					map.put("checkNum", checkNum);
					map = sendRequestInToYzms(requestHeaderMap, ipAddress,
							attributeMap, requestAddress, method, map);
				}
			}else{
				map.put("connLength", connLength);
				map.put("in", in);
			}
		} catch (SocketException e3) {
			logger.error("验证码获取异常" + e3.getMessage());
			map.put("errorMsg", e3.getMessage());
			if (checkNum < 4) {
				checkNum++;
				map.put("checkNum", checkNum);
				map = sendRequestInToYzms(requestHeaderMap, ipAddress,
						attributeMap, requestAddress, method, map);
			}
		} catch (IOException e) {
			logger.error("验证码获取异常" + e.getMessage());
			map.put("errorMsg", e.getMessage());
			if (checkNum < 4) {
				checkNum++;
				map.put("checkNum", checkNum);
				map = sendRequestInToYzms(requestHeaderMap, ipAddress,
						attributeMap, requestAddress, method, map);
		   }
	   }
	   return map;
	}

	/**
	 * 
	 * @param requestHeaderMap
	 *            请求头信息
	 * @param ipAddress
	 *            ip地址
	 * @param attributeMap
	 *            请求数据
	 * @param requestAddress
	 *            数据库请求地址
	 * @param method
	 *            请求方法 get post
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map sendRequestInToYzms(Map<String, String> requestHeaderMap,
			IpAddress ipAddress, Map<String, String> attributeMap,
			String requestAddress, String method, Map map) {
		TrustAllHosts.trustAllHosts();
		InputStream in = null;
		ipAddress = null;
		HttpURLConnection conn = null;
		URL url = null;
		String addr = "";
		if (StringUtils.isEmpty(method)) {
			return null;
		}
		if (attributeMap != null) {
			addr = gene(requestAddress, attributeMap);
		} else {
			addr = requestAddress;
		}
		if (StringUtils.isEmpty(addr)) {
			return null;
		}
		try {
			url = new URL(addr);
			if (requestHeaderMap != null
					&& requestHeaderMap.get("localid") != null) {
				conn = (HttpURLConnection) url.openConnection();
				requestHeaderMap.remove("localid");
			} else {
				conn = HttpUtils.getConnection(url, ipAddress);
			}
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod(method.toUpperCase());
			conn.setUseCaches(false);
			conn.setConnectTimeout(1000);
			conn.setReadTimeout(10000);
			if (null != requestHeaderMap) {
				Iterator<String> iterator = requestHeaderMap.keySet()
						.iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = (String) requestHeaderMap.get(key);
					conn.setRequestProperty(key, value);
				}
			} else {
				conn.setRequestProperty(HeaderType.USERAGENT, BrowerType.firfox);
			}
			in = conn.getInputStream();// 获取发票验真返回信息
			long connLength = conn.getContentLength();
			map.put("connLength", connLength);
			map.put("in", in);
		} catch (SocketException e3) {
			map = sendRequestInToYzm(requestHeaderMap, ipAddress, attributeMap,
					requestAddress, method, map);
		} catch (IOException e) {
			map = sendRequestInToYzm(requestHeaderMap, ipAddress, attributeMap,
					requestAddress, method, map);
		}
		return map;
	}

	/**
	 * 
	 * @param in
	 *            输入流
	 * @param charSet
	 *            字符集
	 * @param returnType
	 *            json text
	 * @return
	 */
	public static Object iSToJSONOrDocument(InputStream in, String charSet,
			String returnType) {
		JSONObject jsonObject = null;
		BufferedReader br = null;
		InputStreamReader isr = null;
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isEmpty(returnType)) {
			returnType = "text";
		}
		try {
			if (StringUtils.isEmpty(charSet)) {
				isr = new InputStreamReader(in);
			} else {
				isr = new InputStreamReader(in, charSet);
			}
			br = new BufferedReader(isr);

			String string_cache = "";
			while ((string_cache = br.readLine()) != null) {
				sb.append(string_cache + "\n");
			}
		} catch (IOException e) {
			logger.error("获取iSToJSONOrDocument异常", e);
		} finally {
			try {
				in.close();
				if (null != br) {
					br.close();
				}
				if (null != isr) {
					isr.close();
				}
			} catch (IOException e) {
				logger.error("关闭br流异常", e);
			}
		}
		try {
			if ("json".equals(returnType)) {
				if (sb.toString().startsWith("[")) {
					jsonObject = new JSONObject(sb.toString().substring(
							sb.indexOf("[") + 1, sb.indexOf("]")));
				} else {
					jsonObject = new JSONObject(sb.toString());
				}
			} else if ("json1".equals(returnType)) {
				jsonObject = new JSONObject(sb.toString().substring(
						sb.indexOf("(") + 1, sb.indexOf(")")));
				String key3 = (String) jsonObject.get("key3");
				key3.replaceAll("\"", "\"");
				jsonObject.putOpt("key3", key3);
			}
		} catch (Exception e1) {
			logger.error("获取jsonObject异常", e1);
		}
		return returnType.equals("text") ? Jsoup.parse(sb.toString())
				: jsonObject;
	}

	/**
	 * 拼接地址后面参数
	 * 
	 * @param address
	 * @param attributeMap
	 * @return
	 */

	private static String gene(String address, Map<String, String> attributeMap) {
		if (null != attributeMap) {
			String end = "";
			Set<Entry<String, String>> attributeESet = attributeMap.entrySet();
			Iterator<Entry<String, String>> it = attributeESet.iterator();
			while (it.hasNext()) {
				Entry<String, String> attributeEntry = (Entry<String, String>) it
						.next();
				String key = attributeEntry.getKey();
				if ("hscEntity".equals(key)) {
					continue;
				}
				String value = attributeEntry.getValue();
				end += key + "=" + value + "&";
			}
			if (StringUtils.isNotEmpty(address)) {
				if (address.contains("?")
						&& ((address.length() - 1) == address.indexOf("?"))) {
					address += end;
				} else if (address.contains("?") && address.contains("=")) {
					address += "&" + end;
				} else if (!address.contains("?") && !address.contains("=")) {
					address += "?" + end;
				}
			}
			return address.substring(0, address.length() - 1);
		}
		return null;
	}

	/**
	 * 构建属性
	 * 
	 * @param attributeMap
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String structureAttribute(Map attributeMap) {
		Set attributeSet = attributeMap.entrySet();
		Iterator it = attributeSet.iterator();
		StringBuffer attributeString = new StringBuffer();
		while (it.hasNext()) {
			Entry<String, Object> entryMap = (Entry<String, Object>) it.next();
			attributeString.append(entryMap.getKey() + "="
					+ entryMap.getValue() + "&");
		}
		return attributeString.substring(0, attributeString.length() - 1);
	}

	@SuppressWarnings("unused")
	public static Map<String, Object> sendRequestMap(
			Map<String, String> requestHeaderMap, IpAddress ipAddress,
			Map<String, String> attributeMap, String requestAddress,
			String method) {
		Map<String, Object> map1 = new HashMap<String, Object>();
		InputStream in = null;
		List<String> cookieList = null;
		HttpURLConnection conn = null;
		URL url = null;
		String addr = "";
		ipAddress = null;
		if (StringUtils.isEmpty(method)) {
			return null;
		}
		// 生成地址
		addr = gene(requestAddress, attributeMap);
		if (StringUtils.isEmpty(requestAddress)) {
			return null;
		}
		try {
			url = new URL(requestAddress);
			if (requestHeaderMap != null
					&& requestHeaderMap.get("localid") != null) {
				conn = (HttpURLConnection) url.openConnection();
				requestHeaderMap.remove("localid");
			} else {
				conn = HttpUtils.getConnection(url, ipAddress);
			}
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod(method.toUpperCase());
			conn.setUseCaches(false);
			conn.setConnectTimeout(15000);
			conn.setReadTimeout(15000);
			if (null != requestHeaderMap) {
				Iterator<String> iterator = requestHeaderMap.keySet()
						.iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = (String) requestHeaderMap.get(key);
					conn.setRequestProperty(key, value);
				}
			} else {
				conn.setRequestProperty(HeaderType.USERAGENT, BrowerType.firfox);
			}
			Map<String, List<String>> topheader = conn.getHeaderFields();
			Set<Entry<String, List<String>>> headerSet = topheader.entrySet();// 返回此映射中包含的映射关系的
			Iterator<Entry<String, List<String>>> it = headerSet.iterator();
			while (it.hasNext()) {
				Entry<String, List<String>> en = (Entry<String, List<String>>) it
						.next();
				if ("set-cookie".equalsIgnoreCase(en.getKey())) {
					cookieList = en.getValue();
					break;
				}
			}
			in = conn.getInputStream();// 获取发票验真返回信息
			map1.put("in", in);
			map1.put("set-cookie", cookieList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map1;
	}

	public static List<String> sendRequestCookie(
			Map<String, String> requestHeaderMap, IpAddress ipAddress,
			Map<String, String> attributeMap, String requestAddress,
			String method) {
		ipAddress = null;
		HttpURLConnection conn = null;
		List<String> cookieList = null;
		try {
			URL url = new URL(requestAddress);
			if (requestHeaderMap != null
					&& requestHeaderMap.get("localid") != null) {
				conn = (HttpURLConnection) url.openConnection();
				requestHeaderMap.remove("localid");
			} else {
				conn = HttpUtils.getConnection(url, ipAddress);
			}
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setUseCaches(false);
			conn.setConnectTimeout(15000);
			conn.setReadTimeout(15000);
			if (null != requestHeaderMap) {
				Iterator<String> iterator = requestHeaderMap.keySet()
						.iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = (String) requestHeaderMap.get(key);
					conn.setRequestProperty(key, value);
				}
			} else {
				conn.setRequestProperty(HeaderType.USERAGENT, BrowerType.firfox);
			}
			// 响应头信息
			Map<String, List<String>> topheader = conn.getHeaderFields();
			Set<Entry<String, List<String>>> headerSet = topheader.entrySet();// 返回此映射中包含的映射关系的
			Iterator<Entry<String, List<String>>> it = headerSet.iterator();
			while (it.hasNext()) {
				Entry<String, List<String>> en = (Entry<String, List<String>>) it
						.next();
				if ("set-cookie".equalsIgnoreCase(en.getKey())) {
					cookieList = en.getValue();
					break;
				}
			}
		} catch (Exception e) {
			logger.error("获取sendRequest时异常", e);
		}
		return cookieList;
	}

	public static List<String> sendRequest(Map<String, String> map,
			IpAddress ipAddress, String address) {
		List<String> cookieList = null;
		try {
			ipAddress = null;
			URL url = new URL(address);
			HttpURLConnection conn = HttpUtils.getConnection(url, ipAddress);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setUseCaches(false);
			conn.setConnectTimeout(15000);
			conn.setReadTimeout(15000);
			if (null != map) {
				Iterator<String> iterator = map.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = (String) map.get(key);
					conn.setRequestProperty(key, value);
				}
			} else {
				conn.setRequestProperty(HeaderType.USERAGENT, BrowerType.firfox);
			}
			// 响应头信息
			Map<String, List<String>> topheader = conn.getHeaderFields();
			Set<Entry<String, List<String>>> headerSet = topheader.entrySet();// 返回此映射中包含的映射关系的
			Iterator<Entry<String, List<String>>> it = headerSet.iterator();
			while (it.hasNext()) {
				Entry<String, List<String>> en = (Entry<String, List<String>>) it
						.next();
				if ("set-cookie".equalsIgnoreCase(en.getKey())) {
					cookieList = en.getValue();
					break;
				}
			}
		} catch (Exception e) {
			logger.error("获取sendRequest时异常", e);
		}
		return cookieList;
	}

	/**
	 * 
	 * @param map
	 *            请求头
	 * @param ipAddress
	 *            ip
	 * @param attributeMap
	 *            请求数据
	 * @param requestAddress
	 *            数据库请求地址
	 * @return
	 */
	public static InputStream sendRequestPost(Map<String, String> map,
			IpAddress ipAddress, String requestAddress,
			Map<String, String> attributeMap) {
		InputStream in = null;
	/*	OutputStream out = null;
		DataOutputStream dos = null;
		String attributeString = null;*/
		HttpURLConnection conn = null;
		URL url = null;
		try {
			ipAddress= null;
			requestAddress = gene(requestAddress, attributeMap);
			url = new URL(requestAddress);
			conn = HttpUtils.getConnection(url, ipAddress);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setConnectTimeout(12000);
			conn.setReadTimeout(15000);
			if (null != map) {
				Iterator<String> iterator = map.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = (String) map.get(key);
					conn.setRequestProperty(key, value);
				}
			} else {
				conn.setRequestProperty(HeaderType.USERAGENT, BrowerType.firfox);
			}
			/*if (null != attributeMap) {
				attributeString = structureAttribute(attributeMap);
			}
			out = conn.getOutputStream();
			dos = new DataOutputStream(out);
			dos.writeBytes(attributeString);
			dos.flush();
			out.flush();*/
			System.out.println(conn.getResponseCode());
			in = conn.getInputStream();// 获取发票验真返回信息
			return in;
		} catch (Exception e) {
			logger.error("获取sendRequestPost时异常", e);
		}
		return in;
	}
    
	/**
	 * 
	 * @param map
	 *            请求头
	 * @param ipAddress
	 *            ip
	 * @param attributeMap
	 *            请求数据
	 * @param requestAddress
	 *            数据库请求地址
	 * @return
	 */
	public static InputStream sendRequestPostToGZGS(Map<String, String> map,
			IpAddress ipAddress, String requestAddress,
			Map<String, String> attributeMap) {
		InputStream in = null;
		HttpURLConnection conn = null;
		URL url = null;
		try {
			ipAddress = null;
			requestAddress = gene(requestAddress, attributeMap);
			url = new URL(requestAddress);
			conn = HttpUtils.getConnection(url, ipAddress);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setConnectTimeout(12000);
			conn.setReadTimeout(15000);
			if (null != map) {
				Iterator<String> iterator = map.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = (String) map.get(key);
					conn.setRequestProperty(key, value);
				}
			} else {
				conn.setRequestProperty(HeaderType.USERAGENT, BrowerType.firfox);
			}
			/*if (null != attributeMap) {
				attributeString = structureAttribute(attributeMap);
			}*/
			System.out.println(conn.getResponseCode());
			in = conn.getInputStream();// 获取发票验真返回信息
			return in;
		} catch (Exception e) {
			logger.error("获取sendRequestPost时异常", e);
		}
		return in;
	}
	public static InputStream sendRequestGet(Map<String, String> map,
			IpAddress ipAddress, String requestAddress,
			Map<String, String> attributeMap) {
		InputStream in = null;
		OutputStream out = null;
		DataOutputStream dos = null;
		String attributeString = null;
		HttpURLConnection conn = null;
		URL url = null;
		try {
			ipAddress= null;
			url = new URL(requestAddress);
			conn = HttpUtils.getConnection(url, ipAddress);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setUseCaches(false);
			conn.setConnectTimeout(15000);
			conn.setReadTimeout(15000);
			if (null != map) {
				Iterator<String> iterator = map.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = (String) map.get(key);
					conn.setRequestProperty(key, value);
				}
			} else {
				conn.setRequestProperty(HeaderType.USERAGENT, BrowerType.firfox);
			}
			if (null != attributeMap) {
				attributeString = structureAttribute(attributeMap);
			}
			out = conn.getOutputStream();
			dos = new DataOutputStream(out);
			dos.writeBytes(attributeString);
			dos.flush();
			out.flush();
			if (conn.getResponseCode() != 200)
				return null;
			in = conn.getInputStream();// 获取发票验真返回信息
			return in;
		} catch (Exception e) {
			logger.error("获取sendRequestPost时异常", e);
		}
		return in;
	}

	/**
	 * 
	 * @param in
	 *            输入流
	 * @param charSet
	 *            字符集
	 * @param returnType
	 *            xml
	 * @return
	 */
	public static Object iSToXMLDocument(InputStream in, String charSet,
			String returnType) {
		JSONObject jsonObject = null;
		BufferedReader br = null;
		InputStreamReader isr = null;
		StringBuffer sb = new StringBuffer();
		try {
			if (StringUtils.isEmpty(charSet)) {
				isr = new InputStreamReader(in);
			} else {
				isr = new InputStreamReader(in, charSet);
			}
			br = new BufferedReader(isr);

			String string_cache = "";
			while ((string_cache = br.readLine()) != null) {
				sb.append(string_cache + "\n");
			}
		} catch (IOException e) {
			logger.error("获取iSToXMLDocument异常", e);
		} finally {
			try {
				if (null != br) {
					br.close();
				}
				if (null != isr) {
					isr.close();
				}
			} catch (IOException e) {
				logger.error("关闭br流异常", e);
			}
		}
		try {
			if ("XML".equals(returnType)) {
				DataObject object = new DataObject(sb.toString());
				jsonObject = new JSONObject(object.getJson());
			}
		} catch (Exception e1) {
			logger.error("获取xml异常", e1);
		}
		return jsonObject;
	}

	public static String sent(String url, Map<String, String> map,
			String encoding) throws ParseException, IOException {
		String body = "";

		// 创建httpclient对象
		CloseableHttpClient client = HttpClients.createDefault();
		// 创建post方式请求对象
		HttpPost httpPost = new HttpPost(url);

		// 装填参数
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (map != null) {
			for (Entry<String, String> entry : map.entrySet()) {
				nvps.add(new BasicNameValuePair(entry.getKey(), entry
						.getValue()));
			}
		}
		// 设置参数到请求对象中
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, encoding));

		System.out.println("请求地址：" + url);
		System.out.println("请求参数：" + nvps.toString());

		// 设置header信息
		// 指定报文头【Content-type】、【User-Agent】
		httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
		httpPost.setHeader("User-Agent",
				"Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

		// 执行请求操作，并拿到结果（同步阻塞）
		CloseableHttpResponse response = client.execute(httpPost);
		// 获取结果实体
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			// 按指定编码转换结果实体为String类型
			body = EntityUtils.toString(entity, encoding);
		}
		EntityUtils.consume(entity);
		// 释放链接
		response.close();
		return body;
	}

	/*** -------------来至GETYzm-------------------- **/

}
