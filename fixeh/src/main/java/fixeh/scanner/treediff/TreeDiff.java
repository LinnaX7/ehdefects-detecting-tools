package fixeh.scanner.treediff;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import fixeh.scanner.treediff.changes.Change;
import fixeh.scanner.treediff.changes.Deletion;
import fixeh.scanner.treediff.changes.Insertion;
import fixeh.scanner.treediff.changes.Modification;
import fixeh.scanner.treediff.changes.Movement;
import spoon.reflect.declaration.CtElement;

/**
 * Created by Shunjie Ding on 22/12/2017.
 */
public class TreeDiff {
    // Gumtree properties
    static {
        System.setProperty("gumtree.match.bu.sim", "0.4");
        System.setProperty("gumtree.match.gt.minh", "1");
        System.setProperty("gumtree.match.bu.size", "1000");
    }

    private final TreeContext treeContext;

    private final MappingStore mappingStore;

    private final List<Change> rootChanges;

    private final List<Change> allChanges;

    private final ITree left, right;

    protected TreeDiff(TreeContext treeContext, MappingStore mappingStore, List<Change> rootChanges,
        List<Change> allChanges, ITree left, ITree right) {
        this.treeContext = treeContext;
        this.mappingStore = mappingStore;
        this.rootChanges = rootChanges;
        this.allChanges = allChanges;
        this.left = left;
        this.right = right;
    }

    private static List<Action> getRootActions(MappingStore mappingStore, List<Action> actions) {
        Set<ITree> srcUpdTrees = new HashSet<>();
        Set<ITree> dstUpdTrees = new HashSet<>();
        Set<ITree> srcMvTrees = new HashSet<>();
        Set<ITree> dstMvTrees = new HashSet<>();
        Set<ITree> srcDelTrees = new HashSet<>();
        Set<ITree> dstAddTrees = new HashSet<>();
        Map<ITree, Action> originalActionsSrc = new HashMap<>();
        Map<ITree, Action> originalActionsDst = new HashMap<>();

        for (Action action : actions) {
            final ITree original = action.getNode();
            if (action instanceof Delete) {
                srcDelTrees.add(original);
                originalActionsSrc.put(original, action);
            } else if (action instanceof Insert) {
                dstAddTrees.add(original);
                originalActionsDst.put(original, action);
            } else if (action instanceof Update) {
                ITree dest = mappingStore.getDst(original);
                srcUpdTrees.add(original);
                dstUpdTrees.add(dest);
                originalActionsSrc.put(original, action);
            } else if (action instanceof Move) {
                ITree dest = mappingStore.getDst(original);
                srcMvTrees.add(original);
                dstMvTrees.add(dest);
                originalActionsDst.put(dest, action);
            }
        }

        final List<Action> rootActions =
            srcUpdTrees.stream().map(originalActionsSrc::get).collect(Collectors.toList());
        rootActions.addAll(
            srcDelTrees.stream()
                .filter(t
                    -> !srcDelTrees.contains(t.getParent()) && !srcUpdTrees.contains(t.getParent()))
                .map(originalActionsSrc::get)
                .collect(Collectors.toList()));
        rootActions.addAll(
            dstAddTrees.stream()
                .filter(t
                    -> !dstAddTrees.contains(t.getParent()) && !dstUpdTrees.contains(t.getParent()))
                .map(originalActionsDst::get)
                .collect(Collectors.toList()));
        rootActions.addAll(dstMvTrees.stream()
                               .filter(t -> !dstMvTrees.contains(t.getParent()))
                               .map(originalActionsDst::get)
                               .collect(Collectors.toList()));
        rootActions.removeAll(Collections.singleton(null));
        return rootActions;
    }

    private static List<Change> convertToChanges(MappingStore mappingStore, List<Action> actions) {
        return actions.stream()
            .map(action -> {
                if (action instanceof Insert) {
                    return new Insertion((Insert) action);
                } else if (action instanceof Delete) {
                    return new Deletion((Delete) action);
                } else if (action instanceof Move) {
                    return new Movement((Move) action, mappingStore.getDst(action.getNode()));
                } else if (action instanceof Update) {
                    return new Modification((Update) action, mappingStore.getDst(action.getNode()));
                } else {
                    throw new IllegalArgumentException(
                        "Please support the new type " + action.getClass());
                }
            })
            .collect(Collectors.toList());
    }

    public static TreeDiff diff(TreeContext treeContext, ITree l, ITree r) {
        Matcher matcher = new CompositeMatchers.ClassicGumtree(l, r, new MappingStore());
        matcher.match();

        ActionGenerator actionGenerator = new ActionGenerator(l, r, matcher.getMappings());
        actionGenerator.generate();

        return new TreeDiff(treeContext, matcher.getMappings(),
            convertToChanges(matcher.getMappings(),
                getRootActions(matcher.getMappings(), actionGenerator.getActions())),
            convertToChanges(matcher.getMappings(), actionGenerator.getActions()), l, r);
    }

    public static TreeDiff diff(CtElement l, CtElement r) {
        TreeContext treeContext = new TreeContext();
        return diff(treeContext, new SpoonTreeGenerator(treeContext).generate(l),
            new SpoonTreeGenerator(treeContext).generate(r));
    }

    public static TreeDiff diff(String filename, List<String> classPaths, byte[] l, byte[] r) {
        return diff(new SpoonAstBuilder(classPaths, true).build(filename, l),
            new SpoonAstBuilder(classPaths, true).build(filename, r));
    }

    public static TreeDiff diff(List<String> classPaths, File l, File r)
        throws FileNotFoundException {
        return diff(new SpoonAstBuilder(classPaths, true).build(l),
            new SpoonAstBuilder(classPaths, true).build(r));
    }

    public TreeContext getTreeContext() {
        return treeContext;
    }

    public MappingStore getMappingStore() {
        return mappingStore;
    }

    public CtElement getSrcFromMapping(CtElement dst) {
        return Conventions.nodeOf(mappingStore.getSrc(Conventions.nodeOf(dst)));
    }

    public CtElement getDstFromMapping(CtElement src) {
        return Conventions.nodeOf(mappingStore.getDst(Conventions.nodeOf(src)));
    }

    public String getTypeName(CtElement element) {
        if (element == null) {
            return null;
        }
        ITree node = Conventions.nodeOf(element);
        if (node != null) {
            return treeContext.getTypeLabel(node);
        }
        return String.format("null(%s)", element.getClass().getSimpleName());
    }

    public List<Change> getRootChanges() {
        return rootChanges;
    }

    public List<Change> getAllChanges() {
        return allChanges;
    }

    public ITree getLeftGumtree() {
        return left;
    }

    public ITree getRightGumtree() {
        return right;
    }

    public CtElement getLeftSpoonTree() {
        return Conventions.nodeOf(left);
    }

    public CtElement getRightSpoonTree() {
        return Conventions.nodeOf(right);
    }
}
