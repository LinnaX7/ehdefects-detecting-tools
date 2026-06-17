package fixeh.scanner.util;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import java.io.PrintStream;

/**
 * Created by Shunjie Ding on 09/01/2018.
 */
class GumtreePrinter {
    private PrintStream printStream = System.out;

    private TreeContext treeContext;

    public GumtreePrinter(TreeContext treeContext) {
        this.treeContext = treeContext;
    }

    public GumtreePrinter(TreeContext treeContext, PrintStream printStream) {
        this(treeContext);
        this.printStream = printStream;
    }

    private void print(ITree root, String prefix) {
        if (root == null) {
            return;
        }

        if (!root.getLabel().isEmpty()) {
            printStream.println(
                prefix + treeContext.getTypeLabel(root.getType()) + ": " + root.getLabel());
        } else {
            printStream.println(prefix + treeContext.getTypeLabel(root.getType()));
        }

        for (ITree node : root.getChildren()) {
            print(node, "  " + prefix);
        }
    }

    public void print(ITree root) {
        print(root, "- ");
    }
}
