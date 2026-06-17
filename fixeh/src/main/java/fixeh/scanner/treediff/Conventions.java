package fixeh.scanner.treediff;

import com.github.gumtreediff.tree.ITree;

import spoon.reflect.declaration.CtElement;

/**
 * Created by Shunjie Ding on 25/12/2017.
 */
public final class Conventions {
    private static final String SPOON_NODE_META = "spoon_node";
    private static final String GUMTREE_NODE_META = "gumtree_node";

    private static final String SPOON_TREE_STORE = "spoon_tree_store";

    private Conventions() {}

    public static CtElement nodeOf(ITree tree) {
        if (tree == null) {
            return null;
        }
        return (CtElement) tree.getMetadata(SPOON_NODE_META);
    }

    public static ITree nodeOf(CtElement element) {
        if (element == null) {
            return null;
        }
        return (ITree) element.getMetadata(GUMTREE_NODE_META);
    }

    private static void setSpoon(ITree tree, CtElement node) {
        tree.setMetadata(SPOON_NODE_META, node);
    }

    private static void setGumtree(CtElement node, ITree tree) {
        node.putMetadata(GUMTREE_NODE_META, tree);
    }

    static void setMetadata(CtElement node, ITree tree) {
        setSpoon(tree, node);
        setGumtree(node, tree);
    }

    /**
     * Get tree store from node, only works for root node.
     * @param element root node which have been put into tree store and haven't
     * removed.
     * @return tree store if found, or null.
     */
    public static SpoonTreeStore getTreeStore(CtElement element) {
        return (SpoonTreeStore) element.getMetadata(SPOON_TREE_STORE);
    }

    static void setTreeStore(CtElement element, SpoonTreeStore treeStore) {
        element.putMetadata(SPOON_TREE_STORE, treeStore);
    }

    public CtElement getSpoonRoot(ITree tree) {
        // tree must not be null
        while (tree.getParent() != null) {
            tree = tree.getParent();
        }
        return nodeOf(tree);
    }

    public ITree getGumtreeRoot(CtElement element) {
        // if there is no gumtree nodes set, return null.
        if (nodeOf(element) == null) {
            return null;
        }
        while (nodeOf(element.getParent()) != null) {
            element = element.getParent();
        }
        return nodeOf(element);
    }

    public boolean containsGumtree(CtElement element) {
        return nodeOf(element) != null;
    }
}
