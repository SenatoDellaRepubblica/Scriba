package it.senato.areatesti.ebook.scriba.misc.xml;

import it.senato.areatesti.ebook.scriba.Context;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;


/**
 * Class to pretty print a String having XML inside (it parses XML)
 */
public class XmlUtils {

    /**
     * Pretty Prints an XML file
     *
     * @return the String content
     */
    public static String prettyFormatXml(Document document) throws TransformerException {
        // TODO: it doesn't work...
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.ENCODING, "us-ascii");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");

        DOMSource source = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        return writer.getBuffer().toString();

		/*
		OutputFormat format = new OutputFormat(document);
		format.setLineWidth(65);
		format.setIndenting(true);
		format.setIndent(2);
		Writer out = new StringWriter();
		XMLSerializer serializer = new XMLSerializer(out, format);
		try {
			serializer.serialize(document);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return out.toString();

		*/


    }

    /**
     * parses an XML document
     *
     * @return the XML Document
     */
    public static Document parseXmlFile(String contentToParse) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            // XXX: You MUST enable NamespaceAware otherwise the DOMSource doesn't work!!!!
            // http://download.oracle.com/javase/6/docs/api/javax/xml/transform/dom/DOMSource.html?
            dbf.setNamespaceAware(true);
            dbf.setValidating(false);
            try {
                // Sax
                dbf.setFeature("http://xml.org/sax/features/validation", false);

                // Xerces
                dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
                dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            } catch (ParserConfigurationException e) {
                Context.getInstance().getLogger().error(ExceptionUtils.getStackTrace(e));
            }

            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(contentToParse));

            return db.parse(is);
        } catch (ParserConfigurationException | IOException e) {
            Context.getInstance().getLogger()
                    .error(ExceptionUtils.getStackTrace(e));
        } catch (SAXException e) {

            Context.getInstance().getLogger()
                    .error(ExceptionUtils.getStackTrace(e));
            Context.getInstance().getLogger()
                    .error("XML parsed file with errors: " + contentToParse);

        }
        return null;
    }

    /**
     * Gets a node Value as a string
     *
     * @return the string node value
     */
    public static String getNodeValue(Node node) {
        StringBuilder buf = new StringBuilder();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node textChild = children.item(i);
            if (textChild.getNodeType() != Node.TEXT_NODE) {
                System.err.println("Mixed content! Skipping child element "
                        + textChild.getNodeName());
                continue;
            }
            buf.append(textChild.getNodeValue());
        }
        return buf.toString();
    }
}
