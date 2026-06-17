package fixeh.runner;

import java.io.PrintStream;
import java.util.List;

/**
 * Created by Shunjie Ding on 26/01/2018.
 */
public class JUnitTestRunner extends TestRunner<JUnitTestResult> {
    // TODO
    protected JUnitTestRunner(PrintStream output) {
        super(output);
    }

    @Override
    public List<String> getTestClasses() throws Exception {
        return null;
    }

    @Override
    public TestReport runTest(String testClass, String testMethod) throws Exception {
        return null;
    }

    @Override
    public TestReport runTests(String testClass) throws Exception {
        return null;
    }

    @Override
    public TestReport runAllTests() throws Exception {
        return null;
    }
}
