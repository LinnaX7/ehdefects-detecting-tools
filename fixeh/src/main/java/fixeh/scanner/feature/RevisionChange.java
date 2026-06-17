package fixeh.scanner.feature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import fixeh.project.vcs.Revision;

/**
 * Created by Shunjie Ding on 07/01/2018.
 */
public class RevisionChange implements Serializable {
    private final String revisionId;
    private final String revisionMessage;
    private final List<ClassChange> classChanges;

    protected RevisionChange(
        String revisionId, String revisionMessage, List<ClassChange> classChanges) {
        this.revisionId = revisionId;
        this.revisionMessage = revisionMessage;
        this.classChanges = classChanges == null ? new ArrayList<>(0) : classChanges;
    }

    public static RevisionChange newRevisionChange(
        Revision revision, List<ClassChange> classChanges) {
        return new RevisionChange(revision.getId(), revision.getMessage(), classChanges);
    }

    public static RevisionChange newRevisionChange(
        String revisionId, String revisionMessage, List<ClassChange> classChanges) {
        return new RevisionChange(revisionId, revisionMessage, classChanges);
    }

    public String getRevisionId() {
        return revisionId;
    }

    public List<ClassChange> getClassChanges() {
        return new ArrayList<>(classChanges);
    }

    public String getRevisionMessage() {
        return revisionMessage;
    }
}
