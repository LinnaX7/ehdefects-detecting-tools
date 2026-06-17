package fixeh.cli;

import com.android.ddmlib.IDevice;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import fixeh.Constants;
import fixeh.instrument.RemotePolicyController;
import fixeh.instrument.woventools.policy.InvalidPolicyConfigException;
import fixeh.instrument.woventools.policy.PolicyConfig;
import fixeh.instrument.woventools.policy.PolicyConfigUtils;
import fixeh.runner.AndroidTestRunner;
import fixeh.runner.TestReport;
import fixeh.util.AndroidUtils;
import fixeh.util.ResourceUtils;
import fixeh.util.TmpDirUtils;

// TODO Support junit test
@Parameters(commandNames = "test", commandDescription = "Run android instrumentation tests")
public class CommandTest implements Command {
    private final Logger logger = LoggerFactory.getLogger(CommandTest.class);

    @Parameter(names = {"-a", "--apk"}, description = "Target apk file", required = true)
    private String apk;

    @Parameter(names = {"-b", "--test-apk"}, description = "Test apk file", required = true)
    private String testApk;

    @Parameter(names = {"--force-sign"}, description = "Force sign apk files")
    private boolean forceSign;

    @Parameter(names = {"--class"}, description = "Specify test class to run")
    private String testClass;

    @Parameter(names = {"--method"},
        description = "Specify test method to run, must specify class at the same time")
    private String testMethod;

    @Parameter(names = {"--policy-xml"}, description = "Specify policy xml file")
    private String policyXml;

    @Parameter(names = {"--force"}, description = "Force execute tests (override policy configs)")
    private boolean forceExecute;

    @Parameter(names = {"--uninstall-previous"},
        description = "Uninstall previous packages to avoid problems")
    private boolean uninstallPrevious;

    // TODO implement this
    @Parameter(names = {"--collect"},
        description =
            "Collect resource related api called in tests, save results to rr-methods.txt")
    private boolean collect;

    private static String getRemotePolicyPath() {
        return AndroidUtils.REMOTE_TMPDIR + "/fixeh-policy.xml";
    }

    @Override
    public String name() {
        return "test";
    }

    private boolean checkApk(String file) {
        return file != null && file.endsWith(".apk") && new File(file).isFile();
    }

    private String getFileName(String file) {
        return Paths.get(file).getFileName().toString();
    }

    private IDevice detectUsable(IDevice[] devices) {
        for (IDevice device : devices) {
            if (!device.isOnline()) {
                continue;
            }

            if (forceExecute) {
                return device;
            }

            if (!AndroidUtils.isRemoteFileExists(device, getRemotePolicyPath())) {
                return device;
            }
        }
        return null;
    }

    private File getPolicyXmlFile() {
        if (policyXml != null && !policyXml.isEmpty()) {
            if (policyXml.endsWith(".xml")) {
                File localConfig = new File(policyXml);
                if (localConfig.isFile()) {
                    return localConfig;
                } else {
                    logger.warn("Specified policy config does not exists!");
                }
            } else {
                logger.warn("Specified policy config does not end with .xml! Fallback to default!");
            }
        }
        return ResourceUtils.getFixehPolicyXml();
    }

    private PolicyConfig parsePolicyConfig() throws IOException, InvalidPolicyConfigException {
        File f = getPolicyXmlFile();
        return PolicyConfigUtils.buildPolicyConfig(f.toURI().toURL());
    }

    private Thread startDaemonThread(Runnable runnable) {
        if (runnable == null) {
            return null;
        }

        Thread t = new Thread(runnable);
        t.setDaemon(true);
        t.start();
        return t;
    }

    private void startControlServerIfEnabled(PolicyConfig policyConfig) {
        if (policyConfig.isRemoteControllerEnabled()) {
            logger.info("Starting control server in another thread ...");
            startDaemonThread(new RemotePolicyController());
        }
    }

    @Override
    public void run(CommandOptions options) throws Exception {
        if (!checkApk(apk) || !checkApk(testApk)) {
            throw new RuntimeException("Invalid apk files!");
        }

        if (testClass != null && testClass.isEmpty()) {
            throw new RuntimeException("Test class must not be empty!");
        }

        // Parse policy config and start control server if enabled
        PolicyConfig policyConfig = parsePolicyConfig();
        startControlServerIfEnabled(policyConfig);

        File apkFile = new File(apk);
        File testApkFile = new File(testApk);

        if (forceSign) {
            logger.info("Signing apks ...");
            File tmpDir = TmpDirUtils.allocateTmpDir();

            File signedApkFile = new File(tmpDir, getFileName(apk));
            File signedTestApkFile = new File(tmpDir, getFileName(testApk));
            AndroidUtils.alignAndSignApk(
                Constants.LATEST_ANDROID_BUILD_TOOLS_VERSION, apkFile, signedApkFile);
            AndroidUtils.alignAndSignApk(
                Constants.LATEST_ANDROID_BUILD_TOOLS_VERSION, testApkFile, signedTestApkFile);

            apkFile = signedApkFile;
            testApkFile = signedTestApkFile;
        }

        IDevice[] devices = AndroidUtils.getAdb().getDevices();
        if (devices.length == 0) {
            throw new RuntimeException("No usable devices detected!");
        }

        IDevice device = detectUsable(devices);
        if (device == null) {
            throw new RuntimeException("No usable devices detected!");
        }

        // Push config file to remote
        AndroidUtils.pushFileTo(device, getPolicyXmlFile(), getRemotePolicyPath());

        AndroidTestRunner runner = new AndroidTestRunner(System.out, device);
        runner.setApkFile(apkFile.getAbsolutePath()).setTestApkFile(testApkFile.getAbsolutePath());
        runner.setUninstallPrevious(uninstallPrevious);

        TestReport report;
        if (testClass != null && !testClass.isEmpty()) {
            if (testMethod != null && !testMethod.isEmpty()) {
                logger.info("Running test {}: {} ...", testClass, testMethod);
                report = runner.runTest(testClass, testMethod);
            } else {
                logger.info("Running tests in class {} ...", testClass);
                report = runner.runTests(testClass);
            }
        } else {
            logger.info("Running all tests ...");
            report = runner.runAllTests();
        }

        logger.info("Test result:\n{}", report);

        // Delete remote config file
        AndroidUtils.removeRemoteFile(device, getRemotePolicyPath());
    }
}
