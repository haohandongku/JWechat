package com.dcits.portal.spring.userdetail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.dcits.app.security.User;

public class UserDetailServiceUtils {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static UserDetails loadUser(String userName, String password,
			List roles, Map userInfo) {
		User user = new User();
		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		List<String> roleList = new ArrayList<String>();
		if (CollectionUtils.isNotEmpty(roles)) {
			for (Object object : roles) {
				String roleId = null;
				if (object instanceof Map) {
					Map role = (Map) object;
					roleId = (String) role.get("roleId");
				} else if (object instanceof String) {
					roleId = (String) object;
				}
				roleList.add(roleId);
				SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
						"ROLE_" + roleId);
				authorities.add(authority);
			}
		}
		user.setAuthorities(authorities);

		user.setPassword(password);
		user.setUsername(userName);
		user.setAccountNonExpired(isAccountNonExpired(userInfo));
		user.setAccountNonLocked(isAccountNonLocked(userInfo));
		user.setCredentialsNonExpired(isCredentialsNonExpired(userInfo));
		user.setEnabled(true);
		user.setOther(userInfo);
		userInfo.put("roleList", roleList);
		return user;
	}

	@SuppressWarnings("rawtypes")
	private static boolean isAccountNonExpired(Map userInfo) {
		return true;
	}

	@SuppressWarnings("rawtypes")
	private static boolean isAccountNonLocked(Map userInfo) {
		return true;
	}

	@SuppressWarnings("rawtypes")
	private static boolean isCredentialsNonExpired(Map userInfo) {
		return true;
	}

}