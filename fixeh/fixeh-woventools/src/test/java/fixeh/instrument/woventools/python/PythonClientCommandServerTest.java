package fixeh.instrument.woventools.python;

import org.junit.Test;

import fixeh.instrument.woventools.InvocationInfo;

/**
 * Created by Shunjie Ding on 2018/5/4.
 */
public class PythonClientCommandServerTest {
    @Test
    public void testServer() {
        PythonClientCommandServer server = new PythonClientCommandServer();
        server.onPass(new InvocationInfo("java.io.FileInputStream: void <init>(java.io.File)",
            "org.apache.commons.io.FileUtils: long copyFile(java.io.File,java.io.OutputStream)",
            "java.io.FileNotFoundException", "org.apache.commons.io.FileUtils:1092"));
        server.onForceThrow(new InvocationInfo("java.io.FileInputStream: void <init>(java.io.File)",
            "org.apache.commons.io.FileUtils: long copyFile(java.io.File,java.io.OutputStream)",
            "java.io.FileNotFoundException", "org.apache.commons.io.FileUtils:1092"));
        server.run();
    }
}
