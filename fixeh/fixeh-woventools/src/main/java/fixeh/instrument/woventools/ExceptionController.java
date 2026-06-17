package fixeh.instrument.woventools;

import android.os.Environment;
import android.os.FileObserver;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import fixeh.instrument.woventools.policy.*;

/**
 * Created by Shunjie Ding on 22/01/2018.
 */
public final class   ExceptionController {
    private static final Log LOG = LogProxy.getInstance();

    // private static final InvocationCounter INVOCATION_COUNTER = new InvocationCounter();
    private static final Counter INVOCATION_COUNTER = new newCounter();
    private static final Class JUNIT_TEST_ANNOTATION_CLASS = getJUnitTestAnnotationClass();
    private static FileObserver configFileObserver;
    // private static final PythonClientCommandServer PYTHON_CLIENT_COMMAND_SERVER=new
    // PythonClientCommandServer(); private static final ControlPolicy
    // CONTROL_POLICY=PYTHON_CLIENT_COMMAND_SERVER.getControlPolicy();
    private static ControlPolicy CONTROL_POLICY = newControlPolicyFactory.autoDetect();
    private static String currentTestMethod;


    static {
        LOG.i(LogProxy.LOG_TAG, "Enter policy is " + CONTROL_POLICY.toString());

        try {
            //Path configPath = Paths.get("/data/local/tmp/fixeh-policy.xml");
            //String configPathString = configPath.toFile().getCanonicalPath();
            File file1= new File(Environment.getDataDirectory(),"local");
            /*if (file1.isDirectory()){
                LOG.i(LogProxy.LOG_TAG, "find local dir!");
            }*/
            File file2 = new File(file1.getAbsolutePath(),"tmp");
            /*if (file2.isDirectory()) {
                LOG.i(LogProxy.LOG_TAG, "FIND TMP DIR!");
            }*/
            File file3 = new File(file2.getAbsolutePath(),"fixeh-policy.xml");
            if (file3.isFile()){
                LOG.i(LogProxy.LOG_TAG, "FIND POLICY FILE!!");
            }
            //String configPathString = "/data/local/tmp/fixeh-policy.xml";
            configFileObserver = new FileObserver(file3.getCanonicalPath(),
                FileObserver.ATTRIB | FileObserver.CREATE | FileObserver.DELETE
                    | FileObserver.MODIFY) {
                @Override
                public void onEvent(int i, String s) {
                    LOG.i(LogProxy.LOG_TAG, "Event " + i + " on file " + s);
                    reset();
                }
            };
            LOG.i(LogProxy.LOG_TAG, "Start watching config file " + file3.getAbsolutePath());
            configFileObserver.startWatching();
        } catch (Exception e) {
            LOG.e(LogProxy.LOG_TAG, "Could not watch config file due to", e);
        }
        /*PYTHON_CLIENT_COMMAND_SERVER.setActionHandler(
            new PythonClientCommandServer.ActionHandler() {
                @Override
                public synchronized void onEnable() {}

                @Override
                public synchronized void onDisable() {
                    INVOCATION_COUNTER.reset();
                }

                @Override
                public synchronized void onReset() {
                    INVOCATION_COUNTER.reset();
                }
            });
        // Start python client command server
        Executors.newSingleThreadExecutor().submit(PYTHON_CLIENT_COMMAND_SERVER);*/
    }

    private synchronized static void reset() {
        INVOCATION_COUNTER.reset();
        CONTROL_POLICY = ControlPolicyFactory.autoDetect();
        currentTestMethod = null;

        LOG.i(LogProxy.LOG_TAG, "Exception controller has been reset.");
        LOG.i(LogProxy.LOG_TAG, "Enter policy is " + CONTROL_POLICY.toString());
    }

    private static Class getJUnitTestAnnotationClass() {
        try {
            return Class.forName("org.junit.Test");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static boolean inTestMethod() {
        if (JUNIT_TEST_ANNOTATION_CLASS == null) {
            return false;
        }

        return currentTestMethod != null;
    }

    private static String getParamTypes(Class[] paramClasses) {
        if (paramClasses.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(paramClasses[0].getName());
        for (int i = 1; i < paramClasses.length; ++i) {
            sb.append(", ").append(paramClasses[i].getName());
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static String getCurrentTestMethod(StackTraceElement[] stackTraceElements) {
        if (JUNIT_TEST_ANNOTATION_CLASS == null) {
            return null;
        }

        boolean espressoDetected = false;

        for (StackTraceElement st : stackTraceElements) {
            try {
                Class clz = Class.forName(st.getClassName());
                // If current stack contains methods in android.support.test.espresso, return
                // Espresso
                if (clz.getPackage().getName().startsWith("android.support.test.espresso")) {
                    espressoDetected = true;
                }

                Method method = clz.getMethod(st.getMethodName());
                Annotation[] annotations = method.getAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType().equals(JUNIT_TEST_ANNOTATION_CLASS)) {
                        return String.format("%s: %s(%s)", clz.getName(), method.getName(),
                            getParamTypes(method.getParameterTypes()));
                    }
                }
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                // ignore;
            }
        }

        if (espressoDetected) {
            return "Espresso";
        }

        return null;
    }

    /* private static void setCurrentTestMethod(Throwable tr) {
         String testMethod = getCurrentTestMethod(tr.getStackTrace());

         // If test method changed, notify control policy
         if (testMethod != null && !testMethod.equals(currentTestMethod)) {
             CONTROL_POLICY.onTestChanged(testMethod);
             INVOCATION_COUNTER.reset();
         }

         currentTestMethod = testMethod;
     }

     public static void enterTestMethod(String testMethod) {
         currentTestMethod = testMethod;
         INVOCATION_COUNTER.reset();
         CONTROL_POLICY.onTestChanged(testMethod);
     }
 */
    public static synchronized void enter(String method, String caller, String location,
        Throwable tr, boolean forceControl) throws Throwable {
        /* Deprecated due to unit tests using espresso can not be easily detected.
         */
        // setCurrentTestMethod(tr);
        // int count = 0;
        // if (currentTestMethod != null && currentTestMethod.equals("Espresso")) {
        //     LOG.w(LogProxy.LOG_TAG,
        //         "Detect espresso stack, count may be invalid (greater than expected)!");
        // }

        int count = INVOCATION_COUNTER.increase(method);
        //int exceptionMethodCount = INVOCATION_COUNTER.exceptionMethodIncrease(
               // tr.getClass().getName(),method);


        LOG.d(LogProxy.LOG_TAG, "current stack is " + LogMock.getStackTraceString(tr));

        /*LOG.i(LogProxy.LOG_TAG,
            "XMLCONTROLLER::entering(" + count + ") " + method + " with exception "
                + tr.getClass().getName() + " and such exception entering(" + exceptioncount + ")"
                + "and in such method entering(" + exceptionMethodCount + "）");*/

        //InvocationInfo invocationInfo = new InvocationInfo(method, caller, location, tr.getClass().getName());
        if (CONTROL_POLICY instanceof GeneralControlPolicy) {
            int limitation = ((GeneralControlPolicy) CONTROL_POLICY).getStackMaxrepeat(method);
            if(((GeneralControlPolicy) CONTROL_POLICY).isFilter(method,tr)){
                LOG.i(LogProxy.LOG_TAG,
                        "XMLCONTROLLER::entering" + method + "is filtered");
                return;
            }
            /*if (limitation != -1 &&
                    limitation < INVOCATION_COUNTER.stackMethodIncrease(tr.getClass().getName(), tr.getStackTrace())) {
                LOG.i(LogProxy.LOG_TAG,
                        "XMLCONTROLLER::entering" + method + " with exception "
                                + tr.getClass().getName() + " SkipCycle ");
                return;
            }

            int exceptioncount = INVOCATION_COUNTER.exceptionIncrease(tr.getClass().getName());
            LOG.i(LogProxy.LOG_TAG,
                    "XMLCONTROLLER::entering(" + count + ") " + method + " with exception "
                            + tr.getClass().getName() + " and such exception entering(" + exceptioncount + ")");
            if ((forceControl || inTestMethod())
                && ((GeneralControlPolicy) CONTROL_POLICY)
                       .takeOver(method, count, tr, exceptioncount)) {
                LOG.i(LogProxy.LOG_TAG, "triggering exception on " + method, tr);
                // PYTHON_CLIENT_COMMAND_SERVER.onForceThrow(invocationInfo);
                throw tr;
            } else {
                LOG.i(LogProxy.LOG_TAG, "passing on " + method);
                // PYTHON_CLIENT_COMMAND_SERVER.onPass(invocationInfo);
            }*/
        } else if(CONTROL_POLICY instanceof newGeneralControlPolicy){
            LOG.i(LogProxy.LOG_TAG,
                    "XMLCONTROLLER::entering(" + count + ") " + method + " with exception "
                            + tr.getClass().getName());
            if ((forceControl || inTestMethod()) && CONTROL_POLICY.takeOver(method, count, tr)) {

                LOG.i(LogProxy.LOG_TAG, "triggering exception on " + method, tr);

                // PYTHON_CLIENT_COMMAND_SERVER.onForceThrow(invocationInfo);
                throw tr;
            } else {
                LOG.i(LogProxy.LOG_TAG, "passing on " + method);
                // PYTHON_CLIENT_COMMAND_SERVER.onPass(invocationInfo);
            }
        }
    }

    public static synchronized void enter(
        String method, String caller, String location, Throwable tr) throws Throwable {
        enter(method, caller, location, tr, true);
    }


    public static void load() {
        // empty method for loading this class
    }
}
