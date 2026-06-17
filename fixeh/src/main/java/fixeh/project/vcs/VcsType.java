package fixeh.project.vcs;

import java.util.Arrays;
import java.util.List;
import fixeh.project.vcs.exceptions.UnsupportedVcsException;

/**
 * Created by Shunjie Ding on 19/12/2017.
 */
public enum VcsType {
    GIT,
    MERCURIAL,
    SVN;

    public static VcsType toVcsType(String type) throws UnsupportedVcsException {
        List<VcsType> types = Arrays.asList(values());
        for (VcsType t : types) {
            if (t.name().equals(type.toUpperCase())) {
                return t;
            }
        }
        throw new UnsupportedVcsException(
            "Could not get VCS type " + type + ", currently unsupported!");
    }
}
