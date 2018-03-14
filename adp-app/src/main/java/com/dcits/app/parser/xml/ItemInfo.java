package com.dcits.app.parser.xml;

public class ItemInfo {

	private int weight;
	private String item;
	private ItemInfo parent;

	public ItemInfo() {
	}

	public ItemInfo(int weight, String item) {
		this.weight = weight;
		this.item = item;
	}

	public int getWeight() {
		return this.weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getItem() {
		return this.item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public ItemInfo getParent() {
		return this.parent;
	}

	public void setParent(ItemInfo parent) {
		this.parent = parent;
	}

}