package com.dcits.portal.spring.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;

import com.dcits.portal.spring.listener.AuthenticationSuccessListerner;

public class SavedRequestAwareAuthenticationSuccessHandler
		extends
		org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler {

	private AuthenticationSuccessListerner listerner;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws ServletException, IOException {
		if (listerner != null) {
			listerner.beforeAuthenticationSuccess(request, authentication);
		}
		super.onAuthenticationSuccess(request, response, authentication);
	}

	public void setListerner(AuthenticationSuccessListerner listerner) {
		this.listerner = listerner;
	}

}