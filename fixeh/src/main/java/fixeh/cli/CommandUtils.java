package fixeh.cli;

import com.android.ddmlib.IDevice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import fixeh.util.AndroidUtils;

/**
 * Created by Shunjie Ding on 2018/4/4.
 */
final class CommandUtils {
    public static boolean isAndroidApkFile(String file) {
        return file != null && file.endsWith(".apk");
    }

    public static boolean isAndroidApkFiles(String... files) {
        return Arrays.stream(files).allMatch(CommandUtils::isAndroidApkFile);
    }

    public static boolean isXmlFile(String file) {
        return file != null && file.endsWith(".xml");
    }

    public static boolean isJarFile(String file) {
        return file != null && file.endsWith(".jar");
    }

    private static String getRemotePolicyPath() {
        return AndroidUtils.REMOTE_TMPDIR + "/fixeh-policy.xml";
    }

    public static IDevice[] detectUsableDevices(boolean forceExecute, int size) {
        IDevice[] devices = AndroidUtils.getAdb().getDevices();
        if (devices.length < size) {
            return null;
        }

        List<IDevice> result = new ArrayList<>();
        for (IDevice device : devices) {
            if (device.isOnline()) {
                if (forceExecute
                    || !AndroidUtils.isRemoteFileExists(device, getRemotePolicyPath())) {
                    result.add(device);
                    if (result.size() == size) {
                        break;
                    }
                }
            }
        }

        if (result.size() < size) {
            return null;
        }

        return result.toArray(new IDevice[0]);
    }
}
