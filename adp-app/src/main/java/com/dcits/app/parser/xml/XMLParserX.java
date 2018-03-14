package com.dcits.app.parser.xml;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.dcits.app.exception.BizRuntimeException;

public class XMLParserX extends DefaultHandler {

	private static final String SAXDriver = "org.apache.xerces.parsers.SAXParser";
	private XMLReader xr;
	private boolean flag = true;
	private String eValue = "";
	private Element tree;
	@SuppressWarnings("rawtypes")
	private Stack stack = new Stack();
	@SuppressWarnings("rawtypes")
	private HashMap attributes = null;

	public XMLParserX() {
		super();
	}

	public void startDocument() {
	}

	public void endDocument() {
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void startElement(String uri, String name, String qName,
			Attributes atts) {
		Item parent = null;
		try {
			parent = (Item) stack.peek();
		} catch (EmptyStackException e) {
		}
		String[] aname = null;
		if (atts != null && atts.getLength() > 0) {
			aname = new String[atts.getLength()];
			this.attributes = new HashMap();
			for (int i = 0; i < atts.getLength(); i++) {
				aname[i] = atts.getQName(i);
				this.attributes.put(aname[i], atts.getValue(i));
			}
		}

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

	public void endElement(String uri, String name, String qName) {
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

				if (leaf.getParent() != null) {
					leaf.getParent().addElement((Object) leaf);
					leaf.getParent().removeElement((Object) node);
				} else {
					node = leaf;
				}
			}
		}

		eValue = "";
		if (stack.empty()) {
			tree = node;
		}
	}

	public void characters(char ch[], int start, int length) {
		if (flag) {
			flag = false;
		}

		String tempStr = new String(ch, start, length);
		if (flag) {
			eValue = eValue + tempStr;
		} else {
			eValue = tempStr;
		}
	}

	public Element parseXML(InputSource Is) throws BizRuntimeException {
		try {
			xr = XMLReaderFactory.createXMLReader(SAXDriver);
			xr.setContentHandler(this);
			xr.setErrorHandler(this);
			xr.parse(Is);
		} catch (Exception e) {
			throw new BizRuntimeException("解析XML时出现异常", e);
		}
		return tree;
	}

	public Element parseXML(StringBuffer sb) throws Exception {
		return parseXMLStr(sb.toString());
	}

	public Element parseXMLStr(String sb) throws Exception {
		ByteArrayInputStream inStream = new ByteArrayInputStream(sb.getBytes());
		InputSource is = new InputSource(inStream);
		return parseXML(is);
	}

	public Element parseXML(String fn) throws BizRuntimeException {
		StringBuffer sb = new StringBuffer();
		try {
			FileInputStream fis = new FileInputStream(fn);
			int b;

			while ((b = fis.read()) != -1) {
				sb.append((char) b);
			}

			return this.parseXML(sb);
		} catch (Exception e) {
			throw new BizRuntimeException("解析XML时出现异常", e);
		}
	}

}