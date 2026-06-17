package fixeh.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import fixeh.Constants;

public final class ResourceUtils {
    private static final File debugKeyStore = new File(Constants.TMPDIR, "debug.keystore");

    private static final File fixehPolicyXml = new File(Constants.TMPDIR, "fixeh-policy.xml");

    private static final File runtimeExceptionXls =
        new File(Constants.TMPDIR, "runtime_exceptions.xls");

    private static File getResourceFile(File f, String resourceName) {
        if (!f.exists()) {
            // Copy debug.keystore inside jar to local
            URL inputUrl = ResourceUtils.class.getClassLoader().getResource(resourceName);
            assert inputUrl != null;
            try {
                FileUtils.copyURLToFile(inputUrl, f);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        assert f.isFile();

        return f;
    }

    public static File getDebugKeyStore() {
        return getResourceFile(debugKeyStore, "debug.keystore");
    }

    public static File getFixehPolicyXml() {
        return getResourceFile(fixehPolicyXml, "fixeh-policy.xml");
    }

    public static File getRuntimeExceptionXls() {
        return getResourceFile(runtimeExceptionXls, "runtime_exceptions.xls");
    }
}
