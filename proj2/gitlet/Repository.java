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

    public static final File OBJECTS_DIR = Utils.join(GITLET_DIR, "objects");

    public static final File BLOBS_DIR = Utils.join(OBJECTS_DIR, "blobs");

    public static final File COMMITS_DIR = Utils.join(OBJECTS_DIR, "commits");

    public static final File HEAD = Utils.join(GITLET_DIR, "HEAD");

    public static final File STAGING_DIR = Utils.join(GITLET_DIR, "staging");

    public static final File STAGE = Utils.join(GITLET_DIR, "stage");

    public static final File REFS_DIR = Utils.join(GITLET_DIR, "refs");

    public static final File HEADS_DIR = Utils.join(REFS_DIR, "heads");

    public static final File REMOTE_DIR = Utils.join(REFS_DIR, "remote");

    public void init() {
        if (GITLET_DIR.exists() && GITLET_DIR.isDirectory()) {
            System.out.println("A Gitlet version-control system already exists " +
                    "in the current directory.");
            System.exit(0);
        }
        mkdir(GITLET_DIR);
        mkdir(OBJECTS_DIR);
        mkdir(BLOBS_DIR);
        mkdir(COMMITS_DIR);
        mkdir(STAGING_DIR);
        Utils.writeObject(STAGE, new Stage());
        mkdir(REFS_DIR);
        mkdir(HEADS_DIR);
        mkdir(REMOTE_DIR);

        // Initialise commit
        Commit initialCommit = new Commit();
        Utils.writeObject(Utils.join(COMMITS_DIR, initialCommit.getId()), initialCommit);

        // Create master branch
        Utils.writeContents(HEAD, "master");
        Utils.writeContents(HEADS_DIR, "master");

        // Create HEAD
        Utils.writeContents(HEAD, "master");
    }

    public void add(String filename) {
        File file = Utils.join(CWD, filename);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        Commit head = getHeadCommit();
        Stage stage = Utils.readObject(STAGE, Stage.class);
        String headId = head.getBlobs().getOrDefault(filename, "");
        String stageId = stage.getAdded().getOrDefault(filename, "");
        Blob blob = new Blob(filename, CWD);
        String blobId = blob.getId();

        if (headId.equals(blobId)) {
            // Delete the file from staging
            if (!stageId.equals(blobId)) {
                Utils.join(STAGING_DIR, stageId).delete();
                stage.getAdded().remove(stageId);
                stage.getRemoved().remove(filename);
                Utils.writeObject(STAGE, stage);
            }
        } else if (!stageId.equals(blobId)) {
            // Update staging
            if (!"".equals(stageId)) {
                Utils.join(STAGING_DIR, stageId).delete();
            }
            Utils.writeObject(Utils.join(STAGING_DIR, blobId), blob);
            stage.addFile(filename, blobId);
            Utils.writeObject(STAGE, stage);
        }
    }

    private Commit getHeadCommit() {
        String branch = Utils.readContentsAsString(HEAD);
        File branchFile = getBranchFile(branch);
        Commit head = getCommitFromBranchFile(branchFile);
        if (head == null) {
            System.out.println("HEAD not found!");
            System.exit(0);
        }
        return head;
    }

    private Commit getCommitFromBranchFile(File branchFile) {
        String commitId = Utils.readContentsAsString(branchFile);
        return getCommitFromCommitId(commitId);
    }

    private Commit getCommitFromCommitId(String id) {
        File file = Utils.join(COMMITS_DIR, id);
        return "null".equals(id) || !file.exists() ? null
                : Utils.readObject(file, Commit.class);
    }

    private File getBranchFile(String branch) {
        File file;
        String[] branches = branch.split("/");
        if (branches.length == 1) {
            file = Utils.join(HEADS_DIR, branch);
        } else {
            file = Utils.join(REMOTE_DIR, branches[0], branches[1]);
        }
        return file;
    }

    public void mkdir(File file) {
        if (!file.mkdir()) {
            System.exit(0);
        }
    }

    public void checkInitialiseDirectoryExists() {
        if (!GITLET_DIR.isDirectory()) {
            System.out.println("Not in an initialized Gitlet directory.");
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
