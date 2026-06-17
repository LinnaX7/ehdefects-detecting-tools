package fixeh.instrument.woventools.remote;

import java.nio.channels.SocketChannel;

public class EmptyCommandChannel extends CommandChannel {
    public EmptyCommandChannel(SocketChannel socketChannel) {
        super(socketChannel);
    }

    @Override
    public void handleString(String s) {}

    @Override
    public void handleObject(Object o) {}

    @Override
    public void handleBoolean(boolean b) {}
}
