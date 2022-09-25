package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {

    private String id;

    private byte[] contents;

    private File filename;

    private String filePath;

    private File blobSaveFilename;

    public Blob(File filename) {
        this.filename = filename;
        this.contents = Utils.readContents(filename);
        this.filePath = filename.getPath();
        this.id = Utils.sha1(filePath, contents);
        this.blobSaveFilename = Utils.join(Repository.BLOBS_DIR, id);
    }

    public void save() {
        Utils.writeObject(blobSaveFilename, this);
    }

    public String getId() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }

    public File getFilename() {
        return filename;
    }

    public byte[] getContents() {
        return contents;
    }


}
