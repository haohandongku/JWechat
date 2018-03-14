package com.dcits.app.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class PropertiesUtil {

	private static PropertiesUtil readProperties = null;

	public static PropertiesUtil getObj() {
		if (null == readProperties) {
			readProperties = new PropertiesUtil();
		}
		return readProperties;
	}

	public Properties readProperties(String path) {
		URL url = this.getClass().getResource(path);
		InputStream in = null;
		Properties properties = null;
		try {
			in = url.openStream();
			properties = new Properties();
			properties.load(in);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != in) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return properties;
	}
}
