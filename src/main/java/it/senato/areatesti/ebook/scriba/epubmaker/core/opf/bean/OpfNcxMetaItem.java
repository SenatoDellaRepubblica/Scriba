package it.senato.areatesti.ebook.scriba.epubmaker.core.opf.bean;

import it.senato.areatesti.ebook.scriba.Context;

/**
 * Defines a meta item in the NCX
 */
public class OpfNcxMetaItem {
    private final String name;
    private final String content;

    /**
     * Constructor
     */
    public OpfNcxMetaItem(String name, String content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public String toString() {
        return "<meta name=\"" + name + "\" content=\"" + content + "\"/>"
                + Context.NEWLINE;
    }

}
