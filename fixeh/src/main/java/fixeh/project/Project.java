package fixeh.project;

import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import fixeh.project.vcs.GitWrapper;
import fixeh.project.vcs.Vcs;
import fixeh.project.vcs.VcsType;
import fixeh.project.vcs.exceptions.UnsupportedVcsException;
import fixeh.project.vcs.exceptions.VcsNotFoundException;

/**
 * Created by Shunjie Ding on 19/12/2017.
 */
public class Project {
    private final String name;

    private final String path;

    private final Vcs vcs;

    private Project(String name, String path, Vcs vcs) {
        if (name == null || path == null || vcs == null) {
            throw new IllegalArgumentException("Project must have name, path and VCS!");
        }
        this.name = name;
        this.path = path;
        this.vcs = vcs;
    }

    public Project(String name, String path, VcsType vcsType, boolean bare)
        throws UnsupportedVcsException {
        this(name, path, getVcs(vcsType, path, bare));
    }

    public Project(String path, VcsType vcsType, boolean bare) throws UnsupportedVcsException {
        this(getFileName(path), path, getVcs(vcsType, path, bare));
    }

    public Project(String name, String path) throws VcsNotFoundException {
        this(name, path, getVcs(path));
    }

    public Project(String path) throws VcsNotFoundException {
        this(getFileName(path), path, getVcs(path));
    }

    private static String getFileName(String path) {
        return Paths.get(path).getFileName().toString();
    }

    private static Pair<VcsType, Boolean> detectVcs(String path) throws VcsNotFoundException {
        // Check if it there is git repo.
        File gitDir = new File(path, ".git");
        if (gitDir.isDirectory()) {
            return Pair.of(VcsType.GIT, false);
        }

        // Check if it is a isBare git dir
        final String[] regularFiles = new String[] {"HEAD", "config", "description"};
        final String[] dirs = new String[] {"hooks", "info", "objects", "refs"};

        if (Arrays.stream(regularFiles).allMatch(f -> new File(path, f).isFile())
            && Arrays.stream(dirs).allMatch(f -> new File(path, f).isDirectory())) {
            return Pair.of(VcsType.GIT, true);
        }

        throw new VcsNotFoundException("Can not find git directory under project " + path);
    }

    private static Vcs getVcs(String path) throws VcsNotFoundException {
        Pair<VcsType, Boolean> vcsInfo = detectVcs(path);
        try {
            return getVcs(vcsInfo.getKey(), path, vcsInfo.getRight());
        } catch (UnsupportedVcsException e) {
            // ignore
        }
        return null;
    }

    private static Vcs getVcs(VcsType vcsType, String path, boolean bare)
        throws UnsupportedVcsException {
        try {
            switch (vcsType) {
                case GIT:
                    return new GitWrapper(bare ? path : path + "/.git", bare);
                default:
                    break;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // FIXME Currently not supported
        throw new UnsupportedVcsException("Not supported yet!");
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Vcs getVcs() {
        return vcs;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Project) {
            Project p = (Project) obj;
            return name.equals(p.name) && path.equals(p.path);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 5 + path.hashCode() * 7;
    }
}
