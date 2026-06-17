package fixeh.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import fixeh.scanner.feature.MethodRelatedTestcase;

/**
 * Created by Shunjie Ding on 15/01/2018.
 */
public class TestcaseSet {
    private final List<String> targetMethods;

    private final HashMap<String, List<MethodRelatedTestcase>> testCaseHashMap = new HashMap<>();

    public TestcaseSet(List<String> targetMethods) {
        this.targetMethods = targetMethods;
    }

    private List<MethodRelatedTestcase> getTestCaseList(String revisionId) {
        if (!testCaseHashMap.containsKey(revisionId)) {
            testCaseHashMap.put(revisionId, new ArrayList<>());
        }
        return testCaseHashMap.get(revisionId);
    }

    void addTestcaseList(String revisionId, List<MethodRelatedTestcase> testcaseList) {
        testcaseList.stream()
            .filter(t -> t.getRevisionId().equals(revisionId))
            .forEach(t -> getTestCaseList(revisionId).add(t));
    }

    void addTestcaseList(List<MethodRelatedTestcase> testcaseList) {
        testcaseList.forEach(t -> getTestCaseList(t.getRevisionId()).add(t));
    }

    public List<String> getTargetMethods() {
        return targetMethods;
    }

    public HashMap<String, List<MethodRelatedTestcase>> getTestCaseHashMap() {
        return testCaseHashMap;
    }
}
