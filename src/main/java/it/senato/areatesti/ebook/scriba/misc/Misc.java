package it.senato.areatesti.ebook.scriba.misc;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.EbookType;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

/**
 * Miscellaneous utilities
 */
public class Misc {
    /**
     * Normalizes the package Path in the content.xml (it adjusts the "/" or "\" at the end or start)
     *
     * @return the normalized Package Path
     */
    public static String normalizePackagePath(String packagePath) {
        // normalizes the \\ in /
        packagePath = packagePath.replace("\\", Context.PATH_SEP);
        // normalizes the // in /
        packagePath = packagePath.replaceAll("/+", Context.PATH_SEP);

        // normalizes the starting or ending /
        if (packagePath.length() > 1) {
            int endIndex = packagePath.length();
            int beginIndex = 0;
            if (packagePath.endsWith(Context.PATH_SEP))
                endIndex--;
            if (packagePath.startsWith(Context.PATH_SEP))
                beginIndex++;

            packagePath = packagePath.substring(beginIndex, endIndex);

            return packagePath + Context.PATH_SEP;
        } else if (packagePath.length() == 1) {
            if (packagePath.startsWith(Context.PATH_SEP))
                packagePath = "";

            return packagePath;
        }

        return packagePath;
    }

    /**
     * Normalizes the filename
     */
    public static String normalizeFileNameHard(String fileName) {
        return StringUtils.strip(
                StringUtils.left(
                        StringUtils.trim(fileName).replace(" ", "-").toUpperCase().replaceAll("[^A-Z\\-0-9]+", "")
                        , Context.NORMALIZED_FILE_LENGHT), "-");
    }

    /**
     * Normalizes the filename: soft version
     */
    public static String normalizeFileNameSoft(String fileName) {
        return StringUtils.trim(fileName).replace(" ", "-").replaceAll("[^A-Za-z\\-0-9._]+", "");
    }

    /**
     * Puts a generic string content into a ZIP Stream
     *
     * @return true or false
     */
    public static boolean putStringContentInZipStream(ArchiveOutputStream zipOutput,
                                                      String prefix, String xmlContent, String entryName, boolean isStored) {
        if (xmlContent != null) {
            ZipEntry entry = new ZipEntry(prefix + entryName);
            if (isStored)
                entry.setMethod(ZipEntry.STORED);
            else
                entry.setMethod(ZipEntry.DEFLATED);

            ZipArchiveEntry entryArchive;
            try {
                byte[] buffer = xmlContent.getBytes(Context.DEF_ENCODING);

                entryArchive = new ZipArchiveEntry(entry);
                if (isStored) {
                    entryArchive.setSize(buffer.length);

                    // calculates the CRC32
                    CRC32 crc32 = new CRC32();
                    crc32.update(buffer);

                    entryArchive.setCrc(crc32.getValue());
                }

                zipOutput.putArchiveEntry(entryArchive);
                zipOutput.write(buffer);
                zipOutput.closeArchiveEntry();

            } catch (IOException e) {
                Context.getInstance().getLogger()
                        .error(ExceptionUtils.getStackTrace(e));
                return false;
            }

            return true;
        }
        return false;
    }

    /**
     * Puts a byte content in a zip stream
     */
    public static void putByteContentInZipStream(ArchiveOutputStream zipOutput,
                                                 String entryString, byte[] bArray) {
        ZipArchiveEntry entry = new ZipArchiveEntry(entryString);

        entry.setExternalAttributes(0);
        // entry.setSize(bArray.length);
        try {
            zipOutput.putArchiveEntry(entry);
            zipOutput.write(bArray);
            zipOutput.closeArchiveEntry();
        } catch (IOException e) {
            Context.getInstance().getLogger()
                    .error(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Gets the content in the property section
     *
     * @return the content
     */
    public static String getContentProp(String propFileName, String charEncoding) {
        try {
            InputStream is2 = Thread.currentThread().getContextClassLoader().getResourceAsStream(propFileName);
            return IOUtils.toString(is2, charEncoding);
        } catch (IOException e) {
            Context.getInstance().getLogger()
                    .error(ExceptionUtils.getStackTrace(e));
            return null;
        }

    }


    /**
     * Gets the timestamp in the form YYYYMMDD - HH:MM
     */
    public static String getNowTimestampLong() {
        Calendar cal = new GregorianCalendar();
        return String.format(Context._02D, cal.get(Calendar.DAY_OF_MONTH)) + Context.PATH_SEP +
                String.format(Context._02D, cal.get(Calendar.MONTH) + 1) + Context.PATH_SEP +
                String.format(Context._04D, cal.get(Calendar.YEAR)) + " - " +
                String.format(Context._02D, cal.get(Calendar.HOUR_OF_DAY)) + ":" +
                String.format(Context._02D, cal.get(Calendar.MINUTE));
    }

    /**
     * Gets the timestamp in the form YYYYMMDD
     */
    public static String getNowTimestamp() {
        Calendar cal = new GregorianCalendar();
        return String.format(Context._04D, cal.get(Calendar.YEAR)) + "-" +
                String.format(Context._02D, cal.get(Calendar.MONTH) + 1) + "-" +
                String.format(Context._02D, cal.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Get the extension for the EBook
     */
    public static String getEbookExtension(EbookType etype) {
        String extension;

        switch (etype) {
            case EPUB:
                extension = Context.EPUB_EXT;
                break;

            case ZIP:
                extension = Context.ZIP_EXT;
                break;

            case PDF:
                extension = Context.PDF_EXT;
                break;

            default:
                extension = "";
                break;
        }

        return extension;
    }

}
