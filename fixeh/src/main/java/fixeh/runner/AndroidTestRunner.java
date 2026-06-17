package fixeh.runner;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.testrunner.ITestRunListener;
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner;
import com.android.ddmlib.testrunner.TestIdentifier;
import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.DexClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Shunjie Ding on 26/01/2018.
 */
public class AndroidTestRunner extends TestRunner<AndroidTestResult> {
    private final IDevice device;
    private Logger logger = LoggerFactory.getLogger(AndroidTestRunner.class);
    private String apkFile;

    private String testApkFile;

    private boolean uninstallPrevious = false;

    private boolean skipInstall = false;

    public AndroidTestRunner(IDevice device) {
        super(null);
        this.device = device;
    }

    public AndroidTestRunner(PrintStream output, IDevice device) {
        super(output);
        this.device = device;
    }

    private static String readPackageNameFromApk(String apkFile) throws IOException {
        try (ApkFile f = new ApkFile(new File(apkFile))) {
            return f.getApkMeta().getPackageName();
        }
    }

    public AndroidTestRunner setApkFile(String apkFile) {
        this.apkFile = apkFile;
        return this;
    }

    public AndroidTestRunner setTestApkFile(String testApkFile) {
        this.testApkFile = testApkFile;
        return this;
    }

    private void installApk(String f) throws InstallException {
        if (uninstallPrevious) {
            try {
                device.uninstallPackage(readPackageNameFromApk(f));
            } catch (IOException | InstallException e) {
                // ignore
            }
        }
        device.installPackage(f, true, "-t", "-d", "-g");
    }

    private TestRunListener runTestClasses(List<String> testClasses, String testPackage)
        throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException,
               IOException {
        RemoteAndroidTestRunner runner = new RemoteAndroidTestRunner(
            testPackage, "android.support.test.runner.AndroidJUnitRunner", device);
        TestRunListener listener = new TestRunListener();
        runner.setClassNames(testClasses.toArray(new String[0]));
        runner.run(listener);
        return listener;
    }

    private TestRunListener runTestClass(String testClass, String testPackage)
        throws IOException, AdbCommandRejectedException, ShellCommandUnresponsiveException,
               TimeoutException {
        return runTestClasses(Collections.singletonList(testClass), testPackage);
    }

    private TestRunListener runAllTest(String testPackage)
        throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException,
               IOException {
        RemoteAndroidTestRunner runner = new RemoteAndroidTestRunner(
            testPackage, "android.support.test.runner.AndroidJUnitRunner", device);
        TestRunListener listener = new TestRunListener();
        runner.run(listener);
        return listener;
    }

    public void setUninstallPrevious(boolean uninstallPrevious) {
        this.uninstallPrevious = uninstallPrevious;
    }

    @Override
    public List<String> getTestClasses() throws Exception {
        // FIXME Classes in test apk should all be tests but there are exceptions.
        try (ApkFile apkFile = new ApkFile(new File(testApkFile))) {
            DexClass[] dexClasses = apkFile.getDexClasses();
            return Arrays
                .stream(dexClasses)
                // select public classes
                .filter(clz
                    -> !clz.isEnum() && !clz.isInterface() && !clz.isAnnotation() && clz.isPublic())
                .map(DexClass::getClassType)
                // filter out inner classes
                .filter(t -> !t.contains("$"))
                .collect(Collectors.toList());
        }
    }

    public void installAndroidPackages() throws InstallException {
        try {
            installApk(apkFile);
            installApk(testApkFile);
        } catch (InstallException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    private TestRunListener runTest(String testMethod, String testClass, String testPackage)
        throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException,
               IOException {
        RemoteAndroidTestRunner runner = new RemoteAndroidTestRunner(
            testPackage, "android.support.test.runner.AndroidJUnitRunner", device);
        TestRunListener listener = new TestRunListener();
        runner.setClassName(testClass);
        runner.setMethodName(testClass, testMethod);
        runner.run(listener);
        return listener;
    }

    @Override
    public TestReport runTest(String testClass, String testMethod) throws Exception {
        if (!skipInstall) {
            installAndroidPackages();
        }

        String testPackageName = readPackageNameFromApk(testApkFile);

        TestRunListener listener = runTest(testMethod, testClass, testPackageName);

        return listener.generateReport();
    }

    @Override
    public TestReport runTests(String testClass) throws Exception {
        if (!skipInstall) {
            installAndroidPackages();
        }

        String testPackageName = readPackageNameFromApk(testApkFile);

        TestRunListener listener = runTestClass(testClass, testPackageName);

        return listener.generateReport();
    }

    @Override
    public TestReport runAllTests() throws Exception {
        if (!skipInstall) {
            installAndroidPackages();
        }

        String testPackageName = readPackageNameFromApk(testApkFile);

        TestRunListener listener = runAllTest(testPackageName);

        return listener.generateReport();
    }

    public IDevice getDevice() {
        return device;
    }

    public void setSkipInstall(boolean skipInstall) {
        this.skipInstall = skipInstall;
    }

    class TestRunListener implements ITestRunListener {
        private boolean passed = true;

        private List<AndroidTestResult> results;

        private String errorMessage;

        private long elapsedTime;

        private AndroidTestResult current;

        private TestIdentifier currentTestId;

        public boolean isPassed() {
            return passed;
        }

        public List<AndroidTestResult> getResults() {
            return results;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public long getElapsedTime() {
            return elapsedTime;
        }

        public TestReport generateReport() {
            return new TestReport(isPassed(), getResults(), getErrorMessage(), getElapsedTime());
        }

        @Override
        public void testRunStarted(String runName, int testCount) {
            results = new ArrayList<>(testCount);
        }

        private AndroidTestResult createNewTest(String className, String testName) {
            AndroidTestResult testResult = new AndroidTestResult();
            testResult.setClassName(className);
            testResult.setMethodName(testName);
            testResult.setPassed(true);
            return testResult;
        }

        @Override
        public void testStarted(TestIdentifier test) {
            current = createNewTest(test.getClassName(), test.getTestName());
            currentTestId = test;
        }

        private void setFailed(TestIdentifier test, String trace) {
            if (test.equals(currentTestId)) {
                current.setPassed(false);
                current.setTrace(trace);
                passed = false;
            } else {
                logger.warn("Current test and result not match!");
            }
        }

        @Override
        public void testFailed(TestIdentifier test, String trace) {
            setFailed(test, trace);
        }

        @Override
        public void testAssumptionFailure(TestIdentifier test, String trace) {
            setFailed(test, trace);
        }

        @Override
        public void testIgnored(TestIdentifier test) {
            // reset current test result
            current = null;
        }

        @Override
        public void testEnded(TestIdentifier test, Map<String, String> testMetrics) {
            // Ignore runMetrics
            if (test.equals(currentTestId)) {
                if (current != null) {
                    results.add(current);
                }
            } else {
                logger.warn("Current test and result not match!");
            }
        }

        @Override
        public void testRunFailed(String errorMessage) {
            passed = false;
            this.errorMessage = errorMessage;
        }

        @Override
        public void testRunStopped(long elapsedTime) {
            passed = false;
            this.errorMessage = "Stopped";
            this.elapsedTime = elapsedTime;
        }

        @Override
        public void testRunEnded(long elapsedTime, Map<String, String> runMetrics) {
            // Ignore runMetrics
            this.elapsedTime = elapsedTime;
        }
    }
}
