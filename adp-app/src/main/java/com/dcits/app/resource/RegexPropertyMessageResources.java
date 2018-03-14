package com.dcits.app.resource;

import java.util.Locale;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class RegexPropertyMessageResources extends AbstractMessageResources {

	private static final Log LOG = LogFactory
			.getLog(RegexPropertyMessageResources.class);
	private PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
	private Properties props = new Properties();

	public RegexPropertyMessageResources() {
	}

	public RegexPropertyMessageResources(String baseName) {
		setBaseNames(new String[] { baseName });
	}

	public RegexPropertyMessageResources(String[] baseNames) {
		setBaseNames(baseNames);
	}

	public void setBaseName(String baseName) {
		setBaseNames(new String[] { baseName });
	}

	public void setBaseNames(String[] baseNames) {
		try {
			for (String baseName : baseNames) {
				Resource[] resources = patternResolver.getResources(baseName);
				for (Resource resource : resources) {
					Properties tmp = new Properties();
					tmp.load(resource.getInputStream());
					props.putAll(tmp);
				}
			}
		} catch (Exception e) {
			LOG.error("资源文件加载时出现异常：", e);
		}
	}

	@Override
	public String getMessage(String key, Object[] params,
			String defaultMessage, Locale locale) {
		return props.get(key).toString();
	}
}