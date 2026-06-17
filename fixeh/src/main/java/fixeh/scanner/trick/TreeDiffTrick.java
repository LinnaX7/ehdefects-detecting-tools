package fixeh.scanner.trick;

import fixeh.scanner.treediff.TreeDiff;

/**
 * Created by Shunjie Ding on 09/01/2018.
 */
public interface TreeDiffTrick extends RecursiveTrick<TreeDiff> {
    /**
     * Apply this trick to treeDiff and returns a new diff after trick applied.
     * @param treeDiff treeDiff to apply
     * @return new treeDiff
     * @throws Exception when any exception occurs and we can't recover
     */
    @Override
    TreeDiff apply(TreeDiff treeDiff) throws Exception;
}
