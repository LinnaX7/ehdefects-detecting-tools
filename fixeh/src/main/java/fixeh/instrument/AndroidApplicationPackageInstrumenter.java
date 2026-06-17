package fixeh.instrument;

import com.google.common.reflect.ClassPath;

import fixeh.util.container.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import fixeh.Constants;
import fixeh.instrument.soot.SootAndroidInstrumenter;
import fixeh.instrument.soot.SootInjector;
import fixeh.instrument.woventools.ExceptionController;

/**
 * Created by Shunjie Ding on 22/01/2018.
 */
public class AndroidApplicationPackageInstrumenter extends SootAndroidInstrumenter {
    private final Logger logger =
        LoggerFactory.getLogger(AndroidApplicationPackageInstrumenter.class);

    private List<String> targetPackages = Arrays.asList(Constants.RESOURCE_RELATED_PACKAGES);
    private static final List<String> runtimeExceptionRelatedMethods =
        Arrays.asList(Constants.RUNTIME_EXCEPTION_RELATED_METHODS);
    private static final Map<String, List<String>> totalRuntimeExceptionRelatedMethods =
        Constants.TOTAL_RUNTIME_EXCEPTIONS_RELATED_METHODS;
    private List<String> wovenClassPath;
    private List<String> wovenClasses = new ArrayList<>();
    private boolean classesAdded = false;

    public AndroidApplicationPackageInstrumenter(String targetFile, List<String> wovenClassPath,
        String javaHome, String androidHome, int sdkVersion, String buildToolsVersion) {
        super(wovenClassPath, targetFile, javaHome, sdkVersion, buildToolsVersion, androidHome);
        this.wovenClassPath = wovenClassPath;
    }

    public AndroidApplicationPackageInstrumenter(
        String targetFile, List<String> wovenClassPath, int sdkVersion, String buildToolsVersion) {
        super(wovenClassPath, targetFile, Constants.JAVA_HOME, sdkVersion, buildToolsVersion,
            Constants.ANDROID_HOME);
        this.wovenClassPath = wovenClassPath;
    }

    public List<String> getTargetPackages() {
        return targetPackages;
    }

    public void setTargetPackages(List<String> targetPackages) {
        this.targetPackages = targetPackages;
    }

    private void setWovenClasses() throws IOException {
        // Get all classes, including inner classes
        List<ClassPath.ClassInfo> classInfoList =
            getClassPathFromResources(wovenClassPath)
                .getAllClasses()
                .asList()
                .stream()
                .filter(classInfo
                    -> classInfo.getName().startsWith(
                        ExceptionController.class.getPackage().getName()))
                .collect(Collectors.toList());
        for (ClassPath.ClassInfo classInfo : classInfoList) {
            logger.info("Load woven class {}", classInfo.getName());
            Scene.v().addBasicClass(classInfo.getName(), SootClass.BODIES);
            wovenClasses.add(classInfo.getName());
        }
    }

    private void setBasicExceptionClasses(ClassPath classPath) {
        // add RuntimeException by default
        for (String runtimeException : totalRuntimeExceptionRelatedMethods.keySet()) {
            logger.info("Load unchecked exception class {}", runtimeException);
            Scene.v().addBasicClass(runtimeException, SootClass.SIGNATURES);
        }

        for (String pkg : targetPackages) {
            for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(pkg)) {
                if (Throwable.class.isAssignableFrom(classInfo.load())) {
                    logger.info("Load exception class {}", classInfo.getName());
                    Scene.v().addBasicClass(classInfo.getName(), SootClass.SIGNATURES);
                }
            }
        }
    }

    private boolean isPackageTargeted(String packageName) {
        return getTargetPackages().stream().anyMatch(packageName::startsWith);
    }

    private boolean isClassTargeted(String className) {
        return getTargetPackages().stream().anyMatch(className::startsWith);
    }

    private static boolean isRuntimeExceptionRelated(final SootMethod method) {
        final String signature = SootUtils.getMethodSignature(method);
        return runtimeExceptionRelatedMethods.stream().anyMatch(signature::equals);
    }
    private static Pair<Boolean, Set<String>> isTotalRuntimeExceptionRelated(
        final SootMethod method) {
        final String signature = SootUtils.getMethodSignature(method);
        Set<String> exceptions = new HashSet<>();
        for (String key : totalRuntimeExceptionRelatedMethods.keySet()) {
            if (totalRuntimeExceptionRelatedMethods.get(key).stream().anyMatch(signature::equals)) {
                exceptions.add(key);
            }
        }
        return Pair.create(!exceptions.isEmpty(), exceptions);
    }

    private synchronized void addWovenClasses() {
        if (classesAdded) {
            return;
        }

        // Add classes to apk
        for (String wovenClass : wovenClasses) {
            Scene.v().getSootClass(wovenClass).setApplicationClass();
        }

        classesAdded = true;
    }

    private static String convertClassesToString(List<SootClass> sootClasses) {
        if (sootClasses == null || sootClasses.isEmpty()) {
            return "";
        }
        return sootClasses.stream().map(SootClass::getName).collect(Collectors.joining(","));
    }

    private final HashSet<InstrumentInfo> statistics = new HashSet<>();

    public final HashSet<InstrumentInfo> getStatistics() {
        return statistics;
    }

    private void setTransfromJtpInstrumentInvocations() {
        PackManager.v().getPack("jtp").add(
            new Transform("jtp.instrumentInvocations", new BodyTransformer() {
                @Override
                protected void internalTransform(
                    Body b, String phaseName, Map<String, String> options) {
                    final PatchingChain<Unit> units = b.getUnits();

                    final SootInjector sootInjector = new SootInjector(b.getLocals());

                    final SootClass bodyClass = b.getMethod().getDeclaringClass();

                    Iterator<Unit> it = units.snapshotIterator();
                    while (it.hasNext()) {
                        Unit unit = it.next();
                        if (unit instanceof Stmt) {
                            try{
                            Stmt stmt = (Stmt) unit;

                            if (!(stmt instanceof JAssignStmt) && !(stmt instanceof JInvokeStmt)) {
                                // Avoid RuntimeException when calling containsInvokeExpr
                                continue;
                            }

                            if (!stmt.containsInvokeExpr()) {
                                // Ignore statements that do not contain invoke expressions
                                continue;
                            }

                            InvokeExpr invokeExpr = stmt.getInvokeExpr();
                            //try {
                                SootMethod method = invokeExpr.getMethod();

                            // Check if method is targeted
                            if (!isPackageTargeted(method.getDeclaringClass().getPackageName())) {
                                continue;
                            }
                            // Ignore
                            if (isTotalRuntimeExceptionRelated(method).first) {
                                logger.info(
                                        "Injecting controller for both checked and unchecked exceptions before invocation of {} ({}) in method {}.",
                                        method.getSignature(),
                                        convertClassesToString(method.getExceptions()),
                                        b.getMethod().getSignature());

                                InstrumentInfo info = InstrumentInfo.newInstrumentInfo(method,
                                        b.getMethod(), isTotalRuntimeExceptionRelated(method).second,
                                        bodyClass.getName(), stmt.getJavaSourceStartLineNumber());
                                synchronized (statistics) {
                                    // FIXME here we should add unchecked exceptions
                                    statistics.add(info);
                                }

                                List<Unit> toInsert = sootInjector.enterMethodWithTotalRuntimeException(method, info,
                                        isTotalRuntimeExceptionRelated(method).second);
                                SootInjector.setLineNumberTagsFor(toInsert, stmt.getJavaSourceStartLineNumber());
                                units.insertBefore(toInsert, unit);
                            } else if (isRuntimeExceptionRelated(method)) {
                                // Ignore methods does not declare any exceptions and not
                                // RuntimeException related
                                logger.info(
                                        "Injecting controller for both checked and unchecked exceptions before invocation of {} ({}) in method {}.",
                                        method.getSignature(),
                                        convertClassesToString(method.getExceptions()),
                                        b.getMethod().getSignature());

                                InstrumentInfo info =
                                        InstrumentInfo.newInstrumentInfo(method, b.getMethod(), null,
                                                bodyClass.getName(), stmt.getJavaSourceStartLineNumber());
                                synchronized (statistics) {
                                    // FIXME here we should add unchecked exceptions
                                    statistics.add(info);
                                }

                                List<Unit> toInsert = sootInjector.enterMethodWithRuntimeException(method, info);
                                SootInjector.setLineNumberTagsFor(toInsert, stmt.getJavaSourceStartLineNumber());
                                units.insertBefore(toInsert, unit);
                            } else if (!method.getExceptions().isEmpty()) {
                                logger.info(
                                        "Injecting controller for checked exceptions before invocation of {} ({}) in method {}.",
                                        method.getSignature(),
                                        convertClassesToString(method.getExceptions()),
                                        b.getMethod().getSignature());

                                InstrumentInfo info =
                                        InstrumentInfo.newInstrumentInfo(method, b.getMethod(), null,
                                                bodyClass.getName(), stmt.getJavaSourceStartLineNumber());
                                synchronized (statistics) {
                                    statistics.add(info);
                                }

                                List<Unit> toInsert = sootInjector.enterMethod(method, info);
                                SootInjector.setLineNumberTagsFor(toInsert, stmt.getJavaSourceStartLineNumber());
                                units.insertBefore(toInsert, unit);
                            }
                        }catch (SootMethodRefImpl.ClassResolutionFailedException e) {
                                logger.error("i am not wrong!");
                                continue;
                            }
                        }
                    }
                    b.validate();

                    addWovenClasses();
                }
            }));
    }

    private void setTransformJtpInstrumentActivityOnCreate() {
        PackManager.v().getPack("jtp").add(
            new Transform("jtp.instrumentActivityOnCreate", new BodyTransformer() {
                @Override
                protected void internalTransform(Body body, String s, Map<String, String> map) {
                    final PatchingChain<Unit> units = body.getUnits();
                    final SootInjector sootInjector = new SootInjector(body.getLocals());
                    final SootMethod method = body.getMethod();

                    // inject all onCreate non-static methods
                    if (method.getName().equals("onCreate") && !method.isStatic()
                        && method.isConcrete()) {
                        Iterator<Unit> it = units.iterator();
                        int paramSize = method.getParameterCount();
                        for (int i = 0; i < paramSize; ++i, it.next()) {
                        }
                        // skip param + 1 units
                        Unit point = it.next();
                        List<Unit> toInsert = sootInjector.loadExceptionControllerClass();
                        SootInjector.setLineNumberTagsFor(toInsert, point.getJavaSourceStartLineNumber());
                        units.insertAfter(toInsert, point);
                    }
                }
            }));
    }

    private void setTransforms() {
        setTransformJtpInstrumentActivityOnCreate();
        setTransfromJtpInstrumentInvocations();
    }

    @Override
    protected boolean instrumentAndSave(File target, File outputDir) {
        if (targetPackages == null || targetPackages.isEmpty()) {
            logger.warn("Target packages of apk instrumenter is empty! Skip instrumenting!");
            return false;
        }

        try {
            // Options must be set at first
            setOptions(target, outputDir);

            ClassPath classPath = getClassPathFromResources(getClassPaths());
            setWovenClasses();
            setBasicExceptionClasses(classPath);

            setTransforms();

            statistics.clear();
            soot.Main.main(new String[0]);
        } catch (Throwable tr) {
            tr.printStackTrace();
            return false;
        }

        return true;
    }
}
