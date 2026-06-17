package fixeh.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import fixeh.Constants;

/**
 * Created by Shunjie Ding on 13/01/2018.
 */
public final class TmpDirUtils {
    private static final Set<File> tmpDirs = new HashSet<>(20);

    static {
        // Delete tmp dirs on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> tmpDirs.forEach(f -> {
            if (f.exists()) {
                try {
                    FileUtils.forceDelete(f);
                } catch (IOException e) {
                    // ignore
                }
            }
        })));
    }

    public synchronized static File allocateTmpDir() throws IOException {
        RandomStringGenerator randomStringGenerator =
            new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
                .build();

        File file = new File(Constants.TMPDIR, randomStringGenerator.generate(10));
        while (tmpDirs.contains(file) || file.exists()) {
            file = new File(Constants.TMPDIR, randomStringGenerator.generate(10));
        }
        Files.createDirectory(file.toPath());
        tmpDirs.add(file);
        return file;
    }

    public synchronized static void releaseTmpDir(File file) throws IOException {
        if (tmpDirs.contains(file)) {
            if (file.exists()) {
                FileUtils.forceDelete(file);
            }
            tmpDirs.remove(file);
        }
    }
}
