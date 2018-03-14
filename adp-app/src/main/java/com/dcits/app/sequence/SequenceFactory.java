package com.dcits.app.sequence;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SequenceFactory {

	private static final Log LOG = LogFactory.getLog(SequenceFactory.class);
	private static final String FUNC_STANDARD = "P_SEQUENCE_STANDARD";
	private static Map<String, SequenceGenerator> mapSequence;
	private static SequenceFactory instance;
	private PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
	private String sequenceFileLocations;

	public static SequenceFactory getInstance() {
		if (instance == null) {
			instance = new SequenceFactory();
		}
		return instance;
	}

	public SequenceFactory() {
		if (mapSequence == null) {
			mapSequence = new HashMap<String, SequenceGenerator>();
		}
	}

	public void init() throws Exception {
		if (sequenceFileLocations == null) {
			return;
		}
		Resource resources[] = patternResolver
				.getResources(sequenceFileLocations);
		if (resources == null) {
			return;
		}
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		XPath xpath = XPathFactory.newInstance().newXPath();
		for (Resource resource : resources) {
			Document document = builder.parse(resource.getInputStream());
			NodeList nodeList = (NodeList) xpath.evaluate(
					"/sequencelist/sequence", document, XPathConstants.NODESET);
			for (int j = 0; j < nodeList.getLength(); j++) {
				String name = "";
				Node node = nodeList.item(j);
				Node nameNode = node.getAttributes().getNamedItem("name");
				if (nameNode != null) {
					name = nameNode.getNodeValue().trim();
				}
				if (name.length() > 0) {
					String function = "";
					Node functionNode = node.getAttributes().getNamedItem(
							"function");
					if (functionNode != null)
						function = functionNode.getNodeValue().trim();
					if (function.length() == 0)
						function = FUNC_STANDARD;
					mapSequence.put(name, createGenerator(function));
				} else {
					LOG.error("sequence的name属性不能为空！");
				}
			}
		}
	}

	public String getSequence(String sequenceName) throws Exception {
		if (!mapSequence.containsKey(sequenceName)) {
			mapSequence.put(sequenceName, createGenerator(FUNC_STANDARD));
			LOG.info((new StringBuilder()).append("序列（号）[")
					.append(sequenceName).append("]没有自定义信息，将通过函数[")
					.append(FUNC_STANDARD).append("]获取。").toString());
		}
		return ((SequenceGenerator) mapSequence.get(sequenceName))
				.getSequence();
	}

	public void setSequenceFileLocations(String sequenceFileLocations) {
		this.sequenceFileLocations = sequenceFileLocations;
	}

	private SequenceGenerator createGenerator(String procedureName)
			throws Exception {
		SequenceGenerator sequenceGenerator = new SequenceGenerator();
		sequenceGenerator.setProcedureName(procedureName);
		return sequenceGenerator;
	}

}