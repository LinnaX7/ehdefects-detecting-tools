package fixeh.detector;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import fixeh.instrument.woventools.policy.ControlPolicyFactory;
import fixeh.instrument.woventools.policy.PolicyConfig;
import fixeh.instrument.woventools.policy.PolicyConfigUtils;
import fixeh.runner.AndroidTestResult;
import fixeh.runner.AndroidTestRunner;
import fixeh.runner.TestReport;
import fixeh.util.AndroidUtils;
import fixeh.util.ConcurrentUtils;
import fixeh.util.TmpDirUtils;

/**
 * Created by Shunjie Ding on 27/03/2018.
 */
public class ExceptionHandlingBugFixDetector {
    private final Logger logger = LoggerFactory.getLogger(ExceptionHandlingBugFixDetector.class);
    private final List<String> testMethods;
    private AndroidTestRunner runnerLeft, runnerRight;
    private int limit = 5;

    public ExceptionHandlingBugFixDetector(IDevice[] devices, File debugApkLeft, File debugApkRight,
        File debugTestApkLeft, File debugTestApkRight, List<String> testMethods) {
        this.testMethods = testMethods;

        if (devices == null || devices.length < 2) {
            throw new IllegalArgumentException("Must provide 2 android test devices!");
        }

        runnerLeft = new AndroidTestRunner(devices[0]);
        runnerRight = new AndroidTestRunner(devices[1]);

        // Assume that the given apks are signed and aligned
        runnerLeft.setApkFile(debugApkLeft.getAbsolutePath())
            .setTestApkFile(debugTestApkLeft.getAbsolutePath());
        runnerRight.setApkFile(debugApkRight.getAbsolutePath())
            .setTestApkFile(debugTestApkRight.getAbsolutePath());
    }

    private static String getRemotePolicyPath() {
        return AndroidUtils.REMOTE_TMPDIR + "/fixeh-policy.xml";
    }

    private static String convertPatternFromInt(int patternIdx, int limit) {
        StringBuilder builder = new StringBuilder();
        while (patternIdx != 0) {
            builder.append(patternIdx & 1);
            patternIdx >>= 1;
        }
        while (builder.length() < limit) {
            builder.append('0');
        }
        return builder.toString();
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    private void installApksAndSkipFollowingInstalls(AndroidTestRunner runner)
        throws InstallException {
        runner.installAndroidPackages();
        runner.setSkipInstall(true);
    }

    private Result diffReports(
        PolicyConfig config, TestReport left, TestReport right, Result baseline) {
        logger.debug("Reports:\n{}\n{}", left, right);

        HashMap<String, AndroidTestResult> testMethodResultMap = new HashMap<>();
        for (AndroidTestResult testResult : left.getResults()) {
            testMethodResultMap.put(testResult.getQualifiedTestName(), testResult);
        }

        List<String> resultTestMethods = new ArrayList<>();
        for (AndroidTestResult testResult : right.getResults()) {
            AndroidTestResult leftTestResult =
                testMethodResultMap.get(testResult.getQualifiedTestName());
            // Skip not matched
            if (leftTestResult != null) {
                if (leftTestResult.isPassed() != testResult.isPassed()) {
                    resultTestMethods.add(testResult.getQualifiedTestName());
                }
            }
        }

        if (resultTestMethods.isEmpty()) {
            return null;
        }

        List<String> methods =
            (config.getPolicyEntries() == null || config.getPolicyEntries().isEmpty())
            ? null
            : config.getPolicyEntries()
                  .stream()
                  .map(PolicyConfig.PolicyEntry::getValue)
                  .collect(Collectors.toList());
        String pattern = config.getGeneralPattern();
        if (pattern == null) {
            pattern = (config.getPolicyEntries() == null || config.getPolicyEntries().isEmpty())
                ? null
                : config.getPolicyEntries().get(0).getOther("pattern");
        }

        // if baseline exists, remove results in baseline
        if (baseline != null) {
            resultTestMethods.removeAll(baseline.getTestMethods());
        }

        if (resultTestMethods.isEmpty()) {
            return null;
        }

        return new Result(
            config.isExclude(), methods, pattern, config.getLimit(), resultTestMethods);
    }

    private static PolicyConfig defaultPolicyConfig() {
        PolicyConfig policyConfig = new PolicyConfig(null, null);
        policyConfig.setExclude(false);
        return policyConfig;
    }

    private Result runTestOnBothRunnerWithPolicyConfig(PolicyConfig policyConfig,
        File tmpConfigFile, ExecutorService executorService, Result baseline)
        throws ParserConfigurationException, TransformerException, TimeoutException,
               AdbCommandRejectedException, SyncException, IOException, ExecutionException,
               InterruptedException {
        // Generate policy config xml file and push it to the devices
        PolicyConfigUtils.saveDocToFile(
            PolicyConfigUtils.buildDocument(policyConfig), tmpConfigFile);

        AndroidUtils.pushFileTo(runnerLeft.getDevice(), tmpConfigFile, getRemotePolicyPath());
        AndroidUtils.pushFileTo(runnerRight.getDevice(), tmpConfigFile, getRemotePolicyPath());

        // Run test on both devices and get reports
        Future<TestReport> reportFutureLeft =
            executorService.submit(() -> runnerLeft.runAllTests());
        Future<TestReport> reportFutureRight =
            executorService.submit(() -> runnerRight.runAllTests());

        return diffReports(policyConfig, reportFutureLeft.get(), reportFutureRight.get(), baseline);
    }

    private List<Result> runDetectionWithPolicyConfigGenerator(PolicyConfigGenerator generator)
        throws TimeoutException, AdbCommandRejectedException, SyncException, IOException,
               ParserConfigurationException, InstallException, TransformerException,
               ExecutionException, InterruptedException {
        // Install all packages on both devices
        logger.info("Installing debug and test packages on test devices.");
        installApksAndSkipFollowingInstalls(runnerLeft);
        installApksAndSkipFollowingInstalls(runnerRight);

        final File tmpdir = TmpDirUtils.allocateTmpDir();
        final File tmpConfigFile = new File(tmpdir, "fixeh-policy.xml");
        logger.info("Save temporary xml file to {}.", tmpConfigFile.getAbsolutePath());

        logger.info("Begin to run tests ...");
        ExecutorService executorService = ConcurrentUtils.newExecutorService(2);

        Result baseline = runTestOnBothRunnerWithPolicyConfig(
            defaultPolicyConfig(), tmpConfigFile, executorService, null);
        logger.info("Baseline without control is {}", baseline);

        int runCount = 0;
        List<Result> result = new ArrayList<>();
        try {
            PolicyConfig policyConfig;
            while ((policyConfig = generator.next()) != null) {
                logger.info("Running test {}, remote policy should be {}", runCount++,
                    ControlPolicyFactory.buildGeneralControlPolicy(policyConfig));
                Result res = runTestOnBothRunnerWithPolicyConfig(
                    policyConfig, tmpConfigFile, executorService, baseline);
                if (res != null) {
                    result.add(res);
                }
            }
        } finally {
            executorService.shutdownNow();
            AndroidUtils.removeRemoteFile(runnerLeft.getDevice(), getRemotePolicyPath());
            AndroidUtils.removeRemoteFile(runnerRight.getDevice(), getRemotePolicyPath());
        }

        return result;
    }

    public List<Result> runDetectionInGeneral(List<String> excludedMethods) throws Exception {
        final GeneralPolicyConfigGenerator generator =
            new GeneralPolicyConfigGenerator(limit, excludedMethods);
        return runDetectionWithPolicyConfigGenerator(generator);
    }

    public List<Result> runDetectionInMethodLevel() throws Exception {
        final MethodLevelPolicyConfigGenerator generator =
            new MethodLevelPolicyConfigGenerator(testMethods, limit);
        return runDetectionWithPolicyConfigGenerator(generator);
    }

    private interface PolicyConfigGenerator { PolicyConfig next(); }

    private static class GeneralPolicyConfigGenerator implements PolicyConfigGenerator {
        private final int limit;
        private int patternIdx = 0;

        private List<String> excludedMethods;

        private GeneralPolicyConfigGenerator(int limit, List<String> excludedMethods) {
            this.limit = limit;
            this.excludedMethods = excludedMethods;
        }

        @Override
        public PolicyConfig next() {
            if (patternIdx == (1 << limit)) {
                return null;
            }

            PolicyConfig policyConfig = new PolicyConfig(null,
                excludedMethods.stream()
                    .map(m -> new PolicyConfig.PolicyEntry("method", m))
                    .collect(Collectors.toList()));
            policyConfig.setGeneralPattern(convertPatternFromInt(patternIdx++, limit));
            policyConfig.setLimit(limit);
            policyConfig.setExclude(true);
            return policyConfig;
        }
    }

    private static class MethodLevelPolicyConfigGenerator implements PolicyConfigGenerator {
        private final List<String> testMethods;
        private final int limit;

        private int methodIdx = 0;
        private int patternIdx = 0;

        private MethodLevelPolicyConfigGenerator(List<String> testMethods, int limit) {
            this.testMethods = testMethods;
            this.limit = limit;
        }

        @Override
        public PolicyConfig next() {
            if (patternIdx == (1 << limit)) {
                methodIdx++;
                patternIdx = 0;
            }

            if (methodIdx >= testMethods.size()) {
                return null;
            }

            PolicyConfig.PolicyEntry policyEntry =
                new PolicyConfig.PolicyEntry("method", testMethods.get(methodIdx));
            policyEntry.addOther("pattern", convertPatternFromInt(patternIdx++, limit));
            PolicyConfig policyConfig =
                new PolicyConfig(null, Collections.singletonList(policyEntry));

            policyConfig.setExclude(false);
            policyConfig.setLimit(limit);
            return policyConfig;
        }
    }

    public static class Result implements Serializable {
        private boolean exclude;
        private List<String> methods;
        private String pattern;
        private int limit;

        private List<String> testMethods;

        Result(boolean exclude, List<String> methods, String pattern, int limit,
            List<String> testMethods) {
            this.exclude = exclude;
            this.methods = methods == null ? new ArrayList<>() : methods;
            this.pattern = pattern;
            this.limit = limit;
            this.testMethods = testMethods;
        }

        public String getPattern() {
            return pattern;
        }

        public int getLimit() {
            return limit;
        }

        public List<String> getTestMethods() {
            return testMethods;
        }

        @Override
        public String toString() {
            return String.format(
                "Result (Exclude: %s, Method: {%s}, Pattern: %s, Limit: %d, Test Methods: {%s})",
                exclude, methods.stream().collect(Collectors.joining(",\n")), pattern, limit,
                testMethods.stream().collect(Collectors.joining(",\n")));
        }
    }
}
