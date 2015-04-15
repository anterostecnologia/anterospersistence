package br.com.anteros.persistence.xml;

import org.w3c.dom.Element;

public class XMLElement {

	private String name;
	private XMLAttribute[] attributes;
	private String value;
	private Element owner;
	private Boolean addNullValue=true; 
	
	public XMLElement(String name, String value, XMLAttribute[] attributes, Element owner, boolean addNullValue) {
		this.name = name;
		this.attributes = attributes;
		this.owner = owner;
		this.value = value;
		this.addNullValue = addNullValue;
	}
	
	public XMLElement(String name, String value, XMLAttribute[] attributes,boolean addNullValue) {
		this.name = name;
		this.attributes = attributes;
		this.value = value;
		this.addNullValue = addNullValue;
	}
	
	public XMLElement(String name, String value, boolean addNullValue) {
		this.name = name;
		this.value = value;
		this.addNullValue = addNullValue;
	}
	
	public XMLElement(String name, String value, XMLAttribute[] attributes, Element owner) {
		this.name = name;
		this.attributes = attributes;
		this.owner = owner;
		this.value = value;
	}
	
	public XMLElement(String name, String value, XMLAttribute[] attributes) {
		this.name = name;
		this.attributes = attributes;
		this.value = value;
	}
	
	public XMLElement(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public XMLAttribute[] getAttributes() {
		return attributes;
	}

	public void setAttributes(XMLAttribute[] attributes) {
		this.attributes = attributes;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Element getOwner() {
		return owner;
	}

	public void setOwner(Element owner) {
		this.owner = owner;
	}

	public Boolean getAddNullValue() {
		return addNullValue;
	}

	public void setAddNullValue(Boolean addNullValue) {
		this.addNullValue = addNullValue;
	}
	
	public static XMLElementList list() {
		return new XMLElementList();
	}	
}
