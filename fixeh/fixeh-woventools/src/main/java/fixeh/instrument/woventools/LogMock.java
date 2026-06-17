package fixeh.instrument.woventools;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Shunjie Ding on 22/01/2018.
 */
// Mock android.util.Log
public final class LogMock {
    private static final PrintStream ps = System.out;

    private static final SimpleDateFormat timeFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static String getStackTraceString(Throwable tr) {
        StringWriter writer = new StringWriter();
        tr.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    public static String getFullStackTraceString(Throwable tr){
        String wholeStackString = "";
        for (StackTraceElement element :tr.getStackTrace()){
            wholeStackString += element.toString();
            wholeStackString += '\n';
        }
        return wholeStackString;
    }


    private static String getString(String msg, Throwable tr) {
        String res = msg == null ? "" : msg;
        if (tr != null) {
            if (res.isEmpty()) {
                res = getStackTraceString(tr);
            } else {
                res += "\n" + getStackTraceString(tr);
            }
        }
        return res;
    }

    public static int println(String level, String tag, String msg, Throwable tr) {
        ps.println(
            String.format("[%s] %s %s", timeFormat.format(new Date()), level, getString(msg, tr)));
        return 0;
    }

    public static int d(String tag, String msg, Throwable tr) {
        return println("DEBUG", tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        return d(tag, msg, null);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return println("ERROR", tag, msg, tr);
    }

    public static int e(String tag, String msg) {
        return e(tag, msg, null);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return println("INFO", tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        return i(tag, msg, null);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return println("VERBOSE", tag, msg, tr);
    }

    public static int v(String tag, String msg) {
        return v(tag, msg, null);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return println("WARN", tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        return w(tag, msg, null);
    }

    public static int wtf(String tag, String msg, Throwable tr) {
        return println("WTF", tag, msg, tr);
    }

    public static int wtf(String tag, Throwable tr) {
        return wtf(tag, null, tr);
    }

    public static int wtf(String tag, String msg) {
        return wtf(tag, msg, null);
    }
}
