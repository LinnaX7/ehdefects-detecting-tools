package fixeh.instrument.woventools.policy;

/**
 * Created by Shunjie Ding on 29/01/2018.
 */
public class ForceControlPolicy implements ControlPolicy {
    @Override
    public boolean takeOver(String method, int count, Throwable tr) {
        return false;
    }

    @Override
    public void onTestChanged(String testMethod) {}

    @Override
    public String toString() {
        return ForceControlPolicy.class.getSimpleName();
    }
}
