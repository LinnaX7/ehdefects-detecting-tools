package fixeh.scanner.treediff.changes;

import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import fixeh.scanner.treediff.Conventions;
import spoon.reflect.declaration.CtElement;

/**
 * Created by Shunjie Ding on 25/12/2017.
 */
public class Modification extends Change<Update> {
    private final ITree destNode;

    public Modification(Update action, ITree destNode) {
        super(action);
        this.destNode = destNode;
    }

    @Override
    public String getRawLeftNodeType(TreeContext treeContext) {
        return treeContext.getTypeLabel(getAction().getNode());
    }

    @Override
    public String getRawRightNodeType(TreeContext treeContext) {
        return treeContext.getTypeLabel(destNode);
    }

    @Override
    public CtElement getLeftNode() {
        return Conventions.nodeOf(getAction().getNode());
    }

    @Override
    public CtElement getRightNode() {
        return Conventions.nodeOf(destNode);
    }
}