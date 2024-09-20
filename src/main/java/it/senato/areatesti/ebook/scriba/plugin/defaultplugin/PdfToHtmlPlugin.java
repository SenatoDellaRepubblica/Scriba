package it.senato.areatesti.ebook.scriba.plugin.defaultplugin;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.misc.pdf.PdfEncryption;
import it.senato.areatesti.ebook.scriba.misc.pdf.PdfTexter;
import it.senato.areatesti.ebook.scriba.plugin.base.AbstractPlugin;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentItem;
import it.senato.areatesti.ebook.scriba.scf.bean.MetadataList;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.exceptions.CryptographyException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic plugin to convert PDF file into pure text
 */
public class PdfToHtmlPlugin extends AbstractPlugin {

    public PdfToHtmlPlugin(Object[] initArgs) {
        super(initArgs);
    }

    /**
     * The encoding is determined at run-time and not set before
     */
    @Override
    public List<ContentItem> elaborateContent(ContentItem content,
                                              MetadataList metadataList) {
        ArrayList<ContentItem> ciList = new ArrayList<>();
        ciList.add(content);
        return ciList;
    }

    @Override
    public ArrayList<ContentItem> makesHtmlFromPdf(
            ContentItem contentItemOfPdfRef, String fileNamePdf)
            throws IOException {
        // ATTENTION: the encoding of the PDF text is Windows 1252 so I must set explicitly the encoding Windows 1252
        // XXX: it should be passed by the method
        String textPdfEncoding = Context.WINDOWS_CP1252_LATIN1;

        ArrayList<ContentItem> ciList = new ArrayList<>();

        try {
            PdfTexter pdfTexter;
            pdfTexter = new PdfTexter(PdfEncryption.decryptDocument(null), textPdfEncoding, fileNamePdf);
            ByteArrayOutputStream bo = pdfTexter.convertToHtml();
            String text = bo.toString(textPdfEncoding);

            String htmlContent = adjustConvertedHtml(text);
            String packageFile = FilenameUtils.removeExtension(contentItemOfPdfRef.getPackageFile()) + ".out" + Context.XHTML_EXT;

            // inherits attributes from the original ContentItem
            ContentItem ciText = new ContentItem(
                    contentItemOfPdfRef.getPackagePath(), packageFile, Context.ID_OPF_PREFIX + packageFile,
                    null, null, null, Context.XHTML_MIMETYPE, null, null, null,
                    contentItemOfPdfRef.isCover(), contentItemOfPdfRef.isInSpine(),
                    contentItemOfPdfRef.isNeededTidy(),
                    contentItemOfPdfRef.isNeededXsl(), false);


            ciText.setStringContent(htmlContent);
            ciList.add(ciText);
        } catch (CryptographyException e) {
            Context.getInstance().getLogger()
                    .error(ExceptionUtils.getStackTrace(e));
        }


        return ciList;

    }

    @Override
    public String adjustConvertedHtml(String htmlContent) {
        //htmlContent = htmlTransWinUni(htmlContent);
        return htmlContent;
    }

    @Override
    public String convertEncoding(byte[] byteContent, String outputEncoding) {
        throw new RuntimeException("Not implemented!");
    }

}
