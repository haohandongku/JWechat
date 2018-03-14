package com.dcits.app.parser.xml;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import com.dcits.app.exception.BizRuntimeException;

public class XMLParser extends DefaultHandler {

	private boolean flag = true;
	private String eValue = "";
	@SuppressWarnings("rawtypes")
	private ArrayList leafList = new ArrayList();
	private Element tree;
	@SuppressWarnings("rawtypes")
	private Stack stack = new Stack();
	@SuppressWarnings("rawtypes")
	private HashMap attributes = null;
	private static SAXParserFactory factory = null;
	@SuppressWarnings("rawtypes")
	private static LinkedBlockingQueue arrayBQueue = new LinkedBlockingQueue();

	static {
		factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(false);
		factory.setValidating(false);
	}

	public XMLParser() {
		super();
	}

	public void startDocument() {
	}

	@SuppressWarnings("rawtypes")
	public ArrayList getLeaf() {
		return this.leafList;
	}

	public void endDocument() {
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void startElement(String uri, String name, String qName,
			Attributes atts) {
		flag = true;
		eValue = "";
		Item parent = null;
		try {
			parent = (Item) stack.peek();
		} catch (EmptyStackException e) {
		}
		String[] aname = null;
		this.attributes = null;
		if (atts != null && atts.getLength() > 0) {
			aname = new String[atts.getLength()];
			this.attributes = new HashMap();
			for (int i = 0; i < atts.getLength(); i++) {
				aname[i] = atts.getQName(i);
				this.attributes.put(aname[i], atts.getValue(i));
			}
		}

		name = qName;
		Item node = new Item(name.trim(), parent);
		node.setAttributes(attributes, aname);
		if (parent != null) {
			node.setFullPathName(parent.getFullPathName() + "."
					+ node.getName());
			parent.addElement(node);
		} else {
			node.setFullPathName(node.getName());
		}
		stack.push(node);
	}

	@SuppressWarnings("unchecked")
	public void endElement(String uri, String name, String qName) {
		flag = false;
		Element node = (Item) stack.pop();
		if (node != null) {
			boolean bNullValueLeaf = (((Item) node).getElements().size() == 0) ? true
					: false;

			if (eValue != null) {
				eValue = eValue.trim().equals("") ? null : eValue;
			}

			if (eValue != null || bNullValueLeaf) {
				Leaf leaf = new Leaf(node.getName(), node.getParent(), eValue);
				leaf.setAttributes(node.getAttributes());
				leaf.setFullPathName(node.getFullPathName());
				leafList.add(leaf);

				if (leaf.getParent() != null) {
					leaf.getParent().addElement((Object) leaf);
					leaf.getParent().removeElement((Object) node);
				} else {
					node = leaf;
				}
			}
		}

		eValue = "";
		if (stack.empty())
			tree = node;
	}

	public void characters(char ch[], int start, int length) {
		String tempStr = new String(ch, start, length);
		if (flag) {
			eValue = eValue + tempStr;
		} else {
			eValue = tempStr;
		}

		if (!flag) {
			eValue = "";
		}
	}

	@SuppressWarnings("unchecked")
	public Element parseXML(InputSource Is) throws BizRuntimeException {
		SAXParser parser = null;
		try {
			parser = (SAXParser) arrayBQueue.poll();
			if (parser == null) {
				parser = factory.newSAXParser();
			}

			parser.parse(Is, this);
		} catch (Throwable e) {
			throw new BizRuntimeException("解析XML时出现异常", e);
		} finally {
			if (parser != null) {
				arrayBQueue.offer(parser);
			}
		}
		return tree;
	}

	public Element parseXML(StringBuffer sb) throws Exception {
		return parseXMLStr(sb.toString());
	}

	public Element parseXMLStr(String sb) throws Exception {
		String enc = getEncoding(sb);
		InputSource is = new InputSource(new StringReader(sb));
		if (enc != null) {
			is.setEncoding(enc);
		}
		Element element = null;
		element = parseXML(is);
		return element;
	}

	public Element parseXML(String fn) throws BizRuntimeException {
		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					fn)));
			int b;
			while ((b = br.read()) != -1) {
				sb.append((char) b);
			}
			return this.parseXML(sb);
		} catch (Exception e) {
			throw new BizRuntimeException("解析XML时出现异常", e);
		}
	}

	private static String getEncoding(String text) {
		String result = null;
		String xml = text.trim();
		if (xml.startsWith("<?")) {
			int end = xml.indexOf("?>");
			if (end < 0) {
				return null;
			}
			String sub = xml.substring(0, end);
			StringTokenizer tokens = new StringTokenizer(sub, " =\"\'");

			while (tokens.hasMoreTokens()) {
				String token = tokens.nextToken();
				if ("encoding".equalsIgnoreCase(token)) {
					if (tokens.hasMoreTokens()) {
						result = tokens.nextToken();
					}
					break;
				}
			}
		}
		return result;
	}

}