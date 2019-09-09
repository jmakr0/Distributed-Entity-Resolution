package de.hpi.rdse.der.fw;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FloydWarshallTest {

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
    public void testAlgorithm() {

        int[][] expectedOutput = new int[6][6];

        int [] line7 =  {0,  6,  1, 6, 4, 8};
        int [] line8 =  {2,  0,  3, 8, 6, 10};
        int [] line9 =  {8,  6,  0, 5, 3, 7};
        int [] line10 = {12, 10, 4, 0, 3, 2};
        int [] line11 = {14, 12, 6, 2, 0, 4};
        int [] line12 = {15, 13, 7, 3, 1, 0};

        expectedOutput[0] = line7;
        expectedOutput[1] = line8;
        expectedOutput[2] = line9;
        expectedOutput[3] = line10;
        expectedOutput[4] = line11;
        expectedOutput[5] = line12;

        int[][] result = FloydWarshall.apply(testMatrix);

        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result.length; j++) {
                Assert.assertTrue(result[i][j] == expectedOutput[i][j]);
            }
        }
    }

}
