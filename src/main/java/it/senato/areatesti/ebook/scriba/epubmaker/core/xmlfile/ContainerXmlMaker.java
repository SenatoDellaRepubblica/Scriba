package it.senato.areatesti.ebook.scriba.epubmaker.core.xmlfile;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.misc.Misc;

/**
 * Manages the container.xml file of the epub format
 */
public class ContainerXmlMaker {
    /**
     * Gets the container.xml
     *
     * @return the content of container.xml
     */
    public String getContainerXmlContent() {
        return Misc.getContentProp("prop/epubfile/container.xml", Context.DEF_ENCODING);
    }

}
