package it.senato.areatesti.ebook.scriba.misc.pdf;

import it.senato.areatesti.ebook.scriba.Context;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;

/**
 * Manages the encryption of the PDF file
 */
public class PdfEncryption {
    /**
     * Decrypts PDF document and return the password in clear form
     */
    public static String decryptDocument(PDDocument document) throws CryptographyException, IOException {
        if (document != null && document.isEncrypted())
            document.decrypt(Context.getInstance().pdfPassword);

        return Context.getInstance().pdfPassword;

    }


}
