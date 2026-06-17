package fixeh.scanner.filter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import fixeh.scanner.Context;

/**
 * Created by Shunjie Ding on 19/12/2017.
 */
public abstract class MessageFilter<T> extends Filter<T> {
    protected final List<String> keywords;

    public MessageFilter(List<String> keywords) {
        this.keywords = keywords;
    }

    public MessageFilter(String[] keywords) {
        this.keywords = Arrays.asList(keywords);
    }

    /**
     * Accepts if any of the keywords is found in target string.
     * @param target target string.
     * @return true if accepts and false otherwise.
     * @throws Exception any exception occurred when filter works.
     */
    public boolean accept(Context ctx, String target) throws Exception {
        String lowerCaseTarget = target.toLowerCase();
        return keywords.stream().anyMatch(lowerCaseTarget::contains);
    }

    @Override
    public boolean accept(Context ctx, T target) throws Exception {
        return accept(ctx, targetMessage(target));
    }

    protected abstract String targetMessage(T target);

    @Override
    public String toString() {
        return String.format(
            "<%s(keywords = [%s])>", name(), keywords.stream().collect(Collectors.joining(", ")));
    }
}
