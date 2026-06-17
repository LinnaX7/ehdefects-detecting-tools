package fixeh.instrument.woventools.policy;
import fixeh.instrument.woventools.Log;
import fixeh.instrument.woventools.LogProxy;

import java.io.StringWriter;
import java.util.*;

/**
 * Created by Lu Lu on 25/12/2019.
 */

// only for include mode
public class newGeneralControlPolicy implements ControlPolicy {
    private static final Log LOG = LogProxy.getInstance();
    private static final int UNLIMITED = -1;
    boolean filterPackages = false;
    boolean filterExceptions = false;
    boolean filterStackKeyWords = false;
    boolean filterInvocations = false;
    boolean filterClasses = false;
    // package name , stack = "null" or stacktrace
    private Map<String, Set<String>> activePackages = null;
    // filter package name , stack = "null" or stacktrace
    private Map<String, Set<String>> activeClasses = null;
    // filter classes name , stack = "null" or stacktrace
    private Map<String, Set<String>> activeInvocations = null;
    // filter package name , stack = "null" or stacktrace
    private Map<String,Set<String>> activeExceptions = null;
    private Set<String> activeStackKeyWords = null;

    private Map<String, BitSet> methodsPatterns = null;
    private BitSet generalPattern = null;

    private volatile boolean exclude = false;
    private volatile int limit = 0;
    // total count;
    private volatile int callCount = 0;

    private static String boolToString(boolean flag){
        return flag ? "true" : "false";
    }

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

   /* private static <K,T,V> String DmapToString(Map<T,Map<K,V>> dMap) {
        if(dMap == null){
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Iterator<Map.Entry<T,Map<K,V>>> fatherIT = dMap.entrySet().iterator();
        while(fatherIT.hasNext()){
            Map.Entry<T, Map<K,V>> fatherOBJ = fatherIT.next();
            sb.append(String.format("{%s:%s}",fatherOBJ.getKey(),mapToString(fatherOBJ.getValue())));
            if(fatherIT.hasNext()){
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }*/

    private static <K,T> String SmapToString(Map<T,Set<K>> sMap){
        if(sMap == null){
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Iterator<Map.Entry<T,Set<K>>> it = sMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<T,Set<K>> obj = it.next();
            sb.append(String.format("{%s:%s}",obj.getKey(),setToString(obj.getValue())));
            if(it.hasNext()){
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
        String policyFormat = "GeneralControlPolicy {limit: %d, exclude: %s, " +
                "activepackage: %s, filtermode: %s "+
                "activeClass: %s, filtermode: %s, " +
                "activeInvocation: %s, filtermode: %s, " +
                "activeException: %s, filtermode: %s," +
                "activeStack: %s, filtermode: %s, " +
                "generalMethodPattern: %s }";
        String output = String.format(policyFormat, limit, boolToString(exclude),
                SmapToString(activePackages), boolToString(filterPackages),
                SmapToString(activeClasses), boolToString(filterClasses),
                SmapToString(activeInvocations), boolToString(filterInvocations),
                SmapToString(activeExceptions), boolToString(filterExceptions),
                setToString(activeStackKeyWords), boolToString(filterStackKeyWords),
                mapToString(methodsPatterns));
        return output;
    }

    public void setFilterPackage(boolean flag){
        filterPackages = flag;
    }
    public void setFilterClasses(boolean flag){
        filterClasses = flag;
    }
    public void setFilterInvocations(boolean flag){
        filterInvocations = flag;
    }
    public void setFilterExceptions(boolean flag){
        filterExceptions = flag;
    }
    public void setFilterStackKeyWords(boolean flag){
        filterStackKeyWords = flag;
    }

    public synchronized void setActivePackages(String fpackage,String stackKeyWord){
        if(fpackage == null) return;
        if(activePackages == null)
            activePackages = new HashMap<>();
        if(!activePackages.containsKey(fpackage)){
            activePackages.put(fpackage, null);
        }
        if(stackKeyWord == null){
            return;
        }
        if(activePackages.get(fpackage) == null)
            activePackages.put(fpackage, new HashSet<String>());
        activePackages.get(fpackage).add(stackKeyWord);

    }

    public synchronized void setActiveClasses(String fclass, String stackKeyWord){
        if(fclass == null) return;
        if(activeClasses == null)
            activeClasses = new HashMap<>();
        if(!activeClasses.containsKey(fclass)){
            activeClasses.put(fclass, null);
        }
        if(stackKeyWord == null){
            return;
        }
        if(activeClasses.get(fclass) == null)
            activeClasses.put(fclass, new HashSet<String>());
        activeClasses.get(fclass).add(stackKeyWord);

    }

    public synchronized void setActiveInvocations(String fmethod, String stackKeyWord){
        if(fmethod == null) return;
        if(activeInvocations == null)
            activeInvocations = new HashMap<>();
        if(!activeInvocations.containsKey(fmethod)){
            activeInvocations.put(fmethod, null);
        }
        if(stackKeyWord == null){
            return;
        }
        if(activeInvocations.get(fmethod) == null)
            activeInvocations.put(fmethod, new HashSet<String>());
        activeInvocations.get(fmethod).add(stackKeyWord);

    }

    public synchronized void setActiveExceptions(String fexception, String stackKeyWord){
        if(fexception == null) return;
        if(activeExceptions == null)
            activeExceptions= new HashMap<>();
        if(!activeExceptions.containsKey(fexception)){
            activeExceptions.put(fexception, null);
        }
        if(stackKeyWord == null){
            return;
        }
        if(activeExceptions.get(fexception) == null)
            activeExceptions.put(fexception, new HashSet<String>());
        activeExceptions.get(fexception).add(stackKeyWord);

    }

    public synchronized void setActiveStackKeyWords(String fstackKeyWords){
        if(fstackKeyWords == null)
            return;

        if(activeStackKeyWords == null){
            activeStackKeyWords= new HashSet<>();
        }
        activeStackKeyWords.add(fstackKeyWords);
    }

    public void setInvocationPatterns(String method, String pattern) {
        if(method == null)
            return;
        if (methodsPatterns == null) {
            methodsPatterns = new HashMap<>();
        }
        if(pattern == null){
            methodsPatterns.put(method,null);
            return;
        }
        methodsPatterns.put(method, convertStrToBitSet(pattern));
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

    public boolean takeOverExclude(String method, int count, Throwable tr) {
        String className = getClassName(method);

        // Avoid using stream(). It introduces dynamic invoke instruction which soot currently not
        // supports.

        if (methodsPatterns != null && methodsPatterns.containsKey(method)) {
            return false;
        }

        return true;
    }

    private boolean takeOverUsingGeneralPattern(int count) {
        return generalPattern.get(count);
    }

    private synchronized int nextCount() {
        return callCount++;
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
            return takeOverInclude(method, tr) && takeOverUsingMethodPattern(method, count);
        } else {
            return takeOverExclude(method, count, tr);
        }
    }

   /* public synchronized boolean takeOver(String method, int count, Throwable tr, int exceptioncount) {
        if (limit != UNLIMITED) {
            return false;
        }
            return takeOverIncludeException(tr, exceptioncount, method)
                    && takeOverUsingExceptionPattern(tr.getClass().getName(), exceptioncount,method);
        } else {
            return takeOver(method, count, tr);
        }
    }*/

    private boolean takeOverInclude(String method, Throwable tr){
        String className = getClassName(method);
        String packageName = getPackageName(className);
        String exceptionName = tr.getClass().getName();
        boolean included = false;
        boolean flag = false;
        // exception && stackTrace have different way to conduct;
        // Include in package

        if(activePackages != null && activePackages.containsKey(packageName)){
            flag = false;
            if(activePackages.get(packageName) == null){
                flag = true;
            }else {
                for (String estr : activePackages.get(packageName)) {
                    if (sameStackTrace(estr, tr)) {
                        flag = true;
                        break;
                    }
                }
            }
            if(flag){
                if(filterPackages)
                    return false;
                included = true;
            }
        }else{
            if(!filterPackages && activePackages != null)
                return false;
        }
        // Include in class

        if(activeClasses != null && activeClasses.containsKey(className)){
            flag = false;
            if(activeClasses.get(className) == null){
                flag = true;
            }else {
                for (String estr : activeClasses.get(className)) {
                    if (sameStackTrace(estr, tr)) {
                        flag = true;
                        break;
                    }
                }
            }
            if(flag){
                if(filterClasses)
                    return false;
                included = true;
            }
        }else{
            if(!filterClasses && activeClasses != null)
                return false;
        }
        // Include in invocation
        if(activeInvocations != null && activeInvocations.containsKey(method)){
            flag = false;
            if(activeInvocations.get(method) == null){
                flag = true;
            }else {
                for (String estr : activeInvocations.get(method)) {
                    if (sameStackTrace(estr, tr)) {
                        //LOG.i(LogProxy.LOG_TAG,"you are right!!");
                        flag = true;
                        break;
                    }
                }
            }
            if(flag){
                if(filterInvocations)
                    return false;
                included = true;
            }
        }else{
            if(!filterInvocations && activeInvocations != null)
                return false;
        }
        // Include in Exception
        if(activeExceptions != null && activeExceptions.containsKey(exceptionName)){
            flag = false;
            if(activeExceptions.get(exceptionName) == null){
                flag = true;
            }else {
                for (String estr : activeExceptions.get(exceptionName)) {
                    if (sameStackTrace(estr, tr)) {
                        flag = true;
                        break;
                    }
                }
            }
            if(flag){
                if(filterExceptions)
                    return false;
                included = true;
            }
        }else{
            if(!filterExceptions && activeExceptions != null){
                return false;
            }
        }
        // Include in Stack
        if(activeStackKeyWords != null){
            flag = false;
            for(String estr : activeStackKeyWords){
                if(sameStackTrace(estr, tr)){
                    flag = true;
                    break;
                }
            }
            if(flag){
                if(filterStackKeyWords)
                    return false;
                included = true;
            }
        }else{
            if(!filterStackKeyWords && activeStackKeyWords != null){
                return false;
            }
        }
        //LOG.i(LogProxy.LOG_TAG, "producing:include:" + boolToString(included));
        return included;
    }

    private boolean takeOverUsingMethodPattern(String method, int count){
        if(methodsPatterns == null) return true;
        if(methodsPatterns.containsKey(method)){
            return methodsPatterns.get(method) == null ? true : methodsPatterns.get(method).get(count);
        }
        return true;
    }


     public static boolean sameStackTrace(String model,Throwable tr){
        for(String estr: model.split("@@")){
            boolean flag = false;
            for(StackTraceElement element: tr.getStackTrace()){
                //LOG.i("lulutest",element.toString() + estr.trim());
                if(element.toString().contains(estr.trim())){
                    flag = true;
                    break;
                }
            }
            if(!flag)
                return false;
        }
        return true;
    }

    @Override
    public void onTestChanged(String testMethod) {
        resetCount();
    }

    private synchronized void resetCount() {
        callCount = 0;
    }

   /*public synchronized void setExceptionPattern(String exception, BitSet pattern) {
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

    }*/

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


    public synchronized void setGeneralPattern(BitSet generalPattern) {
        this.generalPattern = generalPattern;
    }

    public synchronized void setGeneralPattern(String generalPatternStr) {
        setGeneralPattern(convertStrToBitSet(generalPatternStr));
    }

    /*
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
     */

    public synchronized void reset() {
        limit = 0;
        callCount = 0;
        exclude = false;
        generalPattern = null;
        activePackages = null;
        activeClasses = null;
        activeInvocations = null;
        activeExceptions = null;
        activeStackKeyWords = null;
        methodsPatterns = null;
        filterExceptions = false;
        filterInvocations = false;
        filterClasses = false;
        filterPackages = false;
        filterStackKeyWords = false;
    }
}
