package com.dcits.app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.dcits.app.dao.DataWindow;
import com.dcits.app.data.DataObject;
import com.dcits.app.security.UserDetails;
import com.dcits.app.util.ApplicationContextUtils;
import com.dcits.app.util.IPUtils;
import com.dcits.app.util.JacksonUtils;
import com.dcits.app.constant.Constant;

public class CommonService {

	protected IProxy proxy = (IProxy) ApplicationContextUtils.getContext()
			.getBean("iedsProxy");

	@SuppressWarnings("rawtypes")
	public DataObject getQyjgRoot(DataObject dataObject) {
		DataObject qyjg = proxy.callService("Security_QYGL_01", dataObject,
				null);
		Map rootNode = (Map) qyjg.getMap().get(Constant.ATTACH_MSG);
		return new DataObject(rootNode);
	}

	public DataObject getQyjgTree(DataObject dataObject) {
		return proxy.callService("Security_QYGL_02", dataObject, null);
	}

	public DataObject getQyjgTreeSync(DataObject dataObject) {
		return proxy.callService("Security_QYGL_03", dataObject, null);
	}

	public DataObject getQyryTreeSync(DataObject dataObject) {
		return proxy.callService("Security_QYGL_04", dataObject, null);
	}

	@SuppressWarnings("rawtypes")
	public DataObject getSxjgRoot(DataObject dataObject) {
		DataObject qyjg = proxy.callService("Security_SXGL_01", dataObject,
				null);
		Map rootNode = (Map) qyjg.getMap().get(Constant.ATTACH_MSG);
		return new DataObject(rootNode);
	}

	public DataObject getSxjgTree(DataObject dataObject) {
		return proxy.callService("Security_SXGL_02", dataObject, null);
	}

	public DataObject getSxjgTreeSync(DataObject dataObject) {
		return proxy.callService("Security_SXGL_03", dataObject, null);
	}

	public DataObject getXzqh(DataObject dataObject) {
		return this.initCombobox(dataObject, "app.CommonService_getXzqh");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected DataObject initCombobox(DataObject dataObject, String sqlKey) {
		Map parameter = dataObject.getMap();
		Map map = new HashMap();
		DataWindow dataWindow = DataWindow.query(sqlKey, parameter);
		map.put("rows", dataWindow.getList());
		return new DataObject(map);
	}

	protected DataObject initComboTree(DataObject dataObject, String sqlKey) {
		return initTree(dataObject, sqlKey);
	}

	protected DataObject initComboTreeSync(DataObject dataObject, String sqlKey) {
		return initTreeSync(dataObject, sqlKey);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected DataObject initTree(DataObject dataObject, String sqlKey) {
		Map parameter = dataObject.getMap();
		String idField = (String) parameter.get("ID_FIELD");
		String textField = (String) parameter.get("TEXT_FIELD");
		DataWindow dataWindow = DataWindow.query(sqlKey, parameter);
		List list = new ArrayList();
		if (CollectionUtils.isNotEmpty(dataWindow.getList())) {
			for (Object object : dataWindow.getList()) {
				Map map = (Map) object;
				parameter.put(idField, map.get(idField));
				dataWindow = DataWindow.query(sqlKey, parameter);
				Map temp = new HashMap();
				if (CollectionUtils.isNotEmpty(dataWindow.getList())) {
					temp.put("state", "closed");
				}
				temp.put("id", map.get(idField));
				temp.put("text", map.get(textField));
				try {
					temp.put("attributes", JacksonUtils.getJsonFromMap(map));
				} catch (Throwable e) {
					temp.put("attributes", null);
				}
				list.add(temp);
			}
		}
		Map map = new HashMap();
		map.put("treedata", list);
		return new DataObject(map);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected DataObject initTreeSync(DataObject dataObject, String sqlKey) {
		Map parameter = dataObject.getMap();
		parameter.put("INITTREE_KEY", sqlKey);
		String idField = (String) parameter.get("ID_FIELD");
		String rootId = (String) parameter.get("ROOT_ID");
		String rootText = (String) parameter.get("ROOT_TEXT");
		parameter.put(idField, rootId);
		Map rootNode = new HashMap();
		rootNode.put("id", rootId);
		rootNode.put("text", rootText);
		rootNode.put("attributes", null);
		rootNode.put("children", getChildrenNode(parameter));
		rootNode.put("state", "closed");
		List list = new ArrayList();
		list.add(rootNode);
		Map map = new HashMap();
		map.put("treedata", list);
		return new DataObject(map);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Object> getChildrenNode(Map parameter) {
		Map param = parameter;
		String idField = (String) parameter.get("ID_FIELD");
		String textField = (String) parameter.get("TEXT_FIELD");
		String sqlKey = (String) parameter.get("INITTREE_KEY");
		String idFieldValue = (String) parameter.get(idField);
		param.put(idField, idFieldValue);
		DataWindow dataWindow = DataWindow.query(sqlKey, param);
		List list = new ArrayList();
		if (CollectionUtils.isNotEmpty(dataWindow.getList())) {
			for (Object obj : dataWindow.getList()) {
				Map node = (Map) obj;
				String idFieldValueTemp = "";
				if (node.get(idField) instanceof Integer) {
					idFieldValueTemp = node.get(idField).toString();
				} else {
					idFieldValueTemp = String.valueOf(node.get(idField));
				}
				param.put(idField, idFieldValueTemp);
				dataWindow = DataWindow.query(sqlKey, param);
				Map temp = new HashMap();
				if (CollectionUtils.isNotEmpty(dataWindow.getList())) {
					temp.put("state", "closed");
					List<Object> children = getChildrenNode(param);
					if (CollectionUtils.isNotEmpty(children)) {
						temp.put("children", children);
					}
				}
				temp.put("id", node.get(idField));
				temp.put("text", node.get(textField));
				try {
					temp.put("attributes", JacksonUtils.getJsonFromMap(node));
				} catch (Throwable e) {
					temp.put("attributes", null);
				}
				list.add(temp);
			}
		}
		return list;
	}

	public DataObject getUserByQybm(DataObject dataObject) {
		return proxy.callService("Security_14", dataObject, null);
	}

	public DataObject getUser(DataObject dataObject) {
		UserDetails userDetails = this.getUser();
		if (userDetails == null) {
			return new DataObject();
		}
		return new DataObject(userDetails.getOther());
	}

	public UserDetails getUser() {
		Authentication authentication = SecurityContextHolder.getContext()
				.getAuthentication();
		if (authentication != null) {
			Object object = authentication.getPrincipal();
			if (object instanceof UserDetails) {
				return (UserDetails) object;
			}
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DataObject getCityIdByIp(DataObject dataObject) {
		Map parameter = dataObject.getMap();
		String ip = String.valueOf(parameter.get("ip"));
		String cityId = IPUtils.getCityIdByIp(ip);
		if ("110100".equals(cityId) || "110200".equals(cityId)) {
			cityId = "110000";
		}
		if ("120100".equals(cityId) || "120200".equals(cityId)) {
			cityId = "120000";
		}
		if ("310100".equals(cityId) || "310200".equals(cityId)) {
			cityId = "310000";
		}
		if ("500100".equals(cityId) || "500200".equals(cityId)) {
			cityId = "500000";
		}
		Map result = new HashMap();
		result.put("cityId", cityId);
		return new DataObject(result);
	}

}