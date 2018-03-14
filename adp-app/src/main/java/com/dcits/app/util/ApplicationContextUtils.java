package com.dcits.app.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

public class ApplicationContextUtils implements ApplicationContextAware {

	private static ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		ApplicationContextUtils.context = context;
	}

	public static ApplicationContext getContext() {
		return context;
	}

	public static String getBeanNameByBeanTarget(Object target) {
		String serviceName = null;
		Service service = target.getClass().getAnnotation(
				org.springframework.stereotype.Service.class);
		if (service != null) {
			serviceName = service.value();
		} else {
			String[] names = context.getBeanNamesForType(target.getClass());
			for (String name : names) {
				if (target.toString().equals(context.getBean(name).toString())) {
					serviceName = name;
					break;
				}
			}
		}
		return serviceName;
	}

}