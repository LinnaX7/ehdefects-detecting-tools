package fixeh.scanner.filter;

import fixeh.scanner.Context;

/**
 * Created by Shunjie Ding on 09/01/2018.
 */
public abstract class RejectFilter<T> extends Filter<T> {
    public abstract boolean reject(Context ctx, T target);

    @Override
    public boolean accept(Context ctx, T target) {
        return !reject(ctx, target);
    }
}
