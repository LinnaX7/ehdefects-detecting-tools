package fixeh.project.vcs;

import com.google.common.collect.Lists;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.RawParseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import fixeh.project.vcs.exceptions.InvalidBranchException;
import fixeh.project.vcs.exceptions.InvalidRevisionException;
import fixeh.util.TmpDirUtils;

/**
 * Created by Shunjie Ding on 19/12/2017.
 */
public class GitWrapper implements Vcs {
    private final Logger logger = LoggerFactory.getLogger(GitWrapper.class);
    private final boolean bare;
    private final ConcurrentHashMap<String, String> trackingCheckouts = new ConcurrentHashMap<>();
    private String path;
    private Git git;

    public GitWrapper(String path, boolean bare) throws IOException {
        this.path = path;
        this.git = Git.open(new File(path));
        this.bare = bare;
    }

    private static String getBranchName(String branch) {
        if (!branch.startsWith("refs/heads/")) {
            return "refs/heads/" + branch;
        }
        return branch;
    }

    private ObjectId getHeadObjectId() throws IOException {
        return git.getRepository().resolve(Constants.HEAD);
    }

    private String getHeadId() throws IOException {
        return getHeadObjectId().getName();
    }

    @Override
    public Revision getHead() {
        try {
            return convertCommitToRevision(getCommitFromRevisionId(getHeadId()));
        } catch (InvalidRevisionException | IOException e) {
            logger.error("Exception occurs when retrieving getHead revision of VCS {}: {}", this,
                e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean checkout(Revision revision, boolean force) throws InvalidRevisionException {
        if (bare) {
            return false;
        }

        try {
            logger.debug("Checkout working dir to revision {}.", revision.getId());
            git.checkout()
                .setAllPaths(true)
                .setForce(force)
                .setStartPoint(getCommitFromRevisionId(revision.getId()))
                .call();
            return true;
        } catch (GitAPIException e) {
            logger.error("Exception occurs when VCS {} calling checkout: {}", this, e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private boolean checkout(RevCommit commit, File dir) {
        try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
            treeWalk.addTree(commit.getTree());
            treeWalk.setRecursive(true);

            while (treeWalk.next()) {
                FileMode fileMode = treeWalk.getFileMode();
                String filePathRel = treeWalk.getPathString();

                // Create missing parent dirs
                Path filePath = Paths.get(dir.getAbsolutePath(), filePathRel);
                Files.createDirectories(filePath.getParent());

                if (fileMode == FileMode.TREE) {
                    // Create path
                    Files.createDirectory(filePath);
                } else if (fileMode == FileMode.SYMLINK) {
                    // Create symlink
                    ObjectLoader loader = git.getRepository().open(treeWalk.getObjectId(0));
                    String target = RawParseUtils.decode(loader.getBytes());
                    Path targetPath = Paths.get(target);
                    Files.createSymbolicLink(filePath, targetPath);
                } else if (fileMode == FileMode.REGULAR_FILE
                    || fileMode == FileMode.EXECUTABLE_FILE) {
                    // Create file and copy contents to it.
                    ObjectLoader loader = git.getRepository().open(treeWalk.getObjectId(0));
                    loader.copyTo(new FileOutputStream(filePath.toFile()));

                    // Set executable permission on if file mode is executable
                    if (fileMode == FileMode.EXECUTABLE_FILE) {
                        filePath.toFile().setExecutable(true);
                    }
                } else {
                    logger.warn(
                        "Encounter into a missing entry {} when walking on revision {} of VCS {}!",
                        filePathRel, commit.getId().name(), this);
                }
            }

        } catch (Exception e) {
            logger.error("Exception occurs when checkout getFiles of revision {} to dir {}!",
                commit.getId().name(), dir.getAbsolutePath());
            e.printStackTrace();
        }

        return true;
    }

    private void copyDirectoryWithPermissionsAndModifiedDatePreserved(Path src, Path dst)
        throws IOException {
        Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
                Path destFile = dst.resolve(src.relativize(file));
                Files.createDirectories(destFile.getParent());
                Files.copy(
                    file, destFile, StandardCopyOption.COPY_ATTRIBUTES, LinkOption.NOFOLLOW_LINKS);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public boolean checkout(Revision revision, String dir, boolean force)
        throws InvalidRevisionException, IOException {
        File targetDir = new File(dir);
        if (!force && targetDir.isDirectory() && targetDir.list().length != 0) {
            logger.warn("Can not checkout revision {} to path {}, directory is not empty!",
                revision.getId(), dir);
            return false;
        }

        if (trackingCheckouts.contains(revision.getId())) {
            // Copy to given path
            FileUtils.deleteDirectory(targetDir);
            // Copy using Files.copy to preserve file attributes
            copyDirectoryWithPermissionsAndModifiedDatePreserved(
                Paths.get(trackingCheckouts.get(revision.getId())), targetDir.toPath());
            return true;
        }

        RevCommit commit = getCommitFromRevisionId(revision.getId());

        // Allocate a tmp dir to checkout
        File tmpDir = TmpDirUtils.allocateTmpDir();
        if (!checkout(commit, tmpDir)) {
            // Remove tmp dir at once
            FileUtils.forceDelete(tmpDir);
            return false;
        }

        // Copy to given path
        // Copy using Files.copy to preserve file attributes
        FileUtils.deleteDirectory(targetDir);
        copyDirectoryWithPermissionsAndModifiedDatePreserved(tmpDir.toPath(), targetDir.toPath());

        trackingCheckouts.put(revision.getId(), dir);

        // Release tmp dir
        TmpDirUtils.releaseTmpDir(tmpDir);
        return true;
    }

    @Override
    public void removeDir(Revision revision) {
        if (trackingCheckouts.contains(revision.getId())) {
            String path = trackingCheckouts.remove(revision.getId());
            try {
                FileUtils.deleteDirectory(new File(path));
            } catch (IOException e) {
                // ignore
            }
        }
    }

    @Override
    public boolean isBare() {
        return bare;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public List<String> getBranches() {
        try {
            List<Ref> refs = git.branchList().call();
            // Remove detached HEAD
            return refs.stream()
                .map(Ref::getName)
                .filter(r -> !r.equals("HEAD"))
                .collect(Collectors.toList());
        } catch (GitAPIException e) {
            logger.error("GitAPIException occurred when calling branch list!");
            e.printStackTrace();
        }
        return null;
    }

    private Revision convertCommitToRevision(RevCommit commit) {
        return new Revision(this, commit.getId().name(), commit.getCommitterIdent().getWhen(),
            commit.getFullMessage(),
            new Author(commit.getAuthorIdent().getName(), commit.getAuthorIdent().getEmailAddress(),
                commit.getAuthorIdent().getTimeZoneOffset() / 60),
            Arrays.stream(commit.getParents())
                .map(parentCommit -> parentCommit.getId().name())
                .collect(Collectors.toList()));
    }

    @Override
    public List<Revision> getRevisions() {
        try {
            List<RevCommit> commits = Lists.newArrayList(git.log().all().call());
            return commits.stream()
                .map(this ::convertCommitToRevision)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Exception occurred when calling log all!");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int countRevisions() {
        try {
            List<RevCommit> commits = Lists.newArrayList(git.log().all().call());
            return (int) commits.stream().count();
        } catch (Exception e) {
            logger.error("Exception occurred when calling log all!");
            e.printStackTrace();
        }
        return -1;
    }

    private ObjectId getBranchId(String branch) throws InvalidBranchException {
        try {
            return git.getRepository().resolve(getBranchName(branch));
        } catch (IOException e) {
            logger.error("Exception occurred resolving branch {}!", branch);
            throw new InvalidBranchException(e);
        }
    }

    @Override
    public List<Revision> getRevisionsOfBranch(String branch) throws InvalidBranchException {
        ObjectId branchId = getBranchId(branch);

        try {
            List<RevCommit> commits = Lists.newArrayList(git.log().add(branchId).call());
            return commits.stream()
                .map(this ::convertCommitToRevision)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Exception occurred when calling log branch {}!", branch);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public int countRevisionsOfBranch(String branch) throws InvalidBranchException {
        ObjectId branchId = getBranchId(branch);

        try {
            List<RevCommit> commits = Lists.newArrayList(git.log().add(branchId).call());
            return (int) commits.stream().count();
        } catch (Exception e) {
            logger.error("Exception occurred when calling log branch {}!", branch);
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public Revision getLastRevisionOfBranch(String branch) throws InvalidBranchException {
        ObjectId branchId = getBranchId(branch);

        try (RevWalk walk = new RevWalk(git.getRepository())) {
            return convertCommitToRevision(walk.parseCommit(branchId));
        } catch (Exception e) {
            logger.error("Exception occurred when get the last revision on branch {}!", branch);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Revision getRevision(String revisionId) throws InvalidRevisionException {
        return convertCommitToRevision(getCommitFromRevisionId(revisionId));
    }

    private RevCommit getCommitFromRevisionId(String revisionId) throws InvalidRevisionException {
        try {
            return getCommitFromRevisionId(ObjectId.fromString(revisionId));
        } catch (IOException e) {
            throw new InvalidRevisionException("Invalid revision id " + revisionId, e);
        }
    }

    private RevCommit getCommitFromRevisionId(ObjectId id) throws IOException {
        try (RevWalk revWalk = new RevWalk(git.getRepository())) {
            return revWalk.parseCommit(id);
        }
    }

    @Override
    public List<String> getFiles(Revision revision) throws InvalidRevisionException {
        return getFiles(revision.getId());
    }

    @Override
    public List<String> getFiles(String revisionId) throws InvalidRevisionException {
        RevCommit commit = getCommitFromRevisionId(revisionId);

        try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
            treeWalk.addTree(commit.getTree());
            treeWalk.setRecursive(true);

            List<String> fileList = new ArrayList<>();
            while (treeWalk.next()) {
                FileMode fileMode = treeWalk.getFileMode();
                if (fileMode == FileMode.REGULAR_FILE || fileMode == FileMode.EXECUTABLE_FILE) {
                    fileList.add(treeWalk.getPathString());
                }
            }
            return fileList;
        } catch (Exception e) {
            logger.error("Exception occurred: could not list getFiles of revision {}!", revisionId);
            e.printStackTrace();
        }

        return null;
    }

    private AbstractTreeIterator getTreeIter(RevCommit commit, ObjectReader reader)
        throws IOException {
        RevWalk revWalk = new RevWalk(git.getRepository());
        return new CanonicalTreeParser(null, reader, revWalk.parseTree(commit.getTree()));
    }

    private VcsDiffEntry convertCommitToRevision(
        DiffEntry diffEntry, RevCommit commit, RevCommit parent) {
        VcsDiffEntry.ChangeType changeType = VcsDiffEntry.ChangeType.UNKNOWN;
        switch (diffEntry.getChangeType()) {
            case ADD:
                changeType = VcsDiffEntry.ChangeType.CREATE;
                break;
            case DELETE:
                changeType = VcsDiffEntry.ChangeType.DELETE;
                break;
            case COPY:
                changeType = VcsDiffEntry.ChangeType.COPY;
                break;
            case MODIFY:
                changeType = VcsDiffEntry.ChangeType.MODIFY;
                break;
            case RENAME:
                changeType = VcsDiffEntry.ChangeType.RENAME;
                break;
        }
        return new VcsDiffEntry(changeType, diffEntry.getOldPath(), diffEntry.getNewPath(),
            convertCommitToRevision(parent), convertCommitToRevision(commit));
    }

    @Override
    public List<VcsDiffEntry> getDiffEntries(String revisionId) throws InvalidRevisionException {
        ObjectReader reader = git.getRepository().newObjectReader();
        RevCommit commit = getCommitFromRevisionId(revisionId);
        if (commit.getParentCount() != 1) {
            throw new InvalidRevisionException("Could not get diff context for root/merge commit!");
        }

        RevCommit pCommit = getCommitFromRevisionId(commit.getParent(0).getId().getName());
        try {
            List<DiffEntry> diffEntries = git.diff()
                                              .setNewTree(getTreeIter(commit, reader))
                                              .setOldTree(getTreeIter(pCommit, reader))
                                              .call();
            // Return diff entries.
            return diffEntries.stream()
                .map(e -> convertCommitToRevision(e, commit, pCommit))
                .collect(Collectors.toList());
        } catch (IOException | GitAPIException e) {
            logger.error("Exception occurred: could not list diffs of revision {}!", revisionId);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<VcsDiffEntry> getDiffEntries(Revision revision) throws InvalidRevisionException {
        return getDiffEntries(revision.getId());
    }

    @Override
    public byte[] readFile(String revisionId, String filePath)
        throws InvalidRevisionException, IOException {
        // If revision is checkout to a given dir and being tracked, then reading file instead of
        // reading objects in git.
        if (trackingCheckouts.contains(revisionId)) {
            File file = Paths.get(trackingCheckouts.get(revisionId), filePath).toFile();
            if (file.isFile()) {
                // Read contents and return
                try {
                    return FileUtils.readFileToByteArray(file);
                } catch (IOException e) {
                    logger.error(
                        "Exception occurs when reading file {} of revision {} from tracking path {}: {}!",
                        filePath, revisionId, file.getAbsoluteFile(), e.getMessage());
                    e.printStackTrace();
                    logger.warn("Fallback to reading objects from git repository!");
                }
            }
        }

        // Read file content from git repo.

        RevCommit revCommit = getCommitFromRevisionId(revisionId);

        try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
            treeWalk.addTree(revCommit.getTree());
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(filePath));

            if (!treeWalk.next()) {
                throw new FileNotFoundException(
                    String.format("Could not find file %s in revision %s!", filePath, revisionId));
            }

            // Load file content
            ObjectLoader loader = git.getRepository().open(treeWalk.getObjectId(0));
            return loader.getBytes();
        }
    }

    @Override
    public byte[] readFile(Revision revision, String filePath)
        throws InvalidRevisionException, IOException {
        return readFile(revision.getId(), filePath);
    }

    @Override
    public String toString() {
        return String.format("Git repository at (%s), %d commits totally.", path, countRevisions());
    }
}
