package fixeh.runner;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shunjie Ding on 26/01/2018.
 */
public abstract class TestRunner<T extends TestResult> {
    private final PrintStream output;
    private final List<T> testResults = new ArrayList<>();

    TestRunner(PrintStream output) {
        this.output = output == null ? System.err : output;
    }

    public final List<T> getTestResults() {
        return testResults;
    }

    public abstract List<String> getTestClasses() throws Exception;

    public abstract TestReport runTest(String testClass, String testMethod) throws Exception;

    public abstract TestReport runTests(String testClass) throws Exception;

    public abstract TestReport runAllTests() throws Exception;
}
