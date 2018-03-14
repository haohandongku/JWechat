package com.dcits.app.parser.xml;

public class TagInfo {

	private int weight;
	private String name;
	private String parent;
	private String value;
	private boolean isLeaf;

	public TagInfo() {
	}

	public TagInfo(int weight, String name, String parent, String value,
			boolean isLeaf) {
		this.weight = weight;
		this.name = name;
		this.parent = parent;
		this.value = value;
		this.isLeaf = isLeaf;
	}

	public int getWeight() {
		return this.weight;
	}

	public String getName() {
		return this.name;
	}

	public String getParent() {
		return this.parent;
	}

	public String getValue() {
		return this.value;
	}

	public boolean getIsLeaf() {
		return this.isLeaf;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setIsLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

}