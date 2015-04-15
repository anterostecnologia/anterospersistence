package br.com.anteros.persistence.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLReader {

	public static String readElementFromXML(String xml, String expr) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		Document doc = parser.parse(is);
		XPath xpath = XPathFactory.newInstance().newXPath();
		String value = (String) xpath.evaluate(expr, doc, XPathConstants.STRING);
		return value;
	}
	
	public static NodeList readNodesFromNode(Node node, String expr) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		return readNodesFromXML(nodeToString(node), expr);
	}

	public static NodeList readNodesFromXML(String xml, String expr) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		Document doc = parser.parse(is);
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList value = (NodeList) xpath.evaluate(expr, doc, XPathConstants.NODESET);
		return value;
	}

	public static String readElementFromNode(Node node, String exp) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		return readElementFromXML(nodeToString(node), exp);
	}
	
	public static String readAttributeFromNode(Node node, String expr, String attrib) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		return readAttributeFromXML(nodeToString(node), expr, attrib);
	}

	public static String nodeToString(Node node) {
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException te) {
			System.out.println("nodeToString Transformer Exception");
		}
		return sw.toString();
	}

	public static int readElementsLenghtFromXML(String xml, String expr) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		Document doc = parser.parse(is);
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList) xpath.evaluate(expr, doc, XPathConstants.NODESET);
		return nodeList.getLength();
	}

	public static String readAttributeFromXML(String xml, String expr, String attrib) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		Document doc = parser.parse(is);
		XPath xpath = XPathFactory.newInstance().newXPath();
		Node node = (Node) xpath.evaluate(expr, doc, XPathConstants.NODE);
		NamedNodeMap attributes = node.getAttributes();
		String value = "";
		for (int i = 0; i < attributes.getLength(); i++) {
			if (Node.ATTRIBUTE_NODE == attributes.item(i).getNodeType() && attrib.equals(attributes.item(i).getNodeName())) {
				value = attributes.item(i).getTextContent();
			}
		}
		return value;
	}

	public static String[] readAttributesFromXML(String xml, String expr, String attrib) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		Document doc = parser.parse(is);
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodes = (NodeList) xpath.evaluate(expr, doc, XPathConstants.NODESET);
		String[] values = new String[nodes.getLength()];
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			NamedNodeMap attributes = node.getAttributes();
			for (int j = 0; j < attributes.getLength(); j++) {
				if (Node.ATTRIBUTE_NODE == attributes.item(j).getNodeType() && attrib.equals(attributes.item(j).getNodeName())) {
					values[i] = attributes.item(j).getTextContent();
				}
			}
		}
		return values;
	}

	public static boolean existsTagOnXML(String xml, String expr) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		Document doc = parser.parse(is);
		XPath xpath = XPathFactory.newInstance().newXPath();
		String value = (String) xpath.evaluate(expr, doc, XPathConstants.STRING);
		return value != "";
	}

}
