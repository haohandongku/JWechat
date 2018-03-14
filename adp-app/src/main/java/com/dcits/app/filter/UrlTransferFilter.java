package com.dcits.app.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dcits.app.resource.RegexPropertyMessageResources;
import com.dcits.app.util.ApplicationContextUtils;

public class UrlTransferFilter implements Filter {
	private static final Log LOG = LogFactory.getLog(UrlTransferFilter.class);
	private static final RegexPropertyMessageResources regexPropertyMessageResources = (RegexPropertyMessageResources) ApplicationContextUtils
			.getContext().getBean("propertyMessageResources");
	private PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
	private Map<String, String> urlMap;

	public void init(FilterConfig filterConfig) throws ServletException {
		String confPath = filterConfig.getInitParameter("confPath");
		urlMap = new HashMap<String, String>();
		Resource resources[] = null;
		DocumentBuilder builder = null;
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			resources = patternResolver.getResources(confPath);
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			for (Resource resource : resources) {
				Document document = builder.parse(resource.getInputStream());
				NodeList nodeList = (NodeList) xpath
						.evaluate("/urlrewrite/tansfer", document,
								XPathConstants.NODESET);
				for (int i = 0; i < nodeList.getLength(); i++) {
					String origin = "";
					Node node = nodeList.item(i);
					Node originNode = node.getAttributes().getNamedItem(
							"origin");
					if (originNode != null) {
						origin = originNode.getNodeValue().trim();
					}
					if (StringUtils.isNotBlank(origin)) {
						String target = "";
						Node targetNode = node.getAttributes().getNamedItem(
								"target");
						if (targetNode != null) {
							target = targetNode.getNodeValue().trim();
						}
						urlMap.put(origin, target);
					} else {
						LOG.error("transfers的origin属性不能为空！");
					}
				}
			}
		} catch (Throwable e) {
			LOG.error("加载url转换配置文件时出现异常", e);
		}
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		String isRemoveContext = regexPropertyMessageResources
				.getMessage("isRemoveContext").toString();
		HttpServletResponse httpResponse = (HttpServletResponse) resp;
		HttpServletRequest httpRequest = (HttpServletRequest) req;
		String contextPath = httpRequest.getContextPath();
		String requestUrl = httpRequest.getRequestURL().toString();
		String queryString = (httpRequest.getQueryString() == null ? "" : "?"
				+ httpRequest.getQueryString());
		boolean isMatch = false;
		for (Map.Entry<String, String> entry : urlMap.entrySet()) {
			String originUrl = entry.getKey();
			String targetUrl = entry.getValue();
			if (requestUrl.indexOf(originUrl) > 0) {
				isMatch = true;
				httpResponse
						.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
				requestUrl = requestUrl.replace(originUrl, targetUrl);
				if ("true".equals(isRemoveContext)) {
					requestUrl = requestUrl.replace(contextPath, "");
				}
			}
		}
		if (isMatch) {
			httpResponse.setHeader("Location", requestUrl + queryString);
		} else {
			chain.doFilter(req, resp);
		}
	}
}
