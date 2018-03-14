package com.dcits.fpcy.commons.utils;

import java.io.FileReader;
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
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.dcits.fpcy.commons.constant.BrowerType;
import com.dcits.fpcy.commons.constant.HeaderType;

public class test {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) throws Exception {
		Map HeaderMap = new HashMap<String, String>();
		String url = "https://fpcyweb.zjtax.gov.cn/WebQuery/yzmQuery?";
		HeaderMap.put("Referer", "https://inv-veri.chinatax.gov.cn/index.html");
		HeaderMap.put("Host", "fpcyweb.zjtax.gov.cn");
		HeaderMap.put("Connection", "keep-alive");
		HeaderMap.put("Accept", "*/*");
		HeaderMap.put("Accept-Encoding", "gzip, deflate, sdch");
		HeaderMap.put("Accept-Language", "zh-CN,zh;q=0.8");
		HeaderMap.put(HeaderType.USERAGENT, BrowerType.google2);
		Map<String, String> mar = new HashMap<String, String>();
		String nowtime=String.valueOf(System.currentTimeMillis());
		String fpdm="3300171320";
		mar.put("fpdm",fpdm);
		mar.put("callback",
				"jQuery110205222222650445494_" +String.valueOf(System.currentTimeMillis()));
		double rad = Math.random();
		mar.put("r", String.valueOf(rad));
		mar.put("nowtime",nowtime);
		//获取publicKey
		String publicKey=getKey(fpdm,nowtime);
		mar.put("publicKey", publicKey);
		//System.out.println(new DataObject(mar).getJson());
		String  urlparam="callback="+mar.get("callback")+"&fpdm="+fpdm
				+"&r="+mar.get("r")+"&nowtime="+nowtime+"&publickey="+publicKey
				+"&_="+String.valueOf(System.currentTimeMillis());
		System.out.println(get(url, urlparam, mar, mar, mar, 0));
	}
	
	
    @SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	public static Map get(String url,String urlParam,Map logMap, Map paramterMap,
			Map headerMap,int i){
    	CloseableHttpClient httpClient = null;
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
		/*	String str = EntityUtils.toString(new UrlEncodedFormEntity(nvps,
					"utf-8"));*/
			if (url.contains("?")) {
				url = url + urlParam;
			} else {
				url = url + "?" + urlParam;
			}
			// 新建cookie防止税局对cookeie拦截
			// 连接池管理器
		    PoolingHttpClientConnectionManager connMgr=null;
			CookieStore cookieStore = new BasicCookieStore();
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
						.build();
			} else {
				httpClient = HttpClients.custom()
						.setConnectionManager(connMgr)
						.build();
			}
			HttpClientContext context = HttpClientContext.create();
			context.setCookieStore(cookieStore);
			HttpGet httpGet = new HttpGet(url);
			// 获取头信息
			putGetHeader(headerMap, httpGet);
			httpResponse =httpClient.execute(httpGet, context);
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
			resultCodeName = e.getClass().getName();
			resultCode = e.getMessage();
		} finally {
			// 消耗实体内容
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (IOException e) {
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
			} catch (Exception e) {
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
	
	

	public static String getKey(String fpdm, String nowTime) {
		try {
			StringBuilder jsBuilder = new StringBuilder("jQuery.ck('");
			jsBuilder.append(fpdm).append("','");
			jsBuilder.append(nowTime).append("')");
			Object result = evalFile().eval(jsBuilder.toString());
			return (String) result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static ScriptEngine evalFile(){
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("javascript");
		String path = "";
	/*	String folderPath = Path.class.getProtectionDomain()
				.getCodeSource().getLocation().getPath();
		if (folderPath.indexOf("WEB-INF") > 0) {
			path = folderPath.substring(0,
					folderPath.indexOf("WEB-INF"));
					,"aes.js", "AesUtil.js", "pbkdf2.js", "m.q.d.min.js", "q.b.a.min.js" ,"t.q.d.min.js","s.d.b.min.js
		}*/
		
		//String filepath = path + "static/js/";
		String filepath="E:\\work2\\newfpcy\\adp\\WebRoot\\static\\js\\";
		//新增需求
		String[] publicKeyFiles = new String[] {"base.js" , "m.q.d.min.js", "q.b.a.min.js" ,"t.q.d.min.js","s.d.b.min.js"};
		try {
			for (int i = 0; i < publicKeyFiles.length; i++) {
				FileReader fr = new FileReader(filepath + publicKeyFiles[i]);
				engine.eval(fr);   
			}
		} catch (Exception e) {
		}
		return engine;
	}
}
