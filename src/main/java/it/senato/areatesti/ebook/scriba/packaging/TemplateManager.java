
package it.senato.areatesti.ebook.scriba.packaging;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.misc.Misc;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentItem;
import org.apache.commons.text.StringEscapeUtils;

import java.util.Locale;
import java.util.Map.Entry;
import java.util.ResourceBundle;

/**
 * manages the template content
 */
public class TemplateManager {

    private static String language = "";

    /**
     * Sets Language
     */
    public static void setLang(String lang) {

        language = lang;
    }

    /**
     * Gets Language Translations
     */
    private static ResourceBundle getMessages() {

        if (language.isEmpty())
            setLang("it");

        return ResourceBundle.getBundle("i18n.MessagesBundle", new Locale(language));
    }

    /**
     * Gets the custom template for PDF thumbnail managed by a plugin
     */
    static String getCustomTemplateCoverPdf(ContentItem contentItemOfPdfRef) {
        String extPdfXhtml;

        extPdfXhtml = Misc.getContentProp(contentItemOfPdfRef.getPhXhtmlPageForPdf(),
                Context.DEF_ENCODING);

        for (Entry<String, String> entry : contentItemOfPdfRef.getMapToSubstInXhtmlPhForPdf().entrySet()) {
            if (entry.getKey().contains("%html:"))
                extPdfXhtml = extPdfXhtml.replace(entry.getKey().replace("%html:", "%"), entry.getValue());
            else
                extPdfXhtml = extPdfXhtml.replace(entry.getKey(), StringEscapeUtils.escapeHtml4(entry.getValue()));
        }

        return extPdfXhtml;
    }

    /**
     * Template for the cover of PDF thumbs
     */
    static String getDefaultTemplateCoverPdf(String titolo, String titoletto,
                                             String linkExtPdf) {
        String extPdfXhtml;
        ResourceBundle messages = getMessages();

        extPdfXhtml = Misc.getContentProp("prop/template/extPdf.templ.xhtml",
                Context.DEF_ENCODING);

        extPdfXhtml = extPdfXhtml.replace("%scheda%", messages.getString("scheda"));
        extPdfXhtml = extPdfXhtml.replace("%visualizza%", messages.getString("visualizza"));
        extPdfXhtml = extPdfXhtml.replace("%titolo%", StringEscapeUtils.escapeHtml4(titolo));
        extPdfXhtml = extPdfXhtml.replace("%titoletto%", StringEscapeUtils.escapeHtml4(titoletto));
        extPdfXhtml = extPdfXhtml.replace("%link%", linkExtPdf);

        return extPdfXhtml;
    }

    /**
     * Gets the error page
     */
    static String getErrorTemplatePage(String errorMessage) {
        String errorPageTempl;
        ResourceBundle messages = getMessages();

        errorPageTempl = Misc.getContentProp("prop/template/errorPage.templ.xhtml", Context.DEF_ENCODING);

        errorPageTempl = errorPageTempl.replace("%titolo%", messages.getString("errore"));
        errorPageTempl = errorPageTempl.replace("%errormessage%", errorMessage);

        return errorPageTempl;
    }

    /**
     * Template for thumb page
     */
    static String getThumbPdfTemplate(String title, String imageSrc,
                                      String imageAlt) {
        String extPdfXhtml;

        extPdfXhtml = Misc.getContentProp("prop/template/thumb.templ.xhtml",
                Context.DEF_ENCODING);

        extPdfXhtml = extPdfXhtml.replace("%titolo%", StringEscapeUtils.escapeHtml4(title));
        extPdfXhtml = extPdfXhtml.replace("%image_src%", imageSrc);
        extPdfXhtml = extPdfXhtml.replace("%image_alt%", imageAlt);

        return extPdfXhtml;
    }

    /**
     * Gets the template for the HTML ebook format Index.html
     */
    public static String getHtmlIndexTemplate(String idxXmlString) {
        String templ;
        ResourceBundle messages = getMessages();

        templ = Misc.getContentProp("prop/template/index.templ.xhtml", Context.DEF_ENCODING);

        templ = templ.replace("%scheda%", messages.getString("scheda"));
        templ = templ.replace("%titolo%", "Indice");
        templ = templ.replace("%titoletto%", "Indice");
        templ = templ.replace("%index_body%", idxXmlString);
        return templ;
    }

    /**
     * Gets the template for the Section Title
     */
    public static String getSectionTemplate(String title, String sectionTitle) {
        String templ;
        ResourceBundle messages = getMessages();

        templ = Misc.getContentProp("prop/template/section.templ.xhtml", Context.DEF_ENCODING);

        templ = templ.replace("%sezione%", messages.getString("sezione"));
        templ = templ.replace("%titolo%", title);
        templ = templ.replace("%titolo_sezione%", sectionTitle);
        return templ;
    }

}
