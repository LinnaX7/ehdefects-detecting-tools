package fixeh.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import fixeh.Constants;
import fixeh.util.AndroidUtils;

/**
 * Created by Shunjie Ding on 24/01/2018.
 */
@Parameters(
    commandNames = "sign", commandDescription = "Sign (and align) with default debug keystore")
public class CommandSign implements Command {
    private final Logger logger = LoggerFactory.getLogger(CommandSign.class);

    @Parameter(names = {"--android-build-tools"},
        description = "Android build tools version, currently must be 25.0.0 or later")
    private String androidBuildToolsVersion = Constants.LATEST_ANDROID_BUILD_TOOLS_VERSION;

    @Parameter(names = {"--align"}, description = "Zipalign the apk")
    private boolean align;

    @Parameter(names = {"-o", "--output"},
        description = "Output file name, default is {filename}-signed.apk")
    private String output;

    @Parameter(names = {"-t", "--target"}, description = "Target apk file", required = true)
    private String target;

    @Override
    public String name() {
        return "sign";
    }

    public String getOutputFile() {
        if (output != null) {
            return output;
        }

        Path targetPath = Paths.get(target).toAbsolutePath();
        String targetName = targetPath.getFileName().toString();
        if (targetName.endsWith(".apk")) {
            String outputFileName;
            if (targetName.contains("unsigned")) {
                outputFileName = targetName.replaceAll("unsigned", "signed");
            } else {
                outputFileName = targetName.substring(0, targetName.length() - 4) + "-signed.apk";
            }
            return targetPath.getParent().resolve(outputFileName).toString();
        }
        return "";
    }

    @Override
    public void run(CommandOptions options) throws Exception {
        File targetFile = new File(target);
        if (!target.endsWith(".apk") || !targetFile.isFile()) {
            throw new RuntimeException("Target file should be an apk file!");
        }

        boolean result;
        if (align) {
            result = AndroidUtils.alignAndSignApk(
                androidBuildToolsVersion, targetFile, new File(getOutputFile()));
        } else {
            result = AndroidUtils.signApk(
                androidBuildToolsVersion, targetFile, new File(getOutputFile()));
        }
        if (!result) {
            logger.error("Can not sign(align) the given apk {}!", target);
        } else {
            logger.info("Sign(align) succeeded! Result file is at {}!", getOutputFile());
        }
    }
}
