package com.dcits.app.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ReflectionUtils;

import com.dcits.app.constant.Constant;
import com.dcits.app.data.DataObject;
import com.dcits.app.exception.BizRuntimeException;
import com.dcits.app.util.ApplicationContextUtils;
import com.dcits.app.util.JacksonUtils;
import com.dcits.db.config.CharsetConfig;

@SuppressWarnings("serial")
public class WebServlet extends HttpServlet {

	private static final Log LOG = LogFactory.getLog(WebServlet.class);

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String serviceName = (String) request
				.getParameter(Constant.SERVICE_NAME);
		String methodName = (String) request.getParameter(Constant.METHOD_NAME);
		String requestJson = (String) request
				.getParameter(Constant.REQUEST_JSON);
		String responseJson = null;
		try {
			Map parameter = JacksonUtils.getMapFromJson(requestJson);
			if (parameter.get(Constant.SESSION_VALIDCODE_KEY) != null) {
				String key = (String) parameter
						.get(Constant.SESSION_VALIDCODE_KEY);
				if (request.getSession().getAttribute(key) != null) {
					parameter.put(key, request.getSession().getAttribute(key));
				}
			}
			DataObject dataObject = new DataObject(parameter);
			Object service = ApplicationContextUtils.getContext().getBean(
					serviceName);
			Method method = ReflectionUtils.findMethod(service.getClass(),
					methodName, new Class[] { DataObject.class });
			DataObject result = (DataObject) ReflectionUtils.invokeMethod(
					method, service, dataObject);
			result.getMap().put(Constant.RTN_CODE, Constant.RTN_CODE_SUCCESS);
			responseJson = result.getJson();
		} catch (Throwable e) {
			Map map = new HashMap();
			map.put(Constant.RTN_CODE, Constant.RTN_CODE_FAILURE);
			if (e instanceof BizRuntimeException) {
				map.put(Constant.BIZ_MSG, e.getMessage());
			}
			responseJson = JacksonUtils.getJsonFromMap(map);
			LOG.error("业务处理时出现异常：", e);
		}
		response.setContentType("text/json");
		response.setCharacterEncoding(CharsetConfig.localCharset);
		PrintWriter out = response.getWriter();
		LOG.debug("\n前台请求的serviceName: " + serviceName + "  methodName："
				+ methodName + "\n后台返回的json数据：" + responseJson);
		out.print(responseJson);
		out.flush();
		out.close();
	}

}