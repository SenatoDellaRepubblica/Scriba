package it.senato.areatesti.ebook.scriba.plugin.defaultplugin;


import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.misc.xslt.XsltMgr;
import it.senato.areatesti.ebook.scriba.packaging.PackageMaker;
import it.senato.areatesti.ebook.scriba.plugin.senato.base.AbstractTestiSenatoPlugin;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentItem;
import it.senato.areatesti.ebook.scriba.scf.bean.MetadataList;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Plugin to apply an XSLT to an input XML file and obtain a complaint XHTML for the EPUB
 */
public class Xslt4XhtmlPlugin extends AbstractTestiSenatoPlugin {

    private static final String XSL_FULL_PATH = "xslFullPath";

    protected Xslt4XhtmlPlugin(Object[] initArgs) {
        super(initArgs);
    }

    @Override
    public List<ContentItem> elaborateContent(ContentItem content,
                                              MetadataList metadataList) throws IOException {
        this.metadataList = metadataList;
        this.currentContent = content;

        byte[] bContent;

        // Takes the URL content (XML)
        bContent = PackageMaker.downloadContentStrict(content, null, Context.UTF8_ENCODING);
        if (bContent == null) {
            content = setErrorPage(content);
        } else {
            // Sets the original encoding: it depends on the document encoding and we must know this one!
            content.setByteContent(bContent);

            // To disable Internal Tidy and Xslt
            content.setNeededTidy(false);
            content.setNeededXsl(false);

            String rootXslFullPath = readTheXslPath();
            String rootXslContent;
            try {

                //System.out.println(content.getStringContent().toString());

                rootXslContent = readUrlContent(rootXslFullPath);

                // Modify the XSLT if needed
                rootXslContent = modifyXslt(rootXslContent);

                // Modifies the data.xml, if needed
                String contentMod = modifyInputData(content.getStringContent());

                // Apply the XSL
                String xhtml = XsltMgr.applyXslToXmlContent(contentMod, rootXslContent, rootXslFullPath);

                // Only for the PDF ebook
                xhtml = this.updateHtmlForDivProblemsInIText(xhtml);

                // Set the final content
                content.setStringContent(xhtml);

            } catch (URISyntaxException e) {
                Context.getInstance().getLogger().error("File: " + rootXslFullPath, e);
                content = null;
            }

        }

        ArrayList<ContentItem> ciList = new ArrayList<>();

        ciList.add(content);

        return ciList;
    }


    /**
     * Reads the XSL Path
     */
    protected String readTheXslPath() {
        return metadataList.searchMetaContent(XSL_FULL_PATH);
    }

    /**
     * Modifies the XML data input to the XSLT
     */
    protected String modifyInputData(String data) {
        return data;
    }

    /**
     * Modify the XSLT file
     */
    protected String modifyXslt(String xsl) {
        return xsl;
    }

    @Override
    public ArrayList<ContentItem> makesHtmlFromPdf(
            ContentItem contentItemOfPdfRef, String fileNamePdf) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public String adjustConvertedHtml(String htmlContent) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public String convertEncoding(byte[] byteContent, String outputEncoding) {
        throw new RuntimeException("Not implemented!");
    }

}
