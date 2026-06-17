package fixeh.instrument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import fixeh.instrument.woventools.policy.ReplayControlPolicy;
import fixeh.instrument.woventools.policy.command.DecisionRequest;
import fixeh.instrument.woventools.policy.command.ReplayRequest;
import fixeh.instrument.woventools.policy.command.TestcaseChangedNotification;
import fixeh.instrument.woventools.remote.CommandChannel;
import fixeh.instrument.woventools.remote.CommandChannelFactory;
import fixeh.instrument.woventools.remote.CommandClient;
import fixeh.instrument.woventools.remote.CommandServer;
import fixeh.instrument.woventools.remote.Notification;
import fixeh.instrument.woventools.remote.Request;

public class RemotePolicyController extends CommandServer {
    public RemotePolicyController() {
        super(new DynamicControlPolicyCommandChannelFactory());
    }

    public static class DynamicControlPolicyCommandChannel extends CommandClient {
        private final Logger logger =
            LoggerFactory.getLogger(DynamicControlPolicyCommandChannel.class);

        public DynamicControlPolicyCommandChannel(SocketChannel socketChannel) {
            super(socketChannel);
        }

        private void handleDecisionRequest(DecisionRequest request) {
            logger.info("Receiving remote decision command: {}", request);
            // FIXME Should be controlled
            logger.info("Responding with automatic false");
            try {
                writeBoolean(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleTestcaseChangedNotification(TestcaseChangedNotification notification) {
            logger.info("Test case changed, currently in test {}", notification.getMethod());
            // FIXME
        }

        private void handleReplayRequest(ReplayRequest replayRequest) {
            try {
                // TODO
                writeObject(new ReplayControlPolicy.ReplayBundle());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void handleRequest(Request request) {
            if (request instanceof DecisionRequest) {
                handleDecisionRequest((DecisionRequest) request);
            } else if (request instanceof ReplayRequest) {
                handleReplayRequest((ReplayRequest) request);
            } else {
                throw new RuntimeException(
                    "Unsupported command type " + request.getClass().getName());
            }
        }

        @Override
        public void handleNotification(Notification notification) {
            if (notification instanceof TestcaseChangedNotification) {
                handleTestcaseChangedNotification((TestcaseChangedNotification) notification);
            } else {
                throw new RuntimeException(
                    "Unsupported notification type" + notification.getClass().getName());
            }
        }
    }

    public static class DynamicControlPolicyCommandChannelFactory implements CommandChannelFactory {
        @Override
        public CommandChannel create(SocketChannel socketChannel) {
            return new DynamicControlPolicyCommandChannel(socketChannel);
        }
    }
}
