package it.senato.areatesti.ebook.scriba.plugin.defaultplugin;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.misc.Misc;
import it.senato.areatesti.ebook.scriba.packaging.PackageMaker;
import it.senato.areatesti.ebook.scriba.plugin.base.AbstractPlugin;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentItem;
import it.senato.areatesti.ebook.scriba.scf.bean.MetadataItem;
import it.senato.areatesti.ebook.scriba.scf.bean.MetadataList;
import it.senato.areatesti.ebook.scriba.scf.bean.base.IItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Plugin of the Cover internal page
 */
public class CoverPlugin extends AbstractPlugin {

    public CoverPlugin(Object[] initArgs) {
        super(initArgs);
    }

    @Override
    public List<ContentItem> elaborateContent(ContentItem content, MetadataList metadataList)
            throws IOException {
        byte[] bContent = PackageMaker.downloadContentStrict(content);

        if (bContent == null)
            return null;

        String text = new String(bContent, Context.DEF_ENCODING);
        text = text.replace(Context.DC_DATE_PLACEHOLDER, Misc.getNowTimestamp());
        text = text.replace(Context.PRETTY_DATE_PLACEHOLDER, Misc.getNowTimestampLong());

        // Searches for the title in the metadatas
        for (IItem item : metadataList.getIntList()) {
            MetadataItem mi = (MetadataItem) item;
            if (mi.getElemName().equals("title"))
                text = text.replace("%titolo%", mi.getElemVal());
        }

        content.setByteContent(text.getBytes(Context.DEF_ENCODING));
        List<ContentItem> clist = new ArrayList<>();
        clist.add(content);

        return clist;

    }

    @Override
    public ArrayList<ContentItem> makesHtmlFromPdf(
            ContentItem contentItemOfPdfRef, String fileNamePdf) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public String adjustConvertedHtml(String htmlContent) {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public String convertEncoding(byte[] byteContent, String outputEncoding) {
        throw new RuntimeException("Not implemented!");
    }


}
