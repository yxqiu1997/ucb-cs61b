package gitlet;

import java.io.Serializable;
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

    public String getId() {
        return id;
    }

}
