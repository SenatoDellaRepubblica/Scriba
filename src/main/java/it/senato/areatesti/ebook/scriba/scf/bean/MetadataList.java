package it.senato.areatesti.ebook.scriba.scf.bean;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.scf.bean.base.AbstractItemList;
import it.senato.areatesti.ebook.scriba.scf.bean.base.IItem;

import java.util.ArrayList;

/**
 * This is a list of Metadata IItem
 */
public class MetadataList extends AbstractItemList {
    /**
     * Constructor
     */
    public MetadataList() {
        super();
        this.intList = new ArrayList<>();
    }

    /**
     * Search for a meta tag with a defined name
     */
    public String searchMetaContent(String metaName) {
        // Searches for the title in the metadatas
        for (IItem item : this.intList) {
            // ATTENTION: The "destination" type "plugin" of "meta" is only a placeholder to distinguish between "opf" and "ncx"
            MetadataItem mi = (MetadataItem) item;
            if (mi.getMetaName() != null && mi.getMetaName().equals(metaName))
                return mi.getMetaContent();
        }

        Context.getInstance().getLogger().info(String.format("The META \"%s\" has not found in the SCF file", metaName));
        return "";
    }
}
