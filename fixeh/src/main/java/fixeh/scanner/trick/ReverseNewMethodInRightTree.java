package fixeh.scanner.trick;

import fixeh.scanner.treediff.TreeDiff;

/**
 * Created by Shunjie Ding on 09/01/2018.
 */
public class ReverseNewMethodInRightTree implements TreeDiffTrick {
    @Override
    public TreeDiff apply(TreeDiff treeDiff) throws Exception {
        // TODO
        return treeDiff;
    }

    @Override
    public int priority() {
        return 1;
    }
}
