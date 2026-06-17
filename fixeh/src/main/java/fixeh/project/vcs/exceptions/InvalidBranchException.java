package fixeh.project.vcs.exceptions;

/**
 * Created by Shunjie Ding on 21/12/2017.
 */
public class InvalidBranchException extends Exception {
    public InvalidBranchException() {
        super();
    }

    public InvalidBranchException(String msg) {
        super(msg);
    }

    public InvalidBranchException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public InvalidBranchException(Throwable cause) {
        super(cause);
    }
}
