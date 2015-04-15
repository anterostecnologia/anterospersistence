package br.com.anteros.persistence.xml;

import java.util.ArrayList;

public class XMLAttributeList extends ArrayList<XMLAttribute> {

	private static final long serialVersionUID = 1L;

	public XMLAttributeList addAttribute(String name, String value) {
		this.add(new XMLAttribute(name, value));
		return this;
	}

}
