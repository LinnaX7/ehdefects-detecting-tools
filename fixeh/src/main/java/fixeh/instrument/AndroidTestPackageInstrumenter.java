package fixeh.instrument;

import com.google.common.reflect.ClassPath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.options.Options;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import fixeh.Constants;
import fixeh.instrument.soot.SootAndroidInstrumenter;
import fixeh.instrument.soot.SootInjector;
import fixeh.instrument.woventools.ExceptionController;

/**
 * Created by Shunjie Ding on 2018/4/12.
 */
public class AndroidTestPackageInstrumenter extends SootAndroidInstrumenter {
    private final Logger logger = LoggerFactory.getLogger(AndroidTestPackageInstrumenter.class);

    public AndroidTestPackageInstrumenter(
        String targetFile, List<String> classPaths, int sdkVersion, String buildToolsVersion) {
        super(classPaths, targetFile, Constants.JAVA_HOME, sdkVersion, buildToolsVersion,
            Constants.ANDROID_HOME);
    }

    private boolean hasTestAnnotation(SootMethod method) {
        VisibilityAnnotationTag tag =
            (VisibilityAnnotationTag) method.getTag("VisibilityAnnotationTag");
        if (tag == null) {
            return false;
        }

        for (AnnotationTag annotationTag : tag.getAnnotations()) {
            if (annotationTag.getType().equals("Lorg/junit/Test;")) {
                return true;
            }
        }

        return false;
    }

    private void setTransforms() {
        PackManager.v().getPack("jtp").add(
            new Transform("jtp.instrumentInvocations", new BodyTransformer() {
                @Override
                protected void internalTransform(
                    Body b, String phaseName, Map<String, String> options) {
                    final SootMethod method = b.getMethod();

                    // Exit if it is not a test method
                    if (!hasTestAnnotation(method)) {
                        return;
                    }

                    // And Junit ensures that the method must start with public void
                    if (method.isPrivate() || method.isStatic() || method.isAbstract()) {
                        return;
                    }

                    final SootInjector sootInjector = new SootInjector(b.getLocals());
                    final PatchingChain<Unit> units = b.getUnits();

                    Unit firstUnit = units.getFirst();
                    units.insertAfter(sootInjector.enterUnitTestMethod(method), firstUnit);

                    b.validate();
                }
            }));
    }

    private void setWovenClasses(ClassPath classPath) {
        // Get all classes, including inner classes
        List<ClassPath.ClassInfo> classInfoList =
            classPath.getAllClasses()
                .asList()
                .stream()
                .filter(classInfo
                    -> classInfo.getName().startsWith(
                        ExceptionController.class.getPackage().getName()))
                .collect(Collectors.toList());
        for (ClassPath.ClassInfo classInfo : classInfoList) {
            logger.info("Load woven class {}", classInfo.getName());
            Scene.v().addBasicClass(classInfo.getName(), SootClass.BODIES);
        }
    }

    @Override
    protected boolean instrumentAndSave(File target, File outputDir) {
        try {
            // set_include_all must be called at first!
            Options.v().set_include_all(true);
            setOptions(target, outputDir);

            setWovenClasses(getClassPathFromResources(getClassPaths()));

            setTransforms();

            soot.Main.main(new String[0]);
        } catch (Throwable tr) {
            tr.printStackTrace();
            return false;
        }

        return true;
    }
}
