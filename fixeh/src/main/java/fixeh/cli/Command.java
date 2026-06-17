package fixeh.cli;

public interface Command {
    String name();

    void run(CommandOptions options) throws Exception;
}
