package com.dcits.fpcy.commons.factory.utils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sun.misc.BASE64Decoder;

import com.alibaba.druid.util.Base64;
import com.dcits.app.data.DataObject;
import com.dcits.fpcy.commons.bean.CookieList;
import com.dcits.fpcy.commons.bean.IpAddress;
import com.dcits.fpcy.commons.bean.TaxOfficeBean;
import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.utils.FilePath;
import com.dcits.fpcy.commons.utils.HttpUtils;
import com.dcits.fpcy.commons.utils.ResultUtils;
import com.dcits.ieds.proxy.IEDSProxy;

public class YzmRequestUtils {
	private static Log logger = LogFactory.getLog(YzmRequestUtils.class);

	/**
	 * 下载验证码需要cookie 一种是在请求头信息加cookie 另一种是在下载验证码的地址后面拼接
	 * 
	 * @param ipAddress
	 * @param paras
	 * @param headers
	 * @return
	 */
	public static Map<String, Object> sendYzmRequest(IpAddress ipAddress, TaxOfficeBean paras,
			Map<String, String> headers, String cookie) {
		String yzmdz = paras.yzm_dz;
		ipAddress = null;
		// 在下载验证码的地址后面拼接cookie信息
		// cookie[0] cookie key cookie[1] value
		if (null != cookie && paras.cookie == 2) {
			yzmdz += cookie;
		}

		InputStream in = null;
		ByteArrayOutputStream out = null;
		byte[] buffer = null;
		Map<String, Object> backMap = new HashMap<String, Object>();
		if (StringUtils.isEmpty(yzmdz) || yzmdz.equals("none")) {
			return backMap;
		}
		List<String> cookieList = null;
		URL url = null;
		HttpURLConnection conn = null;
		try {
			// 创建连接
			url = new URL(yzmdz);
			conn = HttpUtils.getConnection(url, ipAddress);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod(paras.yzm_qqfs.toUpperCase());
			conn.setUseCaches(false);
			conn.setConnectTimeout(500);
			conn.setReadTimeout(4000);
			// conn.setReadTimeout(10000);
			if (null != cookie && paras.cookie == 1) {
				conn.setRequestProperty(HeaderType.COOKIE, cookie);
			}
			if (null != headers) {
				Iterator<String> iterator = headers.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = (String) headers.get(key);
					conn.setRequestProperty(key, value);
				}
			} else {
				conn.setRequestProperty(HeaderType.USERAGENT, BrowerType.firfox);
			}
			in = conn.getInputStream();
			Map<String, List<String>> topheader = conn.getHeaderFields();
			Set<Entry<String, List<String>>> headerSet = topheader.entrySet();
			Iterator<Entry<String, List<String>>> it = headerSet.iterator();
			while (it.hasNext()) {
				Entry<String, List<String>> en = (Entry<String, List<String>>) it.next();
				if ("set-cookie".equalsIgnoreCase(en.getKey())) {
					cookieList = en.getValue();
					break;
				}
			}
			out = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = in.read(b)) != -1) {
				out.write(b, 0, n);
			}
			buffer = out.toByteArray();
			backMap.put("filepath", buffer);
			backMap.put("cookieList", cookieList);
		} catch (Exception e) {
			logger.error("下载验证码出现异常", e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				logger.error("[ERROR] 下载验证码异常：" + e);
			}
		}
		return backMap;
	}

	/**
	 * 获取上海国税验证码
	 * 
	 * @param ipAddress
	 * @param paras
	 * @param headers
	 * @param cookie
	 * @param HttpsCon
	 * @return
	 */
	@SuppressWarnings("unused")
	public static Map<String, Object> sendSHGYzmRequest(IpAddress ipAddress, TaxOfficeBean paras,
			Map<String, String> headers, String jessionID) {
		String yzmdz = paras.yzm_dz;
		InputStream in = null;
		ByteArrayOutputStream out = null;
		byte[] buffer = null;
		Map<String, Object> backMap = new HashMap<String, Object>();
		List<String> cookieList = null;

		try {
			// 创建连接
			String url = paras.yzm_dz
					+ "?s="
					+ new SimpleDateFormat("EE MMM dd yyyy HH:mm:ss zZZ ", Locale.ENGLISH).format(new Date())
							.replace("CST", "GMT").replace(" ", "%20")
					+ "(%E4%B8%AD%E5%9B%BD%E6%A0%87%E5%87%86%E6%97%B6%E9%97%B4)&" + jessionID;
			HttpsURLConnection HttpsCon = HttpUtils.getHttpsConnection(url, ipAddress);
			if (null != jessionID && paras.cookie == 1) {
				HttpsCon.setRequestProperty(HeaderType.COOKIE, jessionID);
			}
			if (null != headers) {
				Iterator<String> iterator = headers.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = (String) headers.get(key);
					HttpsCon.setRequestProperty(key, value);
				}
			}

			in = HttpsCon.getInputStream();
			Map<String, List<String>> topheader = HttpsCon.getHeaderFields();
			Set<Entry<String, List<String>>> headerSet = topheader.entrySet();
			Iterator<Entry<String, List<String>>> it = headerSet.iterator();
			while (it.hasNext()) {
				Entry<String, List<String>> en = (Entry<String, List<String>>) it.next();
				if ("set-cookie".equalsIgnoreCase(en.getKey())) {
					cookieList = en.getValue();
					break;
				}
			}
			out = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = in.read(b)) != -1) {
				out.write(b, 0, n);
			}
			buffer = out.toByteArray();
			backMap.put("filepath", buffer);
			backMap.put("cookieList", cookieList);
		} catch (Exception e) {
			logger.error("下载验证码出现异常", e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				logger.error("[ERROR] 下载验证码异常：" + e);
			}
		}
		return backMap;
	}

	/**
	 * https形式 山东国税
	 * 
	 * @param ipAddress
	 * @param paras
	 * @param headers
	 * @param jessionID
	 * @return
	 */
	@SuppressWarnings("unused")
	public static Map<String, Object> sendSDDSYzmRequest(IpAddress ipAddress, TaxOfficeBean paras,
			Map<String, String> headers, String jessionID) {
		InputStream in = null;
		ByteArrayOutputStream out = null;
		byte[] buffer = null;
		Map<String, Object> backMap = new HashMap<String, Object>();
		List<String> cookieList = null;

		try {
			String url = paras.yzm_dz;
			HttpsURLConnection HttpsCon = HttpUtils.getHttpsConnection(url, ipAddress);

			if (null != headers) {
				Iterator<String> iterator = headers.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = (String) headers.get(key);
					HttpsCon.setRequestProperty(key, value);
				}
			}

			in = HttpsCon.getInputStream();
			Map<String, List<String>> topheader = HttpsCon.getHeaderFields();
			Set<Entry<String, List<String>>> headerSet = topheader.entrySet();
			Iterator<Entry<String, List<String>>> it = headerSet.iterator();
			while (it.hasNext()) {
				Entry<String, List<String>> en = (Entry<String, List<String>>) it.next();
				if ("set-cookie".equalsIgnoreCase(en.getKey())) {
					cookieList = en.getValue();
					break;
				}
			}
			String filepath = FilePath.GetFileName(paras.yzm_tplx);
			out = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = in.read(b)) != -1) {
				out.write(b, 0, n);
			}
			buffer = out.toByteArray();
			backMap.put("filepath", buffer);
			backMap.put("cookieList", cookieList);
		} catch (Exception e) {
			logger.error("下载验证码出现异常", e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				logger.error("[ERROR] 下载验证码异常：" + e);
			}
		}
		return backMap;
	}

	/**
	 * @param map
	 *            请求头 如果是null HeaderType.USERAGENT, BrowerType.firfox
	 * @param yzm_dz
	 *            验证码地址
	 * @param yzm_qqfs
	 *            请求方法
	 * @param yzm_tplx
	 *            图片类型
	 * @return 图片地址 jsessionid
	 */
	@SuppressWarnings("unused")
	public static Map<String, Object> sendYzmRequest(Map<String, String> map, IpAddress ipAddress, String yzm_dz,
			TaxOfficeBean fpcyParas) {
		String yzm_qqfs = fpcyParas.yzm_qqfs;
		String yzm_tplx = fpcyParas.yzm_tplx;
		InputStream in = null;
		Map<String, Object> backMap = new HashMap<String, Object>();
		List<String> cookieList = null;
		URL url = null;
		ByteArrayOutputStream out = null;
		byte[] buffer = null;
		HttpURLConnection conn = null;
		try {
			// 创建连接
			url = new URL(yzm_dz);
			ipAddress = null;
			conn = HttpUtils.getConnection(url, ipAddress);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod(yzm_qqfs.toUpperCase());
			conn.setUseCaches(false);
			conn.setConnectTimeout(500);
			conn.setReadTimeout(4000);
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
			in = conn.getInputStream();
			// 响应头信息
			Map<String, List<String>> topheader = conn.getHeaderFields();
			Set<Entry<String, List<String>>> headerSet = topheader.entrySet();// 返回此映射中包含的映射关系的
																				// Set
																				// 视图
			Iterator<Entry<String, List<String>>> it = headerSet.iterator();
			while (it.hasNext()) {
				Entry<String, List<String>> en = (Entry<String, List<String>>) it.next();
				if ("set-cookie".equalsIgnoreCase(en.getKey())) {
					cookieList = en.getValue();
					break;
				}
			}
			out = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = in.read(b)) != -1) {
				out.write(b, 0, n);
			}
			buffer = out.toByteArray();
			backMap.put("filepath", buffer);
			backMap.put("cookieList", cookieList);
		} catch (Exception e) {
			logger.error("下载验证码出现异常", e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				logger.error("[ERROR] 下载验证码异常：" + e);
			}
		}
		return backMap;
	}

	/**
	 * 陕西验证码获取
	 * 
	 * @param yzm_dz
	 * @param yzm_qqfs
	 * @param yzm_tplx
	 * @param jessionID
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	public static Map sendSXDSYzmRequest(Map<String, String> map, IpAddress ipAddress, TaxOfficeBean fpcyParas,
			String jessionID) {
		String yzm_dz = fpcyParas.yzm_dz;
		String yzm_qqfs = fpcyParas.yzm_qqfs;
		String yzm_tplx = fpcyParas.yzm_tplx;
		InputStream in = null;
		Map<String, Object> backMap = null;
		HttpURLConnection con = null;
		ByteArrayOutputStream out = null;
		byte[] buffer = null;
		try {
			// 创建连接
			URL url = new URL(yzm_dz + "time=" + new Date().getTime());
			con = HttpUtils.getConnection(url, ipAddress);
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestMethod(yzm_qqfs.toUpperCase());
			con.setUseCaches(false);
			con.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
			con.setRequestProperty("Referer", "http://fpcx.xads.gov.cn/sxlt/inv/invqueryinit.do");
			con.setRequestProperty("Cookie", jessionID);

			in = con.getInputStream();
			backMap = new HashMap<String, Object>();
			out = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = in.read(b)) != -1) {
				out.write(b, 0, n);
			}
			buffer = out.toByteArray();
			backMap.put("filepath", buffer);

		} catch (Exception e) {
			logger.error("[ERROR] 下载验证码异常：" + e);
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					logger.error("[ERROR] 下载验证码异常：" + e);
				}
		}
		return backMap;
	}

	/**
	 * 广东地税验证码获得 modify by fangys . 2016/1/22
	 * 
	 * @param map
	 *            请求头 如果是null HeaderType.USERAGENT, BrowerType.firfox
	 * @param yzm_dz
	 *            验证码地址
	 * @param yzm_qqfs
	 *            请求方法
	 * @param yzm_tplx
	 *            图片类型
	 * @return 图片地址 jsessionid
	 */

	public static Map<String, Object> sendGuangDDSYzmRequest(Map<String, String> map, IpAddress ipAddress,
			String yzm_dz, String yzm_qqfs, String yzm_tplx) {
		InputStream in = null;
		Map<String, Object> backMap = new HashMap<String, Object>();
		List<String> cookieList = null;
		ByteArrayOutputStream out = null;
		byte[] buffer = null;
		try {
			// 创建连接
			URL url = new URL(yzm_dz);
			HttpURLConnection conn = HttpUtils.getConnection(url, ipAddress);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod(yzm_qqfs.toUpperCase());
			conn.setUseCaches(false);
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
			in = conn.getInputStream();
			// 响应头信息
			Map<String, List<String>> topheader = conn.getHeaderFields();
			Set<Entry<String, List<String>>> headerSet = topheader.entrySet();// 返回此映射中包含的映射关系的
																				// Set
																				// 视图
			Iterator<Entry<String, List<String>>> it = headerSet.iterator();
			while (it.hasNext()) {
				Entry<String, List<String>> en = (Entry<String, List<String>>) it.next();
				if ("set-cookie".equalsIgnoreCase(en.getKey())) {
					cookieList = en.getValue();
					break;
				}
			}

			// 先一步得到JSESSIONID
			String JSESSIONID = CookieList.getItem(cookieList);
			// 广东新图片地址拼接
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "utf-8"));
			String ss = null;
			String html = "";
			while ((ss = bufferedReader.readLine()) != null) {
				html += ss;
			}
			Document doc = Jsoup.parse(html);
			Elements imgel = doc.select("img");
			Element imgno = imgel.get(0);
			String imgaddress = "http://www.gdltax.gov.cn" + imgno.attr("src");
			url = new URL(imgaddress);
			HttpURLConnection httpconection = (HttpURLConnection) url.openConnection();
			httpconection.setDoInput(true);
			httpconection.setDoOutput(true);
			httpconection.setRequestMethod("GET");
			httpconection.setUseCaches(false);
			httpconection.setRequestProperty(HeaderType.USERAGENT, BrowerType.firfox);
			if (JSESSIONID != null && JSESSIONID.length() > 0)
				httpconection.setRequestProperty("Cookie", JSESSIONID);
			httpconection.connect();
			in = httpconection.getInputStream();// 获取发票验真返回信息
			out = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = in.read(b)) != -1) {
				out.write(b, 0, n);
			}
			buffer = out.toByteArray();
			backMap.put("filepath", buffer);
			backMap.put("cookieList", cookieList);
			backMap.put("JSESSIONID", JSESSIONID);
		} catch (Exception e) {
			logger.error("[ERROR] 程序执行操作出现异常：" + e.getMessage());
		}
		return backMap;
	}

	/**
	 * 甘肃地税验证码获得 modify by fangys . 2016/1/25
	 * 
	 * @param map
	 *            请求头 如果是null HeaderType.USERAGENT, BrowerType.firfox
	 * @param yzm_dz
	 *            验证码地址
	 * @param yzm_qqfs
	 *            请求方法
	 * @param yzm_tplx
	 *            图片类型
	 * @return 图片地址 jsessionid
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, Object> sendGanSDSYzmRequest(Map<String, String> map, IpAddress ipAddress, String yzm_dz,
			String yzm_qqfs, String yzm_tplx) {
		InputStream in = null;
		OutputStream out = null;
		Map<String, Object> backMap = new HashMap<String, Object>();
		List<String> cookieList = null;
		try {
			// 创建连接
			URL url = new URL(yzm_dz);
			HttpURLConnection conn = HttpUtils.getConnection(url, ipAddress);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod(yzm_qqfs.toUpperCase());
			conn.setUseCaches(false);
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

			// 创建返回信息Map
			Map topheader = conn.getHeaderFields();// 相应头信息
			Set headerSet = topheader.entrySet();// 返回此映射中包含的映射关系的 Set 视图
			Iterator it = headerSet.iterator();
			while (it.hasNext()) {
				Entry<String, List> en = (Entry<String, List>) it.next();
				if ("set-cookie".equalsIgnoreCase(en.getKey())) {
					cookieList = en.getValue();
				}
			}
			StringBuffer cookieStr = new StringBuffer();
			int j = 0;
			do {
				cookieStr.append(cookieList.get(j) + "：");
				j++;
			} while (j < cookieList.size());

			in = conn.getInputStream();
			backMap.put("JSESSIONID", CookieList.getItem(cookieList));
			String imgaddress = "http://fpcx.gs-l-tax.gov.cn:8000/fpcx/wlfp/fpxxcx/fpxxcx_index.do?method=sendCheckcode";//
			url = new URL(imgaddress);
			HttpURLConnection httpconection = (HttpURLConnection) url.openConnection();
			httpconection.setDoInput(true);
			httpconection.setDoOutput(true);
			httpconection.setRequestMethod("GET");
			httpconection.setUseCaches(false);
			httpconection
					.setRequestProperty("User-Agent",
							"Mozilla/5.0 (X11; U; Linux x86_64; zh-CN; rv:1.9.2.9) Gecko/20100827 Red Hat/3.6.9-2.el6 Firefox/3.6.9 ");
			if (backMap.get("JSESSIONID").toString() != null && backMap.get("JSESSIONID").toString().length() > 0)
				httpconection.setRequestProperty("Cookie", backMap.get("JSESSIONID").toString());
			httpconection.connect();
			in = httpconection.getInputStream();// 获取发票验真返回信息

			// 图片要保存的路径及文件名
			String filepath = FilePath.GetFileName(yzm_tplx);
			// String filepath = "E:/fpyzm."+yzm_tplx;
			File img = new File(filepath);
			out = new FileOutputStream(img);
			int count = 0;
			while ((count = in.read()) != -1) {
				out.write(count);
			}
			backMap.put("filepath", filepath);
			backMap.put("cookieList", cookieList);
			backMap.put("JSESSIONID", backMap.get("JSESSIONID").toString());
		} catch (Exception e) {
			logger.error("[ERROR] 下载验证码异常：" + e);
		}
		return backMap;
	}

	/**
	 * 超级鹰获取验证码
	 * 
	 * @throws IOException
	 * @throws JSONException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String getYZM(Map parameter) {
		BASE64Decoder decoder = new BASE64Decoder();
		byte[] b = null;
		BufferedImage bi = null;
		int jsonk = parameter.get("key1").toString().length();
		long startTime=System.currentTimeMillis();
		try {
			if (jsonk > 10) {
				b = decoder.decodeBuffer(parameter.get("key1").toString());
				ByteArrayInputStream is = new ByteArrayInputStream(b);
				bi = ImageIO.read(is);
				String index1 = parameter.get("key4").toString();
				File file = ResultUtils.getFile(index1);
				// 拼接图片
				BufferedImage bi1 = ImageIO.read(file);
				b = ResultUtils.mergeImage(bi, bi1, 1);
			}else{
				parameter.put("errorCode", "04");
				parameter.put("errorMsg", "从税局拿下来的key1不全");
			}
		} catch (IOException e) {
			logger.error("系统处理税局图片出错:" + e.getMessage());
			// 系统处理税局图片出错
			parameter.put("errorCode", "01");
			parameter.put("errorMsg", "系统处理税局图片时出错");
		}
		long startTime2=System.currentTimeMillis();
		logger.debug("获取拼图时间:" + (startTime2-startTime));
		String result = "";
	/*	result="baz";
		String picId=UUID.randomUUID()+"";
		parameter.put("pic_id", picId);*/
		if (b != null) {
			String codeBase64=Base64.byteArrayToBase64(b);
			Map ocrMap=new HashMap();
			ocrMap.put("codeType", "6004");
			ocrMap.put("fileType", "gif");
			ocrMap.put("codeBase64",codeBase64);
			ocrMap.put("requestId",parameter.get("requestId"));
			logger.debug("打码服务开始---------------------------------");
			DataObject dataObject=IEDSProxy.doService("OCRPost", new DataObject(ocrMap), null);
			Map map=dataObject.getMap();
			if(map!=null){
				String bizCode=(String) map.get("bizCode");
				String bizMsg=(String) map.get("bizMsg");
				String picId=(String) map.get("picId");
				String ocrStr=(String) map.get("ocrStr");
				if("00".equals(bizCode)){
					result=ocrStr;
					parameter.put("pic_id", picId);
				}else{
					logger.error("打码服务异常");
				}
				parameter.put("errorCode", bizCode);
				parameter.put("errorMsg",bizMsg);
			}else{
				logger.error("打码服务异常");
				parameter.put("errorCode", "99");
				parameter.put("errorMsg","打码服务异常");
			}
		}
		return result;
	}

	/**
	 * 上海国税https请求
	 * 
	 * @param map
	 * @param ipAddress
	 * @param url
	 * @return
	 */
	public static List<String> sendSHGSHttpsRequest(Map<String, String> map, IpAddress ipAddress, String url) {

		List<String> cookieList = null;

		HttpsURLConnection httpsCon = HttpUtils.getHttpsConnection(url, ipAddress);
		Map<String, List<String>> topheader = httpsCon.getHeaderFields();
		Set<Entry<String, List<String>>> headerSet = topheader.entrySet();// 返回此映射中包含的映射关系的
		Iterator<Entry<String, List<String>>> it = headerSet.iterator();
		while (it.hasNext()) {
			Entry<String, List<String>> en = (Entry<String, List<String>>) it.next();
			if ("set-cookie".equalsIgnoreCase(en.getKey())) {
				cookieList = en.getValue();
				break;
			}
		}

		return cookieList;
	}
}
