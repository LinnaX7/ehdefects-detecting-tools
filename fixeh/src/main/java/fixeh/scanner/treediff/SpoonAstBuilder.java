package fixeh.scanner.treediff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.List;
import fixeh.Constants;
import spoon.SpoonModelBuilder;
import spoon.compiler.SpoonResourceHelper;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.VirtualFile;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

/**
 * Created by Shunjie Ding on 25/12/2017.
 */
public final class SpoonAstBuilder {
    private final Logger logger = LoggerFactory.getLogger(SpoonAstBuilder.class);
    private final boolean noClasspath;
    private final Factory factory;

    public SpoonAstBuilder(boolean noClasspath) {
        this.noClasspath = noClasspath;
        this.factory = newDefaultFactory(null);
    }

    public SpoonAstBuilder(Factory factory, boolean noClasspath) {
        this.noClasspath = noClasspath;
        this.factory = factory;
        factory.getEnvironment().setNoClasspath(noClasspath);
    }

    public SpoonAstBuilder(List<String> classPaths, boolean noClasspath) {
        this.noClasspath = noClasspath;
        this.factory = newDefaultFactory(classPaths);
    }

    public static String getQualifiedClassName(String filePath, Reader content) {
        if (!filePath.endsWith(".java")) {
            throw new IllegalArgumentException("Must be a Java file!");
        }
        if (content == null) {
            throw new IllegalArgumentException("Must specify the contents of the file");
        }

        // Remove parent paths and .java suffix
        String className = Paths.get(filePath).getFileName().toString();
        className = className.substring(0, className.length() - 5);

        String packageName = "";
        try (BufferedReader bufferedReader = new BufferedReader(content)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("package ") && line.endsWith(";")) {
                    packageName = line.substring(8, line.length() - 1).replaceAll(" ", "");
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Can not read the contents!");
        }

        if (packageName.isEmpty()) {
            return className;
        }
        return packageName + "." + className;
    }

    private Factory newDefaultFactory(List<String> classPaths) {
        Factory factory = new FactoryImpl(new DefaultCoreFactory(), new StandardEnvironment());
        factory.getEnvironment().setLevel(Constants.isCompilerVerbose() ? "DEBUG" : "OFF");
        factory.getEnvironment().setNoClasspath(noClasspath);
        if (classPaths == null || classPaths.isEmpty()) {
            factory.getEnvironment().setNoClasspath(true);
        } else {
            factory.getEnvironment().setSourceClasspath(classPaths.toArray(new String[0]));
        }
        return factory;
    }

    public CtType build(String filePath, byte[] content) {
        SpoonModelBuilder compiler = new JDTBasedSpoonCompiler(factory);
        compiler.addInputSource(new VirtualFile(new String(content), filePath));
        if (!compiler.build()) {
            logger.debug("Some errors occurred when building file {}!", filePath);
        }
        return factory.Type().get(
            getQualifiedClassName(filePath, new StringReader(new String(content))));
    }

    public CtType build(File f) throws FileNotFoundException {
        SpoonModelBuilder compiler = new JDTBasedSpoonCompiler(factory);
        compiler.addInputSource(SpoonResourceHelper.createFile(f));
        if (compiler.build()) {
            logger.debug("Some errors occurred when building file {}!", f.getAbsoluteFile());
        }
        return factory.Type().get(getQualifiedClassName(f.getAbsolutePath(), new FileReader(f)));
    }
}
