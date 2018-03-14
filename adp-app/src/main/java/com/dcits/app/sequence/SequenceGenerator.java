package com.dcits.app.sequence;

import java.util.HashMap;
import java.util.Map;

import com.dcits.app.dao.DataWindow;

public class SequenceGenerator {

	private int top;
	private String seed;
	private String procedureName;

	public SequenceGenerator() {
		top = 0;
	}

	public void setProcedureName(String procedureName) {
		this.procedureName = procedureName;
	}

	public synchronized String getSequence() throws Exception {
		if (top < 1 || top >= 1000) {
			seed = getSequence(procedureName);
			int len = seed.length();
			seed = seed.substring(0, len - 3);
			top = 1;
		}
		String fix = (new String((new StringBuilder()).append("")
				.append(1000 + top).toString())).substring(1);
		top++;
		return (new StringBuilder()).append(seed).append(fix).toString();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected String getSequence(String procedureName) throws Exception {
		if ((procedureName == null) || (procedureName.trim().length() == 0))
			throw new IllegalArgumentException("存储过程名称不能为空!");
		Map parameter = new HashMap();
		parameter.put("procedureName", procedureName);
		DataWindow.queryOne("app.SequenceGenerator_getSequence", parameter);
		return (String) parameter.get("sequenceNumber");
	}

}