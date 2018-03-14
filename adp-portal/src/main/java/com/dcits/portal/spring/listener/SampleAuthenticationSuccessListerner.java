package com.dcits.portal.spring.listener;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.core.Authentication;

import com.dcits.app.asynctask.AsyncTaskService;
import com.dcits.app.util.ApplicationContextUtils;

public class SampleAuthenticationSuccessListerner implements
		AuthenticationSuccessListerner {

	private List<String> listenerNames;

	@Override
	public void beforeAuthenticationSuccess(final HttpServletRequest request,
			final Authentication authentication) {
		if (CollectionUtils.isNotEmpty(listenerNames)) {
			for (String listenerName : listenerNames) {
				final AuthenticationSuccessListerner listener = (AuthenticationSuccessListerner) ApplicationContextUtils
						.getContext().getBean(listenerName);
				if (listener != null) {
					AsyncTaskService.getInstance().execute(new Runnable() {

						@Override
						public void run() {
							listener.beforeAuthenticationSuccess(request,
									authentication);
						}
					});
				}
			}
		}
	}

	public void setListenerNames(List<String> listenerNames) {
		this.listenerNames = listenerNames;
	}

}