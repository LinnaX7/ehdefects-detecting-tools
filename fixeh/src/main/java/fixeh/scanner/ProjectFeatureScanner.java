package fixeh.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import fixeh.project.Project;
import fixeh.project.vcs.Revision;
import fixeh.project.vcs.Vcs;
import fixeh.project.vcs.VcsDiffEntry;
import fixeh.project.vcs.exceptions.InvalidRevisionException;
import fixeh.scanner.feature.ClassChange;
import fixeh.scanner.feature.FeatureSet;
import fixeh.scanner.feature.HandlerChange;
import fixeh.scanner.feature.MethodChange;
import fixeh.scanner.feature.RevisionChange;
import fixeh.scanner.treediff.SpoonTreeStore;
import fixeh.scanner.treediff.TreeDiff;
import fixeh.scanner.treediff.changes.Change;
import fixeh.scanner.treediff.changes.Deletion;
import fixeh.scanner.treediff.changes.Insertion;
import fixeh.scanner.treediff.changes.Modification;
import fixeh.scanner.treediff.changes.Movement;
import fixeh.scanner.trick.RecursiveTrickApplier;
import fixeh.scanner.trick.ReverseNewMethodInRightTree;
import fixeh.scanner.util.ScannerUtils;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtBreak;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtContinue;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;

/**
 * Created by Shunjie Ding on 19/12/2017.
 */
public class ProjectFeatureScanner extends AbstractScanner<Project, FeatureSet> {
    private final Logger logger = LoggerFactory.getLogger(ProjectFeatureScanner.class);
    private final SuspiciousRevisionScanner suspiciousRevisionScanner;

    private final SpoonTreeStore treeStore;

    private final boolean buildRevision;
    private final Map<String, Integer> revisionUsefulMap = new HashMap<>();

    public ProjectFeatureScanner(
        Project target, boolean scanSuspicious, List<String> classPaths, boolean buildRevision) {
        super(target);
        this.treeStore = new SpoonTreeStore(classPaths);
        this.buildRevision = buildRevision;
        if (scanSuspicious) {
            this.suspiciousRevisionScanner = SuspiciousRevisionScanner.defaultScanner(target);
        } else {
            this.suspiciousRevisionScanner = null;
        }
    }

    public ProjectFeatureScanner(Project target, boolean scanSuspicious, List<String> classPaths) {
        this(target, scanSuspicious, classPaths, false);
    }

    public ProjectFeatureScanner(SuspiciousRevisionScanner suspiciousRevisionScanner,
        List<String> classPaths, boolean buildRevision) {
        super(suspiciousRevisionScanner.getTarget());
        this.suspiciousRevisionScanner = suspiciousRevisionScanner;
        this.treeStore = new SpoonTreeStore(classPaths);
        this.buildRevision = buildRevision;
    }

    public ProjectFeatureScanner(
        SuspiciousRevisionScanner suspiciousRevisionScanner, List<String> classPaths) {
        this(suspiciousRevisionScanner, classPaths, false);
    }

    public SpoonTreeStore getTreeStore() {
        return treeStore;
    }

    private synchronized void increaseUsefulness(String id) {
        revisionUsefulMap.put(id, revisionUsefulMap.getOrDefault(id, 0) + 1);
    }

    private synchronized void decreaseUsefulness(String id) {
        if (revisionUsefulMap.containsKey(id)) {
            int value = revisionUsefulMap.get(id);
            if (--value == 0) {
                revisionUsefulMap.remove(id);
            } else {
                revisionUsefulMap.put(id, value);
            }
        }
    }

    private synchronized boolean isUseless(String id) {
        return !revisionUsefulMap.containsKey(id);
    }

    private void initializeUsefulMap(List<Revision> revisions) {
        revisions.stream().filter(r -> r.parentCounts() == 1).forEach(r -> {
            increaseUsefulness(r.getId());
            increaseUsefulness(r.getParentIds().get(0));
        });
    }

    @Override
    protected FeatureSet scan(Project target) throws Exception {
        logger.info("Class paths used for AST builds for project {} are {}", target.getName(),
            treeStore.getClassPaths());

        List<Revision> revisionList;
        if (suspiciousRevisionScanner != null) {
            revisionList = suspiciousRevisionScanner.scan(target);
        } else {
            revisionList = target.getVcs().getRevisions();
        }

        initializeUsefulMap(revisionList);

        // Specify tricks to be applied

        RecursiveTrickApplier<TreeDiff> treeDiffRecursiveTrickApplier =
            new RecursiveTrickApplier<>(Collections.singleton(new ReverseNewMethodInRightTree()));

        // Scan for revisions in parallel
        logger.info("Scanning revisions(size {}) to collect features ...", revisionList.size());

        List<RevisionChange> changes =
            revisionList.parallelStream()
                .map(revision -> {
                    try {
                        return new RevisionFeatureScanner(
                            revision, treeStore, treeDiffRecursiveTrickApplier)
                            .scan();
                    } catch (Exception e) {
                        logger.error("Exception occurred when scan features for revision {}",
                            revision.getId());
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Remove invalid changes
        changes = changes.stream()
                      .filter(revisionChange -> !revisionChange.getClassChanges().isEmpty())
                      .collect(Collectors.toList());

        logger.info("Find {} valid class changes!", changes.size());

        return FeatureSet.newFeatureSet(target, changes);
    }

    protected class RevisionFeatureScanner
        extends AbstractSpoonRelatedScanner<Revision, RevisionChange> {
        private final Logger logger = LoggerFactory.getLogger(RevisionFeatureScanner.class);

        private final SpoonTreeStore treeStore;

        private final RecursiveTrickApplier<TreeDiff> treeDiffRecursiveTrickApplier;

        public RevisionFeatureScanner(Revision target, SpoonTreeStore treeStore,
            RecursiveTrickApplier<TreeDiff> treeDiffRecursiveTrickApplier) {
            super(target);
            this.treeStore = treeStore;
            this.treeDiffRecursiveTrickApplier = treeDiffRecursiveTrickApplier;
        }

        protected String getClassName(TreeDiff treeDiff) {
            CtElement rightSpoonTree = treeDiff.getRightSpoonTree();
            if (rightSpoonTree instanceof CtType) {
                return ((CtType) rightSpoonTree).getQualifiedName();
            }
            return "";
        }

        protected boolean isCreateChange(VcsDiffEntry diffEntry) {
            return diffEntry.getChangeType() == VcsDiffEntry.ChangeType.CREATE;
        }

        protected boolean isPublicClass(TreeDiff treeDiff) {
            CtElement rightSpoonTree = treeDiff.getRightSpoonTree();
            return rightSpoonTree instanceof CtType
                && ((CtType) rightSpoonTree).getModifiers().contains(ModifierKind.PUBLIC);
        }

        protected boolean inTryBlock(Change change) {
            // FIXME consider MOVE from non-try to try
            return inTryBlock(change.getLeftNode()) || inTryBlock(change.getRightNode());
        }

        protected boolean inTryBody(Change change) {
            return inTryBody(change.getLeftNode()) || inTryBody(change.getRightNode());
        }

        protected Set<HandlerChange.HandlerAction> recognizeHandlerActions(
            CtStatementList block, boolean isCatch) {
            Set<HandlerChange.HandlerAction> actions = new HashSet<>();
            List<CtStatement> statements = block.getStatements();
            if (statements.isEmpty()) {
                if (isCatch) {
                    // swallow for catch
                    return Collections.singleton(HandlerChange.HandlerAction.SWALLOW);
                } else {
                    // empty for finally
                    return Collections.singleton(HandlerChange.HandlerAction.EMPTY);
                }
            }

            // FIXME There are still two types (ASK_FOR_USER_INPUT/CLEAN_UP) of
            // actions I have no idea how to recognize.

            final List<String> loggerKeywords =
                Arrays.asList("info", "warn", "printStackTrace", "debug", "error", "print");
            for (CtStatement statement : statements) {
                if (statement instanceof CtInvocation) {
                    // If invoke log methods, recognize as LOG. if invoke methods
                    // beginning with set, recognize as UPDATE_STATE

                    CtInvocation invocation = (CtInvocation) statement;
                    CtExecutableReference executableRef = invocation.getExecutable();
                    // FIXME we can do it more accurately, by checking method signature,
                    // e.g. java.lang.Exception::printStackTrace,
                    // java.lang.logging.Logger.info/warning,
                    // org.slf4j.Logger.info/warn/error/debug,
                    // java.lang.PrintStream.print(f/ln)
                    String executableSignature = executableRef.getSignature();
                    if (!actions.contains(HandlerChange.HandlerAction.LOG)
                        && loggerKeywords.stream().anyMatch(executableSignature::contains)) {
                        actions.add(HandlerChange.HandlerAction.LOG);
                    }

                    if (executableRef.getSimpleName().startsWith("set")) {
                        actions.add(HandlerChange.HandlerAction.UPDATE_STATE);
                    }
                } else if (statement instanceof CtReturn || statement instanceof CtBreak
                    || statement instanceof CtContinue) {
                    // If statement is one of return/break/continue, recognize as
                    // CHANGE_CONTROL_FLOW

                    actions.add(HandlerChange.HandlerAction.CHANGE_CONTROL_FLOW);
                } else if (statement instanceof CtAssignment) {
                    // If statement is an assignment to class field, recognize as
                    // UPDATE_STATE

                    CtAssignment assignment = (CtAssignment) statement;
                    CtExpression assigned = assignment.getAssigned();
                    if (assigned instanceof CtFieldAccess) {
                        actions.add(HandlerChange.HandlerAction.UPDATE_STATE);
                    }
                } else if (statement instanceof CtThrow) {
                    if (isCatch) {
                        actions.add(HandlerChange.HandlerAction.RETHROW);
                    } else {
                        // If throw in finally block, recognize as a CHANGE_CONTROL_FLOW
                        actions.add(HandlerChange.HandlerAction.CHANGE_CONTROL_FLOW);
                    }
                }
            }

            if (actions.isEmpty()) {
                actions.add(HandlerChange.HandlerAction.OTHER);
            }
            return actions;
        }

        // FIXME Should we autoDetect caught exception related?
        protected boolean isChangeExceptionRelated(Change change) {
            if (change == null) {
                return false;
            }
            return isNodeExceptionRelated(change.getLeftNode())
                || isNodeExceptionRelated(change.getRightNode());
        }

        protected HandlerChange.CatchHandler newCatchHandler(CtCatch ctCatch) {
            return HandlerChange.newCatchHandler(ctCatch.toString(),
                recognizeHandlerActions(ctCatch.getBody(), true),
                ctCatch.getParameter()
                    .getReferencedTypes()
                    .stream()
                    .map(CtTypeInformation::getQualifiedName)
                    .collect(Collectors.toSet()));
        }

        protected HandlerChange.FinallyHandler newFinallyHandler(CtBlock finallyBlock) {
            return HandlerChange.newFinallyHandler(
                finallyBlock.toString(), recognizeHandlerActions(finallyBlock, false));
        }

        protected HandlerChange.FinallyHandler newFinallyHandler(CtTry ctTry) {
            if (ctTry.getFinalizer() != null) {
                CtBlock finallyBlock = ctTry.getFinalizer();
                return HandlerChange.newFinallyHandler(
                    finallyBlock.toString(), recognizeHandlerActions(finallyBlock, false));
            }
            return null;
        }

        protected boolean containsTryBlock(Change change) {
            return containsTryBlock(change.getLeftNode())
                || containsTryBlock(change.getRightNode());
        }

        @SuppressWarnings("unchecked")
        protected MethodChange scanChangesInMethod(
            CtMethod method, List<Change> changes, TreeDiff treeDiff) {
            // Filter out changes not contain or in try blocks.
            List<Change> changesInTryBlock =
                changes.stream().filter(this ::inTryBlock).collect(Collectors.toList());

            List<Change> changesContainTryBlock = changes.stream()
                                                      .filter(c -> !changesInTryBlock.contains(c))
                                                      .filter(this ::containsTryBlock)
                                                      .collect(Collectors.toList());

            // Detect changed try blocks, where changes must be exception related.
            HashSet<CtTry> changedTrySet =
                changes.stream()
                    .map(change -> {
                        if (inTryBody(change) && isNodeExceptionRelated(change.getRightNode())) {
                            CtElement leftNode = change.getLeftNode();
                            return leftNode == null ? null : getParentTry(leftNode, true);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(HashSet::new));

            HashSet<CtCatch> catchScanned = new HashSet<>();
            HashSet<CtBlock> finallyScanned = new HashSet<>();

            List<HandlerChange> handlerChanges = new ArrayList<>();

            // Scan for changes that contain try block (and not in try block)

            for (Change change : changesContainTryBlock) {
                ChangeContainsTryBlockFeatureScanner scanner =
                    new ChangeContainsTryBlockFeatureScanner(change, catchScanned, finallyScanned);
                handlerChanges.addAll(scanner.scan());
            }

            // Scan for changes in try block

            for (Change change : changesInTryBlock) {
                if (change instanceof Insertion) {
                    CtElement inserted = change.getRightNode();

                    // Ignore changes outside try block

                    CtElement leftNode = change.getLeftNode();
                    CtTry leftTry = getParentTry(leftNode, true);
                    if (leftTry == null || changedTrySet.contains(leftTry)) {
                        continue;
                    }

                    // Insert a new try block
                    if (inserted instanceof CtTry) {
                        for (CtCatch ctCatch : ((CtTry) inserted).getCatchers()) {
                            catchScanned.add(ctCatch);
                            handlerChanges.add(
                                HandlerChange.newInserted(null, newCatchHandler(ctCatch)));
                        }
                        if (((CtTry) inserted).getFinalizer() != null) {
                            finallyScanned.add(((CtTry) inserted).getFinalizer());
                        }
                        HandlerChange.FinallyHandler finallyHandler =
                            newFinallyHandler((CtTry) inserted);
                        if (finallyHandler != null) {
                            handlerChanges.add(HandlerChange.newInserted(null, finallyHandler));
                        }

                        continue;
                    }

                    // Insert in catch block or finally block

                    if (inCatchBlock(inserted)) {
                        if (inserted instanceof CtCatch) {
                            // Insert a catch block
                            catchScanned.add((CtCatch) inserted);
                            handlerChanges.add(HandlerChange.newInserted(
                                null, newCatchHandler((CtCatch) inserted)));
                        } else {
                            // Insert in catch block
                            CtCatch rightCatch = getParentCatch(inserted, false);
                            CtElement leftCatch = treeDiff.getSrcFromMapping(rightCatch);
                            if (!(leftCatch instanceof CtCatch)) {
                                logger.warn("Mapping's not match: {} and {}!",
                                    treeDiff.getTypeName(leftCatch),
                                    treeDiff.getTypeName(rightCatch));
                                continue;
                            }

                            if (catchScanned.contains(leftCatch)
                                || catchScanned.contains(rightCatch)) {
                                continue;
                            }

                            catchScanned.add((CtCatch) leftCatch);
                            catchScanned.add(rightCatch);

                            handlerChanges.add(HandlerChange.newModified(
                                newCatchHandler((CtCatch) leftCatch), newCatchHandler(rightCatch)));
                        }
                    } else if (inFinallyBlock(inserted)) {
                        if (inserted instanceof CtBlock) {
                            // Insert a finally block
                            finallyScanned.add((CtBlock) inserted);
                            handlerChanges.add(HandlerChange.newInserted(
                                null, newFinallyHandler((CtBlock) inserted)));
                        } else {
                            // Insert in finally block
                            CtBlock rightFinallyBlock = getParentFinally(inserted, false);
                            CtElement leftFinallyBlock =
                                treeDiff.getSrcFromMapping(rightFinallyBlock);
                            if (!(leftFinallyBlock instanceof CtBlock)) {
                                logger.warn("Mapping's not match: {} and {}!",
                                    treeDiff.getTypeName(leftFinallyBlock),
                                    treeDiff.getTypeName(rightFinallyBlock));
                                continue;
                            }

                            if (!isDirectFinallyBlock((CtBlock) leftFinallyBlock)) {
                                logger.warn("Mapping's not match: not a finally block!");
                                continue;
                            }

                            if (finallyScanned.contains(leftFinallyBlock)
                                || finallyScanned.contains(rightFinallyBlock)) {
                                continue;
                            }

                            finallyScanned.add((CtBlock) leftFinallyBlock);
                            finallyScanned.add(rightFinallyBlock);

                            handlerChanges.add(HandlerChange.newModified(
                                newFinallyHandler((CtBlock) leftFinallyBlock),
                                newFinallyHandler(rightFinallyBlock)));
                        }
                    }
                } else if (change instanceof Deletion) {
                    CtElement deleted = change.getLeftNode();

                    // Ignore changes outside try block
                    CtTry leftTry = getParentTry(deleted, true);
                    if (leftTry == null || changedTrySet.contains(leftTry)) {
                        continue;
                    }

                    // Delete a try block

                    if (deleted instanceof CtTry) {
                        for (CtCatch ctCatch : ((CtTry) deleted).getCatchers()) {
                            catchScanned.add(ctCatch);
                            handlerChanges.add(
                                HandlerChange.newDeleted(newCatchHandler(ctCatch), null));
                        }
                        if (((CtTry) deleted).getFinalizer() != null) {
                            finallyScanned.add(((CtTry) deleted).getFinalizer());
                        }
                        HandlerChange.FinallyHandler finallyHandler =
                            newFinallyHandler((CtTry) deleted);
                        if (finallyHandler != null) {
                            handlerChanges.add(HandlerChange.newInserted(null, finallyHandler));
                        }

                        continue;
                    }

                    // Delete in catch or finally block
                    if (inCatchBlock(deleted)) {
                        if (deleted instanceof CtCatch) {
                            // Delete a catch
                            catchScanned.add((CtCatch) deleted);
                            handlerChanges.add(
                                HandlerChange.newDeleted(newCatchHandler((CtCatch) deleted), null));
                        } else {
                            // Delete in catch
                            CtCatch leftCatch = getParentCatch(deleted, false);
                            CtElement rightCatch = treeDiff.getDstFromMapping(leftCatch);
                            if (!(rightCatch instanceof CtCatch)) {
                                logger.warn("Mapping's not match: {} and {}!",
                                    treeDiff.getTypeName(leftCatch),
                                    treeDiff.getTypeName(rightCatch));
                                continue;
                            }

                            if (catchScanned.contains(leftCatch)
                                || catchScanned.contains(rightCatch)) {
                                continue;
                            }

                            catchScanned.add(leftCatch);
                            catchScanned.add((CtCatch) rightCatch);

                            handlerChanges.add(HandlerChange.newModified(
                                newCatchHandler(leftCatch), newCatchHandler((CtCatch) rightCatch)));
                        }
                    } else if (inFinallyBlock(deleted)) {
                        if (deleted instanceof CtBlock) {
                            // Delete a finally
                            finallyScanned.add((CtBlock) deleted);
                            handlerChanges.add(HandlerChange.newDeleted(
                                newFinallyHandler((CtBlock) deleted), null));
                        } else {
                            // Delete in finally block
                            CtBlock leftFinallyBlock = getParentFinally(deleted, false);
                            CtElement rightFinallyBlock =
                                treeDiff.getDstFromMapping(leftFinallyBlock);
                            if (!(rightFinallyBlock instanceof CtBlock)) {
                                logger.warn("Mapping's not match: {} and {}!",
                                    treeDiff.getTypeName(leftFinallyBlock),
                                    treeDiff.getTypeName(rightFinallyBlock));
                                continue;
                            }

                            if (!isDirectFinallyBlock((CtBlock) rightFinallyBlock)) {
                                logger.warn("Mapping's not match: not a finally block");
                                continue;
                            }

                            if (finallyScanned.contains(leftFinallyBlock)
                                || finallyScanned.contains(rightFinallyBlock)) {
                                continue;
                            }

                            finallyScanned.add(leftFinallyBlock);
                            finallyScanned.add((CtBlock) rightFinallyBlock);
                            handlerChanges.add(
                                HandlerChange.newModified(newFinallyHandler(leftFinallyBlock),
                                    newFinallyHandler((CtBlock) rightFinallyBlock)));
                        }
                    }
                } else if (change instanceof Modification || change instanceof Movement) {
                    // FIXME Just dealt Movement the same as Modification. Do we have
                    // another way?

                    // Ignore changes outside try block
                    CtTry leftTry = getParentTry(change.getLeftNode(), true);
                    if (leftTry == null || changedTrySet.contains(leftTry)) {
                        continue;
                    }

                    // There should never be modifications on CtTry/CtCatch/CtBlock
                    CtElement leftModified = change.getLeftNode();
                    if (inCatchBlock(leftModified)) {
                        CtCatch leftCatch = getParentCatch(leftModified, false);
                        CtCatch rightCatch = getParentCatch(change.getRightNode(), false);
                        if (leftCatch == null || rightCatch == null
                            || catchScanned.contains(leftCatch)
                            || catchScanned.contains(rightCatch)) {
                            continue;
                        }

                        catchScanned.add(leftCatch);
                        catchScanned.add(rightCatch);
                        handlerChanges.add(HandlerChange.newModified(
                            newCatchHandler(leftCatch), newCatchHandler(rightCatch)));
                    } else if (inFinallyBlock(leftModified)) {
                        CtBlock leftFinallyBlock = getParentFinally(leftModified, false);
                        CtBlock rightFinallyBlock = getParentFinally(change.getRightNode(), false);

                        if (leftFinallyBlock == null || rightFinallyBlock == null
                            || !isDirectFinallyBlock(rightFinallyBlock)
                            || finallyScanned.contains(leftFinallyBlock)
                            || finallyScanned.contains(rightFinallyBlock)) {
                            continue;
                        }

                        finallyScanned.add(leftFinallyBlock);
                        finallyScanned.add(rightFinallyBlock);
                        handlerChanges.add(
                            HandlerChange.newModified(newFinallyHandler(leftFinallyBlock),
                                newFinallyHandler(rightFinallyBlock)));
                    }
                }
            }

            Set<CtTypeReference> typeReferences = method.getThrownTypes();
            List<String> exceptionDeclared = typeReferences.stream()
                                                 .map(CtTypeInformation::getQualifiedName)
                                                 .collect(Collectors.toList());

            MethodChange methodChange = MethodChange.newMethodChange(method.getSignature(),
                method.getModifiers().contains(ModifierKind.PUBLIC), false, exceptionDeclared,
                handlerChanges);

            // Fill stats
            MethodStatsScanner statsScanner = new MethodStatsScanner();
            statsScanner.scan(method);
            methodChange.setNumberTryBlocks(statsScanner.getTryCount());
            methodChange.setNumberCatchBlocks(statsScanner.getCatchCount());
            methodChange.setNumberFinallyBlocks(statsScanner.getFinallyCount());

            methodChange.setNumberTryBlocksChanged(
                changes.stream()
                    .mapToInt(change -> {
                        int count = 0;
                        if (getParentTry(change.getLeftNode(), true) != null) {
                            count++;
                        }
                        if (getParentTry(change.getRightNode(), true) != null) {
                            count++;
                        }
                        return count;
                    })
                    .sum());
            catchScanned.remove(null);
            finallyScanned.remove(null);
            methodChange.setNumberCatchBlocksChanged(catchScanned.size());
            methodChange.setNumberFinallyBlocksChanged(finallyScanned.size());

            return methodChange;
        }

        protected MethodChange scanNewMethod(CtMethod method, TreeDiff treeDiff) {
            // FIXME Currently not supported
            return null;
        }

        protected MethodChange scanDeletedMethod(CtMethod method, TreeDiff treeDiff) {
            // FIXME Currently not supported
            return null;
        }

        protected List<MethodChange> scanTreeDiff(VcsDiffEntry vcsDiffEntry, TreeDiff treeDiff) {
            HashMap<CtMethod, List<Change>> methodListHashMap = new HashMap<>();
            List<Change> changes = treeDiff.getRootChanges();

            List<MethodChange> methodChanges = new ArrayList<>();

            for (Change change : changes) {
                CtElement leftNode = change.getLeftNode();
                if (leftNode == null) {
                    continue;
                }

                // Changes on entire method
                if (change instanceof Insertion && change.getRightNode() instanceof CtMethod) {
                    methodChanges.add(scanNewMethod((CtMethod) change.getRightNode(), treeDiff));
                    continue;
                } else if (change instanceof Deletion && change.getLeftNode() instanceof CtMethod) {
                    methodChanges.add(scanDeletedMethod((CtMethod) change.getLeftNode(), treeDiff));
                    continue;
                }

                CtMethod leftMethod = getParentMethod(leftNode, change instanceof Insertion);
                if (leftMethod == null) {
                    continue;
                }
                if (!methodListHashMap.containsKey(leftMethod)) {
                    methodListHashMap.put(leftMethod, new ArrayList<>());
                }
                methodListHashMap.get(leftMethod).add(change);
            }

            // Map changes to each left method
            methodChanges.addAll(
                methodListHashMap.entrySet()
                    .stream()
                    .map(entry -> scanChangesInMethod(entry.getKey(), entry.getValue(), treeDiff))
                    .collect(Collectors.toList()));

            // Remove invalid method change
            return methodChanges.stream()
                .filter(Objects::nonNull)
                .filter(methodChange -> !methodChange.getHandlerChanges().isEmpty())
                .collect(Collectors.toList());
        }

        protected ClassChange scanModifyDiffEntry(VcsDiffEntry diffEntry) throws Exception {
            if (diffEntry.getChangeType() != VcsDiffEntry.ChangeType.MODIFY) {
                logger.debug("Ignore change except MODIFY of file {} in revision {}!",
                    diffEntry.getNewFile(), diffEntry.getNewRevision().getId());
                return null;
            }

            // Build tree diff
            TreeDiff treeDiff = ScannerUtils.treeDiffOnEntry(diffEntry, treeStore);

            // Apply tricks we proposed
            treeDiff = treeDiffRecursiveTrickApplier.applyAll(treeDiff);

            return ClassChange.newClassChange(getClassName(treeDiff), isCreateChange(diffEntry),
                isPublicClass(treeDiff), scanTreeDiff(diffEntry, treeDiff));
        }

        private void decreaseUsefulnessAndDeleteTreesIfUseless(Revision revision)
            throws InvalidRevisionException {
            decreaseUsefulness(revision.getId());
            if (isUseless(revision.getId())) {
                logger.info("Remove all trees of revision {}!", revision.getId());
                treeStore.removeAll(revision);
            }
        }

        @Override
        protected RevisionChange scan(Revision target) throws Exception {
            logger.info("Scanning target revision {}", target.getId());

            if (target.parentCounts() != 1) {
                // Ignore root or merge revision
                return null;
            }

            Vcs vcs = target.getVcs();
            Revision parentRev = vcs.getRevision(target.getParentIds().get(0));

            // Only scan MODIFY's on Java getFiles

            List<VcsDiffEntry> vcsDiffEntries =
                vcs.getDiffEntries(target)
                    .stream()
                    .filter(
                        diffEntry -> diffEntry.getChangeType() == VcsDiffEntry.ChangeType.MODIFY)
                    .filter(diffEntry -> diffEntry.getNewFile().endsWith(".java"))
                    .collect(Collectors.toList());

            if (vcsDiffEntries.isEmpty()) {
                // There're none Java getFiles modifications, ignore.
                return null;
            }

            if (buildRevision) {
                // Build all Java getFiles in this revision and parent revision
                // Use default class paths
                // treeStore.setAstBuilder(target, new SpoonAstBuilder(classPaths));
                ScannerUtils.buildAllJavaFilesInRevision(target, treeStore);
                ScannerUtils.buildAllJavaFilesInRevision(parentRev, treeStore);
            }

            // Scan each diff entry for details, remove invalid class change
            List<ClassChange> classChanges =
                vcsDiffEntries.stream()
                    .map(diffEntry -> {
                        try {
                            return scanModifyDiffEntry(diffEntry);
                        } catch (Exception e) {
                            logger.error("Can not perform feature scan on diff entry {}! {}",
                                diffEntry, e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(classChange -> !classChange.getMethodChanges().isEmpty())
                    .collect(Collectors.toList());

            // Remove all trees to decrease memory usage.
            treeStore.removeAll(target);
            treeStore.removeAll(parentRev);
            // Decrease usefulness and check if trees are not required
            // decreaseUsefulnessAndDeleteTreesIfUseless(target);
            // decreaseUsefulnessAndDeleteTreesIfUseless(parentRev);

            return RevisionChange.newRevisionChange(target, classChanges);
        }

        class ChangeContainsTryBlockFeatureScanner extends CtScanner {
            private final Change change;
            private final Set<CtCatch> catchScanned;
            private final Set<CtBlock> finallyScanned;
            private List<HandlerChange> handlerChanges = new ArrayList<>();

            ChangeContainsTryBlockFeatureScanner(
                Change change, Set<CtCatch> catchScanned, Set<CtBlock> finallyScanned) {
                this.change = change;
                this.catchScanned = catchScanned;
                this.finallyScanned = finallyScanned;
            }

            public List<HandlerChange> scan() {
                if (change instanceof Insertion) {
                    scan(change.getRightNode());
                } else if (change instanceof Deletion) {
                    scan(change.getLeftNode());
                } else if (change instanceof Movement) {
                    // Ignore
                } else if (change instanceof Modification) {
                    // IMO, we will not have a modification contains try block?
                    // But if something like stream().filter(() -> {try ... catch}) are considered
                    // as Modification, just scan its right node.
                    scan(change.getRightNode());
                }
                return handlerChanges;
            }

            @Override
            public void visitCtTry(CtTry tryBlock) {
                if (change instanceof Insertion || change instanceof Modification) {
                    for (CtCatch ctCatch : tryBlock.getCatchers()) {
                        catchScanned.add(ctCatch);
                        handlerChanges.add(
                            HandlerChange.newInserted(null, newCatchHandler(ctCatch)));
                    }
                    HandlerChange.FinallyHandler finallyHandler = newFinallyHandler(tryBlock);
                    if (tryBlock.getFinalizer() != null) {
                        finallyScanned.add(tryBlock.getFinalizer());
                    }
                    if (finallyHandler != null) {
                        handlerChanges.add(HandlerChange.newInserted(null, finallyHandler));
                    }
                } else if (change instanceof Deletion) {
                    for (CtCatch ctCatch : tryBlock.getCatchers()) {
                        catchScanned.add(ctCatch);
                        handlerChanges.add(
                            HandlerChange.newDeleted(newCatchHandler(ctCatch), null));
                    }
                    if (tryBlock.getFinalizer() != null) {
                        finallyScanned.add(tryBlock.getFinalizer());
                    }
                    HandlerChange.FinallyHandler finallyHandler = newFinallyHandler(tryBlock);
                    if (finallyHandler != null) {
                        handlerChanges.add(HandlerChange.newInserted(null, finallyHandler));
                    }
                } else if (change instanceof Movement) {
                    // Ignore
                }

                super.visitCtTry(tryBlock);
            }
        }

        class MethodStatsScanner extends CtScanner {
            private int tryCount, catchCount, finallyCount;

            public int getTryCount() {
                return tryCount;
            }

            public int getCatchCount() {
                return catchCount;
            }

            public int getFinallyCount() {
                return finallyCount;
            }

            @Override
            public void visitCtTry(CtTry tryBlock) {
                tryCount++;
                super.visitCtTry(tryBlock);
            }

            @Override
            public void visitCtCatch(CtCatch catchBlock) {
                catchCount++;
                super.visitCtCatch(catchBlock);
            }

            @Override
            public <R> void visitCtBlock(CtBlock<R> block) {
                if (isDirectFinallyBlock(block)) {
                    finallyCount++;
                }
                super.visitCtBlock(block);
            }
        }
    }
}
