package fixeh.project.vcs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Shunjie Ding on 21/12/2017.
 */
public final class Revision implements Serializable {
    private String id;

    private Date commitTime;

    private String message;

    private Author author;

    private List<String> parentIds;

    private transient Vcs vcs;

    protected Revision() {}

    public Revision(Vcs vcs, String id, Date commitTime, String message, Author author,
        List<String> parentIds) {
        if (id == null) {
            throw new IllegalArgumentException("Revision Id must not be null.");
        }
        if (commitTime == null) {
            throw new IllegalArgumentException("Revision commit time must not be null.");
        }
        if (message == null) {
            throw new IllegalArgumentException("Revision message must not be null.");
        }
        if (author == null) {
            throw new IllegalArgumentException("Revision author must not be null");
        }

        this.vcs = vcs;
        this.id = id;
        this.commitTime = commitTime;
        this.message = message;
        this.author = author;
        this.parentIds = parentIds;
        // Make sure parent ids is not null
        if (parentIds == null) {
            parentIds = new ArrayList<>();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Revision) {
            Revision r = (Revision) obj;
            return id.equals(r.id) && commitTime.equals(r.commitTime) && message.equals(r.message)
                && author.equals(r.author) && parentIds.equals(r.parentIds);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String getId() {
        return id;
    }

    public String getAbbreviatedId(int length) {
        return id.substring(0, length);
    }

    public Date getCommitTime() {
        return commitTime;
    }

    public String getMessage() {
        return message;
    }

    public String getShortMessage() {
        try (BufferedReader reader = new BufferedReader(new StringReader(message))) {
            String line = reader.readLine();
            // Trim those not alphanumeric tailing characters
            int l = line.length() - 1;
            while (l >= 0 && !Character.isLetterOrDigit(line.codePointAt(l))) {
                --l;
            }
            return line.substring(0, l + 1);
        } catch (IOException e) {
            // ignore
        }
        return "";
    }

    public Author getAuthor() {
        return author;
    }

    public Vcs getVcs() {
        return vcs;
    }

    public int parentCounts() {
        return parentIds == null ? 0 : parentIds.size();
    }

    public List<String> getParentIds() {
        return parentIds;
    }
}
