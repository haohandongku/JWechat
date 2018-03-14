package com.dcits.fpcy.commons.bean;

import java.util.HashMap;

/**
 * 页面解析实体类
 * 
 * @author wuche
 * 
 */
public class ResultBean extends HashMap<String, String> {
	private static final long serialVersionUID = 1L;

	public ResultBean(String name1, String name2, String value) {
		put("name1", name1);
		put("name2", name2);
		put("value", value);
	}

	public ResultBean(String name2, String value) {
		put("name2", name2);
		put("value", value);
	}

	@Override
	public String toString() {
		return "{\"name1\":\"" + getName1() + "\",\"name2\":\"" + getName2() + "\",\"value\":\"" + getValue() + "\"}";
	}

	public String getName1() {
		return get("name1");
	}

	public String getName2() {
		return get("name2");
	}

	public String getValue() {
		return get("value");
	}
}
