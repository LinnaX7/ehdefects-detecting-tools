package fixeh.cli;

import com.beust.jcommander.Parameter;

public class CommandOptions {
    @Parameter(names = {"-v", "--verbose"}, description = "Verbose level")
    private int verbose = 0;

    @Parameter(
        names = {"--compiler-verbose"}, description = "Enable verbose mode for Spoon compiler")
    private boolean compilerVerbose = false;

    @Parameter(names = {"-D", "--debug"}, description = "Debug mode")
    private boolean debug;

    @Parameter(names = {"-h", "--help"}, help = true)
    private boolean help;

    public boolean isDebug() {
        return debug;
    }

    public int getVerbose() {
        return verbose;
    }

    public boolean isCompilerVerbose() {
        return compilerVerbose;
    }

    public boolean isHelp() {
        return help;
    }
}
