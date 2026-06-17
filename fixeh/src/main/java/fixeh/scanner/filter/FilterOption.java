package fixeh.scanner.filter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Shunjie Ding on 19/12/2017.
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FilterOption {
    /**
     * Defines which stage this filter will be applied.
     * Currently there are only two filter levels supported: commit, class.
     */
    String stage();
}
