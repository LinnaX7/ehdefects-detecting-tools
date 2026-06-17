package fixeh.scanner.feature;

import com.bing.excel.annotation.CellConfig;
import com.bing.excel.annotation.OutAlias;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import fixeh.project.Project;
import fixeh.project.vcs.Revision;
import fixeh.project.vcs.Vcs;
import fixeh.project.vcs.exceptions.InvalidBranchException;
import fixeh.project.vcs.exceptions.InvalidRevisionException;

/**
 * Created by Shunjie Ding on 07/01/2018.
 */
@OutAlias("Project Overview")
public class ProjectOverview implements Serializable {
    @CellConfig(index = 0, aliasName = "Project Name", readRequired = true)
    private String projectName;

    @CellConfig(index = 1, aliasName = "First Commit Time", readRequired = true)
    private Date firstCommitTime;

    @CellConfig(index = 2, aliasName = "Last Commit Time", readRequired = true)
    private Date lastCommitTime;

    @CellConfig(index = 3, aliasName = "Number of Total Revisions", readRequired = true)
    private int numberRevisions;

    @CellConfig(index = 4, aliasName = "Number of Related Revisions", readRequired = true)
    private int numberRelatedRevisions;

    @CellConfig(index = 5, aliasName = "Number of Total Java Files", readRequired = true)
    private int numberJavaFiles;

    @CellConfig(index = 6, aliasName = "Number of Affected Java Files", readRequired = true)
    private int numberTotalAffectedFiles;

    // FIXME Should we implement content differ for this? Currently it's always 0.
    @CellConfig(index = 7, aliasName = "Number of Affected Lines", readRequired = true)
    private int numberTotalAffectedLines;

    public ProjectOverview() {}

    ProjectOverview(String projectName) {
        this.projectName = projectName;
    }

    private static int countJavaFiles(Revision revision) {
        try {
            return (int) revision.getVcs()
                .getFiles(revision)
                .stream()
                .filter(s -> s.endsWith(".java"))
                .count();
        } catch (InvalidRevisionException e) {
            // Never reaches here, swallow
        }
        return 0;
    }

    public static ProjectOverview generateOverview(Project project) {
        ProjectOverview overview = new ProjectOverview(project.getName());

        Vcs vcs = project.getVcs();
        List<String> branches = vcs.getBranches();
        for (String branch : branches) {
            try {
                for (Revision revision : vcs.getRevisionsOfBranch(branch)) {
                    overview.setFirstCommitTime(revision.getCommitTime());
                    overview.setLastCommitTime(revision.getCommitTime());

                    overview.numberRevisions++;
                }
            } catch (InvalidBranchException e) {
                // Never reaches here, swallow
            }
        }

        // Counting java getFiles on the latest revision of master branch.
        String masterBranch = branches.contains("master")
            ? "master"
            : (branches.contains("origin/master") ? "origin/master" : branches.get(0));
        try {
            overview.numberJavaFiles = countJavaFiles(vcs.getLastRevisionOfBranch(masterBranch));
        } catch (InvalidBranchException e) {
            // Never reaches here, swallow
        }

        return overview;
    }

    public static void updateOverview(
        ProjectOverview overview, List<RevisionChange> revisionChanges) {
        overview.numberRelatedRevisions = revisionChanges.size();
        overview.numberTotalAffectedFiles = 0;
        for (RevisionChange revisionChange : revisionChanges) {
            overview.numberTotalAffectedFiles += revisionChange.getClassChanges().size();
        }
    }

    public String getProjectName() {
        return projectName;
    }

    public Date getFirstCommitTime() {
        return firstCommitTime;
    }

    protected void setFirstCommitTime(Date time) {
        if (firstCommitTime == null || firstCommitTime.after(time)) {
            firstCommitTime = time;
        }
    }

    public Date getLastCommitTime() {
        return lastCommitTime;
    }

    protected void setLastCommitTime(Date time) {
        if (lastCommitTime == null || lastCommitTime.before(time)) {
            lastCommitTime = time;
        }
    }

    public int getNumberRevisions() {
        return numberRevisions;
    }

    public int getNumberRelatedRevisions() {
        return numberRelatedRevisions;
    }

    public int getNumberJavaFiles() {
        return numberJavaFiles;
    }

    public int getNumberTotalAffectedFiles() {
        return numberTotalAffectedFiles;
    }

    public int getNumberTotalAffectedLines() {
        return numberTotalAffectedLines;
    }
}
