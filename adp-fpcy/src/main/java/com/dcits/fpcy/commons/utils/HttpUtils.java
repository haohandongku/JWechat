package com.dcits.fpcy.commons.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;

import com.dcits.fpcy.commons.bean.IpAddress;

/**
 * 对应原来的Proxy1
 * @author wuche
 *
 */
public class HttpUtils {
	
	public static HttpURLConnection getConnection(URL url,IpAddress ipAddress){
		ipAddress = null;
		HttpURLConnection connection = null;
		if(null == url){
			return null;
		}
		try {
			if(null == ipAddress){
				connection = (HttpURLConnection) url.openConnection();
			}/*else{
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ipAddress.getIp(), ipAddress.getPort()));
				connection = (HttpURLConnection) url.openConnection(proxy);
			}*/
			return connection;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static HttpsURLConnection getConnection(URL url,IpAddress ipAddress,String https){
		if(null == url || null == ipAddress){
			return null;
		}
		try {
			HttpsURLConnection connection = null;
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ipAddress.getIp(), ipAddress.getPort()));
			connection = (HttpsURLConnection) url.openConnection(proxy);
			return connection;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取https连接(2016.3.23修改)
	 * @param url
	 * @return
	 */
	public static HttpsURLConnection getHttpsConnection(String url,IpAddress ipAddress){
		HttpsURLConnection httpsCon = null ;
		
		SSLContext sslContext = SSLContexts.createDefault();
		
		if(null == url && null == ipAddress){
			return null;
		}
		
		try {
			sslContext = SSLContexts.custom()
					.loadTrustMaterial(null, new TrustStrategy() {
						public boolean isTrusted(X509Certificate[] chain,
								String authType) throws CertificateException {
							return true;
						}
					}).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		SSLSocketFactory ssf = sslContext.getSocketFactory();
		try {
			URL myURL = new URL(url);
			if(null == ipAddress){
				httpsCon = (HttpsURLConnection) myURL.openConnection();
			}else{
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ipAddress.getIp(), ipAddress.getPort()));
				httpsCon = (HttpsURLConnection) myURL.openConnection(proxy);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		httpsCon.setSSLSocketFactory(ssf);
		return httpsCon;
	}
}
