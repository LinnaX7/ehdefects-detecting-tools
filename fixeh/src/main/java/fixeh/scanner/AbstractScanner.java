package fixeh.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Created by Shunjie Ding on 19/12/2017.
 */
public abstract class AbstractScanner<T, R> implements Callable<R> {
    private final Logger logger = LoggerFactory.getLogger(AbstractScanner.class);

    private final T target;

    public AbstractScanner(T target) {
        this.target = target;
    }

    protected T getTarget() {
        return target;
    }

    /**
     * Scan target and return the result.
     * @param target target to scan.
     * @return results.
     * @throws Exception if any exception occurs.
     */
    protected abstract R scan(T target) throws Exception;

    public R scan() throws Exception {
        return scan(target);
    }

    @Override
    public R call() {
        try {
            return scan();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
