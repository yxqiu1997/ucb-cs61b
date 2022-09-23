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
            default:
        }
    }
}
