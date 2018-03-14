package com.dcits.app.parser.xml;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import com.dcits.app.exception.BizRuntimeException;

@SuppressWarnings("serial")
public class XMLDataObject implements IDataObject {

	public static final int TOXML_NULL = 0;
	public static final int TOXML_NOTNULL = 1;
	public static final int TOXML_SHORT = 2;
	private Element xmlTree;
	private Element treeStub;
	private String startPath;
	private Element cutRoot;
	@SuppressWarnings("rawtypes")
	private ArrayList data = new ArrayList();
	private boolean bFullPathMode = false;
	private boolean bSingleRow = false;

	public XMLDataObject() {
	}

	public XMLDataObject(Element elment) throws NodeNotFoundException {
		if (elment == null) {
			throw new IllegalArgumentException("参数为空！");
		}
		this.startPath = elment.getFullPathName();
		treeStub = xmlTree = elment;
	}

	public XMLDataObject(Element elment, String startPath)
			throws NodeNotFoundException {
		if (elment == null || startPath == null) {
			throw new IllegalArgumentException("参数为空！");
		}
		this.startPath = startPath;
		treeStub = xmlTree = elment;
	}

	public Element getCutRoot() {
		return cutRoot;
	}

	public boolean getBFullPathMode() {
		return this.bFullPathMode;
	}

	public void setBFullPathMode(boolean bFullPathMode) {
		this.bFullPathMode = bFullPathMode;
	}

	public void setXmlTree(Item item) {
		xmlTree = item;
	}

	@SuppressWarnings("rawtypes")
	public void rootScrollTo(String nodeName) throws BizRuntimeException {
		ArrayList list = this.getItem(treeStub, nodeName);
		if (list == null || list.size() < 1) {
			throw new BizRuntimeException("错误的结点名：" + nodeName);
		}
		this.xmlTree = (Element) list.get(0);
		this.startPath = xmlTree.getFullPathName();
		this.retrieve();
	}

	public void resetRoot() throws BizRuntimeException {
		this.xmlTree = treeStub;
		this.startPath = xmlTree.getFullPathName();
		this.retrieve();
	}

	public void resetRoot(String startPath) throws BizRuntimeException {
		this.xmlTree = treeStub;
		this.startPath = startPath;
		this.retrieve();
	}

	@SuppressWarnings("rawtypes")
	public ArrayList getItem(Element element, String itemName) {
		if (element == null || itemName == null) {
			return null;
		}
		GetItemVisitor getItemVisitor = new GetItemVisitor(itemName);
		getItemVisitor.setBFullPathMode(this.bFullPathMode);
		element.accept(getItemVisitor);
		return getItemVisitor.getFound();
	}

	@SuppressWarnings("rawtypes")
	public ArrayList getItems(Element element, String itemName) {
		if (element == null || itemName == null)
			return null;

		GetItemVisitor getItemVisitor = new GetItemVisitor(itemName, true);
		getItemVisitor.setBFullPathMode(this.bFullPathMode);
		element.accept(getItemVisitor);
		return getItemVisitor.getFound();
	}

	@SuppressWarnings("rawtypes")
	public Element getUniqueItem(String itemName) {
		ArrayList list = getItem(this.xmlTree, itemName);
		if (null != list && list.size() > 0) {
			return (Element) list.get(0);
		}
		return null;
	}

	public int getColumnCount(int arow) {
		if (data == null) {
			return 0;
		}

		if (xmlTree instanceof Leaf) {
			if (arow != 0) {
				throw new IllegalArgumentException("错误的行位置：" + arow);
			}
			return 0;
		}

		if (isBSingleRow()) {
			if (arow != 0) {
				throw new IllegalArgumentException("错误的行位置：" + arow);
			}
			return data.size();
		}

		if (arow < 0 || arow >= data.size()) {
			throw new IllegalArgumentException("错误的行位置：" + arow);
		}
		Item row = (Item) data.get(arow);

		return row.getElements().size();
	}

	public int getColumnCount() {
		if (data == null) {
			return 0;
		}

		if (xmlTree instanceof Leaf) {
			return 0;
		}

		if (isBSingleRow()) {
			return data.size();
		}
		Item row = (Item) data.get(0);
		return row.getElements().size();
	}

	public boolean isBSingleRow() {
		if (data == null) {
			return false;
		}

		if (xmlTree instanceof Leaf) {
			return true;
		}

		if (data.size() == 0) {
			return true;
		}

		for (int i = 0; i < data.size(); i++) {
			if (data.get(i) instanceof Leaf) {
				return true;
			}
		}
		return false;
	}

	public String getItemValue(String nodeName) {
		return this.getItemValue(this.xmlTree, nodeName);
	}

	@SuppressWarnings("rawtypes")
	public String getItemValue(Element element, String itemName) {
		if (element == null || itemName == null) {
			return null;
		}

		if (element instanceof Leaf) {
			Leaf leaf = (Leaf) element;
			if (element.getName().equals(itemName)) {
				return (String) leaf.getValue();
			} else {
				return null;
			}
		}

		ArrayList list = getItem((Item) element, itemName);
		if (list.size() == 0) {
			return null;
		}

		Debugger.ASSERT(list.size() == 1);
		return (String) ((Element) list.get(0)).getValue();
	}

	public void setTransObject(Object t) {
	}

	public long retrieve(String sql) throws BizRuntimeException {
		return -1;
	}

	public void update(boolean bResetUpdate) throws BizRuntimeException {
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public long retrieve() throws BizRuntimeException {
		retrieveStart();
		data = new ArrayList();

		if (xmlTree instanceof Leaf) {
			cutRoot = xmlTree;
			data.add(xmlTree);
		} else {
			boolean b = this.bFullPathMode;
			this.setBFullPathMode(true);
			ArrayList list = this.getItem((Item) xmlTree, startPath);
			if (list != null) {
				cutRoot = (Element) list.get(0);
				if (cutRoot != null) {
					if (cutRoot instanceof Leaf) {
						data.add(cutRoot);
					} else {
						Item cutRootItem = (Item) cutRoot;
						if ((cutRootItem).getElements() == null
								|| (cutRootItem).getElements().size() == 0)
							data.add(cutRoot);
						else {
							if (!this.checkSinglRow(cutRootItem)) {
								data = (cutRootItem).getElements();
							} else {
								data.add(cutRoot);
							}
						}
					}
				}
			}
			this.setBFullPathMode(b);
		}

		retrieveEnd();
		bSingleRow = this.isBSingleRow();
		return this.getRowCount();
	}

	public void retrieveStart() throws BizRuntimeException {
	}

	public boolean retrieveRow(long row) {
		return true;
	}

	public void retrieveEnd() throws BizRuntimeException {
	}

	public String sqlPreview(String sql) {
		return null;
	}

	public long getRowCount() {
		if (data == null) {
			return 0;
		}

		return bSingleRow ? 1 : data.size();
	}

	@SuppressWarnings("rawtypes")
	public void reset() {
		if (data != null) {
			data.clear();
		} else {
			data = new ArrayList();
		}
	}

	public void resetUpdate() {
	}

	public void setItemAny(long row, int col, Object value) {
	}

	public Attribute getItemAttribute(long arow, int col) {
		if (col < 0) {
			return null;
		}
		int row = (int) arow;
		if (row < 0 || row >= this.getRowCount()) {
			throw new IllegalArgumentException("行号超出范围！");
		}
		if (bSingleRow) {
			if (xmlTree instanceof Leaf) {
				return xmlTree.getAttributes();
			}
			Leaf leaf = (Leaf) ((Item) xmlTree).getElements().get(col);
			return leaf.getAttributes();
		}

		Element element = (Element) data.get(row);
		element = this.getColElement(element, col);
		return element.getAttributes();
	}

	public Attribute getItemAttribute(long arow, String colName) {
		if (colName == null) {
			return null;
		}
		int row = (int) arow;
		if (row < 0 || row >= this.getRowCount()) {
			throw new IllegalArgumentException("行号超出范围！");
		}
		Element element = null;
		if (bSingleRow) {
			element = this.getColElement(xmlTree, colName);
		} else {
			element = (Element) data.get(row);
			element = this.getColElement(element, colName);
		}
		return element.getAttributes();
	}

	public Object getItemNameAny(long arow, int col) {
		if (col < 0) {
			return null;
		}

		int row = (int) arow;
		if (row < 0 || row >= this.getRowCount()) {
			throw new IllegalArgumentException("行号超出范围：" + arow);
		}
		Element element;
		if (bSingleRow) {
			element = this.getColElement(xmlTree, col);
		} else {
			Item item = (Item) data.get(row);
			element = this.getColElement(item, col);
		}
		return element.getName();
	}

	public Object getItemAny(long arow, int col) {
		int row = (int) arow;
		if (row < 0 || row >= this.getRowCount()) {
			throw new IllegalArgumentException("行号超出范围：" + arow);
		}
		Element element;
		if (bSingleRow) {
			element = this.getColElement(xmlTree, col);
		} else {
			Item item = (Item) data.get(row);
			element = this.getColElement(item, col);
		}
		return element instanceof Leaf ? element.getValue() : element;
	}

	public Object getItemAny(long arow, String colName) {
		int row = (int) arow;
		if (row < 0 || row >= this.getRowCount()) {
			throw new IllegalArgumentException("行号超出范围：" + arow);
		}
		Element element;
		if (bSingleRow) {
			element = this.getColElement(xmlTree, colName);
		} else {
			Item it = (Item) data.get(row);
			element = this.getColElement(it, colName);
		}
		return element instanceof Leaf ? element.getValue() : element;
	}

	public void deleteRow(long row) {
	}

	public void insertDoDeprecated(XMLDataObject xdo) throws Exception {
		XMLParser parser = new XMLParser();
		resetRoot();
		String str = this.toXML().toString();
		str = replace(str, "<" + this.treeStub.getName() + ">", "");
		str = replace(str, "</" + this.treeStub.getName() + ">", "");

		StringBuffer sb = new StringBuffer();
		sb.append("<" + this.treeStub.getName() + ">");
		sb.append(str);
		sb.append(xdoToXML(xdo));
		sb.append("</" + this.treeStub.getName() + ">");
		Element element = parser.parseXML(sb);

		if (element == null) {
			throw new IllegalArgumentException("参数为空！");
		}
		this.startPath = element.getFullPathName();
		treeStub = xmlTree = element;
		retrieve();
	}

	public void insertDo(XMLDataObject xdo) throws Exception {
		this.insertAsChild(this.xmlTree, xdo.xmlTree);
	}

	public void insertDo(XMLDataObject xdo, String nodePath) throws Exception {
		insertDo(xdo, nodePath, 0);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void insertDoDeprecated(XMLDataObject xmlDataObject,
			String nodePath, int n) throws Exception {
		ArrayList list = new ArrayList();
		XMLParser parser = new XMLParser();
		resetRoot();

		for (int i = 0; i <= n; i++) {
			XMLDataObject xdo = getDo(nodePath, i);
			list.add(xdoToXML(xdo));
		}

		int start = 0;
		XMLDataObject temp = getDo(nodePath, n);
		temp.retrieve();
		String string = xdoToXML(temp);
		temp.insertDo(xmlDataObject);

		String xml = xdoToXML(this);
		int e = 0;
		for (int i = 0; i <= n; i++) {
			String tmp = (String) list.get(i);
			start = xml.indexOf(tmp, e);
			e = start + tmp.length();
		}

		xml = xml.substring(0, start) + string
				+ xml.substring(start + string.length());
		StringBuffer sb = new StringBuffer(xml);

		Element t = parser.parseXML(sb);
		if (t == null) {
			throw new IllegalArgumentException("参数为空！");
		}
		this.startPath = t.getFullPathName();
		treeStub = xmlTree = t;
		retrieve();
	}

	public void insertDo(XMLDataObject xdo, String nodePath, int n)
			throws Exception {
		Element element1 = (Element) this.getItem(this.xmlTree, nodePath)
				.get(n);
		Element element2 = xdo.xmlTree;
		this.insertAsChild(element1, element2);
	}

	private String xdoToXML(XMLDataObject xdo) throws Exception {
		if (xdo == null) {
			throw new Exception("XMLDataObject节点为空！");
		}
		xdo.resetRoot();
		String xml = xdo.toXML().toString();
		if (!xml.startsWith("<" + xdo.treeStub.getName() + ">")) {
			xml = xdo.toXML(true).toString();
		}
		return xml;
	}

	private String replace(String source, String oldString, String newString) {
		StringBuffer output = new StringBuffer();
		int lengthOfSource = source.length();
		int lengthOfOld = oldString.length();
		int posStart = 0;
		int pos;

		while ((pos = source.indexOf(oldString, posStart)) >= 0) {
			output.append(source.substring(posStart, pos));
			output.append(newString);
			posStart = pos + lengthOfOld;
		}
		if (posStart < lengthOfSource)
			output.append(source.substring(posStart));
		return output.toString();
	}

	@SuppressWarnings("rawtypes")
	public XMLDataObject getDo(String nodePath) throws Exception {
		ArrayList al = getItem(this.xmlTree, nodePath);
		if (null != al && al.size() > 0) {
			return new XMLDataObject((Element) al.get(0));
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public XMLDataObject getDo(String nodePath, int n) throws Exception {
		ArrayList al = getItem(this.xmlTree, nodePath);
		if (null != al && al.size() > 0) {
			return new XMLDataObject((Element) al.get(n));
		}
		return null;
	}

	public long insert(long row) throws Exception {
		return -1;
	}

	@SuppressWarnings("unchecked")
	public long insert(long row, String rowName, String[] cols) {
		if (row < 0) {
			row = 0;
		}
		if (row > data.size()) {
			row = data.size() + 1;
		}
		if (cols == null) {
			Leaf leaf = new Leaf(rowName, null, null);
			data.add(leaf);
			return row;
		}

		Item itemrow = new Item(rowName, null);
		data.add(itemrow);

		for (int i = 0; i < cols.length; i++) {
			itemrow.elements.add(new Leaf(cols[i], itemrow, null));
		}
		return row;
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	public void setItemAny(long arow, String colName, Object value) {
		int row = (int) arow;
		if (row < 0 || row > data.size()) {
			throw new IllegalArgumentException("行位置超出！");
		}
		if (colName == null) {
			throw new IllegalArgumentException("列名为空！");
		}
		Element element = (Element) data.get(row);
		if (element instanceof Leaf) {
			if (!element.getName().equals(colName)) {
				throw new IllegalArgumentException("没有找到列：" + colName);
			}
			element.setValue(anyToString(value));
			return;
		}

		ArrayList cols = ((Item) element).elements;
		boolean bFound = false;
		for (int i = 0; i < cols.size(); i++) {
			Element col = (Element) cols.get(i);
			if (col.getName().equals(colName)) {
				if (col instanceof Leaf) {
					col.setValue(value);
					return;
				}
			}
		}
		throw new IllegalArgumentException("没有找到列：" + colName);
	}

	public String getColName(int col) {
		return null;
	}

	public int getColOrder(String colName) {
		return -1;
	}

	@SuppressWarnings("rawtypes")
	protected int getColOrder(ArrayList al, String colName) {
		if (al == null || colName == null) {
			return -1;
		}
		for (int i = 0; al != null && i < al.size(); i++) {
			Element element = (Element) al.get(i);
			if (this.bFullPathMode) {
				if (colName.equals(element.getFullPathName())) {
					return i;
				}
			} else {
				if (colName.equals(element.getName())) {
					return i;
				}
			}
		}
		return -1;
	}

	@SuppressWarnings("static-access")
	public StringBuffer asXML(boolean bPrintRoot) {
		StringBuffer sb = new StringBuffer();
		if (bPrintRoot && this.cutRoot != null) {
			sb.append("<" + cutRoot.getName());
			String[] attrNames = cutRoot.getAttributesName();
			if (attrNames != null && attrNames.length > 0) {
				String attrName;
				String attrValue;
				for (int i = 0; i < attrNames.length; i++) {
					attrName = attrNames[i];
					if (attrName != null && !attrName.trim().equals("")) {
						attrValue = cutRoot.getAttributeValue(attrName);
						if (attrValue == null) {
							attrValue = "";
						}

						attrValue = XmlStringBuffer.filter(attrValue);
						sb.append(" ");
						sb.append(attrName);
						sb.append("=\"");

						sb.append(attrValue);
						sb.append("\"");
					}
				}
			}
			sb.append(">");
		}

		if (data != null) {
			for (int i = 0; i < data.size(); i++) {
				sb.append(nodeToXMLWithAttr(bPrintRoot, (Element) data.get(i),
						this.TOXML_NOTNULL, true).toString());
			}
		}

		if (bPrintRoot && this.cutRoot != null)
			sb.append("</" + cutRoot.getName() + ">");

		return sb;
	}

	public StringBuffer toXMLWithAttr() {
		return toXMLWithAttr(false);
	}

	public StringBuffer toXMLWithAttr(boolean bPrintRoot) {
		StringBuffer sb = new StringBuffer();
		if (bPrintRoot && this.cutRoot != null) {
			sb.append("<" + cutRoot.getName());
			String[] attrNames = cutRoot.getAttributesName();
			if (attrNames != null && attrNames.length > 0) {
				String attrName;
				String attrValue;
				for (int i = 0; i < attrNames.length; i++) {
					attrName = attrNames[i];
					if (attrName != null && !attrName.trim().equals("")) {
						attrValue = cutRoot.getAttributeValue(attrName);
						if (attrValue == null) {
							attrValue = "";
						}

						sb.append(" ");
						sb.append(attrName);
						sb.append("=\"");
						sb.append(attrValue);
						sb.append("\"");
					}
				}
			}
			sb.append(">");
		}

		if (data != null) {
			for (int i = 0; i < data.size(); i++) {
				sb.append(nodeToXMLWithAttr((Element) data.get(i)).toString());
			}
		}

		if (bPrintRoot && this.cutRoot != null)
			sb.append("</" + cutRoot.getName() + ">");

		return sb;
	}

	protected StringBuffer nodeToXMLWithAttr(Element node) {
		StringBuffer sb = new StringBuffer();
		if (node instanceof Leaf) {
			sb.append("<" + node.getName());
			String[] attrNames = node.getAttributesName();
			if (attrNames != null && attrNames.length > 0) {
				String attrName;
				String attrValue;
				for (int i = 0; i < attrNames.length; i++) {
					attrName = attrNames[i];
					if (attrName != null && !attrName.trim().equals("")) {
						attrValue = node.getAttributeValue(attrName);
						if (attrValue == null) {
							attrValue = "";
						}

						sb.append(" ");
						sb.append(attrName);
						sb.append("=\"");
						sb.append(attrValue);
						sb.append("\"");
					}
				}
			}
			sb.append(">");

			sb.append(node.getValue());
			sb.append("</" + node.getName() + ">");
			return sb;
		}

		Item it = (Item) node;
		sb.append("<" + node.getName());
		String[] attrNames = node.getAttributesName();
		if (attrNames != null && attrNames.length > 0) {
			String attrName;
			String attrValue;
			for (int i = 0; i < attrNames.length; i++) {
				attrName = attrNames[i];
				if (attrName != null && !attrName.trim().equals("")) {
					attrValue = node.getAttributeValue(attrName);
					if (attrValue == null) {
						attrValue = "";
					}

					sb.append(" ");
					sb.append(attrName);
					sb.append("=\"");
					sb.append(attrValue);
					sb.append("\"");
				}
			}
		}
		sb.append(">");
		for (int i = 0; i < it.elements.size(); i++) {
			sb.append(nodeToXMLWithAttr((Element) it.elements.get(i)));
		}
		sb.append("</" + node.getName() + ">");
		return sb;
	}

	public StringBuffer toXMLWithRoot(String root) {
		StringBuffer sb = new StringBuffer();
		sb.append("<" + root + ">");

		if (data != null) {
			for (int i = 0; i < data.size(); i++) {
				sb.append(nodeToXML((Element) data.get(i)).toString());
			}
		}

		sb.append("</" + root + ">");

		return sb;
	}

	public StringBuffer toXMLWithRoot(boolean bPrintRoot, String root) {
		StringBuffer sb = new StringBuffer();
		sb.append("<" + root + ">");
		if (bPrintRoot && this.cutRoot != null)
			sb.append("<" + cutRoot.getName() + ">");
		if (data != null) {
			for (int i = 0; i < data.size(); i++) {
				sb.append(nodeToXML((Element) data.get(i)).toString());
			}
		}
		if (bPrintRoot && this.cutRoot != null)
			sb.append("</" + cutRoot.getName() + ">");

		sb.append("</" + root + ">");
		return sb;
	}

	public StringBuffer asXML() {
		return asXML(false);
	}

	public StringBuffer toAutoRootXML() {
		if ((this.cutRoot instanceof Leaf)) {
			return this.toXML();
		} else {
			Item cutRootItem = (Item) cutRoot;
			if (this.checkSinglRow(cutRootItem)
					|| (cutRootItem).getElements() == null
					|| (cutRootItem).getElements().size() == 0) {
				return this.toXML();
			} else {
				return this.toXML(true);
			}
		}
	}

	protected StringBuffer nodeToXML(Element node) {
		StringBuffer sb = new StringBuffer();
		if (node instanceof Leaf) {
			sb.append("<" + node.getName() + ">");
			sb.append(node.getValue());
			sb.append("</" + node.getName() + ">");
			return sb;
		}

		Item it = (Item) node;
		sb.append("<" + node.getName() + ">");
		for (int i = 0; i < it.elements.size(); i++) {
			sb.append(nodeToXML((Element) it.elements.get(i)));
		}
		sb.append("</" + node.getName() + ">");
		return sb;
	}

	public String getRowAttributeValue(long arow, String name) {
		Element element = (Element) getRow(arow);
		return (String) element.getAttributeValue(name);
	}

	public String getRowAttributeValue(long arow, int idx) {
		return (String) ((Element) getRow(arow)).getAttributeValue(idx);
	}

	@SuppressWarnings("rawtypes")
	public Element getColElement(Element row, int col) {
		if (row == null || col < 0)
			return null;
		if (row instanceof Leaf) {
			if (col != 0)
				throw new IllegalArgumentException("错误的列位置：" + col);
			return row;
		}
		Element element = null;
		Item rowItem = (Item) row;
		if (this.checkSinglRow(rowItem)) {
			if (col >= rowItem.getElements().size()) {
				throw new IllegalArgumentException("列超出范围：" + col);
			}
			element = (Leaf) (rowItem.getElements().get(col));
			return element;
		}
		element = (Element) rowItem.getElements().get(0);
		if (element instanceof Leaf) {
			if (col != 0) {
				throw new IllegalArgumentException("列超出范围：" + col);
			}
			return element;
		}
		ArrayList colList = ((Item) element).getElements();
		if (colList == null || col >= colList.size()) {
			throw new IllegalArgumentException("列超出范围：" + col);
		}
		return (Element) colList.get(col);
	}

	@SuppressWarnings("rawtypes")
	public Element getColElement(Element row, String colName) {
		if (row == null || colName == null)
			return null;
		if (row instanceof Leaf) {
			String name = this.bFullPathMode ? row.getFullPathName() : row
					.getName();
			if (!colName.equals(name)) {
				throw new IllegalArgumentException("列不存在：" + colName);
			}
			return row;
		}
		Element element = null;
		Item rowItem = (Item) row;
		if (this.checkSinglRow(rowItem)) {
			int col = this.getColOrder(rowItem.getElements(), colName);
			if (col < 0) {
				throw new IllegalArgumentException("列不存在：" + colName);
			}
			element = (Element) (rowItem.getElements().get(col));
			return element;
		}
		element = (Element) rowItem.getElements().get(0);
		if (element instanceof Leaf) {
			String name = this.bFullPathMode ? element.getFullPathName()
					: element.getName();
			if (!colName.equals(name)) {
				throw new IllegalArgumentException("列不存在：" + colName);
			}
			return element;
		}
		ArrayList colList = ((Item) element).getElements();
		int col = this.getColOrder(colList, colName);
		if (col < 0) {
			throw new IllegalArgumentException("列不存在：" + colName);
		}
		return (Element) colList.get(col);
	}

	@SuppressWarnings("rawtypes")
	protected boolean checkSinglRow(Item it) {
		ArrayList al = it.getElements();
		if (al == null || al.size() == 0)
			return true;
		for (int i = 0; i < al.size(); i++) {
			if (al.get(i) instanceof Leaf)
				return true;
		}
		return false;
	}

	public Object getRow(long arow) {
		int row = (int) arow;
		if (data == null || row >= data.size() || row < 0) {
			return null;
		}
		return data.get(row);
	}

	public void setItem(Object o[]) throws Exception {
	}

	public long insertRow(long row, Object o) {
		return -1;
	}

	public void setFilter(String s) {
	}

	public void setTableName(String n) {

	}

	public void setObjectName(String n) {
	}

	public void registerValidCtrl(String name) {
	}

	public void sqlExecEnd(String sql) {
	}

	public String sqlPreview(String sql, String params) {
		return null;
	}

	public void sqlExecEnd(String sql, String params) {

	}

	public static XMLDataObject unionXDO(XMLDataObject xdo[]) throws Exception {
		XMLParser parser = new XMLParser();
		if (xdo[0] == null) {
			throw new IllegalArgumentException("参数为空！");
		}
		xdo[0].retrieve();
		StringBuffer sb = new StringBuffer();
		sb.append("<" + xdo[0].treeStub.getName() + ">");
		sb.append(xdo[0].toXML().toString());
		for (int i = 1; i < xdo.length; i++) {
			if (xdo[i] != null) {
				xdo[i].retrieve();
				sb.append(xdo[i].toXML().toString());
			}
		}

		sb.append("</" + xdo[0].treeStub.getName() + ">");
		Element element = parser.parseXML(sb);

		if (element == null) {
			throw new IllegalArgumentException("参数为空！");
		}
		return new XMLDataObject(element);

	}

	public String getRowName(long arow) {
		return (String) ((Element) getRow(arow)).getName();
	}

	public void rename(String newName) {
		this.cutRoot.setName(newName);
	}

	public void renameRow(long arow, String newName)
			throws IllegalArgumentException {
		int row = (int) arow;

		if (row < 0 || row > data.size()) {
			throw new IllegalArgumentException("行位置超出！");
		}
		Element element = (Element) data.get(row);

		element.setName(newName);
	}

	@SuppressWarnings("rawtypes")
	public void renameItem(long arow, String colName, String newName)
			throws IllegalArgumentException {
		int row = (int) arow;

		if (row < 0 || row > data.size()) {
			throw new IllegalArgumentException("行位置超出！");
		}
		if (colName == null) {
			throw new IllegalArgumentException("列名为空！");
		}
		Element element = (Element) data.get(row);
		if (element instanceof Leaf) {
			if (!element.getName().equals(colName)) {
				throw new IllegalArgumentException("没有找到列：" + colName);
			}
			element.setName(newName);
			return;
		}

		ArrayList cols = ((Item) element).elements;
		for (int i = 0; i < cols.size(); i++) {
			Element c = (Element) cols.get(i);
			if (c.getName().equals(colName)) {
				if (c instanceof Leaf) {
					c.setName(newName);
					return;
				}
			}
		}
		throw new IllegalArgumentException("没有找到列：" + colName);
	}

	public void delete(Element e1) throws BizRuntimeException {
		Item parent = e1.getParent();
		parent.removeElement(e1);
		this.retrieve();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void deleteAndTakeOverTheChildren(Element element)
			throws BizRuntimeException {
		Item parent = element.getParent();
		if (element instanceof Item) {
			int index = parent.elements.indexOf(element);
			parent.removeElement(element);
			parent.elements.addAll(index, ((Item) element).elements);
			Iterator it = ((Item) element).elements.iterator();
			for (; it.hasNext();) {
				Element child = (Element) it.next();
				child.setParent(parent);
			}
		} else {
			parent.removeElement(element);
		}
		this.retrieve();
	}

	public void insertAsSibling(Element element1, Element element2)
			throws BizRuntimeException {
		Item parent = element1.getParent();
		parent.addElement(element2);
		element2.setParent(parent);
		this.retrieve();
	}

	@SuppressWarnings("unchecked")
	public void insertAsChild(Element element1, Element element2)
			throws BizRuntimeException {
		if (element1 instanceof Leaf) {
			Item item = new Item(element1.getName(), element1.getParent());
			item.setAttributes(element1.getAttributes());
			item.setFullPathName(element1.getFullPathName());
			item.setValue(element1.getValue());

			Item parent = element1.getParent();
			int i = parent.elements.indexOf(element1);
			parent.elements.set(i, item);

			item.addElement(element2);
			element2.setParent(item);
		} else {
			((Item) element1).addElement(element2);
			element2.setParent((Item) element1);
		}
		this.retrieve();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void insertAsChild(int position, String nodeName, String insertName,
			String insertValue) throws BizRuntimeException {
		Element curRoot = this.getCutRoot();
		int insertPos = position;
		if (curRoot != null) {
			ArrayList nodes = this.getItems(curRoot, nodeName);
			if (nodes != null && nodes.size() > 0) {
				if (position < 0 || position >= nodes.size()) {
					insertPos = nodes.size() - 1;
				}
				Element node = (Element) nodes.get(insertPos);
				if (node != null) {
					Leaf leaf = null;
					if (node instanceof Leaf) {
						Item parent = node.getParent();
						Item item = new Item(node.getName(), parent);
						item.setAttributes(node.getAttributes());
						item.setFullPathName(node.getFullPathName());
						item.setValue(node.getValue());

						int i = parent.elements.indexOf(node);
						parent.elements.set(i, item);

						leaf = new Leaf(insertName, item, insertValue);
						leaf.setFullPathName(item.getFullPathName() + "."
								+ insertName);
						item.addElement(leaf);
						leaf.setParent(item);
					} else {
						leaf = new Leaf(insertName, ((Item) node), insertValue);
						leaf.setFullPathName(node.getFullPathName() + "."
								+ insertName);
						((Item) node).addElement(leaf);
						leaf.setParent(((Item) node));
					}
					this.retrieve();
				} else {
					throw new BizRuntimeException("找不到节点：" + nodeName);
				}
			} else {
				throw new BizRuntimeException("找不到节点：" + nodeName);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	public void insertAsChild(int position, String nodeName,
			String[] insertNames, String[] insertValues)
			throws BizRuntimeException {
		Element curRoot = this.getCutRoot();
		int insertPos = position;
		if (curRoot != null) {
			if (insertNames != null && insertValues != null
					&& insertNames.length == insertValues.length) {
				int len = insertNames.length;
				ArrayList nodes = null;
				Element node = null;
				int j = -1;
				for (int i = 0; i < len; i++) {
					nodes = this.getItems(curRoot, nodeName);
					if (nodes != null && nodes.size() > 0) {
						if (position < 0 || position >= nodes.size()) {
							insertPos = nodes.size() - 1;
						}
						node = (Element) nodes.get(insertPos);
						if (node != null) {
							if (node instanceof Leaf) {
								Item parent = node.getParent();
								Item item = new Item(node.getName(), parent);
								item.setAttributes(node.getAttributes());
								item.setFullPathName(node.getFullPathName());
								item.setValue(node.getValue());

								j = parent.elements.indexOf(node);
								parent.elements.set(i, item);

								Leaf leaf = new Leaf(insertNames[i], item,
										insertValues[i]);
								leaf.setFullPathName(item.getFullPathName()
										+ "." + insertNames[i]);
								item.addElement(leaf);
								leaf.setParent(item);
							} else {
								Leaf leaf = new Leaf(insertNames[i],
										((Item) node), insertValues[i]);
								leaf.setFullPathName(node.getFullPathName()
										+ "." + insertNames[i]);
								((Item) node).addElement(leaf);
								leaf.setParent(((Item) node));
							}
						} else {
							throw new BizRuntimeException("找不到节点：" + nodeName);
						}
					} else {
						throw new BizRuntimeException("找不到节点：" + nodeName);
					}
				}
				this.retrieve();
			} else {
				throw new BizRuntimeException("要插入的数据项为空或者名称与值的数目不匹配！");
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void insertAsParent(Element element, Item item)
			throws BizRuntimeException {
		Item parent = element.getParent();
		item.setParent(parent);
		element.setParent(item);
		item.addElement(element);
		int i = parent.elements.indexOf(element);
		parent.elements.set(i, item);
		this.retrieve();
	}

	public StringBuffer toXMLEx(int type) {
		return toXMLEx(false, type);
	}

	public StringBuffer toXMLEx(boolean bPrintRoot, int type) {
		StringBuffer sb = new StringBuffer();
		if (bPrintRoot && this.cutRoot != null)
			sb.append("<" + cutRoot.getName() + ">");

		if (data != null) {
			for (int i = 0; i < data.size(); i++) {
				sb.append(nodeToXMLEx((Element) data.get(i), type).toString());
			}
		}

		if (bPrintRoot && this.cutRoot != null) {
			sb.append("</" + cutRoot.getName() + ">");
		}
		return sb;
	}

	public StringBuffer toXMLWithRootEx(boolean bPrintRoot, String root,
			int type) {
		StringBuffer sb = new StringBuffer();
		sb.append("<" + root + ">");
		if (bPrintRoot && this.cutRoot != null)
			sb.append("<" + cutRoot.getName() + ">");
		if (data != null) {
			for (int i = 0; i < data.size(); i++) {
				sb.append(nodeToXMLEx((Element) data.get(i), type).toString());
			}
		}
		if (bPrintRoot && this.cutRoot != null) {
			sb.append("</" + cutRoot.getName() + ">");
		}
		sb.append("</" + root + ">");
		return sb;
	}

	public StringBuffer toXMLWithRootEx(String root, int type) {
		StringBuffer sb = new StringBuffer();
		sb.append("<" + root + ">");

		if (data != null) {
			for (int i = 0; i < data.size(); i++) {
				sb.append(nodeToXMLEx((Element) data.get(i), type).toString());
			}
		}

		sb.append("</" + root + ">");
		return sb;
	}

	public StringBuffer toAutoRootXMLEx(int type) {
		if ((this.cutRoot instanceof Leaf)) {
			return this.toXMLEx(type);
		} else {
			Item cutRootItem = (Item) cutRoot;
			if (this.checkSinglRow(cutRootItem)
					|| (cutRootItem).getElements() == null
					|| (cutRootItem).getElements().size() == 0) {
				return this.toXMLEx(type);
			} else {
				return this.toXMLEx(true, type);
			}
		}
	}

	protected StringBuffer nodeToXMLEx(Element node, int type) {
		StringBuffer sb = new StringBuffer();
		if (node instanceof Leaf) {
			String nodeValue = (String) node.getValue();
			if (null == nodeValue || 0 == nodeValue.length()) {
				switch (type) {
				case XMLDataObject.TOXML_NULL:
					sb.append("<" + node.getName() + ">");
					sb.append("null");
					sb.append("</" + node.getName() + ">");
					break;

				case XMLDataObject.TOXML_NOTNULL:
					sb.append("<" + node.getName() + ">");
					sb.append("</" + node.getName() + ">");
					break;

				case XMLDataObject.TOXML_SHORT:
					sb.append("<" + node.getName() + "/>");
					break;

				default:
					sb.append("<" + node.getName() + ">");
					sb.append(nodeValue);
					sb.append("</" + node.getName() + ">");
					break;
				}
			} else {
				sb.append("<" + node.getName() + ">");
				sb.append(nodeValue);
				sb.append("</" + node.getName() + ">");
			}
			return sb;
		}

		Item it = (Item) node;
		sb.append("<" + node.getName() + ">");
		for (int i = 0; i < it.elements.size(); i++) {
			sb.append(nodeToXMLEx((Element) it.elements.get(i), type));
		}
		sb.append("</" + node.getName() + ">");
		return sb;
	}

	public StringBuffer toXML() {
		return toXML(false);
	}

	public StringBuffer toXML(boolean bPrintRoot) {
		return toXML(bPrintRoot, TOXML_NOTNULL);
	}

	public StringBuffer toXML(boolean bPrintRoot, int type) {
		StringBuffer sb = new StringBuffer();
		if (bPrintRoot && this.cutRoot != null) {
			sb.append("<" + cutRoot.getName());
			String[] attrNames = null;
			if (cutRoot.getAttributes() != null) {
				attrNames = cutRoot.getAttributesName();
			}
			if (attrNames != null && attrNames.length > 0) {
				String attrName;
				String attrValue;
				for (int i = 0; i < attrNames.length; i++) {
					attrName = attrNames[i];
					if (attrName != null && !attrName.trim().equals("")) {
						attrValue = cutRoot.getAttributeValue(attrName);
						if (attrValue == null) {
							attrValue = "";
						}

						attrValue = XmlStringBuffer.filter(attrValue);
						sb.append(" ");
						sb.append(attrName);
						sb.append("=\"");
						sb.append(attrValue);
						sb.append("\"");
					}
				}
			}
			sb.append(">");
		}

		if (data != null) {
			for (int i = 0; i < data.size(); i++) {
				sb.append(nodeToXMLWithAttr(bPrintRoot, (Element) data.get(i),
						type, false).toString());
			}
		}

		if (bPrintRoot && this.cutRoot != null) {
			sb.append("</" + cutRoot.getName() + ">");
		}
		return sb;
	}

	protected StringBuffer nodeToXMLWithAttr(boolean bPrintRoot, Element node,
			int type, boolean forceRoot) {
		StringBuffer sb = new StringBuffer();
		if ((!forceRoot && node.equals(getCutRoot()))
				|| (bPrintRoot && node.equals(getCutRoot()))) {
			if (node instanceof Leaf) {
				Object nodeValue = node.getValue();
				if (null == nodeValue || 0 == ((String) nodeValue).length()) {
					switch (type) {
					case XMLDataObject.TOXML_NULL:
						sb.append("null");
						break;

					case XMLDataObject.TOXML_NOTNULL:
						break;

					case XMLDataObject.TOXML_SHORT:
						break;

					default:
						sb.append(nodeValue);
						break;
					}
				} else {
					nodeValue = XmlStringBuffer.filter((String) nodeValue);
					sb.append(nodeValue);
				}
				return sb;
			}
			Item it = (Item) node;
			for (int i = 0; i < it.elements.size(); i++) {
				sb.append(nodeToXMLWithAttr(bPrintRoot,
						(Element) it.elements.get(i), type, forceRoot));
			}
			return sb;
		}
		if (node instanceof Leaf) {
			sb.append("<" + node.getName());
			String[] attrNames = null;
			if (node.getAttributes() != null) {
				attrNames = node.getAttributesName();
			}
			if (attrNames != null && attrNames.length > 0) {
				String attrName;
				String attrValue;
				for (int i = 0; i < attrNames.length; i++) {
					attrName = attrNames[i];
					if (attrName != null && !attrName.trim().equals("")) {
						attrValue = node.getAttributeValue(attrName);
						if (attrValue == null) {
							attrValue = "";
						}

						attrValue = XmlStringBuffer.filter(attrValue);
						sb.append(" ");
						sb.append(attrName);
						sb.append("=\"");
						sb.append(attrValue);
						sb.append("\"");
					}
				}
			}

			String nodeValue = (String) node.getValue();
			if (null == nodeValue || 0 == nodeValue.length()) {
				switch (type) {
				case XMLDataObject.TOXML_NULL:
					sb.append(">");
					sb.append("null");
					sb.append("</" + node.getName() + ">");
					break;

				case XMLDataObject.TOXML_NOTNULL:
					sb.append(">");
					sb.append("</" + node.getName() + ">");
					break;

				case XMLDataObject.TOXML_SHORT:
					sb.append("/>");
					break;

				default:
					sb.append(">");
					sb.append(nodeValue);
					sb.append("</" + node.getName() + ">");
					break;
				}
			} else {
				sb.append(">");
				nodeValue = XmlStringBuffer.filter(nodeValue);
				sb.append(nodeValue);
				sb.append("</" + node.getName() + ">");
			}
			return sb;
		}

		Item it = (Item) node;
		sb.append("<" + node.getName());
		String[] attrNames = null;
		if (node.getAttributes() != null) {
			attrNames = node.getAttributesName();
		}
		if (attrNames != null && attrNames.length > 0) {
			String attrName;
			String attrValue;
			for (int i = 0; i < attrNames.length; i++) {
				attrName = attrNames[i];
				if (attrName != null && !attrName.trim().equals("")) {
					attrValue = node.getAttributeValue(attrName);
					if (attrValue == null) {
						attrValue = "";
					}

					attrValue = XmlStringBuffer.filter(attrValue);
					sb.append(" ");
					sb.append(attrName);
					sb.append("=\"");
					sb.append(attrValue);
					sb.append("\"");
				}
			}
		}
		sb.append(">");
		for (int i = 0; i < it.elements.size(); i++) {
			sb.append(nodeToXMLWithAttr(bPrintRoot,
					(Element) it.elements.get(i), type, forceRoot));
		}
		sb.append("</" + node.getName() + ">");
		return sb;
	}

	public String xpath(String xpath) {
		if (xpath == null) {
			return null;
		}
		xpath = xpath.replaceAll("/", ".");
		if (xpath.startsWith(".")) {
			xpath = xpath.substring(1);
		}
		boolean fullPathName = this.getBFullPathMode();
		this.setBFullPathMode(true);
		String value = this.getItemValue(xpath);
		this.setBFullPathMode(fullPathName);
		return value;
	}

	public int getChildCount() {
		if (cutRoot == null) {
			throw new RuntimeException("当前节点为空");
		}
		if (cutRoot instanceof Leaf) {
			return 0;
		} else {
			if (cutRoot instanceof Item) {
				return (((Item) cutRoot).elements).size();
			} else {
				throw new RuntimeException("未知的节点类型！");
			}
		}
	}

	public Element getChild(int row) {
		if (cutRoot == null || cutRoot instanceof Leaf) {
			throw new IllegalArgumentException("当前节点不存在子节点！");
		}
		if (row < 0 || row >= ((Item) cutRoot).elements.size()) {
			throw new IllegalArgumentException("子节点索引越界！");
		}
		return (Element) ((Item) cutRoot).elements.get(row);
	}

	public void load(String xml) {
	}

	public static String anyToString(Object obj) {
		String value = "";
		if (obj != null) {
			if ((obj instanceof Timestamp)) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				value = formatter.format(obj);
			} else {
				value = obj.toString();
			}
		}
		return value;
	}

}