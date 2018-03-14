package com.dcits.app.util;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

public class Excel07Utils {
	private static Font titleFont = null;
	private static CellStyle titleStyle = null;
	private static CellStyle contentStyle = null;

	@SuppressWarnings({ "rawtypes" })
	public static void writeExcel(HttpServletRequest request,HttpServletResponse response,
			String fileName, String sheetName, String[] titles, String[] keys,
			List<Object> datas) throws IOException {
		final String userAgent = request.getHeader("USER-AGENT");
		if(StringUtils.contains(userAgent, "MSIE")){//IE浏览器
			fileName = URLEncoder.encode(fileName,"UTF8");
        }else if(StringUtils.contains(userAgent, "Mozilla")){//google,火狐浏览器
        	fileName = new String(fileName.getBytes(), "ISO8859-1");
        }else{
        	fileName = URLEncoder.encode(fileName,"UTF8");//其他浏览器
        }
		//fileName = URLEncoder.encode(fileName, "UTF-8");
		response.setHeader("Content-disposition", "attachment; filename="
				+ fileName + ".xlsx");
		response.setContentType("application/vnd.ms-excel");
		int rowaccess = 100;
		SXSSFWorkbook wb = new SXSSFWorkbook(rowaccess);
		setExcelStyle(wb);
		Sheet sh = wb.createSheet();
		Row row = sh.createRow(0);
		int titleCount = titles.length;
		for (int k = 0; k < titleCount; k++) {
			Cell cell = row.createCell(k);
			cell.setCellStyle(titleStyle);
			cell.setCellType(XSSFCell.CELL_TYPE_STRING);
			cell.setCellValue(titles[k]);
			sh.setColumnWidth((short) k, (short) 5000);
		}

		List<String[]> contentList = new ArrayList<String[]>();
		if (CollectionUtils.isNotEmpty(datas)) {
			for (int i = 0; i < datas.size(); i++) {
				String[] contents = new String[titleCount];
				Map tmp = (Map) datas.get(i);
				for (int j = 0; j < titleCount; j++) {
					contents[j] = String.valueOf(tmp.get(keys[j]));
				}
				contentList.add(contents);
			}
		}
		int contentCount = contentList.size();
		for (int i = 0; i < contentCount; i++) {
			String[] contents = contentList.get(i);
			Row row2 = sh.createRow((int) (i + 1));
			for (int j = 0; j < titleCount; j++) {
				Cell cell = row2.createCell(j);
				cell.setCellStyle(contentStyle);
				if (contents[j] == null || "null".equals(contents[j])) {
					contents[j] = "";
				}
				cell.setCellValue(new XSSFRichTextString(contents[j]));
			}
			if (i % rowaccess == 0) {
				((SXSSFSheet) sh).flushRows();
			}
		}
		ServletOutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
	}

	public static void setExcelStyle(SXSSFWorkbook workBook) {
		titleFont = workBook.createFont();
		titleFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
		titleStyle = workBook.createCellStyle();
		titleStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
		titleStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		titleStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		titleStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
		titleStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
		titleStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
		titleStyle.setFont(titleFont);
		contentStyle = workBook.createCellStyle();
		contentStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
		contentStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		contentStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		contentStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
		contentStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
		contentStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
	}

}
