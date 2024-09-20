package it.senato.areatesti.ebook.scriba;

import it.senato.areatesti.ebook.scriba.misc.Misc;
import it.senato.areatesti.ebook.scriba.packaging.PackageMaker;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Scriba eBook Maker
 */
public class ScribaEBookMaker {
    private final EbookType ebookType;
    private final File scfXmlFile;
    private final String scfXmlContent;
    private String pdfThumbColor;
    private final boolean noExtractImgFromPdf;


    /**
     * Constructor
     */
    public ScribaEBookMaker(EbookType ebookType, File scfXmlFile, String pdfThumbColor, boolean noExtractImgFromPdf) {
        this(ebookType, scfXmlFile, null, pdfThumbColor, noExtractImgFromPdf);
    }

    /**
     * Constructor
     */
    public ScribaEBookMaker(EbookType ebookType, String scfXmlContent, String pdfThumbColor, boolean noExtractImgFromPdf) {
        this(ebookType, null, scfXmlContent, pdfThumbColor, noExtractImgFromPdf);
    }

    /**
     * Constructor
     *
     * @param pdfThumbColor gray or rgb
     */
    private ScribaEBookMaker(EbookType ebookType, File scfXmlFile, String scfXmlContent, String pdfThumbColor, boolean noExtractImgFromPdf) {
        this.ebookType = ebookType;
        this.scfXmlFile = scfXmlFile;
        this.scfXmlContent = scfXmlContent;
        this.pdfThumbColor = pdfThumbColor;
        this.noExtractImgFromPdf = noExtractImgFromPdf;
        if (this.pdfThumbColor == null || (!this.pdfThumbColor.equals("gray") && !this.pdfThumbColor.equals("rgb"))) {
            Context.getInstance().getLogger().info("Color option must be \"gray\" or \"rgb\": i'm using \"gray\"");
            this.pdfThumbColor = "gray";
        }
    }

    /**
     * Makes the books as a byte stream
     *
     * @return the book as a byte array stream
     */
    public ByteArrayOutputStream makeEBookAsStream() {
        ByteArrayOutputStream bos = null;
        try {
            Context.getInstance().getTempGarbageCleaner().cleanTempFromPrevFiles();

            PackageMaker opsMaker = new PackageMaker(scfXmlContent, pdfThumbColor, noExtractImgFromPdf);
            bos = new ByteArrayOutputStream();
            boolean ret = opsMaker.make(bos, this.ebookType);
            if (!ret) {
                Context.getInstance().getLogger()
                        .error("Errors in the epub creations");
                bos = null;
            }

            Context.getInstance().getTempGarbageCleaner().cleanGarbageCollector();
            return bos;

        } catch (XPathExpressionException | ParserConfigurationException | IOException e) {
            Context.getInstance().getLogger()
                    .error(ExceptionUtils.getStackTrace(e));
        }

        return bos;
    }

    /**
     * Makes the book as a File
     */
    boolean makeEBookAsFile(String fileNameFullPath) {
        try {
            Context.getInstance().getTempGarbageCleaner().cleanTempFromPrevFiles();

            PackageMaker opsMaker = new PackageMaker(scfXmlFile, pdfThumbColor, noExtractImgFromPdf);

            boolean ret = true;
            if (this.ebookType == EbookType.ALL) {
                Context.getInstance().getLogger().info("************* START: EBOOK BATCH CREATION ************* ");

                EbookType[] types = {EbookType.EPUB, EbookType.ZIP, EbookType.PDF};

                String ext;
                for (EbookType t : types) {
                    ext = Misc.getEbookExtension(t);
                    String filename = fileNameFullPath + ext;
                    ret &= opsMaker.make(filename, t);
                }

                Context.getInstance().getLogger().info("************* END: EBOOK BATCH CREATION ************* ");
            } else {
                ret &= opsMaker.make(fileNameFullPath, this.ebookType);
            }

            Context.getInstance().getTempGarbageCleaner().cleanGarbageCollector();
            return ret;

        } catch (XPathExpressionException | ParserConfigurationException | IOException e) {
            Context.getInstance().getLogger()
                    .error(ExceptionUtils.getStackTrace(e));
            return false;
        }

    }

}
