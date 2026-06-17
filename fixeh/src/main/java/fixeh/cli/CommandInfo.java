package fixeh.cli;

import com.beust.jcommander.Parameters;
import org.apache.commons.lang3.SystemUtils;

import java.util.stream.Collectors;
import fixeh.Constants;
import fixeh.util.AndroidUtils;

/**
 * Created by Shunjie Ding on 24/01/2018.
 */
@Parameters(commandNames = "info", commandDescription = "Show detailed information")
public class CommandInfo implements Command {
    private static void println(String fmt, Object... args) {
        System.err.println(String.format(fmt, args));
    }

    @Override
    public String name() {
        return "info";
    }

    private void printJavaDetails() {
        println("JAVA HOME: %s", Constants.JAVA_HOME);
        println("JAVA VERSION: %s", SystemUtils.JAVA_VERSION);
    }

    private void printAndroidDetails() {
        println("ANDROID HOME: %s", Constants.ANDROID_HOME);
        println("AVAILABLE SDK: %s",
            AndroidUtils.availableSDKVersions().stream().collect(Collectors.joining(", ")));
        println("AVAILABLE BUILD TOOL: %s",
            AndroidUtils.availableBuildToolsVersions().stream().collect(Collectors.joining(", ")));
    }

    @Override
    public void run(CommandOptions options) throws Exception {
        printJavaDetails();
        printAndroidDetails();
    }
}
