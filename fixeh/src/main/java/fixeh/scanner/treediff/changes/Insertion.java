package fixeh.scanner.treediff.changes;

import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.tree.TreeContext;

import fixeh.scanner.treediff.Conventions;
import spoon.reflect.declaration.CtElement;

/**
 * Created by Shunjie Ding on 25/12/2017.
 */
public class Insertion extends Change<Insert> {
    public Insertion(Insert action) {
        super(action);
    }

    @Override
    public String getRawLeftNodeType(TreeContext treeContext) {
        return treeContext.getTypeLabel(getAction().getParent());
    }

    @Override
    public String getRawRightNodeType(TreeContext treeContext) {
        return treeContext.getTypeLabel(getAction().getNode());
    }

    @Override
    public CtElement getLeftNode() {
        return Conventions.nodeOf(getAction().getParent());
    }

    @Override
    public CtElement getRightNode() {
        return Conventions.nodeOf(getAction().getNode());
    }
}
