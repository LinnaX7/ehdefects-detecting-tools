package fixeh.project.vcs.exceptions;

/**
 * Created by Shunjie Ding on 21/12/2017.
 */
public class InvalidRevisionException extends Exception {
    public InvalidRevisionException() {
        super();
    }

    public InvalidRevisionException(String msg) {
        super(msg);
    }

    public InvalidRevisionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public InvalidRevisionException(Throwable cause) {
        super(cause);
    }
}
