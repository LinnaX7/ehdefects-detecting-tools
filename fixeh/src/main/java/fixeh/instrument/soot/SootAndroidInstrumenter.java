package fixeh.instrument.soot;

import com.google.common.reflect.ClassPath;

import soot.G;
import soot.options.Options;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import fixeh.instrument.AndroidJavaByteCodeInstrumenter;

/**
 * Created by Shunjie Ding on 2018/4/12.
 */
public abstract class SootAndroidInstrumenter extends AndroidJavaByteCodeInstrumenter {
    private ByteArrayOutputStream sootLogBuffer = new ByteArrayOutputStream();

    protected void setOptions(File target, File outputDir) {
        // Hook PrintStream soot uses
        G.v().out = new PrintStream(sootLogBuffer);

        // Enable unfriendly mode to run soot.Main.main() without args
        Options.v().set_unfriendly_mode(true);

        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_output_format(Options.output_format_dex);
        Options.v().set_allow_phantom_refs(true);

        Options.v().set_process_multiple_dex(true);
        Options.v().set_verbose(false);
        Options.v().set_whole_program(false);
        Options.v().set_keep_line_number(true);

        Options.v().set_android_jars(getAndroidJarPath());
        Options.v().set_soot_classpath(String.join(":", getClassPaths()));
        Options.v().set_process_dir(Collections.singletonList(target.getAbsolutePath()));
        Options.v().set_output_dir(outputDir.getAbsolutePath());
    }

    protected SootAndroidInstrumenter(List<String> classPaths, String targetFile, String javaHome,
        int sdkVersion, String buildToolsVersion, String androidHome) {
        super(classPaths, targetFile, javaHome, sdkVersion, buildToolsVersion, androidHome);
    }

    protected ClassPath getClassPathFromResources(List<String> jarFiles) throws IOException {
        URL[] urls = jarFiles.stream()
                         .map(cp -> {
                             try {
                                 return new URL("file:///" + cp);
                             } catch (MalformedURLException e) {
                                 e.printStackTrace();
                                 return null;
                             }
                         })
                         .filter(Objects::nonNull)
                         .toArray(URL[] ::new);
        return ClassPath.from(URLClassLoader.newInstance(urls));
    }
}
