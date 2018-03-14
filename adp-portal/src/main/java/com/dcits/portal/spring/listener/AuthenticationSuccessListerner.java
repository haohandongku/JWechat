package com.dcits.portal.spring.listener;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;

public interface AuthenticationSuccessListerner {

	public void beforeAuthenticationSuccess(HttpServletRequest request,
			Authentication authentication);

}