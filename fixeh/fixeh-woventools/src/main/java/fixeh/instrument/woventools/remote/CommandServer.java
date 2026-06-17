package fixeh.instrument.woventools.remote;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CommandServer implements Runnable {
    private ServerSocketChannel serverSocketChannel;

    private ExecutorService executorService = new ThreadPoolExecutor(
        2, 128, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
        });

    private int port = Constants.DEFAULT_SERVER_PORT;

    private CommandChannelFactory channelFactory;

    public CommandServer() {}

    public CommandServer(CommandChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }

    public CommandServer(int port, CommandChannelFactory channelFactory) {
        this.port = port;
        this.channelFactory = channelFactory;
    }

    private void startAndListen() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(true);

        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            executorService.submit(channelFactory.create(socketChannel));
        }
    }

    @Override
    public void run() {
        try {
            startAndListen();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        if (serverSocketChannel != null) {
            try {
                serverSocketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        serverSocketChannel = null;
    }

    public void shutdownNow() {
        if (serverSocketChannel != null) {
            executorService.shutdownNow();
        }
        shutdown();
    }
}
