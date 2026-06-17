package fixeh.instrument.woventools.remote;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public abstract class CommandClient extends CommandChannel {
    public CommandClient(String host, int port) throws IOException {
        super();
        setSocketChannel(connect(host, port));
    }

    public CommandClient(SocketChannel socketChannel) {
        super(socketChannel);
    }

    private SocketChannel connect(String host, int port) throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
        socketChannel.configureBlocking(true);
        return socketChannel;
    }

    public abstract void handleRequest(Request request);

    public abstract void handleNotification(Notification notification);

    public void handleCommand(Command command) {
        if (command instanceof Request) {
            handleRequest((Request) command);
        } else if (command instanceof Notification) {
            handleNotification((Notification) command);
        } else {
            throw new RuntimeException("Unsupported command type: " + command.getClass().getName());
        }
    }

    @Override
    public void handleBoolean(boolean b) {}

    @Override
    public void handleString(String s) {}

    @Override
    public final void handleObject(Object o) {
        if (o == null || !(o instanceof Command)) {
            // ignore
            return;
        }
        handleCommand((Command) o);
    }
}
