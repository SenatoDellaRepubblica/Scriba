
package it.senato.areatesti.ebook.scriba.misc.xslt;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.misc.Misc;
import it.senato.areatesti.ebook.scriba.misc.xml.XmlUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * General purpose class to manage XSLT
 */
public class XsltMgr {

    /**
     * Applies the XSL from the property file
     */
    public static String applyXslFromPropPath(String xmlContent,
                                              String xslPropPath) {
        Context.getInstance().getLogger().debug("------ XSL transformation starting (" + xslPropPath + ") ------ ");

        return applyXslToXmlContent(xmlContent, Misc.getContentProp(xslPropPath, Context.DEF_ENCODING));
    }

    /**
     * Apply an XSL to a xmlContent
     */
    private static String applyXslToXmlContent(String xmlContent, String xslContent) {
        return applyXslToXmlContent(xmlContent, xslContent, null);
    }

    /**
     * Apply an XSL to a xmlContent with a base path for the included Xslt
     *
     * @param basePathForXsltIncluded it is the base path for the Xslt that are included in the first one
     * @return the transformed content
     */
    public static String applyXslToXmlContent(String xmlContent, String xslContent, String basePathForXsltIncluded) {
        //Context.getInstance().getLogger().debug("------ XSL transformation starting: the content ------ ");

        // reads the XSL
        StringReader srXslContent = new StringReader(xslContent);

        // XXX: to Debug JAXP processor use "java -Djaxp.debug=1"

        TransformerFactory tFactory = null;
        if (Context.getInstance().xsltEngine.equals("net.sf.saxon.TransformerFactoryImpl")) {
            tFactory = new net.sf.saxon.TransformerFactoryImpl();
        } else if (Context.getInstance().xsltEngine.equals("org.apache.xalan.processor.TransformerFactoryImpl")) {
            tFactory = new org.apache.xalan.processor.TransformerFactoryImpl();
        } else {
            Context.getInstance().getLogger().error("XSLT engine initialization problems");
        }


        Transformer transformer = null;
        try {
            if (basePathForXsltIncluded == null) {
                transformer = tFactory.newTransformer(new StreamSource(srXslContent));
            } else {
                tFactory.setURIResolver(new XsltURIResolver(basePathForXsltIncluded));
                Templates tt = tFactory.newTemplates(new StreamSource(srXslContent));
                transformer = tt.newTransformer();
            }

        } catch (TransformerConfigurationException e) {
            Context.getInstance().getLogger().error(ExceptionUtils.getStackTrace(e));
        }

        StringWriter sw = new StringWriter();
        try {
            Document doc = XmlUtils.parseXmlFile(xmlContent);
            DOMSource domSource = new DOMSource(doc);
            transformer.transform(domSource, new StreamResult(sw));
        } catch (TransformerException e) {
            Context.getInstance().getLogger().error(ExceptionUtils.getStackTrace(e));
        }

        //Context.getInstance().getLogger().debug("------ XSL transformation ended ------ ");

        return sw.toString();
    }


}
