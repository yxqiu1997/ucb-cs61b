package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Repository.CWD;
import static gitlet.Utils.join;

/** Represents a gitlet commit object.
 *
 *  @author
 */
public class Commit implements Serializable {

    private static final Long serialVersionUID = -7961947390106190044L;

    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;

    private String id;

    private String timeStamp;

    private File commitSaveFilename;

    private Map<String, String> filePathToBlobId;

    private List<String> parents;

    public Commit() {
        this.message = "initial commit";
        this.timeStamp = dateToTimeStamp(new Date(0));
        this.filePathToBlobId = new HashMap<>();
        this.parents = new ArrayList<>();
        this.id = Utils.sha1(message, timeStamp, filePathToBlobId.toString(), parents.toString());
        this.commitSaveFilename = Utils.join(Repository.OBJECTS_DIR, id);
    }

    public Commit(String message, Map<String, String> filePathToBlobId, List<String> parents) {
        this.message = message;
        this.filePathToBlobId = filePathToBlobId;
        this.parents = parents;
        this.timeStamp = dateToTimeStamp(new Date());
        this.id = Utils.sha1(message, timeStamp, filePathToBlobId.toString(), parents.toString());
        this.commitSaveFilename = Utils.join(Repository.OBJECTS_DIR, id);
    }

    public void save() {
        Utils.writeObject(commitSaveFilename, this);
    }

    private String dateToTimeStamp(Date date) {
        return new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH)
                .format(date);
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getFilePathToBlobId() {
        return filePathToBlobId;
    }

    public boolean exists(String filePath) {
        return filePathToBlobId.containsKey(filePath);
    }

    public List<String> getParents() {
        return parents;
    }

    public String getMessage() {
        return message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public List<String> getFilenameList() {
        List<String> filename = new ArrayList<>();
        List<Blob> blobList = new ArrayList<>();
        for (String blobId : filePathToBlobId.values()) {
            Blob blob = Utils.readObject(Utils.join(Repository.OBJECTS_DIR, blobId), Blob.class);
            blobList.add(blob);
        }

        for (Blob blob : blobList) {
            filename.add(blob.getFilename().getName());
        }
        return filename;
    }

    public Blob getBlobByFilename(String filename) {
        File file = join(CWD, filename);
        String path = file.getPath();
        String blobId = filePathToBlobId.get(path);
        return Utils.readObject(Utils.join(Repository.OBJECTS_DIR, blobId), Blob.class);
    }

    public List<String> getBlobIdList() {
        return new ArrayList<>(filePathToBlobId.values());
    }

}
