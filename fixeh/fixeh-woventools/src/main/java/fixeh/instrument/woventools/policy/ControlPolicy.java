package fixeh.instrument.woventools.policy;

/**
 * Created by Shunjie Ding on 29/01/2018.
 */
public interface ControlPolicy {
    boolean takeOver(String method, int count, Throwable tr);

    void onTestChanged(String testMethod);
}