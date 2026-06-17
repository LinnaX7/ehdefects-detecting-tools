package fixeh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import ch.qos.logback.classic.Level;
import fixeh.project.Project;
import fixeh.project.vcs.VcsType;
import fixeh.project.vcs.exceptions.UnsupportedVcsException;
import fixeh.util.LoggerUtils;

/**
 * Created by Shunjie Ding on 08/01/2018.
 */
public class TestBase {
    protected static final String REMOTE_PROJECT_REPO_URL =
        "git@bitbucket.org:sjdresearch/fixeh-testpro.git";
    protected static final String TEST_REPO_NAME = "fixeh-testpro";
    protected static final String ID_RSA_PASSWD = System.getenv("ID_RSA_PASSWD");
    private final Logger logger = LoggerFactory.getLogger(TestBase.class);
    protected Project project;

    public TestBase() {
        LoggerUtils.setLogLevel(Level.INFO);
        try {
            project = initProject();
        } catch (GitAPIException e) {
            // Crash here!
            throw new RuntimeException(e);
        }
    }

    protected void cloneRemoteRepo(String URI) throws GitAPIException {
        // Clone the test repo to local
        Git.cloneRepository()
            .setURI(REMOTE_PROJECT_REPO_URL)
            .setDirectory(new File(Constants.TMPDIR, TEST_REPO_NAME))
            .setBranch("master")
            .setCloneAllBranches(true)
            .setTransportConfigCallback(transport
                -> ((SshTransport) transport).setSshSessionFactory(new JschConfigSessionFactory() {
                @Override
                protected JSch createDefaultJSch(FS fs) throws JSchException {
                    JSch defaultJSch = super.createDefaultJSch(fs);
                    // Set password for id_rsa private key if necessary
                    if (ID_RSA_PASSWD != null && !ID_RSA_PASSWD.isEmpty()) {
                        defaultJSch.addIdentity(
                            Paths.get(fs.userHome().getAbsolutePath(), ".ssh/id_rsa").toString(),
                            ID_RSA_PASSWD);
                    }
                    return defaultJSch;
                }

                @Override
                protected void configure(OpenSshConfig.Host hc, Session session) {
                    // empty
                }
            }))
            .call();
    }

    protected Project initProject() throws GitAPIException {
        File destFile = new File(Constants.TMPDIR, TEST_REPO_NAME);
        if (!destFile.isDirectory()) {
            logger.info("Cloning remote repo to " + destFile.getAbsolutePath());
            try {
                cloneRemoteRepo(REMOTE_PROJECT_REPO_URL);
            } catch (GitAPIException e) {
                logger.info(
                    "Please check if your ssh private key has password and environment variable ID_RSA_PASSWD is set correctly!");
                // Delete the empty dir on exit
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        if (destFile.exists()) {
                            FileUtils.forceDelete(destFile);
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }));
                throw e;
            }
        }
        logger.info("Initializing git using local repo " + destFile.getAbsolutePath());
        try {
            return new Project(TEST_REPO_NAME, destFile.getAbsolutePath(), VcsType.GIT, false);
        } catch (UnsupportedVcsException e) {
            // ignore
        }
        return null;
    }
}
