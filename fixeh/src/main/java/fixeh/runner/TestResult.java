package fixeh.runner;

/**
 * Created by Shunjie Ding on 26/01/2018.
 */
public abstract class TestResult {
    private String className;

    private String methodName;

    private boolean passed;

    private String trace;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getQualifiedTestName() {
        return String.format("%s(%s)", getClassName(), getMethodName());
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public abstract String getDetails();

    public String getTrace() {
        return trace;
    }

    public void setTrace(String trace) {
        this.trace = trace;
    }

    @Override
    public String toString() {
        return getDetails();
    }
}
