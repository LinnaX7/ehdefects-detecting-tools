package fixeh.instrument;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import fixeh.util.TmpDirUtils;

/**
 * Created by Shunjie Ding on 22/01/2018.
 */
public abstract class JavaByteCodeInstrumenter {
    private final int javaMinorVersion = 8;
    private List<String> classPaths;
    private String targetFile;
    private String javaHome;

    protected JavaByteCodeInstrumenter(
        List<String> classPaths, String targetFile, String javaHome) {
        this.targetFile = targetFile;
        this.javaHome = javaHome;
        setClassPaths(classPaths);
    }

    private static String getFileName(String filePath) {
        return Paths.get(filePath).getFileName().toString();
    }

    protected List<String> getClassPaths() {
        List<String> res = new ArrayList<>(classPaths);
        res.add(getRtJarPath());
        return res;
    }

    public void setClassPaths(List<String> classPaths) {
        this.classPaths = new ArrayList<>(classPaths);
    }

    protected String getTargetFile() {
        return targetFile;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public String getRtJarPath() {
        return Paths.get(getJavaHome(), "lib/rt.jar").toString();
    }

    public int getJavaMinorVersion() {
        return javaMinorVersion;
    }

    protected abstract boolean instrumentAndSave(File target, File outputDir);

    public boolean instrumentAndSave(File outputDir) {
        return instrumentAndSave(new File(getTargetFile()), outputDir);
    }

    /**
     * Instrument the target file and overwrite the original file.
     */
    public boolean instrumentAndSave() {
        try {
            File tmpdir = TmpDirUtils.allocateTmpDir();
            instrumentAndSave(tmpdir);

            // Copy to original place
            FileUtils.copyFile(new File(tmpdir, getFileName(targetFile)), new File(targetFile));
            TmpDirUtils.releaseTmpDir(tmpdir);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
