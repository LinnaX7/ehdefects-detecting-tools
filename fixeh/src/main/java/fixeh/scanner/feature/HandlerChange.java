package fixeh.scanner.feature;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by Shunjie Ding on 07/01/2018.
 */
public final class HandlerChange implements Serializable {
    private HandlerChangeType type;
    private Handler left, right;

    private HandlerChange(HandlerChangeType type, Handler left, Handler right) {
        this.type = type;
        this.left = left;
        this.right = right;
    }

    private static HandlerChange newHandlerChange(
        HandlerChangeType type, Handler left, Handler right) {
        switch (type) {
            case DELETED:
                if (left == null || right != null) {
                    throw new IllegalArgumentException(
                        "Handler deleted should have left != null && right == null!");
                }
                break;
            case INSERTED:
                if (left != null || right == null) {
                    throw new IllegalArgumentException(
                        "Handler inserted should have left == null && right != null!");
                }
                break;
            case MODIFIED:
                if (left == null || right == null || !left.getClass().equals(right.getClass())) {
                    throw new IllegalArgumentException(
                        "Handler modified should have left and right both not null!");
                }
        }
        return new HandlerChange(type, left, right);
    }

    public static HandlerChange newInserted(Handler left, Handler right) {
        return newHandlerChange(HandlerChangeType.INSERTED, left, right);
    }

    public static HandlerChange newDeleted(Handler left, Handler right) {
        return newHandlerChange(HandlerChangeType.DELETED, left, right);
    }

    public static HandlerChange newModified(Handler left, Handler right) {
        return newHandlerChange(HandlerChangeType.MODIFIED, left, right);
    }

    public static CatchHandler newCatchHandler(
        String codes, Set<HandlerAction> actions, Set<String> exceptions) {
        return new CatchHandler(codes, actions, exceptions);
    }

    public static FinallyHandler newFinallyHandler(String codes, Set<HandlerAction> actions) {
        return new FinallyHandler(codes, actions);
    }

    public HandlerChangeType getType() {
        return type;
    }

    public Handler getLeft() {
        return left;
    }

    public Handler getRight() {
        return right;
    }

    public enum HandlerChangeType { INSERTED, DELETED, MODIFIED }

    public enum HandlerAction {
        LOG,
        SWALLOW,
        RETHROW,
        CHANGE_CONTROL_FLOW,
        ASK_FOR_USER_INPUT,
        UPDATE_STATE,
        CLEAN_UP,
        OTHER,
        EMPTY
    }

    public static abstract class Handler implements Serializable {
        private String codes;

        private Set<HandlerAction> actions;

        Handler(String codes, Set<HandlerAction> actions) {
            this.codes = codes;
            this.actions = actions;
        }

        public String getCodes() {
            return codes;
        }

        public Set<HandlerAction> getActions() {
            return actions;
        }
    }

    public static class CatchHandler extends Handler {
        private Set<String> exceptions;

        CatchHandler(String codes, Set<HandlerAction> actions, Set<String> exceptions) {
            super(codes, actions);
            this.exceptions = exceptions;
        }

        public Set<String> getExceptions() {
            return exceptions;
        }
    }

    public static class FinallyHandler extends Handler {
        FinallyHandler(String codes, Set<HandlerAction> actions) {
            super(codes, actions);
        }
    }
}
