package com.dcits.portal.spring.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class LoginOutServlet extends HttpServlet {

	private static final String SERVICE = "/j_spring_security_logout";

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		doPost(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		StringBuffer sb = new StringBuffer();
		sb.append(SERVICE);
		request.getRequestDispatcher(sb.toString()).forward(request, response);
	}

}