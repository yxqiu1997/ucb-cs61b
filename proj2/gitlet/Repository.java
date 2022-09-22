package gitlet;

import java.io.File;

/** Represents a gitlet repository.
 *
 *  @author Qiu Yuxuan
 */
public class Repository {
    /*
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");

    public static final File BLOBS_DIR = Utils.join(GITLET_DIR, "blobs");

    public static final File COMMITS_DIR = Utils.join(GITLET_DIR, "commits");

    public static final File HEAD = Utils.join(GITLET_DIR, "HEAD");

    public static final File REFS_DIR = Utils.join(GITLET_DIR, "refs");

    public static final File HEADS_DIR = Utils.join(REFS_DIR, "heads");

    public void init() {
        if (GITLET_DIR.exists() && GITLET_DIR.isDirectory()) {
            System.out.println("A Gitlet version-control system already exists " +
                    "in the current directory.");
            System.exit(0);
        }
        mkdir(GITLET_DIR);
        mkdir(REFS_DIR);
        mkdir(BLOBS_DIR);
        mkdir(COMMITS_DIR);

        // Initialise commit
        Commit initialCommit = new Commit();
        Utils.writeObject(Utils.join(COMMITS_DIR, initialCommit.getId()), initialCommit);

        // Create master branch
        Utils.writeContents(HEAD, "master");
        Utils.writeContents(HEADS_DIR, "master");

        // Create HEAD
        Utils.writeContents(HEAD, "master");
    }

    public void mkdir(File file) {
        if (!file.mkdir()) {
            System.exit(0);
        }
    }

    public void checkOperands(int actual, int expect) {
        if (actual != expect) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
