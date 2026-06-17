package fixeh.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class SerializeUtils {
    public static void writeObject(File f, Serializable serializable) throws IOException {
        try (ObjectOutput output = new ObjectOutputStream(new FileOutputStream(f))) {
            output.writeObject(serializable);
        }
    }

    public static <T> T readObject(File f, Class<T> clz)
        throws IOException, ClassNotFoundException {
        try (ObjectInput input = new ObjectInputStream(new FileInputStream(f))) {
            return clz.cast(input.readObject());
        }
    }
}
