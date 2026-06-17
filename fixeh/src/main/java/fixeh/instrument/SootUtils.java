package fixeh.instrument;

import soot.SootMethod;

/**
 * Created by Shunjie Ding on 14/03/2018.
 */
public final class SootUtils {
    public static String getMethodSignature(SootMethod method) {
        String signature = method.getSignature();
        return signature.substring(1, signature.length() - 1);
    }
}
