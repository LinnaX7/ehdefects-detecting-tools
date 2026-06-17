package fixeh;

import com.android.ddmlib.AndroidDebugBridge;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.UnixStyleUsageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import ch.qos.logback.classic.Level;
import fixeh.cli.Command;
import fixeh.cli.CommandDetect;
import fixeh.cli.CommandInfo;
import fixeh.cli.CommandInstrument;
import fixeh.cli.CommandOptions;
import fixeh.cli.CommandScan;
import fixeh.cli.CommandSign;
import fixeh.cli.CommandTest;
import fixeh.util.LoggerUtils;

/**
 * Created by Shunjie Ding on 19/12/2017.
 */
public final class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static void gracefulShutdown() {
        AndroidDebugBridge.terminate();
    }

    private static void parseCommandsAndRun(String[] args) throws Exception {
        CommandOptions options = new CommandOptions();
        List<Command> commands = Arrays.asList(new CommandScan(), new CommandInstrument(),
            new CommandSign(), new CommandInfo(), new CommandTest(), new CommandDetect());
        JCommander commander = JCommander.newBuilder().addObject(options).build();
        commander.setUsageFormatter(new UnixStyleUsageFormatter());
        commands.forEach(commander::addCommand);

        try {
            commander.parse(args);

            if (options.isHelp() || commander.getParsedCommand() == null) {
                // Print usage and exit
                commander.usage();
                return;
            }

            Constants.setDebugMode(options.isDebug());
            Constants.setCompilerVerbose(options.isCompilerVerbose());

            if (Constants.isDebugMode()) {
                // Set to DEBUG level
                LoggerUtils.setLogLevel(Level.DEBUG);
                logger.debug("Debug mode enabled!");
            } else {
                LoggerUtils.setLogLevel(Level.INFO);
            }

            Optional<Command> commandOptional =
                commands.stream()
                    .filter(c -> c.name().equals(commander.getParsedCommand()))
                    .findFirst();
            if (commandOptional.isPresent()) {
                commandOptional.get().run(options);
            } else {
                logger.warn("Command {} not found!", commander.getParsedCommand());
                // Print usage if command not found
                commander.usage();
            }
        } catch (ParameterException e) {
            logger.error(e.getMessage());
            // Print usage on exception
            Optional<Command> commandOptional =
                commands.stream()
                    .filter(c -> c.name().equals(commander.getParsedCommand()))
                    .findFirst();
            if (commandOptional.isPresent()) {
                commander.usage(commander.getParsedCommand());
            } else {
                e.usage();
            }
        }
    }

    public static void main(String[] args) {
        try {
            parseCommandsAndRun(args);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            gracefulShutdown();
        }
    }
}
