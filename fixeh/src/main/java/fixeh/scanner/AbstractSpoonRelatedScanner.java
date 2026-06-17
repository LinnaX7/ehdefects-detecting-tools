package fixeh.scanner;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.Filter;

public abstract class AbstractSpoonRelatedScanner<T, R> extends AbstractScanner<T, R> {
    public AbstractSpoonRelatedScanner(T target) {
        super(target);
    }

    protected CtMethod getParentMethod(CtElement element, boolean inclusive) {
        if (element == null) {
            return null;
        }
        if (inclusive && element instanceof CtMethod) {
            return (CtMethod) element;
        }
        return element.getParent(CtMethod.class);
    }

    protected CtBlock getParentBlock(CtElement element, boolean inclusive) {
        if (element == null) {
            return null;
        }
        if (inclusive && element instanceof CtBlock) {
            return (CtBlock) element;
        }
        return element.getParent(CtBlock.class);
    }

    protected CtTry getParentTry(CtElement element, boolean inclusive) {
        if (element == null) {
            return null;
        }
        if (inclusive && element instanceof CtTry) {
            return (CtTry) element;
        }
        return element.getParent(CtTry.class);
    }

    protected CtCatch getParentCatch(CtElement element, boolean inclusive) {
        if (element == null) {
            return null;
        }
        if (inclusive && element instanceof CtCatch) {
            return (CtCatch) element;
        }
        return element.getParent(CtCatch.class);
    }

    protected CtBlock getParentFinally(CtElement element, boolean inclusive) {
        if (element == null) {
            return null;
        }
        if (inclusive && element instanceof CtBlock && isDirectFinallyBlock((CtBlock) element)) {
            return (CtBlock) element;
        }
        return element.getParent(this ::isDirectFinallyBlock);
    }

    protected boolean isNodeExceptionRelated(CtElement element) {
        if (element == null || element instanceof CtTry || element instanceof CtCatch
            || (element instanceof CtBlock && isDirectFinallyBlock((CtBlock) element))) {
            return false;
        }
        final NodeExceptionRelatedScanner nodeScanner = new NodeExceptionRelatedScanner();
        nodeScanner.scan(element);
        return nodeScanner.isRelated();
    }

    protected boolean inCatchBlock(CtElement element) {
        return element != null && getParentCatch(element, true) != null;
    }

    protected boolean inFinallyBlock(CtElement element) {
        return element != null && getParentFinally(element, true) != null;
    }

    protected boolean inCatchOrFinallyBlock(CtElement element) {
        return inCatchBlock(element) || inFinallyBlock(element);
    }

    protected boolean isDirectFinallyBlock(CtBlock block) {
        CtTry ctTry = getParentTry(block, false);
        return ctTry != null && ctTry.getFinalizer() != null && ctTry.getFinalizer() == block;
    }

    protected boolean inTryBody(CtElement element) {
        if (element == null) {
            return false;
        }

        if (element instanceof CtTry) {
            return true;
        }
        CtTry ctTry = getParentTry(element, false);
        if (ctTry == null) {
            return false;
        }
        return element.getParent((Filter<CtBlock>) element1 -> element1 == ctTry.getBody()) != null;
    }

    protected boolean inTryBlock(CtElement element) {
        return element instanceof CtTry || inTryBody(element) || inCatchOrFinallyBlock(element);
    }

    protected boolean containsTryBlock(CtElement element) {
        TryBlockScanner tryBlockScanner = new TryBlockScanner();
        tryBlockScanner.scan(element);
        return tryBlockScanner.isFound();
    }

    class TryBlockScanner extends CtScanner {
        private boolean found = false;

        public boolean isFound() {
            return found;
        }

        @Override
        public void scan(Object o) {
            if (!found) {
                super.scan(o);
            }
        }

        @Override
        public void visitCtTry(CtTry tryBlock) {
            found = true;
        }
    }

    class NodeExceptionRelatedScanner extends CtScanner {
        private boolean related = false;

        boolean isRelated() {
            return related;
        }

        @Override
        public void scan(CtElement element) {
            if (!related) {
                super.scan(element);
            }
        }

        @Override
        public void visitCtThrow(CtThrow throwStatement) {
            related = true;
        }

        @Override
        public <T> void visitCtInvocation(CtInvocation<T> invocation) {
            if (invocation.getExecutable() != null) {
                try {
                    CtExecutable executable = invocation.getExecutable().getExecutableDeclaration();
                    if (executable != null && !executable.getThrownTypes().isEmpty()) {
                        related = true;
                        return;
                    }
                } catch (RuntimeException e) {
                    // Catch NullPointerException raised in getExecutableDeclaration.
                    // I have build all getFiles in the project, but there are times that
                    // dependencies are broken.

                    // Swallow
                }
            }
            super.visitCtInvocation(invocation);
        }
    }
}
