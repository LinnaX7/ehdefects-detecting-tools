package fixeh.runner;

import java.util.List;

/**
 * Created by Shunjie Ding on 31/01/2018.
 */
public final class TestReport {
    private boolean passed;

    private List<AndroidTestResult> results;

    private String errorMessage;

    private long elapsedTime;

    public TestReport(
        boolean passed, List<AndroidTestResult> results, String errorMessage, long elapsedTime) {
        this.passed = passed;
        this.results = results;
        this.errorMessage = errorMessage;
        this.elapsedTime = elapsedTime;
    }

    public int totalTestNum() {
        return results.size();
    }

    public int passedTestNum() {
        return (int) results.stream().filter(TestResult::isPassed).count();
    }

    public List<AndroidTestResult> getResults() {
        return results;
    }

    private String getDetailedResults() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.size(); ++i) {
            sb.append(String.format("(%d) %s", i, results.get(i)));
            if (i != results.size() - 1) {
                sb.append("\n\n");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        int totalNum = totalTestNum();
        int passedNum = passedTestNum();
        return String.format(
            "TEST REPORT:\nTest %s:\n%d tests run, %d passed, %d failed\nElapsed time: %d ms\n"
                + "%s",
            passed ? "PASSED" : "FAILED(" + errorMessage + ")", totalNum, passedNum,
            totalNum - passedNum, elapsedTime, getDetailedResults());
    }
}
