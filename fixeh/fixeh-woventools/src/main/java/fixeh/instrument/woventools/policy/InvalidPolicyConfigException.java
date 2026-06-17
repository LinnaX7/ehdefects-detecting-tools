package fixeh.instrument.woventools.policy;

public class InvalidPolicyConfigException extends Exception {
    public InvalidPolicyConfigException(String msg) {
        super(msg);
    }

    public InvalidPolicyConfigException(String msg, Throwable cause) {
        super(msg, cause);
    }
}