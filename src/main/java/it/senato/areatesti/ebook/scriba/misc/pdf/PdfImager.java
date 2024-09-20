package it.senato.areatesti.ebook.scriba.misc.pdf;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.misc.image.Imager;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFImageWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class to manage the PDF and extract images from it
 */
public class PdfImager {
    /**
     * Creates thumbs list from a PDF file: until the end of the file is reached
     *
     * @param color gray or rgb
     * @return the path to the thumb image
     */
    public static ArrayList<String> createThumbList(String pdfFileName,
                                                    int startPage, String color) {
        // XXX: JAI ImageIO patch? http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4821108
        ImageIO.setUseCache(true);

        ArrayList<String> arListOrigThumb = createThumbList(pdfFileName, startPage, 0, color);
        if (arListOrigThumb!=null) {
            return resizeThumbImages(arListOrigThumb);
        }

        return null;
    }

    /**
     * Resizes the thumb images
     */
    private static ArrayList<String> resizeThumbImages(ArrayList<String> origThumbImages) {
        ArrayList<String> arRes = new ArrayList<>();
        for (String im : origThumbImages) {
            try {
                im = Imager.resizeImage(im);
                arRes.add(im);
            } catch (IOException e) {
                Context.getInstance().getLogger()
                        .error(ExceptionUtils.getStackTrace(e));
            }
        }

        return arRes;
    }


    /**
     * Creates thumbs list from a PDF file
     *
     * @param endPage equals to zero means until the end of the PDF
     * @param color   gray or rgb
     * @return the path to the thumb image
     */
    private static ArrayList<String> createThumbList(String pdfFileName, int startPage, int endPage, String color) {
        String imageFormat = Context.IMAGE_FORMAT;
        int resolution;

        /*
         * try { resolution = Toolkit.getDefaultToolkit().getScreenResolution();
         * } catch (HeadlessException e) { resolution = 96; }
         */

        Context.getInstance()
                .getLogger()
                .debug("making PDF's thumb: " + pdfFileName);

        resolution = Context.PDF2IMAGE_RESOLUTION;

        String outputPrefix = pdfFileName.substring(0, pdfFileName.lastIndexOf('.'));

        int imageType;
        if ("bilevel".equalsIgnoreCase(color)) {
            imageType = BufferedImage.TYPE_BYTE_BINARY;
        } else if ("indexed".equalsIgnoreCase(color)) {
            imageType = BufferedImage.TYPE_BYTE_INDEXED;
        } else if ("gray".equalsIgnoreCase(color)) {
            imageType = BufferedImage.TYPE_BYTE_GRAY;
        } else if ("rgb".equalsIgnoreCase(color)) {
            imageType = BufferedImage.TYPE_INT_RGB;
        } else if ("rgba".equalsIgnoreCase(color)) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        } else {
            Context.getInstance()
                    .getLogger()
                    .error("Error: the number of bits per pixel must be 1, 8 or 24.");
            return null;
        }

        try (PDDocument document = PDDocument.load(pdfFileName)) {

            // Id the endPage == 0 then it continues until the end of the
            // document
            if (endPage == 0)
                endPage = document.getNumberOfPages();

            // Makes the call to convert the PDF into the list of thumbs
            PDFImageWriter imageWriter = new PDFImageWriter();

            boolean success = imageWriter.writeImage(document, imageFormat,
                    PdfEncryption.decryptDocument(document), startPage, endPage, outputPrefix, imageType,
                    resolution);

            if (success) {
                ArrayList<String> resImages = new ArrayList<>();
                for (int i = startPage; i <= endPage; i++) {
                    String sb = outputPrefix + i + "." + imageFormat;
                    resImages.add(sb);
                }
                return resImages;
            } else {
                Context.getInstance()
                        .getLogger()
                        .error("Error: no writer found for image format '"
                                + imageFormat + "'");
            }
        } catch (IOException e) {
            Context.getInstance().getLogger().error("IO Exception: "+e.getMessage());
        } catch (CryptographyException e) {
            Context.getInstance().getLogger().error("Decrypt Exception: "+e.getMessage());
        }

        return null;
    }


}
