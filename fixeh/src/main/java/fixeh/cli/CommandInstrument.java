package fixeh.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import fixeh.Constants;
import fixeh.instrument.AndroidApplicationPackageInstrumenter;
import fixeh.instrument.AndroidTestPackageInstrumenter;
import fixeh.instrument.InstrumentInfo;
import fixeh.instrument.JavaByteCodeInstrumenter;
import fixeh.util.AndroidUtils;
import fixeh.util.TmpDirUtils;

/**
 * Created by Shunjie Ding on 23/01/2018.
 */
@Parameters(commandNames = "instrument",
    commandDescription = "Instrument apk/jar files to inject control codes")
public class CommandInstrument implements Command {
    private final Logger logger = LoggerFactory.getLogger(CommandInstrument.class);

    @Parameter(names = {"--android-sdk"}, description = "Android SDK version.")
    private int androidSdkVersion = Constants.LATEST_ANDROID_SDK_VERSION;

    @Parameter(names = {"--android-build-tools"}, description = "Android build tools version")
    private String androidBuildToolsVersion = Constants.LATEST_ANDROID_BUILD_TOOLS_VERSION;

    @Parameter(names = {"--woven-jars"},
        description = "Java packages used to instrument, at least provide woventools.jar",
        required = true, splitter = SemiColonParameterSplitterWithTrimming.class)
    private List<String> wovenJars;

    @Parameter(names = {"-o", "--output"},
        description = "Output file name (dir), default is instrumented/{filename}")
    private String output;

    @Parameter(
        names = {"-t", "--target"}, description = "Target file, should be apk/jar", required = true)
    private String target;

    @Parameter(names = {"--test"}, description = "Instrument test packages")
    private boolean test;

    @Parameter(names = {"--sign"}, description = "Sign and align the output apk")
    private boolean sign;

    private static boolean isDirectory(String path) {
        return new File(path).isDirectory();
    }

    private static boolean isDirectory(Path path) {
        return path.toFile().isDirectory();
    }

    @Override
    public String name() {
        return "instrument";
    }

    private String getFileName(String filePath) {
        return Paths.get(filePath).getFileName().toString();
    }

    private String getOutputFilePath() throws IOException {
        Path targetPath = Paths.get(target).toAbsolutePath();
        String targetName = targetPath.getFileName().toString();
        if (output == null) {
            Path outputDir = Paths.get("instrumented");
            Files.createDirectories(outputDir);
            return outputDir.resolve(targetName).toString();
        } else {
            if (isDirectory(output)) {
                return Paths.get(output, targetName).toString();
            } else {
                return output;
            }
        }
    }

    private String getStatsOutputFilePath() throws IOException {
        Path outputDir = output == null ? Paths.get("instrumented") : Paths.get(output);
        String targetName = Paths.get(target).getFileName().toString();
        if (output == null) {
            Files.createDirectories(outputDir);
        } else if (!outputDir.toFile().isDirectory()) {
            targetName = outputDir.getFileName().toString();
            outputDir = outputDir.getParent();
        }
        if (targetName.endsWith(".apk")) {
            targetName = targetName.substring(0, targetName.length() - 4);
        }
        targetName += ".csv";
        return outputDir.resolve(targetName).toAbsolutePath().toString();
    }

    private JavaByteCodeInstrumenter newInstrumenter() {
        return test ? new AndroidTestPackageInstrumenter(
                          target, wovenJars, androidSdkVersion, androidBuildToolsVersion)
                    : new AndroidApplicationPackageInstrumenter(
                          target, wovenJars, androidSdkVersion, androidBuildToolsVersion);
    }

    @Override
    public void run(CommandOptions options) throws Exception {
        if (!CommandUtils.isJarFile(target) && !CommandUtils.isAndroidApkFile(target)) {
            throw new RuntimeException("Unsupported target file");
        }

        logger.info("Instrumenting on {} ...", target);

        File tmpDir = TmpDirUtils.allocateTmpDir();

        if (target.endsWith(".apk")) {
            JavaByteCodeInstrumenter instrumenter = newInstrumenter();
            boolean instrumented = instrumenter.instrumentAndSave(tmpDir);
            if (instrumented && instrumenter instanceof AndroidApplicationPackageInstrumenter) {
                String statsOutput = getStatsOutputFilePath();
                logger.info("Saving instrumenting statistics to {}", statsOutput);
                HashSet<InstrumentInfo> stats =
                    ((AndroidApplicationPackageInstrumenter) instrumenter).getStatistics();
                InstrumentInfo.writeToCsvFile(stats, statsOutput);
            }
        } else {
            throw new RuntimeException("Not supported yet!");
        }

        if (sign) {
            AndroidUtils.alignAndSignApk(androidBuildToolsVersion,
                new File(tmpDir, getFileName(target)), new File(getOutputFilePath()));
        } else {
            FileUtils.copyFile(
                new File(tmpDir, getFileName(target)), new File(getOutputFilePath()));
        }

        logger.info("Instrument complete! Result files is {}.", getOutputFilePath());

        TmpDirUtils.releaseTmpDir(tmpDir);
    }
}
