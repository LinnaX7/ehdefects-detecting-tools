package fixeh.instrument.woventools.trigger;


import android.os.Environment;
import android.os.FileObserver;
import fixeh.instrument.woventools.Log;
import fixeh.instrument.woventools.LogProxy;

import java.io.File;
import java.util.HashSet;

public class TriggerClientCommandServer {
    private static final Log LOG = LogProxy.getInstance();
    private static FileObserver configFileObserver;
    public static boolean trigger = false;
    public static int count = 0;


    static {
        LOG.i(LogProxy.LOG_TAG, "Enter policy is " + "file trigger");

        try {
            //Path configPath = Paths.get("/data/local/tmp/fixeh-policy.xml");
            //String configPathString = configPath.toFile().getCanonicalPath();
            File file1 = new File(Environment.getDataDirectory(), "local");
            if (file1.isDirectory()) {
                LOG.i(LogProxy.LOG_TAG, "find local dir!");
            }
            File file2 = new File(file1.getAbsolutePath(), "tmp");
            if (file2.isDirectory()) {
                LOG.i(LogProxy.LOG_TAG, "find tmp dir!");
            }
            File file3 = new File(file2.getAbsolutePath(), "triggered.xml");
            if (file3.isFile()) {
                LOG.i(LogProxy.LOG_TAG, "find triggered");
            }
            //String configPathString = "/data/local/tmp/fixeh-policy.xml";
            configFileObserver = new FileObserver(file3.getCanonicalPath(),
                    FileObserver.ATTRIB | FileObserver.CREATE | FileObserver.DELETE
                            | FileObserver.MODIFY) {
                @Override
                public void onEvent(int i, String s) {
                    LOG.i(LogProxy.LOG_TAG, "triggering");
                    trigger = true;
                }
            };
        } catch (Exception e) {
            LOG.e(LogProxy.LOG_TAG, "Could not watch config file due to", e);
        }

    }

    public static void reset() {
        count = 0;
        trigger = true;
    }
}

