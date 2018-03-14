package com.dcits.app.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class HtmlUtils {

	public static void exportHtml(HttpServletResponse response,
			String xslFilePath, String xmlData) throws Throwable {
		StreamSource xslStreamSource = getStreamSourceFromFilePath(xslFilePath);
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer(xslStreamSource);

		StreamSource xmlStreamSource = new StreamSource(
				new ByteArrayInputStream(xmlData.getBytes("utf-8")));

		StringWriter stringWriter = new StringWriter();
		Result result = new StreamResult(stringWriter);
		transformer.transform(xmlStreamSource, result);
		String html = stringWriter.toString();

		response.setContentType("text/html; charset=utf-8");
		ServletOutputStream servletOutputStream = response.getOutputStream();
		servletOutputStream.write(html.getBytes("utf-8"));

	}

	private static StreamSource getStreamSourceFromFilePath(String filePath)
			throws Throwable {
		String path = Thread.currentThread().getContextClassLoader()
				.getResource("").getPath();
		InputStream xslInputStream = new FileInputStream(path + filePath);
		BufferedInputStream xslBufferedInputStream = new BufferedInputStream(
				xslInputStream);
		ByteArrayOutputStream xslOutputStream = new ByteArrayOutputStream();
		byte[] temp = new byte[1024];
		int i;
		while ((i = xslBufferedInputStream.read(temp)) != -1) {
			xslOutputStream.write(temp, 0, i);
		}
		BufferedInputStream xlsBufferedInputStream = new BufferedInputStream(
				new ByteArrayInputStream(xslOutputStream.toByteArray()));
		StreamSource xslStreamSource = new StreamSource(xlsBufferedInputStream);
		return xslStreamSource;
	}

}