package fixeh.instrument.woventools.policy;

import java.io.Serializable;

public class ReplayControlPolicy implements ControlPolicy {
    private boolean matched = true;

    public ReplayControlPolicy(ReplayBundle bundle) {}

    @Override
    public String toString() {
        return ReplayControlPolicy.class.getSimpleName();
    }

    // TODO
    @Override
    public boolean takeOver(String method, int count, Throwable tr) {
        return false;
    }

    @Override
    public void onTestChanged(String testMethod) {}

    public boolean isMatched() {
        return matched;
    }

    public static class ReplayBundle implements Serializable {}
}
