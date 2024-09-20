package it.senato.areatesti.ebook.scriba.epubmaker.core.opf.bean;

/**
 * A DC tag in the meta section of the OPF file
 */
public class OpfDcItem {
    private final String elementName;
    private final String elementVal;
    private final String idAttributeVal;
    private final String roleAttributeVal;

    /**
     * Constructor
     */
    public OpfDcItem(String elementName, String elementVal) {
        this(elementName, elementVal, null, null);
    }

    /**
     * Constructor
     */
    public OpfDcItem(String elementName, String elementVal,
                     String idAttributeVal, String roleAttributeVal) {
        this.elementName = elementName;
        this.elementVal = elementVal;
        this.idAttributeVal = idAttributeVal;
        this.roleAttributeVal = roleAttributeVal;
    }

    /**
     * The ID attribute value
     *
     * @return the idAttributeVal
     */
    public String getIdAttributeVal() {
        return idAttributeVal;
    }

    @Override
    public String toString() {
        String idattr = "";
        String roleattr = "";

        if (this.idAttributeVal != null)
            idattr = " id=\"" + this.idAttributeVal + "\" ";

        if (this.roleAttributeVal != null)
            roleattr = " opf:role=\"" + this.roleAttributeVal + "\" ";

        return "<dc:" + elementName + idattr + roleattr + ">" + elementVal + "</dc:"
                + elementName + ">";
    }

}
