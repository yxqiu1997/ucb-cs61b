package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {

    private String id;

    private String filename;

    private byte[] contents;

    public Blob(String filename, File CWD) {
        this.filename = filename;
        File file = Utils.join(CWD, filename);
        if (file.exists()) {
            this.contents = Utils.readContents(file);
            this.id = Utils.sha1(filename, contents);
        } else {
            this.contents = null;
            this.id = Utils.sha1(filename);
        }
    }

    public String getId() {
        return id;
    }
}
