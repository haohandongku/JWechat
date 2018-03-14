package com.dcits.portal.spring.userdetail;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.dcits.app.constant.Constant;
import com.dcits.app.data.DataObject;
import com.dcits.portal.commons.IEDSProxy;

public class UserDetailsService implements
		org.springframework.security.core.userdetails.UserDetailsService {

	@SuppressWarnings("rawtypes")
	@Override
	public UserDetails loadUserByUsername(String userName)
			throws UsernameNotFoundException {
		DataObject dataObject = IEDSProxy.Uc_User01(userName);
		Map map = dataObject.getMap();
		String rtnCode = (String) map.get(Constant.RTN_CODE);
		if (Constant.RTN_CODE_SUCCESS.equals(rtnCode)) {
			Map attachMsg = (Map) map.get("attachMsg");
			Map userInfo = (Map) attachMsg.get("userInfo");
			if (userInfo != null) {
				String password = (String) userInfo.get("password");
				List roleList = (List) attachMsg.get("roleList");
				return UserDetailServiceUtils.loadUser(userName, password, roleList,
						userInfo);
			}
		}
		return null;
	}

}