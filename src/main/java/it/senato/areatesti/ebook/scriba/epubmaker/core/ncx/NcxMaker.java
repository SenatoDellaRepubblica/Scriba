package it.senato.areatesti.ebook.scriba.epubmaker.core.ncx;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.epubmaker.core.OpsUtils;
import it.senato.areatesti.ebook.scriba.epubmaker.core.ncx.bean.NcxNavPointItem;
import it.senato.areatesti.ebook.scriba.epubmaker.core.opf.bean.OpfNcxMetaItem;
import it.senato.areatesti.ebook.scriba.index.base.AbstractIndexMaker;
import it.senato.areatesti.ebook.scriba.index.base.IIndexItem;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentItem;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentList;
import it.senato.areatesti.ebook.scriba.scf.bean.MetadataList;
import it.senato.areatesti.ebook.scriba.scf.bean.base.IItem;

import java.util.ArrayList;

import static it.senato.areatesti.ebook.scriba.epubmaker.core.opf.OpfMaker.parseAndPrint;

/**
 * Creates the TOC (NCX) file for the ePub format
 *
 * @author roberto
 */
public class NcxMaker extends AbstractIndexMaker {

    /**
     * Constructor
     */
    public NcxMaker(ContentList contentList, MetadataList metadataList) {
        super(contentList, metadataList);
    }


    /**
     * Maker of NCX TOC file
     *
     * @return the NCX file as String
     */
    public String makeNcx(String docTitle) {
        // Loads the MetaItem List

        ArrayList<OpfNcxMetaItem> metaBeanList = OpsUtils.loadMetaItemList(metadataList, "ncx");

        String ncxXmlString = createDocument(docTitle, metaBeanList, contentList);

        //Context.getInstance().getLogger().debug(ncxXmlString);

        // Now pretty print the XML string
        return parseAndPrint(ncxXmlString);
    }


    /**
     * Creates the entire XML document
     */
    private String createDocument(String docTitle,
                                  ArrayList<OpfNcxMetaItem> metaBeanList, ContentList cmap) {
        String head = createHead(metaBeanList);
        String title = createDocTitle(docTitle);
        String navMap = createNcxIndexTree(cmap);
        String ncx = createNcx(head, title, navMap);

        return createDoc(ncx);

    }

    private String createNcxIndexTree(ContentList cmap) {
        ArrayList<IIndexItem> nvpRootList = createIndexTree(cmap, new NcxNavPointItem());

        // ---------------------- Builds the NavPoint XML part invoking its
        // recursive toString()
        StringBuilder sb = new StringBuilder();
        sb.append("<navMap>").append(Context.NEWLINE);
        for (IIndexItem nvp : nvpRootList)
            sb.append(nvp.toString()).append(Context.NEWLINE);
        sb.append("</navMap>").append(Context.NEWLINE);

        return sb.toString();
    }


    private String createDocTitle(String docTitle) {

        return "<docTitle>" + Context.NEWLINE +
                "<text>" + docTitle + "</text>" +
                Context.NEWLINE +
                "</docTitle>" + Context.NEWLINE;
    }

    /**
     * Creates the Head part
     */
    private String createHead(ArrayList<OpfNcxMetaItem> metaBeanList) {
        StringBuilder sb = new StringBuilder();
        sb.append("<head>");
        sb.append(Context.NEWLINE);

        for (OpfNcxMetaItem metabean : metaBeanList)
            sb.append(metabean.toString()).append(Context.NEWLINE);

        sb.append("</head>").append(Context.NEWLINE);

        return sb.toString();
    }

    /**
     * Creates the NCX section
     */
    private String createNcx(String head, String docTitle, String navMap) {
        return "<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" xml:lang=\"en\" version=\"2005-1\">" +
                Context.NEWLINE +
                head + Context.NEWLINE +
                docTitle + Context.NEWLINE +
                navMap + Context.NEWLINE +
                "</ncx>" + Context.NEWLINE;
    }

    /**
     * Creates the doc section
     */
    private String createDoc(String ncx) {

        return "<?xml version=\"1.0\" encoding=\"" + Context.DEF_ENCODING + "\"?>" +
                Context.NEWLINE +
                "<!DOCTYPE ncx PUBLIC \"-//NISO//DTD ncx 2005-1//EN\" \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">" +
                Context.NEWLINE +
                ncx + Context.NEWLINE;
    }


    @Override
    protected int calculateSectionOffset(ContentItem prevContentItem) {
        int delta = 1;
        int indexOfPrevContentItem = this.contentList.getIntList().indexOf(prevContentItem);
        ArrayList<IItem> list = this.contentList.getIntList();
        while (indexOfPrevContentItem + delta + 1 < list.size()) {
            // because it jumps over the current on the next XHTML
            int currentOffset = indexOfPrevContentItem + delta;

            //Context.getInstance().getLogger().debug("NCX Prev contentItem: "+prevCi.getPackageFile());

            if (!((ContentItem) list.get(currentOffset)).getContentMediaType().equals(Context.XHTML_MIMETYPE) ||
                    ((ContentItem) list.get(currentOffset)).getIsDependentOn() != null) {
                delta = delta + 1;
            } else {
                break;
            }
        }
        //Context.getInstance().getLogger().debug("NCX Offset: "+delta);
        return delta;
    }

}
