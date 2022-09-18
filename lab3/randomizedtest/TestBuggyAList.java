package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {

    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> correct = new AListNoResizing<>(){{
            addLast(4);
            addLast(5);
            addLast(6);
        }};
        BuggyAList<Integer> broken = new BuggyAList<>(){{
            addLast(4);
            addLast(5);
            addLast(6);
        }};
        assertEquals(correct.size(), broken.size());
        for (int i = 0; i < correct.size(); ++i) {
            assertEquals(correct.removeLast(), broken.removeLast());
        }
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        BuggyAList<Integer> broken = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                correct.addLast(randVal);
                broken.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int size = correct.size();
                assertEquals(correct.size(), broken.size());
            } else if (correct.size() <= 0) {
                continue;
            } else if (operationNumber == 2) {
                // getLast
                assertEquals(correct.getLast(), broken.getLast());
            } else if (operationNumber == 3) {
                // removeLast
                assertEquals(correct.removeLast(), broken.removeLast());
            }
        }
    }

}
