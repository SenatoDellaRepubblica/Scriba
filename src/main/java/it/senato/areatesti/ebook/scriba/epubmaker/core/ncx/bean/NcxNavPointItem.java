package it.senato.areatesti.ebook.scriba.epubmaker.core.ncx.bean;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.index.base.IIndexItem;
import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;

/**
 * This is an element of the TOC
 */
public class NcxNavPointItem implements IIndexItem {
    private String navLabel;
    private String contentSrc;

    private String playOrder;
    private String id;

    private ArrayList<IIndexItem> children;

    /**
     * Default Constructor
     */
    public NcxNavPointItem() {

    }

    /**
     * Constructor for a NavPoint item in the TOC (it doesn't have any src)
     */
    public NcxNavPointItem(String navLabel, String contentSrc, int playOrder) {
        this.navLabel = StringEscapeUtils.escapeXml11(navLabel);
        this.contentSrc = contentSrc;
        this.playOrder = Integer.toString(playOrder);
        this.id = "navPointId-" + playOrder;

        this.children = new ArrayList<>();
    }

    /**
     * Gets the children list
     *
     * @return children list
     */
    @Override
    public ArrayList<IIndexItem> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<navPoint ").append("id=\"").append(id).append("\" playOrder=\"").append(playOrder).append("\">");
        // NavLabel
        sb.append("<navLabel>").append(Context.NEWLINE);
        sb.append("<text>").append(navLabel).append("</text>").append(Context.NEWLINE);
        sb.append("</navLabel>").append(Context.NEWLINE);

        // Content
        if (contentSrc != null)
            sb.append("<content ").append("src=\"").append(contentSrc).append("\" />").append(Context.NEWLINE);

        // Navpoint children
        for (IIndexItem ni : children)
            sb.append(ni.toString()).append(Context.NEWLINE);

        sb.append("</navPoint>");

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

    /**
     * @param playOrder the playOrder to set
     */
    public void setPlayOrder(Integer playOrder) {
        this.playOrder = Integer.toString(playOrder);
    }


}
