package it.senato.areatesti.ebook.scriba.zipmaker.bean;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.index.base.IIndexItem;
import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;

/**
 * makes the HTML index
 */
public class HtmlNavPointItem implements IIndexItem {
    private String navLabel;
    private String contentSrc;
    private boolean newLevel;
    private ArrayList<IIndexItem> children;

    /**
     * Default Constructor
     */
    public HtmlNavPointItem() {

    }

    /**
     * Constructor for a NavPoint item in the TOC (it doesn't have any src)
     */
    public HtmlNavPointItem(String navLabel, String contentSrc, boolean newLevel) {
        this.navLabel = StringEscapeUtils.escapeHtml4(navLabel);
        this.contentSrc = contentSrc;
        this.newLevel = newLevel;

        this.children = new ArrayList<>();
    }

    /**
     * Gets the children list
     *
     * @return children as an ArrayList
     */
    public ArrayList<IIndexItem> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (this.contentSrc == null)
            sb.append("<li>").append(navLabel).append("</li>").append(Context.NEWLINE);
        else
            sb.append("<li>").append("<a href=\"").append(this.contentSrc).append("\">").append(navLabel).append("</a></li>").append(Context.NEWLINE);

        if (this.newLevel)
            sb.append("<ol>").append(Context.NEWLINE);

        // Navpoint children
        for (IIndexItem ni : children)
            sb.append(ni.toString()).append(Context.NEWLINE);

        if (this.newLevel)
            sb.append("</ol>").append(Context.NEWLINE);

        return sb.toString();

    }

    @Override
    public String getContentSrc() {
        return this.contentSrc;
    }

    @Override
    public void setContentSrc(String newContentSrc) {
        this.contentSrc = newContentSrc;
    }

    @Override
    public String getNavLabel() {
        return this.navLabel;
    }


}
