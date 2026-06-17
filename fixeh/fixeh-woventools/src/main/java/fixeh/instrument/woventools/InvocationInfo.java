package fixeh.instrument.woventools;

import java.util.Objects;

/**
 * Created by Shunjie Ding on 2018/5/23.
 */
public final class InvocationInfo {
    private final String callee;
    private final String caller;
    private final String location;
    private final String exception;

    private int hash = 0;

    public InvocationInfo(String callee, String caller, String location, String exception) {
        this.callee = callee;
        this.caller = caller;
        this.location = location;
        this.exception = exception;
    }

    public String getCallee() {
        return callee;
    }

    public String getCaller() {
        return caller;
    }

    public String getLocation() {
        return location;
    }

    public String getException() {
        return exception;
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Objects.hash(callee, caller, location, exception);
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InvocationInfo)) {
            return false;
        }
        InvocationInfo other = (InvocationInfo) obj;
        return Objects.equals(callee, other.callee) && Objects.equals(caller, other.caller)
            && Objects.equals(location, other.location)
            && Objects.equals(exception, other.exception);
    }

    @Override
    public String toString() {
        return String.format(
            "{\"caller\": \"%s\", \"callee\": \"%s\", \"location\": \"%s\", \"exception\": \"%s\"}",
            getCaller(), getCallee(), getLocation(), getException());
    }
}
