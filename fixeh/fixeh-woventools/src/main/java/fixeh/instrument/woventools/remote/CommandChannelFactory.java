package fixeh.instrument.woventools.remote;

import java.nio.channels.SocketChannel;

public interface CommandChannelFactory { CommandChannel create(SocketChannel socketChannel); }
