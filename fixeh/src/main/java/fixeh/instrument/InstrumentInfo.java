package fixeh.instrument;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import soot.SootClass;
import soot.SootMethod;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Shunjie Ding on 2018/5/22.
 */
public final class InstrumentInfo {
    private String callee;
    private String caller;
    private Set<String> checkedExceptions;
    private Set<String> uncheckedExceptions;
    private String location;

    public InstrumentInfo(String callee, String caller, Set<String> checkedExceptions,
        Set<String> uncheckedExceptions, String location) {
        this.callee = callee;
        this.caller = caller;
        this.checkedExceptions = checkedExceptions;
        this.uncheckedExceptions = uncheckedExceptions;
        this.location = location;
    }

    static InstrumentInfo newInstrumentInfo(SootMethod callee, SootMethod caller,
        Set<String> uncheckedExceptions, String className, int lineNumber) {
        return new InstrumentInfo(SootUtils.getMethodSignature(callee),
            SootUtils.getMethodSignature(caller),
            callee.getExceptions().stream().map(SootClass::getName).collect(Collectors.toSet()),
            uncheckedExceptions, String.format("%s:%d", className, lineNumber));
    }

    private static <T> String collection2Str(Collection<T> collection, CharSequence delimiter) {
        if (collection == null)
            return null;
        return collection.stream().map(Object::toString).collect(Collectors.joining(delimiter));
    }

    public static void writeToCsvFile(Collection<InstrumentInfo> stats, String filePath)
        throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath));
             CSVPrinter csvPrinter = new CSVPrinter(writer,
                 CSVFormat.DEFAULT.withHeader("Callee", "Caller", "Location", "Checked Exceptions",
                     "Unchecked Exceptions"));) {
            for (InstrumentInfo info : stats) {
                csvPrinter.printRecord(info.getCallee(), info.getCaller(), info.getLocation(),
                    collection2Str(info.getCheckedExceptions(), ", "),
                    collection2Str(info.getUncheckedExceptions(), ", "));
            }
            csvPrinter.flush();
        }
    }

    public String getCallee() {
        return callee;
    }

    public String getCaller() {
        return caller;
    }

    public Set<String> getCheckedExceptions() {
        return checkedExceptions == null ? null : new HashSet<>(checkedExceptions);
    }

    public boolean hasCheckedExceptions() {
        return checkedExceptions != null && !checkedExceptions.isEmpty();
    }

    public Set<String> getUncheckedExceptions() {
        return uncheckedExceptions == null ? null : new HashSet<>(uncheckedExceptions);
    }

    public String getLocation() {
        return location;
    }

    public boolean hasUncheckedExceptions() {
        return uncheckedExceptions != null && !uncheckedExceptions.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(callee, caller, checkedExceptions, uncheckedExceptions);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InstrumentInfo)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        InstrumentInfo other = (InstrumentInfo) obj;
        return Objects.equals(caller, other.caller) && Objects.equals(callee, other.callee)
            && Objects.equals(checkedExceptions, other.checkedExceptions)
            && Objects.equals(uncheckedExceptions, other.uncheckedExceptions);
    }
}
