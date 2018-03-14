package com.dcits.app.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dcits.app.data.DataObject;
import com.dcits.db.config.CharsetConfig;

@SuppressWarnings("serial")
public class ClientIpServlet extends HttpServlet {

	private static final Log LOG = LogFactory.getLog(ClientIpServlet.class);

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("clientIP", getRemortIP(request));
		DataObject dataObject = new DataObject(map);
		String responseJson = dataObject.getJson();
		response.setContentType("text/json");
		response.setCharacterEncoding(CharsetConfig.localCharset);
		PrintWriter out = response.getWriter();
		out.print(responseJson);
		out.flush();
		out.close();
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