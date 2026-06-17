package fixeh.scanner.treediff.changes;

import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import fixeh.scanner.treediff.Conventions;
import spoon.reflect.declaration.CtElement;

/**
 * Created by Shunjie Ding on 25/12/2017.
 */
public class Movement extends Change<Move> {
    private final ITree destNode;

    public Movement(Move action, ITree destNode) {
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
