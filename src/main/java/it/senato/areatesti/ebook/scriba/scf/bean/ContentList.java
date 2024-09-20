package it.senato.areatesti.ebook.scriba.scf.bean;

import it.senato.areatesti.ebook.scriba.scf.bean.base.AbstractItemList;

import java.util.ArrayList;

/**
 * List of ContentItem
 */
public class ContentList extends AbstractItemList {
    private String tocId;

    /**
     * Constructor
     */
    public ContentList() {
        super();
        this.intList = new ArrayList<>();
    }

    /**
     * Gets the TOC ID
     *
     * @return the tocId
     */
    public String getTocId() {
        return tocId;
    }

    /**
     * Sets the TOC ID
     *
     * @param tocId the tocId to set
     */
    public void setTocId(String tocId) {
        this.tocId = tocId;
    }


}
