package fixeh.scanner.feature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import fixeh.project.Project;

/**
 * Created by Shunjie Ding on 19/12/2017.
 */
public final class FeatureSet implements Serializable {
    private final ProjectOverview overview;

    private final List<RevisionChange> revisionChanges;

    private FeatureSet(ProjectOverview overview, List<RevisionChange> revisionChanges) {
        this.overview = overview;
        this.revisionChanges = revisionChanges == null ? new ArrayList<>(0) : revisionChanges;
    }

    public static FeatureSet newFeatureSet(Project project, List<RevisionChange> revisionChanges) {
        if (project == null) {
            throw new IllegalArgumentException("Project of FeatureSet must not be null!");
        }

        ProjectOverview overview = ProjectOverview.generateOverview(project);
        ProjectOverview.updateOverview(overview, revisionChanges);
        return new FeatureSet(overview, revisionChanges);
    }

    public List<RevisionChange> getRevisionChanges() {
        return new ArrayList<>(revisionChanges);
    }

    public Optional<RevisionChange> getRevisionChange(String revisionId) {
        return revisionChanges.stream()
            .filter(change -> change.getRevisionId().equals(revisionId))
            .findFirst();
    }

    public ProjectOverview getOverview() {
        return overview;
    }
}
