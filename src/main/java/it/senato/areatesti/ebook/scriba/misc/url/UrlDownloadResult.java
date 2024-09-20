
package it.senato.areatesti.ebook.scriba.misc.url;

import java.util.ArrayList;

/**
 * The result of an URL download
 * <p>
 * (to finish, some methods are not used)
 */
public class UrlDownloadResult {
    private byte[] mainContent;
    private ArrayList<byte[]> attaches;
    private String mainContentMimeType;

    /**
     * Constructor
     *
     * @param mainContentMimeType the mimetype of the main content
     */
    public UrlDownloadResult(String mainContentMimeType) {
        this.mainContentMimeType = mainContentMimeType;
    }

    String getMainContentMimeType() {
        return mainContentMimeType;
    }

    void setMainContentMimeType(String mainContentMimeType) {
        this.mainContentMimeType = mainContentMimeType;
    }

    /**
     * The main content downloaded
     *
     * @return the mainContent
     */
    public byte[] getMainContent() {
        return mainContent;
    }

    public void setMainContent(byte[] mainContent) {
        this.mainContent = mainContent;
    }

    public ArrayList<byte[]> getAttaches() {
        return attaches;
    }

    public void setAttaches(ArrayList<byte[]> attaches) {
        this.attaches = attaches;
    }

}
