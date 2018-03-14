package com.dcits.app.dao;

import java.util.List;

import com.dcits.app.constant.DmSequenceName;

public class DataWindow {

	private DataWindowBasic dwb;

	public DataWindow() {
		dwb = new DataWindowBasic();
	}

	public String basicGetSequence() throws Exception {
		return dwb.getSequence(DmSequenceName.XH);
	}

	public static String getSequence() throws Exception {
		DataWindow dw = new DataWindow();
		return dw.basicGetSequence(DmSequenceName.XH);
	}

	public String basicGetSequence(String sequenceName) throws Exception {
		return dwb.getSequence(sequenceName);
	}

	public static String getSequence(String sequenceName) throws Exception {
		DataWindow dw = new DataWindow();
		return dw.basicGetSequence(sequenceName);
	}

	public int basicGetTotal(String key, Object parameter) {
		return dwb.getTotal(key, parameter);
	}

	public static int getTotal(String key, Object parameter) {
		DataWindow dw = new DataWindow();
		return dw.basicGetTotal(key, parameter);
	}

	public Object basicQueryOne(String key, Object parameter) {
		return dwb.queryOne(key, parameter);
	}

	public static Object queryOne(String key, Object parameter) {
		DataWindow dw = new DataWindow();
		return dw.basicQueryOne(key, parameter);
	}

	public void basicQuery(String key, Object parameter) {
		dwb.query(key, parameter);
	}

	public static DataWindow query(String key, Object parameter) {
		DataWindow dw = new DataWindow();
		dw.basicQuery(key, parameter);
		return dw;
	}

	public void basicQuery(String key, Object parameter, int pageNumber,
			int pageSize) {
		dwb.query(key, parameter, pageNumber, pageSize);
	}

	public static DataWindow query(String key, Object parameter,
			int pageNumber, int pageSize) {
		DataWindow dw = new DataWindow();
		dw.basicQuery(key, parameter, pageNumber, pageSize);
		return dw;
	}

	public void basicInsert(String key, Object parameter) {
		dwb.insert(key, parameter);
	}

	public static void insert(String key, Object parameter) {
		DataWindow dw = new DataWindow();
		dw.basicInsert(key, parameter);
	}

	public void basicUpdate(String key, Object parameter) {
		dwb.update(key, parameter);
	}

	public static void update(String key, Object parameter) {
		DataWindow dw = new DataWindow();
		dw.basicUpdate(key, parameter);
	}

	public void basicDelete(String key, Object parameter) {
		dwb.delete(key, parameter);
	}

	public static void delete(String key, Object parameter) {
		DataWindow dw = new DataWindow();
		dw.basicDelete(key, parameter);
	}

	public List<Object> getList() {
		return dwb.getList();
	}

	public Object getItemAny(int row, String colName) {
		return dwb.getItemAny(row, colName);
	}

}