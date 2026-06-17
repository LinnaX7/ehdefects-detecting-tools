package fixeh.scanner.treediff;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.TreeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtInheritanceScanner;
import spoon.reflect.visitor.CtScanner;

/**
 * Created by Shunjie Ding on 25/12/2017.
 */
public class SpoonTreeGenerator {
    private final Logger logger = LoggerFactory.getLogger(SpoonTreeGenerator.class);

    private final TreeContext treeContext;

    public SpoonTreeGenerator() {
        treeContext = new TreeContext();
    }

    public SpoonTreeGenerator(TreeContext treeContext) {
        this.treeContext = treeContext;
    }

    private static String getTypeName(String spoonTypeName) {
        // Removes the "Ct" at the beginning and the "Impl" at the end.
        return spoonTypeName.substring(2, spoonTypeName.length() - 4);
    }

    public TreeContext getTreeContext() {
        return treeContext;
    }

    private ITree createNode(String typeName, CtElement element, String label) {
        ITree newNode = createNode(typeName, label);
        Conventions.setMetadata(element, newNode);
        return newNode;
    }

    private ITree createNode(CtElement element, String label) {
        String typeName = getTypeName(element.getClass().getSimpleName());
        return createNode(typeName, element, label);
    }

    private ITree createNode(String typeName, String label) {
        return treeContext.createTree(typeName.hashCode(), label, typeName);
    }

    public ITree generate(CtElement element) {
        ITree dummyRoot = createNode("dummy", "");
        TreeScanner scanner = new TreeScanner(this, dummyRoot);
        scanner.scan(element);
        ITree root = dummyRoot.getChild(0);

        root.refresh();
        TreeUtils.postOrderNumbering(root);
        TreeUtils.computeHeight(root);
        return root;
    }

    protected class TreeScanner extends CtScanner {
        private final SpoonTreeGenerator generator;

        private final Stack<ITree> nodeStack = new Stack<>();

        TreeScanner(SpoonTreeGenerator generator, ITree root) {
            this.generator = generator;
            nodeStack.push(root);
        }

        @Override
        protected void enter(CtElement e) {
            if (e instanceof CtReference) {
                nodeStack.push(null);
                return;
            }

            LabelFinder labelFinder = new LabelFinder();
            labelFinder.scan(e);
            ITree newNode = generator.createNode(e, labelFinder.label);

            ITree parent = nodeStack.peek();
            if (parent != null) {
                parent.addChild(newNode);
            }
            nodeStack.push(newNode);

            // Add modifiers for modifiable nodes
            if (e instanceof CtModifiable) {
                ITree modifierSet = generator.createNode("ModifierSet", "");
                for (ModifierKind kind : ((CtModifiable) e).getModifiers()) {
                    ITree modifier = generator.createNode("Modifier", e, kind.toString());
                    modifierSet.addChild(modifier);
                }
                newNode.addChild(modifierSet);
            }

            // Add variable type
            if (e instanceof CtVariable) {
                try {
                    for (CtTypeReference typeRef : e.getReferencedTypes()) {
                        newNode.addChild(
                            generator.createNode("VariableType", e, typeRef.getQualifiedName()));
                    }
                } catch (Exception ex) {
                    // ignore can not getting type refs
                }
            }
        }

        @Override
        protected void exit(CtElement e) {
            nodeStack.pop();
        }
    }

    protected class LabelFinder extends CtInheritanceScanner {
        public String label = "";

        @Override
        public void scanCtNamedElement(CtNamedElement e) {
            label = e.getSimpleName();
        }

        @Override
        public <T> void scanCtVariableAccess(CtVariableAccess<T> variableAccess) {
            label = variableAccess.getVariable().getSimpleName();
        }

        @Override
        public <T> void visitCtInvocation(CtInvocation<T> invocation) {
            if (invocation.getExecutable() != null) {
                CtTypeReference decl = invocation.getExecutable().getDeclaringType();
                label = (decl != null ? decl.getQualifiedName() : "") + "#"
                    + invocation.getExecutable().getSignature();
            }
        }

        @Override
        public <T> void visitCtConstructorCall(CtConstructorCall<T> ctConstructorCall) {
            if (ctConstructorCall.getExecutable() != null) {
                label = ctConstructorCall.getExecutable().getSignature();
            }
        }

        @Override
        public <T> void visitCtLiteral(CtLiteral<T> literal) {
            label = literal.toString();
        }

        @Override
        public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
            label = operator.getKind().toString();
        }

        @Override
        public <T> void visitCtUnaryOperator(CtUnaryOperator<T> operator) {
            label = operator.getKind().toString();
        }

        @Override
        public <T> void visitCtThisAccess(CtThisAccess<T> thisAccess) {
            label = thisAccess.toString();
        }

        @Override
        public <T> void visitCtTypeAccess(CtTypeAccess<T> typeAccess) {
            if (typeAccess.getAccessedType() != null) {
                label = typeAccess.getAccessedType().getQualifiedName();
            }
        }
    }
}
