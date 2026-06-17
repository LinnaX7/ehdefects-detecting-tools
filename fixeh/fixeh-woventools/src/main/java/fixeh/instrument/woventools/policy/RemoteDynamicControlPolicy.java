package fixeh.instrument.woventools.policy;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import fixeh.instrument.woventools.Log;
import fixeh.instrument.woventools.LogProxy;
import fixeh.instrument.woventools.policy.command.DecisionRequest;
import fixeh.instrument.woventools.policy.command.ReplayRequest;
import fixeh.instrument.woventools.policy.command.TestcaseChangedNotification;
import fixeh.instrument.woventools.remote.Command;
import fixeh.instrument.woventools.remote.CommandClient;
import fixeh.instrument.woventools.remote.Notification;
import fixeh.instrument.woventools.remote.Request;

/**
 * Created by Shunjie Ding on 29/01/2018.
 */
public class RemoteDynamicControlPolicy implements RemoteControlPolicy {
    private static final Log LOG = LogProxy.getInstance();
    String host;
    int port;
    private LinkedBlockingDeque<Boolean> resultQueue = new LinkedBlockingDeque<>();
    private ControlPolicy defaultPolicy;
    // TODO
    private ReplayControlPolicy replayControlPolicy;
    private LinkedBlockingDeque<Command> sendingQueue = new LinkedBlockingDeque<>();

    public RemoteDynamicControlPolicy(String host, int port, ControlPolicy defaultPolicy)
        throws IOException {
        this.host = host;
        this.port = port;

        this.defaultPolicy = defaultPolicy;

        CommandClient commandClient = startCommandClient(host, port);
        startCommandSender(commandClient, sendingQueue);

        // send replay command
        try {
            send(new ReplayRequest());
        } catch (InterruptedException e) {
            // ignore
        }
    }

    @Override
    public String toString() {
        return String.format("RemoteDynamicControlPolicy{host: %s, port: %d}", host, port);
    }

    private CommandClient startCommandClient(String host, int port) throws IOException {
        CommandClient commandClient = new PolicyCommandClient(host, port, resultQueue);
        Thread t = new Thread(commandClient);
        t.setDaemon(true);
        t.start();

        // Sleep for 1s to wait connection
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // ignore
        }

        return commandClient;
    }

    private void startCommandSender(
        final CommandClient commandClient, final BlockingQueue<Command> sendingQueue) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Command command = sendingQueue.take();
                        commandClient.writeObject(command);
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        });

        t.setDaemon(true);
        t.start();
    }

    private void send(Command command) throws InterruptedException {
        sendingQueue.put(command);
    }

    private boolean getResult() throws InterruptedException {
        return resultQueue.take();
    }

    @Override
    public boolean takeOver(String method, int count, Throwable tr) {
        if (replayControlPolicy != null && replayControlPolicy.isMatched()) {
            return replayControlPolicy.takeOver(method, count, tr);
        }

        if (defaultPolicy != null && defaultPolicy.takeOver(method, count, tr)) {
            try {
                // send command and wait for result
                send(new DecisionRequest(method, count, tr));
                return getResult();
            } catch (InterruptedException e) {
                LOG.e(LogProxy.LOG_TAG, "[Policy] Give up taking over due to an exception!", e);
                return false;
            }
        }
        return false;
    }

    @Override
    public void onTestChanged(String testMethod) {
        try {
            send(new TestcaseChangedNotification(testMethod));
        } catch (InterruptedException e) {
            // ignore
        }
    }

    static class PolicyCommandClient extends CommandClient {
        private LinkedBlockingDeque<Boolean> resultQueue;

        PolicyCommandClient(String host, int port, LinkedBlockingDeque<Boolean> resultQueue)
            throws IOException {
            super(host, port);
            this.resultQueue = resultQueue;
        }

        @Override
        public void handleRequest(Request request) {}

        @Override
        public void handleNotification(Notification notification) {}

        @Override
        public void handleBoolean(boolean b) {
            try {
                resultQueue.put(b);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}
