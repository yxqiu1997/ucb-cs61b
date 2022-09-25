package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                checkArgs(args.length, 1);
                Repository.init();
                break;
            case "add":
                checkArgs(args.length, 2);
                Repository.checkIfInitialised();
                Repository.add(args[1]);
                break;
            case "commit":
                checkArgs(args.length, 2);
                Repository.checkIfInitialised();
                Repository.commit(args[1]);
                break;
            case "rm":
                checkArgs(args.length, 2);
                Repository.checkIfInitialised();
                Repository.rm(args[1]);
                break;
            case "log":
                checkArgs(args.length, 1);
                Repository.checkIfInitialised();
                Repository.log();
                break;
            case "global-log":
                checkArgs(args.length, 1);
                Repository.checkIfInitialised();
                Repository.globalLog();
                break;
            case "find":
                checkArgs(args.length, 2);
                Repository.checkIfInitialised();
                Repository.find(args[1]);
                break;
            case "status":
                checkArgs(args.length, 1);
                Repository.checkIfInitialised();
                Repository.status();
                break;
            case "checkout":
                if (args.length == 2) {
                    // java gitlet.Main checkout [branch name]
                    Repository.checkoutBranch(args[1]);
                } else if (args.length == 3) {
                    // java gitlet.Main checkout -- [file name]
                    checkEqual(args[1], "--");
                    Repository.checkout(args[2]);
                } else if (args.length == 4) {
                    // java gitlet.Main checkout [commit id] -- [file name]
                    checkEqual(args[2], "--");
                    Repository.checkout(args[1], args[3]);
                } else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                break;
            case "branch":
                checkArgs(args.length, 2);
                Repository.checkIfInitialised();
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                checkArgs(args.length, 2);
                Repository.checkIfInitialised();
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                checkArgs(args.length, 2);
                Repository.checkIfInitialised();
                Repository.reset(args[1]);
                break;
            case "merge":
                checkArgs(args.length, 2);
                Repository.checkIfInitialised();
                Repository.merge(args[1]);
                break;
            default:
        }
    }

    private static void checkEqual(String actualStr, String expectedStr) {
        if (!actualStr.equals(expectedStr)) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    private static void checkArgs(int actualLength, int expectedLength) {
        if (actualLength != expectedLength) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
