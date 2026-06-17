package fixeh.instrument;

import java.nio.file.Paths;
import java.util.List;

/**
 * Created by Shunjie Ding on 22/01/2018.
 */
public abstract class AndroidJavaByteCodeInstrumenter extends JavaByteCodeInstrumenter {
    private final int sdkVersion;

    private final String buildToolsVersion;

    private final String androidHome;

    protected AndroidJavaByteCodeInstrumenter(List<String> classPaths, String targetFile,
        String javaHome, int sdkVersion, String buildToolsVersion, String androidHome) {
        super(classPaths, targetFile, javaHome);
        this.sdkVersion = sdkVersion;
        this.buildToolsVersion = buildToolsVersion;
        this.androidHome = androidHome;
    }

    @Override
    protected List<String> getClassPaths() {
        List<String> res = super.getClassPaths();
        res.add(getAndroidJarPath());
        return res;
    }

    public int getSdkVersion() {
        return sdkVersion;
    }

    public String getBuildToolsVersion() {
        return buildToolsVersion;
    }

    public String getAndroidHome() {
        return androidHome;
    }

    protected String getPlatformPath() {
        return Paths.get(androidHome, "platforms", "android-" + sdkVersion).toString();
    }

    protected String getBuildToolsPath() {
        return Paths.get(androidHome, "build-tools", buildToolsVersion).toString();
    }

    protected String getPlatformToolsPath() {
        return Paths.get(androidHome, "platform-tools").toString();
    }

    protected String getAndroidJarPath() {
        return Paths.get(getPlatformPath(), "android.jar").toString();
    }
}
