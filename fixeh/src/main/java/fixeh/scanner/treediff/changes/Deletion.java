package fixeh.scanner.treediff.changes;

import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.tree.TreeContext;

import fixeh.scanner.treediff.Conventions;
import spoon.reflect.declaration.CtElement;

/**
 * Created by Shunjie Ding on 25/12/2017.
 */
public class Deletion extends Change<Delete> {
    public Deletion(Delete action) {
        super(action);
    }

    @Override
    public String getRawLeftNodeType(TreeContext treeContext) {
        return treeContext.getTypeLabel(getAction().getNode());
    }

    @Override
    public String getRawRightNodeType(TreeContext treeContext) {
        return "";
    }

    @Override
    public CtElement getLeftNode() {
        return Conventions.nodeOf(getAction().getNode());
    }

    @Override
    public CtElement getRightNode() {
        return null;
    }
}
