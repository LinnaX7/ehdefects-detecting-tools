package fixeh.scanner.trick;

/**
 * Created by Shunjie Ding on 09/01/2018.
 */
public interface Trick<T, R> {
    R apply(T target) throws Exception;

    /**
     * Priority for auto apply tricks, smaller is better, commonly in 1 - 100
     * @return priority number
     */
    int priority();

    default String name() {
        return this.getClass().getSimpleName();
    }
}
