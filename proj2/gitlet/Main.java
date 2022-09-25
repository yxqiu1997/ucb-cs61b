package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *
 *  @author Qiu Yuxuan
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        Repository repository = new Repository();
        switch(firstArg) {
            case "init":
                repository.checkOperands(args.length, 1);
                repository.init();
                break;
            case "add":
                repository.checkOperands(args.length, 2);
                repository.checkInitialiseDirectoryExists();
                repository.add(args[1]);
                break;
            case "commit":
                repository.checkOperands(args.length, 2);
                repository.checkInitialiseDirectoryExists();
                repository.commit(args[1]);
                break;
            case "rm":
                repository.checkOperands(args.length, 2);
                repository.checkInitialiseDirectoryExists();
                repository.rm(args[1]);
                break;
            case "log":
                repository.checkOperands(args.length, 1);
                repository.checkInitialiseDirectoryExists();
                repository.log();
                break;
            case "global-log":
                repository.checkOperands(args.length, 1);
                repository.checkInitialiseDirectoryExists();
                repository.globalLog();
                break;
            case "find":
                repository.checkOperands(args.length, 2);
                repository.checkInitialiseDirectoryExists();
                repository.find(args[1]);
                break;
            case "status":
                repository.checkOperands(args.length, 1);
                repository.checkInitialiseDirectoryExists();
                repository.status();
                break;
            case "checkout":
                repository.checkInitialiseDirectoryExists();
                if (args.length == 2) {
                    // java gitlet.Main checkout [branch name]
                    repository.checkoutBranch(args[1]);
                } else if (args.length == 3) {
                    // java gitlet.Main checkout -- [file name]
                    repository.checkOperands(args[1], "--");
                    repository.checkoutFileFromHead(args[2]);
                } else if (args.length == 4) {
                    // java gitlet.Main checkout [commit id] -- [file name]
                    repository.checkOperands(args[2], "--");
                    repository.checkoutFileFromCommitId(args[1], args[3]);
                } else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                break;
            case "branch":
                repository.checkOperands(args.length, 2);
                repository.checkInitialiseDirectoryExists();
                repository.branch(args[1]);
                break;
            case "rm-branch":
                repository.checkOperands(args.length, 2);
                repository.checkInitialiseDirectoryExists();
                repository.rmBranch(args[1]);
                break;
            case "reset":
                repository.checkOperands(args.length, 2);
                repository.checkInitialiseDirectoryExists();
                repository.reset(args[1]);
                break;
            case "merge":
                repository.checkOperands(args.length, 2);
                repository.checkInitialiseDirectoryExists();
                repository.merge(args[1]);
                break;
            default:
        }
    }
}
