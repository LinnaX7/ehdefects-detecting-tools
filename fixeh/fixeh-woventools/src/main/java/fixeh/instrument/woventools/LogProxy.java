package fixeh.instrument.woventools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Shunjie Ding on 22/01/2018.
 */
public class LogProxy implements Log {
    public static final String LOG_TAG = "fixeh";
    private static final String ANDROID_LOG_CLASS = "android.util.Log";
    private static Log instance;
    private final Class<?> clz;
    private final Map<String, Method> methodMap = new HashMap<>();

    LogProxy(String className) throws ClassNotFoundException, NoSuchMethodException {
        clz = Class.forName(className);

        // get methods
        final String[] methodNames = {"d", "e", "i", "v", "w", "wtf"};
        for (String m : methodNames) {
            methodMap.put(m + "ss", clz.getMethod(m, getParamSS()));
            methodMap.put(m + "sst", clz.getMethod(m, getParamSST()));
        }
        methodMap.put("wtfst", clz.getMethod("wtf", getParamST()));
    }

    public static Log getInstance() {
        if (instance == null) {
            try {
                instance = new LogProxy(ANDROID_LOG_CLASS);
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                // Fallback to LogMock to enable tests
                try {
                    instance = new LogProxy(LogMock.class.getName());
                } catch (Exception ex) {
                    // ignore
                }
            }
        }
        return instance;
    }

    private static Class[] getParamSS() {
        return new Class[] {String.class, String.class};
    }

    private static Class[] getParamST() {
        return new Class[] {String.class, Throwable.class};
    }

    private static Class[] getParamSST() {
        return new Class[] {String.class, String.class, Throwable.class};
    }

    @Override
    public int d(String tag, String msg, Throwable tr) {
        try {
            return (int) methodMap.get("dsst").invoke(null, tag, msg, tr);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unexpected error!");
        }
    }

    @Override
    public int d(String tag, String msg) {
        try {
            return (int) methodMap.get("dss").invoke(null, tag, msg);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unexpected error!");
        }
    }

    @Override
    public int e(String tag, String msg, Throwable tr) {
        try {
            return (int) methodMap.get("esst").invoke(null, tag, msg, tr);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unexpected error!");
        }
    }

    @Override
    public int e(String tag, String msg) {
        try {
            return (int) methodMap.get("ess").invoke(null, tag, msg);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unexpected error!");
        }
    }

    @Override
    public int i(String tag, String msg, Throwable tr) {
        try {
            return (int) methodMap.get("isst").invoke(null, tag, msg, tr);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unexpected error!");
        }
    }

    @Override
    public int i(String tag, String msg) {
        try {
            return (int) methodMap.get("iss").invoke(null, tag, msg);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unexpected error!");
        }
    }

    @Override
    public int v(String tag, String msg, Throwable tr) {
        try {
            return (int) methodMap.get("vsst").invoke(null, tag, msg, tr);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unexpected error!");
        }
    }

    @Override
    public int v(String tag, String msg) {
        try {
            return (int) methodMap.get("vss").invoke(null, tag, msg);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unexpected error!");
        }
    }

    @Override
    public int w(String tag, String msg, Throwable tr) {
        try {
            return (int) methodMap.get("wsst").invoke(null, tag, msg, tr);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unexpected error!");
        }
    }

    @Override
    public int w(String tag, String msg) {
        try {
            return (int) methodMap.get("wss").invoke(null, tag, msg);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unexpected error!");
        }
    }

    @Override
    public int wtf(String tag, String msg, Throwable tr) {
        try {
            return (int) methodMap.get("wtfsst").invoke(null, tag, msg, tr);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unexpected error!");
        }
    }

    @Override
    public int wtf(String tag, Throwable tr) {
        try {
            return (int) methodMap.get("wtfst").invoke(null, tag, tr);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unexpected error!");
        }
    }

    @Override
    public int wtf(String tag, String msg) {
        try {
            return (int) methodMap.get("wtfss").invoke(null, tag, msg);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unexpected error!");
        }
    }
}
