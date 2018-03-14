package com.dcits.app.parser.xml;

import java.io.Serializable;
import java.util.HashMap;

@SuppressWarnings("serial")
public class Element implements Serializable {

	protected Object value;
	private String name;
	private String fullPathName;
	private Attribute attributes;
	private Item parent;

	public Element(String name, Object parent) {
		this.name = name;
		this.parent = (Item) parent;
	}

	public boolean equalName(String name) {
		return this.name == null ? false : this.name.equals(name);
	}

	public void setFullPathName(String fullPathName) {
		this.fullPathName = fullPathName;
	}

	public String getFullPathName() {
		return fullPathName;
	}

	public boolean equalFullPathName(String fullPathName) {
		return this.fullPathName == null ? false : this.fullPathName
				.equals(fullPathName);
	}

	public Element getItem(String fullPathName) {
		return null;
	}

	public boolean accept(Visitor visitor) {
		return true;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@SuppressWarnings("rawtypes")
	public void setAttributes(HashMap map, String[] names) {
		this.attributes = new Attribute(names, map);
	}

	public void setAttributes(Attribute attributes) {
		this.attributes = attributes;
	}

	public Attribute getAttributes() {
		return attributes;
	}

	public String[] getAttributesName() {
		return attributes.getNames();
	}

	public String getAttributeValue(String name) {
		return attributes.getAttributeValue(name);
	}

	public int getAttributeCount() {
		if (attributes == null) {
			return 0;
		}
		return attributes.getAttributeCount();
	}

	public String getAttributeName(int idx) {
		return attributes.getAttributeName(idx);
	}

	public String getAttributeValue(int idx) {
		return attributes.getAttributeValue(idx);
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}

	public Item getParent() {
		return parent;
	}

	public void setParent(Item item) {
		this.parent = item;
	}

}