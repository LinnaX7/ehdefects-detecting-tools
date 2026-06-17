package fixeh.instrument;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import fixeh.instrument.woventools.policy.command.DecisionRequest;
import fixeh.instrument.woventools.remote.CommandChannel;
import fixeh.instrument.woventools.remote.CommandChannelFactory;
import fixeh.instrument.woventools.remote.CommandClient;
import fixeh.instrument.woventools.remote.CommandServer;
import fixeh.instrument.woventools.remote.Notification;
import fixeh.instrument.woventools.remote.Request;

public class RemoteCollectServer extends CommandServer {
    private static final Set<String> resourceRelatedMethods =
        Collections.synchronizedSet(new HashSet<>());

    public RemoteCollectServer() {
        super(new CollectClientFactory());
    }

    public static Set<String> getResourceRelatedMethods() {
        return resourceRelatedMethods;
    }

    private static class CollectClient extends CommandClient {
        private final Set<String> resourceRelatedMethods;

        public CollectClient(SocketChannel socketChannel, Set<String> resourceRelatedMethods) {
            super(socketChannel);
            this.resourceRelatedMethods = resourceRelatedMethods;
        }

        @Override
        public void handleRequest(Request request) {
            if (request instanceof DecisionRequest) {
                resourceRelatedMethods.add(((DecisionRequest) request).getMethodSignature());
                try {
                    writeBoolean(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void handleNotification(Notification notification) {}
    }

    private static class CollectClientFactory implements CommandChannelFactory {
        @Override
        public CommandChannel create(SocketChannel socketChannel) {
            return new CollectClient(socketChannel, resourceRelatedMethods);
        }
    }
}
