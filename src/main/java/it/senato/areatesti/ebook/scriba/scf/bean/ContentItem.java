package it.senato.areatesti.ebook.scriba.scf.bean;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.misc.Misc;
import it.senato.areatesti.ebook.scriba.scf.bean.base.IItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * A generic content item defined in the XML file input (SCF file) of Scriba
 */
public class ContentItem implements IItem {

    /**
     * Could be null if the ContentItem ha not dependeces
     * <p>
     * It is needed to manage the indexes and Section part that must be before ContentItem having this properties null
     */
    private ContentItem dependOn;
    private final String packagePath;
    private final String packagePathEscaped;
    private final String packageId;
    private String packageFile;
    private final String tocName;
    private final String contentUrl;
    private final String contentAltUrl;
    private String contentMediaType;
    private String finalEbookTypes;
    private boolean neededTidyExist;
    private boolean inSpineExist;
    private final boolean coverExist;
    private boolean neededXslExist;
    private String plugin;
    private final boolean pdfToHtml;
    private String contentSrcEncoding;

    private final int nestedLevel;

    private String placeHolderXhtmlPageForPdf;
    private Hashtable<String, String> stringToSubstituteInXhtmlPhForPdf;


    private byte[] byteContent = null;
    private String stringContent = null;
    private boolean spineLinear = true;

    private final HashMap<String, String> pluginArgs;

    /**
     * Constructor for the general case
     *
     * @param finalEbookTypes ebook types for the final ebook
     * @param pdfToHtml       to convert PDF to HTML
     */
    public ContentItem(String packagePath, String packageFile,
                       String packageId, String tocName, String contentUrl, String contentAltUrl,
                       String contentMediaType, String finalEbookTypes, String plugin,
                       String encoding, boolean isCover, boolean isInSpine,
                       boolean isNeededTidy, boolean isNeededXsl, boolean pdfToHtml) {
        this.packagePath = Misc.normalizePackagePath(packagePath);
        this.packagePathEscaped = StringEscapeUtils.escapeXml11(Misc.normalizePackagePath(packagePath));
        this.packageId = packageId;

        this.packageFile = packageFile;
        if (packageFile != null) {
            this.packageFile = Misc.normalizeFileNameSoft(this.packageFile);
        }
        this.contentUrl = contentUrl;
        this.contentAltUrl = contentAltUrl;
        this.contentMediaType = contentMediaType;
        this.finalEbookTypes = finalEbookTypes;
        this.plugin = plugin;
        this.neededTidyExist = isNeededTidy;
        this.neededXslExist = isNeededXsl;
        this.tocName = tocName;
        this.coverExist = isCover;
        this.inSpineExist = isInSpine;
        this.pdfToHtml = pdfToHtml;

        /*
         * Calculates the nested level: for CSS reference
         */
        this.nestedLevel = StringUtils.countMatches(this.packagePath, Context.PATH_SEP);

        /*
         * The optional ARGS passed to the plugin
         */
        this.pluginArgs = new HashMap<>();

    }

    /**
     * Constructor for different mimetype respect of Xhtml
     */
    public ContentItem(String packagePath, String packageFile,
                       String packageId, String tocName, String contentUrl,
                       String contentMediaType) {

        this(packagePath, packageFile, packageId, tocName, contentUrl, null, contentMediaType, null, null, null, false, false, false, false, false);
    }

    /**
     * Constructor for the plugin case
     */
    public ContentItem(String packagePath,
                       String packageId, String tocName, String contentUrl,
                       String plugin) {

        this(packagePath, null, packageId, tocName, contentUrl, null, null, null, null, null, false, false, false, false, false);
    }


    /**
     * @return the pluginArgs
     */
    public HashMap<String, String> getPluginArgs() {
        return pluginArgs;
    }

    /**
     * Gets the package Path escaped for the representation in XML file
     */
    public String getPackagePathEscaped() {
        return packagePathEscaped;
    }

    /**
     * Gets the nested level
     *
     * @return the levels in the TOC
     */
    public int getNestedLevel() {
        return nestedLevel;
    }

    /**
     * Gets the package ID
     *
     * @return the packageId
     */
    public String getPackageId() {
        return packageId;
    }

    /**
     * Gets the media type of the content
     *
     * @return the contentMediaType
     */
    public String getContentMediaType() {
        return contentMediaType;
    }

    /**
     * Sets the content media type
     *
     * @param contentMediaType the contentMediaType to set
     */
    public void setContentMediaType(String contentMediaType) {
        this.contentMediaType = contentMediaType;
    }

    /**
     * Gets the packagePath
     *
     * @return the package path
     */
    public String getPackagePath() {
        return packagePath;
    }

    /**
     * Gets the package file
     *
     * @return the package file
     */
    public String getPackageFile() {
        return packageFile;
    }

    /**
     * Sets the package file
     *
     * @param packageFile the packageFile to set
     */
    public void setPackageFile(String packageFile) {
        this.packageFile = packageFile;
    }

    /**
     * Gets the content url
     *
     * @return the content URL
     */
    public String getContentUrl() {
        return contentUrl;
    }

    /**
     * Gets the alternative content url
     * (an alternative url...)
     *
     * @return the content URL
     */
    public String getContentAltUrl() {
        return contentAltUrl;
    }

    /**
     * @return the dependOn
     */
    public ContentItem getIsDependentOn() {
        return dependOn;
    }

    /**
     * @param dependOn the dependOn to set
     */
    public void setIsDependentOn(ContentItem dependOn) {
        this.dependOn = dependOn;
    }

    /**
     * if it's needed Tidy
     *
     * @return true or false
     */
    public boolean isNeededTidy() {
        return neededTidyExist;
    }

    /**
     * Sets the JTidy processor
     *
     * @param isNeededTidy the isNeededTidy to set
     */
    public void setNeededTidy(boolean isNeededTidy) {
        this.neededTidyExist = isNeededTidy;
    }

    /**
     * if it's needed XSL
     *
     * @return true or false
     */
    public boolean isNeededXsl() {
        return neededXslExist;
    }

    /**
     * Sets the XSL processor
     *
     * @param isNeededXsl the isNeededXsl to set
     */
    public void setNeededXsl(boolean isNeededXsl) {
        this.neededXslExist = isNeededXsl;
    }

    /**
     * is it a cover?
     *
     * @return true or false
     */
    public boolean isCover() {
        return coverExist;
    }

    /**
     * Is it in the spine section?
     *
     * @return true or false
     */
    public boolean isInSpine() {
        return inSpineExist;
    }

    /**
     * Is in the spine section
     */
    public void setInSpine(boolean val) {
        inSpineExist = val;
    }

    /**
     * Is the PDF to HTML set
     *
     * @return the pdfToHtml
     */
    public boolean isPdfToHtml() {
        return pdfToHtml;
    }

    /**
     * Gets the TOC name of the content
     *
     * @return the item name in the TOC
     */
    public String getTocName() {
        return tocName;
    }

    /**
     * Get the content as byte[]
     *
     * @return byte array
     */
    public byte[] getByteContent() {
        return byteContent;
    }

    /**
     * Sets the byte content and the string Content (relative to the default encoding)
     *
     * @param byteContent the byte array
     */
    public void setByteContent(byte[] byteContent) throws UnsupportedEncodingException {
        this.byteContent = byteContent;
        //Context.getInstance().getLogger().debug(String.format("Max/Total/Free Heap space: %s/%s/%s", FileUtils.byteCountToDisplaySize(Runtime.getRuntime().maxMemory()),FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory()),FileUtils.byteCountToDisplaySize(Runtime.getRuntime().freeMemory())));
        this.stringContent = new String(byteContent, Context.DEF_ENCODING);
    }


    /**
     * Gets the plugin class path
     */
    public String getPlugin() {
        return plugin;
    }


    /**
     * Sets the plugin class path
     */
    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the placeholder page for the PDF content
     * <p>
     * When it's processed a PDF file a placeholder page has put before it
     *
     * @return the placeHolderXhtmlPageForPdf
     */
    public String getPhXhtmlPageForPdf() {
        return placeHolderXhtmlPageForPdf;
    }

    /**
     * Sets the placeholder page
     *
     * @param placeHolderXhtmlPageForPdf the placeHolderXhtmlPageForPdf to set
     */
    public void setPhXhtmlPageForPdf(String placeHolderXhtmlPageForPdf) {
        this.placeHolderXhtmlPageForPdf = placeHolderXhtmlPageForPdf;
    }

    /**
     * Gets the Maps for the substitution in the placeholder page for PDF
     *
     * @return the stringToSubstituteInXhtmlPhForPdf
     */
    public Hashtable<String, String> getMapToSubstInXhtmlPhForPdf() {
        return stringToSubstituteInXhtmlPhForPdf;
    }

    /**
     * Gets the Maps for the substitution in the placeholder page for PDF
     *
     * @param stringToSubstituteInXhtmlPhForPdf the stringToSubstituteInXhtmlPhForPdf to set
     */
    public void setMapToSubstInXhtmlPhForPdf(
            Hashtable<String, String> stringToSubstituteInXhtmlPhForPdf) {
        this.stringToSubstituteInXhtmlPhForPdf = stringToSubstituteInXhtmlPhForPdf;
    }

    /**
     * Gets the content source encoding
     */
    public String getContentSrcEncoding() {
        return contentSrcEncoding;
    }

    /**
     * Sets the content source encoding
     *
     * @param contentSrcEncoding the content source enc to set
     */
    public void setContentSrcEncoding(String contentSrcEncoding) {
        this.contentSrcEncoding = contentSrcEncoding;
    }

    /**
     * Gets the string content
     *
     * @return the stringContent
     */
    public String getStringContent() {
        return stringContent;
    }

    /**
     * Sets the string content and the byte Content (relative to the default encoding)
     *
     * @param stringContent the stringContent to set
     */
    public void setStringContent(String stringContent) throws UnsupportedEncodingException {
        this.stringContent = stringContent;
        this.byteContent = stringContent.getBytes(Context.DEF_ENCODING);
    }

    /**
     * the linear attribute value: "yes" or "no"
     */
    public boolean IsSpineLinear() {
        return this.spineLinear;
    }

    /**
     * the linear attribute value: "yes" or "no"
     */
    public void setSpineLinear(boolean val) {
        this.spineLinear = val;
    }

    /**
     * @return the finalEbookTypes
     */
    public String getFinalEbookTypes() {
        return finalEbookTypes;
    }

    /**
     * @param finalEbookTypes the finalEbookTypes to set
     */
    public void setFinalEbookTypes(String finalEbookTypes) {
        this.finalEbookTypes = finalEbookTypes;
    }

}
