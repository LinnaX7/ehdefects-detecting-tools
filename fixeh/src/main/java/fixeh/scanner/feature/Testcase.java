package fixeh.scanner.feature;

/**
 * Created by Shunjie Ding on 15/01/2018.
 */
public class Testcase {
    private final String filename;

    private final String signature;

    public Testcase(String filename, String signature) {
        this.filename = filename;
        this.signature = signature;
    }

    public String getFilename() {
        return filename;
    }

    public String getSignature() {
        return signature;
    }
}