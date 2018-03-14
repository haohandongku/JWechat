package com.dcits.app.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ReflectionUtils;

import com.dcits.app.constant.Constant;
import com.dcits.app.data.DataObject;
import com.dcits.app.util.ApplicationContextUtils;
import com.dcits.app.util.JacksonUtils;
import com.dcits.db.config.CharsetConfig;

@SuppressWarnings("serial")
public class FileUploadServlet extends HttpServlet {

	private static final Log LOG = LogFactory.getLog(FileUploadServlet.class);

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		final HttpSession httpSession = request.getSession();
		try {
			if (!ServletFileUpload.isMultipartContent(request)) {
				PrintWriter writer = response.getWriter();
				writer.println("Form must has enctype=multipart/form-data");
				writer.flush();
				return;
			}
			@SuppressWarnings("deprecation")
			DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setProgressListener(new ProgressListener() {
				public void update(long pBytesRead, long pContentLength,
						int pItems) {
					int rate = Math.round(new Float(pBytesRead)
							/ new Float(pContentLength) * 100);
					httpSession.setAttribute(Constant.PROGRESS_INFO_KEY, rate);
				}
			});
			upload.setHeaderEncoding(CharsetConfig.localCharset);
			List<FileItem> fileList = upload.parseRequest(request);
			List<FileItem> list = new ArrayList<FileItem>();
			Map map = new HashMap();
			for (FileItem fileItem : fileList) {
				if (fileItem.isFormField()) {
					String fieldName = fileItem.getFieldName();
					String fieldValue = fileItem
							.getString(CharsetConfig.localCharset);
					map.put(fieldName, fieldValue);
				} else {
					list.add(fileItem);
				}
			}
			String serviceName = (String) request
					.getParameter(Constant.SERVICE_NAME);
			if (serviceName == null) {
				serviceName = (String) map.get(Constant.SERVICE_NAME);
			}
			String methodName = (String) request
					.getParameter(Constant.METHOD_NAME);
			if (methodName == null) {
				methodName = (String) map.get(Constant.METHOD_NAME);
			}
			String requestJson = (String) request
					.getParameter(Constant.REQUEST_JSON);
			if (requestJson == null) {
				requestJson = (String) map.get(Constant.REQUEST_JSON);
			}
			Map parameter = JacksonUtils.getMapFromJson(requestJson);
			parameter.put(Constant.HTTP_SERVLET_RESPONSE, response);
			parameter.put(Constant.FILELIST, list);
			Object service = ApplicationContextUtils.getContext().getBean(
					serviceName);
			Method method = ReflectionUtils.findMethod(service.getClass(),
					methodName, new Class[] { DataObject.class });
			DataObject dataObject = new DataObject(parameter);
			ReflectionUtils.invokeMethod(method, service, dataObject);
		} catch (Throwable e) {
			LOG.error("文件上传时出现异常：", e);
		}
	}

}