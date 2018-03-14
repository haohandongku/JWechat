package com.dcits.app.parser.xml;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Item extends Element {

	@SuppressWarnings("rawtypes")
	protected ArrayList elements = new ArrayList();

	public Item(String name, Object parent) {
		super(name, parent);
	}

	@SuppressWarnings("rawtypes")
	public ArrayList getElements() {
		return elements;
	}

	@SuppressWarnings("unchecked")
	public void addElement(Object o) {
		elements.add(o);
	}

	public void removeElement(Object o) {
		elements.remove(o);
	}

	public boolean accept(Visitor visitor) {
		visitor.visitItem(this);
		for (int i = 0; i < elements.size(); i++) {
			Element e = (Element) elements.get(i);
			if (!e.accept(visitor))
				return false;
		}
		return true;
	}

}