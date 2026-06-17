package fixeh.util;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import fixeh.Constants;

/**
 * Created by Shunjie Ding on 19/12/2017.
 */
public final class ConcurrentUtils {
    private static ExecutorService executorService =
        ConcurrentUtils.newExecutorService(Constants.NUM_CORES);

    public static ExecutorService defaultExecutor() {
        return executorService;
    }

    public static ExecutorService newExecutorService(int size) {
        if (size > Constants.NUM_CORES) {
            return Executors.newFixedThreadPool(Constants.NUM_CORES);
        } else {
            return Executors.newFixedThreadPool(size);
        }
    }

    public static <T> List<T> waitAndGetAllResults(List<Future<T>> futures) {
        // Wait for all tasks to be done and set the results.
        // If some execution failed, set null at the corresponding position.
        return futures.stream()
            .map(f -> {
                try {
                    return f.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    return null;
                }
            })
            .collect(Collectors.toList());
    }

    public static <T, R> R callOnFutureDone(Future<T> future, Function<T, R> function)
        throws ExecutionException, InterruptedException {
        return function.apply(future.get());
    }

    public static <T, R> List<R> callAsyncOnFuturesDone(
        List<Future<T>> futures, Function<T, R> function) {
        return futures.parallelStream()
            .map(f -> {
                try {
                    return callOnFutureDone(f, function);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            })
            .collect(Collectors.toList());
    }
}
