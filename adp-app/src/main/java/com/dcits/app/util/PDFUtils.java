package com.dcits.app.util;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.PDFCreationListener;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

public class PDFUtils {

	private static String charSet = "utf-8";
	private static String fontKt = "fs.ttf";
	private static String fontHt = "ht.ttf";
	private static String fontFs = "kt.ttf";

	private static class HeaderAndFooter extends PdfPageEventHelper {

		private String header;
		private Font font;
		private PdfTemplate pdfTemplate;

		public HeaderAndFooter(String header, Font font) {
			this.header = header;
			this.font = font;
		}

		public void onOpenDocument(PdfWriter writer, Document document) {
			pdfTemplate = writer.getDirectContent().createTemplate(100, 100);
		}

		@Override
		public void onEndPage(PdfWriter writer, Document document) {
			Rectangle rectangle = writer.getBoxSize("art");
			// 页眉
			if (StringUtils.isNotBlank(this.header)) {
				ColumnText.showTextAligned(writer.getDirectContent(),
						Element.ALIGN_CENTER, new Phrase(this.header, font),
						(rectangle.getLeft() + rectangle.getRight()) / 2,
						rectangle.getTop() + 40, 0);
			}

			// 页脚
			PdfContentByte pdfContentByte = writer.getDirectContent();
			pdfContentByte.saveState();
			String text = writer.getPageNumber() + " / ";
			float textSize = font.getBaseFont().getWidthPoint(text,
					font.getSize());
			pdfContentByte.beginText();
			pdfContentByte.setTextMatrix(
					(document.left() + document.right()) / 2,
					document.bottom() + 5);
			pdfContentByte.setFontAndSize(font.getBaseFont(), font.getSize());
			pdfContentByte.showText(text);
			pdfContentByte.endText();
			pdfContentByte.addTemplate(pdfTemplate,
					((document.left() + document.right()) / 2) + textSize,
					document.bottom() + 5);
			pdfContentByte.saveState();
		}

		@Override
		public void onCloseDocument(PdfWriter pdfwriter, Document document) {
			pdfTemplate.beginText();
			pdfTemplate.setTextMatrix(0, 0);
			pdfTemplate.setFontAndSize(font.getBaseFont(), font.getSize());
			pdfTemplate.showText("" + (pdfwriter.getPageNumber() - 1));
			pdfTemplate.endText();
		}

	}

	public static void createPdf(HttpServletResponse response, String fileName,
			String xslFilePath, String xmlData, String header)
			throws IOException, TransformerException, DocumentException {
		InputStream xslInputStream = PDFUtils.class
				.getResourceAsStream(xslFilePath);

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

		TransformerFactory factory = TransformerFactory.newInstance(
				"org.apache.xalan.processor.TransformerFactoryImpl", null);
		Transformer transformer = factory.newTransformer(xslStreamSource);

		Properties properties = transformer.getOutputProperties();
		properties.setProperty(OutputKeys.ENCODING, charSet);
		properties.setProperty(OutputKeys.METHOD, "xml");

		StreamSource xmlDataStreamSource = new StreamSource(
				new ByteArrayInputStream(xmlData.getBytes(charSet)));
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		StreamResult streamResult = new StreamResult(byteArrayOutputStream);
		transformer.transform(xmlDataStreamSource, streamResult);

		ITextRenderer renderer = new ITextRenderer();
		renderer.setDocumentFromString(new String(byteArrayOutputStream
				.toByteArray(), charSet));

		ITextFontResolver fontResolver = renderer.getFontResolver();
		fontResolver.addFont(getFontPath(fontKt), BaseFont.IDENTITY_H,
				BaseFont.NOT_EMBEDDED);
		fontResolver.addFont(getFontPath(fontHt), BaseFont.IDENTITY_H,
				BaseFont.NOT_EMBEDDED);
		fontResolver.addFont(getFontPath(fontFs), BaseFont.IDENTITY_H,
				BaseFont.NOT_EMBEDDED);

		renderer.layout();

		Font font = new Font((BaseFont.createFont(getFontPath(fontFs),
				BaseFont.IDENTITY_H, BaseFont.EMBEDDED)), 8);
		final HeaderAndFooter headerAndFooter = new HeaderAndFooter(header,
				font);

		renderer.setListener(new PDFCreationListener() {

			@Override
			public void preOpen(ITextRenderer renderer) {
				Rectangle rect = new Rectangle(36, 54, 559, 788);
				rect.setBorderColor(Color.BLACK);
				renderer.getWriter().setBoxSize("art", rect);
				renderer.getWriter().setPageEvent(headerAndFooter);
			}

			@Override
			public void onClose(ITextRenderer itextrenderer) {
			}

		});

		// fileName = URLEncoder.encode(fileName, charSet);
		fileName = new String(fileName.getBytes("GB2312"), "ISO-8859-1");
		response.setHeader("Content-disposition", "attachment; filename="
				+ fileName + ".pdf");
		response.setContentType("application/pdf");
		renderer.createPDF(response.getOutputStream());
	}

	private static String getFontPath(String font) throws IOException {
		String fontPath = XtSvcUtils.getXtcs("40001");
		return fontPath + font;
	}

}