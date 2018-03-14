package com.dcits.portal.spring.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.dcits.portal.spring.contant.Constant;

public class UsernamePasswordAuthenticationFilter
		extends
		org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter {

	private static final Log LOG = LogFactory
			.getLog(UsernamePasswordAuthenticationFilter.class);

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException {
		try {
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
					obtainUsername(request), obtainPassword(request));
			this.setDetails(request, authenticationToken);
			return this.getAuthenticationManager().authenticate(
					authenticationToken);
		} catch (Throwable t) {
			LOG.error("用户登录时出现异常：", t);
			if (t instanceof AuthenticationException) {
				throw (AuthenticationException) t;
			} else {
				throw new AuthenticationServiceException(Constant.ERROR);
			}
		}
	}

}