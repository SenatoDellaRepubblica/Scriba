package it.senato.areatesti.ebook.scriba.plugin.defaultplugin;


import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.misc.url.UrlDownloadResult;
import it.senato.areatesti.ebook.scriba.misc.url.WebGet;
import it.senato.areatesti.ebook.scriba.plugin.base.AbstractPlugin;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentItem;
import it.senato.areatesti.ebook.scriba.scf.bean.MetadataList;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * The Bungeni Plugin
 */
public class BungeniXml2XhtmlPlugin extends AbstractPlugin {
    public BungeniXml2XhtmlPlugin(Object[] initArgs) {
        super(initArgs);
    }

    @Override
    public List<ContentItem> elaborateContent(ContentItem content,
                                              MetadataList metadataList) throws IOException {
        ArrayList<ContentItem> ciList = new ArrayList<>();

        try {

            byte[] bArray = null;
            if (content.getContentUrl() != null) {
                WebGet webGet = new WebGet(content.getContentUrl(), content.getContentMediaType(), "UTF-8");
                UrlDownloadResult udr = webGet.getUrlContent();
                if (udr != null) {
                    bArray = udr.getMainContent();
                } else {
                    Context.getInstance().getLogger().error("[BungeniXml2XhtmlPlugin] URL path is probably bad written!");
                }
            } else {
                bArray = content.getByteContent();
            }


            //content.setContentSrcEncoding(webGet.getSourceEncoding());

            StringReader reader = new StringReader(new String(bArray, StandardCharsets.UTF_8));
            File xsltFile = new File("translate.xsl");

            // JAXP reads data using the Source interface
            Source xmlSource = new StreamSource(reader);
            Source xsltSource = new StreamSource(xsltFile);

            // the factory pattern supports different XSLT processors
            TransformerFactory transFact = TransformerFactory.newInstance();
            Transformer trans = transFact.newTransformer(xsltSource);
            StreamResult resultOutput = new StreamResult(new StringWriter());
            trans.transform(xmlSource, resultOutput);

            String xmlString = resultOutput.getWriter().toString();
            content.setStringContent(xmlString);

            content.setNeededTidy(false);
            content.setNeededXsl(false);
        } catch (TransformerException transConfigException) {

            Context.getInstance().getLogger().error(ExceptionUtils.getStackTrace(transConfigException));
        }

        ciList.add(content);
        return ciList;
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
