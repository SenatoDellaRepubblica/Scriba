package it.senato.areatesti.ebook.scriba.epubmaker.core.xmlfile;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.misc.Misc;

/**
 * Manages the "mimetype" file
 */
public class MimetypeMaker {

    /**
     * Gets the mimetype content
     *
     * @return the OPS mimetype file content
     */
    public String getMimetypeContent() {
        return Misc.getContentProp("prop/epubfile/mimetype", Context.DEF_ENCODING);
    }

}
