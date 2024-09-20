package it.senato.areatesti.ebook.scriba.epubmaker.core;

import it.senato.areatesti.ebook.scriba.epubmaker.core.opf.bean.OpfNcxMetaItem;
import it.senato.areatesti.ebook.scriba.scf.bean.MetadataItem;
import it.senato.areatesti.ebook.scriba.scf.bean.MetadataList;
import it.senato.areatesti.ebook.scriba.scf.bean.base.IItem;

import java.util.ArrayList;


/**
 * Utils for the Ops
 */
public class OpsUtils {

    /**
     * Loads the MetaItem list
     */
    public static ArrayList<OpfNcxMetaItem> loadMetaItemList(MetadataList mmap,
                                                             String destinationFile) {
        ArrayList<OpfNcxMetaItem> miList = new ArrayList<>();
        for (IItem item : mmap.getIntList()) {
            if (item instanceof MetadataItem) {
                MetadataItem m = (MetadataItem) item;
                if (m.getElemType().equals("meta")
                        && m.getMetaDestination().equals(destinationFile)) {
                    OpfNcxMetaItem mi = new OpfNcxMetaItem(m.getMetaName(),
                            m.getMetaContent());
                    miList.add(mi);
                }
            }
        }
        return miList;
    }

}
