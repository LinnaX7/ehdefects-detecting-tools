package fixeh.project.vcs.exceptions;

/**
 * Created by Shunjie Ding on 19/12/2017.
 */
public final class UnsupportedVcsException extends Exception {
    public UnsupportedVcsException() {
        super();
    }

    public UnsupportedVcsException(String msg) {
        super(msg);
    }

    public UnsupportedVcsException(Throwable cause) {
        super(cause);
    }

    public UnsupportedVcsException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
