package sharpen.core;

import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.w3c.dom.*;
import org.w3c.dom.ls.*;

public class XmlDocumentationOverlay implements DocumentationOverlay {

	private Document _document;
	private LSSerializer _serializer;
	private XPath _xpath;

	public XmlDocumentationOverlay(String uri) {
		_document = loadXML(uri);
	}
	
	public String forType(String fullName) {
		return forXPath("/doc/type[@name='" + fullName + "']/doc");
	}
	
	public String forMember(String fullTypeName, String signature) {
		return forXPath("/doc/type[@name='" + fullTypeName + "']/member[@name='" + signature + "']/doc");
	}

	private String forXPath(String xpath) {
		Element found = selectElement(xpath);
		if (null == found) return null;
		return serializeContent(found);
	}

	private String serializeContent(Element e) {
		return stripFirstAndLastLines(serializer().writeToString(e));
	}

	private String stripFirstAndLastLines(String s) {
		int firstLineFeed = s.indexOf("\n");
		int lastLineFeed = s.lastIndexOf("\n");
		return s.substring(firstLineFeed+1, lastLineFeed);
	}

	private LSSerializer serializer() {
		if (null != _serializer) return _serializer;
		return _serializer = newSerializer();
	}

	private LSSerializer newSerializer() {
		LSSerializer serializer = ((DOMImplementationLS)_document.getImplementation()).createLSSerializer();
		serializer.getDomConfig().setParameter("xml-declaration", Boolean.FALSE);
		serializer.getDomConfig().setParameter("well-formed", Boolean.FALSE);
		return serializer;
	}

	protected Element selectElement(String xpath) {
		try {
			return (Element)xpath().evaluate(xpath, _document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	private XPath xpath() {
		if (null != _xpath) return _xpath;
		return _xpath = newXPath();
	}

	private XPath newXPath() {
		return XPathFactory.newInstance().newXPath();
	}
	
	private static Document loadXML(String uri) {
		try {
			return builderFactory().newDocumentBuilder().parse(uri);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static DocumentBuilderFactory builderFactory() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);
		return factory;
	}

}
