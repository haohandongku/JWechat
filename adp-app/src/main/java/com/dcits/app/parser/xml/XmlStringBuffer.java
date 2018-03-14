package com.dcits.app.parser.xml;

public class XmlStringBuffer {

	public final String LABEL_PROTOCAL = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private StringBuffer sb;

	public XmlStringBuffer() {
		sb = new StringBuffer();
	}

	public XmlStringBuffer(boolean printProtocal) {
		if (printProtocal) {
			sb = new StringBuffer(LABEL_PROTOCAL);
		} else {
			sb = new StringBuffer();
		}
	}

	public XmlStringBuffer(StringBuffer sb) {
		this.sb = sb;
	}

	public void append(String label, String value) {
		this.appendHead(label);
		this.append(value);
		this.appendTail(label);
	}

	public void appendHead(String label) {
		this.sb.append("<");
		this.sb.append(label);
		this.sb.append(">");
	}

	public void append(String value) {
		this.sb.append(value);
	}

	public void appendTail(String label) {
		this.sb.append("</");
		this.sb.append(label);
		this.sb.append(">");
	}

	public StringBuffer getValue() {
		return this.sb;
	}

	public String toString() {
		return this.sb.toString();
	}

	public static String filter(String input) {
		if (input == null || input.equals("")) {
			return "";
		}
		StringBuffer filtered = new StringBuffer(input.length());
		char c;
		for (int i = 0; i < input.length(); i++) {
			c = input.charAt(i);
			if (c == '<') {
				filtered.append("&lt;");
			} else if (c == '>') {
				filtered.append("&gt;");
			} else if (c == '"') {
				filtered.append("&quot;");
			} else if (c == '&') {
				filtered.append("&amp;");
			} else if (c == '\'') {
				filtered.append("&apos;");
			} else {
				filtered.append(c);
			}
		}
		return (filtered.toString());
	}

	public static String filterForTax(String input) {
		if (input == null || input.equals("")) {
			return "";
		}
		StringBuffer filtered = new StringBuffer(input.length());
		char c;
		for (int i = 0; i < input.length(); i++) {
			c = input.charAt(i);
			if (c == '<') {
				filtered.append("&lt;");
			} else if (c == '>') {
				filtered.append("&gt;");
			} else if (c == '"') {
				filtered.append("&quot;");
			} else if (c == '&') {
				String remains = input.substring(i);
				if (remains.startsWith("&lt;") || remains.startsWith("&gt;")
						|| remains.startsWith("&quot;")
						|| remains.startsWith("&amp;")
						|| remains.startsWith("&apos;")) {
					filtered.append(c);
				} else {
					filtered.append("&amp;");
				}
			} else if (c == '\'') {
				filtered.append("&apos;");
			} else {
				filtered.append(c);
			}
		}
		return (filtered.toString());
	}

}