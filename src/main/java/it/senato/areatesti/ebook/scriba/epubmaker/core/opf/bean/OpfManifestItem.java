package it.senato.areatesti.ebook.scriba.epubmaker.core.opf.bean;

/**
 * This is the Manifest Item
 */
public class OpfManifestItem {
    private final String id;
    private final String href;
    private final String mediaType;

    /**
     * Constructor
     */
    public OpfManifestItem(String id, String href, String mediaType) {
        this.id = id;
        this.href = href;
        this.mediaType = mediaType;
    }

    private String getId() {
        return " id=\"" + this.id + "\" ";
    }

    private String getHref() {
        return " href=\"" + this.href + "\" ";
    }

    private String getMediaType() {
        return " media-type=\"" + this.mediaType + "\" ";
    }

    @Override
    public String toString() {
        return "<item " + getId() + getHref() + getMediaType() + " />";
    }

}
