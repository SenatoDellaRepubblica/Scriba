package it.senato.areatesti.ebook.scriba.scf.bean;

import it.senato.areatesti.ebook.scriba.scf.bean.base.IItem;

/**
 * This is a metadata IItem
 */
public class MetadataItem implements IItem {
    private final String elemName;
    private final String elemType;
    private final String elemVal;

    // Attributes only for type "dc"
    private final String dcId;
    private final String role;

    // Attributes only for type "meta"
    private final String metaName;
    private final String metaContent;
    private final String metaDestination;

    /**
     * Constructor
     */
    public MetadataItem(String elemName, String elemType, String elementVal,
                        String dcId, String role, String metaName, String metaContent,
                        String metaDestination) {
        this.elemName = elemName;
        this.elemType = elemType;
        this.elemVal = elementVal;

        this.dcId = dcId;
        this.role = role;

        this.metaName = metaName;
        this.metaContent = metaContent;
        this.metaDestination = metaDestination;
    }

    /**
     * Gets the Element value
     *
     * @return the elemVal
     */
    public String getElemVal() {
        return elemVal;
    }

    /**
     * Gets the Elment name
     *
     * @return the elemName
     */
    public String getElemName() {
        return elemName;
    }

    /**
     * Gets the Element type
     *
     * @return the elemType
     */
    public String getElemType() {
        return elemType;
    }

    /**
     * Gets the DC ID
     *
     * @return the dcId
     */
    public String getDcId() {
        return dcId;
    }

    /**
     * Gets the META name
     *
     * @return the metaName
     */
    public String getMetaName() {
        return metaName;
    }

    /**
     * Gets the META content
     *
     * @return the metaContent
     */
    public String getMetaContent() {
        return metaContent;
    }

    /**
     * Gets the META destination
     *
     * @return the metaDestination
     */
    public String getMetaDestination() {
        return metaDestination;
    }

    /**
     * Gets the role
     *
     * @return the role
     */
    public String getRole() {
        return role;
    }


}
