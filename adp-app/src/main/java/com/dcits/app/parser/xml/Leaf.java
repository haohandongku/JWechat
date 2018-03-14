package com.dcits.app.parser.xml;

@SuppressWarnings("serial")
public class Leaf extends Element {

	public Leaf(String name, Object parent, Object value) {
		super(name, parent);
		this.value = value;
	}

	public boolean accept(Visitor visitor) {
		return visitor.visitLeaf(this);
	}

	public Object getValue() {
		return this.value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}