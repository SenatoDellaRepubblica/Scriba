package it.senato.areatesti.ebook.scriba.plugin.senato.base;

import it.senato.areatesti.ebook.scriba.EbookType;
import it.senato.areatesti.ebook.scriba.packaging.PackageMaker;
import it.senato.areatesti.ebook.scriba.plugin.base.AbstractPlugin;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;

/**
 * Manages a generic Document for Italian Senate
 *
 * @author roberto.battistoni
 */
public abstract class AbstractTestiSenatoPlugin extends AbstractPlugin {

    /**
     * Constructor
     */
    protected AbstractTestiSenatoPlugin(Object[] initArgs) {
        super(initArgs);
    }

    protected String updateHtmlForDivProblemsInIText(String htmlContent) {
        if (this.eBookType == EbookType.PDF) {
            htmlContent = manageContentForPDFType(htmlContent);
        }
        return htmlContent;
    }

    protected ContentItem getContentItem(ContentItem contentItem, byte[] bContent, String xslPath) throws UnsupportedEncodingException {
        if (bContent == null) {
            contentItem = setErrorPage(contentItem);
        } else {
            // Sets the original encoding: it depends on the document encoding and we know this one
            contentItem.setByteContent(bContent);

            // sets the Tidy and Xsl attribute: it must be defined into the code and override the xml strings
            contentItem.setNeededTidy(true);
            contentItem.setNeededXsl(false);
            if (xslPath!=null)
                contentItem.setNeededXsl(true);

            // Applies the XSL
            contentItem = PackageMaker.adjustXslTidyCssLink(contentItem, xslPath);

            // sets the Tidy and Xsl attribute: it is needed to avoid the default Tidy and Xsl processing for XHTML page
            contentItem.setNeededTidy(false);
            contentItem.setNeededXsl(false);

            // cleans the HTML
            contentItem.setStringContent(adjustConvertedHtml(contentItem.getStringContent()));
        }
        return contentItem;
    }


    private String manageContentForPDFType(String htmlContent) {
        Document doc = Jsoup.parse(htmlContent);
        correctDivProblems(doc);
        return doc.html();
    }

    /**
     * Patches the problem with iText that truncates TABLE in DIV
     * (removes the DIV surrounding the table)
     */

    // Patch for the iText xmlWorker bug on TABLE truncated in DIV
    private void correctDivProblems(Document doc) {
        Elements elements = doc.select("div:has(table)");
        for (org.jsoup.nodes.Element e : elements) {
            e.tagName("fakediv");
        }

        elements = doc.select("div:has(img)");
        for (org.jsoup.nodes.Element e : elements) {
            removeIMG_Width(e);
            e.tagName("fakediv");
        }
    }

    /**
     * Removes the "width" attribute from the IMG tags
     */

    // Patch for the iText xmlWorker bug on Image width
    protected void removeIMG_Width(Element ele) {
        Elements imgTags = ele.getElementsByTag("img");
        if (imgTags != null && imgTags.size() > 0) {
            for (Element e : imgTags) {
                // XXX: this patch works only for xmlWorker 5.4.5 but it breaks the next patch for the fit of the image in the cell
				/*
				if (!e.attributes().hasKey("height"))
				{
					e.attributes().put("height", "100%");
				}
				*/
                // XXX: this patch works only for xmlWorker 5.4.3
                e.attributes().remove("width");
            }
        }
    }

}
