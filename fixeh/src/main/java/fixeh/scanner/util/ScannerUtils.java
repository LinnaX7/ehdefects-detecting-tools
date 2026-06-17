package fixeh.scanner.util;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import fixeh.project.vcs.Revision;
import fixeh.project.vcs.Vcs;
import fixeh.project.vcs.VcsDiffEntry;
import fixeh.project.vcs.exceptions.InvalidRevisionException;
import fixeh.scanner.treediff.SpoonTreeGenerator;
import fixeh.scanner.treediff.SpoonTreeStore;
import fixeh.scanner.treediff.TreeDiff;
import fixeh.util.ConcurrentUtils;
import spoon.reflect.declaration.CtElement;

/**
 * Created by Shunjie Ding on 08/01/2018.
 */
public final class ScannerUtils {
    public static TreeDiff treeDiffOnEntry(VcsDiffEntry diffEntry, SpoonTreeStore treeStore)
        throws IOException, InvalidRevisionException {
        CtElement leftAst =
            treeStore.getOrBuild(diffEntry.getOldRevision(), diffEntry.getOldFile());
        CtElement rightAst =
            treeStore.getOrBuild(diffEntry.getNewRevision(), diffEntry.getNewFile());

        SpoonTreeGenerator generator = new SpoonTreeGenerator();
        ITree leftGmt = generator.generate(leftAst);
        ITree rightGmt = generator.generate(rightAst);

        return TreeDiff.diff(generator.getTreeContext(), leftGmt, rightGmt);
    }

    public static TreeDiff treeDiffOnEntryAsync(VcsDiffEntry diffEntry, SpoonTreeStore treeStore)
        throws InterruptedException {
        List<Future<CtElement>> astFutures = treeStore.getOrBuildAsync(
            Arrays.asList(Pair.of(diffEntry.getOldRevision(), diffEntry.getOldFile()),
                Pair.of(diffEntry.getNewRevision(), diffEntry.getNewFile())));

        if (astFutures == null) {
            throw new RuntimeException("Task futures should not be null!");
        }

        SpoonTreeGenerator generator = new SpoonTreeGenerator();
        List<ITree> trees = ConcurrentUtils.callAsyncOnFuturesDone(astFutures, generator::generate);
        ITree leftGmt = trees.get(0), rightGmt = trees.get(1);

        return TreeDiff.diff(generator.getTreeContext(), leftGmt, rightGmt);
    }

    public static TreeDiff treeDiffOnRevisionAsync(
        Revision revision, String filePath, SpoonTreeStore treeStore)
        throws InvalidRevisionException, InterruptedException, FileNotFoundException {
        Vcs vcs = revision.getVcs();

        List<VcsDiffEntry> diffEntries = vcs.getDiffEntries(revision);
        Optional<VcsDiffEntry> entryOptional =
            diffEntries.stream()
                .filter(vcsDiffEntry -> vcsDiffEntry.getNewFile().equals(filePath))
                .findFirst();

        if (entryOptional.isPresent()) {
            return treeDiffOnEntryAsync(entryOptional.get(), treeStore);
        }

        throw new FileNotFoundException(filePath + " is not found in revision " + revision.getId());
    }

    public static TreeDiff treeDiffOnRevision(Revision revision, String filePath,
        SpoonTreeStore treeStore) throws InvalidRevisionException, IOException {
        Vcs vcs = revision.getVcs();

        List<VcsDiffEntry> diffEntries = vcs.getDiffEntries(revision);
        Optional<VcsDiffEntry> entryOptional =
            diffEntries.stream()
                .filter(vcsDiffEntry -> vcsDiffEntry.getNewFile().equals(filePath))
                .findFirst();

        if (entryOptional.isPresent()) {
            return treeDiffOnEntry(entryOptional.get(), treeStore);
        }

        throw new FileNotFoundException(filePath + " is not found in revision " + revision.getId());
    }

    public static void buildAllJavaFilesInRevision(Revision revision, SpoonTreeStore treeStore)
        throws InvalidRevisionException {
        Vcs vcs = revision.getVcs();

        List<String> files = vcs.getFiles(revision)
                                 .stream()
                                 .filter(f -> f.endsWith(".java"))
                                 .collect(Collectors.toList());

        files.forEach(file -> {
            try {
                treeStore.getOrBuild(revision, file);
            } catch (IOException | InvalidRevisionException e) {
                e.printStackTrace();
            }
        });
    }

    public static void printGumtree(TreeContext context, ITree tree) {
        new GumtreePrinter(context).print(tree);
    }
}
