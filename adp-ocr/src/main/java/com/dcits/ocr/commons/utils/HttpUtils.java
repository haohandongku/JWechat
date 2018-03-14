package com.dcits.ocr.commons.utils;

import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.SocketAddress;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HttpUtils {
	private static Log LOG = LogFactory.getLog(HttpUtils.class);
	private static Proxy proxy;
	private static SSLSocketFactory sslSocketFactory;

	private static String isProxy;
	private static String httpProxyUsername;
	private static String httpProxyPassword;
	private static String proxyHost;
	private static int proxyPort;

	public static HttpURLConnection getConnection(URL url) {
		HttpURLConnection connection = null;
		if (null == url) {
			return null;
		}
		try {
			if ("YES".equals(isProxy)) {
				LOG.debug("httpUtils进入代理模式");
				if (null == proxy) {
					initProxy();
				}
				LOG.debug("proxy = "+proxy);
				connection = (HttpURLConnection) url.openConnection(proxy);
			} else {
				connection = (HttpURLConnection) url.openConnection();
			}
			if (connection instanceof HttpsURLConnection) {
				HttpsURLConnection https = (HttpsURLConnection) connection;
				if (null == sslSocketFactory) {
					initSSLSocketFactory();
				}
				https.setSSLSocketFactory(sslSocketFactory);
			}
			return connection;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return connection;
	}

	private static void initProxy() {
		LOG.debug("httpUtils初始化代理");
		SocketAddress sa = new InetSocketAddress(proxyHost, proxyPort);
		proxy = new Proxy(Type.HTTP, sa);
		if (httpProxyUsername != null && httpProxyPassword != null && !"".equals(httpProxyUsername)
				&& !"".equals(httpProxyPassword)) {
			Authenticator.setDefault(new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(httpProxyUsername, httpProxyPassword.toCharArray());
				}
			});
		}
	}

	private static void initSSLSocketFactory() {
		// 创建SSLContext对象，并使用我们指定的信任管理器初始化
		TrustManager[] tm = { new MyTrustManager() };
		SSLContext sslContext;
		try {
			sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			sslSocketFactory = sslContext.getSocketFactory();
		} catch (NoSuchAlgorithmException e) {
			LOG.error(e.getMessage());
		} catch (NoSuchProviderException e) {
			LOG.error(e.getMessage());
		} catch (KeyManagementException e) {
			LOG.error(e.getMessage());
		}

	}

	public static void setIsProxy(String isProxy) {
		HttpUtils.isProxy = isProxy;
	}

	public static void setHttpProxyUsername(String httpProxyUsername) {
		HttpUtils.httpProxyUsername = httpProxyUsername;
	}

	public static void setHttpProxyPassword(String httpProxyPassword) {
		HttpUtils.httpProxyPassword = httpProxyPassword;
	}

	public static void setProxyHost(String proxyHost) {
		HttpUtils.proxyHost = proxyHost;
	}

	public static void setProxyPort(int proxyPort) {
		HttpUtils.proxyPort = proxyPort;
	}

}
