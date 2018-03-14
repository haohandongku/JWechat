package com.dcits.app.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.List;

import javax.swing.filechooser.FileSystemView;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class CsvUtils {

	public static void createCsv(String fileName, String[] titles,
			List<String[]> datas) throws Exception {
		File desktopDir = FileSystemView.getFileSystemView().getHomeDirectory();
		String desktopPath = desktopDir.getAbsolutePath();// 获取桌面路径
		File csvFile = File.createTempFile(fileName, ".csv", new File(
				desktopPath));
		Writer writer = new FileWriter(csvFile);
		CSVWriter csvWriter = new CSVWriter(writer, ',');
		if (null != titles && 0 < titles.length) {
			csvWriter.writeNext(titles);
		}
		for (String[] values : datas) {
			csvWriter.writeNext(values);
		}
		csvWriter.close();
	}

	public static List<String[]> getCsvData(InputStreamReader reader,
			boolean withTitles) throws IOException {
		CSVReader csvReader = new CSVReader(reader);
		if (!withTitles) {
			csvReader.readNext();
		}
		List<String[]> list = csvReader.readAll();
		csvReader.close();
		return list;
	}

}