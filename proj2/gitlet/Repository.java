package gitlet;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

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

    public void checkOperands(int actual, int expect) {
        if (actual != expect) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public void checkOperands(String actual, String expect) {
        if (!actual.equals(expect)) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

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
        File master = Utils.join(HEADS_DIR, "master");
        Utils.writeContents(master, initialCommit.getId());

        // Create HEAD
        Utils.writeContents(HEAD, "master");
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

    public void commit(String message) {
        if (message == null || message.length() == 0) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit head = getHeadCommit();
        commitFile(message, Collections.singletonList(head));
    }

    private void commitFile(String message, List<Commit> parents) {
        Stage stage = Utils.readObject(STAGE, Stage.class);
        if (stage == null || stage.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Commit commit = new Commit(message, parents, stage);
        clearStage(stage);
        Utils.writeObject(Utils.join(COMMITS_DIR, commit.getId()), commit);

        File branch = getBranchFile(Utils.readContentsAsString(HEAD));
        Utils.writeContents(branch, commit.getId());
    }

    private void clearStage(Stage stage) {
        File[] files = STAGING_DIR.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        Path targetDir = BLOBS_DIR.toPath();
        for (File file : files) {
            Path sourceDir = file.toPath();
            try {
                Files.move(sourceDir, targetDir.resolve(sourceDir.getFileName()),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Utils.writeObject(STAGE, stage);
    }

    public void rm(String filename) {
        File file = Utils.join(CWD, filename);
        Commit head = getHeadCommit();
        Stage stage = Utils.readObject(STAGE, Stage.class);

        String headId = head.getBlobs().getOrDefault(filename, "");
        String stageId = stage.getAdded().getOrDefault(filename, "");
        if ("".equals(headId) && "".equals(stageId)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
         // Unstage the file if it is currently staged for addition
        if ("".equals(stageId)) {
            // Stage it for removal
            stage.getRemoved().add(filename);
        } else {
            stage.getAdded().remove(filename);
        }

        Blob blob = new Blob(filename, CWD);
        if (blob.isExist() && headId.equals(blob.getId())) {
            Utils.restrictedDelete(file);
        }
        Utils.writeObject(STAGE, stage);
    }

    public void log() {
        StringBuilder sb = new StringBuilder();
        Commit commit = getHeadCommit();
        while (commit != null) {
            sb.append(commit.getCommitInfo());
            commit = getCommitFromCommitId(commit.getFirstParentId());
        }
        System.out.println(sb);
    }

    public void globalLog() {
        StringBuilder sb = new StringBuilder();
        List<String> filenameList = Utils.plainFilenamesIn(COMMITS_DIR);
        if (filenameList == null || filenameList.size() == 0) {
            return;
        }
        filenameList.stream().filter(Objects::nonNull).forEach(filename -> {
            Commit commit = getCommitFromCommitId(filename);
            if (commit != null) {
                sb.append(commit.getCommitInfo());
            }
        });
        System.out.println(sb);
    }

    public void find(String target) {
        StringBuilder sb = new StringBuilder();
        List<String> filenameList = Utils.plainFilenamesIn(COMMITS_DIR);
        if (filenameList == null || filenameList.size() == 0) {
            return;
        }
        for (String filename : filenameList) {
            Commit commit = getCommitFromCommitId(filename);
            if (commit != null && commit.getMessage().contains(target)) {
                sb.append(commit.getId()).append("\n");
            }
        }
        if (sb.length() == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
        System.out.println(sb);
    }

    public void status() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Branches ===\n");
        String headBranch = Utils.readContentsAsString(HEAD);
        List<String> branchList = Utils.plainFilenamesIn(HEADS_DIR);
        if (branchList == null || branchList.size() == 0) {
            return;
        }
        for (String branch : branchList) {
            if (branch.equals(headBranch)) {
                sb.append("*").append(headBranch).append("\n");
            } else {
                sb.append(branch).append("\n");
            }
        }
        sb.append("\n");

        Stage stage = Utils.readObject(STAGE, Stage.class);
        sb.append("=== Staged Files ===\n");
        for (String filename : stage.getAdded().keySet()) {
            sb.append(filename).append("\n");
        }
        sb.append("\n");

        sb.append("=== Removed Files ===\n");
        for (String filename : stage.getRemoved()) {
            sb.append(filename).append("\n");
        }
        sb.append("\n");

        sb.append("=== Modifications Not Staged For Commit ===\n");
        Commit head = getCommitFromBranchFile(getBranchFile(headBranch));
        List<String> modifiedFiles = getModifiedFileList(head, stage);
        for (String str : modifiedFiles) {
            sb.append(str).append("\n");
        }
        sb.append("\n");

        sb.append("=== Untracked Files ===\n");
         List<String> untrackedFiles = getUntrackedFileList();
         for (String filename : untrackedFiles) {
             sb.append(filename).append("\n");
         }
        sb.append("\n");

        System.out.println(sb);
    }

    private List<String> getUntrackedFileList() {
        List<String> untrackedFileList = new ArrayList<>();
        List<String> stageFileList = Utils.readObject(STAGE, Stage.class).getStagedFilenameList();
        Set<String> headFileList = getHeadCommit().getBlobs().keySet();
        for (String filename : Objects.requireNonNull(Utils.plainFilenamesIn(CWD))) {
            if (!stageFileList.contains(filename) && !headFileList.contains(filename)) {
                untrackedFileList.add(filename);
            }
        }
        Collections.sort(untrackedFileList);
        return untrackedFileList;
    }

    private List<String> getModifiedFileList(Commit head, Stage stage) {
        List<String> modifiedFileList = new ArrayList<>();
        Set<String> headFileList = head.getBlobs().keySet();
        List<String> stagedFileList = stage.getStagedFilenameList();
        List<String> currentFileList = Utils.plainFilenamesIn(CWD);
        if (currentFileList == null || currentFileList.size() == 0) {
            return new ArrayList<>();
        }
        Set<String> allFileSet = new HashSet<>(){{
            addAll(currentFileList);
            addAll(headFileList);
            addAll(stagedFileList);
        }};

        for (String filename : allFileSet) {
            if (!currentFileList.contains(filename)) {
                if (stage.getAdded().containsKey(filename) || headFileList.contains(filename)
                        && !stagedFileList.contains(filename)) {
                    modifiedFileList.add(filename + " (deleted)");
                }
            } else {
                String blobId = new Blob(filename, CWD).getId();
                String stageId = stage.getAdded().getOrDefault(filename, "");
                String headId = head.getBlobs().getOrDefault(filename, "");
                boolean flag = (!"".equals(headId) && !headId.equals(blobId) && "".equals(stageId))
                        || (!"".equals(stageId) && !stageId.equals(blobId));
                if (flag){
                    modifiedFileList.add(filename + " (modified)");
                }
            }
        }
        Collections.sort(modifiedFileList);
        return modifiedFileList;
    }

    public void checkoutBranch(String branch) {
        File branchFile = getBranchFile(branch);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        String headBranch = Utils.readContentsAsString(HEAD);
        if (branch.equals(headBranch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Commit commit = getCommitFromBranchFile(getBranchFile(branch));
        checkUntrackedFile(commit.getBlobs());
        clearStage(Utils.readObject(STAGE, Stage.class));
        replaceWorkingDirectory(commit);
        Utils.writeContents(HEAD, branch);
    }

    private void replaceWorkingDirectory(Commit commit) {
        File[] files = CWD.listFiles(gitletFilter);
        if (files == null || files.length == 0) {
            return;
        }
        for (File file : files) {
            deleteFile(file);
        }
        commit.getBlobs().forEach((filename, blobId) -> {
            File file = Utils.join(CWD, filename);
            Blob blob = Utils.readObject(Utils.join(BLOBS_DIR, blobId), Blob.class);
            Utils.writeContents(file, blob.getContents());
        });
    }

    private void deleteFile(File file) {
        if (file.isDirectory()) {
            for (File f : Objects.requireNonNull(file.listFiles())) {
                deleteFile(f);
            }
        }
        file.delete();
    }

    private final FilenameFilter gitletFilter = (dir, name) -> !".gitlet".equals(name);

    private void checkUntrackedFile(Map<String, String> blobs) {
        List<String> untrackedFileList = getUntrackedFileList();
        if (untrackedFileList.isEmpty()) {
            return;
        }
        for (String filename : untrackedFileList) {
            String blobId = new Blob(filename, CWD).getId();
            String fileId = blobs.getOrDefault(filename, "");
            if (!fileId.equals(blobId)) {
                System.out.println("There is an untracked file in the way; delete it, " +
                        "or add and commit it first.");
                System.exit(0);
            }
        }
    }

    public void checkoutFileFromHead(String filename) {
        Commit head = getHeadCommit();
        checkoutFileFromCommit(head, filename);
    }

    private void checkoutFileFromCommit(Commit commit, String filename) {
        String blobId = commit.getBlobs().getOrDefault(filename, "");
        if ("".equals(blobId)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Blob blob = Utils.readObject(Utils.join(BLOBS_DIR, blobId), Blob.class);
        Utils.writeContents(Utils.join(CWD, blob.getFilename()), blob.getContents());
    }

    public void checkoutFileFromCommitId(String commitId, String filename) {
        String entireCommitId = getEntireCommitId(commitId);
        File file = Utils.join(COMMITS_DIR, entireCommitId);
        if (!file.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = Utils.readObject(file, Commit.class);
        checkoutFileFromCommit(commit, filename);
    }

    private String getEntireCommitId(String commitId) {
        if (commitId.length() == Utils.UID_LENGTH) {
            return commitId;
        }
        for (String filename : Objects.requireNonNull(COMMITS_DIR.list())) {
            if (filename.startsWith(commitId)) {
                return filename;
            }
        }
        return "";
    }

}
