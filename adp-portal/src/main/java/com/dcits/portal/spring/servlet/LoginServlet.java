package com.dcits.portal.spring.servlet;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.dcits.app.resource.RegexPropertyMessageResources;
import com.dcits.app.util.ApplicationContextUtils;
import com.dcits.portal.spring.contant.Constant;

@SuppressWarnings("serial")
public class LoginServlet extends HttpServlet {

	private static final RegexPropertyMessageResources regexPropertyMessageResources = (RegexPropertyMessageResources) ApplicationContextUtils
			.getContext().getBean("propertyMessageResources");
	private static final String USERNAME_PARAMETER = "j_username";
	private static final String PASSWORD_PARAMETER = "j_password";
//	private static final String AUTHCODE_PARAMETER = "j_authcode";
	private static final String LOCAL_LOGIN_SUFFIX = "/j_spring_security_login";
	private static final String CHAR_SET = "utf-8";

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		doPost(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String username = obtainUsername(request);
		if (StringUtils.isNotBlank(username)) {
			username = username.trim();
			request.setAttribute(USERNAME_PARAMETER, username);
		}

		String password = obtainPassword(request);
		if (StringUtils.isNotBlank(password)) {
			password = password.trim();
			request.setAttribute(PASSWORD_PARAMETER, password);
		}

		String loginPage = (String) regexPropertyMessageResources.getMessage("adp.login.page");
		if (StringUtils.isBlank(username)) {
			setMessage(request, Constant.EMPTYUSERNAME);
			request.getRequestDispatcher(loginPage).forward(request, response);
			return;
		}

		if (StringUtils.isBlank(password)) {
			setMessage(request, Constant.EMPTYPASSWORD);
			request.getRequestDispatcher(loginPage).forward(request, response);
			return;
		}

		/*
		String authCode = obtainAuthCode(request);
		if (StringUtils.isBlank(authCode)) {
			setMessage(request, Constant.EMPTYVALIDCODE);
			request.getRequestDispatcher(loginPage).forward(request, response);
			return;
		}

		authCode = authCode.trim();
		String sessionAuthCode = (String) request.getSession().getAttribute(
				Constant.LOGIN_SESSION_VALIDCODE_KEY);
		if (!authCode.equalsIgnoreCase(sessionAuthCode)) {
			setMessage(request, Constant.BADVALIDCODE);
			request.getRequestDispatcher(loginPage).forward(request, response);
			return;
		}
		*/

		StringBuffer sb = new StringBuffer();
		sb.append(LOCAL_LOGIN_SUFFIX);
		sb.append("?j_spring_security_username=");
		String encodeUsername = URLEncoder.encode(username, CHAR_SET);
		sb.append(encodeUsername);
		sb.append("&j_spring_security_password=");
		String encodePassword = URLEncoder.encode(password, CHAR_SET);
		sb.append(encodePassword);
		request.getRequestDispatcher(sb.toString()).forward(request, response);
	}

	private String obtainUsername(HttpServletRequest request) {
		return request.getParameter(USERNAME_PARAMETER);
	}

	private String obtainPassword(HttpServletRequest request) {
		return request.getParameter(PASSWORD_PARAMETER);
	}

	/*
	private String obtainAuthCode(HttpServletRequest request) {
		return request.getParameter(AUTHCODE_PARAMETER);
	}
	*/

	private void setMessage(HttpServletRequest request, String message) {
		request.setAttribute("message", message);
	}

}