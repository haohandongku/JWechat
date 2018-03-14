package com.dcits.app.security;

import java.util.Map;

public interface UserDetails extends
		org.springframework.security.core.userdetails.UserDetails {

	@SuppressWarnings("rawtypes")
	public Map getOther();

}