package fixeh.scanner.trick;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Shunjie Ding on 09/01/2018.
 */
public class RecursiveTrickApplier<T> {
    private final Logger logger = LoggerFactory.getLogger(RecursiveTrickApplier.class);

    private final List<RecursiveTrick<T>> tricks;

    public RecursiveTrickApplier(Set<RecursiveTrick<T>> tricks) {
        this.tricks = tricks.stream()
                          .sorted(Comparator.comparingInt(Trick::priority))
                          .collect(Collectors.toList());

        logger.info("These tricks are going to be applied: {}",
            tricks.stream()
                .map(trick -> String.format("%s(%d)", trick.name(), trick.priority()))
                .collect(Collectors.joining(", ")));
    }

    public T applyAll(T target) {
        for (RecursiveTrick<T> trick : tricks) {
            try {
                target = trick.apply(target);
            } catch (Exception e) {
                logger.error(
                    "Exception occurs when applying trick %s! %s", trick.name(), e.getMessage());
                e.printStackTrace();
            }
        }

        return target;
    }
}
