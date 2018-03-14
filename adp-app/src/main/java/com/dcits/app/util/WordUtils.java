package com.dcits.app.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;

import com.lowagie.text.DocumentException;

public class WordUtils {

	private static String charSet = "utf-8";

	public static void createWord(HttpServletResponse response, String fileName,
			String templateFilePath, Map<String, String> data)
			throws IOException, TransformerException, DocumentException {
		InputStream inputStream = WordUtils.class
				.getResourceAsStream(templateFilePath);
		HWPFDocument document = new HWPFDocument(inputStream);
		Range range = document.getRange();
		for (Map.Entry<String, String> entry : data.entrySet()) {
			range.replaceText("#" + entry.getKey(), entry.getValue());
		}
		fileName = URLEncoder.encode(fileName, charSet);
		response.addHeader("Content-Disposition", "attachment; filename="
				+ fileName + ".doc");
		response.setContentType("application/x-msdownload");
		document.write(response.getOutputStream());
	}

}