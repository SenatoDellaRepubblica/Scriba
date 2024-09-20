
package it.senato.areatesti.ebook.scriba.scf.file;

/**
 * Defines the SCF (Scriba Content File) tokens
 */
public class ScfTokens {
    /**
     * Prefix for named arguments of Plugins
     */
    public static final String CONTENT_PLUGIN_ARGS_PREFIX = "plugin-arg-";
    public static final String DC_TITLE = "title";
    public static final String DC_CREATOR = "creator";
    public static final String DC_LANGUAGE = "language";
    public static final String DC_IDENTIFIER = "identifier";
    public static final String DC_SUBJECT = "subject";
    public static final String DC_DATE = "date";
    public static final String DC_CREATOR_AUTHOR = "aut";
    public static final String DC_CREATOR_EDITOR = "edt";
    static final String CONTENTS_TOC_ID = "tocId";
    static final String CONTENT_PDF_TO_HTML = "pdfToHtml";
    static final String CONTENT_IS_NEEDED_XSL = "isNeededXsl";
    static final String CONTENT_IS_NEEDED_TIDY = "isNeededTidy";
    static final String CONTENT_IS_IN_SPINE = "isInSpine";
    static final String CONTENT_PLUGIN = "plugin";
    static final String CONTENT_CONTENT_MEDIA_TYPE = "contentMediaType";
    static final String CONTENT_CONTENT_URL = "contentUrl";
    static final String CONTENT_CONTENT_ALT_URL = "contentAltUrl";
    static final String CONTENT_TOC_NAME = "tocName";
    static final String CONTENT_PACKAGE_ID = "packageId";
    static final String CONTENT_PACKAGE_FILE = "packageFile";
    static final String CONTENT_PACKAGE_PATH = "packagePath";
    static final String METAITEM_DESTINATION = "destination";
    static final String METAITEM_CONTENT = "content";
    static final String METAITEM_NAME = "name";

    // Only for not binary Mimetype files
    static final String METAITEM_DC = "dc";
    static final String METAITEM_META = "meta";
    static final String METAITEM_ROLE = "role";
    static final String METAITEM_ID = "id";
    static final String METAITEM_ELENAME = "elename";
    static final String METAITEM_ELETYPE = "eletype";
    static final String METAITEM_NAME_COVER = "cover";
    // Specify for what kind of ebook the content has to be considered
    static final String EBOOK_TYPES = "ebookTypes";
    // not used yet! For future uses
    static final String CONTENT_ENCODING = "encoding";


}
