package fixeh.project.vcs;

import java.io.IOException;
import java.util.List;
import fixeh.project.vcs.exceptions.InvalidBranchException;
import fixeh.project.vcs.exceptions.InvalidRevisionException;

/**
 * Created by Shunjie Ding on 19/12/2017.
 */
public interface Vcs {
    /**
     * Get the getHead revision.
     * @return getHead revision, or null if any exception occurs.
     */
    Revision getHead();

    /**
     * Checkout specified revision to the project path. This method should fail when VCS is
     * isBare. Also see {@link #checkout(Revision, String, boolean)}. This method is not
     * thread-safe.
     * @param revision specified revision.
     * @param force force checkout, overwrite changed not added.
     * @return true if checkout successfully, false otherwise.
     * @throws InvalidRevisionException if there's no such revision.
     */
    boolean checkout(Revision revision, boolean force) throws InvalidRevisionException;

    /**
     * Checkout specified revision to given path. VCS will track this dir so do not delete it by
     * yourself, using {@link #removeDir(Revision)} to remove the dir safely. Also see {@link
     * #checkout(Revision, boolean)}.
     * @param revision specified revision.
     * @param dir specified dir.
     * @param force force checkout, overwrite the given path.
     * @return true if checkout successfully, false otherwise.
     * @throws InvalidRevisionException if there's no such revision.
     * @throws IOException if given path exists and not empty, or any other io problems when
     * writing to the given path.
     */
    boolean checkout(Revision revision, String dir, boolean force)
        throws InvalidRevisionException, IOException;

    /**
     * Remove the dir tracked by this VCS. See more details in {@link #checkout(Revision, String,
     * boolean)}.
     * @param revision specified revision.
     */
    void removeDir(Revision revision);

    /**
     * Check if the VCS is isBare.
     * @return true if it's isBare, false otherwise.
     */
    boolean isBare();

    /**
     * Get path of vcs project.
     * @return path of vcs project.
     */
    String getPath();

    /**
     * List getBranches.
     * @return a list of branch names, or null if any exception occurs.
     */
    List<String> getBranches();

    /**
     * List revisions.
     * @return a list of revisions, or null if any exception occurs.
     */
    List<Revision> getRevisions();

    /**
     * Count revisions.
     * @return count of all revisions, or -1 if any exception occurs.
     */
    int countRevisions();

    /**
     * List revisions of specific branch.
     * @param branch specified branch name.
     * @return a list of revisions, or null if any exception occurs.
     * @throws InvalidBranchException if there is no such branch in this vcs or
     * branch is not supported.
     */
    List<Revision> getRevisionsOfBranch(String branch) throws InvalidBranchException;

    /**
     * Count revision of specific branch.
     * @param branch specified branch name.
     * @return count of revisions in branch, or -1 if any exception occurs.
     * @throws InvalidBranchException if there is no such branch in this vcs or
     * branch is not supported.
     */
    int countRevisionsOfBranch(String branch) throws InvalidBranchException;

    /**
     * Get the latest revision of the specific branch.
     * @param branch specified branch name.
     * @return the latest revision, or null if any exception occurs.
     * @throws InvalidBranchException if there is no such branch in this vcs or
     * branch is not supported.
     */
    Revision getLastRevisionOfBranch(String branch) throws InvalidBranchException;

    /**
     * Get revision from revision id.
     * @param revisionId specified revision id, e.g. sha-1 hash for git.
     * @return revision with id, or null if any exception occurs.
     * @throws InvalidRevisionException if there is no such revision.
     */
    Revision getRevision(String revisionId) throws InvalidRevisionException;

    /**
     * List getFiles of this revision. This will only list regular getFiles, that is to say,
     * symlinks are excluded.
     * @param revision specified revision.
     * @return a list of getFiles of this revision, or null if any exception occurs.
     * @throws InvalidRevisionException if there is no such revision.
     */
    List<String> getFiles(Revision revision) throws InvalidRevisionException;

    /**
     * List getFiles of specified revision id. This will only list regular getFiles, that is to say,
     * symlinks are excluded.
     * @param revisionId specified revision id, e.g. sha-1 hash for git.
     * @return a list of getFiles of this revision, or null if any exception occurs.
     * @throws InvalidRevisionException if there is no such revision.
     */
    List<String> getFiles(String revisionId) throws InvalidRevisionException;

    /**
     * List changes of specified revision id.
     * @param revisionId specified revision id, e.g. sha-1 hash for git.
     * @return a list of changes of this revision, or null if any exception
     * occurs.
     * @throws InvalidRevisionException if there is no such revision.
     */
    List<VcsDiffEntry> getDiffEntries(String revisionId) throws InvalidRevisionException;

    /**
     * List changes of specified revision.
     * @param revision specified revision.
     * @return a list of changes of this revision, or null if any exception
     * occurs.
     * @throws InvalidRevisionException if there is no such revision.
     */
    List<VcsDiffEntry> getDiffEntries(Revision revision) throws InvalidRevisionException;

    /**
     * Get content of a file in specified revision.
     * @param revisionId specified revision id.
     * @param filePath file path.
     * @return content of file in bytes.
     * @throws InvalidRevisionException if there is no such revision.
     * @throws IOException if there is no such file.
     */
    byte[] readFile(String revisionId, String filePath)
        throws InvalidRevisionException, IOException;

    /**
     * Get content of a file in specified revision.
     * @param revision specified revision.
     * @param filePath file path.
     * @return content of file in bytes.
     * @throws InvalidRevisionException if there is no such revision.
     * @throws IOException if there is no such file.
     */
    byte[] readFile(Revision revision, String filePath)
        throws InvalidRevisionException, IOException;
}
