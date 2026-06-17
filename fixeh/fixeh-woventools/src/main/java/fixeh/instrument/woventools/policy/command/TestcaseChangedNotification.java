package fixeh.instrument.woventools.policy.command;

import fixeh.instrument.woventools.remote.Notification;

public class TestcaseChangedNotification extends Notification {
    private String method;

    public TestcaseChangedNotification(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
