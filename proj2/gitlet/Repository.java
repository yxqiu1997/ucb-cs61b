package gitlet;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *
 *  @author
 */
public class Repository {
    /**
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     *
     * The current working directory.
     *   .gitlet
     *      |--objects
     *      |     |--commit and blob
     *      |--refs
     *      |    |--heads
     *      |         |--master
     *      |--HEAD
     *      |--stage
     */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");

    public static final File HEADS_DIR = join(REFS_DIR, "heads");

    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");

    public static final File ADD_STAGE_FILE = join(GITLET_DIR, "add_stage");

    public static final File REMOVE_STAGE_FILE = join(GITLET_DIR, "remove_stage");

    private static Commit currentCommit;

    private static String currentBranch;

    private static Stage addStage = new Stage();

    private static Stage removeStage = new Stage();

    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists "
                    + "in the current directory.");
            System.exit(0);
        }
        if (!GITLET_DIR.mkdir() || !OBJECTS_DIR.mkdir() || !REFS_DIR.mkdir()
                || !HEADS_DIR.mkdir()) {
            throw new IllegalArgumentException("Failed to create directory");
        }
        // Initialise commit
        Commit commit = new Commit();
        currentCommit = commit;
        commit.save();

        // Initialise HEAD
        writeContents(HEAD_FILE, "master");

        // Initialise heads
        writeContents(join(HEADS_DIR, "master"), currentCommit.getId());

    }

    public static void checkIfInitialised() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static void add(String file) {
        File filename = join(CWD, file);
        if (!filename.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Blob blob = new Blob(filename);
        currentCommit = getCurrentCommit();
        addStage = readAddStage();
        removeStage = readRemoveStage();
        if (!currentCommit.getFilePathToBlobId().containsValue(blob.getId())
                || !removeStage.isNew(blob)) {
            if (addStage.isNew(blob)) {
                if (removeStage.isNew(blob)) {
                    blob.save();
                    if (addStage.isFilePathExists(blob.getFilePath())) {
                        addStage.delete(blob);
                    }
                    addStage.add(blob);
                    addStage.saveAddStage();
                } else {
                    removeStage.delete(blob);
                    removeStage.saveRemoveStage();
                }
            }
        }
    }

    private static Stage readAddStage() {
        if (!ADD_STAGE_FILE.exists()) {
            return new Stage();
        }
        return readObject(ADD_STAGE_FILE, Stage.class);
    }

    private static Stage readRemoveStage() {
        if (!REMOVE_STAGE_FILE.exists()) {
            return new Stage();
        }
        return readObject(REMOVE_STAGE_FILE, Stage.class);
    }

    private static Commit getCurrentCommit() {
        String currentBranchName = getCurrentBranch();
        String currentCommitId = readContentsAsString(join(HEADS_DIR, currentBranchName));
        File currentCommitFile = join(OBJECTS_DIR, currentCommitId);
        return readObject(currentCommitFile, Commit.class);
    }

    private static String getCurrentBranch() {
        return readContentsAsString(HEAD_FILE);
    }

    public static void commit(String message) {
        if ("".equals(message)) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit newCommit = getNewCommit(message);
        saveNewCommit(newCommit);
    }

    private static void saveNewCommit(Commit commit) {
        commit.save();
        addStage.clear();
        addStage.saveAddStage();
        removeStage.clear();
        removeStage.saveRemoveStage();

        currentCommit = commit;
        String currentBranch = getCurrentBranch();
        writeContents(join(HEADS_DIR, currentBranch), currentCommit.getId());
    }

    private static Commit getNewCommit(String message) {
        Map<String, String> addBlobMap = new HashMap<>();
        addStage = readAddStage();
        List<Blob> addBlobList = addStage.getBlobList();
        for (Blob blob : addBlobList) {
            addBlobMap.put(blob.getFilePath(), blob.getId());
        }

        Map<String, String> removeBlobMap = new HashMap<>();
        removeStage = readRemoveStage();
        List<Blob> removeBlobList = removeStage.getBlobList();
        for (Blob blob : removeBlobList) {
            removeBlobMap.put(blob.getFilePath(), blob.getId());
        }

        if (addBlobMap.isEmpty() && removeBlobMap.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        currentCommit = getCurrentCommit();
        Map<String, String> blobMap = currentCommit.getFilePathToBlobId();
        if (!addBlobMap.isEmpty()) {
            for (String path : addBlobMap.keySet()) {
                blobMap.put(path, addBlobMap.get(path));
            }
        }
        if (!removeBlobMap.isEmpty()) {
            for (String path : removeBlobMap.keySet()) {
                blobMap.remove(path);
            }
        }

        List<String> parents = new ArrayList<>() {{add(getCurrentCommit().getId());}};
        return new Commit(message, blobMap, parents);
    }

    public static void rm(String filename) {
        File file = join(CWD, filename);
        String filePath = file.getPath();
        addStage = readAddStage();
        currentCommit = getCurrentCommit();

        if (addStage.exists(filePath)) {
            addStage.delete(filePath);
            addStage.saveAddStage();
        } else if (currentCommit.exists(filePath)) {
            removeStage = readRemoveStage();
            Blob removeBlob = getBlobByFilePath(filePath, currentCommit);
            removeStage.add(removeBlob);
            removeStage.saveRemoveStage();
            file.delete();
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    private static Blob getBlobByFilePath(String filePath, Commit commit) {
        String blobId = commit.getFilePathToBlobId().get(filePath);
        return readObject(join(Repository.OBJECTS_DIR, blobId), Blob.class);
    }

    public static void log() {
        String currentBranchName = getCurrentBranch();
        String currentCommitId = readContentsAsString(join(HEADS_DIR, currentBranchName));
        currentCommit = readObject(join(OBJECTS_DIR, currentCommitId), Commit.class);
        while (!currentCommit.getParents().isEmpty()) {
            if (isMergeCommit(currentCommit)) {
                printMergeCommit(currentCommit);
            } else {
                printCommit(currentCommit);
            }
            List<String> parentsCommitId = currentCommit.getParents();
            currentCommit = getCommitById(parentsCommitId.get(0));
        }
        printCommit(currentCommit);
    }

    private static Commit getCommitById(String commitId) {
        if (commitId.length() == 40) {
            File commitFile = join(OBJECTS_DIR, commitId);
            return !commitFile.exists() ? null : readObject(commitFile, Commit.class);
        }
        List<String> objectIdList = plainFilenamesIn(OBJECTS_DIR);
        for (String o : Objects.requireNonNull(objectIdList)) {
            if (commitId.equals(o.substring(0, commitId.length()))) {
                return readObject(join(OBJECTS_DIR, o), Commit.class);
            }
        }
        return null;
    }

    private static void printCommit(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commit.getId());
        System.out.println("Date: " + commit.getTimeStamp());
        System.out.println(commit.getMessage() + "\n");
    }

    private static void printMergeCommit(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commit.getId());

        List<String> parentsCommitId = commit.getParents();
        String parent1 = parentsCommitId.get(0);
        String parent2 = parentsCommitId.get(1);
        System.out.println("Merge: " + parent1.substring(0, 7) + " " + parent2.substring(0, 7));

        System.out.println("Date: " + commit.getTimeStamp());
        System.out.println(commit.getMessage() + "\n");
    }

    private static boolean isMergeCommit(Commit commit) {
        return commit.getParents().size() == 2;
    }

    public static void globalLog() {
        List<String> commitList = plainFilenamesIn(OBJECTS_DIR);
        Commit commit;
        for (String id : commitList) {
            commit = getCommitById(id);
            if (isMergeCommit(commit)) {
                printMergeCommit(commit);
            } else {
                printCommit(commit);
            }
        }
    }

    public static void find(String message) {
        List<String> commitList = plainFilenamesIn(OBJECTS_DIR);
        List<String> idList = new ArrayList<>();
        for (String id : Objects.requireNonNull(commitList)) {
            Commit commit = getCommitById(id);
            if (message.equals(commit.getMessage())) {
                idList.add(id);
            }
        }
        if (idList.isEmpty()) {
            System.out.println("Found no commit with that message.");
        } else {
            for (String id : idList) {
                System.out.println(id);
            }
        }
    }

    public static void status() {
        List<String> branchList = plainFilenamesIn(HEADS_DIR);
        currentBranch = getCurrentBranch();
        System.out.println("=== Branches ===");
        System.out.println("*" + currentBranch);
        if (Objects.requireNonNull(branchList).size() > 1) {
            for (String branch : branchList) {
                if (!branch.equals(currentBranch)) {
                    System.out.println(branch);
                }
            }
        }
        System.out.println("\n=== Staged Files ===");
        addStage = readAddStage();
        for (Blob blob : addStage.getBlobList()) {
            System.out.println(blob.getFilename().getName());
        }
        System.out.println("\n=== Removed Files ===");
        removeStage = readRemoveStage();
        for (Blob blob : removeStage.getBlobList()) {
            System.out.println(blob.getFilename().getName());
        }
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        System.out.println("\n=== Untracked Files ===");
    }

    public static void checkoutBranch(String branch) {
        currentBranch = getCurrentBranch();
        if (branch.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        List<String> branchList = plainFilenamesIn(HEADS_DIR);
        if (!branchList.contains(branch)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        currentCommit = getCurrentCommit();
        Commit newCommit = getCommitByBranchName(branch);
        changeCommitTo(newCommit);
        writeContents(HEAD_FILE, branch);
    }

    private static void changeCommitTo(Commit newCommit) {
        List<String> onlyCurrCommitTracked = findOnlyCurrCommitTracked(newCommit);
        List<String> bothCommitTracked = findBothCommitTracked(newCommit);
        List<String> onlyNewCommitTracked = findOnlyNewCommitTracked(newCommit);
        deleteFiles(onlyCurrCommitTracked);
        overwriteFiles(bothCommitTracked, newCommit);
        writeFiles(onlyNewCommitTracked, newCommit);
        clearAllStage();
    }

    private static void clearAllStage() {
        addStage = readAddStage();
        addStage.clear();
        addStage.saveAddStage();
        removeStage = readRemoveStage();
        removeStage.clear();
        removeStage.saveRemoveStage();
    }

    private static void overwriteFiles(List<String> bothCommitTracked, Commit newCommit) {
        if (bothCommitTracked.isEmpty()) {
            return;
        }
        for (String fileName : bothCommitTracked) {
            Blob blob = newCommit.getBlobByFilename(fileName);
            writeBlob(blob);
        }
    }

    private static void writeFiles(List<String> onlyNewCommitTracked, Commit newCommit) {
        if (onlyNewCommitTracked.isEmpty()) {
            return;
        }
        for (String fileName : onlyNewCommitTracked) {
            File file = join(CWD, fileName);
            if (file.exists()) {
                System.out.println("There is an untracked file in the way; delete it, "
                        + "or add and commit it first.");
                System.exit(0);
            }
        }
        overwriteFiles(onlyNewCommitTracked, newCommit);
    }

    private static void writeBlob(Blob blob) {
        File fileName = join(CWD, blob.getFilename().getName());
        byte[] bytes = blob.getContents();
        writeContents(fileName, new String(bytes, StandardCharsets.UTF_8));
    }

    private static void deleteFiles(List<String> onlyCurrCommitTracked) {
        if (onlyCurrCommitTracked.isEmpty()) {
            return;
        }
        for (String filename : onlyCurrCommitTracked) {
            File file = join(CWD, filename);
            restrictedDelete(file);
        }
    }

    private static List<String> findOnlyNewCommitTracked(Commit newCommit) {
        List<String> currCommitFiles = currentCommit.getFilenameList();
        List<String> onlyNewCommitTracked = newCommit.getFilenameList();
        for (String s : currCommitFiles) {
            onlyNewCommitTracked.remove(s);
        }
        return onlyNewCommitTracked;
    }

    private static List<String> findBothCommitTracked(Commit newCommit) {
        List<String> newCommitFiles = newCommit.getFilenameList();
        List<String> currCommitFiles = currentCommit.getFilenameList();
        List<String> bothCommitTracked = new ArrayList<>();
        for (String s : newCommitFiles) {
            if (currCommitFiles.contains(s)) {
                bothCommitTracked.add(s);
            }
        }
        return bothCommitTracked;
    }

    private static List<String> findOnlyCurrCommitTracked(Commit newCommit) {
        List<String> newCommitFiles = newCommit.getFilenameList();
        List<String> onlyCurrCommitTracked = currentCommit.getFilenameList();
        for (String s : newCommitFiles) {
            onlyCurrCommitTracked.remove(s);
        }
        return onlyCurrCommitTracked;
    }

    private static Commit getCommitByBranchName(String branch) {
        File branchFile = join(HEADS_DIR, branch);
        String newCommitID = readContentsAsString(branchFile);
        return getCommitById(newCommitID);
    }

    public static void checkout(String filename) {
        Commit currCommmit = getCurrentCommit();
        List<String> filenameList = currCommmit.getFilenameList();
        if (filenameList.contains(filename)) {
            Blob blob = currCommmit.getBlobByFilename(filename);
            writeBlob(blob);
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    public static void checkout(String commitId, String filename) {
        Commit commit = getCommitById(commitId);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        List<String> filenameList = commit.getFilenameList();
        if (filenameList.contains(filename)) {
            Blob blob = commit.getBlobByFilename(filename);
            writeBlob(blob);
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    public static void branch(String branch) {
        List<String> branchList = plainFilenamesIn(HEADS_DIR);
        if (branchList.contains(branch)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        File newBranchFile = join(HEADS_DIR, branch);
        currentCommit = getCurrentCommit();
        writeContents(newBranchFile, currentCommit.getId());
    }

    public static void rmBranch(String branch) {
        currentBranch = getCurrentBranch();
        if (currentBranch.equals(branch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        List<String> branchList = plainFilenamesIn(HEADS_DIR);
        if (!branchList.contains(branch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        File filename = join(HEADS_DIR, branch);
        if (!filename.isDirectory()) {
            filename.delete();
        }
    }

    public static void reset(String commitId) {
        Commit commit = getCommitById(commitId);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        currentCommit = getCurrentCommit();
        Commit newCommit = getCommitById(commitId);
        changeCommitTo(newCommit);

        currentBranch = readContentsAsString(HEAD_FILE);
        File branchFile = join(HEADS_DIR, currentBranch);
        writeContents(branchFile, commitId);
    }

    public static void merge(String mergedBranch) {
        currentBranch = getCurrentBranch();
        addStage = readAddStage();
        removeStage = readRemoveStage();
        if (!(addStage.isEmpty() && removeStage.isEmpty())) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        List<String> branchList = plainFilenamesIn(HEADS_DIR);
        if (!branchList.contains(mergedBranch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (currentBranch.equals(mergedBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        currentCommit = getCurrentCommit();
        Commit mergedCommit = getCommitByBranchName(mergedBranch);
        Commit splitPoint = findSplitPoint(currentCommit, mergedCommit);
        if (splitPoint.getId().equals(mergedCommit.getId())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (splitPoint.getId().equals(currentCommit.getId())) {
            System.out.println("Current branch fast-forwarded.");
            checkoutBranch(mergedBranch);
        }
        Map<String, String> currCommitBlobs = currentCommit.getFilePathToBlobId();

        String message = "Merged " + mergedBranch + " into " + currentBranch + ".";
        String currentBranchCommitId = getCommitByBranchName(currentBranch).getId();
        String mergeBranchCommitId = getCommitByBranchName(mergedBranch).getId();
        List<String> parents = new ArrayList<>();
        parents.add(currentBranchCommitId);
        parents.add(mergeBranchCommitId);
        Commit newCommit = new Commit(message, currCommitBlobs, parents);

        Commit mergeCommit = mergeFilesToNewCommit(splitPoint, newCommit, mergedCommit);
        saveNewCommit(mergeCommit);
    }

    private static Commit mergeFilesToNewCommit(Commit splitPoint, Commit newCommit,
                                                Commit mergedCommit) {
        Set<String> fileSet = new HashSet<>();
        fileSet.addAll(splitPoint.getBlobIdList());
        fileSet.addAll(newCommit.getBlobIdList());
        fileSet.addAll(mergedCommit.getBlobIdList());
        List<String> fileList = new ArrayList<>(fileSet);
        /*
         * case 1 5 6: write mergeCommit files into newCommit
         * case 1: overwrite files
         * case 5: write files
         * case 6: delete files
         */
        List<String> overwriteFileList =
                computeOverwriteFiles(splitPoint, newCommit, mergedCommit);
        List<String> writeFileList =
                computeWriteFiles(splitPoint, newCommit, mergedCommit);
        List<String> deleteFileList =
                computeDeleteFiles(splitPoint, newCommit, mergedCommit);

        overwriteFiles(changeBlobIdListToFilenameList(overwriteFileList), mergedCommit);
        writeFiles(changeBlobIdListToFilenameList(writeFileList), mergedCommit);
        deleteFiles(changeBlobIdListToFilenameList(deleteFileList));

        /* * case 3-1: deal conflict */
        checkIfConflict(fileList, splitPoint, newCommit, mergedCommit);

        /* * case 2 4 7 3-1: do nothing */

        return computeMergedCommit(newCommit, overwriteFileList, writeFileList, deleteFileList);
    }

    private static Commit computeMergedCommit(Commit newCommit, List<String> overwriteFileList,
                                              List<String> writeFileList,
                                              List<String> deleteFileList) {
        Map<String, String> mergedCommitBlobs = newCommit.getFilePathToBlobId();
        if (!overwriteFileList.isEmpty()) {
            for (String blobID : overwriteFileList) {
                Blob blob = readObject(join(OBJECTS_DIR, blobID), Blob.class);
                mergedCommitBlobs.put(blob.getFilePath(), blobID);
            }
        }
        if (!writeFileList.isEmpty()) {
            for (String blobID : writeFileList) {
                Blob blob = readObject(join(OBJECTS_DIR, blobID), Blob.class);
                mergedCommitBlobs.put(blob.getFilePath(), blobID);
            }
        }
        if (!deleteFileList.isEmpty()) {
            for (String blobID : overwriteFileList) {
                Blob blob = readObject(join(OBJECTS_DIR, blobID), Blob.class);
                mergedCommitBlobs.remove(blob.getFilePath());
            }
        }
        return new Commit(newCommit.getMessage(), mergedCommitBlobs, newCommit.getParents());
    }

    private static void checkIfConflict(List<String> fileList, Commit splitPoint,
                                        Commit newCommit, Commit mergedCommit) {
        Map<String, String> splitPointMap = splitPoint.getFilePathToBlobId();
        Map<String, String> newCommitMap = newCommit.getFilePathToBlobId();
        Map<String, String> mergeCommitMap = mergedCommit.getFilePathToBlobId();

        boolean conflict = false;
        for (String blobID : fileList) {
            String path = readObject(join(OBJECTS_DIR, blobID), Blob.class).getFilePath();
            int commonPath = 0;
            if (splitPointMap.containsKey(path)) {
                commonPath += 1;
            }
            if (newCommitMap.containsKey(path)) {
                commonPath += 2;
            }
            if (mergeCommitMap.containsKey(path)) {
                commonPath += 4;
            }
            if ((commonPath == 3 && (!splitPointMap.get(path).equals(newCommitMap.get(path))))
                    || (commonPath == 5
                        && (!splitPointMap.get(path).equals(mergeCommitMap.get(path))))
                    || (commonPath == 6
                        && (!newCommitMap.get(path).equals(mergeCommitMap.get(path))))
                    || (commonPath == 7
                        && (!splitPointMap.get(path).equals(newCommitMap.get(path)))
                        && (!splitPointMap.get(path).equals(mergeCommitMap.get(path)))
                        && (!newCommitMap.get(path).equals(mergeCommitMap.get(path))))) {
                conflict = true;
                String currBranchContents = "";
                if (newCommitMap.containsKey(path)) {
                    Blob newCommitBlob = readObject(
                            join(OBJECTS_DIR, newCommitMap.get(path)), Blob.class);
                    currBranchContents = new String(newCommitBlob.getContents(),
                            StandardCharsets.UTF_8);
                }

                String givenBranchContents = "";
                if (mergeCommitMap.containsKey(path)) {
                    Blob mergeCommitBlob = readObject(
                            join(OBJECTS_DIR, mergeCommitMap.get(path)), Blob.class);
                    givenBranchContents = new String(mergeCommitBlob.getContents(),
                            StandardCharsets.UTF_8);
                }

                String conflictContents = "<<<<<<< HEAD\n" + currBranchContents + "=======\n"
                        + givenBranchContents + ">>>>>>>\n";
                String fileName = readObject(join(OBJECTS_DIR, blobID), Blob.class)
                        .getFilename().getName();
                File conflictFile = join(CWD, fileName);
                writeContents(conflictFile, conflictContents);
            }
        }
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private static List<String> changeBlobIdListToFilenameList(List<String> blobIdList) {
        List<String> fileNameList = new ArrayList<>();
        for (String id : blobIdList) {
            Blob blob = readObject(join(OBJECTS_DIR, id), Blob.class);
            fileNameList.add(blob.getFilename().getName());
        }
        return fileNameList;
    }

    private static List<String> computeDeleteFiles(Commit splitPoint, Commit newCommit,
                                                   Commit mergeCommit) {
        Map<String, String> splitPointMap = splitPoint.getFilePathToBlobId();
        Map<String, String> newCommitMap = newCommit.getFilePathToBlobId();
        Map<String, String> mergeCommitMap = mergeCommit.getFilePathToBlobId();
        List<String> deleteFiles = new ArrayList<>();
        for (String path : splitPointMap.keySet()) {
            if (newCommitMap.containsKey(path) && (!mergeCommitMap.containsKey(path))) {
                deleteFiles.add(newCommitMap.get(path));
            }
        }
        return deleteFiles;
    }


    private static List<String> computeWriteFiles(Commit splitPoint, Commit newCommit,
                                                  Commit mergeCommit) {
        Map<String, String> splitPointMap = splitPoint.getFilePathToBlobId();
        Map<String, String> newCommitMap = newCommit.getFilePathToBlobId();
        Map<String, String> mergeCommitMap = mergeCommit.getFilePathToBlobId();
        List<String> writeFiles = new ArrayList<>();
        for (String path : mergeCommitMap.keySet()) {
            if ((!splitPointMap.containsKey(path)) && (!newCommitMap.containsKey(path))) {
                writeFiles.add(mergeCommitMap.get(path));
            }
        }
        return writeFiles;
    }

    private static List<String> computeOverwriteFiles(Commit splitPoint, Commit newCommit,
                                                      Commit mergedCommit) {
        Map<String, String> splitPointMap = splitPoint.getFilePathToBlobId();
        Map<String, String> newCommitMap = newCommit.getFilePathToBlobId();
        Map<String, String> mergeCommitMap = mergedCommit.getFilePathToBlobId();
        List<String> overwriteFiles = new ArrayList<>();
        for (String path : splitPointMap.keySet()) {
            if (newCommitMap.containsKey(path) && mergeCommitMap.containsKey(path)) {
                if ((splitPointMap.get(path).equals(newCommitMap.get(path)))
                        && (!splitPointMap.get(path).equals(mergeCommitMap.get(path)))) {
                    overwriteFiles.add(mergeCommitMap.get(path));
                }
            }
        }
        return overwriteFiles;
    }

    private static Commit findSplitPoint(Commit first, Commit second) {
        Map<String, Integer> firstIdToLength = computeCommitMap(first, 0);
        Map<String, Integer> secondIdToLength = computeCommitMap(second, 0);
        return computeSplitPoint(firstIdToLength, secondIdToLength);
    }

    private static Commit computeSplitPoint(Map<String, Integer> first,
                                             Map<String, Integer> second) {
        int minLength = Integer.MAX_VALUE;
        String minId = "";
        for (String id : first.keySet()) {
            if (second.containsKey(id) && second.get(id) < minLength) {
                minId = id;
                minLength = second.get(id);
            }
        }
        return getCommitById(minId);
    }

    private static Map<String, Integer> computeCommitMap(Commit commit, int length) {
        Map<String, Integer> map = new HashMap<>();
        if (commit.getParents().isEmpty()) {
            map.put(commit.getId(), length);
            return map;
        }
        map.put(commit.getId(), length);
        length++;
        for (String id : commit.getParents()) {
            Commit parent = getCommitById(id);
            map.putAll(computeCommitMap(parent, length));
        }
        return map;
    }
}
