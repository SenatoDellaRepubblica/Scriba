package it.senato.areatesti.ebook.scriba.scf.bean.base;

import java.util.ArrayList;

/**
 * An abstract list of IItem
 */
public abstract class AbstractItemList {

    protected ArrayList<IItem> intList;

    /**
     * Gets the internal List
     *
     * @return the intList
     */
    public ArrayList<IItem> getIntList() {
        return intList;
    }

    /**
     * @param intList the intList to set
     */
    public void setIntList(ArrayList<IItem> intList) {
        this.intList = intList;
    }

    /**
     * Adds an IItem to the list
     */
    public void addContent(IItem item) {
        this.intList.add(item);
    }

    /**
     * Adds an IItem to the list
     */
    public void addContent(int index, IItem item) {
        this.intList.add(index, item);
    }


    /**
     * Gets an item from the list
     */
    public IItem getItem(int index) {
        return this.intList.get(index);
    }

    /**
     * This is the size of the internal list
     *
     * @return the size
     */
    public int size() {
        return this.intList.size();
    }

}
