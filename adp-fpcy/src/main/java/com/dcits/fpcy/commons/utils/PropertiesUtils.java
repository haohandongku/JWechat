package com.dcits.fpcy.commons.utils;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.dcits.app.util.PropertiesUtil;

public class PropertiesUtils {
	/** 微信配置文件 */
	public final static String FPCY_PROPERTIES_FILENAME = "/config-fpcy.properties";

	@SuppressWarnings("rawtypes")
	public static Map map;

	@SuppressWarnings("rawtypes")
	public static void init() throws IOException {
		Properties pro = PropertiesUtil.getObj().readProperties(FPCY_PROPERTIES_FILENAME);
		if (null != pro) {
			map = (Map) pro;
		}
	}

	public static String getPropertiesValue(String str) {
		return String.valueOf(map.get(str));
	}
}
