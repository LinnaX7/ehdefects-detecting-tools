package fixeh.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import fixeh.Constants;
import fixeh.output.SerializeUtils;
import fixeh.project.Project;
import fixeh.project.build.BuildSystem;
import fixeh.project.build.BuildSystemType;
import fixeh.project.build.exceptions.BuildEssentialsNotFoundException;
import fixeh.project.build.exceptions.UnsupportedBuildSystemException;
import fixeh.project.vcs.Revision;
import fixeh.project.vcs.VcsType;
import fixeh.project.vcs.exceptions.UnsupportedVcsException;
import fixeh.project.vcs.exceptions.VcsNotFoundException;
import fixeh.scanner.ProjectFeatureScanner;
import fixeh.scanner.SuspiciousRevisionScanner;
import fixeh.scanner.feature.FeatureSet;
import fixeh.scanner.feature.FeatureSetWriter;
import fixeh.scanner.filter.RevisionMessageFilter;
import fixeh.scanner.filter.RevisionTypeFilter;

@Parameters(commandNames = "scan",
    commandDescription =
        "Scan projects and generate suspicious commit list or features of exception handling fixes in project")
public class CommandScan implements Command {
    private final Logger logger = LoggerFactory.getLogger(CommandScan.class);

    @Parameter(names = {"--feature"}, description = "Perform feature scan.")
    private boolean feature;

    @Parameter(names = {"--commit"},
        description =
            "Perform suspicious scan/Perform feature scan on suspicious result if '--feature' exists")
    private boolean commit;

    @Parameter(names = {"-k", "--keywords"}, description = "Keywords for suspicious scan",
        splitter = SemiColonParameterSplitterWithTrimming.class)
    private List<String> keywords = Arrays.asList(Constants.MESSAGE_KEYWORDS);

    @Parameter(names = {"-p", "--project-path"}, required = true, description = "Project path")
    private String projectPath;

    @Parameter(names = {"-b", "--build-system"},
        description =
            "Build system type (gradle, eclipse, maven[✘], ant[✘], auto autoDetect if unspecified)")
    private String buildSystem = "gradle";

    @Parameter(names = {"-t", "--vcs-type"}, description = "VCS type (git, svn[✘], mercurial[✘])")
    private String vcsType = "git";

    @Parameter(names = {"--bare"}, description = "Specified when VCS repo is bare")
    private boolean bare;

    @Parameter(names = {"-n", "--dry-run"}, description = "Show details of the given action")
    private boolean isDryRun;

    @Parameter(names = "--enable-classpath",
        description = "Enable compiling with classpath for AST compiler")
    private boolean enableClasspath;

    @Parameter(names = "--build-revision", description = "Build all java files in revision")
    private boolean buildRevision;

    @Parameter(names = {"-o", "--output"}, description = "Output file path")
    private String outputFile = "result";

    @Parameter(names = {"--output-type"}, description = "Output type (serialize, excel)")
    private String outputType = "excel";

    @Override
    public String name() {
        return "scan";
    }

    private boolean checkParameters() {
        if (Stream.of("gradle", "eclipse", "maven", "ant", "").noneMatch(buildSystem::equals)) {
            return false;
        }

        if (Stream.of("git", "svn", "mercurial", "").noneMatch(vcsType::equals)) {
            return false;
        }

        if (Stream.of("serialize", "excel", "").noneMatch(outputType::equals)) {
            return false;
        }

        return true;
    }

    private Project getProject() throws VcsNotFoundException, UnsupportedVcsException {
        if (!vcsType.isEmpty()) {
            return new Project(projectPath, VcsType.toVcsType(vcsType), bare);
        } else {
            return new Project(projectPath);
        }
    }

    private String getOutputFileName() {
        if (outputType.equals("excel")) {
            if (!outputFile.endsWith(".xlsx") && !outputFile.endsWith(".xls")) {
                return outputFile + ".xlsx";
            }
        } else if (outputType.equals("serialize")) {
            if (!outputFile.equals(".ser")) {
                return outputFile + ".ser";
            }
        }

        return outputFile;
    }

    private List<String> getClassPaths(Project project) {
        if (!enableClasspath) {
            // Return null when classpath is disabled
            return null;
        }

        try {
            if (buildSystem == null) {
                // auto autoDetect
                return BuildSystem.getBuildSystem(projectPath).getClassPaths();
            }
            return BuildSystem
                .getBuildSystem(BuildSystemType.toBuildSystemType(buildSystem), projectPath)
                .getClassPaths();
        } catch (BuildEssentialsNotFoundException | UnsupportedBuildSystemException e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    private SuspiciousRevisionScanner getSuspiciousRevisionScanner(Project project) {
        return new SuspiciousRevisionScanner(
            project, Arrays.asList(new RevisionTypeFilter(), new RevisionMessageFilter(keywords)));
    }

    private ProjectFeatureScanner getProjectFeatureScanner(Project project) {
        if (commit) {
            return new ProjectFeatureScanner(
                getSuspiciousRevisionScanner(project), getClassPaths(project), buildRevision);
        }

        return new ProjectFeatureScanner(project, false, getClassPaths(project), buildRevision);
    }

    private void dryRun(CommandOptions options) throws Exception {
        logger.info("Scanner Information");
        logger.info("=============================================");
        logger.info("Feature Scan: {}", feature);
        logger.info("Suspicious Commit Scan: {}", commit);
        logger.info("Keywords: {}", keywords.stream().collect(Collectors.joining(", ")));
        logger.info("Project Path: {}", projectPath);
        logger.info("VCS type: {}, bare({})", vcsType, bare);
        logger.info("Build System Type: {}", buildSystem);

        logger.info("Building Information");
        logger.info("=============================================");
        List<String> classPaths = getClassPaths(getProject());
        logger.info("Classpath: {}",
            classPaths == null ? "null" : classPaths.stream().collect(Collectors.joining(", ")));
    }

    @Override
    public void run(CommandOptions options) throws Exception {
        if (!checkParameters()) {
            throw new RuntimeException(
                "Parameter validation fails, please check options and rerun!");
        }

        if (!feature && !commit) {
            logger.error("Must specify how to scan the project!");
            return;
        }

        if (isDryRun) {
            dryRun(options);
            return;
        }

        logger.info("Scanning project {} ...", projectPath);

        Project project = getProject();

        if (feature) {
            ProjectFeatureScanner scanner = getProjectFeatureScanner(project);
            FeatureSet featureSet = scanner.scan();

            logger.info("Complete successfully! Feature set generated!");
            logger.info("Writing FeatureSet object to {}.", getOutputFileName());
            FeatureSetWriter writer = new FeatureSetWriter();
            if (outputType.equals("excel")) {
                writer.writeToExcel(new File(getOutputFileName()), featureSet);
            } else {
                writer.serializeToFile(new File(getOutputFileName()), featureSet);
            }
        } else if (commit) {
            SuspiciousRevisionScanner scanner = getSuspiciousRevisionScanner(project);
            List<Revision> revisions = scanner.scan();
            logger.info("Complete successfully! Suspicious revision list generated!");
            logger.info("Writing revision list to {}.", getOutputFileName());
            FeatureSetWriter writer = new FeatureSetWriter();
            if (outputType.equals("excel")) {
                writer.writeSuspiciousRevisionsToExcel(new File(getOutputFileName()), revisions);
            } else {
                SerializeUtils.writeObject(
                    new File(getOutputFileName()), new ArrayList<>(revisions));
            }
        }
    }
}
