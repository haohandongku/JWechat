package com.dcits.portal.spring.userdetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jasig.cas.client.validation.Assertion;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;

public class GrantedAuthorityFromAssertionAttributesUserDetailsService extends
		AbstractCasAssertionUserDetailsService {

	@SuppressWarnings("unchecked")
	protected UserDetails loadUserDetails(Assertion assertion) {
		Map<String, Object> attributes = assertion.getPrincipal()
				.getAttributes();
		String userName = assertion.getPrincipal().getName();
		String password = (String) attributes.get("password");
		Object object = (Object) attributes.get("roleList");
		List<String> roleList = null;
		if (object != null) {
			if (object instanceof String) {
				roleList = new ArrayList<String>();
				roleList.add((String) object);
			} else if (object instanceof List) {
				roleList = (List<String>) object;
			}
		}
		return UserDetailServiceUtils.loadUser(userName, password, roleList,
				attributes);
	}

}