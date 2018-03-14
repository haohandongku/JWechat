package com.dcits.app.parser.xml;

import java.util.ArrayList;

public class XmlFormat {

	@SuppressWarnings("rawtypes")
	public ArrayList tagList = new ArrayList();

	@SuppressWarnings("unused")
	public void format(String xmlString) {
		int rightPar = xmlString.indexOf("<");
		int leftPar = xmlString.indexOf(">");
		String tagName = xmlString.substring(leftPar, rightPar);
	}

	public boolean isEnd(String tagName, String xmlString) {
		int lastTag = xmlString.lastIndexOf(tagName) + tagName.length();
		if (xmlString.lastIndexOf(">") == lastTag) {
			return true;
		}
		return false;
	}

}