package com.dcits.app.parser.xml;

@SuppressWarnings("serial")
public class PrintVisitor implements Visitor {

	public boolean visitLeaf(Leaf leaf) {
		leaf.getFullPathName();
		return true;
	}

	public boolean visitItem(Item item) {
		item.getFullPathName();
		return true;
	}

}