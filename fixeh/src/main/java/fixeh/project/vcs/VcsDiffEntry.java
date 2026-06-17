package fixeh.project.vcs;

import java.io.Serializable;

/**
 * Created by Shunjie Ding on 21/12/2017.
 */
public class VcsDiffEntry implements Serializable {
    /**
     * One of CREATE, MODIFY, DELETE and RENAME.
     */
    private ChangeType changeType;
    private String oldFile;
    private String newFile;
    private Revision oldRevision;
    private Revision newRevision;

    protected VcsDiffEntry() {}

    public VcsDiffEntry(ChangeType changeType, String oldFile, String newFile, Revision oldRevision,
        Revision newRevision) {
        this.changeType = changeType;
        this.oldFile = oldFile;
        this.newFile = newFile;
        this.oldRevision = oldRevision;
        this.newRevision = newRevision;
    }

    static VcsDiffEntry newCreated(
        String oldFile, String newFile, Revision oldRevision, Revision newRevision) {
        return new VcsDiffEntry(ChangeType.CREATE, oldFile, newFile, oldRevision, newRevision);
    }

    static VcsDiffEntry newModified(
        String oldFile, String newFile, Revision oldRevision, Revision newRevision) {
        return new VcsDiffEntry(ChangeType.MODIFY, oldFile, newFile, oldRevision, newRevision);
    }

    static VcsDiffEntry newDeleted(
        String oldFile, String newFile, Revision oldRevision, Revision newRevision) {
        return new VcsDiffEntry(ChangeType.DELETE, oldFile, newFile, oldRevision, newRevision);
    }

    static VcsDiffEntry newRenamed(
        String oldFile, String newFile, Revision oldRevision, Revision newRevision) {
        return new VcsDiffEntry(ChangeType.RENAME, oldFile, newFile, oldRevision, newRevision);
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public String getOldFile() {
        return oldFile;
    }

    public String getNewFile() {
        return newFile;
    }

    public Revision getOldRevision() {
        return oldRevision;
    }

    public Revision getNewRevision() {
        return newRevision;
    }

    @Override
    public String toString() {
        return String.format("<DiffEntry(%s %s[%s], parent %s[%s])>", getChangeType(), getNewFile(),
            getNewRevision().getId(), getOldFile(), getOldRevision().getId());
    }

    public enum ChangeType { CREATE, MODIFY, DELETE, RENAME, COPY, UNKNOWN }
}
