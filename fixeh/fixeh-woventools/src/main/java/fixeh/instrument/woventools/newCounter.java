package fixeh.instrument.woventools;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Lulu 2020.02.27
 */
public final class newCounter implements Counter{
    private static final HashMap<String, Integer> countMap = new HashMap<>();
    private static final Log LOG = LogProxy.getInstance();

    @Override
    public synchronized int getCount(String signature)  {
        if (!countMap.containsKey(signature)) {
            countMap.put(signature, 0);
        }
        return countMap.get(signature);
    }

    public synchronized int increase(String signature) {
        return countMap.put(signature, getCount(signature) + 1);
    }


    public synchronized void reset() {
        countMap.clear();

    }
}
