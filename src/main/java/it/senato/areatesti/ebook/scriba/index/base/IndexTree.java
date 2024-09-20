package it.senato.areatesti.ebook.scriba.index.base;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.scf.bean.ContentItem;
import it.senato.areatesti.ebook.scriba.scf.bean.base.IItem;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The Abstract tree to manage indexes for Scriba
 *
 * @author roberto.battistoni
 */
public class IndexTree {
    /**
     * the children which are paths
     */
    public final List<IndexTree> childrenPathList;

    /**
     * the children which are Contents
     */
    public final List<ContentItem> childrenContentList;

    /**
     * The subPath Label of the node
     */
    public String nodeLabel;

    /**
     * The parent
     */
    private IndexTree parent;

    /**
     * Constructor
     */
    private IndexTree() {
        this.childrenContentList = new ArrayList<>();
        this.childrenPathList = new ArrayList<>();
    }

    /**
     * Constructor
     */
    private IndexTree(String nodeLabel) {
        this();
        this.nodeLabel = nodeLabel;
    }

    /**
     * Builds a tree starting from a list of ContentItem
     */
    public static IndexTree buildTree(List<IItem> contentList) {
        IndexTree root = new IndexTree();
        for (IItem item : contentList) {
            ContentItem content = (ContentItem) item;
            String[] pathNodes = StringUtils.split(content.getPackagePath(), "/");

            //System.out.println(Arrays.toString(pathNodes));
            buildPathNode(root, pathNodes, 0, content);
        }
        return root;
    }

    /**
     * InOrder tree visit to Print the Tree
     */
    public static void debugPrintTree(IndexTree root) {
        debugPrintTree(root, 0);
    }

    /**
     * InOrder tree visit to Print the Tree
     */
    private static void debugPrintTree(IndexTree root, int level) {
        String space10 = new String(new char[level]).replace('\0', '-') + ">";
        String space101 = new String(new char[level]).replace('\0', '=') + ">";

        Context.getInstance().getLogger().debug(space101 + "P:" + root.nodeLabel);
        for (ContentItem ci : root.childrenContentList)
            Context.getInstance().getLogger().debug(space10 + "C:" + ci.getTocName());

        for (IndexTree child : root.childrenPathList)
            debugPrintTree(child, level + 1);

    }


    /**
     * Builds a path in the IndexTree
     */
    private static void buildPathNode(IndexTree root, String[] pathNodes, int startPosition, ContentItem leafContent) {
        if (root == null)
            throw new RuntimeException("You cannot pass a NULL root to IndexTree");

        // Search if the child exists
        IndexTree node;
        if (pathNodes.length > 0)
            for (int i = 0; i < root.childrenPathList.size(); i++) {
                //System.out.println(startPosition);
                //System.out.println(pathNodes[startPosition]);

                if (root.childrenPathList.get(i).nodeLabel != null &&
                        root.childrenPathList.get(i).nodeLabel.equals(pathNodes[startPosition])) {
                    node = root.childrenPathList.get(i);
                    buildPathNode(node, pathNodes, startPosition + 1, leafContent);
                    return;
                }
            }

        // otherwise it adds the new child: the child is a new Path
        if (startPosition < pathNodes.length) {
            IndexTree newChild = new IndexTree(pathNodes[startPosition]);
            newChild.parent = root;
            root.childrenPathList.add(newChild);
            buildPathNode(newChild, pathNodes, startPosition + 1, leafContent);
        }
        // The child is the Content
        else {
            root.childrenContentList.add(leafContent);
        }


    }
}
