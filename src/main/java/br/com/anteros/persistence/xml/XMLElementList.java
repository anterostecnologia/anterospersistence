package br.com.anteros.persistence.xml;

import java.util.ArrayList;

public class XMLElementList extends ArrayList<XMLElement> {

	private static final long serialVersionUID = 1L;

	public XMLElementList addXMLElement(String name, String value) {
		this.add(new XMLElement(name, value));
		return this;
	}

	public XMLElementList addXMLElement(String name, String value, boolean addNullValue) {
		this.add(new XMLElement(name, value, addNullValue));
		return this;
	}

}
