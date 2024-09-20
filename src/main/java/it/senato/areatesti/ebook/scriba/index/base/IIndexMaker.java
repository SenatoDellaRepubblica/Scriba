package it.senato.areatesti.ebook.scriba.index.base;

import it.senato.areatesti.ebook.scriba.scf.bean.ContentList;

import java.util.ArrayList;

/**
 * The index maker
 */
interface IIndexMaker {

    /**
     * Creates the index tree
     *
     * @param indexItemInstance the instance of the IndexItem type to use
     */
    ArrayList<IIndexItem> createIndexTree(ContentList cmap, IIndexItem indexItemInstance);
}
