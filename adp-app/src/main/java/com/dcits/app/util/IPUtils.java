package com.dcits.app.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dcits.app.dao.DataWindow;
import com.dcits.app.data.DataObject;

public class IPUtils {
	private static final Log LOG = LogFactory.getLog(IPUtils.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String getCityIdByIp(String ipStr) {
		String cityCode = null;
		if (ipStr != null && StringUtils.isNotBlank(ipStr)) {
			String[] array = ipStr.split("\\.");
			long ipNum = 0;
			for (int i = 0; i < array.length; i++) {
				int power = 3 - i;
				ipNum += ((Integer.parseInt(array[i]) % 256 * Math.pow(256,
						power)));
			}
			Map parameter = new HashMap();
			parameter.put("ipNum", ipNum);
			DataObject dataObject = new DataObject(parameter);
			String result = RedisUtils.getValue("com.dcits.app.util.IPUtils",
					"getCityIdByIp", dataObject);
			if (result == null) {
				synchronized (IPUtils.class) {
					Map map = (Map) DataWindow.queryOne(
							"app.IPUtils_queryIpCode", parameter);
					if (map != null
							&& StringUtils.isNotBlank((String) map
									.get("cityCode"))) {
						result = (String) map.get("cityCode");
					} else {
						result = "1";
					}
					RedisUtils.putValue("com.dcits.app.util.IPUtils",
							"getCityIdByIp", dataObject, result);
				}
			}
			cityCode = result;
			if (cityCode != null && "1".equals(cityCode)) {
				cityCode = null;
			}
		}
		if (cityCode != null && cityCode.length() < 4) {
			cityCode = null;
		}
		return cityCode;
	}

	public static String getRemortIP(HttpServletRequest request) {
		String ip = null;
		try {
			ip = request.getHeader("x-forwarded-for");
			if (StringUtils.isNotBlank(ip)) {
				if (ip.indexOf(",") != -1) {
					String[] array = ip.split(",");
					ip = array[0];
				}
			}
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("WL-Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0
					|| "unknown".equalsIgnoreCase(ip)) {
				ip = request.getRemoteAddr();
			}
		} catch (Throwable e) {
			LOG.error("获取客户端IP出现异常", e);
		}
		return ip;
	}
}
