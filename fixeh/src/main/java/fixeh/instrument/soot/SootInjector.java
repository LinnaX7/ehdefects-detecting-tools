package fixeh.instrument.soot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.DoubleType;
import soot.FloatType;
import soot.IntegerType;
import soot.Local;
import soot.LongType;
import soot.PrimType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.LongConstant;
import soot.jimple.NullConstant;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StringConstant;
import soot.tagkit.LineNumberTag;
import soot.util.Chain;

import java.util.*;
import java.util.stream.Collectors;
import fixeh.instrument.InstrumentInfo;
import fixeh.instrument.SootUtils;
import fixeh.instrument.woventools.ExceptionController;
import fixeh.instrument.woventools.InvocationInfo;

/**
 * Created by Shunjie Ding on 22/01/2018.
 */
public final class SootInjector {
    private static final List<String> BASIC_CLASSES = Arrays.asList("java.lang.String",
        "java.lang.Integer", "java.lang.Long", "java.lang.Short", "java.lang.Byte",
        "java.lang.Character", "java.lang.Boolean", "java.lang.Float", "java.lang.Double");
    private final Logger logger = LoggerFactory.getLogger(SootInjector.class);
    private final Chain<Local> locals;

    public SootInjector(Chain<Local> locals) {
        this.locals = locals;
    }

    public Local newLocal(String name, Type type) {
        Local local = Jimple.v().newLocal(name, type);
        locals.add(local);
        return local;
    }

    public List<Unit> format(ValueBox resBox, String fmt, Value... values) {
        if (values.length == 0) {
            locals.remove(resBox.getValue());
            resBox.setValue(StringConstant.v(fmt));
            return new ArrayList<>();
        }

        final SootClass stringClass = Scene.v().getSootClass("java.lang.String");
        final SootClass objectClass = Scene.v().getSootClass("java.lang.Object");

        List<Unit> result = new ArrayList<>();

        // Prepare array and invoke String.format(String, Object[])
        Local args = newLocal(null, objectClass.getType().getArrayType());
        for (int i = 0; i < values.length; ++i) {
            AssignStmt assignStmt =
                Jimple.v().newAssignStmt(Jimple.v().newArrayRef(args, IntConstant.v(i)), values[i]);
            result.add(assignStmt);
        }

        StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(
            stringClass
                .getMethod("format",
                    Arrays.asList(stringClass.getType(), objectClass.getType().getArrayType()),
                    stringClass.getType())
                .makeRef(),
            StringConstant.v(fmt), args);
        AssignStmt assignStmt = Jimple.v().newAssignStmt(resBox.getValue(), staticInvokeExpr);
        result.add(assignStmt);

        return result;
    }

    private boolean isBasicRefType(RefType type) {
        return BASIC_CLASSES.stream().anyMatch(t -> type.getClassName().equals(t));
    }

    private boolean isBasicType(Type type) {
        return type instanceof PrimType
            || (type instanceof RefType && isBasicRefType((RefType) type));
    }

    private boolean isAllBasicTypes(List<Type> types) {
        return types.stream().allMatch(this ::isBasicType);
    }

    private Value getDefaultValueForType(Type type) {
        if (!isBasicType(type)) {
            return NullConstant.v();
        }

        if (type instanceof PrimType) {
            if (type instanceof IntegerType) {
                return IntConstant.v(0);
            } else if (type instanceof LongType) {
                return LongConstant.v(0);
            } else if (type instanceof FloatType) {
                return FloatConstant.v(0.0f);
            } else if (type instanceof DoubleType) {
                return DoubleConstant.v(0.0);
            }
        } else if (type instanceof RefType) {
            RefType refType = (RefType) type;
            if (refType.getClassName().equals("java.lang.String")) {
                return StringConstant.v("");
            } else if (Arrays
                           .stream(new String[] {"java.lang.Integer", "java.lang.Short",
                               "java.lang.Byte", "java.lang.Character", "java.lang.Boolean"})
                           .anyMatch(t -> refType.getClassName().equals(t))) {
                return IntConstant.v(0);
            } else if (refType.getClassName().equals("java.lang.Long")) {
                return LongConstant.v(0);
            } else if (refType.getClassName().equals("java.lang.Float")) {
                return FloatConstant.v(0.0f);
            } else if (refType.getClassName().equals("java.lang.Double")) {
                return DoubleConstant.v(0.0);
            }
        }

        return NullConstant.v();
    }

    private List<Value> getDefaultValuesForTypes(List<Type> types) {
        return types.stream().map(this ::getDefaultValueForType).collect(Collectors.toList());
    }

    public List<Unit> newThrowable(ValueBox resBox, String msg, SootClass throwableClass)
        throws NoUsableConstructorException {
        final SootClass exceptionClass =
            Scene.v().getSootClass(resBox.getValue().getType().toString());
        final SootClass stringClass = Scene.v().getSootClass("java.lang.String");

        List<Unit> results = new ArrayList<>();

        msg = msg == null ? "" : msg;

        Local exception = (Local) resBox.getValue();
        // Initialize exception
        SootMethod constructor = exceptionClass.getMethodUnsafe(SootMethod.constructorName,
            Collections.singletonList(stringClass.getType()), VoidType.v());
        // If default <init>(Ljava/lang/String;) is not found, find a usable constructor
        results.add(Jimple.v().newAssignStmt(
            resBox.getValue(), Jimple.v().newNewExpr(exceptionClass.getType())));
        if (constructor != null) {
            SpecialInvokeExpr initExpr = Jimple.v().newSpecialInvokeExpr(
                exception, constructor.makeRef(), StringConstant.v(msg));
            results.add(Jimple.v().newInvokeStmt(initExpr));
        } else {
            // Check if <init>() exists
            constructor = exceptionClass.getMethodUnsafe(
                SootMethod.constructorName, new ArrayList<>(), VoidType.v());
            if (constructor != null) {
                SpecialInvokeExpr initExpr =
                    Jimple.v().newSpecialInvokeExpr(exception, constructor.makeRef());
                results.add(Jimple.v().newInvokeStmt(initExpr));
            } else {
                List<SootMethod> methods = exceptionClass.getMethods();
                for (SootMethod method : methods) {
                    if (method.getName().equals(SootMethod.constructorName)) {
                        if (isAllBasicTypes(method.getParameterTypes())) {
                            SpecialInvokeExpr initExpr =
                                Jimple.v().newSpecialInvokeExpr(exception, method.makeRef(),
                                    getDefaultValuesForTypes(method.getParameterTypes()));
                            results.add(Jimple.v().newInvokeStmt(initExpr));
                            break;
                        }
                    }
                }
            }
        }

        // If no usable constructor found, set resBox to null and clear results
        if (results.size() == 1) {
            results.clear();
            throw new NoUsableConstructorException();
        }

        return results;
    }

    private List<Unit> enterMethod(
        SootMethod method, List<SootClass> exClasses, InstrumentInfo info) {
        final SootClass exceptionControllerClass =
            Scene.v().getSootClass(ExceptionController.class.getName());
        final SootClass stringClass = Scene.v().getSootClass("java.lang.String");
        final SootClass throwableClass = Scene.v().getSootClass("java.lang.Throwable");

        List<Unit> results = new ArrayList<>();

        for (SootClass exClass : exClasses) {
            Local ex = newLocal(null, exClass.getType());
            ValueBox exBox = Jimple.v().newLocalBox(ex);
            try {
                results.addAll(newThrowable(exBox, null, throwableClass));
                // Call ExceptionController.enter(String, Throwable)
                results.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(
                    exceptionControllerClass
                        .getMethod("enter",
                            Arrays.asList(stringClass.getType(), stringClass.getType(),
                                stringClass.getType(), throwableClass.getType()))
                        .makeRef(),
                    StringConstant.v(SootUtils.getMethodSignature(method)),
                    StringConstant.v(info.getCaller()), StringConstant.v(info.getLocation()),
                    exBox.getValue())));
            } catch (NoUsableConstructorException e) {
                logger.error(
                    "Could not find usable constructor for exception {}", exClass.getName());

                locals.remove(ex);
            }
        }

        return results;
    }

    public static void setLineNumberTagsFor(List<Unit> units, int lineNumber) {
        for (Unit unit: units) {
            if (unit.hasTag("LineNumberTag")) {
                unit.removeTag("LineNumberTag");
            }
            unit.addTag(new LineNumberTag(lineNumber));
        }
    }

    public List<Unit> enterMethod(SootMethod method, InstrumentInfo info) {
        return enterMethod(method, method.getExceptions(), info);
    }

    public List<Unit> enterMethodWithTotalRuntimeException(
        SootMethod method, InstrumentInfo info, Set<String> exceptionnames) {
        List<SootClass> specialETypes = new ArrayList<>();
        for (String specialEType : exceptionnames) {
            specialETypes.add(Scene.v().getSootClass(specialEType));
        }
        List<SootClass> exceptions = new ArrayList<SootClass>(method.getExceptions().size() + 1);
        exceptions.addAll(method.getExceptions());
        exceptions.addAll(specialETypes);
        return enterMethod(method, exceptions, info);
    }

    public List<Unit> enterMethodWithRuntimeException(SootMethod method, InstrumentInfo info) {
        final SootClass runtimeExceptionClass =
            Scene.v().getSootClass("java.lang.RuntimeException");
        List<SootClass> exceptions = new ArrayList<SootClass>(method.getExceptions().size() + 1);
        exceptions.addAll(method.getExceptions());
        exceptions.add(runtimeExceptionClass);
        return enterMethod(method, exceptions, info);
    }

    public List<Unit> enterUnitTestMethod(SootMethod testMethod) {
        final SootClass exceptionControllerClass =
            Scene.v().getSootClass(ExceptionController.class.getName());
        final SootClass stringClass = Scene.v().getSootClass("java.lang.String");

        List<Unit> results = new ArrayList<>();
        results.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(
            exceptionControllerClass
                .getMethod("enterTestMethod", Collections.singletonList(stringClass.getType()))
                .makeRef(),
            StringConstant.v(SootUtils.getMethodSignature(testMethod)))));
        return results;
    }

    private static class NoUsableConstructorException extends Exception {}

    public List<Unit> loadExceptionControllerClass() {
        final SootClass exceptionControllerClass =
            Scene.v().getSootClass(ExceptionController.class.getName());
        List<Unit> results = new ArrayList<>();
        results.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(
            exceptionControllerClass.getMethodByName("load").makeRef())));
        return results;
    }
}