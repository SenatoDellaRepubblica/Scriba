package it.senato.areatesti.ebook.scriba.plugin.base;

import it.senato.areatesti.ebook.scriba.exception.TerminatedGenerationException;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentItem;
import it.senato.areatesti.ebook.scriba.scf.bean.MetadataList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface of plugins
 */
public interface IPlugin {
    /**
     * Elaborates the content
     *
     * @param content the List of content made
     */
    List<ContentItem> elaborateContent(ContentItem content, MetadataList metaList) throws IOException, TerminatedGenerationException;

    /**
     * Makes an HTML from PDF
     */
    ArrayList<ContentItem> makesHtmlFromPdf(ContentItem contentItemOfPdfRef, String fileNamePdf) throws IOException;

    /**
     * Converts the HTML if needed
     */
    String adjustConvertedHtml(String htmlContent);

    /**
     * Converts the byte array from its native encoding to the outputEncoding
     */
    String convertEncoding(byte[] byteContent, String outputEncoding) throws IOException;


}
