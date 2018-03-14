package com.dcits.app.parser.xml;

import java.io.Serializable;

public interface DBEvent extends Serializable {

	public boolean retrieveRow(long row);

	public String sqlPreview(String sql);

	public void sqlExecEnd(String sql);

	public String sqlPreview(String sql, String params);

	public void sqlExecEnd(String sql, String params);

}