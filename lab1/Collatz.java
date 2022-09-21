/** Class that prints the Collatz sequence starting from a given number.
 *  @author YOUR NAME HERE
 */
public class Collatz {

    /**
     * Next number in Collatz sequence
     * @param n the given number
     * @return next number of the sequence
     */
    public static int nextNumber(int n) {
        if (n <= 1) {
            throw new IllegalArgumentException();
        }
        return n % 2 == 0 ? n / 2 : 3 * n + 1;
    }

    public static void main(String[] args) {
        int n = 5;
        System.out.print(n + " ");
        while (n != 1) {
            n = nextNumber(n);
            System.out.print(n + " ");
        }
        System.out.println();
    }
}

