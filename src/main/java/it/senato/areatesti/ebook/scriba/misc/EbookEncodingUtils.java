package it.senato.areatesti.ebook.scriba.misc;

import it.senato.areatesti.ebook.scriba.Context;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;


/**
 * Class to manage encoding
 */
public class EbookEncodingUtils {
    private String detectedEncoding;


    /**
     * Converts a byte buffer from an encoding to another one
     *
     * @return the outcome
     */
    private static byte[] convertEncoding(String inputEncoding,
                                          String outputEncoding, byte[] bufferToConvert)
            throws UnsupportedEncodingException {
        return simpleConversion(inputEncoding, outputEncoding, bufferToConvert);

        //return advancedConversion(inputEncoding, outputEncoding,bufferToConvert);
    }

    /**
     * It uses java functions
     */
    private static byte[] simpleConversion(String inputEncoding,
                                           String outputEncoding, byte[] bufferToConvert)
            throws UnsupportedEncodingException {
        String ucs2Content = new String(bufferToConvert, inputEncoding);
        return ucs2Content.getBytes(outputEncoding);
    }

    /**
     * Detects the charset of a file
     *
     * @return the detected encoding
     */
    public static String detectFileEncoding(File fileName, int bufferLenght)
            throws IOException {
        FileInputStream fis = new FileInputStream(fileName);
        return detectEncoding(fis, bufferLenght);
    }

    /**
     * Detects the encoding of a byte buffer
     *
     * @return the detected encoding
     */
    private static String detectEncoding(InputStream is, int bufferLenght)
            throws IOException {
        byte[] buf = new byte[bufferLenght];
        BufferedInputStream bis = new BufferedInputStream(is);
        UniversalDetector detector = new UniversalDetector(null);

        int nread;
        while ((nread = bis.read(buf)) > 0 && !detector.isDone())
            detector.handleData(buf, 0, nread);

        detector.dataEnd();

        String encoding = detector.getDetectedCharset();
        detector.reset();

        bis.close();

        return encoding;
    }

    public static byte[] convertDeclaredEncoding(byte[] byteContent, String declaredEncoding, String outputEncoding) throws UnsupportedEncodingException {
        Context.getInstance().getLogger().debug("Declared encoding: " + declaredEncoding);
        if (declaredEncoding != null)
            return convertEncoding(declaredEncoding, outputEncoding, byteContent);

        return byteContent;
    }

    /**
     * Converts the native encoding
     */
    public byte[] convertNativeEncoding(byte[] byteContent, String outputEncoding) throws IOException {
        this.detectedEncoding = detectEncoding(new ByteArrayInputStream(byteContent), byteContent.length);
        Context.getInstance().getLogger().debug("native encoding detected: " + detectedEncoding);
        if (detectedEncoding != null)
            return convertEncoding(detectedEncoding, outputEncoding, byteContent);

        return byteContent;
    }

    /**
     * Gets the detected encoding
     *
     * @return the detectedEncoding
     */
    public String getDetectedEncoding() {
        return detectedEncoding;
    }


}
