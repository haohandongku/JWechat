package com.dcits.app.resource;

import java.util.Locale;

public abstract class AbstractMessageResources {

	public Object getMessage(String key) {
		return getMessage(key, Locale.getDefault());
	}

	public Object getMessage(String key, Object param) {
		return getMessage(key, new Object[] { param }, null,
				Locale.getDefault());
	}

	public Object getMessage(String key, Object[] params) {
		return getMessage(key, params, Locale.getDefault());
	}

	public Object getMessage(String key, Object[] params, Locale locale) {
		return getMessage(key, params, null, locale);
	}

	public Object getMessage(String key, Locale locale) {
		return getMessage(key, null, null, locale);
	}

	public Object getMessage(String key, Object param, Locale locale) {
		return getMessage(key, new Object[] { param }, null, locale);
	}

	public abstract Object getMessage(String key, Object[] params,
			String defaultMessage, Locale locale);
}