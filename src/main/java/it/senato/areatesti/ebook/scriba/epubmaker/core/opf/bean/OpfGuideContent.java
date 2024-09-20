package it.senato.areatesti.ebook.scriba.epubmaker.core.opf.bean;

import it.senato.areatesti.ebook.scriba.Context;

/**
 * The content in the Guide section of OPF
 */
public class OpfGuideContent {
    private final String title;
    private final String href;

    /**
     * Constructor
     */
    public OpfGuideContent(String title, String href) {
        this.title = title;
        this.href = href;
    }

    @Override
    public String toString() {
        return "<guide>" + Context.NEWLINE +
                "<reference type=\"cover\" title=\"" + title + "\" href=\""
                + href + "\" />" +
                Context.NEWLINE +
                "</guide>" + Context.NEWLINE;
    }
}
