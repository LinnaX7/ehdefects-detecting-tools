package fixeh.runner;

/**
 * Created by Shunjie Ding on 26/01/2018.
 */
public class AndroidTestResult extends TestResult {
    @Override
    public String getDetails() {
        String prefix = String.format("%s(%s)", getClassName(), getMethodName());
        if (isPassed()) {
            return prefix + " PASSED!";
        } else {
            String trace = getTrace();
            if (trace == null) {
                return prefix + " FAILED!";
            } else {
                return prefix + " FAILED!"
                    + "\n" + trace;
            }
        }
    }
}
