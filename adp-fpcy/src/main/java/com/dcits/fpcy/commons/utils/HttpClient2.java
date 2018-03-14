package com.dcits.fpcy.commons.utils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;
import com.dcits.fpcy.commons.dao.YzmDao;
/**
 *  单连接池
 * @author wuche
 * @version 优化httpConnection 请求（算是定制服务类，现在还不支持cookie扩展，获取图片流形式）
 * 提供get和post请求
 * 实时创建httpClient
 */
public class HttpClient2 {
	private static Log logger = LogFactory.getLog(HttpClient2.class);
	private  static String proxyHost;
	private static int proxyPort;
	private  static String isProxy;
	public void setProxyHost(String proxyHost) {
		HttpClient2.proxyHost = proxyHost;
	}

	public void setProxyPort(int proxyPort) {
		HttpClient2.proxyPort = proxyPort;
	}

	public void setIsProxy(String isProxy) {
		HttpClient2.isProxy = isProxy;
	}
	// 连接池管理器
	private static PoolingHttpClientConnectionManager connMgr;
	// 连接参数设置
	private static RequestConfig defaultRequestConfig;
	// 设置连接监听器
	private static HttpRequestRetryHandler httpRequestRetryHandler1;
	private static final int MAX_TIMEOUT = 10*1000;

	static {
		// 设置连接池
		RequestConfig.Builder configBuilder = RequestConfig.custom();
		// 设置连接超时
		configBuilder.setConnectTimeout(5*1000);
		// 设置读取超时
		configBuilder.setSocketTimeout(MAX_TIMEOUT);
		// 设置从连接池获取连接实例的超时
		configBuilder.setConnectionRequestTimeout(2000);
		// 在提交请求之前 测试连接是否可用
		configBuilder.setStaleConnectionCheckEnabled(true);
		defaultRequestConfig = configBuilder.build();
		httpRequestRetryHandler1 = new DefaultHttpRequestRetryHandler(
				0, false);
	}

	
    /**
     * 
     * @param url
     * @param logMap(日志参数放reqestType,swjgmc,来源)
     * @param paramterMap
     * @param headerMap
     * @param configMap
     * @return
     */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map callGetService(String url,Map  logMap, Map paramterMap, Map headerMap,
			Map configMap) {
		logger.debug("---httpGet请求路径：" + url);
		long  startTime=System.currentTimeMillis();
		CloseableHttpResponse httpResponse = null;
		CloseableHttpClient httpClient = null;
		RequestConfig requestConfig = null;
		Map returnMap = new HashMap();
		String resultData = "";
		String resultCode = "99";
		String resultCodeName = "服务器忙";
		try {
			if (configMap != null) {
				requestConfig = getRequestConfig(configMap);
			} else {
				requestConfig = defaultRequestConfig;
			}
			if ("NO".equals(isProxy)) {
				requestConfig = RequestConfig.copy(requestConfig)
						.setProxy(new HttpHost(proxyHost, proxyPort))
						.build();
			}
			if (StringUtils.isBlank(url)) {
				resultCodeName = "HttpClient.callService的url为空！";
			} else {
				// 处理https写法
				if (url.trim().startsWith("https")) {
					// 采用绕过验证的方式处理https请求
					SSLContext sslcontext = createIgnoreVerifySSL();
					// 设置协议http和https对应的处理socket链接工厂的对象
					Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
							.<ConnectionSocketFactory> create()
							.register("http",
									PlainConnectionSocketFactory.INSTANCE)
							.register("https",
									new SSLConnectionSocketFactory(
											sslcontext,
									        new String[] {"TLSv1", "TLSv1.1", "TLSv1.2"},
									        null,
									        SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER))
							.build();
					connMgr = new PoolingHttpClientConnectionManager(
							socketFactoryRegistry);
					connMgr.setMaxTotal(300);
					connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());
					httpClient = HttpClients.custom()
							.setConnectionManager(connMgr)
							.setDefaultRequestConfig(requestConfig)
							.setRetryHandler(httpRequestRetryHandler1)
							.build();
				} else {
					httpClient = HttpClients.custom()
							.setConnectionManager(connMgr)
							.setDefaultRequestConfig(requestConfig)
							.setRetryHandler(httpRequestRetryHandler1)
							.build();
				}
				// 放置参数
				List<NameValuePair> nvps = getParamList(paramterMap);
				// 转换为键值对
				String str = EntityUtils.toString(new UrlEncodedFormEntity(
						nvps, "utf-8"));
				if(url.contains("?")){
					url=url + str;
				}else{
					url=url +"?"+ str;
				}
				
				HttpGet httpGet = new HttpGet(url);
				// 获取头信息
				putGetHeader(headerMap, httpGet);
				// 新建cookie防止税局对cookeie拦截
				CookieStore cookieStore = new BasicCookieStore();
				HttpClientContext context = HttpClientContext.create();
				context.setCookieStore(cookieStore);
				httpResponse = httpClient.execute(httpGet,context);
				HttpEntity returnEntity = httpResponse.getEntity();
				if (returnEntity != null) {
					resultData = EntityUtils.toString(returnEntity, "UTF-8");
					if (StringUtils.isEmpty(resultData)
							|| -1 < resultData.indexOf("400 Bad Request")
							) {
						resultData = "";
						resultCode = "400 Bad Request";
						resultCodeName = "400 Bad Request找不到页面或者拿不到数据";
					}else if(-1 < resultData.indexOf("Error 503--Service Unavailable")){
						resultData = "";
						resultCode = "Service Unavailable";
						resultCodeName = "Error 503--Service Unavailable";
					}else if(-1 < resultData.indexOf("ISAPI plug-in Error Message")){
						resultData = "";
						resultCode = "Service Unavailable";
						resultCodeName = "ISAPI plug-in Error Message:No backend server available for connection: timed out after 10 seconds or idempotent set to OFF.";
					}else if (-1 < resultData
							.indexOf("Error 500--Internal Server Error")) {
						resultData = "";
						resultCode = "Service Unavailable";
						resultCodeName = "Error 500--Internal Server Error";
					}else if (-1 < resultData
							.indexOf("Bad Request")) {
						resultData = "";
						resultCode = "Service Unavailable";
						resultCodeName = "Error 400--Bad Request";
					} else {
						resultCode = "SUCCESS";
						resultCodeName =resultData;
					}
					EntityUtils.consume(returnEntity);
				} else {
					resultData = "";
					resultCode = "Service Unavailable";
					resultCodeName = "系统获取不到数据";
				}
			}
		} catch (Exception e) {
			logger.error("httpGet获取请求异常1：" + e);
			resultCodeName = e.getClass().getName();
			resultCode = e.getMessage();
		} finally {
			// 消耗实体内容
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (IOException e) {
					logger.error("httpGet获取请求异常2：" + e.getMessage());
					resultCodeName = e.getClass().getName();
					resultCode = e.getMessage();
				}
			}
			// 关闭相应 丢弃http连接
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					logger.error("httpGet获取请求异常：" + e.getMessage());
					resultCodeName = e.getClass().getName();
					resultCode = e.getMessage();
				}
			}
			long  endTime=System.currentTimeMillis();
			//记录日志
			logMap.put("requestConent", DataConvertUtil.MapToString(paramterMap));
			logMap.put("errorMsg",resultCodeName);
			logMap.put("errorCode",resultCode);
			if("SUCCESS".equals(resultCode)){
				logMap.put("isSuccess","Y");
			}else{
				logMap.put("isSuccess","N");
			}
			logMap.put("requestTime",String.valueOf(endTime-startTime));
			try {
			    YzmDao.saveRequestLog(logMap);
			}catch(Exception e){
				logger.error("httpGet获取请求保存异常：" + e.getMessage());
			}
		}
		returnMap.put("resultData", resultData);
		returnMap.put("resultCode", resultCode);
		returnMap.put("resultCodeName", resultCodeName);
		return returnMap;
	}
     
	 /**
     * 
     * @param url
     * @param logMap(日志参数放reqestType,swjgmc,来源)
     * @param paramterMap
     * @param headerMap
     * @param configMap
     * @return
     */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map callPostService(String url,Map  logMap, Map paramterMap, Map headerMap,
			Map configMap) {
		logger.debug("---httpGet请求路径：" + url);
		long  startTime=System.currentTimeMillis();
		CloseableHttpResponse httpResponse = null;
		CloseableHttpClient httpClient = null;
		RequestConfig requestConfig = null;
		Map returnMap = new HashMap();
		String resultData = "";
		String resultCode = "99";
		String resultCodeName = "服务器忙";
		try {
			if (configMap != null) {
				requestConfig = getRequestConfig(configMap);
			} else {
				requestConfig = defaultRequestConfig;
			}
			if ("NO".equals(isProxy)) {
				requestConfig = RequestConfig.copy(requestConfig)
						.setProxy(new HttpHost(proxyHost, proxyPort))
						.build();
			}
			if (StringUtils.isBlank(url)) {
				resultCodeName = "HttpClient.callService的url为空！";
			} else {
				// 处理https写法
				if (url.trim().startsWith("https")) {
					// 采用绕过验证的方式处理https请求
					SSLContext sslcontext = createIgnoreVerifySSL();
					// 设置协议http和https对应的处理socket链接工厂的对象
					Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
							.<ConnectionSocketFactory> create()
							.register("http",
									PlainConnectionSocketFactory.INSTANCE)
							.register("https",
									new SSLConnectionSocketFactory(sslcontext))
							.build();
					connMgr = new PoolingHttpClientConnectionManager(
							socketFactoryRegistry);
					connMgr.setMaxTotal(300);
					connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());
					httpClient = HttpClients.custom()
							.setConnectionManager(connMgr)
							.setDefaultRequestConfig(requestConfig)
							.setRetryHandler(httpRequestRetryHandler1)
							.build();
				} else {
					httpClient = HttpClients.custom()
							.setConnectionManager(connMgr)
							.setDefaultRequestConfig(requestConfig)
							.setRetryHandler(httpRequestRetryHandler1)
							.build();
				}
				
				// 新建cookie防止税局对cookeie拦截
				CookieStore cookieStore = new BasicCookieStore();
				HttpClientContext context = HttpClientContext.create();
				context.setCookieStore(cookieStore);
				HttpPost httpPost = new HttpPost(url);
				// 获取头信息
				putPostHeader(headerMap, httpPost);
				// 放置参数
				List<NameValuePair> nvps = getParamList(paramterMap);
				httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
				httpResponse = httpClient.execute(httpPost,context);
				HttpEntity returnEntity = httpResponse.getEntity();
				if (returnEntity != null) {
					resultData = EntityUtils.toString(returnEntity, "UTF-8");
					if (StringUtils.isEmpty(resultData)
							|| -1 < resultData.indexOf("400 Bad Request")
							) {
						resultData = "";
						resultCode = "400 Bad Request";
						resultCodeName = "400 Bad Request找不到页面或者拿不到数据";
					}else if(-1 < resultData.indexOf("Error 503--Service Unavailable")){
						resultData = "";
						resultCode = "Service Unavailable";
						resultCodeName = "Error 503--Service Unavailable";
					}else if(-1 < resultData.indexOf("ISAPI plug-in Error Message")){
						resultData = "";
						resultCode = "Service Unavailable";
						resultCodeName = "ISAPI plug-in Error Message:No backend server available for connection: timed out after 10 seconds or idempotent set to OFF.";
					}else if (-1 < resultData
							.indexOf("Error 500--Internal Server Error")) {
						resultData = "";
						resultCode = "Service Unavailable";
						resultCodeName = "Error 500--Internal Server Error";
					} else {
						resultCode = "SUCCESS";
						resultCodeName =resultData;
					}
					EntityUtils.consume(returnEntity);
				}else {
					resultData = "";
					resultCode = "Service Unavailable";
					resultCodeName = "找不到数据Error 500--Service Unavailable";
				}
			}
		} catch (Exception e) {
			logger.error("httpGet获取请求异常1：" + e);
			resultCodeName = e.getClass().getName();
			resultCode = e.getMessage();
		} finally {
			// 消耗实体内容
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (IOException e) {
					logger.error("httpGet获取请求异常2：" + e.getMessage());
					resultCodeName = e.getClass().getName();
					resultCode = e.getMessage();
				}
			}
			// 关闭相应 丢弃http连接
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					logger.error("httpGet获取请求异常：" + e.getMessage());
					resultCodeName = e.getClass().getName();
					resultCode = e.getMessage();
				}
			}
			long  endTime=System.currentTimeMillis();
			//记录日志
			logMap.put("requestConent", DataConvertUtil.MapToString(paramterMap));
			logMap.put("errorMsg",resultCodeName);
			logMap.put("errorCode",resultCode);
			if("SUCCESS".equals(resultCode)){
				logMap.put("isSuccess","Y");
			}else{
				logMap.put("isSuccess","N");
			}
			logMap.put("requestTime",String.valueOf(endTime-startTime));
			try {
			    YzmDao.saveRequestLog(logMap);
			}catch(Exception e){
				logger.error("httpGet获取请求保存异常：" + e.getMessage());
			}
		}
		returnMap.put("resultData", resultData);
		returnMap.put("resultCode", resultCode);
		returnMap.put("resultCodeName", resultCodeName);
		return returnMap;
	}
	/**
	 * 绕过验证
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public static SSLContext createIgnoreVerifySSL()
			throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sc = SSLContext.getInstance("SSLv3");
		// 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
		X509TrustManager trustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		sc.init(null, new TrustManager[] { trustManager }, null);
		return sc;
	}

	/**
	 * 获取参数
	 * 
	 * @param parmaterMap
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static List<NameValuePair> getParamList(Map parmaterMap) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (null != parmaterMap) {
			Iterator<String> iterator = parmaterMap.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				String value = (String) parmaterMap.get(key);
				params.add(new BasicNameValuePair(key, value));
			}
		}
		return params;
	}


	/**
	 * 放置post头信息
	 * 
	 * @param headerMap
	 * @param httpGet
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void putGetHeader(Map headerMap, HttpGet httpGet) {
		if (null != headerMap) {
			Iterator<String> iterator = headerMap.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				String value = (String) headerMap.get(key);
				httpGet.setHeader(key, value);
			}
		}
	}
	/**
	 * 放置post头信息
	 * 
	 * @param headerMap
	 * @param httpGet
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void putPostHeader(Map headerMap, HttpPost httpPost) {
		if (null != headerMap) {
			Iterator<String> iterator = headerMap.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				String value = (String) headerMap.get(key);
				httpPost.setHeader(key, value);
			}
		}
	}

	/**
	 * 重置requestConfig
	 * 
	 * @param headerMap
	 * @param httpGet
	 */
	@SuppressWarnings({ "rawtypes" })
	private static RequestConfig getRequestConfig(Map configMap) {
		RequestConfig.Builder configBuilder = RequestConfig.custom();
		Object connectionTime = configMap.get("connectionTime");
		Object socketTime = configMap.get("socketTime");
		Object requestTime = configMap.get("requestTime");
		if (connectionTime instanceof Integer && (Integer) connectionTime != 0) {
			// 设置连接超时
			configBuilder.setConnectTimeout((Integer) connectionTime);
		} else {
			// 设置连接超时
			configBuilder.setConnectTimeout(MAX_TIMEOUT);
		}
		if (socketTime instanceof Integer && (Integer) socketTime != 0) {
			// 设置读取超时
			configBuilder.setSocketTimeout((Integer) socketTime);
		} else {
			// 设置读取超时
			configBuilder.setSocketTimeout(MAX_TIMEOUT);
		}
		if (requestTime instanceof Integer && (Integer) requestTime != 0) {
			// 设置从连接池获取连接实例的超时
			configBuilder.setConnectionRequestTimeout((Integer) requestTime);
		} else {
			// 设置从连接池获取连接实例的超时
			configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);
		}
		// 在提交请求之前 测试连接是否可用
		configBuilder.setStaleConnectionCheckEnabled(true);
		RequestConfig requestConfig = configBuilder.build();
		return requestConfig;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) throws Exception {
		Map HeaderMap = new HashMap<String, String>();
		String url = "https://fpcy.sc-n-tax.gov.cn/WebQuery/yzmQuery";
		/*
		 * // 放置参数 Map<String, String> mar = new HashMap<String, String>();
		 * mar.put("_", String.valueOf(System.currentTimeMillis()));
		 * mar.put("fpdm", "4200164320"); List<NameValuePair> nvps =
		 * getParamList(mar); String str = EntityUtils.toString(new
		 * UrlEncodedFormEntity(nvps, "utf-8"));
		 * System.out.println(HttpClientUtil.get(url+"?"+str));
		 */
		HeaderMap.put("Cache-Control", "max-age=0");
		HeaderMap.put("Referer", "https://inv-veri.chinatax.gov.cn");
		HeaderMap.put("Host", "fpcy.sc-n-tax.gov.cn");
		HeaderMap.put("Connection", "keep-alive");
		HeaderMap.put("Accept", "*/*");
		HeaderMap.put("Accept-Encoding", "gzip, deflate, sdch");
		HeaderMap.put("Accept-Language", "zh-CN,zh;q=0.8");
		HeaderMap.put(HeaderType.USERAGENT, BrowerType.firfox);
		Map<String, String> mar = new HashMap<String, String>();
		mar.put("_", String.valueOf(System.currentTimeMillis()));
		mar.put("fpdm", "151001722002");
		HttpClient2 httpClient = new HttpClient2();
		Map requestConfigMap = new HashMap();
		requestConfigMap.put("connectionTime", 1000);
		requestConfigMap.put("socketTime", 1000);
		Map logMap=new HashMap();
		for (int i = 0; i < 1000; i++) {
			long startTime=System.currentTimeMillis();
			System.out.println(httpClient.callGetService(url,logMap, mar, HeaderMap,
					requestConfigMap));
			long endTime=System.currentTimeMillis();
	        System.out.println("运行时间："+(endTime-startTime));
		}
	}
	
	
}