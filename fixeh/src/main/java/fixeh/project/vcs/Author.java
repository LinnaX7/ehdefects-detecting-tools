package fixeh.project.vcs;

import java.io.Serializable;

/**
 * Created by Shunjie Ding on 21/12/2017.
 */
public final class Author implements Serializable {
    private String name;

    private String email;

    private int timeZone;

    protected Author() {}

    public Author(String name, String email, int timeZone) {
        if (name == null || email == null) {
            throw new IllegalArgumentException("Author's name and email must not be null.");
        }
        this.name = name;
        this.email = email;
        this.timeZone = timeZone;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Author) {
            Author author = (Author) obj;
            return name.equals(author.name) && email.equals(author.email)
                && timeZone == author.timeZone;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode() ^ email.hashCode() + timeZone;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getTimeZone() {
        return timeZone;
    }
}
