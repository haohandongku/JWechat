package com.dcits.app.aop;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.dcits.app.asynctask.AsyncTaskService;
import com.dcits.app.data.DataObject;
import com.dcits.app.util.ApplicationContextUtils;

@SuppressWarnings("deprecation")
public class LogInterceptor implements MethodInterceptor {

	private static final Log LOG = LogFactory.getLog(LogInterceptor.class);
	private static DocumentBuilderFactory factory = DocumentBuilderFactory
			.newInstance();

	@Override
	public Object invoke(final MethodInvocation method) throws Throwable {
		long begin = 0;
		if (LOG.isDebugEnabled()) {
			begin = System.currentTimeMillis();
		}
		final Object result = method.proceed();
		long end = System.currentTimeMillis();
		if (LOG.isDebugEnabled()) {
			final Object target = method.getThis();
			final Object[] arguments = method.getArguments();
			final LogInterceptor instance = this;
			final long cost = end - begin;
			AsyncTaskService.getInstance().execute(new Runnable() {

				@Override
				public void run() {
					instance.debug(result, method.getMethod(), arguments,
							target, cost);
				}
			});

		}
		return result;
	}

	private void debug(Object result, Method mehtod, Object[] arguments,
			Object target, long cost) {
		if (LOG.isDebugEnabled()) {
			try {
				String serviceName = ApplicationContextUtils
						.getBeanNameByBeanTarget(target);
				if (StringUtils.isBlank(serviceName)) {
					serviceName = "未知ID";
				}

				StringBuffer sb = new StringBuffer();
				sb.append("服务调用结束，服务ID：" + serviceName + "，类名："
						+ target.getClass().getName() + "，方法名："
						+ mehtod.getName() + "，");

				if (arguments.length > 0) {
					if (arguments.length == 1
							&& arguments[0] instanceof DataObject) {
						sb.append(" \n 输入参数：\n"
								+ format(arguments[0].toString()));
					}
				} else {
					sb.append(" \n 无输入参数。" + " \n 总耗时：" + cost + "毫秒。");
				}

				if (result instanceof DataObject) {
					sb.append(" \n 返回值：\n" + format(result.toString())
							+ " \n 耗时：" + cost + "毫秒。");
				}
				LOG.debug(sb.toString());
			} catch (Throwable e) {
				LOG.error("日志记录时出现异常：", e);
			}
		}
	}

	private static String format(String xml) {
		xml = xml.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
		try {
			Document document = parse(xml);
			OutputFormat format = new OutputFormat(document);
			format.setLineWidth(65);
			format.setIndenting(true);
			format.setIndent(2);
			Writer writer = new StringWriter();
			XMLSerializer serializer = new XMLSerializer(writer, format);
			serializer.serialize(document);
			return writer.toString();
		} catch (Exception e) {
		}
		return xml;
	}

	private static Document parse(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource inputSource = new InputSource(new StringReader(xml));
		return builder.parse(inputSource);
	}

}