package fixeh.instrument.woventools;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Shunjie Ding on 22/01/2018.
 */
public final class InvocationCounter {
    private static final HashMap<String, Integer> countMap = new HashMap<>();
    private static final HashMap<String, Integer> exceptionCount = new HashMap<>();
    private static final HashMap<String,Map<StackTraceElement[],Integer>> stackMethodCount =new HashMap<>();
    private static final Log LOG = LogProxy.getInstance();


    public synchronized int getCount(String signature) {
        if (!countMap.containsKey(signature)) {
            countMap.put(signature, 0);
        }
        return countMap.get(signature);
    }
    public synchronized int getExceptionCount(String exception) {
        if (!exceptionCount.containsKey(exception)) {
            exceptionCount.put(exception, 0);
        }
        return exceptionCount.get(exception);
    }



    public synchronized int increase(String signature) {
        return countMap.put(signature, getCount(signature) + 1);
    }

    public synchronized int exceptionIncrease(String exception) {
            return exceptionCount.put(exception, getExceptionCount(exception) + 1);

    }

    public synchronized int stackMethodIncrease(String exception,StackTraceElement[] stackTrace){
        int result = -1;
        int length = stackTrace.length;
        if (!stackMethodCount.containsKey(exception)){
            stackMethodCount .put(exception, new ConcurrentHashMap<StackTraceElement[], Integer>());
            stackMethodCount.get(exception).put(stackTrace,1);
        return 1;
        }
        boolean appear = false;
        for(StackTraceElement[] stacktraceElement: stackMethodCount.get(exception).keySet()){
            if(stacktraceElement.length > length){
                stackMethodCount.get(exception).remove(stacktraceElement);
                LOG.i(LogProxy.LOG_TAG,"larger reset "+exception);
                continue;
            }else if(stacktraceElement.length == length) {
                boolean tflag = true;
                if(stacktraceElement[0].getClassName().equals(stackTrace[0].getClassName())
                && stacktraceElement[0].getLineNumber() == stackTrace[0].getLineNumber()
                && stacktraceElement[0].getMethodName().equals(stackTrace[0].getMethodName())
                && stacktraceElement[0].getFileName().equals(stackTrace[0].getFileName())){
                    appear = true;
                }
                for(int i = 1;i<length && tflag;i++){
                    if(stacktraceElement[i].getClassName().equals(stackTrace[i].getClassName())){
                        if(stacktraceElement[i].getLineNumber() == stackTrace[i].getLineNumber()){
                            if(stacktraceElement[i].getMethodName().equals(stackTrace[i].getMethodName())){
                                if(stacktraceElement[i].getFileName().equals(stackTrace[i].getFileName())){
                                    continue;
                                }
                            }

                        }
                    }
                    appear = false;
                    tflag = false;
                }
                if(!tflag){
                    stackMethodCount.get(exception).remove(stacktraceElement);
                    LOG.i(LogProxy.LOG_TAG,"equal reset "+exception);
                }else{
                    if(appear){
                        int val = stackMethodCount.get(exception).get(stacktraceElement);
                        stackMethodCount.get(exception).put(stacktraceElement,val+1);
                        result = val + 1;
                    }
                }
            } else {
                    boolean flag = false;
                    for (StackTraceElement element : stacktraceElement){
                        boolean f = false;
                        for(StackTraceElement el : stackTrace){
                            if(el.getClassName().equals(element.getClassName())){
                                if(el.getFileName().equals(element.getFileName())){
                                    if(el.getMethodName().equals(element.getMethodName())){
                                        if(el.getLineNumber() == element.getLineNumber()){
                                            f = true;
                                        }
                                    }
                                }
                            }
                            if(!f){
                                flag = true;
                                break;
                            }
                        }
                    }
                    if(flag){
                        stackMethodCount.get(exception).remove(stacktraceElement);
                        LOG.i(LogProxy.LOG_TAG,"smaller reset "+exception);
                        continue;
                    }else{
                            int val = stackMethodCount.get(exception).get(stacktraceElement);
                            stackMethodCount.get(exception).put(stacktraceElement,val+1);
                            continue;
                        }
                }
            }
        if(!appear){
            stackMethodCount.get(exception).put(stackTrace,1);
            result = 1;
        }
        return result;

    }


    public synchronized void reset() {
        countMap.clear();
        //exceptionMethodCount.clear();
        stackMethodCount.clear();
        exceptionCount.clear();

    }
}
