package fixeh.cli;

import com.android.ddmlib.IDevice;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import fixeh.detector.ExceptionHandlingBugFixDetector;

/**
 * Created by Shunjie Ding on 2018/4/4.
 */
@Parameters(commandNames = "detect",
    commandDescription =
        "Detects different test results from two versions of given app and test package.")
public class CommandDetect implements Command {
    private final Logger logger = LoggerFactory.getLogger(CommandDetect.class);
    @Parameter(names = {"-a"}, description = "Debug apk file of version A", required = true)
    private String debugApkA;

    @Parameter(names = {"-b"}, description = "Debug apk file of version B", required = true)
    private String debugApkB;

    @Parameter(names = {"-t"}, description = "Test apk file", required = true)
    private String testApk;

    @Parameter(names = {"-tb"}, description = "Test apk file for version B")
    private String testApkB;

    @Parameter(names = {"--test-methods", "-m"}, description = "Test methods to instrument")
    private List<String> testMethods;

    @Parameter(names = {"--limit"}, description = "Limit parameter for exception controller")
    private int limit = 5;

    @Parameter(
        names = {"--method"}, description = "Generate and run instrumented tests on method level")
    private boolean methodLevel = false;

    @Parameter(names = {"--exclude-methods"}, description = "Excluded methods in general level",
        splitter = SemiColonParameterSplitterWithTrimming.class)
    private List<String> excludeMethods = new ArrayList<>();

    @Override
    public String name() {
        return "detect";
    }

    @Override
    public void run(CommandOptions options) throws Exception {
        if (!CommandUtils.isAndroidApkFiles(debugApkA, debugApkA, testApk)) {
            logger.error("Please specify correct apk files (filename must end with .apk)!");
            throw new RuntimeException();
        }

        IDevice[] devices = CommandUtils.detectUsableDevices(false, 2);
        if (devices == null) {
            logger.error("Can not find enough devices for detection!");
            throw new RuntimeException();
        }

        File testApkFileA = new File(testApk),
             testApkFileB = testApkB.isEmpty() ? testApkFileA : new File(testApkB);
        ExceptionHandlingBugFixDetector detector = new ExceptionHandlingBugFixDetector(devices,
            new File(debugApkA), new File(debugApkB), testApkFileA, testApkFileB, testMethods);
        detector.setLimit(limit);

        List<ExceptionHandlingBugFixDetector.Result> results;

        results = methodLevel ? detector.runDetectionInMethodLevel()
                              : detector.runDetectionInGeneral(excludeMethods);

        if (results.isEmpty()) {
            logger.info("Found none different testcase after instrumentation!");
            return;
        }

        logger.info("Found {} different testcase!", results.size());
        for (ExceptionHandlingBugFixDetector.Result result : results) {
            logger.info("{}!", result);
        }
    }
}
