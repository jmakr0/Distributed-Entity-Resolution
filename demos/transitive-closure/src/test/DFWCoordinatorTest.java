package test;

import main.DFWCoordinator;
import main.DFWPosition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DFWCoordinatorTest {

    private int matrixSize = 3;
    private int blkSize = 1;
    private Map<Integer, Set<DFWPosition>> testMatrix = new HashMap<>();

    @Before
    public void prepareTestMatrix() {
        int round = 0;

        this.testMatrix.put(round, new HashSet<>());
        this.testMatrix.get(round).add(new DFWPosition(0, 0));

        for (int k = 0; k < matrixSize; k += blkSize) {

            this.testMatrix.put(round, new HashSet<>());

            // tuple
            for (int i = 0; i < matrixSize; i += blkSize) {

                if (i != k) {
                    // x
                    this.testMatrix.get(round).add(new DFWPosition(i, k));
                    // y
                    this.testMatrix.get(round).add(new DFWPosition(k, i));

                    // triple
                    for (int j = 0; j < matrixSize; j += blkSize) {

                        if (j != k) {
                            this.testMatrix.get(round).add(new DFWPosition(j, i));
                            this.testMatrix.get(round).add(new DFWPosition(i, j));
                        }

                    }

                }

            }

            round ++;
        }
    }

    @Test
    public void calculated() {
        DFWCoordinator dfwCoordinator = new DFWCoordinator(matrixSize, blkSize);

        int round = 0;
        DFWPosition pos;

        while (dfwCoordinator.isNotDone()) {
            pos = dfwCoordinator.getNext();

            this.testMatrix.get(round).remove(pos);

            dfwCoordinator.calculated(pos);

            if (this.testMatrix.get(round).isEmpty()) {
                this.testMatrix.remove(round);
                round++;
            }
        }

        Assert.assertTrue(this.testMatrix.isEmpty());
    }



}
