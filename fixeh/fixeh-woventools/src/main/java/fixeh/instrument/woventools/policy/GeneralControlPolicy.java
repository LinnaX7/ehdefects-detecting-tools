package fixeh.instrument.woventools.policy;

import fixeh.instrument.woventools.Log;
import fixeh.instrument.woventools.LogProxy;

import java.util.*;

/**
 * Created by Shunjie Ding on 29/01/2018.
 */
public class GeneralControlPolicy implements ControlPolicy {
    private static final Log LOG = LogProxy.getInstance();
    private static final int UNLIMITED = -1;

    private Set<String> packages;

    private Set<String> classes;

    private Set<String> methods;

    private Map<String, BitSet> methodPatterns;

    private BitSet generalPattern;

    private Map<String, BitSet> exceptionPatterns;

    private Set<String> exceptions;

    private Map<String,Map<String,BitSet>> exceptionMethodPatterns;
    private Map<String,Integer> stackMaxrepeat;

    private volatile boolean exclude = false;

    private volatile int limit = 0;

    private volatile int callCount = 0;
    private volatile int exceptionCallCount = 0;

    private Map<String, Set<String>> filterMethods;
    private Set<String> filterStackKeyWords;
    private Map<String, Set<String>>  filterPackages;
    private Map<String, Set<String>>  filterClasses;

    private static <T> String setToString(Set<T> set) {
        if (set == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Iterator<T> it = set.iterator();
        while (it.hasNext()) {
            T obj = it.next();
            sb.append(obj.toString());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private static <K, V> String mapToString(Map<K, V> map) {
        if (map == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, V> obj = it.next();
            sb.append("(").append(obj.getKey()).append(",").append(obj.getValue()).append(")");
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private static String getClassName(String method) {
        int index = method.lastIndexOf(':');
        return method.substring(0, index);
    }

    private static String getPackageName(String className) {
        int index = className.lastIndexOf('.');
        return className.substring(0, index);
    }

    @Override
    public String toString() {
        String format = "exception : %s, with methodspatterns: %s\n";
        String output = String.format(
            "GeneralControlPolicy {limit: %d, exclude: %s, packages: %s, classes: %s, methods: %s, pattern: %s method patterns:%s}",
            limit, exclude, setToString(packages), setToString(classes), setToString(methods), generalPattern,mapToString(methodPatterns));
        output += String.format(" expections: %s, expectionsPattern: %s,expectionStackRepeat: %s",setToString(exceptions),mapToString(exceptionPatterns),mapToString(stackMaxrepeat));
        output += String.format(" filterpackages: %s", mapToString(filterPackages));
        output += String.format(" filterclasses: %s",mapToString(filterClasses));
        output += String.format(" filtermethodss: %s",mapToString(filterMethods));
        output += String.format(" filterstackkeywords: %s",setToString(filterStackKeyWords));

        //for (String exception:exceptionMethodPatterns.keySet()){
         //   output += String.format(format, exception,mapToString(exceptionMethodPatterns.get(exception)));

        //}
        return output;
    }

    public synchronized void addFilterMethods(String method, String stackKeyWord){
        if(method == null) return;

        if(filterMethods == null){
            filterMethods = new HashMap<>();
        }
        if(!filterMethods.containsKey(method))
            filterMethods.put(method, new HashSet<String>());
        if(stackKeyWord == null) return;
        filterMethods.get(method).add(stackKeyWord);
    }

    public synchronized void addFilterPackage(String fpackage,String stackKeyWord){
        if(fpackage == null) return;

        if(filterPackages == null){
            filterPackages = new HashMap<>();
        }
        if(!filterPackages.containsKey(fpackage))
            filterPackages.put(fpackage,new HashSet<String>());
        if(stackKeyWord == null) return;
        filterPackages.get(fpackage).add(stackKeyWord);
    }

    public synchronized void addFilterClasses(String fclass, String stackKeyWord){
        if(fclass == null) return;
        if(filterClasses == null){
            filterClasses = new HashMap<>();
        }
        if(!filterClasses.containsKey(fclass))
            filterClasses.put(fclass,new HashSet<String>());
        if(stackKeyWord == null) return;
        filterClasses.get(fclass).add(stackKeyWord);
    }

    public synchronized void addFilterStackKeyWords(String stackKeyWords){
        if(filterStackKeyWords == null){
            filterStackKeyWords= new HashSet<>();
        }
        filterStackKeyWords.add(stackKeyWords);
    }


    public synchronized void addPackage(String pkg) {
        if (packages == null) {
            packages = new HashSet<>();
        }
        packages.add(pkg);
    }

    public synchronized void removePackage(String pkg) {
        if (packages == null) {
            return;
        }
        packages.remove(pkg);
    }

    public synchronized void addClass(String cls) {
        if (classes == null) {
            classes = new HashSet<>();
        }
        classes.add(cls);
    }

    public synchronized void addException(String exp) {
        if (exceptions == null) {
            exceptions = new HashSet<>();
        }
        if(stackMaxrepeat==null){
            stackMaxrepeat = new HashMap<>();
        }
        exceptions.add(exp);
        stackMaxrepeat.put(exp,-1);

    }

    public synchronized void removeClass(String cls) {
        if (classes == null) {
            return;
        }
        classes.remove(cls);
    }

    public synchronized void addMethod(String method) {
        if (methods == null) {
            methods = new HashSet<>();
        }
        methods.add(method);
    }

    public synchronized void removeMethod(String method) {
        if (methods == null) {
            return;
        }
        methods.remove(method);
        methodPatterns.remove(method);
    }

    public synchronized void removeException(String exception) {
        if (exceptions == null) {
            return;
        }
        exceptions.remove(exception);
        exceptionPatterns.remove(exception);
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isExclude() {
        return exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    public boolean takeOverIncludeException(Throwable tr, int count, String method) {
        if (exceptions != null && exceptions.contains(tr.getClass().getName())) {
            return (methods == null && classes == null && packages == null)
                || takeOverInclude(method, count, tr);
        }
        return false;
    }

    public boolean takeOverInclude(String method, int count, Throwable tr) {
        LOG.d(LogProxy.LOG_TAG,"method: " + method + "class: "+ getClassName(method) + "package: "+ getPackageName(method));
        if (methods != null && methods.contains(method)) {
            LOG.d(LogProxy.LOG_TAG,"get method!");
            return true;
        }

        String className = getClassName(method);
        if (classes != null && classes.contains(className)) {
            LOG.d(LogProxy.LOG_TAG,"get class!");
            return true;
        }

        String packageName = getPackageName(className);
        if (packages != null && packages.contains(packageName)) {
            LOG.d(LogProxy.LOG_TAG,"get packagesss!");
            return true;
        }

        return false;
    }

    public boolean takeOverExclude(String method, int count, Throwable tr) {
        String className = getClassName(method);

        // Avoid using stream(). It introduces dynamic invoke instruction which soot currently not
        // supports.
        if (packages != null) {
            String packageName = getPackageName(className);
            for (String pkg : packages) {
                // If package is excluded or is subpackage of one of the excluded package
                if (packageName.equals(pkg) || packageName.startsWith(pkg + ".")) {
                    return false;
                }
            }
        }

        if (classes != null && classes.contains(className)) {
            return false;
        }

        if (methods != null && methods.contains(method)) {
            return false;
        }

        return true;
    }

    private boolean takeOverUsingMethodPattern(String method, int count) {
        return methodPatterns == null
            || (methodPatterns.containsKey(method) && methodPatterns.get(method).get(count));
    }

    private boolean takeOverUsingExceptionPattern(String exception, int count,String method) {
        return exceptionPatterns == null
            || (exceptionPatterns.containsKey(exception)
                   && exceptionPatterns.get(exception).get(count));
    }


    private boolean takeOverIncludeExceptionMethodPattern(String exception, int exceptionMethodCount,
                                                          String method){
        if(exceptionMethodPatterns.get(exception) == null || exceptionMethodPatterns.size()==0)
            return true;
        else if( ! exceptionMethodPatterns.get(exception).containsKey(method)) {
            return false;
        } else{
            return exceptionMethodPatterns.get(exception).get(method) == null||
            exceptionMethodPatterns.get(exception).get(method).get(exceptionMethodCount);
        }
    }

    private boolean takeOverIncludeStackLimited(String exception, int count){
        if (stackMaxrepeat.get(exception) == -1){
            return true;
        }
        return stackMaxrepeat.get(exception) >= count;
    }

    private boolean takeOverUsingGeneralPattern(int count) {
        return generalPattern.get(count);
    }

    private synchronized int nextCount() {
        return callCount++;
    }

    private synchronized int nextExceptionCount() {
        return exceptionCallCount++;
    }

    private synchronized void resetCount() {
        callCount = 0;
        exceptionCallCount = 0;
    }

    @Override
    public synchronized boolean takeOver(String method, int count, Throwable tr) {
        if (limit != UNLIMITED && count > limit) {
            return false;
        }
        if (generalPattern != null) {
            if (exclude) {
                return takeOverExclude(method, count, tr)
                    && takeOverUsingGeneralPattern(nextCount());
            } else {
                return takeOverUsingGeneralPattern(nextCount());
            }
        }

        if (!exclude) {
            return takeOverInclude(method, count, tr) && takeOverUsingMethodPattern(method, count);
        } else {
            return takeOverExclude(method, count, tr);
        }
    }

    public synchronized boolean takeOver(
        String method, int count, Throwable tr, int exceptioncount) {
        if (exceptions != null) {
            if (limit != UNLIMITED && exceptioncount > limit) {
                return false;
            }
            return takeOverIncludeException(tr, exceptioncount, method)
                && takeOverUsingExceptionPattern(tr.getClass().getName(), exceptioncount,method);
        } else {
            return takeOver(method, count, tr);
        }
    }

    public synchronized boolean isFilter(String method, Throwable tr){
        if(filterMethods != null){
            if(filterMethods.keySet().contains(method)){
                //LOG.d(LogProxy.LOG_TAG,"fmethode:"+fmethod+"  method:"+method);
                if(filterMethods.get(method).isEmpty()) return true;
                for(String keyword : filterMethods.get(method)){
                    for(StackTraceElement element : tr.getStackTrace()){
                        if(element.toString().contains(keyword)){
                            LOG.d(LogProxy.LOG_TAG,"stack is: " + element.toString());
                            return true;
                        }
                    }
                }
            }
        }

        if(filterStackKeyWords != null){
            for(String keywords : filterStackKeyWords){
                for(StackTraceElement element: tr.getStackTrace()){
                    if(element.toString().contains(keywords)){
                        LOG.d(LogProxy.LOG_TAG,"stack is : " + element.toString());
                        return true;
                    }
                }
            }
        }

        String classname = getClassName(method);
        if(filterClasses != null){
            //LOG.d(LogProxy.LOG_TAG,"fclass:"+classname);
            if(filterClasses.keySet().contains(classname)){
                if(filterClasses.get(classname).isEmpty()) return true;
                for(String keyword : filterClasses.get(classname)){
                    for(StackTraceElement element : tr.getStackTrace()){
                        if(element.toString().contains(keyword)){
                            LOG.d(LogProxy.LOG_TAG,"stack is: " + element.toString());
                            return true;
                        }
                    }
                }
            }
        }
        String packagename = getPackageName(classname);
        if(filterPackages != null){
            //LOG.d(LogProxy.LOG_TAG,"package:"+packagename);
            if(filterPackages.keySet().contains(packagename)){
                //LOG.d(LogProxy.LOG_TAG,"find package!");
                if(filterPackages.get(packagename).isEmpty()) return true;
                for(String keyword : filterPackages.get(packagename)){
                    for(StackTraceElement element : tr.getStackTrace()){
                        if(element.toString().contains(keyword)) {
                            LOG.d(LogProxy.LOG_TAG,"stack is: " + element.toString());
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
    @Override
    public void onTestChanged(String testMethod) {
        resetCount();
    }

    public synchronized void setMethodPattern(String method, BitSet pattern) {
        if (methodPatterns == null) {
            methodPatterns = new HashMap<>();
        }
        methodPatterns.put(method, pattern);
    }

    public synchronized void setExceptionPattern(String exception, BitSet pattern) {
        if (exceptionPatterns == null) {
            exceptionPatterns = new HashMap<>();
        }
        exceptionPatterns.put(exception, pattern);
    }

    public synchronized void setExceptionMethodPatterns(String exception, String method, String pattern){
        if(exceptionMethodPatterns == null){
            exceptionMethodPatterns = new HashMap<>();
        }
        exceptionMethodPatterns.put(exception,new HashMap<String, BitSet>());
        if(method!=null){
            if(pattern !=null){
                exceptionMethodPatterns.get(exception).put(method,convertStrToBitSet(pattern));
            }else{
                exceptionMethodPatterns.get(exception).put(method,null);
            }
        }

    }

    private static BitSet convertStrToBitSet(String patternStr) {
        if (patternStr == null) {
            return null;
        }

        BitSet bitSet = new BitSet();
        for (int i = 0; i < patternStr.length(); ++i) {
            char c = patternStr.charAt(i);
            if (c == '1') {
                bitSet.set(i);
            }
        }
        return bitSet;
    }

    public synchronized void setMethodPattern(String method, String patternStr) {
        setMethodPattern(method, convertStrToBitSet(patternStr));
    }

    public synchronized void setExceptionPattern(String exception, String patternStr) {
        setExceptionPattern(exception, convertStrToBitSet(patternStr));
    }

    public synchronized void setGeneralPattern(BitSet generalPattern) {
        this.generalPattern = generalPattern;
    }

    public synchronized void setGeneralPattern(String generalPatternStr) {
        setGeneralPattern(convertStrToBitSet(generalPatternStr));
    }
    public synchronized void setStackMaxrepeat(String exception, String stackmaxrepeat){
        if(this.stackMaxrepeat == null){
            this.stackMaxrepeat = new HashMap<>();
        }
        this.stackMaxrepeat.put(exception,Integer.parseInt(stackmaxrepeat));
    }
    public synchronized int getStackMaxrepeat(String exception){
        if(this.stackMaxrepeat == null) return -1;
        if(this.stackMaxrepeat.containsKey(exception)) {
            return this.stackMaxrepeat.get(exception);
        }
        return -1;
    }

    public synchronized void reset() {
        limit = 0;
        callCount = 0;
        exclude = false;
        generalPattern = null;
        methodPatterns = null;
        packages = null;
        classes = null;
        methods = null;
        stackMaxrepeat = null;
        exceptionMethodPatterns = null;
        exceptionCallCount = 0;
        filterMethods = null;
        filterClasses = null;
        filterPackages = null;
        filterStackKeyWords = null;
    }
}
