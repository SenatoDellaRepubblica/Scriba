package it.senato.areatesti.ebook.scriba.packaging;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.EbookType;
import it.senato.areatesti.ebook.scriba.misc.TempCleaner;
import it.senato.areatesti.ebook.scriba.misc.pdf.PdfImager;
import it.senato.areatesti.ebook.scriba.misc.xhtml.EPubXhtmlMgr;
import it.senato.areatesti.ebook.scriba.plugin.base.AbstractPlugin;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * manager for the PDF into the PackageManager
 */
class PdfManager {
    private final String pdfThumbColor;

    /**
     * Constructor
     */
    PdfManager(String pdfThumbColor) {
        this.pdfThumbColor = pdfThumbColor;
    }


    /**
     * Extract further content prof PDF: images, texts and so on
     */
    ArrayList<ContentItem> extractContentsFromPdf(
            ContentItem contentItemOfPdfRef, EbookType ebookType, boolean notExtractImgFromPdf) {
        if (contentItemOfPdfRef.getByteContent() != null) {
            try {
                String fileName = null;


                if (!notExtractImgFromPdf) {
                    // Writes the PDF to disk
                    File f = writePdfFileToDisk(contentItemOfPdfRef);
                    Context.getInstance().getTempGarbageCleaner()
                            .putFileInGarbageCollector(f.getPath());
                    fileName = f.getPath();
                }

                // ---------- makes the Cover
                contentItemOfPdfRef = makesCoverXhtmlPageForPdf(contentItemOfPdfRef);

                // ---------- creates the PDF images

                if (!contentItemOfPdfRef.isPdfToHtml()) {
                    if (!notExtractImgFromPdf)
                        return makesPdfImages(contentItemOfPdfRef, fileName, true);
                } else {
                    // Checks the reference for the plugin
                    if (contentItemOfPdfRef.getPlugin() != null) {

                        AbstractPlugin plugInstance = PackageMaker.getPluginInstance(contentItemOfPdfRef, ebookType);

                        ArrayList<ContentItem> ciList = plugInstance.makesHtmlFromPdf(contentItemOfPdfRef, fileName);
                        // Now it executes the Tidy, Xsl and CSS stuffs
                        for (ContentItem c : ciList)
                            PackageMaker.adjustXslTidyCssLink(c);

                        return ciList;
                    } else
                        Context.getInstance().getLogger().error("You need to specify a plugin to manage HTML extracted from PDF!");

                }

            } catch (Exception e) {
                Context.getInstance().getLogger()
                        .error(ExceptionUtils.getStackTrace(e));
            }

        }
        return null;
    }


    /**
     * Writes the PDF temp file onto disk
     */
    private File writePdfFileToDisk(ContentItem contentItemOfPdfRef)
            throws IOException {
        File f = getPdfTempName(contentItemOfPdfRef);
        FileUtils.writeByteArrayToFile(f, contentItemOfPdfRef.getByteContent());
        return f;
    }


    /**
     * Gets the PDF temp name
     */
    private File getPdfTempName(ContentItem contentItemOfPdfRef) {

        String tempFileNameString = TempCleaner.getTempDirectory() + File.separator +
                FilenameUtils.removeExtension(contentItemOfPdfRef.getPackageFile()) +
                Context.getInstance().getTempExt() + Context.PDF_EXT;

        return new File(tempFileNameString);
    }

    /**
     * Makes the PDF Images
     */
    private ArrayList<ContentItem> makesPdfImages(ContentItem contentItemOfPdfRef,
                                                  String pdfFileName, boolean isInSpine) {
        ArrayList<String> arThumbLists = PdfImager.createThumbList(pdfFileName, 1, this.pdfThumbColor);
        ArrayList<ContentItem> ciList = new ArrayList<>();
        if (arThumbLists != null && !arThumbLists.isEmpty()) {
            // makes the PH and the thumb
            for (String thumbFileName : arThumbLists) {
                if (thumbFileName != null) {
                    File fthumb = new File(thumbFileName);
                    // creates the new ContentItem for the thumb
                    // image
                    ContentItem ciImgThumb = new ContentItem(
                            contentItemOfPdfRef.getPackagePath(),
                            fthumb.getName(), Context.ID_OPF_PREFIX + fthumb.getName(),
                            null, fthumb.toURI().toString(),
                            "image" + Context.PATH_SEP + Context.IMAGE_FORMAT);

                    try {
                        // downloads the content of the img thumb from the disk
                        byte[] bArrayImgThumb = PackageMaker.downloadContentStrict(ciImgThumb);

                        ciImgThumb.setByteContent(bArrayImgThumb);

                        // Creates the xhtml page as container for thumb image
                        String xhtmlTemplThumb = TemplateManager
                                .getThumbPdfTemplate(
                                        ciImgThumb.getPackageFile()
                                                + Context.XHTML_EXT,
                                        ciImgThumb.getPackageFile(),
                                        ciImgThumb.getPackageFile()
                                                + "_image");

                        if (xhtmlTemplThumb != null) {

                            // adjusts the CSS reference
                            xhtmlTemplThumb = EPubXhtmlMgr
                                    .manageCssReference(
                                            ciImgThumb.getNestedLevel(),
                                            xhtmlTemplThumb,
                                            Context.getInstance().cssStyleName);

                            // creates the contentItem

                            ContentItem ciXhtmlThumb = new ContentItem(
                                    ciImgThumb.getPackagePath(),
                                    ciImgThumb.getPackageFile() + Context.XHTML_EXT,
                                    Context.ID_OPF_PREFIX + ciImgThumb.getPackageFile() + Context.XHTML_EXT, null,
                                    null, null, Context.XHTML_MIMETYPE, null, null, null,
                                    false, isInSpine, false, false,
                                    false);

                            ciXhtmlThumb.setStringContent(xhtmlTemplThumb);
                            ciList.add(ciXhtmlThumb);
                        }

                    } catch (UnsupportedEncodingException e) {
                        Context.getInstance().getLogger()
                                .error(ExceptionUtils.getStackTrace(e));
                    }

                    // adds the contentItem to the return list
                    ciList.add(ciImgThumb);

                    // puts the resource into the TempGarbageCollector
                    Context.getInstance()
                            .getTempGarbageCleaner()
                            .putFileInGarbageCollector(
                                    thumbFileName);


                }
            }
            return ciList;
        }

        return null;
    }


    /**
     * Makes the cover page before the PDF (image) file
     */
    private ContentItem makesCoverXhtmlPageForPdf(
            ContentItem contentItemOfPdfRef) throws UnsupportedEncodingException {
        String sContent;
        if (contentItemOfPdfRef.getPhXhtmlPageForPdf() == null)
            sContent = TemplateManager.getDefaultTemplateCoverPdf(
                    contentItemOfPdfRef.getTocName(),
                    contentItemOfPdfRef.getTocName(),
                    contentItemOfPdfRef.getContentUrl());
        else
            sContent = TemplateManager
                    .getCustomTemplateCoverPdf(contentItemOfPdfRef);

        if (sContent != null) {
            contentItemOfPdfRef.setStringContent(sContent);
            contentItemOfPdfRef.setContentMediaType(Context.XHTML_MIMETYPE);
            contentItemOfPdfRef.setPackageFile(contentItemOfPdfRef.getPackageFile()
                    + Context.XHTML_EXT);

            contentItemOfPdfRef.setStringContent(
                    EPubXhtmlMgr.manageCssReference(
                            contentItemOfPdfRef.getNestedLevel(), contentItemOfPdfRef.getStringContent(),
                            Context.getInstance().cssStyleName));
        }

        return contentItemOfPdfRef;
    }
}
