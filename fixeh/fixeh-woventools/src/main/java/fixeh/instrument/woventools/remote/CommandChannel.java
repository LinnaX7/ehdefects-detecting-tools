package fixeh.instrument.woventools.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class CommandChannel implements Runnable {
    private static final Set<Byte> KNOWN_COMMAND_CODES = new HashSet<>();

    static {
        KNOWN_COMMAND_CODES.addAll(Arrays.asList(CommandCodes.NULL, CommandCodes.OBJECT,
            CommandCodes.BOOLEAN, CommandCodes.STRING, CommandCodes.CLOSE));
    }

    private final ByteBuffer readByteBuffer = ByteBuffer.allocate(Constants.READ_BUFFER_SIZE);
    private final ByteBuffer writeByteBuffer = ByteBuffer.allocate(Constants.WRITE_BUFFER_SIZE);
    private SocketChannel socketChannel;

    protected CommandChannel() {}

    public CommandChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    private void writeToSocket(byte code, byte... content) throws IOException {
        writeByteBuffer.put(code);
        if (content.length > 0) {
            writeByteBuffer.putInt(content.length);
            writeByteBuffer.put(content);
        }
        writeByteBuffer.flip();
        while (writeByteBuffer.hasRemaining()) {
            socketChannel.write(writeByteBuffer);
        }
        writeByteBuffer.clear();
    }

    private void writeNull(byte originCode) throws IOException {
        writeToSocket(CommandCodes.NULL, originCode);
    }

    private byte[] getObjectSerializedBytes(Serializable object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            outputStream.writeObject(object);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private Object getObjectFromSerializedBytes(byte[] bytes)
        throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try (ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream)) {
            return inputStream.readObject();
        }
    }

    public void writeObject(Serializable object) throws IOException {
        if (object == null) {
            // even if object is null
            writeNull(CommandCodes.OBJECT);
            return;
        }
        writeToSocket(CommandCodes.OBJECT, getObjectSerializedBytes(object));
    }

    public void close() throws IOException {
        writeToSocket(CommandCodes.CLOSE);
    }

    private void readFromSocket() throws IOException {
        while (readByteBuffer.hasRemaining()) {
            if (socketChannel.read(readByteBuffer) == -1) {
                throw new EOFException();
            }
        }
    }

    private byte readByte() throws IOException {
        readByteBuffer.limit(1);
        readFromSocket();
        readByteBuffer.flip();
        byte res = readByteBuffer.get();
        readByteBuffer.clear();
        return res;
    }

    private int readInt() throws IOException {
        readByteBuffer.limit(4);
        readFromSocket();
        readByteBuffer.flip();
        int res = readByteBuffer.getInt();
        readByteBuffer.clear();
        return res;
    }

    private byte[] readContent(int length) throws IOException {
        readByteBuffer.limit(length);
        readFromSocket();
        readByteBuffer.flip();
        byte[] res = new byte[length];
        readByteBuffer.get(res);
        readByteBuffer.clear();
        return res;
    }

    public abstract void handleString(String s);

    public abstract void handleObject(Object o);

    public abstract void handleBoolean(boolean b);

    private void listenAndHandle() throws IOException {
        try {
            while (true) {
                byte code = readByte();
                if (!KNOWN_COMMAND_CODES.contains(code)) {
                    throw new RuntimeException("Unrecognized command code " + code
                        + "! Shutdown because we do now known how to deal with messed messages");
                }

                if (code == CommandCodes.CLOSE) {
                    // exit listen on close
                    return;
                }

                int length = readInt();
                byte[] content = readContent(length);

                if (code == CommandCodes.NULL) {
                    assert length == 1;
                    if (content[0] == CommandCodes.STRING) {
                        handleString(null);
                    } else if (content[0] == CommandCodes.OBJECT) {
                        handleObject(null);
                    }
                } else if (code == CommandCodes.OBJECT) {
                    handleObject(getObjectFromSerializedBytes(content));
                } else if (code == CommandCodes.STRING) {
                    handleString(new String(content));
                } else if (code == CommandCodes.BOOLEAN) {
                    handleBoolean(content[0] == 1);
                }
            }
        } catch (EOFException e) {
            // exit loop
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            listenAndHandle();

            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void writeString(String s) throws IOException {
        if (s == null) {
            writeNull(CommandCodes.STRING);
            return;
        }
        writeToSocket(CommandCodes.STRING, s.getBytes());
    }

    public void writeBoolean(boolean b) throws IOException {
        writeToSocket(CommandCodes.BOOLEAN, (byte) (b ? 1 : 0));
    }

    private static class CommandCodes {
        private static final byte NULL = 0x7f;
        private static final byte CLOSE = 0x0;
        private static final byte OBJECT = 0x1;
        private static final byte STRING = 0x2;
        private static final byte BOOLEAN = 0x3;
    }
}
