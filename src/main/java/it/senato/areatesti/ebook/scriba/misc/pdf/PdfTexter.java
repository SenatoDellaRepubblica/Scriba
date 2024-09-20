package it.senato.areatesti.ebook.scriba.misc.pdf;

import it.senato.areatesti.ebook.scriba.Context;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.BadSecurityHandlerException;
import org.apache.pdfbox.pdmodel.encryption.StandardDecryptionMaterial;
import org.apache.pdfbox.util.PDFText2HTML;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Transforms a PDF in a Text file
 */
public class PdfTexter {
    private final String password;
    private final String encoding;
    private final String pdfFile;
    private String outputFile;

    /**
     * Constructor
     */
    public PdfTexter(String password, String encoding, String pdfFile) {
        this.password = password;
        this.encoding = encoding;
        this.pdfFile = pdfFile;
    }

    /**
     * Conversion to HTML
     */
    public ByteArrayOutputStream convertToHtml() {
        return this.convert(-1, -1, false, true, true, false, false);
    }

    /**
     * Generic conversion
     */
    private ByteArrayOutputStream convert(int startPage, int endPage, boolean toConsole, boolean toHTML, boolean force, boolean sort, boolean separateBeads) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Defaults to text files
        String ext = ".txt";

        if (startPage == -1)
            startPage = 1;

        if (endPage == -1)
            endPage = Integer.MAX_VALUE;

        Writer output = null;
        PDDocument document = null;
        try {
            try {
                //basically try to load it from a url first and if the URL
                //is not recognized then try to load it from the file system.
                URL url = new URL(pdfFile);
                document = PDDocument.load(url, force);
                String fileName = url.getFile();
                if (outputFile == null && fileName.length() > 4) {
                    outputFile = new File(fileName.substring(0, fileName.length() - 4) + ext).getName();
                }
            } catch (MalformedURLException e) {
                try {
                    document = PDDocument.load(pdfFile, force);
                    if (outputFile == null && pdfFile.length() > 4) {
                        outputFile = pdfFile.substring(0, pdfFile.length() - 4) + ext;
                    }
                } catch (IOException e1) {
                    Context.getInstance().getLogger().error(e);
                }
            } catch (IOException e) {
                Context.getInstance().getLogger().error(e);
            }

            if (document!=null) {
                //document.print();
                if (document.isEncrypted()) {
                    StandardDecryptionMaterial sdm = new StandardDecryptionMaterial(password);
                    document.openProtection(sdm);
                    AccessPermission ap = document.getCurrentAccessPermission();

                    if (!ap.canExtractContent()) {
                        Context.getInstance().getLogger().error("You do not have permission to extract text");
                    }
                }

                if (toConsole) {
                    output = new OutputStreamWriter(System.out);
                } else {
                    output = new OutputStreamWriter(byteArrayOutputStream, encoding);
                }

                PDFTextStripper stripper;
                if (toHTML) {
                    stripper = new PDFText2HTML(encoding);
                } else {
                    stripper = new PDFTextStripper(encoding);
                }
                stripper.setForceParsing(force);
                stripper.setSortByPosition(sort);
                stripper.setShouldSeparateByBeads(separateBeads);
                stripper.setStartPage(startPage);
                stripper.setEndPage(endPage);
                stripper.writeText(document, output);
            }

        } catch (BadSecurityHandlerException | IOException | CryptographyException e) {
            Context.getInstance().getLogger().error(e);
        } finally {
            if (output != null) try {
                output.close();
            } catch (IOException e) {
                Context.getInstance().getLogger().error(e);
            }
            if (document != null) try {
                document.close();
            } catch (IOException e) {
                Context.getInstance().getLogger().error(e);
            }
        }

        return byteArrayOutputStream;
    }
}
