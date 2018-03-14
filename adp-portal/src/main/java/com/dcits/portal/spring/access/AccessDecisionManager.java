package com.dcits.portal.spring.access;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.dcits.portal.spring.contant.Constant;

public class AccessDecisionManager implements
		org.springframework.security.access.AccessDecisionManager {

	@Override
	public void decide(Authentication authentication, Object object,
			Collection<ConfigAttribute> configAttributes)
			throws AccessDeniedException, InsufficientAuthenticationException {
		if (CollectionUtils.isEmpty(configAttributes)) {
			return;
		}
		if (CollectionUtils.isNotEmpty(authentication.getAuthorities())) {
			for (ConfigAttribute configAttribute : configAttributes) {
				String attribute = ((SecurityConfig) configAttribute)
						.getAttribute();
				for (GrantedAuthority grantedAuthority : authentication
						.getAuthorities()) {
					if (attribute.equals(grantedAuthority.getAuthority())) {
						return;
					}
				}
			}
		}
		throw new AccessDeniedException(Constant.ACCESSDENIED);
	}

	@Override
	public boolean supports(ConfigAttribute attribute) {
		return true;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}

}