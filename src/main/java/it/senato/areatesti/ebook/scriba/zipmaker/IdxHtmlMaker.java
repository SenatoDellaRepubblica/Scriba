package it.senato.areatesti.ebook.scriba.zipmaker;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.index.base.AbstractIndexMaker;
import it.senato.areatesti.ebook.scriba.index.base.IIndexItem;
import it.senato.areatesti.ebook.scriba.packaging.TemplateManager;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentItem;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentList;
import it.senato.areatesti.ebook.scriba.scf.bean.MetadataList;
import it.senato.areatesti.ebook.scriba.zipmaker.bean.HtmlNavPointItem;

import java.util.ArrayList;

/**
 * Creates the Index for the ZIP ebook format
 */
public class IdxHtmlMaker extends AbstractIndexMaker {

    /**
     * Constructor
     */
    public IdxHtmlMaker(ContentList contentList, MetadataList metadataList) {
        super(contentList, metadataList);
    }


    /**
     * makes the index.xhtml
     *
     * @return the index
     */
    public String make(String docTitle) {
        String idxXmlString = createHtmlIndex(contentList);

        return TemplateManager.getHtmlIndexTemplate(idxXmlString);
    }


    /**
     * makes the index.html file
     */
    private String createHtmlIndex(ContentList cmap) {
        ArrayList<IIndexItem> nvpRootList = createIndexTree(cmap, new HtmlNavPointItem());

        // ---------------------- Build the NavPoint XML part invoking its
        // recursive toString()
        StringBuilder sb = new StringBuilder();
        sb.append("<ol>").append(Context.NEWLINE);
        for (IIndexItem nvp : nvpRootList)
            sb.append(nvp.toString()).append(Context.NEWLINE);
        sb.append("</ol>").append(Context.NEWLINE);

        return sb.toString();
    }

    @Override
    protected int calculateSectionOffset(ContentItem prevContentItem) {
        return 1;
    }


}

