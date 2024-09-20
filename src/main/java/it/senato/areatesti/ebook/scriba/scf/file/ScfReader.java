package it.senato.areatesti.ebook.scriba.scf.file;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.misc.Misc;
import it.senato.areatesti.ebook.scriba.misc.xml.XmlUtils;
import it.senato.areatesti.ebook.scriba.packaging.TemplateManager;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentItem;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentList;
import it.senato.areatesti.ebook.scriba.scf.bean.MetadataItem;
import it.senato.areatesti.ebook.scriba.scf.bean.MetadataList;
import it.senato.areatesti.ebook.scriba.scf.bean.base.IItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Reads the SCF content.xml and builds the data structure in memory to use
 */
public class ScfReader {
    // to be implemented: SCF file validation with XSD

    private static final int MAX_PLUGIN_ARGS_64 = 1024;
    private ContentList contentList;
    private MetadataList metadataList;
    private Document doc;

    /**
     * Reads the contents to insert in the book
     *
     * @param xmlContentsFile the full path of XML content.xml
     */
    public ScfReader(File xmlContentsFile)
            throws ParserConfigurationException, IOException,
            XPathExpressionException {
        ByteArrayInputStream bis = fillContentFileWithPlaceHolder(xmlContentsFile);
        init(bis);
    }

    /**
     * Reads the contents to insert in the book
     */
    public ScfReader(String xmlContentsString) throws XPathExpressionException, IOException, ParserConfigurationException {
        ByteArrayInputStream bis = fillContentFileWithPlaceHolder(xmlContentsString);
        init(bis);
    }


    /**
     * enumerates the META in the SCF, obtaining the value of the paramName
     * <p>
     * Example:
     * <metaitem eletype="meta" elename="meta" name="paramName"
     * content="paramValue" destination="plugin" />
     * <p>
     * "destination" is "plugin"
     */
    public String getMetaParameterValue(String paramName) {
        for (IItem item : metadataList.getIntList()) {
            MetadataItem mi = (MetadataItem) item;
            if (mi.getMetaName() != null && mi.getMetaName().equals(paramName))
                return mi.getMetaContent();
        }

        return null;
    }

    /**
     * Enumerates the DC (eletype) elements in the SCF, obtaining the value of the XML element "metaitem"
     * <p>
     * Example:
     * <metaitem eletype="dc" elename="creator">Senato della Repubblica</metaitem>
     */
    public String getEleValue(String eleName) {
        return getEleValue(eleName, null);
    }

    /**
     * Enumerates the DC (eletype) elements in the SCF, obtaining the value of the XML element "metaitem"
     * <p>
     * Example:
     * <metaitem eletype="dc" elename="creator" role="edt">Senato della Repubblica</metaitem>
     */
    public String getEleValue(String eleName, String roleVal) {

        for (IItem item : metadataList.getIntList()) {
            MetadataItem mi = (MetadataItem) item;
            if (mi.getElemName() != null && mi.getElemName().equals(eleName)) {
                if (roleVal == null || (mi.getRole() != null && mi.getRole().equals(roleVal))) {
                    return mi.getElemVal();
                }
            }
        }

        return null;
    }

    /**
     * Initializator
     */
    private void init(ByteArrayInputStream bis)
            throws IOException, XPathExpressionException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        try {

            doc = db.parse(bis);
            doc.getDocumentElement().normalize();
            contentList = new ContentList();
            metadataList = new MetadataList();

            // Reads the content
            readContents();
            readMetadataItems();

        } catch (SAXException e) {
            bis.reset();
            Context.getInstance().getLogger().error("SAX parsing problems. SCF to parse, follows: " + IOUtils.toString(bis, Context.DEF_ENCODING));
            Context.getInstance().getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }


    /**
     * Fills the content file with various placeholders
     */
    private ByteArrayInputStream fillContentFileWithPlaceHolder(File file)
            throws IOException {
        //String content = Misc.getFileContentAsString(new FileInputStream(file), Context.DEF_ENCODING);
        String content = FileUtils.readFileToString(file, Context.DEF_ENCODING);
        return fillContentFileWithPlaceHolder(content);
    }

    /**
     * Fills the content with place holder value
     */
    private ByteArrayInputStream fillContentFileWithPlaceHolder(String content) throws UnsupportedEncodingException {
        content = content.replace(Context.DC_DATE_PLACEHOLDER, Misc.getNowTimestamp());
        content = content.replace(Context.PRETTY_DATE_PLACEHOLDER, Misc.getNowTimestampLong());

        return new ByteArrayInputStream(content.getBytes(Context.DEF_ENCODING));
    }


    /**
     * Gets the content list
     *
     * @return the contentList
     */
    public ContentList getContentList() {
        return contentList;
    }

    /**
     * Gets the Metadata List
     *
     * @return the metadataList
     */
    public MetadataList getMetadataList() {
        return metadataList;
    }

    /**
     * Reads the Contents from an XML file
     */
    private void readContents() throws XPathExpressionException {
        // I read the "contents" attributes value
        readContentsAttr();
        // I read the various content
        readContentItems();
    }

    /**
     * Reads the metadata element
     */
    private void readMetadataItems() throws XPathExpressionException {
        XPath xpath;
        NamedNodeMap attr;
        xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xpath.evaluate(
                "/book/metadata/metaitem", doc, XPathConstants.NODESET);
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node cNode = nodeList.item(i);
                attr = cNode.getAttributes();

                if (attr.getNamedItem(ScfTokens.METAITEM_ELETYPE) != null)
                    if (attr.getNamedItem(ScfTokens.METAITEM_ELETYPE).getNodeValue().equals(ScfTokens.METAITEM_DC)) {
                        MetadataItem m = new MetadataItem(attr.getNamedItem(
                                ScfTokens.METAITEM_ELENAME).getNodeValue(), attr.getNamedItem(
                                ScfTokens.METAITEM_ELETYPE).getNodeValue(),
                                XmlUtils.getNodeValue(cNode),
                                (attr.getNamedItem(ScfTokens.METAITEM_ID) != null) ? attr
                                        .getNamedItem(ScfTokens.METAITEM_ID).getNodeValue()
                                        : null,
                                (attr.getNamedItem(ScfTokens.METAITEM_ROLE) != null) ? attr
                                        .getNamedItem(ScfTokens.METAITEM_ROLE).getNodeValue()
                                        : null
                                , null, null, null);
                        metadataList.addContent(m);
                    } else if (attr.getNamedItem(ScfTokens.METAITEM_ELETYPE).getNodeValue().equals(ScfTokens.METAITEM_META)) {
                        MetadataItem m = new MetadataItem(attr.getNamedItem(ScfTokens.METAITEM_ELENAME).getNodeValue(),
                                attr.getNamedItem(ScfTokens.METAITEM_ELETYPE).getNodeValue(), null, null, null,
                                attr.getNamedItem(ScfTokens.METAITEM_NAME).getNodeValue(),
                                attr.getNamedItem(ScfTokens.METAITEM_CONTENT).getNodeValue(),
                                attr.getNamedItem(ScfTokens.METAITEM_DESTINATION).getNodeValue());

                        metadataList.addContent(m);
                    }

                if (attr.getNamedItem(ScfTokens.METAITEM_ELENAME).getNodeValue().equals(ScfTokens.DC_LANGUAGE))
                    TemplateManager.setLang(cNode.getTextContent());
            }
        }

    }

    /**
     * Reads the contents items
     */
    private void readContentItems() throws XPathExpressionException {
        XPath xpath;
        NamedNodeMap attr;
        xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xpath.evaluate("/book/contents/content",
                doc, XPathConstants.NODESET);
        if (nodeList != null) {

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node cNode = nodeList.item(i);
                attr = cNode.getAttributes();

                ContentItem content = new ContentItem(
                        attr.getNamedItem(ScfTokens.CONTENT_PACKAGE_PATH).getNodeValue(),
                        (attr.getNamedItem(ScfTokens.CONTENT_PACKAGE_FILE) != null) ? attr
                                .getNamedItem(ScfTokens.CONTENT_PACKAGE_FILE)
                                .getNodeValue() : null,
                        attr.getNamedItem(ScfTokens.CONTENT_PACKAGE_ID).getNodeValue(),
                        (attr.getNamedItem(ScfTokens.CONTENT_TOC_NAME) != null) ? attr
                                .getNamedItem(ScfTokens.CONTENT_TOC_NAME).getNodeValue() : null,
                        (attr.getNamedItem(ScfTokens.CONTENT_CONTENT_URL) != null) ? attr
                                .getNamedItem(ScfTokens.CONTENT_CONTENT_URL).getNodeValue() : null,

                        (attr.getNamedItem(ScfTokens.CONTENT_CONTENT_ALT_URL) != null) ? attr
                                .getNamedItem(ScfTokens.CONTENT_CONTENT_ALT_URL).getNodeValue() : null,

                        (attr.getNamedItem(ScfTokens.CONTENT_CONTENT_MEDIA_TYPE) != null) ? attr
                                .getNamedItem(ScfTokens.CONTENT_CONTENT_MEDIA_TYPE)
                                .getNodeValue() : null,
                        (attr.getNamedItem(ScfTokens.EBOOK_TYPES) != null) ? attr
                                .getNamedItem(ScfTokens.EBOOK_TYPES)
                                .getNodeValue() : null,
                        (attr.getNamedItem(ScfTokens.CONTENT_PLUGIN) != null) ? attr
                                .getNamedItem(ScfTokens.CONTENT_PLUGIN)
                                .getNodeValue() : null,
                        (attr.getNamedItem(ScfTokens.CONTENT_ENCODING) != null) ? attr
                                .getNamedItem(ScfTokens.CONTENT_ENCODING)
                                .getNodeValue() : null,
                        (attr.getNamedItem(ScfTokens.METAITEM_NAME_COVER) != null) && Boolean
                                .parseBoolean(attr.getNamedItem(ScfTokens.METAITEM_NAME_COVER)
                                        .getNodeValue()),
                        (attr.getNamedItem(ScfTokens.CONTENT_IS_IN_SPINE) != null) && Boolean
                                .parseBoolean(attr.getNamedItem(ScfTokens.CONTENT_IS_IN_SPINE)
                                        .getNodeValue()),
                        (attr.getNamedItem(ScfTokens.CONTENT_IS_NEEDED_TIDY) != null) && Boolean
                                .parseBoolean(attr.getNamedItem(ScfTokens.CONTENT_IS_NEEDED_TIDY)
                                        .getNodeValue()),
                        (attr.getNamedItem(ScfTokens.CONTENT_IS_NEEDED_XSL) != null) && Boolean
                                .parseBoolean(attr.getNamedItem(ScfTokens.CONTENT_IS_NEEDED_XSL)
                                        .getNodeValue()),
                        (attr.getNamedItem(ScfTokens.CONTENT_PDF_TO_HTML) != null) && Boolean
                                .parseBoolean(attr.getNamedItem(ScfTokens.CONTENT_PDF_TO_HTML)
                                        .getNodeValue()));


                // Read the optional and named plugin args
                readPluginArgs(attr, content);

                //------------------------------------------------------------//
                // XXX: SAM PATCH for html content in CONTENT tag
                //------------------------------------------------------------//

                try {

                    if (attr.getNamedItem(ScfTokens.CONTENT_CONTENT_URL) == null &&
                            content.getContentMediaType().equals(Context.XHTML_MIMETYPE))
                        content.setStringContent(cNode.getTextContent());
                } catch (Exception e) {

                    Context.getInstance().getLogger().error(ExceptionUtils.getStackTrace(e));
                }

                //------------------------------------------------------------//

                //------------------------------------------------------------//
                // XXX: RB Patch to reference an image
                //		(small refactoring for the name which is relative to the SECTION image)
                //------------------------------------------------------------//

                if (content.getPackageId().equals("section-logo-image") &&
                        content.getContentMediaType().equals(Context.IMAGE_MIMETYPE)) {
                    Context.getInstance().sectionlogoFileName = content.getPackageFile();
                }

                contentList.addContent(content);
            }


        }
    }

    /**
     * Reads the plugin Arguments
     */
    private void readPluginArgs(NamedNodeMap attr, ContentItem content) {
        for (int j = 0; j < MAX_PLUGIN_ARGS_64; j++) {
            for (int m = 0; m < attr.getLength(); m++) {
                if (attr.item(m) != null) {
                    String nodeName = attr.item(m).getNodeName();
                    if (StringUtils.startsWith(nodeName, ScfTokens.CONTENT_PLUGIN_ARGS_PREFIX)) {
                        String nodeValue = attr.item(m).getNodeValue();
                        content.getPluginArgs().put(nodeName, nodeValue);
                    }
                }
            }
        }
    }

    /**
     * Reads contents attributes
     */
    private void readContentsAttr() throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        Node node = (Node) xpath.evaluate("/book/contents", doc,
                XPathConstants.NODE);
        NamedNodeMap attr = node.getAttributes();
        String tocId = attr.getNamedItem(ScfTokens.CONTENTS_TOC_ID).getNodeValue();
        if (tocId != null)
            contentList.setTocId(tocId);
    }

}
