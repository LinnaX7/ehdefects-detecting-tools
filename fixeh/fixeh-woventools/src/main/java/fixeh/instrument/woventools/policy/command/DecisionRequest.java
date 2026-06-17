package fixeh.instrument.woventools.policy.command;

import java.io.PrintWriter;
import java.io.StringWriter;
import fixeh.instrument.woventools.remote.Request;

public class DecisionRequest extends Request {
    private String methodSignature;

    private int callCount;

    private transient Throwable tr;

    private String exceptionClass;

    private String stackTraceString;

    public DecisionRequest(String methodSignature, int callCount, Throwable tr) {
        this.methodSignature = methodSignature;
        this.callCount = callCount;
        this.tr = tr;
        this.exceptionClass = tr.getClass().getName();
        this.stackTraceString = getStackTraceString(tr);
    }

    private static String getStackTraceString(Throwable tr) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        return sw.toString();
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public int getCallCount() {
        return callCount;
    }

    public String getStackTraceString() {
        return stackTraceString;
    }

    @Override
    public String toString() {
        return String.format("Request decision for %s on method %s(%d), stack traces are\n%s",
            exceptionClass, methodSignature, callCount, stackTraceString);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof DecisionRequest) {
            DecisionRequest request = (DecisionRequest) obj;
            return methodSignature.equals(request.methodSignature) && callCount == request.callCount
                && exceptionClass.equals(request.exceptionClass)
                && stackTraceString.equals(request.stackTraceString);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return methodSignature.hashCode() + callCount + exceptionClass.hashCode()
            + stackTraceString.hashCode();
    }
}
