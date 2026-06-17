package fixeh.util;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.DdmPreferences;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import fixeh.Constants;

/**
 * Created by Shunjie Ding on 24/01/2018.
 */
public final class AndroidUtils {
    public static final String REMOTE_TMPDIR = "/data/local/tmp";
    private static final Logger logger = LoggerFactory.getLogger(AndroidUtils.class);
    private static final String ANDROID_UTILS_TMPDIR = Constants.TMPDIR + "/android_utils";
    private static AndroidDebugBridge ADB;

    public static AndroidDebugBridge getAdb() {
        if (ADB == null) {
            DdmPreferences.setSelectedDebugPort(8701);
            AndroidDebugBridge.initIfNeeded(false);
            ADB = AndroidDebugBridge.createBridge(getAdbPath(), true);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        return ADB;
    }

    private static File getDebugKeyStore() {
        return ResourceUtils.getDebugKeyStore();
    }

    public synchronized static boolean isRemoteFileExists(IDevice device, String remotePath) {
        if (device == null) {
            return false;
        }

        try {
            device.pullFile(remotePath, ANDROID_UTILS_TMPDIR + "/whatever");
        } catch (IOException | AdbCommandRejectedException | TimeoutException | SyncException e) {
            return false;
        }

        return true;
    }

    public static void removeRemoteFile(IDevice device, String remotePath) {
        if (device == null) {
            logger.warn("Removing file on a null device! Ignore!");
            return;
        }

        try {
            device.executeShellCommand("rm " + remotePath, new NullOutputReceiver());
        } catch (TimeoutException | AdbCommandRejectedException | IOException
            | ShellCommandUnresponsiveException e) {
            e.printStackTrace();
            // ignore
        }
    }

    public static void removeRemoteDir(IDevice device, String remotePath) {
        if (device == null) {
            logger.warn("Removing dir on a null device! Ignore!");
            return;
        }

        try {
            device.executeShellCommand("rm -r " + remotePath, new NullOutputReceiver());
        } catch (TimeoutException | AdbCommandRejectedException | IOException
            | ShellCommandUnresponsiveException e) {
            e.printStackTrace();
            // ignore
        }
    }

    public static void pushFileTo(IDevice device, File file, String remotePath)
        throws TimeoutException, AdbCommandRejectedException, SyncException, IOException {
        if (device == null) {
            logger.warn("Pushing local file {} to a null device! Ignore!", file.getAbsolutePath());
            return;
        }

        if (!file.exists()) {
            logger.error("Local file {} is empty!", file.getAbsolutePath());
            return;
        }

        device.pushFile(file.getAbsolutePath(), remotePath);
    }

    public static void pushFileToAllDevices(String localPath, String remotePath) {
        File f = new File(localPath);
        if (!f.exists()) {
            logger.error("Local file {} is empty!", f.getAbsolutePath());
        }

        AndroidDebugBridge adb = getAdb();
        for (IDevice device : adb.getDevices()) {
            try {
                pushFileTo(device, f, remotePath);
            } catch (
                TimeoutException | AdbCommandRejectedException | IOException | SyncException e) {
                logger.error("Exception occurs when push file {} to device {}!",
                    f.getAbsolutePath(), device.getAvdName());
                e.printStackTrace();
            }
        }
    }

    private static String getAdbPath() {
        return Paths.get(Constants.ANDROID_HOME, "platform-tools/adb").toString();
    }

    private static boolean signApk(File buildToolsDir, File apkFile) {
        try {
            logger.info("{} sign --ks {} {}",
                Paths.get(buildToolsDir.getAbsolutePath(), "apksigner").toString(),
                getDebugKeyStore().getAbsolutePath(), apkFile.getAbsolutePath());

            Process process = Runtime.getRuntime().exec(new String[] {
                Paths.get(buildToolsDir.getAbsolutePath(), "apksigner").toString(), "sign", "--ks",
                getDebugKeyStore().getAbsolutePath(), apkFile.getAbsolutePath()});
            // Write password for keystore
            process.getOutputStream().write("android\n".getBytes());
            process.getOutputStream().flush();
            return process.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean alignApk(File buildToolsDir, File apkFile, File output) {
        try {
            logger.info("{} -f 4 {} {}",
                Paths.get(buildToolsDir.getAbsolutePath(), "zipalign").toString(),
                apkFile.getAbsolutePath(), output.getAbsolutePath());

            Process process = Runtime.getRuntime().exec(
                new String[] {Paths.get(buildToolsDir.getAbsolutePath(), "zipalign").toString(),
                    "-f", "4", apkFile.getAbsolutePath(), output.getAbsolutePath()});
            return process.waitFor() == 0 && output.isFile();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean signApk(String buildToolsVersion, File apkFile, File outputFile) {
        if (!apkFile.isFile()) {
            throw new IllegalArgumentException(
                "Input file does not exists or isn't a regular file.");
        }

        File buildToolsDir = new File(Constants.ANDROID_HOME, "build-tools/" + buildToolsVersion);
        if (!buildToolsDir.isDirectory()) {
            throw new IllegalArgumentException(
                "Build tools version " + buildToolsVersion + " does not exists!");
        }

        try {
            File tmpDir = TmpDirUtils.allocateTmpDir();

            File tmpFile = new File(tmpDir, apkFile.getName());
            FileUtils.copyFile(apkFile, tmpFile);
            logger.info("cp {} {}", apkFile.getAbsolutePath(), tmpFile.getAbsolutePath());

            if (!signApk(buildToolsDir, tmpFile)) {
                return false;
            }

            FileUtils.moveFile(tmpFile, outputFile);
            logger.info("mv {} {}", tmpDir.getAbsolutePath(), outputFile.getAbsolutePath());
            TmpDirUtils.releaseTmpDir(tmpDir);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean alignAndSignApk(String buildToolsVersion, File apkFile, File outputFile) {
        if (!apkFile.isFile()) {
            throw new IllegalArgumentException(
                "Input file does not exists or isn't a regular file.");
        }

        File buildToolsDir = new File(Constants.ANDROID_HOME, "build-tools/" + buildToolsVersion);
        if (!buildToolsDir.isDirectory()) {
            throw new IllegalArgumentException(
                "Build tools version " + buildToolsVersion + " does not exists!");
        }

        try {
            File tmpDir = TmpDirUtils.allocateTmpDir();

            File output = new File(tmpDir, apkFile.getName());

            // Ignore result of align apk because when apk is already aligned, zipalign returns 1
            alignApk(buildToolsDir, apkFile, output);

            if (!signApk(buildToolsDir, output)) {
                return false;
            }

            FileUtils.moveFile(output, outputFile);
            logger.info("mv {} {}", output.getAbsolutePath(), outputFile.getAbsolutePath());
            TmpDirUtils.releaseTmpDir(tmpDir);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> availableSDKVersions() {
        File[] sdks = new File(Constants.ANDROID_HOME, "platforms")
                          .listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
        if (sdks == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(sdks)
            .filter(f -> f.getName().startsWith("android-"))
            .map(f -> f.getName().substring(8))
            .sorted()
            .collect(Collectors.toList());
    }

    public static List<String> availableBuildToolsVersions() {
        File[] buildTools = new File(Constants.ANDROID_HOME, "build-tools")
                                .listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
        if (buildTools == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(buildTools).map(File::getName).sorted().collect(Collectors.toList());
    }

    public static String getLatestBuildToolsVersion() {
        List<String> buildTools = availableBuildToolsVersions();
        if (buildTools.isEmpty()) {
            return null;
        }
        return buildTools.get(buildTools.size() - 1);
    }

    public static int getLatestSdkVersion() {
        List<String> sdks = availableSDKVersions();
        if (sdks.isEmpty()) {
            return 0;
        }
        String sdkVersion = sdks.get(sdks.size() - 1);
        try {
            return Integer.valueOf(sdkVersion);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String getApkAnalyzerPath() {
        return Paths.get(Constants.ANDROID_HOME, "tools/bin/apkanalyzer").toString();
    }
}
