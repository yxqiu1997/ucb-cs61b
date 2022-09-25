package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/** Represents a gitlet commit object.
 *
 *  @author Qiu Yuxuan
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private final String message;

    private final Date timestamp;

    private final String id;

    private final List<String> parents;

    private final Map<String, String> blobs;

    public Commit() {
        this.message = "initial commit";
        this.timestamp = new Date(0);
        this.parents = new LinkedList<>();
        this.blobs = new HashMap<>();
        this.id = Utils.sha1(message, timestamp.toString(), parents.toString(), blobs.toString());
    }

    public Commit(String message, List<Commit> parents, Stage stage) {
        this.message = message;
        this.timestamp = new Date();
        this.parents = new ArrayList<>();
        for (Commit commit : parents) {
            this.parents.add(commit.getId());
        }
        this.blobs = parents.get(0).getBlobs();
        this.blobs.putAll(stage.getAdded());
        for (String filename : stage.getRemoved()) {
            this.blobs.remove(filename);
        }
        this.id = Utils.sha1(message, timestamp.toString(), parents.toString(), blobs.toString());
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getBlobs() {
        return blobs;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getParents() {
        return parents;
    }

    public String getFirstParentId() {
        return this.parents == null || this.parents.isEmpty() ? "null" : this.parents.get(0);
    }

    public String getTimeStampString() {
        return new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH)
                .format(this.timestamp);
    }

    public String getCommitInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("===\n");
        sb.append("commit ").append(this.id).append("\n");
        if (parents.size() == 2) {
            sb.append("Merge: ").append(parents.get(0).substring(0, 7)).append(" ");
            sb.append(parents.get(1).substring(0, 7)).append("\n");
        }
        sb.append("Date: ").append(this.getTimeStampString()).append("\n");
        sb.append(this.message).append("\n\n");
        return sb.toString();
    }
}
