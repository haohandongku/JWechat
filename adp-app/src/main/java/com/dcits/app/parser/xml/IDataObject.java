package com.dcits.app.parser.xml;

import com.dcits.app.exception.BizRuntimeException;

public interface IDataObject extends MetaDataObject, DBEvent {

	public void registerValidCtrl(String name);

	public void setTransObject(Object object);

	public long retrieve() throws BizRuntimeException;

	public void retrieveStart() throws BizRuntimeException;

	public void retrieveEnd() throws BizRuntimeException;

	public long getRowCount();

	public void reset();

	public void resetUpdate();

	public void setItem(Object[] objects) throws Exception;

	public void setItemAny(long row, int col, Object object);

	public Object getItemAny(long row, int col);

	public void setItemAny(long row, String colName, Object object);

	public Object getItemAny(long row, String colName);

	public void deleteRow(long row);

	public Object getRow(long row);

	public long insert(long row) throws Exception;

	public long insertRow(long row, Object object);

	public String getColName(int col);

	public int getColOrder(String colName);

	public void setFilter(String cond);

	public StringBuffer toXML();

	public void load(String xml);

	public void setTableName(String tableName);

	public void setObjectName(String objectName);

}