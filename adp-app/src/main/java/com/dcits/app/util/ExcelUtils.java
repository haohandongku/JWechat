package com.dcits.app.util;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.NumberFormats;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

public class ExcelUtils {

	public static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	public static WritableWorkbook createWorkbook(HttpServletResponse response,
			String fileName, String sheetName, String[] titles, String[] keys,
			List<Object> datas) throws Exception {
		fileName = URLEncoder.encode(fileName, "UTF-8");
		response.setHeader("Content-disposition", "attachment; filename="
				+ fileName + ".xls");
		response.setContentType("application/vnd.ms-excel");
		WritableWorkbook wwb = Workbook.createWorkbook(response
				.getOutputStream());
		if (datas != null) {
			createSheet(wwb, 0, sheetName, titles, keys, datas);
		}
		return wwb;
	}

	@SuppressWarnings("rawtypes")
	public static WritableSheet createSheet(WritableWorkbook wwb, int index,
			String sheetName, String[] titles, String[] keys, List<Object> datas)
			throws Exception {
		WritableSheet sheet = wwb.createSheet(sheetName, index);
		if (ArrayUtils.isNotEmpty(titles)) {
			// 设置标题样式
			jxl.write.WritableFont font = new jxl.write.WritableFont(
					WritableFont.ARIAL, 11, WritableFont.BOLD);
			WritableCellFormat format = new WritableCellFormat(font);
			format.setAlignment(Alignment.CENTRE);// 居中对齐
			format.setVerticalAlignment(VerticalAlignment.CENTRE);
			format.setWrap(true); // 标题不自动换行
			CellView cellView = new CellView();
			cellView.setFormat(format);
			// cellView.setSize(1125 * 11256);
			// cellView.setAutosize(true); // 设置自动大小
			for (int i = 0; i < titles.length; i++) {
				WritableCell label = putInCell(i, 0, titles[i], format);
				sheet.addCell(label);
				sheet.setColumnView(i, cellView);// 根据内容自动设置列宽
			}
		}

		if (CollectionUtils.isNotEmpty(datas)) {
			WritableCellFormat format = new WritableCellFormat();
			format.setAlignment(Alignment.CENTRE);// 居中对齐
			format.setVerticalAlignment(VerticalAlignment.CENTRE);
			format.setWrap(true); // 标题不自动换行
			CellView cellView = new CellView();
			cellView.setSize(25 * 156);
			// cellView.setAutosize(true); // 设置自动大小
			for (int i = 1; i <= datas.size(); i++) {
				Map data = (Map) datas.get(i - 1);
				if (ArrayUtils.isNotEmpty(keys)) {
					for (int j = 0; j < keys.length; j++) {
						WritableCell label = putInCell(j, i, data.get(keys[j]),
								format);
						sheet.addCell(label);
						sheet.setColumnView(j, cellView);// 根据内容自动设置列宽
					}
				}
			}
		} else {
//			CellView cellView = new CellView();
//			cellView.setSize(25 * 156);
//			if (ArrayUtils.isNotEmpty(keys)) {
//				for (int j = 0; j < keys.length; j++) {
//					WritableCell label = putInCell(j, 1, "", null);
//					sheet.addCell(label);
//					sheet.setColumnView(j, cellView);
//				}
//			}
		}
		return sheet;
	}

	public static WritableCell putInCell(int i, int j, Object object,
			WritableCellFormat format) throws WriteException {
		String value = "";
		if (object != null) {
			if (object instanceof Long) {
				if (format == null) {
					format = new WritableCellFormat(NumberFormats.INTEGER);
					format.setAlignment(Alignment.CENTRE);
				}
				jxl.write.Number number = new jxl.write.Number(i, j,
						Long.valueOf((Long) object), format);
				return number;
			} else if (object instanceof Integer) {
				if (format == null) {
					format = new WritableCellFormat(NumberFormats.INTEGER);
					format.setAlignment(Alignment.CENTRE);
				}
				jxl.write.Number number = new jxl.write.Number(i, j,
						Integer.valueOf((Integer) object), format);
				return number;
			} else if (object instanceof Float) {
				if (format == null) {
					format = new WritableCellFormat(NumberFormats.FLOAT);
					format.setAlignment(Alignment.CENTRE);
				}
				jxl.write.Number number = new jxl.write.Number(i, j,
						Float.valueOf((Float) object), format);
				return number;
			} else if (object instanceof Double) {
				if (format == null) {
					format = new WritableCellFormat(NumberFormats.FLOAT);
					format.setAlignment(Alignment.CENTRE);
				}
				jxl.write.Number number = new jxl.write.Number(i, j,
						Double.valueOf((Double) object), format);
				return number;
			} else if (object instanceof Date) {
				value = df.format(object);
			} else {
				value = object.toString();
			}
		}
		if (format == null) {
			format = new WritableCellFormat(NumberFormats.TEXT);
			format.setVerticalAlignment(VerticalAlignment.CENTRE);
			format.setWrap(true);
		}
		return new Label(i, j, value, format);
	}

	public static WritableCell putInCell(int i, int j, Object object)
			throws WriteException {
		return putInCell(i, j, object, null);
	}

	public static void close(WritableWorkbook wwb) throws Exception {
		if (wwb != null) {
			wwb.write();
			wwb.close();
		}
	}
}