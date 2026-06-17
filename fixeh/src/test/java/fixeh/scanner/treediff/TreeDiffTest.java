package fixeh.scanner.treediff;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.List;
import fixeh.TestBase;
import fixeh.project.build.BuildSystem;
import fixeh.project.build.BuildSystemType;
import fixeh.project.build.exceptions.UnsupportedBuildSystemException;
import fixeh.project.vcs.Revision;
import fixeh.project.vcs.exceptions.InvalidRevisionException;
import fixeh.scanner.treediff.changes.Change;
import fixeh.scanner.treediff.changes.Insertion;
import fixeh.scanner.treediff.changes.Modification;
import fixeh.scanner.util.ScannerUtils;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtMethod;

/**
 * Created by Shunjie Ding on 08/01/2018.
 */
public class TreeDiffTest extends TestBase {
    private final Logger logger = LoggerFactory.getLogger(TreeDiffTest.class);

    private List<String> classPaths;

    private SpoonTreeStore treeStore;

    @Before
    public void setUp() throws UnsupportedBuildSystemException {
        if (classPaths == null) {
            classPaths = BuildSystem.getBuildSystem(BuildSystemType.GRADLE, project.getPath())
                             .getClassPaths();
            treeStore = new SpoonTreeStore(classPaths);
            logger.info("Class paths used for builds are {}", classPaths);
        }
    }

    @Test
    public void testTreeDiff()
        throws InvalidRevisionException, FileNotFoundException, InterruptedException {
        // Find revision 9d52178ebd16d6c7a87fc6ed3d0cf8acc68532ba,
        // which add a new finally block
        Revision revision =
            project.getVcs().getRevision("9d52178ebd16d6c7a87fc6ed3d0cf8acc68532ba");
        TreeDiff diff =
            ScannerUtils.treeDiffOnRevisionAsync(revision, "src/main/java/Main.java", treeStore);

        List<Change> changes = diff.getRootChanges();
        Assert.assertEquals(1, changes.size());
        Assert.assertTrue(changes.get(0) instanceof Insertion);
        Assert.assertTrue(changes.get(0).getRightNode() instanceof CtBlock);
        Assert.assertTrue(changes.get(0).getRightNode().getParent() instanceof CtTry);
    }

    @Test
    public void testTreeDiffCreateMethod()
        throws InvalidRevisionException, FileNotFoundException, InterruptedException {
        // Find revision 5310cd2c7cc88f9479c217dace94a831a564d626,
        // which add a new method in src/main/java/Main.java
        Revision revision =
            project.getVcs().getRevision("5310cd2c7cc88f9479c217dace94a831a564d626");
        TreeDiff diff =
            ScannerUtils.treeDiffOnRevisionAsync(revision, "src/main/java/Main.java", treeStore);

        List<Change> changes = diff.getRootChanges();
        Assert.assertEquals(1, changes.size());
        Assert.assertTrue(changes.get(0) instanceof Insertion);
        Assert.assertTrue(changes.get(0).getRightNode() instanceof CtMethod);
    }

    @Test
    public void testTreeDiffEnlargeException()
        throws InvalidRevisionException, InterruptedException, FileNotFoundException {
        // Find revision abb23f9c086e2ba036a484fbc0eb27eef958b364,
        // which change caught exception from IOException to Exception
        Revision revision =
            project.getVcs().getRevision("abb23f9c086e2ba036a484fbc0eb27eef958b364");
        TreeDiff diff =
            ScannerUtils.treeDiffOnRevisionAsync(revision, "src/main/java/Main.java", treeStore);

        List<Change> changes = diff.getRootChanges();
        Assert.assertEquals(1, changes.size());
        Assert.assertTrue(changes.get(0) instanceof Modification);
        Assert.assertTrue(
            changes.get(0).getRawRightNodeType(diff.getTreeContext()).equals("VariableType"));
        Assert.assertTrue(changes.get(0).getRightNode() instanceof CtCatchVariable);
    }

    @Test
    public void testTreeDiffRethrowException()
        throws InvalidRevisionException, InterruptedException, FileNotFoundException {
        // Find revision 7f5d74dc5fc0e1c78e0aaa68af649ae8bd5cb95d,
        // which rethrow exception in catch block
        Revision revision =
            project.getVcs().getRevision("7f5d74dc5fc0e1c78e0aaa68af649ae8bd5cb95d");
        TreeDiff diff =
            ScannerUtils.treeDiffOnRevisionAsync(revision, "src/main/java/Main.java", treeStore);

        List<Change> changes = diff.getRootChanges();
        Assert.assertEquals(1, changes.size());
        Assert.assertTrue(changes.get(0) instanceof Insertion);
        Assert.assertTrue(changes.get(0).getLeftNode().getParent() instanceof CtCatch);
        Assert.assertTrue(changes.get(0).getRightNode() instanceof CtThrow);
    }
}
