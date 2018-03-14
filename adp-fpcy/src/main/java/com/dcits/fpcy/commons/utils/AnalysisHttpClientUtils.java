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
import java.util.concurrent.TimeUnit;

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
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultHttpResponseParserFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.dcits.fpcy.commons.dao.YzmDao;

/**
 * 
 * @author wuche
 * AnalysisHttpClientUtils(可以代理获取)
 * @version 优化httpConnection 请求（算是定制服务类，现在还不支持cookie扩展，获取图片流形式） 提供get
 *          2017-10-17优化httpClient 请求
 *          1）去掉cookie 
 *          2）定时任务去掉清扫不可用httpClient
 *          
 */
public class AnalysisHttpClientUtils {
	private static Log logger = LogFactory.getLog(AnalysisHttpClientUtils.class);
	//验证码线程池
	static PoolingHttpClientConnectionManager manager = null;
	//查验线程池
	static CloseableHttpClient httpClient = null;
	private  static String proxyHost;
	private static int proxyPort;
	private  static String isProxy;
	public void setProxyHost(String proxyHost) {
		AnalysisHttpClientUtils.proxyHost = proxyHost;
	}

	public void setProxyPort(int proxyPort) {
		AnalysisHttpClientUtils.proxyPort = proxyPort;
	}

	public void setIsProxy(String isProxy) {
		AnalysisHttpClientUtils.isProxy = isProxy;
	}
	// 连接参数设置
	private static synchronized CloseableHttpClient getHttpClient()
			throws KeyManagementException, NoSuchAlgorithmException {
		if (httpClient == null) {
			// 采用绕过验证的方式处理https请求
			SSLContext sslcontext = createIgnoreVerifySSL();
			// 注册访问协议相关的socket工厂
			org.apache.http.config.Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
					.<ConnectionSocketFactory> create()
					.register("http", PlainConnectionSocketFactory.INSTANCE)
					.register("https",
							new SSLConnectionSocketFactory(sslcontext)).build();
			// HttpConnection工厂：配置写请求//解析响应的处理器
			HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory = new ManagedHttpClientConnectionFactory(
					DefaultHttpRequestWriterFactory.INSTANCE,
					DefaultHttpResponseParserFactory.INSTANCE);
			// DNS解析器
			DnsResolver dnsResolver = SystemDefaultDnsResolver.INSTANCE;
			// 创建池化连接器管理器
			manager = new PoolingHttpClientConnectionManager(
					socketFactoryRegistry, connFactory, dnsResolver);
			// 默认为sock配置
			SocketConfig defaultSocketConfig = SocketConfig.custom()
					.setTcpNoDelay(true) // 是否立即发送数据，设置为true会关闭Socket缓冲，默认为false
					/*
					 * .setSoReuseAddress(true) //
					 * 是否可以在一个进程关闭Socket后，即使它还没有释放端口，其它进程还可以立即重用端口
					 * .setSoTimeout(500) // 接收数据的等待超时时间，单位ms .setSoLinger(60)
					 * // 关闭Socket时，要么发送完所有数据，要么等待60s后，就关闭连接
					 * ，此时socket.close()是阻塞的 .setSoKeepAlive(true) //
					 * 开启监视TCP连接是否有效
					 */
					.build();
			manager.setDefaultSocketConfig(defaultSocketConfig);

			manager.setMaxTotal(300);// 设置整个连接池最大连接数
			// 每个路由的默认最大连接，每个路由实际最大连接数默认为太小无法支持大的并发，对maxTotal细分
			manager.setDefaultMaxPerRoute(100);
			// 默认请求配置
			RequestConfig defaultRequestConfig = RequestConfig.custom()
					.setConnectTimeout(20 * 1000) // 连接超时时间
					.setSocketTimeout(60 * 1000) // 读超时时间（等待数据超时时间）
					.setConnectionRequestTimeout(2000) // 从池中获取连接超时时间
					.setStaleConnectionCheckEnabled(true)// 检查是否为陈旧的连接，默认为true，类似testOnBorrow
					.build();
			/**
			 * 重试处理 默认是重试3次
			 */
			// 禁用重试(参数：retryCount、requestSentRetryEnabled)
			HttpRequestRetryHandler requestRetryHandler = new DefaultHttpRequestRetryHandler(
					0, false);
			if("NO".equals(isProxy)){
				httpClient = HttpClients.custom().setConnectionManager(manager)
						// 连接管理器
						 .setProxy(new HttpHost(proxyHost, proxyPort)) //设置代理
								.setDefaultRequestConfig(defaultRequestConfig)
								// 默认请求配置
								// 连接重用策略，即是否能keepalive
								.setConnectionReuseStrategy(
										DefaultConnectionReuseStrategy.INSTANCE)
								// 长连接配置，即获取长连接生产多长时间
								.setKeepAliveStrategy(
										DefaultConnectionKeepAliveStrategy.INSTANCE)

								.setRetryHandler(requestRetryHandler) // 重试策略
								.build();
			}else{
				httpClient = HttpClients.custom().setConnectionManager(manager)
						// 连接管理器
					//	 .setProxy(new HttpHost(proxyHost, proxyPort)) //设置代理
					.setDefaultRequestConfig(defaultRequestConfig)
					// 连接重用策略，即是否能keepalive
					.setConnectionReuseStrategy(
							DefaultConnectionReuseStrategy.INSTANCE)
					// 长连接配置，即获取长连接生产多长时间
					.setKeepAliveStrategy(
							DefaultConnectionKeepAliveStrategy.INSTANCE)
					.setRetryHandler(requestRetryHandler) // 重试策略
					.build();
			}
			// jvm 停止或者重启，关闭连接池释放连接（跟数据库连接池类似）
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						httpClient.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			IdleConnectionMonitorThread  thread=new IdleConnectionMonitorThread(manager);
			thread.start();
		}
		
		return httpClient;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map callGetService(String url,Map logMap, Map paramterMap,
			Map headerMap) {
		logger.debug("---httpGet请求路径：" + url);
		long startTime = System.currentTimeMillis();
		CloseableHttpResponse httpResponse = null;
		Map returnMap = new HashMap();
		String resultData = "";
		String resultCode = "99";
		String resultCodeName = "服务器忙";
		try {
			// 放置参数
			List<NameValuePair> nvps = getParamList(paramterMap);
			// 转换为键值对
			String str = EntityUtils.toString(new UrlEncodedFormEntity(nvps,
					"utf-8"));
			if (url.contains("?")) {
				url = url + str;
			} else {
				url = url + "?" + str;
			}
			// 新建cookie防止税局对cookeie拦截
			CookieStore cookieStore = new BasicCookieStore();
			HttpClientContext context = HttpClientContext.create();
			context.setCookieStore(cookieStore);
			HttpGet httpGet = new HttpGet(url);
			// 获取头信息
			putGetHeader(headerMap, httpGet);
			httpResponse = getHttpClient().execute(httpGet, context);
			HttpEntity returnEntity = httpResponse.getEntity();
			if (returnEntity != null) {
				resultData = EntityUtils.toString(returnEntity, "UTF-8");
				if (StringUtils.isEmpty(resultData)
						|| -1 < resultData.indexOf("400 Bad Request")) {
					resultData = "";
					resultCode = "400 Bad Request";
					resultCodeName = "400 Bad Request找不到页面或者拿不到数据";
				} else if (-1 < resultData
						.indexOf("Error 503--Service Unavailable")) {
					resultData = "";
					resultCode = "Service Unavailable";
					resultCodeName = "Error 503--Service Unavailable";
				} else if (-1 < resultData
						.indexOf("ISAPI plug-in Error Message")) {
					resultData = "";
					resultCode = "Service Unavailable";
					resultCodeName = "ISAPI plug-in Error Message:No backend server available for connection: timed out after 10 seconds or idempotent set to OFF.";
				}else if (-1 < resultData
						.indexOf("Service Unavailable")) {
					resultData = "";
					resultCode = "Service Unavailable";
					resultCodeName = "<h1>Service Unavailable</h1>";
				}else if (-1 < resultData
						.indexOf("Error 500--Internal Server Error")) {
					resultData = "";
					resultCode = "Service Unavailable";
					resultCodeName = "Error 500--Internal Server Error";
				}else {
					resultCode = "SUCCESS";
					resultCodeName = "查询成功:" + resultData;
				}
				EntityUtils.consume(returnEntity);
			} else {
				resultData = "";
				resultCode = "Service Unavailable";
				resultCodeName = "找不到数据Error 500--Service Unavailable";
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
			long endTime = System.currentTimeMillis();
			// 记录日志
			logMap.put("requestConent",
					DataConvertUtil.MapToString(paramterMap));
			logMap.put("errorMsg", resultCodeName);
			logMap.put("errorCode", resultCode);
			if ("SUCCESS".equals(resultCode)) {
				logMap.put("isSuccess", "Y");
			} else {
				logMap.put("isSuccess", "N");
			}
			logMap.put("requestTime", String.valueOf(endTime - startTime));
			try {
				YzmDao.saveRequestLog(logMap);
			} catch (Exception e) {
				logger.error("httpGet获取请求保存异常：" + e.getMessage());
			}
		}
		returnMap.put("resultData", resultData);
		returnMap.put("resultCode", resultCode);
		returnMap.put("resultCodeName", resultCodeName);
		return returnMap;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map callPostService(String url, Map logMap, Map paramterMap,
			Map headerMap) {
		logger.debug("---httpGet请求路径：" + url);
		long startTime = System.currentTimeMillis();
		CloseableHttpResponse httpResponse = null;
		Map returnMap = new HashMap();
		String resultData = "";
		String resultCode = "99";
		String resultCodeName = "服务器忙";
		try {
			// 新建cookie防止税局对cookeie拦截
			CookieStore cookieStore = new BasicCookieStore();
			HttpClientContext context = HttpClientContext.create();
			context.setCookieStore(cookieStore);
			HttpPost httpPost = new HttpPost(url);
			// 获取头信息
			putPostHeader(headerMap, httpPost);
			httpPost.setHeader("Content-Type", "application/json");
			 StringEntity entity =  new StringEntity(DataConvertUtil.MapToString(paramterMap),"utf-8");
			httpPost.setEntity(entity);
			httpResponse = getHttpClient().execute(httpPost, context);
			HttpEntity returnEntity = httpResponse.getEntity();
			if (returnEntity != null) {
				resultData = EntityUtils.toString(returnEntity, "UTF-8");
				if (StringUtils.isEmpty(resultData)
						|| -1 < resultData.indexOf("400 Bad Request")) {
					resultData = "";
					resultCode = "400 Bad Request";
					resultCodeName = "400 Bad Request找不到页面或者拿不到数据";
				} else if (-1 < resultData
						.indexOf("Error 503--Service Unavailable")) {
					resultData = "";
					resultCode = "Service Unavailable";
					resultCodeName = "Error 503--Service Unavailable";
				} else if (-1 < resultData
						.indexOf("ISAPI plug-in Error Message")) {
					resultData = "";
					resultCode = "Service Unavailable";
					resultCodeName = "ISAPI plug-in Error Message:No backend server available for connection: timed out after 10 seconds or idempotent set to OFF.";
				}else if (-1 < resultData
						.indexOf("Service Unavailable")) {
					resultData = "";
					resultCode = "Service Unavailable";
					resultCodeName = "<h1>Service Unavailable</h1>";
				}else if (-1 < resultData
						.indexOf("Error 500--Internal Server Error")) {
					resultData = "";
					resultCode = "Service Unavailable";
					resultCodeName = "Error 500--Internal Server Error";
				}else {
					resultCode = "SUCCESS";
					resultCodeName = resultData;
				}
				EntityUtils.consume(returnEntity);
			} else {
				resultData = "";
				resultCode = "Service Unavailable";
				resultCodeName = "找不到数据Error 500--Service Unavailable";
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
			long endTime = System.currentTimeMillis();
			// 记录日志
			logMap.put("requestConent",
					DataConvertUtil.MapToString(paramterMap));
			logMap.put("errorMsg", resultCodeName);
			logMap.put("errorCode", resultCode);
			if ("SUCCESS".equals(resultCode)) {
				logMap.put("isSuccess", "Y");
			} else {
				logMap.put("isSuccess", "N");
			}
			logMap.put("requestTime", String.valueOf(endTime - startTime));
			try {
				YzmDao.saveRequestLog(logMap);
			} catch (Exception e) {
				logger.error("httpGet获取请求保存异常：" + e.getMessage());
			}
		}
		returnMap.put("resultData", resultData);
		returnMap.put("resultCode", resultCode);
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
	 * 放置get头信息
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
	 * 封装回收不可用线程
	 * @author wuche
	 *
	 */
	public static class IdleConnectionMonitorThread extends Thread {  
	    private  PoolingHttpClientConnectionManager connMgr;  
	    private volatile boolean shutdown;  
	    public IdleConnectionMonitorThread(PoolingHttpClientConnectionManager connMgr) {  
	        super();  
	        this.connMgr = connMgr; 
	    }  
	    @Override  
	    public void run() {  
	        try {  
	            while (!shutdown) {  
	                synchronized (this) {  
	                    wait(3000);  
	                    // Close expired connections  
	                    connMgr.closeExpiredConnections();  
	                    // Optionally, close connections  
	                    // that have been idle longer than 30 sec  
	                    connMgr.closeIdleConnections(5, TimeUnit.SECONDS);  
	                }  
	            }  
	        } catch (InterruptedException ex) {  
	        	logger.error("处理关闭不可用httpClient异常：" + ex);
	        }  
	    }  
	      
	    public void shutdown() {  
	        shutdown = true;  
	        synchronized (this) {  
	            notifyAll();  
	        }  
	    }  
	      
	}  
}