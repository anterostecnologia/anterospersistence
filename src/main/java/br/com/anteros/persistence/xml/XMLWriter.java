package br.com.anteros.persistence.xml;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class XMLWriter {

	private Document document;
	private boolean omitXmlDeclaration;

	private XMLWriter(boolean omitXmlDeclaration) throws Exception {
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
		document = docBuilder.newDocument();
		this.omitXmlDeclaration = omitXmlDeclaration;
	}

	private XMLWriter(byte[] data, boolean omitXmlDeclaration) throws Exception {
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
		document = docBuilder.parse(new ByteArrayInputStream(data));
		this.omitXmlDeclaration = omitXmlDeclaration;
	}

	public Element addElement(String name, String value, XMLAttribute[] attributes, Element owner) {
		Element result = document.createElement(name);
		if ((value != null) && (!"".equals(value))) {
			Text elementValue = document.createTextNode(XMLUtil.processXMLString(value));
			result.appendChild(elementValue);
		}
		if (attributes != null) {
			for (XMLAttribute attribute : attributes) {
				result.setAttribute(attribute.getName(), XMLUtil.processXMLString(attribute.getValue()));
			}
		}
		if (owner != null)
			owner.appendChild(result);
		else
			document.appendChild(result);

		return result;
	}

	public Element addElement(String name, String value, XMLAttribute[] attributes) {
		return addElement(name, value, attributes, null);
	}

	public Element addElement(String name, String value) {
		return addElement(name, value, new XMLAttribute[] {}, null);
	}

	public Element addElement(String name, String value, Element owner) {
		return addElement(name, value, new XMLAttribute[] {}, owner);
	}

	public Element addElement(String name, Element owner) {
		return addElement(name, "", new XMLAttribute[] {}, owner);
	}

	public Element addElement(String name) {
		return addElement(name, "", new XMLAttribute[] {}, null);
	}

	public Element addElement(String name, XMLAttribute[] attributes) {
		return addElement(name, "", attributes, null);
	}

	public Element addElement(String name, XMLAttributeList attributes) {
		return addElement(name, "", attributes, null);
	}

	public Element addElement(String name, String value, XMLAttributeList attributes, Element owner) {
		Element result = document.createElement(name);
		if ((value != null) && (!"".equals(value))) {
			Text elementValue = document.createTextNode(XMLUtil.processXMLString(value));
			result.appendChild(elementValue);
		}
		if (attributes != null) {
			for (XMLAttribute attribute : attributes) {
				result.setAttribute(attribute.getName(), XMLUtil.processXMLString(attribute.getValue()));
			}
		}
		if (owner != null)
			owner.appendChild(result);
		else
			document.appendChild(result);

		return result;
	}

	public Element addElement(String name, XMLAttribute[] attributes, Element owner) {
		return addElement(name, "", attributes, owner);
	}
	
	public Element addElement(String name, XMLAttributeList attributes, Element owner){
		return addElement(name, "", attributes, owner);
	}

	public Element addElement(XMLElement element) {
		if (((element.getValue() == null) && (element.getAddNullValue())) || (element.getValue() != null)) {
			return addElement(element.getName(), element.getValue(), element.getAttributes(), element.getOwner());
		}
		return null;
	}

	public Element addElement(String name, String value, XMLAttribute[] attributes, Element owner, XMLElement[] childs) {
		Element result = addElement(name, value, attributes, owner);

		for (XMLElement child : childs) {
			if (((child.getValue() == null) && (child.getAddNullValue())) || (child.getValue() != null)) {
				child.setOwner(result);
				addElement(child);
			}
		}
		return result;
	}

	public Element addElement(XMLElement element, XMLElement[] childs) {
		Element result = addElement(element);

		for (XMLElement child : childs) {
			if (((child.getValue() == null) && (child.getAddNullValue())) || (child.getValue() != null)) {
				child.setOwner(result);
				addElement(child);
			}
		}
		return result;

	}

	public Element addElement(String name, XMLElement[] childs) {
		Element result = addElement(name);
		for (XMLElement child : childs) {
			if (((child.getValue() == null) && (child.getAddNullValue())) || (child.getValue() != null)) {
				child.setOwner(result);
				addElement(child);
			}
		}
		return result;

	}

	public Element addElement(XMLElement[] childs, Element owner) {
		for (XMLElement child : childs) {
			if (((child.getValue() == null && child.getAddNullValue())) || (child.getValue() != null)) {
				child.setOwner(owner);
				addElement(child);
			}
		}
		return owner;
	}

	public Element addElement(XMLElementList childs, Element owner) {
		for (XMLElement child : childs) {
			if (((child.getValue() == null && child.getAddNullValue())) || (child.getValue() != null)) {
				child.setOwner(owner);
				addElement(child);
			}
		}
		return owner;
	}

	public String getXml() throws Exception {
		return XMLUtil.toStringXML(document, omitXmlDeclaration);
	}

	public static XMLWriter newInstance(boolean omitXmlDeclaration) throws Exception {
		return new XMLWriter(omitXmlDeclaration);
	}

	public static XMLWriter newInstance(byte[] data, boolean omitXmlDeclaration) throws Exception {
		return new XMLWriter(data, omitXmlDeclaration);
	}

	public boolean isOmitXmlDeclaration() {
		return omitXmlDeclaration;
	}

	public void setOmitXmlDeclaration(boolean omitXmlDeclaration) {
		this.omitXmlDeclaration = omitXmlDeclaration;
	}

	public Element addElement(String name, XMLAttribute xmlAttribute, Element owner) {
		return addElement(name, new XMLAttribute[] { xmlAttribute }, owner);

	}
	
	public Element addElement(String name, XMLAttribute xmlAttribute) {
		return addElement(name, new XMLAttribute[] { xmlAttribute }, null);

	}

	public Element addElement(String name, XMLAttribute[] attributes, XMLElement[] childs, Element owner) {
		Element result = addElement(name, attributes, owner);
		addElement(childs, result);
		return result;

	}

	public void addElement(XMLElement child, Element owner) {
		addElement(new XMLElement[] { child }, owner);

	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public void appendXmlFragment(byte[] xmlFragment, Element owner) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document documentoTemp = builder.parse(new ByteArrayInputStream(xmlFragment));
		owner.appendChild(document.importNode(documentoTemp.getDocumentElement(), true));
	}

	public void merge(Element element, Element owner) {
		Node importNode = document.importNode(element, true);
		document.getElementsByTagName(owner.getTagName()).item(0).appendChild(importNode);
	}

	public void xmlMerge(byte[] xml, Element owner) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document documentoTemp = builder.parse(new ByteArrayInputStream(xml));
		owner.appendChild(document.importNode(documentoTemp.getDocumentElement(), true));
	}
}
