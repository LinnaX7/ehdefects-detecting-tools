package fixeh.scanner.feature;

import java.util.List;

/**
 * Created by Shunjie Ding on 15/01/2018.
 */
public final class MethodRelatedTestcase {
    private final String revisionId;

    private final String target;

    private final List<Testcase> testcase;

    public MethodRelatedTestcase(String revisionId, String target, List<Testcase> testcase) {
        this.revisionId = revisionId;
        this.target = target;
        this.testcase = testcase;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public String getTarget() {
        return target;
    }

    public List<Testcase> getTestcase() {
        return testcase;
    }
}
