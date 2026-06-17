package fixeh.scanner.filter;

import fixeh.scanner.Context;

/**
 * Created by Shunjie Ding on 19/12/2017.
 */
public abstract class Filter<T> {
    public abstract boolean accept(Context ctx, T target) throws Exception;

    @SuppressWarnings("unchecked")
    public String stage() {
        Class clz = this.getClass();
        FilterOption level = (FilterOption) clz.getAnnotation(FilterOption.class);
        if (level == null) {
            throw new RuntimeException(
                "Please specify filter stage using @FilterOption(stage='') for " + clz.getName());
        }
        return level.stage();
    }

    protected String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return name();
    }

    public final Filter<T> reverse() {
        return new Filter<T>() {
            @Override
            public boolean accept(Context ctx, T target) throws Exception {
                return Filter.this.accept(ctx, target);
            }

            @Override
            public String stage() {
                return super.stage();
            }
        };
    }

    public final Filter<T> and(Filter<T> filter) {
        if (filter == null) {
            return this;
        }
        if (!stage().equals(filter.stage())) {
            throw new IllegalStateException(String.format(
                "Stages of filters (%s and %s) combined are not equal!", stage(), filter.stage()));
        }
        return new Filter<T>() {
            @Override
            public boolean accept(Context ctx, T target) throws Exception {
                return Filter.this.accept(ctx, target) && filter.accept(ctx, target);
            }
        };
    }

    public final Filter<T> or(Filter<T> filter) {
        if (filter == null) {
            return this;
        }
        if (!stage().equals(filter.stage())) {
            throw new IllegalStateException(String.format(
                "Stages of filters (%s and %s) combined are not equal!", stage(), filter.stage()));
        }
        return new Filter<T>() {
            @Override
            public boolean accept(Context ctx, T target) throws Exception {
                return Filter.this.accept(ctx, target) || filter.accept(ctx, target);
            }
        };
    }
}
