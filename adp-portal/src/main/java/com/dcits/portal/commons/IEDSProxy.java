package com.dcits.portal.commons;

import java.util.HashMap;
import java.util.Map;

import com.dcits.app.data.DataObject;

/**
 * ieds代理
 * 
 * @author zhongym
 * 
 */
public class IEDSProxy {

	/**
	 * 用户名和密码校验服务
	 * 
	 * @param userName
	 *            用户名
	 * @param password
	 *            密码
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static DataObject Uc_Validate01(String userName, String password) {
		Map parameter = new HashMap();
		parameter.put("userName", userName);
		parameter.put("password", password);
		return com.dcits.ieds.proxy.IEDSProxy.doService("Uc_Validate01",
				new DataObject(parameter), null);
	}
	
	/**
	 * 用户名/手机号码登录(同时获取用户信息)
	 * 
	 * @param phoneNumber
	 * 
	 * @return 返回USERBM和isExist标志  》  存在AttachMsg里
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static DataObject Uc_Validate02(String userName, String password) {
		Map parameter = new HashMap();
		parameter.put("userName", userName);
		parameter.put("password", password);
		return com.dcits.ieds.proxy.IEDSProxy.doService("Uc_Validate05",
				new DataObject(parameter), null);
	}
	
	/**
	 * 获取用户信息服务
	 * 
	 * @param userName
	 *            用户名
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static DataObject Uc_User01(String userName) {
		Map parameter = new HashMap();
		parameter.put("userName", userName);
		return com.dcits.ieds.proxy.IEDSProxy.doService("Uc_User01",
				new DataObject(parameter), null);
	}

}