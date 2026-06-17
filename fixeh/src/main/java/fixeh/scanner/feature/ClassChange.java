package fixeh.scanner.feature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shunjie Ding on 09/01/2018.
 */
public class ClassChange implements Serializable {
    private final String name;
    private final boolean isNew;
    private final boolean isPublic;
    private final List<MethodChange> methodChanges;

    protected ClassChange(
        String name, boolean isNew, boolean isPublic, List<MethodChange> methodChanges) {
        this.name = name;
        this.isNew = isNew;
        this.isPublic = isPublic;
        this.methodChanges = methodChanges == null ? new ArrayList<>(0) : methodChanges;
    }

    public static ClassChange newClassChange(
        String name, boolean isNew, boolean isPublic, List<MethodChange> methodChanges) {
        return new ClassChange(name, isNew, isPublic, methodChanges);
    }

    public String getName() {
        return name;
    }

    public boolean isNew() {
        return isNew;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public List<MethodChange> getMethodChanges() {
        return methodChanges;
    }
}
