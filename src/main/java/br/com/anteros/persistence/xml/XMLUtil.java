package br.com.anteros.persistence.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class XMLUtil {

	public static byte[] toByteArray(Document document) {
		try {
			DOMSource source = new DOMSource(document);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Result result = new StreamResult(out);

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.transform(source, result);
			return out.toByteArray();
		} catch (Exception e) {
			return null;
		}
	}

	public static String toStringXML(Document document,
			boolean omitXmlDeclaration) throws Exception {

		OutputFormat format = new OutputFormat(document);
		format.setOmitXMLDeclaration(omitXmlDeclaration);
		StringWriter stringOut = new StringWriter();
		XMLSerializer serial = new XMLSerializer(stringOut, format);
		serial.serialize(document);

		return stringOut.toString();
	}

	public static String toStringXML(Document document) {
		DOMImplementationLS domImplementation = (DOMImplementationLS) document
				.getImplementation();
		LSSerializer lsSerializer = domImplementation.createLSSerializer();
		return lsSerializer.writeToString(document);
	}

	public static void validaSchema(File fileXML, File fileXSD)
			throws SAXException, FileNotFoundException, IOException {
		Schema schema = SchemaFactory.newInstance(
				XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(fileXSD);
		Validator validator = schema.newValidator();
		validator.validate(new SAXSource(new InputSource(new FileInputStream(
				fileXML))));
	}

	public static void validaSchemaPath(String pathXML, String pathXSD)
			throws FileNotFoundException, SAXException, IOException {
		validaSchema(new File(pathXML), new File(pathXSD));
	}

	public static void validaSchema(String xml, String pathXSD)
			throws SAXException, IOException {
		Schema schema = SchemaFactory.newInstance(
				XMLConstants.W3C_XML_SCHEMA_NS_URI)
				.newSchema(new File(pathXSD));
		Validator validator = schema.newValidator();
		validator.validate(new SAXSource(new InputSource(
				new ByteArrayInputStream(xml.getBytes()))));
	}

	public static void validaSchema(byte[] xml, String pathXSD)
			throws SAXException, IOException {
		System.out.println(new String(xml));
		Schema schema = SchemaFactory.newInstance(
				XMLConstants.W3C_XML_SCHEMA_NS_URI)
				.newSchema(new File(pathXSD));
		Validator validator = schema.newValidator();
		
		validator.validate(new SAXSource(new InputSource(
				new ByteArrayInputStream(xml))));
	}

	public static void validaSchema(Document document, String pathXSD)
			throws SAXException, IOException {
		try {
			Schema schema = SchemaFactory.newInstance(
					XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(
					new File(pathXSD));
			Validator validator = schema.newValidator();
			validator.validate(new SAXSource(new InputSource(
					new ByteArrayInputStream(toByteArray(document)))));
		} catch (SAXException e) {
			throw new SAXException(e);
		} catch (IOException e) {
			throw new IOException(e);
		} catch (Exception e) {

		}

	}

	public static Node getElement(Document documento, String tagName) {
		NodeList nodeList = documento.getElementsByTagName(tagName);
		Node node = null;
		for (int i = 0; i < nodeList.getLength(); i++) {
			node = nodeList.item(i);
			if (node.getNodeName().equals(tagName)) {
				return node;
			}
		}

		return null;
	}

	public static String processXMLString(String value) {
		if (value == null)
			return null;
		char[] chArray = value.toCharArray();
		String result = "";
		for (int i = 0; i < chArray.length; i++) {
			switch (chArray[i]) {
			case 192: // �
				result += "A";
				break;
			case 193: // �
				result += "A";
				break;
			case 224: // �
				result += "a";
				break;
			case 225: // �
				result += "a";
				break;
			case 226: // �
				result += "a";
				break;
			case 227: // �
				result += "a";
				break;
			case 195: // �
				result += "A";
				break;
			case 194: // �
				result += "A";
				break;
			case 196: // �
				result += "A";
				break;
			case 228: // �
				result += "a";
				break;
			case 202: // �
				result += "E";
				break;
			case 234: // �
				result += "e";
				break;
			case 233: // �
				result += "e";
				break;
			case 232: // �
				result += "e";
				break;
			case 201: // �
				result += "E";
				break;
			case 200: // �
				result += "E";
				break;
			case 235: // �
				result += "e";
				break;
			case 203: // �
				result += "E";
				break;
			case 243: // �
				result += "o";
				break;
			case 211: // �
				result += "O";
				break;
			case 210: // �
				result += "O";
				break;
			case 242: // �
				result += "o";
				break;
			case 245: // �
				result += "o";
				break;
			case 244: // �
				result += "o";
				break;
			case 212: // �
				result += "O";
				break;
			case 213: // �
				result += "O";
				break;
			case 214: // �
				result += "O";
				break;
			case 246: // �
				result += "o";
				break;
			case 237: // �
				result += "i";
				break;
			case 236: // �
				result += "i";
				break;
			case 205: // �
				result += "I";
				break;
			case 204: // �
				result += "I";
				break;
			case 207: // �
				result += "I";
				break;
			case 239: // �
				result += "i";
				break;
			case 238: // �
				result += "i";
				break;
			case 206: // �
				result += "I";
				break;
			case 250: // �
				result += "u";
				break;
			case 249: // �
				result += "u";
				break;
			case 251: // �
				result += "u";
				break;
			case 218: // �
				result += "U";
				break;
			case 217: // �
				result += "U";
				break;
			case 219: // �
				result += "U";
				break;
			case 220: // �
				result += "U";
				break;
			case 252: // �
				result += "u";
				break;
			case 231: // �
				result += "c";
				break;
			case 199: // �
				result += "C";
				break;
			case 186: // �
				result += " ";
				break;
			case 176: // �
				result += " ";
				break;
			case 170: // �
				result += " ";
				break;
			case 167: // �
				result += " ";
				break;

			default:
				result += chArray[i];
				break;
			}
		}

		return result.trim();
	}

	public static String processStringXML(String value) {
		String result = "";
		result = value.replace("&amp;", "&");
		result = result.replace("&lt;;", "<");
		result = result.replace("&gt;", ">");
		result = result.replace("&quot;", "\"");
		result = result.replace("&#39;", "\'");

		return result;
	}

	public static void toFileXML(String fileName, Document document,
			boolean omitXmlDeclaration) throws Exception {
		FileWriter fw = new FileWriter(new File(fileName));
		OutputFormat format = new OutputFormat(document);
		XMLSerializer serial = new XMLSerializer(fw, format);
		serial.serialize(document);
	}

	public static void toFileXML(String fileName, String documentStr,
			boolean omitXmlDeclaration) throws Exception {
		DocumentBuilder parser = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();

		ByteArrayInputStream is = new ByteArrayInputStream(
				documentStr.getBytes());
		Document document = parser.parse(is);
		toFileXML(fileName, document, omitXmlDeclaration);
	}

	public static void toFileXML(String fileName, String documentStr)
			throws Exception {
		FileWriter fw = new FileWriter(new File(fileName));
		fw.write(documentStr);
		fw.flush();
		fw.close();
	}

	public static void toFileXML(String fileName, byte[] document)
			throws Exception {
		FileOutputStream fos = new FileOutputStream(new File(fileName));
		fos.write(document);
		fos.flush();
		fos.close();
	}

	public static void toFileXML(String fileName, Document document)
			throws Exception {
		toFileXML(fileName, document, false);
	}

	public static void main(String[] args) {
		System.out.println((int) '�');

	}

}
