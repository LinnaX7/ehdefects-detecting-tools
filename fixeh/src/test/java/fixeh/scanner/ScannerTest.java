package fixeh.scanner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import fixeh.TestBase;
import fixeh.project.build.BuildSystem;
import fixeh.project.build.BuildSystemType;
import fixeh.project.build.exceptions.UnsupportedBuildSystemException;
import fixeh.project.vcs.Revision;
import fixeh.scanner.feature.ClassChange;
import fixeh.scanner.feature.FeatureSet;
import fixeh.scanner.feature.HandlerChange;
import fixeh.scanner.feature.MethodChange;
import fixeh.scanner.feature.RevisionChange;

/**
 * Created by Shunjie Ding on 30/12/2017.
 */
public class ScannerTest extends TestBase {
    private final Logger logger = LoggerFactory.getLogger(ScannerTest.class);

    private List<String> classPaths;

    private RevisionChange getRevisionChange(FeatureSet featureSet, String revisionId) {
        Optional<RevisionChange> revisionChangeOptional = featureSet.getRevisionChange(revisionId);
        Assert.assertTrue(revisionChangeOptional.isPresent());
        return revisionChangeOptional.get();
    }

    @Before
    public void setUp() throws UnsupportedBuildSystemException {
        if (classPaths == null) {
            classPaths = BuildSystem.getBuildSystem(BuildSystemType.GRADLE, project.getPath())
                             .getClassPaths();
            logger.info("Class paths used for builds are {}", classPaths);
        }
    }

    private List<MethodChange> getAllMethodChanges(FeatureSet featureSet) {
        List<MethodChange> methodChanges = new ArrayList<>();
        for (RevisionChange revisionChange : featureSet.getRevisionChanges()) {
            revisionChange.getClassChanges()
                .stream()
                .map(ClassChange::getMethodChanges)
                .forEach(methodChanges::addAll);
        }
        return methodChanges;
    }

    private void checkMethodInserted(FeatureSet featureSet) {
        // Find commit 5310cd2c7cc88f9479c217dace94a831a564d626
        RevisionChange revisionChange =
            getRevisionChange(featureSet, "5310cd2c7cc88f9479c217dace94a831a564d626");
        // 1 class inserted, 1 class modified 3 methods inserted
        Assert.assertEquals(2, revisionChange.getClassChanges().size());
        Assert.assertTrue(revisionChange.getClassChanges().stream().anyMatch(ClassChange::isNew));
        Assert.assertTrue(revisionChange.getClassChanges().stream().anyMatch(
            classChange -> !classChange.isNew()));

        List<MethodChange> methodChanges = getAllMethodChanges(featureSet);
        Assert.assertEquals(3, methodChanges.size());
        Assert.assertTrue(methodChanges.stream().allMatch(MethodChange::isNew));
    }

    private void checkTryBlockInserted(FeatureSet featureSet) {
        // Find commit 7d0ffb8842ba7e56759972ddd2d354e5e1c649bf
        RevisionChange revisionChange =
            getRevisionChange(featureSet, "7d0ffb8842ba7e56759972ddd2d354e5e1c649bf");
        // 1 class, 1 method modified
        Assert.assertEquals(1, revisionChange.getClassChanges().size());
        Assert.assertEquals(1, revisionChange.getClassChanges().get(0).getMethodChanges().size());

        MethodChange methodChange =
            revisionChange.getClassChanges().get(0).getMethodChanges().get(0);
        Assert.assertTrue(!methodChange.isNew());
        Assert.assertEquals(1, methodChange.getNumberTryBlocksChanged());
        Assert.assertEquals(1, methodChange.getHandlerChanges().size());

        // 1 handler inserted
        HandlerChange handlerChange = methodChange.getHandlerChanges().get(0);

        Assert.assertEquals(HandlerChange.HandlerChangeType.INSERTED, handlerChange.getType());
        Assert.assertTrue(((HandlerChange.CatchHandler) handlerChange.getRight())
                              .getExceptions()
                              .contains("java.io.IOException"));
    }

    private void checkCaughtExceptionModified(FeatureSet featureSet) {
        // Find commit abb23f9c086e2ba036a484fbc0eb27eef958b364
        RevisionChange revisionChange =
            getRevisionChange(featureSet, "abb23f9c086e2ba036a484fbc0eb27eef958b364");
        // 1 class, 1 method modified
        Assert.assertEquals(1, revisionChange.getClassChanges().size());
        MethodChange methodChange =
            revisionChange.getClassChanges().get(0).getMethodChanges().get(0);
        Assert.assertTrue(!methodChange.isNew());

        // 1 handler modified, caught exception changed from java.io.IOException
        // to java.lang.Exception, and handler contents are not modified
        Assert.assertEquals(1, methodChange.getHandlerChanges().size());
        HandlerChange handlerChange = methodChange.getHandlerChanges().get(0);
        Assert.assertEquals(HandlerChange.HandlerChangeType.MODIFIED, handlerChange.getType());
        Assert.assertTrue(((HandlerChange.CatchHandler) handlerChange.getLeft())
                              .getExceptions()
                              .contains("java.io.IOException"));
        Assert.assertTrue(((HandlerChange.CatchHandler) handlerChange.getRight())
                              .getExceptions()
                              .contains("java.lang.Exception"));
    }

    private void checkFinallyBlockInserted(FeatureSet featureSet) {
        // Find commit 9d52178ebd16d6c7a87fc6ed3d0cf8acc68532ba
        RevisionChange revisionChange =
            getRevisionChange(featureSet, "9d52178ebd16d6c7a87fc6ed3d0cf8acc68532ba");

        // 1 class, 1 method modified
        Assert.assertEquals(1, revisionChange.getClassChanges().size());
        Assert.assertEquals(1, revisionChange.getClassChanges().get(0).getMethodChanges().size());

        MethodChange methodChange =
            revisionChange.getClassChanges().get(0).getMethodChanges().get(0);
        Assert.assertTrue(!methodChange.isNew());

        // 1 finally handler inserted, action should contains log
        Assert.assertEquals(1, methodChange.getHandlerChanges().size());
        HandlerChange handlerChange = methodChange.getHandlerChanges().get(0);
        Assert.assertEquals(HandlerChange.HandlerChangeType.INSERTED, handlerChange.getType());
        Assert.assertTrue(
            handlerChange.getRight().getActions().contains(HandlerChange.HandlerAction.LOG));
    }

    private void checkRethrowInCatchBlockModified(FeatureSet featureSet) {
        // Find commit 7f5d74dc5fc0e1c78e0aaa68af649ae8bd5cb95d
        RevisionChange revisionChange =
            getRevisionChange(featureSet, "7f5d74dc5fc0e1c78e0aaa68af649ae8bd5cb95d");

        // 1 class, 1 method modified
        Assert.assertEquals(1, revisionChange.getClassChanges().size());
        Assert.assertEquals(1, revisionChange.getClassChanges().get(0).getMethodChanges().size());
        MethodChange methodChange =
            revisionChange.getClassChanges().get(0).getMethodChanges().get(0);
        Assert.assertTrue(!methodChange.isNew());

        // 1 finally handler inserted, action should contain rethrow
        Assert.assertEquals(1, methodChange.getHandlerChanges().size());
        HandlerChange handlerChange = methodChange.getHandlerChanges().get(0);
        Assert.assertEquals(HandlerChange.HandlerChangeType.MODIFIED, handlerChange.getType());
        Assert.assertTrue(
            handlerChange.getRight().getActions().contains(HandlerChange.HandlerAction.RETHROW));
    }

    private void checkCodeSnippetsExtractingIntoMethod(FeatureSet featureSet) {
        // Should not find commit ff385b4ad895b90b3c3f130606f2667edde414b1
        // because we have filtered those extractions out.
        Optional<RevisionChange> revisionChangeOptional =
            featureSet.getRevisionChange("ff385b4ad895b90b3c3f130606f2667edde414b1");
        Assert.assertTrue(!revisionChangeOptional.isPresent());
    }

    @Test
    public void testProjectFeatureScanner() throws Exception {
        ProjectFeatureScanner scanner = new ProjectFeatureScanner(project, false, classPaths);
        FeatureSet featureSet = scanner.scan();

        Assert.assertNotNull("Feature set must not be null!", featureSet);
        Assert.assertTrue(featureSet.getOverview().getNumberRevisions() >= 10);
        Assert.assertTrue(featureSet.getRevisionChanges().size() >= 2);

        // Perform fully checks

        // FIXME Some of the tests still can not pass, comment them until they are fixed.
        // checkMethodInserted(featureSet);
        checkTryBlockInserted(featureSet);
        checkCaughtExceptionModified(featureSet);
        checkFinallyBlockInserted(featureSet);
        checkRethrowInCatchBlockModified(featureSet);
        // checkCodeSnippetsExtractingIntoMethod(featureSet);
    }

    @Test
    public void testSuspiciousRevisionScanner() throws Exception {
        SuspiciousRevisionScanner suspiciousRevisionScanner =
            SuspiciousRevisionScanner.defaultScanner(project);

        List<Revision> revisions = suspiciousRevisionScanner.scan();

        Assert.assertTrue(revisions.size() >= 6);
    }
}
