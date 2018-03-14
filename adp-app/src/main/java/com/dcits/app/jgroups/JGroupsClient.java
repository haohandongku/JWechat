package com.dcits.app.jgroups;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.springframework.util.ReflectionUtils;

import com.dcits.app.constant.Constant;
import com.dcits.app.data.DataObject;
import com.dcits.app.util.ApplicationContextUtils;
import com.dcits.app.util.JacksonUtils;

public class JGroupsClient extends ReceiverAdapter {

	private static final Log LOG = LogFactory.getLog(JGroupsClient.class);
	private static JGroupsClient instance;
	private static Channel channel;
	private String groupName;

	public static JGroupsClient getInstance() {
		if (instance == null) {
			instance = new JGroupsClient();
		}
		return instance;
	}

	public JGroupsClient() {
		if (channel == null) {
			try {
				channel = new JChannel("udp.xml");
			} catch (Throwable e) {
				LOG.error("创建channel出现异常", e);
			}
		}
	}

	public void register() {
		try {
			channel.setReceiver(this);
			channel.connect(groupName);
			System.out.println("JGroups服务调用客户端注册成功，所在组：" + groupName);
		} catch (Throwable e) {
			LOG.error("JGroups服务调用客户端注册时出现异常", e);
		}
	}

	@SuppressWarnings("rawtypes")
	public void receive(Message message) {
		try {
			String json = (String) message.getObject();
			LOG.debug("接收消息：" + json);
			Map map = JacksonUtils.getMapFromJson(json);
			String serviceName = (String) map.get(Constant.SERVICE_NAME);
			String methodName = (String) map.get(Constant.METHOD_NAME);
			Map data = (Map) map.get("data");
			DataObject dataObject = new DataObject(data);
			Object service = ApplicationContextUtils.getContext().getBean(
					serviceName);
			Method method = ReflectionUtils.findMethod(service.getClass(),
					methodName, new Class[] { DataObject.class });
			ReflectionUtils.invokeMethod(method, service, dataObject);
		} catch (Throwable e) {
			LOG.error("JGroups接收消息时出现异常", e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void send(String serviceName, String methodName,
			DataObject dataObject) {
		try {
			Map map = new HashMap();
			map.put(Constant.SERVICE_NAME, serviceName);
			map.put(Constant.METHOD_NAME, methodName);
			map.put("data", dataObject.getMap());
			String json = JacksonUtils.getJsonFromMap(map);
			LOG.debug("发送消息： " + json);
			Message message = new Message(null, null, new String(
					json.getBytes("GB2312"), "ISO-8859-1"));
			channel.send(message);
		} catch (Throwable e) {
			LOG.error("JGroups发送消息时出现异常", e);
		}
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

}