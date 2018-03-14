package com.dcits.app.parser.xml;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class GetItemVisitor implements Visitor {

	private String itemName;
	@SuppressWarnings("rawtypes")
	private ArrayList found = new ArrayList();
	private boolean bFullPathMode = false;
	private boolean bFindAll = false;

	public void setBFullPathMode(boolean bFullPathMode) {
		this.bFullPathMode = bFullPathMode;
	}

	public boolean getBFullPathMode() {
		return this.bFullPathMode;
	}

	public GetItemVisitor(String itemName) {
		this.itemName = itemName;
	}

	public GetItemVisitor(String itemName, boolean bFindAll) {
		this.itemName = itemName;
		this.bFindAll = bFindAll;
	}

	@SuppressWarnings("unchecked")
	public boolean visitLeaf(Leaf leaf) {
		if (this.equal(leaf, itemName)) {
			found.add(leaf);
			if (!bFindAll) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean visitItem(Item item) {
		if (this.equal(item, itemName)) {
			found.add(item);
			if (!bFindAll) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	public ArrayList getFound() {
		return found;
	}

	public boolean equal(Element element, String name) {
		if (bFullPathMode) {
			return element.equalFullPathName(name);
		} else {
			return element.equalName(name);
		}
	}

}