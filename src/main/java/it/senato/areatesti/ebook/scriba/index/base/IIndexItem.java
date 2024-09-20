package it.senato.areatesti.ebook.scriba.index.base;

import java.util.ArrayList;

/**
 * An Index item
 */
public interface IIndexItem {

    ArrayList<IIndexItem> getChildren();

    String getContentSrc();

    void setContentSrc(String newContentSrc);

    String getNavLabel();


}
