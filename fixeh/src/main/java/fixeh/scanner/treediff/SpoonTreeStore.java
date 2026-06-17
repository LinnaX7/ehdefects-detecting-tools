package fixeh.scanner.treediff;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import fixeh.project.vcs.Revision;
import fixeh.project.vcs.Vcs;
import fixeh.project.vcs.exceptions.InvalidRevisionException;
import fixeh.util.ConcurrentUtils;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.support.visitor.equals.CloneHelper;

/**
 * Created by Shunjie Ding on 28/12/2017.
 */
public final class SpoonTreeStore {
    private final Logger logger = LoggerFactory.getLogger(SpoonTreeStore.class);

    private final ConcurrentMap<Pair<Revision, String>, CtElement> storeMap =
        new ConcurrentHashMap<>();

    private final ConcurrentMap<Revision, SpoonAstBuilder> builderMap = new ConcurrentHashMap<>();

    /**
     * Default class paths for new AST builders.
     */
    private final List<String> classPaths;

    public SpoonTreeStore(List<String> classPaths) {
        this.classPaths = classPaths;
    }

    private static CtElement cloneTree(CtElement element) {
        if (element == null) {
            return null;
        }
        // Spoon 5.9.0
        // CloneVisitor cloneVisitor = new CloneVisitor();
        // cloneVisitor.scan(element);
        // return cloneVisitor.getClone();

        // Spoon 6.1.0
        return CloneHelper.INSTANCE.clone(element);
    }

    public List<String> getClassPaths() {
        return classPaths == null ? null : new ArrayList<>(classPaths);
    }

    private SpoonAstBuilder getAstBuilder(Revision revision) {
        builderMap.putIfAbsent(revision, new SpoonAstBuilder(classPaths, true));
        return builderMap.get(revision);
    }

    public SpoonAstBuilder setAstBuilder(Revision revision, SpoonAstBuilder astBuilder) {
        return builderMap.put(revision, astBuilder);
    }

    public boolean containsBuilder(Revision revision) {
        return builderMap.containsKey(revision);
    }

    public CtElement put(Revision revision, String filename, CtElement element) {
        Conventions.setTreeStore(element, this);
        return storeMap.put(Pair.of(revision, filename), element);
    }

    public CtElement get(Revision revision, String filename) {
        return storeMap.get(Pair.of(revision, filename));
    }

    public CtElement remove(Revision revision, String filename) {
        CtElement element = storeMap.remove(Pair.of(revision, filename));
        Conventions.setTreeStore(element, null);
        return element;
    }

    public void removeAll(Revision revision) {
        // Remove build to avoid memory leak
        builderMap.remove(revision);

        List<String> files = storeMap.entrySet()
                                 .stream()
                                 .filter(e -> e.getKey().getLeft().equals(revision))
                                 .map(e -> e.getKey().getRight())
                                 .collect(Collectors.toList());
        files.forEach(f -> remove(revision, f));
    }

    public boolean contains(Revision revision, String filename) {
        return storeMap.containsKey(Pair.of(revision, filename));
    }

    public CtElement getOrBuild(Revision revision, String filename)
        throws IOException, InvalidRevisionException {
        if (contains(revision, filename)) {
            return get(revision, filename);
        }

        // Else build the corresponding tree with given class paths.
        Vcs vcs = revision.getVcs();
        CtType root = getAstBuilder(revision).build(filename, vcs.readFile(revision, filename));
        if (root == null) {
            logger.warn("Can not get the AST of {} in revision {}!", filename, revision.getId());
        } else {
            put(revision, filename, root);
        }
        return root;
    }

    public List<Future<CtElement>> getOrBuildAsync(List<Pair<Revision, String>> builds)
        throws InterruptedException {
        // If there are invalid builds, reject all builds
        if (builds.stream().anyMatch(build -> {
                Revision revision = build.getLeft();
                String filename = build.getRight();

                if (revision == null || filename == null || filename.isEmpty()) {
                    logger.warn("Will not build AST for incomplete build!");
                    return true;
                }
                return false;
            })) {
            logger.warn("Builds are rejected because of invalid builds.");
            return null;
        }

        // Submit all tasks
        return ConcurrentUtils.defaultExecutor().invokeAll(
            builds.stream()
                .map(build -> {
                    Revision revision = build.getLeft();
                    String filename = build.getRight();
                    return (Callable<CtElement>) () -> getOrBuild(revision, filename);
                })
                .collect(Collectors.toList()));
    }

    public CtElement getClone(Revision revision, String filename) {
        return cloneTree(get(revision, filename));
    }

    public void clear() {
        storeMap.clear();
    }
}
