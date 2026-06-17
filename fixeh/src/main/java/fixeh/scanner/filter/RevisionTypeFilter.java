package fixeh.scanner.filter;

import fixeh.project.vcs.Revision;
import fixeh.scanner.Context;

/**
 * Created by Shunjie Ding on 21/12/2017.
 */
@FilterOption(stage = "commit")
public class RevisionTypeFilter extends Filter<Revision> {
    /**
     * Accepts if revision is not a merge.
     * @param target target revision.
     * @return true if accepts and false otherwise.
     * @throws Exception exceptions occurs when filter works.
     */
    @Override
    public boolean accept(Context ctx, Revision target) throws Exception {
        return target.parentCounts() == 1;
    }

    @Override
    public String toString() {
        return String.format("<%s(accepts commits except root and merge)>", name());
    }
}
