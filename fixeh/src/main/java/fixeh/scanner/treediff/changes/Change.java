package fixeh.scanner.treediff.changes;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.TreeContext;

import spoon.reflect.declaration.CtElement;

/**
 * Created by Shunjie Ding on 25/12/2017.
 */
public abstract class Change<T extends Action> {
    private final T action;

    protected Change(T action) {
        this.action = action;
    }

    /**
     * Get the raw node (gumtree)'s label, usually the type name of spoon node,
     * could be VariableType or Modifier(s).
     * @param treeContext TreeContext used to build the left tree.
     * @return type name
     */
    public abstract String getRawLeftNodeType(TreeContext treeContext);

    /**
     * Get the raw node (gumtree)'s label, usually the type name of spoon node,
     * could be VariableType or Modifier(s).
     * @param treeContext TreeContext used to build the right tree.
     * @return type name
     */
    public abstract String getRawRightNodeType(TreeContext treeContext);

    /**
     * Get the changed node in the left tree: parent node for insertion, source
     * node for modification/movement/deletion.
     * @return the changed node in the left tree.
     */
    public abstract CtElement getLeftNode();

    /**
     * Get the changed node in the right tree: changed node for insertion, null
     * for deletion, and destination node for modification/movement.
     * @return the changed node in the right tree.
     */
    public abstract CtElement getRightNode();

    public T getAction() {
        return action;
    }

    @Override
    public String toString() {
        return String.format("%s\n%s\n============================\n%s",
            this.getClass().getSimpleName(), getLeftNode(), getRightNode());
    }
}
