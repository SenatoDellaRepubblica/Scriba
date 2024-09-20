package it.senato.areatesti.ebook.scriba.index.base;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.epubmaker.core.ncx.bean.NcxNavPointItem;
import it.senato.areatesti.ebook.scriba.misc.Misc;
import it.senato.areatesti.ebook.scriba.misc.SecUtils;
import it.senato.areatesti.ebook.scriba.misc.xhtml.EPubXhtmlMgr;
import it.senato.areatesti.ebook.scriba.packaging.TemplateManager;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentItem;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentList;
import it.senato.areatesti.ebook.scriba.scf.bean.MetadataList;
import it.senato.areatesti.ebook.scriba.scf.bean.base.IItem;
import it.senato.areatesti.ebook.scriba.zipmaker.bean.HtmlNavPointItem;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * The abstract class of the IndexMaker
 */
public abstract class AbstractIndexMaker implements IIndexMaker {
    protected final ContentList contentList;
    protected final MetadataList metadataList;

    /**
     * Constructor that defines the ebook type it is in progress
     */
    protected AbstractIndexMaker(ContentList contentList, MetadataList metadataList) {
        this.contentList = contentList;
        this.metadataList = metadataList;
    }

    @Override
    public ArrayList<IIndexItem> createIndexTree(ContentList cmap, IIndexItem indexItemInstance) {

        // Creates a LinkedHashMap where the key is the PackagePath and the value is an Array of ContentItems within the packagePath
        // this is a data structure needed for the rest of the Algo
        LinkedHashMap<String, ArrayList<ContentItem>> linkMap = new LinkedHashMap<>();
        for (IItem ii : cmap.getIntList()) {
            if (ii instanceof ContentItem) {
                ContentItem ci = (ContentItem) ii;
                if (ci.getTocName() != null || ci.isCover()) {
                    // Counts the level of the item in the TOC
                    if (!linkMap.containsKey(ci.getPackagePath())) {
                        linkMap.put(ci.getPackagePath(), new ArrayList<>());
                    }
                    linkMap.get(ci.getPackagePath()).add(ci);
                }
            }
        }

        // ---------------------- Creates the NcxNavPointItem tree

        // for every packagePath it builds the descending recursive structure of NavPoint items
        int tempPlayOrder = 1;
        ArrayList<IIndexItem> nvpRootList = new ArrayList<>();
        Hashtable<String, IIndexItem> nvpParentHT = new Hashtable<>();
        ContentItem prevContentItem = null;

        for (Entry<String, ArrayList<ContentItem>> entrykey : linkMap.entrySet()) {
            // ON the first key: it's a node

            ContentItem content = entrykey.getValue().get(0);


            String[] splittedLevel = StringUtils.split(content.getPackagePath(), Context.PATH_SEP);
            StringBuilder currentPath = new StringBuilder();
            IIndexItem rootSectItem = null;
            for (String s : splittedLevel) {
                // builds the current path and put it to the root HT
                currentPath.append(s).append(Context.PATH_SEP);

                // it doesn't exist the parent path in the HT
                if (!nvpParentHT.containsKey(currentPath.toString())) {
                    // creates the section as content item

                    ContentItem secContentItem = putSectionContent(cmap, prevContentItem,
                            content.getPackagePath(),
                            content.getNestedLevel(),
                            s);

                    String secFileName = secContentItem.getPackageId() + Context.XHTML_EXT;

					/*
					if (prevContentItem!=null)
						Context.getInstance().getLogger().debug("F: " + prevContentItem.getPackagePath()+"  " + prevContentItem.getPackageFile() +"--> "+secFileName);
					*/
                    prevContentItem = secContentItem;

                    // adds to the root level the ncx element
                    String completePackFilePath = content.getPackagePath() + secFileName;
                    IIndexItem nvpSectItem;
                    if (indexItemInstance instanceof NcxNavPointItem) {
                        nvpSectItem = new NcxNavPointItem(s, completePackFilePath, tempPlayOrder);
                        tempPlayOrder += 1;

                    } else if (indexItemInstance instanceof HtmlNavPointItem) {
                        nvpSectItem = new HtmlNavPointItem(s, completePackFilePath, true);
                    } else {
                        throw new RuntimeException("Index item instance is of a non definite type");
                    }

                    nvpParentHT.put(currentPath.toString(), nvpSectItem);
                    if (rootSectItem == null)
                        nvpRootList.add(nvpSectItem);
                    else
                        rootSectItem.getChildren().add(nvpSectItem);

                    rootSectItem = nvpSectItem;

                }
                // there is another root so links to it
                else {
                    rootSectItem = nvpParentHT.get(currentPath.toString());
                    //rootSectItem.getChildren().add(nvpSectItem);
                }

            }

            // makes the Nvp contents under the section made before

            // visit the content on the same level with THE SAME KEY
            for (int countItemOnSameKeyLevel = 0; countItemOnSameKeyLevel < entrykey.getValue().size(); countItemOnSameKeyLevel++) {
                content = entrykey.getValue().get(countItemOnSameKeyLevel);

                // it creates the NavPointItem for the content

                IIndexItem currNvp = null;
                if (indexItemInstance instanceof NcxNavPointItem) {
                    currNvp = new NcxNavPointItem(content.getTocName(), content.getPackagePath()
                            + content.getPackageFile(), tempPlayOrder);
                    //Context.getInstance().getLogger().debug("======================================> 2-Playorder = "+tempPlayOrder);
                    tempPlayOrder += 1;
                } else if (indexItemInstance instanceof HtmlNavPointItem) {
                    currNvp = new HtmlNavPointItem(content.getTocName(), content.getPackagePath()
                            + content.getPackageFile(), false);
                }

                if (!nvpParentHT.containsKey(content.getPackagePath())) {
                    nvpRootList.add(currNvp);
                } else {
                    rootSectItem = nvpParentHT.get(content.getPackagePath());
                    rootSectItem.getChildren().add(currNvp);
                }

                // sets the previous content so to know where to position the section in the SPINE
                prevContentItem = content;
            }

        }

        // Adjust the playOrder attribute only for the NcxNavPointItem
        int playOrder = 1;
        for (IIndexItem currRootNode : nvpRootList) {
            if (currRootNode instanceof NcxNavPointItem) {
                playOrder = adjustPlayOrder(currRootNode, playOrder);
            }
        }

        // Completes the section with the reference to the first available content
        int level = 0;
        for (IIndexItem currRootNode : nvpRootList)
            getFirstUsefulSection(currRootNode, level++);

        return nvpRootList;
    }

    /**
     * Inserts the section content
     */
    private ContentItem putSectionContent(ContentList cmap,
                                          ContentItem prevContentItem, String currPackagePath, int nestedLevel,
                                          String title) {
        String suffix = SecUtils.getHex(SecUtils.getRandomBytes(4));
        String id = Context.ID_OPF_PREFIX + Misc.normalizeFileNameHard(title) + "-" + suffix + "-SEC";
        ContentItem secContentItem = null;
        try {
            secContentItem = makesSectionContent(currPackagePath, nestedLevel, id, title);

            int index, offset;
            if (prevContentItem != null) {
                index = cmap.getIntList().indexOf(prevContentItem);

                // the offset must be 1 if the content will not be multiplied otherwise it has to be as the factor of multiplication
                offset = calculateSectionOffset(prevContentItem);
            }
            // Fix: 25
            else {
                index = offset = 0;
            }
            //Context.getInstance().getLogger().debug("Index: "+index + " - offset: "+offset);
            cmap.addContent(index + offset, secContentItem);


        } catch (UnsupportedEncodingException e) {
            Context.getInstance().getLogger().error(e);
        }
        return secContentItem;
    }

    /**
     * It defines the offset at which will be positioned the section after the content
     * (it depends on the fact that the NCX is made before the OPF and so it doesn't know if a contentItem will be only one or more contentItem)
     */
    protected abstract int calculateSectionOffset(ContentItem prevContentItem);

    /**
     * Adjust the playOrder attribute
     */
    private int adjustPlayOrder(IIndexItem node, int lastPlayOrder) {
        NcxNavPointItem nvpNode = (NcxNavPointItem) node;
        nvpNode.setPlayOrder(lastPlayOrder++);
        //Context.getInstance().getLogger().debug("======================================> 3-Playorder = "+lastPlayOrder);
        for (int i = 0; i < node.getChildren().size(); i++) {
            lastPlayOrder = adjustPlayOrder(node.getChildren().get(i), lastPlayOrder);
        }
        return lastPlayOrder;
    }

    /**
     * Recursive method to adjust the sections
     */
    private String getFirstUsefulSection(IIndexItem node, int level) {
        String sectionName = null;
        if (node.getContentSrc().equals(Context.SECTION_LABEL)) {
            for (int i = 0; i < node.getChildren().size(); i++) {
                String sec = getFirstUsefulSection(node.getChildren().get(i), i);
                if (sec != null && i == 0) {
                    node.setContentSrc(sec);
                    sectionName = sec;
                }
            }

            return sectionName;
        } else {
            if (level == 0)
                return node.getContentSrc();
            return null;
        }
    }

    /**
     * Creates the ContentItem corresponding to the Index section page
     */
    private ContentItem makesSectionContent(String currPackagePath, int nestedLevel, String id, String title) throws UnsupportedEncodingException {

        String secContent = TemplateManager.getSectionTemplate(title, title);
        secContent = EPubXhtmlMgr.manageCssReference(nestedLevel,
                secContent,
                Context.getInstance().cssStyleName);
        secContent = EPubXhtmlMgr.manageLogoReference(nestedLevel,
                secContent,
                Context.getInstance().sectionlogoFileName);

        // Builds the ContentItem
        ContentItem ci = new ContentItem(currPackagePath, id + Context.XHTML_EXT, id, null, null, Context.XHTML_MIMETYPE);
        ci.setInSpine(true);
        ci.setSpineLinear(true);
        ci.setStringContent(secContent);

        return ci;
    }

}
