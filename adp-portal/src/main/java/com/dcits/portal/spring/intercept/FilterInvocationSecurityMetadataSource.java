package com.dcits.portal.spring.intercept;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;

public class FilterInvocationSecurityMetadataSource
		implements
		org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource {

	private static Map<String, Collection<ConfigAttribute>> resourceMap = new HashMap<String, Collection<ConfigAttribute>>();
	private Map<String, String> resources;

	public FilterInvocationSecurityMetadataSource(Map<String, String> resources) {
		this.resources = resources;
		loadResourceDefine();
	}

	private void loadResourceDefine() {
		if (resources != null) {
			for (Map.Entry<String, String> entry : resources.entrySet()) {
				ConfigAttribute attribute = new SecurityConfig("ROLE_"
						+ entry.getKey());
				String url = entry.getValue();
				if (resourceMap.containsKey(url)) {
					Collection<ConfigAttribute> value = resourceMap.get(url);
					value.add(attribute);
				} else {
					Collection<ConfigAttribute> value = new ArrayList<ConfigAttribute>();
					value.add(attribute);
					resourceMap.put(url, value);
				}
			}
		}
	}

	@Override
	public Collection<ConfigAttribute> getAllConfigAttributes() {
		return null;
	}

	@Override
	public Collection<ConfigAttribute> getAttributes(Object object)
			throws IllegalArgumentException {
		String requestUrl = ((FilterInvocation) object).getRequestUrl();
		Iterator<String> iterator = resourceMap.keySet().iterator();
		while (iterator.hasNext()) {
			String url = iterator.next();
			String matchUrl = url;
			if (!matchUrl.startsWith("/")) {
				matchUrl = "/" + matchUrl;
			}
			if (requestUrl.startsWith(matchUrl)) {
				return resourceMap.get(url);
			}
		}
		return null;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}

}