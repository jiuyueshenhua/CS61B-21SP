package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */

public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove (){
        AListNoResizing<Integer> AR = new AListNoResizing<>();
        AR.addLast(3);
        AR.addLast(8);
        AR.addLast(9);
        BuggyAList<Integer> BA = new BuggyAList<>();
        BA.addLast(3);
        BA.addLast(8);
        BA.addLast(9);

        assertEquals(BA.getLast(), AR.getLast());

        AR.removeLast();
        BA.removeLast();
        assertEquals(BA.getLast(), AR.getLast());

        AR.removeLast();
        BA.removeLast();
        assertEquals(BA.getLast(),AR.getLast());


    }
    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> BL =new BuggyAList<>();
        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                BL.addLast(randVal);
                assertEquals(L.getLast(),BL.getLast());

                //System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                assertEquals(size,BL.size());
                //System.out.println("size: " + size);
            } else if(operationNumber ==2 ) {
                assertEquals(L.size(),BL.size());
                if(L.size() == 0 ) continue;
                L.removeLast();
                BL.removeLast();
                if (L.size() == 0) continue;
                assertEquals(L.getLast(), BL.getLast());
            } else if(operationNumber == 3 ) {
                assertEquals(L.size(), BL.size());
                if (L.size() == 0) continue;
                assertEquals(L.getLast(), BL.getLast());
            }
        }
    }

}
