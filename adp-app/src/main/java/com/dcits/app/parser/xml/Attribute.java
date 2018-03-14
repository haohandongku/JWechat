package com.dcits.app.parser.xml;

import java.io.Serializable;
import java.util.HashMap;

@SuppressWarnings("serial")
public class Attribute implements Serializable {

	private String[] names;
	@SuppressWarnings("rawtypes")
	private HashMap map;

	@SuppressWarnings("rawtypes")
	public Attribute(String[] names, HashMap map) {
		this.names = names;
		this.map = map;
	}

	public String[] getNames() {
		return names;
	}

	@SuppressWarnings("rawtypes")
	public HashMap getMap() {
		return map;
	}

	public String getAttributeValue(int idx) {
		String name = getAttributeName(idx);
		if (name == null) {
			return null;
		}
		return this.getAttributeValue(getAttributeName(idx));
	}

	public String getAttributeValue(String name) {
		if (map == null) {
			return null;
		}
		if (map.containsKey(name)) {
			return (String) map.get(name);
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public void setAttributeValue(String name, String value) throws Exception {
		if (map == null) {
			throw new Exception("无法设置名为:" + name + "的属性值");
		}
		if (map.containsKey(name)) {
			map.put(name, value);
		} else {
			throw new Exception("无法设置名为:" + name + "的属性值");
		}
	}

	public int getAttributeCount() {
		if (names == null) {
			return 0;
		}
		return names.length;
	}

	public String getAttributeName(int idx) {
		if (idx < 0 || idx > names.length) {
			return null;
		}
		return names[idx];
	}

}