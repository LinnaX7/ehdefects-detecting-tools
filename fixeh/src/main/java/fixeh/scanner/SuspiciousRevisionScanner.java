package fixeh.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import fixeh.Constants;
import fixeh.project.Project;
import fixeh.project.vcs.Revision;
import fixeh.project.vcs.Vcs;
import fixeh.scanner.filter.Filter;
import fixeh.scanner.filter.RevisionMessageFilter;
import fixeh.scanner.filter.RevisionTypeFilter;

/**
 * Created by Shunjie Ding on 08/01/2018.
 */
public class SuspiciousRevisionScanner extends AbstractScanner<Project, List<Revision>> {
    private final Logger logger = LoggerFactory.getLogger(SuspiciousRevisionScanner.class);

    private final List<Filter<Revision>> filters;

    public SuspiciousRevisionScanner(Project target, List<Filter<Revision>> filters) {
        super(target);
        this.filters = filters == null ? new ArrayList<>() : filters;
    }

    public static SuspiciousRevisionScanner defaultScanner(Project target) {
        return new SuspiciousRevisionScanner(target,
            Arrays.asList(
                new RevisionTypeFilter(), new RevisionMessageFilter(Constants.MESSAGE_KEYWORDS)));
    }

    @Override
    protected List<Revision> scan(Project target) throws Exception {
        logger.info("Scanning suspicious revisions using filters: {}",
            filters.stream().map(Object::toString).collect(Collectors.joining(", ")));

        Vcs vcs = target.getVcs();

        Context context = new Context();

        // Parallel filter on revisions
        return vcs.getRevisions()
            .parallelStream()
            .filter(revision -> filters.stream().allMatch(filter -> {
                try {
                    return filter.accept(context, revision);
                } catch (Exception e) {
                    return false;
                }
            }))
            .collect(Collectors.toList());
    }
}
