package fixeh.project.vcs;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import fixeh.Constants;
import fixeh.TestBase;
import fixeh.project.vcs.exceptions.InvalidRevisionException;

public class VcsTest extends TestBase {
    private final Logger logger = LoggerFactory.getLogger(VcsTest.class);

    @Test
    public void testCheckout() throws InvalidRevisionException, IOException {
        Vcs vcs = project.getVcs();
        Revision revision = vcs.getRevision("abb23f9c086e2ba036a484fbc0eb27eef958b364");

        File tmpDir = Paths.get(Constants.TMPDIR, "checkout_test").toFile();
        if (tmpDir.exists()) {
            FileUtils.forceDelete(tmpDir);
        }
        Assert.assertTrue(vcs.checkout(revision, tmpDir.getAbsolutePath(), false));

        final String[] filesInRevision = new String[] {".gitignore", ".idea/compiler.xml",
            ".idea/inspectionProfiles/Project_Default.xml", ".idea/misc.xml", ".idea/modules.xml",
            ".idea/modules/fixeh-testpro.iml", ".idea/modules/fixeh-testpro_main.iml",
            ".idea/modules/fixeh-testpro_test.iml", ".idea/vcs.xml", "build.gradle",
            "gradle/wrapper/gradle-wrapper.properties", "gradlew", "gradlew.bat", "settings.gradle",
            "src/main/java/Main.java", "src/main/java/core/HttpUtils.java"};

        // Assert all files exist.
        Assert.assertTrue(Arrays.stream(filesInRevision).allMatch(file -> {
            File f = new File(tmpDir, file);
            return f.exists() && f.canRead();
        }));

        // Assert gradlew has executable permission
        Assert.assertTrue(new File(tmpDir, "gradlew").canExecute());

        vcs.removeDir(revision);
    }
}
