
package it.senato.areatesti.ebook.scriba.epubmaker.core.opf.bean;

/**
 * Creates the Spine item for OPF
 */
public class OpfSpineItem {
    private final String idref;
    private final String linear;

    /**
     * Constructor
     */
    public OpfSpineItem(String idref, String linear) {
        this.idref = idref;
        this.linear = linear;
    }

    private String getIdref() {
        return " idref=\"" + idref + "\" ";
    }

    private String getLinear() {
        if (linear != null)
            return " linear=\"" + linear + "\" ";
        else
            return "";
    }

    @Override
    public String toString() {
        return "<itemref " + getIdref() + getLinear() + " />";
    }

}
