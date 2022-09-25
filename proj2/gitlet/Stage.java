package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stage implements Serializable {

    private final Map<String, String> filenameToBlobId;

    public Stage() {
        this.filenameToBlobId = new HashMap<>();
    }

    public Map<String, String> getFilenameToBlobId() {
        return filenameToBlobId;
    }

    public boolean isNew(Blob blob) {
        return !filenameToBlobId.containsValue(blob.getId());
    }

    public boolean isFilePathExists(String path) {
        return filenameToBlobId.containsKey(path);
    }

    public void delete(Blob blob) {
        filenameToBlobId.remove(blob.getFilePath());
    }

    public void delete(String path) {
        filenameToBlobId.remove(path);
    }

    public void add(Blob blob) {
        filenameToBlobId.put(blob.getFilePath(), blob.getId());
    }

    public void saveAddStage() {
        Utils.writeObject(Repository.ADD_STAGE_FILE, this);
    }

    public void saveRemoveStage() {
        Utils.writeObject(Repository.REMOVE_STAGE_FILE, this);
    }

    public List<Blob> getBlobList() {
        List<Blob> blobList = new ArrayList<>();
        for (String id : filenameToBlobId.values()) {
            Blob blob = Utils.readObject(Utils.join(Repository.BLOBS_DIR, id), Blob.class);
            blobList.add(blob);
        }
        return blobList;
    }

    public void clear() {
        filenameToBlobId.clear();
    }

    public boolean exists(String filename) {
        return filenameToBlobId.containsKey(filename);
    }

    public boolean isEmpty() {
        return filenameToBlobId.size() == 0;
    }

}
