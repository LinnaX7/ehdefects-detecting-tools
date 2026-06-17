package fixeh.instrument.woventools.python;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

import fixeh.instrument.woventools.InvocationInfo;
import fixeh.instrument.woventools.Log;
import fixeh.instrument.woventools.LogProxy;
import fixeh.instrument.woventools.policy.GeneralControlPolicy;
import fixeh.instrument.woventools.remote.Constants;

/**
 * Created by Shunjie Ding on 2018/5/4.
 */
public class PythonClientCommandServer implements Runnable {
    private static final Log LOG = LogProxy.getInstance();

    private static final int COMMAND_DISABLE = 0x0;
    private static final int COMMAND_ENABLE = 0x1;
    private static final int COMMAND_ADD_METHOD = 0x2;
    private static final int COMMAND_DEL_METHOD = 0x3;
    private static final int COMMAND_ADD_CLASS = 0x4;
    private static final int COMMAND_DEL_CLASS = 0x5;
    private static final int COMMAND_ADD_PACKAGE = 0x6;
    private static final int COMMAND_DEL_PACKAGE = 0x7;
    private static final int COMMAND_SET_LIMIT = 0x8;
    private static final int COMMAND_SET_INCLUDE = 0x9;
    private static final int COMMAND_SET_EXCLUDE = 0x10;
    private static final int COMMAND_GET_STATS_FORCE_THROWN = 0x11;
    private static final int COMMAND_GET_STATS_PASSED = 0x12;
    private static final int COMMAND_RESET = 0xffff;
    private static class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            LOG.i(LogProxy.LOG_TAG, "i am alive!");
        }
    }
    private static MyTimerTask myTimerTask = new MyTimerTask();
    private static Timer mytimer = new Timer();

    private static final HashSet<Integer> KNOWN_COMMAND_CODES = new HashSet<>();

    private static final int DEFAULT_PORT = 8765;

    static {
        KNOWN_COMMAND_CODES.addAll(Arrays.asList(COMMAND_DISABLE, COMMAND_ENABLE, COMMAND_ADD_CLASS,
            COMMAND_ADD_METHOD, COMMAND_ADD_PACKAGE, COMMAND_DEL_CLASS, COMMAND_DEL_METHOD,
            COMMAND_DEL_PACKAGE, COMMAND_RESET, COMMAND_SET_EXCLUDE, COMMAND_SET_INCLUDE,
            COMMAND_SET_LIMIT, COMMAND_GET_STATS_FORCE_THROWN, COMMAND_GET_STATS_PASSED));
        LOG.i(LogProxy.LOG_TAG, "begin!!!!");
        mytimer.schedule(myTimerTask, 1000, 3 * 1000);
        LOG.i(LogProxy.LOG_TAG, "end!!!!");
    }

    private final GeneralControlPolicy controlPolicy;
    private final int port;
    private final ByteBuffer writeByteBuffer = ByteBuffer.allocate(Constants.WRITE_BUFFER_SIZE);
    private final ByteBuffer readByteBuffer = ByteBuffer.allocate(Constants.READ_BUFFER_SIZE);
    private ActionHandler actionHandler = new DefaultActionHandler();

    public PythonClientCommandServer() {
        this(DEFAULT_PORT);
    }

    public PythonClientCommandServer(int port) {
        if (port <= 1024) {
            throw new IllegalArgumentException("Please use port greater than 1024!");
        }
        this.controlPolicy = new GeneralControlPolicy();
        this.port = port;
    }

    private static boolean isUnknownCommand(int code) {
        return !KNOWN_COMMAND_CODES.contains(code);
    }

    private void readFromSocket(SocketChannel socketChannel) throws IOException {
        while (readByteBuffer.hasRemaining()) {
            if (socketChannel.read(readByteBuffer) == -1) {
                throw new EOFException();
            }
        }
    }

    private byte readByte(SocketChannel socketChannel) throws IOException {
        readByteBuffer.limit(1);
        readFromSocket(socketChannel);
        readByteBuffer.flip();
        byte res = readByteBuffer.get();
        readByteBuffer.clear();
        return res;
    }

    private int readInt(SocketChannel socketChannel) throws IOException {
        readByteBuffer.limit(4);
        readFromSocket(socketChannel);
        readByteBuffer.flip();
        int res = readByteBuffer.getInt();
        readByteBuffer.clear();
        return res;
    }

    private void writeByte(SocketChannel socketChannel, byte b) throws IOException {
        writeByteBuffer.put(b);
        writeByteBuffer.flip();
        socketChannel.write(writeByteBuffer);
        writeByteBuffer.clear();
    }

    private void writeInt(SocketChannel socketChannel, int i) throws IOException {
        writeByteBuffer.putInt(i);
        writeByteBuffer.flip();
        while (writeByteBuffer.hasRemaining()) {
            socketChannel.write(writeByteBuffer);
        }
        writeByteBuffer.clear();
    }

    private void writeBytes(SocketChannel socketChannel, byte[] bytes) throws IOException {
        if (bytes == null) {
            return;
        }
        int offset = 0;
        while (offset < bytes.length) {
            int length = Math.min(bytes.length - offset, writeByteBuffer.remaining());
            writeByteBuffer.put(bytes, offset, length);
            writeByteBuffer.flip();
            while (writeByteBuffer.hasRemaining()) {
                socketChannel.write(writeByteBuffer);
            }
            writeByteBuffer.clear();
            offset += length;
        }
    }

    private byte[] readContent(SocketChannel socketChannel, int length) throws IOException {
        readByteBuffer.limit(length);
        readFromSocket(socketChannel);
        readByteBuffer.flip();
        byte[] res = new byte[length];
        readByteBuffer.get(res);
        readByteBuffer.clear();
        return res;
    }

    public void setActionHandler(ActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

    public GeneralControlPolicy getControlPolicy() {
        return controlPolicy;
    }

    private final HashSet<InvocationInfo> forceThrown = new HashSet<>();
    private final HashSet<InvocationInfo> passed = new HashSet<>();

    public void onForceThrow(InvocationInfo info) {
        synchronized (forceThrown) {
            forceThrown.add(info);
        }
    }

    public void onPass(InvocationInfo info) {
        synchronized (passed) {
            passed.add(info);
        }
    }

    public HashSet<InvocationInfo> getForceThrown() {
        synchronized (forceThrown) {
            return new HashSet<>(forceThrown);
        }
    }

    public HashSet<InvocationInfo> getPassed() {
        synchronized (passed) {
            return new HashSet<>(passed);
        }
    }

    private void writeSuccess(SocketChannel socketChannel) throws IOException {
        writeByte(socketChannel, (byte) '1');
    }

    private void writeFailed(SocketChannel socketChannel) throws IOException {
        writeByte(socketChannel, (byte) '0');
    }

    private byte[] writeValuesAsBytes(Collection objs) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write('[');
        Iterator it = objs.iterator();
        while (it.hasNext()) {
            stream.write(writeValueAsBytes(it.next()));
            if (it.hasNext())
                stream.write(',');
        }
        stream.write(']');
        return stream.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private byte[] writeMapAsBytes(Map map) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write('{');
        Iterator<Map.Entry> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = it.next();
            stream.write(e.getKey().toString().getBytes());
            stream.write(':');
            stream.write(writeValueAsBytes(e.getValue()));
            if (it.hasNext())
                stream.write(',');
        }
        stream.write('}');
        return stream.toByteArray();
    }

    private byte[] writeValueAsBytes(Object obj) throws IOException {
        if (obj instanceof Collection) {
            return writeValuesAsBytes((Collection) obj);
        } else if (obj instanceof Map) {
            return writeMapAsBytes((Map) obj);
        } else {
            return obj.toString().getBytes();
        }
    }

    private void writeObject(SocketChannel socketChannel, Object obj) throws IOException {
        byte[] bytes = null;
        try {
            bytes = writeValueAsBytes(obj);
        } catch (IOException e) {
            // ignore
        }
        if (bytes != null) {
            writeInt(socketChannel, bytes.length);
            writeBytes(socketChannel, bytes);
        } else {
            writeInt(socketChannel, 0);
        }
    }

    private void listenOnSocket(SocketChannel socketChannel) {
        if (socketChannel == null) {
            return;
        }

        try {
        OUTER_LOOP:
            while (true) {
                int code = readInt(socketChannel);
                if (isUnknownCommand(code)) {
                    LOG.e(LogProxy.LOG_TAG,
                        "Unknown code " + code + ", could not continue, so exiting!");
                    break;
                }

                switch (code) {
                    case COMMAND_DISABLE: {
                        controlPolicy.setLimit(0);
                        actionHandler.onDisable();
                        writeSuccess(socketChannel);
                    } break;
                    case COMMAND_ENABLE: {
                        controlPolicy.setLimit(-1);
                        actionHandler.onEnable();
                        writeSuccess(socketChannel);
                    } break;
                    case COMMAND_RESET: {
                        controlPolicy.reset();
                        actionHandler.onReset();
                        writeSuccess(socketChannel);
                    } break;
                    case COMMAND_ADD_METHOD: {
                        int length = readInt(socketChannel);
                        String str = new String(readContent(socketChannel, length));
                        if (str.contains(",")) {
                            String[] slice = str.split(",");
                            if (slice.length > 2) {
                                LOG.e(LogProxy.LOG_TAG,
                                    "Could not read \"method,pattern\" from content, more than 2 comma found! Exiting!");
                                break OUTER_LOOP;
                            }
                            controlPolicy.addMethod(slice[0]);
                            controlPolicy.setMethodPattern(slice[0], slice[1]);
                        } else {
                            controlPolicy.addMethod(str);
                        }
                        writeSuccess(socketChannel);
                    } break;
                    case COMMAND_DEL_METHOD: {
                        int length = readInt(socketChannel);
                        String str = new String(readContent(socketChannel, length));
                        controlPolicy.removeMethod(str);
                        writeSuccess(socketChannel);
                    } break;
                    case COMMAND_SET_EXCLUDE: {
                        controlPolicy.setExclude(true);
                        writeSuccess(socketChannel);
                    } break;
                    case COMMAND_SET_INCLUDE: {
                        controlPolicy.setExclude(false);
                        writeSuccess(socketChannel);
                    } break;
                    case COMMAND_SET_LIMIT: {
                        int limit = readInt(socketChannel);
                        controlPolicy.setLimit(limit);
                        writeSuccess(socketChannel);
                    } break;
                    case COMMAND_GET_STATS_FORCE_THROWN: {
                        HashSet<InvocationInfo> stats = getForceThrown();
                        writeSuccess(socketChannel);
                        writeObject(socketChannel, stats);
                    } break;
                    case COMMAND_GET_STATS_PASSED: {
                        HashSet<InvocationInfo> stats = getPassed();
                        writeSuccess(socketChannel);
                        writeObject(socketChannel, stats);
                    } break;
                    default:
                        LOG.e(LogProxy.LOG_TAG, "Unsupported commands, exiting!");
                        break OUTER_LOOP;
                }
            }
        } catch (IOException e) {
            LOG.e(
                LogProxy.LOG_TAG, "Could not read/write bytes to this socket, stop listening!", e);
        } finally {
            try {
                socketChannel.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                LOG.i(LogProxy.LOG_TAG, "Start listening on port " + port);
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.bind(new InetSocketAddress(port));
                serverSocketChannel.configureBlocking(true);
                SocketChannel socketChannel = serverSocketChannel.accept();
                serverSocketChannel.close();
                LOG.i(LogProxy.LOG_TAG, "Accept a client and serving!");

                listenOnSocket(socketChannel);
            }
        } catch (IOException e) {
            LOG.e(LogProxy.LOG_TAG,
                "Could not open server socket channel, exiting python command server!", e);
        } catch (Exception e) {
            LOG.e(LogProxy.LOG_TAG, "something is wroing!");
        }
    }

    public interface ActionHandler {
        void onEnable();

        void onDisable();

        void onReset();
    }

    private static class DefaultActionHandler implements ActionHandler {
        @Override
        public void onEnable() {}

        @Override
        public void onDisable() {}

        @Override
        public void onReset() {}
    }
}
