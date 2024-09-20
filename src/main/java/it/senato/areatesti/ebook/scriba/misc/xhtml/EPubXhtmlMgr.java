package it.senato.areatesti.ebook.scriba.misc.xhtml;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.misc.xml.JTidyManager;

import java.util.regex.Pattern;


/**
 * Manages the Xhtml included in the epub
 */
public class EPubXhtmlMgr {
    private static final String prefixRegEx = "<a(\\s*[^>]*\\s+href=\")";
    private static final String postfixRegEx = "(/[^>]*)";
    private static final String prefixRepl = "<a$1";
    private static final String suffix = "\"";

    /**
     * Converts an HTML content to XHTML content
     *
     * @return the xhtml content
     */
    public static String convertToXhtml(String stringContent) {
        return JTidyManager.fromHtmlToXhtml(stringContent);
    }


    /**
     * Substitutes the template to the CSS with correct CSS
     *
     * @return the outcome
     */
    public static String manageCssReference(int nestedLevel, String htmlText,
                                            String cssStyleName) {
        String content = htmlText;
        //content = content.replace(Context.cssPlaceholder, getNestedPath(nestedLevel).toString() + cssStyleName);

        // if in the placeholder or stylename there is a * char, this means a way to manage multiple styles with multiple ph and style names
        content = Pattern.compile(Context.getInstance().cssPlaceholder).matcher(content).replaceAll(getNestedPath(nestedLevel).toString() + cssStyleName);

        return content;
    }

    /**
     * Get the nested partial path
     */
    private static StringBuilder getNestedPath(int nestedLevel) {
        String exp = ".." + Context.PATH_SEP;
        StringBuilder sb = new StringBuilder();
        sb.append(exp.repeat(Math.max(0, nestedLevel)));
        return sb;
    }

    /**
     * Managing the Logo reference
     */
    public static String manageLogoReference(int nestedLevel, String htmlText,
                                             String logoFileName) {
        String content = htmlText;
        content = content.replace(Context.getInstance().sectionlogoPlaceholder, getNestedPath(nestedLevel).toString() + logoFileName);
        return content;
    }

    /**
     * Substitutes the relative link with absolute link
     *
     * @return the outcome
     */
    public static String manageLinkReference(String htmlText,
                                             String baseUrl, String oldBaseUrl) {
        Context.getInstance().getLogger().debug("Update relative Links");
        String res = substituteRelativeLinks(htmlText, baseUrl);
        res = substituteAbsoluteLinks(res, baseUrl, oldBaseUrl);
        return res;
    }

    /**
     * Substitutes only the relative links as "/loc/..."
     */
    private static String substituteRelativeLinks(String htmlText,
                                                  String baseurl) {
        String regexLink = prefixRegEx + postfixRegEx + suffix;

        String replace = prefixRepl + baseurl + "$2" + suffix;
        return Pattern.compile(regexLink, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(htmlText).replaceAll(replace);
    }

    /**
     * Substitutes only the absolute links as "/loc/..."
     */
    private static String substituteAbsoluteLinks(String htmlText, String baseurl, String oldBaseUrl) {
        String regexLink = prefixRegEx + oldBaseUrl + postfixRegEx + suffix;

        String replace = prefixRepl + baseurl + "$2" + suffix;
        return Pattern.compile(regexLink, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(htmlText).replaceAll(replace);
    }


}
