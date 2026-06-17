package fixeh.scanner;

import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import fixeh.project.vcs.Revision;

/**
 * Created by Shunjie Ding on 30/12/2017.
 */
public class Context {
    private final ConcurrentMap<Pair<Revision, String>, Object> contextStore =
        new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T put(Revision revision, String key, T obj) {
        return (T) contextStore.put(Pair.of(revision, key), obj);
    }

    public <T> T get(Revision revision, String key, Class<T> clz) {
        return clz.cast(contextStore.get(Pair.of(revision, key)));
    }

    public boolean containsKey(Revision revision, String key) {
        return contextStore.containsKey(Pair.of(revision, key));
    }

    public <T> boolean remove(Revision revision, String key, T obj) {
        return contextStore.remove(Pair.of(revision, key), obj);
    }

    public Object remove(Revision revision, String key) {
        return contextStore.remove(Pair.of(revision, key));
    }

    public void clear() {
        contextStore.clear();
    }
}
