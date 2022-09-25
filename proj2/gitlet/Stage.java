package gitlet;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class Stage implements Serializable {

    @Serial
    private static final long serialVersionUID = -4495924536800844486L;

    private Map<String, String> added;

    private Set<String> removed;

    public Stage() {
        this.added = new HashMap<>();
        this.removed = new HashSet<>();
    }

    public void addFile(String filename, String blobId) {
        added.put(filename, blobId);
        removed.remove(filename);
    }

    public boolean isEmpty() {
        return added.isEmpty() && removed.isEmpty();
    }

    public List<String> getStagedFilenameList() {
        return new ArrayList<>(){{
            addAll(added.keySet());
            addAll(removed);
        }};
    }

    public Map<String, String> getAdded() {
        return added;
    }

    public void setAdded(Map<String, String> added) {
        this.added = added;
    }

    public Set<String> getRemoved() {
        return removed;
    }

    public void setRemoved(Set<String> removed) {
        this.removed = removed;
    }
}
