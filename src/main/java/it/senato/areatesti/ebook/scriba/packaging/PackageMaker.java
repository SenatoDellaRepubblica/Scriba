package it.senato.areatesti.ebook.scriba.packaging;

import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.parser.XMLParser;
import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.EbookType;
import it.senato.areatesti.ebook.scriba.epubmaker.core.ncx.NcxMaker;
import it.senato.areatesti.ebook.scriba.epubmaker.core.opf.OpfMaker;
import it.senato.areatesti.ebook.scriba.epubmaker.core.xmlfile.ContainerXmlMaker;
import it.senato.areatesti.ebook.scriba.epubmaker.core.xmlfile.MimetypeMaker;
import it.senato.areatesti.ebook.scriba.exception.TerminatedGenerationException;
import it.senato.areatesti.ebook.scriba.index.base.IndexTree;
import it.senato.areatesti.ebook.scriba.misc.Misc;
import it.senato.areatesti.ebook.scriba.misc.SecUtils;
import it.senato.areatesti.ebook.scriba.misc.image.Imager;
import it.senato.areatesti.ebook.scriba.misc.url.UrlDownloadResult;
import it.senato.areatesti.ebook.scriba.misc.url.WebGet;
import it.senato.areatesti.ebook.scriba.misc.xhtml.EPubXhtmlMgr;
import it.senato.areatesti.ebook.scriba.misc.xml.JTidyManager;
import it.senato.areatesti.ebook.scriba.misc.xslt.XsltMgr;
import it.senato.areatesti.ebook.scriba.plugin.base.AbstractPlugin;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentItem;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentList;
import it.senato.areatesti.ebook.scriba.scf.bean.MetadataItem;
import it.senato.areatesti.ebook.scriba.scf.bean.MetadataList;
import it.senato.areatesti.ebook.scriba.scf.bean.base.IItem;
import it.senato.areatesti.ebook.scriba.scf.file.ScfReader;
import it.senato.areatesti.ebook.scriba.scf.file.ScfTokens;
import it.senato.areatesti.ebook.scriba.zipmaker.IdxHtmlMaker;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringEscapeUtils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;


/**
 * Creates final package of the ebook (it could be EPUB, ZIP, etc.)
 */
public class PackageMaker {

    private static final String imgRegEx = "(<img\\s*[^>]*\\s+src=\")([^\"]*)";
    private static final Pattern imgPattern = Pattern.compile(imgRegEx, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private File contentXmlFile;
    private String contentXmlString;
    private EbookType ebookType;
    private final String pdfThumbColor;

    private String titleInHeader;
    private final boolean noExtractImgFromPdf;


    /**
     * Constructor
     *
     */
    public PackageMaker(File contentXmlFile, String pdfThumbColor, boolean noExtractImgFromPdf) {
        this.contentXmlFile = contentXmlFile;
        this.pdfThumbColor = pdfThumbColor;
        this.noExtractImgFromPdf = noExtractImgFromPdf;
    }

    /**
     * Constructor
     *
     * @param noExtractImgFromPdf transform a PDF in an array of Images
     */
    public PackageMaker(String scfXmlContent, String pdfThumbColor, boolean noExtractImgFromPdf) {
        this.contentXmlString = scfXmlContent;
        this.pdfThumbColor = pdfThumbColor;
        this.noExtractImgFromPdf = noExtractImgFromPdf;
    }

    static AbstractPlugin getPluginInstance(ContentItem contentItem, EbookType ebookType)
            throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException,
            InvocationTargetException {
        //Class<? extends AbstractPlugin> plugClass = (Class<? extends AbstractPlugin>) Class.forName(contentItem.getPlugin());
        Class<? extends AbstractPlugin> plugClass = Class.forName(contentItem.getPlugin()).asSubclass(AbstractPlugin.class);
        Object args = new Object[]{ebookType};
        Constructor<? extends AbstractPlugin> constructor = plugClass.getDeclaredConstructor(args.getClass());
        return constructor.newInstance(args);
    }

    /**
     * Adjusts the content with JTidy, Xsl and CSS link
     *
     */
    static ContentItem adjustXslTidyCssLink(ContentItem contentItem) throws UnsupportedEncodingException {
        return adjustXslTidyCssLink(contentItem, Context.getInstance().xslProp);
    }

    /**
     * Adjusts the content with JTidy, Xsl and CSS link
     *
     */
    public static ContentItem adjustXslTidyCssLink(ContentItem contentItem, String xslPath) throws UnsupportedEncodingException {
        String sContent = contentItem.getStringContent();

        if (contentItem.isNeededTidy()) {
            Context.getInstance().getLogger().debug("Apply the Tidy processor");
            sContent = EPubXhtmlMgr.convertToXhtml(sContent);
        }

        if (contentItem.isNeededXsl()) {
            Context.getInstance().getLogger().debug("Apply the XSL");
            sContent = XsltMgr.applyXslFromPropPath(sContent, xslPath);
        }

        Context.getInstance().getLogger().debug("Apply the CSS");
        sContent = EPubXhtmlMgr.manageCssReference(contentItem.getNestedLevel(), sContent, Context.getInstance().cssStyleName);

        Context.getInstance().getLogger().debug("Apply the LINK update");
        sContent = EPubXhtmlMgr.manageLinkReference(sContent, Context.getInstance().baseUrl, Context.getInstance().oldBaseUrl);

        contentItem.setStringContent(sContent);
        return contentItem;
    }

    /**
     * Get the images included in the HTML content
     *
     */
    private static ArrayList<ContentItem> manageHtmlImages(ContentItem mc) {
        ArrayList<ContentItem> ar = new ArrayList<>();

        if (mc.getContentUrl() != null) {

            // parse the html to find the IMG references
            Matcher matcher;
            String newPath =null;
            try {
                String htmlContentString = mc.getStringContent();

                matcher = imgPattern.matcher(htmlContentString);

                StringBuffer newHtmlContent = new StringBuffer();
                while (matcher.find()) {
                    String imgPath = matcher.group(2);

                    // XXX: temp modification TBD
                    //if (alwaysDownloadImage || !(new URL(imgPath).getProtocol().equals("file")))
                    //{
                    // Download the Image from the reference inside the HTML content

                    byte[] bc;
                    String imgMime, imgExt;
                    StringBuilder imgName = new StringBuilder("htmlimage");

                    imgExt = FilenameUtils.getExtension(imgPath);
                    imgMime = Imager.fromExt2MimeType(imgExt);
                    if (isValidUrl(mc.getContentUrl())) {
                        URL imgUrl = new URL(new URL(mc.getContentUrl()), imgPath);
                        Context.getInstance().getLogger().debug("Found the image path: " + imgPath);
                        WebGet wg = new WebGet(imgUrl.toString(), null, null);
                        bc = wg.httpDownload();
                    } else {
                        newPath = FilenameUtils.getPath(mc.getContentUrl()) +FilenameUtils.getName(imgPath);
                        bc = FileUtils.readFileToByteArray(new File(newPath));
                    }

                    if (bc != null) {
                        imgName.append(SecUtils.getHex(SecUtils.getRandomBytes(4))).append(".").append(imgExt);
                        Context.getInstance().getLogger().debug("New Image name: " + imgName.toString());

                        // Create the ContentItem to store the new image reference
                        ContentItem contImgItem = new ContentItem(
                                Context.PATH_SEP + mc.getPackagePath(),
                                imgName.toString(),
                                Context.ID_OPF_PREFIX + imgName,
                                null,
                                imgName.toString(),
                                imgMime);

                        // Set the byte content of the image
                        contImgItem.setByteContent(bc);

                        //Context.getInstance().getLogger().debug("Image content: "+ new String(bc));
                        ar.add(contImgItem);

                        // Adapt the content to point to the new image name
                        String replString = "$1" + imgName.toString();
                        matcher.appendReplacement(newHtmlContent, replString);
                    }
                }
                //}
                matcher.appendTail(newHtmlContent);

                // Add the new content
                mc.setStringContent(newHtmlContent.toString());

            } catch (MalformedURLException e) {
                Context.getInstance().getLogger().error("Malformed URL for the html image: " + mc.getContentUrl());

            } catch (UnsupportedEncodingException e) {
                Context.getInstance().getLogger().error(ExceptionUtils.getStackTrace(e));
            } catch (IOException e) {
                Context.getInstance().getLogger().error("Problems in reading file: " + newPath);
            }
        }
        return ar;
    }

    /**
     * Downloads the content (only)
     *
     */
    public static byte[] downloadContentStrict(ContentItem contentItem) {
        return downloadContentStrict(contentItem, null, null);
    }

    /**
     * Downloads the content (only)
     *
     */
    public static byte[] downloadContentStrict(ContentItem contentItem, String forcedUrl) {
        return downloadContentStrict(contentItem, forcedUrl, null);
    }

    /**
     * Downloads the content (only)
     * <p>
     * If the content is Text then the encoding is DEF (UTF8)
     *
     * @param forcedUrl      if you want to use this URL instead of ContentItem's URL
     */
    public static byte[] downloadContentStrict(ContentItem contentItem, String forcedUrl, String sourceEncoding) {
        byte[] bArray;
        if (forcedUrl == null)
            forcedUrl = contentItem.getContentUrl();

        String filename = forcedUrl;
        if (isValidUrl(filename)) {
            WebGet webGet = new WebGet(forcedUrl, contentItem.getContentMediaType(), sourceEncoding);
            UrlDownloadResult udr = webGet.getUrlContent();
            if (udr != null) {
                bArray = udr.getMainContent();
                if (sourceEncoding == null)
                    contentItem.setContentSrcEncoding(webGet.getSourceEncoding());
                else
                    contentItem.setContentSrcEncoding(sourceEncoding);
                return bArray;
            } else {
                Context.getInstance().getLogger().error("The URL path is probably bad written or it is not reachable: " + forcedUrl);
                return null;
            }
        } else {
            try {
                return FileUtils.readFileToByteArray(new File(filename));
            } catch (IOException e) {
                Context.getInstance().getLogger().error("The FILE path is probably bad written!", e);
                return null;
            }
        }
    }

    public static boolean isValidUrl(String filename) {
        return StringUtils.startsWith(filename, "file:") || StringUtils.startsWith(filename, "http:") || StringUtils.startsWith(filename, "https:");
    }

    /**
     * Makes the error page
     *
     */
    public static String makeErrorPage(ContentItem contentItem) {
        // XXX: localizes the error messages in a separated file
        String errorMessage = "Non e' stato possibile includere la seguente risorsa: " + StringEscapeUtils.escapeXml11(contentItem.getContentUrl());

        // Error page
        String sContent = TemplateManager.getErrorTemplatePage(errorMessage);

        // Modifies the extension
        contentItem.setContentMediaType(Context.XHTML_MIMETYPE);
        contentItem.setPackageFile(contentItem.getPackageFile() + "-err" + Context.XHTML_EXT);

        return sContent;
    }

    /**
     * Add a rootCi and its dependencies to an existing list
     * <p>
     * XXX: it must be used if the content you are adding has dependent ContentItems (otherwise the NCX could have problems)
     *
     */
    private static void addContentItemWithDepend(
            ArrayList<ContentItem> ciListToAdd, ContentItem rootCi,
            ArrayList<ContentItem> rootCiDependecies) {
        ciListToAdd.add(rootCi);
        if (rootCiDependecies != null) {
            for (ContentItem ci : rootCiDependecies) {
                ci.setIsDependentOn(rootCi);
                ciListToAdd.add(ci);
            }
        }
    }

    /**
     * Makes the package
     *
     */
    public boolean make(ByteArrayOutputStream zipOutputStream,
                        EbookType ebookType) throws ParserConfigurationException,
            IOException, XPathExpressionException {
        this.ebookType = ebookType;

        ScfReader bookReader = getTheReader();
        return makeEbookStream(bookReader, zipOutputStream);
    }

    /**
     * Makes the package
     *
     */
    public boolean make(String zipOpsFileName, EbookType ebookType)
            throws XPathExpressionException, ParserConfigurationException,
            IOException {
        Context.getInstance()
                .getLogger()
                .info("************************* PACKAGE TRAIL ("
                        + zipOpsFileName
                        + ") ******************************************");

        this.ebookType = ebookType;

        ScfReader bookReader = getTheReader();

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        boolean ret = makeEbookStream(bookReader, bo);

        if (ret) {
            // Writes the stream onto file
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(zipOpsFileName));
            bo.writeTo(bos);
            bos.flush();
            bo.close();
        }

        return ret;
    }

    /**
     * Gets the correct reader instance
     *
     */
    private ScfReader getTheReader()
            throws ParserConfigurationException, IOException,
            XPathExpressionException {
        ScfReader bookReader;
        if (contentXmlFile != null)
            bookReader = new ScfReader(contentXmlFile);
        else if (contentXmlString != null)
            bookReader = new ScfReader(contentXmlString);
        else
            throw new RuntimeException("PackageManager invocation problem!");
        return bookReader;
    }

    /**
     * Packages the OPS ZIP Stream
     *
     */
    private boolean makeEbookStream(ScfReader bookReader,
                                    ByteArrayOutputStream zipOutputStream) {
        boolean ret;

        BufferedOutputStream bo = new BufferedOutputStream(zipOutputStream);
        ArchiveOutputStream zipArchive = null;
        PdfPackager pdfPackager = new PdfPackager(bookReader);
        ZipPackager zipPackager = new ZipPackager();

        switch (this.ebookType) {
            case EPUB:
            case ZIP:
                // --------------- creates the zip stream
                zipArchive = zipPackager.openZipFile(bo);
                if (zipArchive == null)
                    return false;
                break;

            case PDF:
                pdfPackager.openPdfFile(bo);
                break;

            default:
                throw new RuntimeException("Not permitted");

        }

        // ---------------- Puts the real contents
        String entryPrefix = "";
        if (this.ebookType == EbookType.EPUB) {
            entryPrefix = "OPS" + Context.PATH_SEP;
        }

        try {
            processPackageContents(bookReader, entryPrefix);
        } catch (TerminatedGenerationException e1) {
            Context.getInstance().getLogger().error("Current generation terminated for errors. Check the log!");
            return false;
        }

        Context.getInstance().getLogger().info("------------------- EBOOK: CONTENTS PROCESSED ------------------");

        // ATTENTION: these are after the contents!!
        switch (this.ebookType) {
            case EPUB:
                ret = zipPackager.writeOpsFiles(bookReader, zipArchive);
                if (!ret) {
                    Context.getInstance().getLogger()
                            .error("Failed to write Ops files");
                    return ret;
                }
                break;

            case ZIP:
                ret = zipPackager.writeHtmlFiles(bookReader, zipArchive);
                if (!ret) {
                    Context.getInstance().getLogger()
                            .error("Failed to write Html files");
                    return ret;
                }
                break;

            case PDF:
                pdfPackager.composePdfEbook();
                break;

            default:
                throw new RuntimeException("Not permitted");
        }
        //Context.gc();

        // ---------------- Flushes the content in the final ebook
        try {
            switch (this.ebookType) {
                case EPUB:
                case ZIP:
                    zipPackager.closeZipFile(bookReader, zipArchive, entryPrefix);
                    break;

                case PDF:
                    pdfPackager.closePdfFile(bo);
                    break;

                default:
                    throw new RuntimeException("Not permitted");

            }

            Context.getInstance()
                    .getLogger()
                    .info("------------------- EBOOK FILE CLOSED------------------");

        } catch (IOException e) {
            Context.getInstance().getLogger()
                    .error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;

    }

    /**
     * Process the ContentItemList to download and adjust the content
     *
     */
    private void processPackageContents(ScfReader bookReader, String opsEntryPrefix) throws TerminatedGenerationException {
        ArrayList<ContentItem> initContentItemList = preProcessContentList(bookReader);

        // Creates the contents
        ArrayList<ContentItem> newContentItemList = processContentList(opsEntryPrefix, initContentItemList);

        // Updates the Internal Content List
        bookReader.getContentList().setIntList(null);
        bookReader.getContentList().setIntList(new ArrayList<>());
        bookReader.getContentList().getIntList().addAll(newContentItemList);
    }

    /**
     * Final processing of the Content list
     *
     */
    private ArrayList<ContentItem> processContentList(String opsEntryPrefix,
                                                      ArrayList<ContentItem> initContentItemList) {
        ArrayList<ContentItem> newContentItemList = new ArrayList<>();
        for (IItem c : initContentItemList) {
            try {
                ContentItem contentItem = ((ContentItem) c);

                String finalEbookTypes = contentItem.getFinalEbookTypes();
                if (finalEbookTypes == null || finalEbookTypes.toLowerCase().contains(this.ebookType.toString().toLowerCase())) {

                    Context.getInstance().getLogger().info("Process Contents (noExtractImgFromPdf=" + this.noExtractImgFromPdf + "): " + opsEntryPrefix + contentItem.getPackagePath() + contentItem.getPackageFile());

                    // --------------------------- PDF content --------------------------
                    if (contentItem.getContentMediaType().equals(Context.PDF_MIMETYPE)) {

                        switch (this.ebookType) {
                            case EPUB:
                                contentItem = getGenericContent(contentItem);

                                // Extrapolates all the pages as images
                                PdfManager pdfManager = new PdfManager(this.pdfThumbColor);
                                ArrayList<ContentItem> newImgCiList = pdfManager.extractContentsFromPdf(contentItem, this.ebookType, this.noExtractImgFromPdf);

                                // Adds the content to the List
                                addContentItemWithDepend(newContentItemList, contentItem, newImgCiList);
                                break;

                            case ZIP:
                                contentItem = getGenericContent(contentItem);
                                newContentItemList.add(contentItem);
                                break;

                            default:
                                newContentItemList.add(contentItem);
                                break;
                        }
                    }
                    // --------------------------- XHTML --------------------------
                    else if (contentItem.getContentMediaType().equals(Context.XHTML_MIMETYPE)) {
                        contentItem = getGenericContent(contentItem);

                        // manage the Tidy, Xsl, internal CSS and link references
                        contentItem = adjustXslTidyCssLink(contentItem);

                        // It manages the included images only for EPUB and ZIP final ebook type
                        if (this.ebookType == EbookType.EPUB || this.ebookType == EbookType.ZIP) {
                            // create a contentItem list with the Images in the XHTML and add to the original list
                            // (it does not follow local URL included in the XHTML)
                            addContentItemWithDepend(newContentItemList, contentItem, manageHtmlImages(contentItem));
                        } else {
                            newContentItemList.add(contentItem);
                        }
                    }
                    // ---- ALL OTHER FORMATS SUCH IMAGES AND SO ON
                    else {
                        contentItem = getGenericContent(contentItem);
                        newContentItemList.add(contentItem);
                    }
                } else {
                    Context.getInstance().getLogger().info("Skipped contents for the final ebook (" + this.ebookType.toString() + ") format: " + opsEntryPrefix
                            + contentItem.getPackagePath()
                            + contentItem.getPackageFile());
                }

            } catch (UnsupportedEncodingException e) {
                Context.getInstance().getLogger()
                        .error(ExceptionUtils.getStackTrace(e));
            }
        }
        return newContentItemList;
    }

    /**
     * Processes the content list for the presence of Plugin and passes the computation to them (if they exists)
     *
     */
    private ArrayList<ContentItem> preProcessContentList(
            ScfReader bookReader) throws TerminatedGenerationException {
        ArrayList<IItem> contentList = bookReader.getContentList().getIntList();
        MetadataList metaList = bookReader.getMetadataList();

        ArrayList<ContentItem> initContentItemList = new ArrayList<>();
        for (IItem c : contentList) {
            ContentItem contentItem = ((ContentItem) c);
            //System.out.println(contentItem.getPackagePath());

            String finalEbookTypes = contentItem.getFinalEbookTypes();
            if (finalEbookTypes == null || finalEbookTypes.toLowerCase().contains(this.ebookType.toString().toLowerCase())) {
                if (contentItem.getPlugin() != null) {
                    processContentWithPlugin(metaList, initContentItemList, contentItem);
                } else {
                    initContentItemList.add(contentItem);
                }
            } else {
                Context.getInstance().getLogger().info("Skipped the plugin processing for this specific ebook type and path: " + this.ebookType.toString() + ": " + contentItem.getPackagePath()
                        + contentItem.getPackageFile());
            }


        }
        return initContentItemList;
    }

    /**
     * Process the content that contains a Plugin
     *
     */
    private void processContentWithPlugin(MetadataList metaList, ArrayList<ContentItem> initContentItemList, ContentItem contentItem) throws TerminatedGenerationException {
        try {
            Context.getInstance().getLogger().info("Using Plugin named: " + contentItem.getPlugin());

            AbstractPlugin plugInstance = getPluginInstance(contentItem, this.ebookType);

            ArrayList<ContentItem> ciList = (ArrayList<ContentItem>) plugInstance.elaborateContent(contentItem, metaList);
            if (ciList!=null)
                initContentItemList.addAll(ciList);

        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            Context.getInstance().getLogger().error("File URL: " + contentItem.getContentUrl(), e);
        }
    }

    /**
     * Gets the byte array for all other kind of ContentItem
     *
     */
    private ContentItem getGenericContent(ContentItem contentItem) throws UnsupportedEncodingException {
        // it downloads the content from the URL only if has not already set the
        // byteContent
        if (contentItem.getContentUrl() != null
                && contentItem.getByteContent() == null)
            contentItem.setByteContent(downloadContent(contentItem));

        return contentItem;
    }

    /**
     * Downloads the content and return an XHTML error page if (the content) is missing
     *
     * @return it could be binary or string content
     */
    private byte[] downloadContent(ContentItem contentItem) throws UnsupportedEncodingException {
        byte[] bArray = downloadContentStrict(contentItem);
        if (bArray == null) {
            // sets the tidy and the xsl off
            contentItem.setNeededTidy(false);
            contentItem.setNeededXsl(false);

            // replaces with error page
            bArray = makeErrorPage(contentItem).getBytes(Context.DEF_ENCODING);
        }

        return bArray;
    }

    private Image getImage(String pathImage) throws BadElementException, IOException {
        Image image;
        if (isValidUrl(pathImage))
            image = Image.getInstance(new URL(pathImage));
        else
            image = Image.getInstance(FileUtils.readFileToByteArray(new File(pathImage)));
        return image;
    }

    private String getTitle(MetadataList mList) {
        // Searches the dc:title and set it as the docTitle of the NCX file
        String title = "";
        for (IItem item : mList.getIntList())
            if (item instanceof MetadataItem) {
                MetadataItem m = (MetadataItem) item;
                if (m.getElemType().equals("dc")
                        && m.getElemName().equals(ScfTokens.DC_TITLE)) {
                    title = m.getElemVal();
                    break;
                }
            }
        return title;
    }

    private class ZipPackager {

        ZipPackager() {
        }

        /**
         * Close the ZIP file
         *
         */
        private void closeZipFile(ScfReader bookReader,
                                  ArchiveOutputStream zipArchive, String entryPrefix)
                throws IOException {
            writePackageContents(bookReader, zipArchive, entryPrefix);
            zipArchive.flush();
            zipArchive.close();
        }

        /**
         * Open the ZIP file
         *
         */
        private ArchiveOutputStream openZipFile(BufferedOutputStream bo) {
            ArchiveOutputStream zipArchive;
            try {
                zipArchive = new ArchiveStreamFactory().createArchiveOutputStream(
                        "zip", bo);

                Context.getInstance()
                        .getLogger()
                        .debug("------------------- EBOOK FILE OPENED------------------");

            } catch (ArchiveException e) {
                Context.getInstance().getLogger()
                        .error(ExceptionUtils.getStackTrace(e));
                return null;
            }
            return zipArchive;
        }

        /**
         * Write the HTML files for HTML ebook format type
         *
         */
        private boolean writeHtmlFiles(ScfReader bookReader,
                                       ArchiveOutputStream zipArchive) {
            boolean ret;
            // Creates the index.xhtml and put it into the Zip stream
            String idxContent = getHtmlIdxContent(bookReader);

            Context.getInstance()
                    .getLogger()
                    .debug("------------------- ZIP: HTML INDEX ------------------");

            ret = Misc.putStringContentInZipStream(zipArchive, "", idxContent,
                    "index.xhtml", false);

            return ret;
        }

        /**
         * Write the OPS files as OPF, TOC, Container.xml (not the contents)
         *
         */
        private boolean writeOpsFiles(ScfReader bookReader,
                                      ArchiveOutputStream zipArchive) {
            boolean ret;


            // ATTENTION: it must be the first file that must be put into the ZIP file
            // ---------------- Puts the descriptor files for EPUB format (this is
            // only for the mimetype (it must be the first file at 38 offset in the
            // zip archive

            // Puts the mimetype file
            String mimetypeFileContent = new MimetypeMaker().getMimetypeContent();
            ret = Misc.putStringContentInZipStream(zipArchive, "", mimetypeFileContent,
                    "mimetype", true);
            if (!ret) {
                Context.getInstance().getLogger()
                        .error("Failed to crete the OPS mimetype");
                return ret;
            }

            Context.getInstance()
                    .getLogger()
                    .debug("------------------- OPS: MIMETYPE finished ------------------");

            // ATTENTION: it must be before the OPF section because the OPF section add the item added in the NCX
            // Creates the TOC.NCX and put it into the Zip stream
            String ncxContent = getNcxContent(bookReader);
            ret = Misc.putStringContentInZipStream(zipArchive, "OPS" + Context.PATH_SEP, ncxContent, "toc.ncx", false);

            if (!ret) {
                Context.getInstance().getLogger().error("Failed to crete the OPS NCX");
                return ret;
            }

            Context.getInstance().getLogger().debug("------------------- OPS: NCX finished ------------------");

            // Creates the CONTENT.OPF and put it into the Zip stream
            String opfContent = getOpfContent(bookReader);
            ret = Misc.putStringContentInZipStream(zipArchive, "OPS"
                    + Context.PATH_SEP, opfContent, "content.opf", false);
            if (!ret) {
                Context.getInstance().getLogger().error("Failed to crete the OPS content.opf");
                return ret;
            }

            Context.getInstance()
                    .getLogger()
                    .debug("------------------- OPS: OPF finished ------------------");

            // Puts the container.xml
            String containerXml = new ContainerXmlMaker()
                    .getContainerXmlContent();
            ret = Misc.putStringContentInZipStream(zipArchive, "META-INF"
                    + Context.PATH_SEP, containerXml, "container.xml", false);

            if (!ret) {
                Context.getInstance().getLogger()
                        .error("Failed to crete the OPS container.xml");
                return ret;
            }

            Context.getInstance()
                    .getLogger()
                    .debug("------------------- OPS: container.xml finished ------------------");

            return true;
        }

        /**
         * Puts the contents into the Package (ZIP, EPUB, etc.)
         *
         */
        void writePackageContents(ScfReader bookReader,
                                     ArchiveOutputStream zipOutput, String opsEntryPrefix) {

            // Stores the contents into the final ZIP file
            for (IItem iZip : bookReader.getContentList().getIntList()) {
                ContentItem cZip = (ContentItem) iZip;

                byte[] bArray = cZip.getByteContent();

                // Prints the Entry in the Zip File
                if (bArray != null) {
                    Misc.putByteContentInZipStream(
                            zipOutput,
                            opsEntryPrefix + cZip.getPackagePath()
                                    + cZip.getPackageFile(), bArray);
                }
            }
        }

        /**
         * Makes the OPF content
         */
        private String getOpfContent(ScfReader bookReader) {
            ContentList cList = bookReader.getContentList();
            MetadataList mList = bookReader.getMetadataList();
            // System.out.println(WebGet.download(cmap.getItem(0).getContentUrl()));
            return new OpfMaker().makeOpf(cList, mList);
        }

        /**
         * Makes the NCX File
         */
        private String getNcxContent(ScfReader bookReader) {
            ContentList cList = bookReader.getContentList();
            MetadataList mList = bookReader.getMetadataList();
            String title = getTitle(mList);

            return new NcxMaker(cList, mList).makeNcx(title);
        }

        /**
         * Makes the index.xhtml content
         */
        private String getHtmlIdxContent(ScfReader bookReader) {
            ContentList cList = bookReader.getContentList();
            MetadataList mList = bookReader.getMetadataList();

            // Searches the dc:title and set it as the docTitle of the NCX file
            String title = getTitle(mList);

            return new IdxHtmlMaker(cList, null).make(title);
        }

    }

    /**
     * Packager for PDF ebooks
     *
     * @author roberto.battistoni
     */
    private class PdfPackager {
        LinkedHashMap<String, String> bookmarkMap;
        private final BaseColor THE_COLOR = new BaseColor(0, 0, 0);
        private final ScfReader bookReader;
        // Events
        private HeaderEvent eventHeader;
        private ChapterSectionTOC eventToc;
        // Patterns
        private final Pattern patternHrefAmp = Pattern.compile("(<a[^>]*href\\s*=\\s*\")([^\"]*)([^>]*>)", Pattern.CASE_INSENSITIVE);
        private final Pattern imgPattern = Pattern.compile("(<img\\s*[^>]*\\s+src=\")([^\"]*)", Pattern.CASE_INSENSITIVE);
        private final Pattern patternHrefJavascript = Pattern.compile("(<a[^>]*href\\s*=\\s*\"\\s*javascript:[^>]*>)([^<]*)(</a>)", Pattern.CASE_INSENSITIVE);
        private Document pdfDocument;
        private PdfWriter pdfWriter;
        private final Font[] FONT = new Font[6];
        private final Font[] FONT_BOLD = new Font[6];

        private final Font[] BIG_FONT = new Font[3];
        private final Font[] BIG_FONT_BOLD = new Font[3];


        private XMLWorker mainXmlWorker;
        private PdfPackagerUtils pdfPackagerUtils;

        /**
         * Constructor
         */
        PdfPackager(ScfReader bookReader) {
            this.bookReader = bookReader;

            FontFamily currentFont = FontFamily.TIMES_ROMAN;

            // very Big Fonts
            BIG_FONT[0] = new Font(currentFont, 48);
            BIG_FONT[1] = new Font(currentFont, 36);
            BIG_FONT[2] = new Font(currentFont, 28);

            // very Big Fonts: bold
            BIG_FONT_BOLD[0] = new Font(currentFont, 48, Font.BOLD);
            BIG_FONT_BOLD[1] = new Font(currentFont, 36, Font.BOLD);
            BIG_FONT_BOLD[2] = new Font(currentFont, 28, Font.BOLD);


            // Fonts
            FONT[0] = new Font(currentFont, 24);
            FONT[1] = new Font(currentFont, 18);
            FONT[2] = new Font(currentFont, 14);
            FONT[3] = new Font(currentFont, 12);
            FONT[4] = new Font(currentFont, 10);
            FONT[5] = new Font(currentFont, 8);

            // Bold fonts
            FONT_BOLD[0] = new Font(currentFont, 24, Font.BOLD);
            FONT_BOLD[1] = new Font(currentFont, 18, Font.BOLD);
            FONT_BOLD[2] = new Font(currentFont, 14, Font.BOLD);
            FONT_BOLD[3] = new Font(currentFont, 12, Font.BOLD);
            FONT_BOLD[4] = new Font(currentFont, 10, Font.BOLD);
            FONT_BOLD[5] = new Font(currentFont, 10, Font.BOLD);

            String title1 = bookReader.getEleValue(ScfTokens.DC_TITLE + "1");
            titleInHeader = bookReader.getEleValue(ScfTokens.DC_TITLE);
            if (title1!=null)
                titleInHeader += " - " + title1;

        }

        /**
         * Opens the PDF document
         */
        private void openPdfFile(BufferedOutputStream bo) {
            eventHeader = new HeaderEvent(bookReader.getEleValue(ScfTokens.DC_CREATOR, ScfTokens.DC_CREATOR_EDITOR));
            eventToc = new ChapterSectionTOC();
            pdfDocument = new Document(PageSize.A4, 50, 50, 50, 50);


            try {
                pdfWriter = PdfWriter.getInstance(pdfDocument, bo);
                pdfWriter.setLinearPageMode();
                pdfWriter.setPageEvent(eventHeader);
                pdfWriter.setPageEvent(eventToc);
            } catch (DocumentException e) {
                Context.getInstance().getLogger().error(ExceptionUtils.getStackTrace(e));
                return;
            }

            pdfDocument.open();

            // Set the default XmlWorker:
            this.pdfPackagerUtils = new PdfPackagerUtils(pdfDocument, pdfWriter);
            try {
                this.mainXmlWorker = this.pdfPackagerUtils.getXmlWorkerInstance(Context.getInstance().pdfCss);
            } catch (IOException e) {
                Context.getInstance().getLogger().error("Encoding problem for XmlWorkerInstance", e);
            }

            Context.getInstance()
                    .getLogger()
                    .debug("------------------- PDF EBOOK FILE OPENED------------------");


        }

        /**
         * Closes the PDF document
         */
        private void closePdfFile(BufferedOutputStream bo) {
            setPdfProperties();
            pdfDocument.close();
            pdfWriter.flush();
            pdfWriter.close();
            try {
                bo.flush();
                bo.close();
            } catch (IOException e) {
                Context.getInstance().getLogger().debug(e);
            }
        }

        /**
         * Composes the ebook in PDF format
         */
        void composePdfEbook() {
            bookmarkMap = new LinkedHashMap<>();

            // Test for the IndexTree

            IndexTree root = IndexTree.buildTree(bookReader.getContentList().getIntList());
            IndexTree.debugPrintTree(root);

            //IndexTree.debugPrintTree(root);
            try {

                composeContentWithChapSec(1, root);
                Context.getInstance().getLogger().debug("PDF Contents created");

                createFirstBookPart();
                createDisclaimerPage();

                Context.getInstance().getLogger().debug("PDF Chapter created");

            } catch (DocumentException e) {
                Context.getInstance().getLogger().error(ExceptionUtils.getStackTrace(e));
            }

            Context.getInstance().getLogger().debug("------------------- PDF: COMPOSED ------------------");
        }


        /**
         * Creates the Cover page
         */
        private void createCoverPage() throws DocumentException {
            String pdfTitle = bookReader.getEleValue(ScfTokens.DC_TITLE);
            String pdfTitleMore = bookReader.getEleValue(ScfTokens.DC_TITLE + "2");
            String title1 = bookReader.getEleValue(ScfTokens.DC_TITLE + "1");
            Font nf;
            Paragraph titleP;

            eventHeader.hideHeader = true;
            pdfDocument.newPage();

            pdfTitle = StringUtils.abbreviate(pdfTitle, 40);


            // Search the logo reference in the SCF file
            String logoUrl = bookReader.getMetadataList().searchMetaContent(Context.getInstance().pdfEbookLogoMetaName);
            putImageIntoPDF(logoUrl, 80, Element.ALIGN_CENTER);

            this.pdfPackagerUtils.addManyNewLines(1);

            // Title 1
            if (title1 != null) {
                nf = BIG_FONT[2];
                titleP = new Paragraph(new Chunk(title1, nf));
                titleP.setAlignment(Chunk.ALIGN_CENTER);
                pdfDocument.add(titleP);
            }

            this.pdfPackagerUtils.addManyNewLines(3);

            // Doc type name
            Font titleFont = BIG_FONT[2];
            String docName = this.bookReader.getMetadataList().searchMetaContent(Context.getInstance().pdfDocNameMetaName);
            titleP = new Paragraph(new Chunk((docName != null) ? docName : "Doc Name: change it!", titleFont));
            titleP.setAlignment(Chunk.ALIGN_CENTER);
            pdfDocument.add(titleP);

            if (pdfTitle != null) {
                // Title
                nf = BIG_FONT_BOLD[2];
                titleP = new Paragraph(new Chunk(pdfTitle, nf));
                titleP.setAlignment(Chunk.ALIGN_CENTER);
                pdfDocument.add(titleP);
                pdfDocument.add(Chunk.NEWLINE);
            }

            if (pdfTitleMore != null) {
                // Title more...
                nf = FONT[3];
                titleP = new Paragraph(new Chunk(pdfTitleMore, nf));
                titleP.setAlignment(Chunk.ALIGN_CENTER);
                pdfDocument.add(titleP);
            }

            this.pdfPackagerUtils.addManyNewLines(10);


            // The date
            nf = FONT[3];
            Paragraph timestampP = new Paragraph(new Chunk(Misc.getNowTimestampLong(), nf));
            timestampP.setAlignment(Chunk.ALIGN_CENTER);
            pdfDocument.add(timestampP);

        }


        private void putImageIntoPDF(String imageUrl, int imageCellH, int align) throws DocumentException {
            if (imageUrl != null && imageUrl.length() > 0) {
                try {
                    Image image = getImage(imageUrl);

                    PdfPTable table = new PdfPTable(1);
                    table.setWidths(new int[]{100});
                    table.setTotalWidth(527);
                    table.getDefaultCell().setFixedHeight(imageCellH);
                    table.setLockedWidth(true);
                    table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                    table.getDefaultCell().setHorizontalAlignment(align);
                    table.addCell(image);

                    pdfDocument.add(table);
                } catch (MalformedURLException e) {
                    Context.getInstance().getLogger().error("URL problems for the PDF image: ");
                } catch (IOException e) {
                    Context.getInstance().getLogger().error("Reading problems for the PDF image: ");
                }
            }
        }

        private void createDisclaimerPage() {
            int PAGE_XPOS = 34;
            float ratio = 0.146f;

            String disclaimer = bookReader.getMetadataList().searchMetaContent(Context.getInstance().pdfDisclaimerMetaName);
            if (disclaimer == null) {
                disclaimer = "";
            }

            int PAGE_YPOS_FOOTER = (int) (ratio * disclaimer.length());
            if (PAGE_YPOS_FOOTER < 80)
                PAGE_YPOS_FOOTER = 80;

            eventHeader.hideHeader = true;

            pdfDocument.newPage();
            PdfPTable table = new PdfPTable(2);
            try {
                table.setWidths(new int[]{10, 90});
                table.setTotalWidth(527);
                table.setLockedWidth(true);
                table.getDefaultCell().setFixedHeight(PAGE_YPOS_FOOTER - 20);
                table.getDefaultCell().setBorder(Rectangle.TOP);
                table.getDefaultCell().setBorderColor(THE_COLOR);
                table.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.getDefaultCell().setPadding(10);


                String logoDptInUrl = bookReader.getMetadataList().searchMetaContent(Context.getInstance().pdfEbookDptMetaName);
                try {
                    if (logoDptInUrl != null && logoDptInUrl.length() > 0) {
                        Image image = getImage(logoDptInUrl);
                        table.addCell(image);
                    } else {
                        Context.getInstance().getLogger().info("PDF cover logo image is not set");
                        table.addCell("---");
                    }
                } catch (MalformedURLException e) {
                    Context.getInstance().getLogger().error(format("URL problems for the PDF dept. logo image: '%s'",logoDptInUrl));
                    table.addCell("---");
                } catch (IOException e) {
                    Context.getInstance().getLogger().error(format("Reading problems for the PDF cover logo image: '%s'",logoDptInUrl));
                    table.addCell("---");
                }

                Font nf = new Font(FONT[4]);
                nf.setColor(THE_COLOR);
                Paragraph titleP = new Paragraph(new Chunk(disclaimer, nf));
                titleP.setAlignment(Chunk.ALIGN_JUSTIFIED);
                table.addCell(titleP);

                table.writeSelectedRows(0, -1, PAGE_XPOS, PAGE_YPOS_FOOTER, pdfWriter.getDirectContent());
            } catch (DocumentException de) {
                Context.getInstance().getLogger().error(de.getMessage());
                throw new ExceptionConverter(de);
            }


        }


        /***
         * Creates the first section of the PDF ebook
         */
        private void createFirstBookPart()
                throws DocumentException {
            pdfDocument.newPage();
            int toc = pdfWriter.getPageNumber();
            createCoverPage();
            createTocPages();
            reorderPages(toc);
        }

        /**
         * Reorder pages: from toc until the end in the first places
         */
        private void reorderPages(int toc) throws DocumentException {
            // always go to a new page before reordering pages.
            pdfDocument.newPage();
            // get the total number of pages that needs to be reordered
            int total = pdfWriter.reorderPages(null);
            // change the order
            int[] order = new int[total];
            for (int i = 0; i < total; i++) {
                order[i] = i + toc;
                if (order[i] > total)
                    order[i] -= total;
            }

            // apply the new order
            pdfWriter.reorderPages(order);
        }

        /**
         * Creates the Table of Contents of the ebook
         */
        private void createTocPages() throws DocumentException {
            pdfDocument.newPage();
            String tocName = this.bookReader.getMetadataList().searchMetaContent(Context.getInstance().pdfTocNameMetaName);
            Paragraph titleP = new Paragraph(new Chunk((tocName != null) ? tocName : "Toc Name: change it!", FONT[0]));
            titleP.setAlignment(Chunk.ALIGN_CENTER);
            pdfDocument.add(titleP);
            pdfDocument.add(Chunk.NEWLINE);

            for (PdfPTable tableRow : eventToc.table) {
                pdfDocument.add(tableRow);
            }
        }


        /**
         * Adds Contents with Chapters and Sections bookmarks
         */
        private void composeContentWithChapSec(int chapter, IndexTree root) {
            coreComposition(chapter, root, 0, null);
        }


        /**
         * Render the Section title on the Page inside the book
         */
        private Paragraph renderSectionTitle(String sectionName) {
            //int leftSpace = 18*level;

            int leftSpace = 0;

            Chunk secTitle = new Chunk(sectionName, FONT_BOLD[0]);
            Paragraph secPara = new Paragraph(secTitle);
            secPara.setAlignment(Chunk.ALIGN_CENTER);
            secPara.setIndentationLeft(leftSpace);

            return secPara;
        }

        /***
         *
         * Adds Contents with Chapters and Sections bookmarks
         *
         */
        private void coreComposition(int chapter, IndexTree root, int level, Section parentSection) {
            Section currentSection = null;
            String nodeTreeLabel = root.nodeLabel;
            if (nodeTreeLabel != null) {
                Paragraph para = renderSectionTitle(nodeTreeLabel);
                Chunk chunkTitle = para.getChunks().get(0);
                currentSection = createChapterOrSection(chapter, parentSection, para);
                chunkTitle.setLocalDestination(currentSection.getTitle().getContent());

                try {
                    createSectionPage(currentSection);
                } catch (DocumentException e) {
                    Context.getInstance().getLogger().error(ExceptionUtils.getStackTrace(e));
                }
            }

            // it loops through the Content children
            for (ContentItem ci : root.childrenContentList) {
                if (ci.getTocName() != null) {
                    Chunk secTitle = new Chunk(ci.getTocName(), FONT_BOLD[0]);
                    Paragraph secTitleP = new Paragraph(secTitle);
                    Section itemSection = createChapterOrSection(chapter++, currentSection, secTitleP);
                    secTitle.setLocalDestination(itemSection.getTitle().getContent());
                    secTitleP.setAlignment(Chunk.ALIGN_LEFT);

                    // Main Title of the Page
                    try {
                        createLastSection(itemSection, ci);
                    } catch (DocumentException e) {
                        Context.getInstance().getLogger().error("Last section problems in PDF ebook: " + ExceptionUtils.getStackTrace(e));
                    }

                    // Add the Header to the page
                    addHeader(pdfWriter.getPageNumber(), titleInHeader, itemSection.getTitle().getContent(), false);

                    // add the Content
                    addContentAsPdf(ci);

                    pdfDocument.newPage();
                }
            }

            // it loops through the Path (subTree) children
            for (IndexTree child : root.childrenPathList) {
                coreComposition(chapter++, child, level + 1, currentSection);
            }

        }

        /**
         * Creates a new page with the title of the section
         */
        void createSectionPage(Section currentSection) throws DocumentException {
            // New lines
            pdfDocument.add(new Paragraph(new Chunk("\n\n\n\n\n\n", FONT_BOLD[0])));


            // The reference to the section type
            //Paragraph para = new Paragraph(new Chunk("Sezione\n\n\n", FONT[0]));
            //Paragraph para = new Paragraph(new Chunk("\n\n\n", FONT[0]));
            //para.setAlignment(Chunk.ALIGN_CENTER);
            //pdfDocument.add(para);
            pdfDocument.add(new Paragraph(new Chunk("\n\n\n", FONT_BOLD[0])));


            // Adds the current Section
            pdfDocument.add(currentSection);
            //paintArrow(level);

            // Adds the header
            addHeader(pdfWriter.getPageNumber(), titleInHeader, currentSection.getTitle().getContent(), false);

            // Jumps to a new page
            pdfDocument.newPage();
        }

        private Section createChapterOrSection(int chapter,
                                               Section parentSection, Paragraph para) {
            Section currentSection;
            if (parentSection == null) {
                Chapter aChapterRoot = new Chapter(para, chapter);
                aChapterRoot.setTriggerNewPage(false);
                currentSection = aChapterRoot;
            } else {
                currentSection = parentSection.addSection(para);
            }
            return currentSection;
        }

        /**
         * Paint the Arrow
         */
        private void paintArrow(int level) {
            //int leftSpace = 18*level;

            String logoUrl = bookReader.getMetadataList().searchMetaContent(Context.getInstance().pdfEbookArrowMetaName);
            if (logoUrl != null) {
                try {
                    Image image = getImage(logoUrl);
                    image.scaleAbsolute(20, 20);
                    //Paragraph imagePara = new Paragraph(new Chunk(image,0,-5));
                    //imagePara.setIndentationLeft(leftSpace);

                    pdfDocument.add(image);
                } catch (IOException | DocumentException e) {
                    Context.getInstance().getLogger().error(ExceptionUtils.getStackTrace(e));
                }


            }
        }

        /**
         * Creates the last section
         */
        private void createLastSection(Section currentSection, ContentItem ci)
                throws DocumentException {
            // Adds a table only for the line

            PdfPTable table = new PdfPTable(1);
            table.setWidths(new int[]{100});
            table.setTotalWidth(527);
            table.getDefaultCell().setFixedHeight(2);
            table.getDefaultCell().setBorderColor(THE_COLOR);
            table.setLockedWidth(true);
            table.getDefaultCell().setBorder(Rectangle.BOTTOM);
            table.addCell("");

            pdfDocument.add(currentSection);
            table.setSpacingBefore(10);
            pdfDocument.add(table);
            table.setSpacingBefore(0);
            pdfDocument.add(table);

            addLinkToDocument(ci);


        }

        /**
         * Add the content pointed in ContentItem as a PDF part into the final ebook
         */
        private void addContentAsPdf(ContentItem contentToAdd) {
            PdfContentByte contentByte = this.pdfWriter.getDirectContent();

            if (contentToAdd.getContentMediaType().equals(Context.PDF_MIMETYPE)) {
                byte[] bArray = contentToAdd.getByteContent();
                    RandomAccessSourceFactory rs = new RandomAccessSourceFactory();
                    RandomAccessFileOrArray ra = null;
                    if (bArray != null) {
                        ra = new RandomAccessFileOrArray(rs.createSource(bArray));
                    }
                    // XXX: If there is not any content in the ContentItemtToAdd, try to reach the URL: it's a useful trick to minimize memory
                    else if (contentToAdd.getContentUrl() != null) {
                        try {
                            ra = new RandomAccessFileOrArray(rs.createSource(new URL(contentToAdd.getContentUrl())));
                        } catch (IOException e) {
                            Context.getInstance().getLogger().error("URL content and byte array content are not readable: "+contentToAdd.getContentUrl());
                        }
                    } else
                        Context.getInstance().getLogger().error("URL content and byte array content are not available");

                    if (ra != null) {
                        PdfReader reader;
                        try {
                            reader = new PdfReader(ra, org.apache.commons.codec.binary.StringUtils.getBytesUtf8(Context.getInstance().pdfPassword));
                            int n = reader.getNumberOfPages();
                            try {
                                for (int pageCount = 0; pageCount < n; pageCount++) {
                                    PdfImportedPage page = pdfWriter.getImportedPage(reader, pageCount + 1);
                                    pdfDocument.newPage();
                                    contentByte.addTemplate(page, 0, 0);
                                }
                            } catch (java.lang.IllegalArgumentException e) {
                                Context.getInstance().getLogger().error("The PDF is probably protected by a password: "+ e.getMessage());
                            }
                            pdfDocument.newPage();
                            //copy.freeReader(reader);
                            //reader.close();
                        } catch (IOException e) {
                            Context.getInstance().getLogger().error("PdfReader reading problem: "+ e.getMessage());
                        }
                    }

            } else if (contentToAdd.getContentMediaType().equals(Context.XHTML_MIMETYPE)) {
                String content = contentToAdd.getStringContent();
                content = JTidyManager.fromNotWellFormedXhtmlToXhtml(content);
                content = adjustHtml(contentToAdd, content);

                try {
                    XMLParser xmp = new XMLParser(true, this.mainXmlWorker);
                    xmp.parse(IOUtils.toBufferedInputStream(IOUtils.toInputStream(content, Context.DEF_ENCODING)));
                    pdfDocument.newPage();

                } catch (IOException e) {
                    Context.getInstance().getLogger().error("XMLWorkerHelper problem!");
                } catch (RuntimeException e) {
                    Context.getInstance().getLogger().error("XMLWorkerHelper problem without error recovery! File URL: " + contentToAdd.getContentUrl()
                            + "\nMessage: " + e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
                }
            }
        }

        /**
         * Add the link to the document
         */
        private void addLinkToDocument(ContentItem contentToAdd) {
            Font font = new Font();
            font.setColor(BaseColor.BLUE);
            font.setStyle(Font.UNDERLINE);

            try {
                String textLinkDoc = bookReader.getMetadataList().searchMetaContent(Context.getInstance().pdfLinkDocTextMetaName);
                String linkDoc = contentToAdd.getContentUrl();
                if (contentToAdd.getContentAltUrl() != null) {
                    // Get the Alternate URL
                    linkDoc = contentToAdd.getContentAltUrl();
                }

                // Add the link only in the case the link is to an external source OR there is an alternate URL
                if (!StringUtils.startsWithIgnoreCase(contentToAdd.getContentUrl().trim(), "file://") || contentToAdd.getContentAltUrl() != null) {
                    Paragraph p = new Paragraph(new Chunk(textLinkDoc, font)
                            .setAnchor(linkDoc));
                    p.setAlignment(Paragraph.ALIGN_CENTER);
                    pdfDocument.add(p);
                }

                pdfDocument.add(new Chunk("\n", FONT[4]));
                pdfDocument.add(new Chunk("\n", FONT[4]));

            } catch (DocumentException e) {
                Context.getInstance().getLogger().error(e);
            }
        }


        /**
         * Manages the Ampersend problem of HTML links
         * and deletes HREF that uses Javascript
         */
        private String adjustHtml(ContentItem ci, String content) {
            StringBuffer sb;

            // Manages the Ampersend
            // Context.getInstance().getLogger().debug("adjustHtml content: "+content);
            sb = manageAmpersand(content);
            content = sb.toString();

            // Manages the HREF with JS
            sb = manageHrefJs(content);
            content = sb.toString();

            // Adjust the relative link for the included images
            sb = managePdfImgs(ci, content);
            content = sb.toString();

            return content;
        }

        /**
         * Manages the IMG refs inside the HTML
         */
        private StringBuffer managePdfImgs(ContentItem ci, String content) {
            StringBuffer sb;
            Matcher matcher;
            sb = new StringBuffer();
            matcher = imgPattern.matcher(content);
            while (matcher.find()) {

                String imgPath = matcher.group(2);

                if (isValidUrl(ci.getContentUrl())) {
                    try {
                        imgPath = new URL(new URL(ci.getContentUrl()), imgPath).toString();
                    } catch (MalformedURLException e) {
                        Context.getInstance().getLogger().error("Malformed URL in HTML for PDF: " + imgPath);
                    }
                } else {
                    imgPath = FilenameUtils.getPath(ci.getContentUrl()) +FilenameUtils.getName(imgPath);
                }

                if (imgPath != null) {
                    matcher.appendReplacement(sb, "$1" + imgPath);
                }
            }
            matcher.appendTail(sb);
            return sb;
        }

        /**
         * Manages the HREF with Javascript inside
         */
        private StringBuffer manageHrefJs(String content) {
            StringBuffer sb = new StringBuffer();
            Matcher matcher = patternHrefJavascript.matcher(content);
            while (matcher.find()) {
                String group = matcher.group(2);
                if (group != null) {
                    matcher.appendReplacement(sb, group);
                }
            }
            matcher.appendTail(sb);
            return sb;
        }

        /**
         * Manages the Ampersand char inside the HTML
         */
        private StringBuffer manageAmpersand(String content) {
            StringBuffer sb;
            sb = new StringBuffer();
            Matcher matcher = patternHrefAmp.matcher(content);
            while (matcher.find()) {
                String group = matcher.group(2);
                if (group != null) {
                    group = group.replace("&amp;", "&");
                    //Context.getInstance().getLogger().debug("$1" + group.replace("$", "\\$") + "$3");
                    matcher.appendReplacement(sb, "$1" + group.replace("$", "\\$") + "$3");
                }
            }
            matcher.appendTail(sb);
            return sb;
        }


        /**
         * Adds the page Header and Footer
         */
        private void addHeader(int page, String title, String tocName, boolean newPage) {
            Context.getInstance().getLogger().debug("Page number: " + page + " - tocName: " + tocName);
            bookmarkMap.put(String.valueOf(page), tocName);
            eventHeader.setTocHeader(title, tocName, String.valueOf(page));
            if (newPage) {
                pdfDocument.newPage();
            }

        }

        /**
         * Sets the PDF properties
         */
        private void setPdfProperties() {
            String pdfTitle = bookReader.getEleValue(ScfTokens.DC_TITLE);
            String pdfEditor = bookReader.getEleValue(ScfTokens.DC_CREATOR, ScfTokens.DC_CREATOR_EDITOR);
            String pdfAuthor = bookReader.getEleValue(ScfTokens.DC_CREATOR, ScfTokens.DC_CREATOR_AUTHOR);
            String pdfLanguage = bookReader.getEleValue(ScfTokens.DC_LANGUAGE);
            String pdfSubject = bookReader.getEleValue(ScfTokens.DC_SUBJECT);

            if (pdfEditor != null)
                pdfDocument.addCreator(pdfEditor);

            if (pdfTitle != null)
                pdfDocument.addTitle(pdfTitle);

            if (pdfAuthor != null)
                pdfDocument.addAuthor(pdfAuthor);

            if (pdfLanguage != null)
                pdfDocument.addLanguage(pdfLanguage);

            if (pdfSubject != null)
                pdfDocument.addSubject(pdfSubject);
        }

        /**
         * Event Handler to put an header and footer in every page
         * <p>
         * example: http://stackoverflow.com/questions/6018299/itext5-1-0-set-header-and-footer-in-document
         */
        class HeaderEvent extends PdfPageEventHelper {

            // Unità di misura in Inch!!!
            private static final int SECOND_COLUMN_PERCENTAGE = 20;
            private static final int FIRST_COLUMN_PERCENTAGE = 80;
            private static final int PAGE_XPOS = 34;
            private static final int PAGE_YPOS_HEADER = 833;
            private static final int PAGE_YPOS_FOOTER = 20;
            boolean hideHeader;
            private String headerRight;
            private String localDestination;
            private final String editor;
            private String headerLeft;

            /**
             * Constructor
             */
            HeaderEvent(String editor) {
                this.editor = Objects.requireNonNullElse(editor, "N/A");
            }

            /**
             * Allows us to change the content of the header.
             *
             * @param headerRight      The new header String
             * @param localDestination v
             */
            void setTocHeader(String headerLeft, String headerRight, String localDestination) {
                this.headerLeft = headerLeft;
                this.headerRight = headerRight;
                this.localDestination = localDestination;
            }

            /**
             * Adds a header to every page
             *
             * @see com.itextpdf.text.pdf.PdfPageEventHelper#onEndPage(
             *com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
             */
            public void onEndPage(PdfWriter writer, Document document) {

                if (!hideHeader && headerRight != null) {
                    addHeader(writer);
                    addFooter(writer);
                }
            }


            /**
             * Adds the page header
             */
            private void addHeader(PdfWriter writer) {
                PdfPTable table = new PdfPTable(2);
                try {
                    table.setWidths(new int[]{50, 50});
                    table.setTotalWidth(527);
                    table.setLockedWidth(true);
                    table.getDefaultCell().setFixedHeight(40);
                    table.getDefaultCell().setBorder(Rectangle.BOTTOM);
                    //table.getDefaultCell().setBorder(Rectangle.BOX);
                    table.getDefaultCell().setBorderColor(THE_COLOR);

                    Font nf = new Font(FONT[4]);
                    nf.setColor(THE_COLOR);

                    // Left part
                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    table.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);
                    //table.getDefaultCell().setBorder(Rectangle.BOX);
                    table.getDefaultCell().setLeading(0f, 1.2f);
                    //table.getDefaultCell().setPadding(3);
                    Chunk cLeft = new Chunk(headerLeft, nf);
                    Phrase pLeft = new Phrase();
                    pLeft.add(cLeft);
                    table.addCell(pLeft);

                    // Right part
                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);
                    table.getDefaultCell().setLeading(0f, 1.2f);
                    //table.getDefaultCell().setPadding(3);
                    //table.getDefaultCell().setBorder(Rectangle.BOTTOM);
                    //table.getDefaultCell().setBorder(Rectangle.BOX);
                    Chunk cRight = new Chunk(headerRight, nf);
                    if (this.localDestination != null) {
                        cRight.setGenericTag(this.localDestination);
                        cRight.setLocalDestination(this.localDestination);
                        //this.localDestination = null;
                    }
                    Phrase pRight = new Phrase();
                    pRight.add(cRight);
                    table.addCell(pRight);

                    table.writeSelectedRows(0, -1, PAGE_XPOS, PAGE_YPOS_HEADER, writer.getDirectContent());
                } catch (DocumentException de) {
                    throw new ExceptionConverter(de);
                }
            }

            /**
             * Adds the page footer
             */
            private void addFooter(PdfWriter writer) {
                PdfPTable table = new PdfPTable(2);
                try {
                    table.setWidths(new int[]{FIRST_COLUMN_PERCENTAGE, SECOND_COLUMN_PERCENTAGE});
                    table.setTotalWidth(527);
                    table.setLockedWidth(true);
                    table.getDefaultCell().setFixedHeight(20);
                    table.getDefaultCell().setBorderColor(THE_COLOR);
                    table.getDefaultCell().setBorder(Rectangle.TOP);

                    Font nf = new Font(FONT[4]);
                    nf.setColor(THE_COLOR);
                    Phrase p = new Phrase(new Chunk(this.editor, nf));
                    table.addCell(p);

                    // Second cell with the page number
                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);

                    Phrase p1 = new Phrase(new Chunk(format("Pag. %d", writer.getPageNumber()), nf));

                    table.addCell(p1);
                    table.writeSelectedRows(0, -1, PAGE_XPOS, PAGE_YPOS_FOOTER, writer.getDirectContent());
                } catch (DocumentException de) {
                    throw new ExceptionConverter(de);
                }
            }

        }

        /**
         * The chapter event handler
         *
         * @author roberto.battistoni
         */
        class ChapterSectionTOC extends PdfPageEventHelper {
            final List<PdfPTable> table = new ArrayList<>();

            private int pagenumber = 0;

            public void onChapter(PdfWriter writer, Document document,
                                  float position, Paragraph title) {
                onSection(writer, document, position, 1, title);

            }


            public void onSection(PdfWriter writer, Document document,
                                  float position, int depth, Paragraph title) {

                PdfPTable tableRow = new PdfPTable(1);
                try {
                    tableRow.setWidths(new int[]{100});
                    tableRow.setTotalWidth(527);
                    tableRow.setLockedWidth(true);
                    tableRow.getDefaultCell().setFixedHeight(20);
                    tableRow.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                    tableRow.getDefaultCell().setBorderColor(BaseColor.GRAY);
                    String pageNumInToc = Integer.toString(pagenumber);

                    tableRow.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    Chunk titleC = new Chunk(title.getContent(), FONT[4]);

                    Paragraph titleP = new Paragraph(titleC.setLocalGoto(pageNumInToc));
                    titleP.setIndentationLeft(18 * (depth - 1));
                    titleP.add(new Chunk(new DottedLineSeparator()).setLocalGoto(pageNumInToc));
                    titleP.add(new Chunk(pageNumInToc).setLocalGoto(pageNumInToc));

                    // Cell to have the indentation working
                    PdfPCell cell = new PdfPCell();
                    cell.setBorder(Rectangle.NO_BORDER);
                    cell.setBorderColor(BaseColor.GRAY);
                    cell.addElement(titleP);
                    tableRow.addCell(cell);

                    table.add(tableRow);
                } catch (DocumentException de) {
                    throw new ExceptionConverter(de);
                }
            }

            public void onStartPage(PdfWriter writer, Document document) {
                pagenumber++;
            }

        }


    }


}
