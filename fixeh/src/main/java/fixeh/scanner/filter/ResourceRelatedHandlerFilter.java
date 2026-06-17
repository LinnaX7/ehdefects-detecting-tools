package fixeh.scanner.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import fixeh.Constants;
import fixeh.scanner.Context;
import fixeh.scanner.feature.HandlerChange;

/**
 * Created by Shunjie Ding on 15/01/2018.
 */
public class ResourceRelatedHandlerFilter extends Filter<HandlerChange.CatchHandler> {
    // Default using packages in Constants.RESOURCE_RELATED_PACKAGES
    private final List<String> resourceRelatedPackages;

    private final List<String> resourceRelatedExceptions;

    public ResourceRelatedHandlerFilter(
        List<String> resourceRelatedPackages, List<String> resourceRelatedExceptions) {
        this.resourceRelatedExceptions =
            resourceRelatedExceptions == null ? new ArrayList<>() : resourceRelatedExceptions;

        // Add "." to the end of packages
        if (resourceRelatedPackages != null) {
            resourceRelatedPackages = resourceRelatedPackages.stream()
                                          .map(p -> p.endsWith(".") ? p : p + ".")
                                          .collect(Collectors.toList());
        }

        this.resourceRelatedPackages =
            resourceRelatedPackages == null ? new ArrayList<>() : resourceRelatedPackages;
    }

    public ResourceRelatedHandlerFilter(List<String> resourceRelatedExceptions) {
        this(resourceRelatedExceptions, null);
    }

    public ResourceRelatedHandlerFilter() {
        this(null, Arrays.asList(Constants.RESOURCE_RELATED_PACKAGES));
    }

    private boolean isResourceRelated(String s) {
        return resourceRelatedExceptions.stream().anyMatch(s::equals)
            || resourceRelatedPackages.stream().anyMatch(s::startsWith);
    }

    @Override
    public boolean accept(Context ctx, HandlerChange.CatchHandler target) throws Exception {
        return target.getExceptions().stream().anyMatch(this ::isResourceRelated);
    }
}
