package test;

import main.DFW;
import main.DFWBlock;
import main.FloydWarshall;
import main.SubMatrix;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DFWTest {


    int[][] testMatrix = new int[6][6];

    @Before
    public void before() {
        // Example taken from:
        // Optimierung für Studierende der Informatik (Uni Hamburg)
        // THOMAS ANDREAE, Wintersemester 2015/16
        testMatrix[0] = new int[]{0, 6, 1, 10, Integer.MAX_VALUE, Integer.MAX_VALUE};
        testMatrix[1] = new int[]{2, 0, 4, Integer.MAX_VALUE, 9, Integer.MAX_VALUE};
        testMatrix[2] = new int[]{Integer.MAX_VALUE, 6, 0, 6, 3, Integer.MAX_VALUE};
        testMatrix[3] = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, 4, 0, 4, 2};
        testMatrix[4] = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 2, 0, Integer.MAX_VALUE};
        testMatrix[5] = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE , 1, 0};
    }

    @Test
    public void getWorkTest() {
        DFW dfw = new DFW(testMatrix, 4);

        int[][] expected = new int[6][6];
        expected[0] = new int[]{0,  6,  1, 6, 4, 8};
        expected[1] = new int[]{2,  0,  3, 8, 6, 10};
        expected[2] = new int[]{8,  6,  0, 5, 3, 7};
        expected[3] = new int[]{12, 10, 4, 0, 3, 2};
        expected[4] = new int[]{14, 12, 6, 2, 0, 4};
        expected[5] = new int[]{15, 13, 7, 3, 1, 0};

        while(!dfw.isDone()) {
            DFWBlock block = dfw.getWork();
            block.calculate();
            dfw.dispatch(block.getTarget());
        }

        int[][] result = dfw.getMatrix();

        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result.length; j++) {
                Assert.assertTrue(result[i][j] == expected[i][j]);
            }
        }
    }

}
