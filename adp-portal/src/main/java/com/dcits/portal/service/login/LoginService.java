package com.dcits.portal.service.login;

import java.util.HashMap;
import java.util.Map;

import com.dcits.app.data.DataObject;
import com.dcits.app.service.BaseService;
import com.dcits.portal.commons.IEDSProxy;

/**
 * 登录服务
 * 
 * @author zhongym
 * 
 */
public class LoginService extends BaseService {

	/**
	 * 从session中获取验证码
	 * 
	 * @param dataObject
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DataObject getAuthCode(DataObject dataObject) {
		Map map = new HashMap();
		try {
			String authCode = this.getValidCode(dataObject.getMap());
			map.put("authCode", authCode);
		} catch (Exception e) {
			LOG.error("从session中获取验证码时出现异常，异常信息：", e);
		}
		return new DataObject(map);
	}

	/**
	 * 校验密码
	 * 
	 * @param dataObject
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public DataObject validatePassword(DataObject dataObject) {
		Map parameter = dataObject.getMap();
		String userName = (String) parameter.get("userName");
		String password = (String) parameter.get("password");
		return IEDSProxy.Uc_Validate01(userName, password);
	}
	
	/**
	 * 校验密码(同时获取用户信息)
	 * 2_0_0表示方法版本号 ：2.0.0
	 * @param dataObject
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public DataObject validatePassword_2_0_0(DataObject dataObject) {
		Map parameter = dataObject.getMap();
		String userName = (String) parameter.get("userName");
		String password = (String) parameter.get("password");
		return IEDSProxy.Uc_Validate02(userName, password);
	}

}