package com.dcits.app.servlet;

import java.io.IOException;
import java.lang.reflect.Method;
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
import com.dcits.app.util.ApplicationContextUtils;
import com.dcits.app.util.JacksonUtils;

@SuppressWarnings("serial")
public class FileDownloadServlet extends HttpServlet {

	private static final Log LOG = LogFactory.getLog(FileDownloadServlet.class);

	@Override
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
		try {
			requestJson = new String(requestJson.getBytes("iso8859-1"), "utf-8");
			Map parameter = JacksonUtils.getMapFromJson(requestJson);
			parameter.put(Constant.HTTP_SERVLET_REQUEST, request);
			parameter.put(Constant.HTTP_SERVLET_RESPONSE, response);
			DataObject dataObject = new DataObject(parameter);
			Object service = ApplicationContextUtils.getContext().getBean(
					serviceName);
			Method method = ReflectionUtils.findMethod(service.getClass(),
					methodName, new Class[] { DataObject.class });
			ReflectionUtils.invokeMethod(method, service, dataObject);
		} catch (Throwable e) {
			LOG.error("文件下载时出现异常：", e);
		}
	}

}