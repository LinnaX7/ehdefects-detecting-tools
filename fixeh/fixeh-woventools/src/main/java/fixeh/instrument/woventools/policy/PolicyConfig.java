package fixeh.instrument.woventools.policy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PolicyConfig {
    static final Set<String> KNOWN_POLICY_ENTRY_KINDS = new HashSet<>();

    static {
        KNOWN_POLICY_ENTRY_KINDS.addAll(
            Arrays.asList(PolicyEntryKinds.FILE, PolicyEntryKinds.PACKAGE, PolicyEntryKinds.CLASS,
                PolicyEntryKinds.METHOD, PolicyEntryKinds.EXCEPTION, PolicyEntryKinds.STACKTRACE,
                    PolicyEntryKinds.FILTER));
    }

    private RemoteController remoteController;
    // default exclude mode
    private boolean exclude = true;
    private List<PolicyEntry> policyEntries;
    private int limit = -1;

    private String generalPattern;

    public String getGeneralPattern() {
        return generalPattern;
    }

    public void setGeneralPattern(String generalPattern) {
        this.generalPattern = generalPattern;
    }

    public PolicyConfig(RemoteController remoteController, List<PolicyEntry> policyEntries) {
        this.remoteController = remoteController;
        this.policyEntries = policyEntries;
    }

    public RemoteController getRemoteController() {
        return remoteController;
    }

    public boolean isRemoteControllerEnabled() {
        return remoteController != null && remoteController.isEnabled();
    }

    public List<PolicyEntry> getPolicyEntries() {
        return policyEntries;
    }

    public String getFirstFilePolicyValue() {
        // Return null if there's no file policy
        if (policyEntries == null || policyEntries.isEmpty()) {
            return null;
        }

        for (PolicyEntry entry : policyEntries) {
            if (entry.kind.equals(PolicyEntryKinds.FILE)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isExclude() {
        return exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    public static class RemoteController {
        private boolean enabled;

        private String address;

        private int port;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    static class PolicyEntryKinds {
        static final String FILE = "file";
        static final String PACKAGE = "package";
        static final String CLASS = "class";
        static final String METHOD = "method";
        static final String EXCEPTION = "exception";
        static final String STACKTRACE = "stacktrace";
        static final String FILTER = "filter";
    }

    public static class PolicyEntry {
        private String kind;

        private String value;

        private Map<String, String> others;

        PolicyEntry() {}

        public PolicyEntry(String kind, String value) {
            this.kind = kind;
            this.value = value;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public void addOther(String key, String value) {
            if (others == null) {
                others = new HashMap<>();
            }
            others.put(key, value);
        }

        public Map<String, String> getOthers() {
            return others;
        }

        public String getOther(String key) {
            if (others == null) {
                return null;
            }
            return others.get(key);
        }
    }
}
