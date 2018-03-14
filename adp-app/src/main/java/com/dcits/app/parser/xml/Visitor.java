package com.dcits.app.parser.xml;

import java.io.Serializable;

public interface Visitor extends Serializable {

	public boolean visitLeaf(Leaf leaf);

	public boolean visitItem(Item item);

}