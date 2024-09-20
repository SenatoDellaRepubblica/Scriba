package it.senato.areatesti.ebook.scriba.api;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.EbookType;
import it.senato.areatesti.ebook.scriba.ScribaEBookMaker;
import it.senato.areatesti.ebook.scriba.misc.xml.JTidyManager;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.ByteArrayOutputStream;

/**
 * Scriba eBook Maker API
 */
public class ScribaEbookMakerAPI {

    private String apiInput;
    private String pdfColor = null;
    private boolean noExtractImgFromPdf;


    private EbookType ebookType;

    /**
     * Constructor
     */
    public ScribaEbookMakerAPI() {

        this.setEbookType(EbookType.EPUB);
        this.setPdfColor("");
        this.noExtractImgFromPdf = true;
    }

    /**
     * Constructor
     *
     */
    public ScribaEbookMakerAPI(EbookType ebookType, String pdfColor) {

        this.setEbookType(ebookType);
        this.setPdfColor(pdfColor);
        this.noExtractImgFromPdf = true;
    }

    /**
     * Set Ebook Type
     *
     */
    private void setEbookType(EbookType ebookType) {

        this.ebookType = ebookType;
    }

    /**
     * Set Pdf Color
     *
     */
    private void setPdfColor(String pdfColor) {

        this.pdfColor = pdfColor;
    }

    public void setnoExtractImgFromPdf(boolean noExtractImgFromPdf) {
        this.noExtractImgFromPdf = noExtractImgFromPdf;
    }


    /**
     * Set api string input
     *
     */
    public void setAPIInput(String apiInput) {

        this.apiInput = apiInput;
    }

    /**
     * Set path to tidy properties file
     *
     */
    public void setTidyLocation(String path) {

        JTidyManager.setTidyPropLoc(path);
    }

    /**
     * Set Make Ebook stream
     *
     * @return Ebook output stream
     */
    public ByteArrayOutputStream makeEBookAsStream() {

        ByteArrayOutputStream baos = null;

        try {

            ScribaEBookMaker maker = new ScribaEBookMaker(ebookType, apiInput, pdfColor, noExtractImgFromPdf);
            baos = maker.makeEBookAsStream();
        } catch (Exception e) {

            Context.getInstance().getLogger().error("-----------------------ScribaEbookMakerAPI-----------------------");
            Context.getInstance().getLogger().error(ExceptionUtils.getStackTrace(e));
            Context.getInstance().getLogger().error("-----------------------ScribaEbookMakerAPI-----------------------");
        }

        return baos;
    }
}
