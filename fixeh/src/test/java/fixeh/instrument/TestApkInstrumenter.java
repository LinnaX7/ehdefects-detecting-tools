package fixeh.instrument;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.Objects;
import fixeh.util.TmpDirUtils;

/**
 * Created by Shunjie Ding on 23/01/2018.
 */
public class TestApkInstrumenter {
    private String getResource(String name) {
        return Objects.requireNonNull(getClass().getClassLoader().getResource(name)).getPath();
    }

    @Test
    public void testApkInstrumenterOnK9MailReleaseApk() throws Exception {
        File tmpDir = TmpDirUtils.allocateTmpDir();

        File targetFile = new File(getResource("k9mail-release-unsigned.apk"));
        AndroidApplicationPackageInstrumenter instrumenter =
            new AndroidApplicationPackageInstrumenter(targetFile.getAbsolutePath(),
                Collections.singletonList(getResource("fixeh-woventools.jar")), 25, "25.0.3");
        instrumenter.instrumentAndSave(tmpDir);

        File outputFile = new File(tmpDir, targetFile.getName());
        Assert.assertTrue(outputFile.isFile());

        TmpDirUtils.releaseTmpDir(tmpDir);
    }
}
