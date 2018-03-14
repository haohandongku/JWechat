package com.dcits.app.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.dcits.app.exception.BizRuntimeException;
import com.dcits.app.parser.xml.Element;
import com.dcits.app.parser.xml.XMLDataObject;
import com.dcits.app.parser.xml.XMLParser;
import com.dcits.app.util.JacksonUtils;

public class DataObject implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final String ROOT = "ROOT";
	private static final String ITEM = "ITEM";
	private Object[] cache = new Object[3];
	private Object data;
    public  HttpServletRequest request;
    
    
	public HttpServletRequest getRequest() {
		
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	@SuppressWarnings("rawtypes")
	public DataObject() {
		this.data = new HashMap();
	}

	@SuppressWarnings("rawtypes")
	public DataObject(Object data) {
		if (data == null) {
			data = new HashMap();
		}
		this.data = data;
	}

	@SuppressWarnings("rawtypes")
	public Map getMap() {
		if (cache[0] != null) {
			return (Map) cache[0];
		}
		if (data instanceof String) {
			XMLParser parse = new XMLParser();
			try {
				Element tree = parse.parseXMLStr((String) data);
				XMLDataObject xdo = new XMLDataObject(tree, tree.getName());
				xdo.rootScrollTo(tree.getName());
				cache[0] = new HashMap();
				fillMapByXdo((HashMap) cache[0], xdo);
				xdo = null;
			} catch (Exception e) {
				throw new BizRuntimeException(e);
			}
		} else if (data instanceof Map) {
			cache[0] = (Map) data;
		} else if (data instanceof XMLDataObject) {
			try {
				Element tree = ((XMLDataObject) data).getCutRoot();
				XMLDataObject xdo = new XMLDataObject(tree, tree.getName());
				xdo.rootScrollTo(tree.getName());
				cache[0] = new HashMap();
				fillMapByXdo((HashMap) cache[0], xdo);
				xdo = null;
			} catch (Exception e) {
				throw new BizRuntimeException(e);
			}
		} else {
			throw new BizRuntimeException("不支持的对象类型："
					+ data.getClass().getClass() + "！");
		}
		return (Map) cache[0];
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void fillMapByXdo(Map map, XMLDataObject xdo) throws Exception {
		if (xdo.getChildCount() == 0) {
			Object value = xdo.getCutRoot().getValue();
			Object object = value;
			if (value instanceof String) {
				object = xml2normal((String) value);
			}
			if (object == null) {
				map.put(xdo.getCutRoot().getName(), "");
			} else {
				map.put(xdo.getCutRoot().getName(), object);
			}
		} else {
			for (int i = 0; i < xdo.getChildCount(); i++) {
				Element child = xdo.getChild(i);
				XMLDataObject tmp = new XMLDataObject(child, child.getName());
				tmp.rootScrollTo(child.getName());
				Object object = getValueFromXdo(tmp);
				if (object == null) {
					map.put(child.getName(), "");
				} else {
					map.put(child.getName(), object);
				}
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object getValueFromXdo(XMLDataObject xdo) throws Exception {
		if (xdo.getChildCount() == 0) {
			Object value = xdo.getCutRoot().getValue();
			if (value instanceof String) {
				return xml2normal((String) value);
			}
			return value;
		} else {
			if (ITEM.equalsIgnoreCase(xdo.getChild(0).getName())) {
				List list = new ArrayList();
				for (int i = 0; i < xdo.getChildCount(); i++) {
					Map map = new HashMap();
					Element child = xdo.getChild(i);
					XMLDataObject tmp = new XMLDataObject(child,
							child.getName());
					tmp.rootScrollTo(child.getName());
					fillMapByXdo(map, tmp);
					list.add(map);
				}
				return list;
			} else {
				Map map = new HashMap();
				fillMapByXdo(map, xdo);
				return map;
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public String getXml() {
		if (cache[1] != null) {
			return (String) cache[1];
		}
		if (data instanceof String) {
			cache[1] = (String) data;
		} else if (data instanceof Map) {
			cache[1] = "<" + ROOT + ">" + getXmlFromMap((Map) data) + "</"
					+ ROOT + ">";
		} else if (data instanceof XMLDataObject) {
			cache[1] = ((XMLDataObject) data).asXML().toString();
		} else {
			throw new BizRuntimeException("不支持的对象类型："
					+ data.getClass().getClass() + "！");
		}
		return (String) cache[1];
	}

	@SuppressWarnings("rawtypes")
	public XMLDataObject getXMLDataObject() {
		if (cache[2] != null) {
			return (XMLDataObject) cache[2];
		}
		if (data instanceof String) {
			XMLParser parse = new XMLParser();
			try {
				Element tree = parse.parseXMLStr((String) data);
				XMLDataObject xdo = new XMLDataObject(tree, tree.getName());
				xdo.rootScrollTo(tree.getName());
				cache[2] = xdo;
			} catch (Exception e) {
				throw new BizRuntimeException(e);
			}
		} else if (data instanceof Map) {
			String xml = "<" + ROOT + ">" + getXmlFromMap((Map) data) + "</"
					+ ROOT + ">";
			XMLParser parse = new XMLParser();
			try {
				Element tree = parse.parseXMLStr((String) xml);
				XMLDataObject xdo = new XMLDataObject(tree, tree.getName());
				xdo.rootScrollTo(tree.getName());
				cache[2] = xdo;
			} catch (Exception e) {
				throw new BizRuntimeException(e);
			}
		} else if (data instanceof XMLDataObject) {
			cache[2] = (XMLDataObject) data;
		} else {
			throw new BizRuntimeException("不支持的对象类型："
					+ data.getClass().getClass() + "！");
		}
		return (XMLDataObject) cache[2];
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String getXmlFromMap(Map fpdata) {
		StringBuffer builder = new StringBuffer();
		for (Map.Entry entry : (Set<Map.Entry>) fpdata.entrySet()) {
			Object value = entry.getValue();
			if (value == null) {
				String key = entry.getKey().toString();
				builder.append("<").append(key).append("/>");
				continue;
			} else if (value instanceof Map) {
				Map map = (Map) value;
				String key = entry.getKey().toString();
				builder.append("<").append(key).append(">")
						.append(getXmlFromMap(map)).append("</").append(key)
						.append(">");
			} else if (value instanceof List) {
				String key = entry.getKey().toString();
				builder.append("<").append(key).append(">");
				List list = (List) value;
				for (Object row : list) {
					if (row instanceof Map) {
						builder.append("<" + ITEM + ">");
						builder.append(getXmlFromMap((Map) row));
						builder.append("</" + ITEM + ">");
					} else if (row instanceof String) {
						builder.append("<" + ITEM + ">");
						builder.append(normal2xml((String) row));
						builder.append("</" + ITEM + ">");
					}
				}
				builder.append("</").append(key).append(">");
			} else {
				String key = String.valueOf(entry.getKey());
				builder.append("<").append(key).append(">")
						.append(normal2xml(value.toString())).append("</")
						.append(key).append(">");
			}
		}
		return builder.toString();
	}

	public String toString() {
		return getXml();
	}

	public String getJson() throws JsonGenerationException,
			JsonMappingException, IOException {
		return JacksonUtils.getJsonFromMap(this.getMap());
	}

	public String getItemValue(String nodeName) {
		return getXMLDataObject().getItemValue(nodeName);
	}

	public long scrollTo(String nodeName) {
		XMLDataObject xdo = getXMLDataObject();
		xdo.rootScrollTo(nodeName);
		long n = xdo.retrieve();
		cache[2] = xdo;
		return n;
	}

	public Object getItemAny(long arow, String colName) {
		return getXMLDataObject().getItemAny(arow, colName);
	}

	@SuppressWarnings("rawtypes")
	public Object getObjectByKey(String key) {
		Map map = getMap();
		Object object = getObjectByKeyFromMap(map, key);
		return object;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object getObjectByKeyFromMap(Map map, String key) {
		Object object = null;
		for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
			if (entry.getKey().toString().equals(key)) {
				object = entry.getValue();
				break;
			} else {
				if (entry.getValue() instanceof Map) {
					object = getObjectByKeyFromMap((Map) entry.getValue(), key);
				} else if (entry.getValue() instanceof List) {
					continue;
				} else {
					continue;
				}
			}
		}
		return object;
	}

	private static String xml2normal(String xml) {
		return xml.replace("&amp;", "&").replace("&lt;", "<")
				.replace("&gt;", ">").replace("&apos;", "'")
				.replace("&quot;", "\"");
	}

	private static String normal2xml(String str) {
		StringBuffer sb = new StringBuffer();
		for (char chr : str.toCharArray()) {
			if ("<".equals(String.valueOf(chr)))
				sb.append("&lt;");
			else if (">".equals(String.valueOf(chr)))
				sb.append("&gt;");
			else if ("&".equals(String.valueOf(chr)))
				sb.append("&amp;");
			else if ("'".equals(String.valueOf(chr)))
				sb.append("&apos;");
			else if ("\"".equals(String.valueOf(chr)))
				sb.append("&quot;");
			else {
				sb.append(chr);
			}
		}
		return sb.toString();
	}

}