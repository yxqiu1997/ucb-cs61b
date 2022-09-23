package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Stage implements Serializable {

    private Map<String, String> added;

    private Map<String, String> removed;

    public Stage() {
        this.added = new HashMap<>();
        this.removed = new HashMap<>();
    }

    public void addFile(String filename, String blobId) {
        added.put(filename, blobId);
        removed.remove(filename);
    }

    public Map<String, String> getAdded() {
        return added;
    }

    public void setAdded(Map<String, String> added) {
        this.added = added;
    }

    public Map<String, String> getRemoved() {
        return removed;
    }

    public void setRemoved(Map<String, String> removed) {
        this.removed = removed;
    }
}
