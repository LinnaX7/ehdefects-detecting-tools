package fixeh.instrument.woventools.policy;

import java.util.Random;

/**
 * Created by Shunjie Ding on 29/01/2018.
 */
public class RandomControlPolicy implements ControlPolicy {
    @Override
    public boolean takeOver(String method, int count, Throwable tr) {
        // Random policy
        return new Random().nextBoolean();
    }

    @Override
    public void onTestChanged(String testMethod) {}

    @Override
    public String toString() {
        return RandomControlPolicy.class.getSimpleName();
    }
}
