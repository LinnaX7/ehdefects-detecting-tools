package fixeh.scanner.filter;

import java.util.List;
import fixeh.project.vcs.Revision;

/**
 * Created by Shunjie Ding on 21/12/2017.
 */
@FilterOption(stage = "commit")
public class RevisionMessageFilter extends MessageFilter<Revision> {
    public RevisionMessageFilter(List<String> keywords) {
        super(keywords);
    }

    public RevisionMessageFilter(String[] keywords) {
        super(keywords);
    }

    @Override
    protected String targetMessage(Revision target) {
        return target.getMessage();
    }
}
