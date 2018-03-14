package com.dcits.fpcy.commons.utils;

import java.net.InetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dcits.fpcy.commons.bean.IpAddress;



public class IpUtils {
	private static Log logger = LogFactory.getLog(IpUtils.class);
	 @SuppressWarnings("static-access")
		public static String getIp() {
	    	InetAddress ia=null;
	    	String localip = null;
	    	String localname = null;
	        try {
	            ia=ia.getLocalHost();
	            localname=ia.getHostName();
	            localip=ia.getHostAddress();
	        } catch (Exception e) {
	        	logger.error("获取IP异常");
	        }
	        return localname+localip;
	    }
	 
	 @SuppressWarnings("static-access")
	public static String getLocalIp() {
	    	InetAddress ia=null;
	    	String localip = null;
	        try {
	        	ia=ia.getLocalHost();
	            localip=ia.getHostAddress();
	        } catch (Exception e) {
	        	logger.error("获取IP异常");
	        }
	        return localip;
	    }
	 public static IpAddress get() {
	 		 return new IpAddress("58.53.128.130", 1111);
		}
}
