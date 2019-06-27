package test;

import main.FloydWarshall;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class FloydWarshallTest {

    int[][] testMatrix = new int[6][6];

    @Before
    public void before() {
        // Example taken from:
        // Optimierung für Studierende der Informatik (Uni Hamburg)
        // THOMAS ANDREAE
        // Wintersemester 2015/16
        int [] line1 = {0                   , 6                 , 1                , 10                , Integer.MAX_VALUE  , Integer.MAX_VALUE};
        int [] line2 = {2                   , 0                 , 4                , Integer.MAX_VALUE , 9                  , Integer.MAX_VALUE};
        int [] line3 = {Integer.MAX_VALUE   , 6                 , 0                , 6                 , 3                  , Integer.MAX_VALUE};
        int [] line4 = {Integer.MAX_VALUE   , Integer.MAX_VALUE , 4                , 0                 , 4                  , 2};
        int [] line5 = {Integer.MAX_VALUE   , Integer.MAX_VALUE , Integer.MAX_VALUE, 2                 , 0                  , Integer.MAX_VALUE};
        int [] line6 = {Integer.MAX_VALUE   , Integer.MAX_VALUE , Integer.MAX_VALUE, Integer.MAX_VALUE , 1                  , 0};

        testMatrix[0] = line1;
        testMatrix[1] = line2;
        testMatrix[2] = line3;
        testMatrix[3] = line4;
        testMatrix[4] = line5;
        testMatrix[5] = line6;
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

    @Test
    public void testPivots() {
        List<int[][]> pivots = new LinkedList<>();
//        List<int[][]> pivots = getPivots(testMatrix, 2);

        List<int[][]> expectedPivots = new LinkedList<>();

        int[][] expectedPivot1 = new int[2][2];
        int[][] expectedPivot2 = new int[2][2];
        int[][] expectedPivot3 = new int[2][2];

        int [] line1 = {0, 6};
        int [] line2 = {2, 0};
        int [] line3 = {0, 6};
        int [] line4 = {4, 0};
        int [] line5 = {0, Integer.MAX_VALUE};
        int [] line6 = {1, 0};

        expectedPivot1[0] = line1;
        expectedPivot1[1] = line2;
        expectedPivot2[0] = line3;
        expectedPivot2[1] = line4;
        expectedPivot3[0] = line5;
        expectedPivot3[1] = line6;

        expectedPivots.add(expectedPivot1);
        expectedPivots.add(expectedPivot2);
        expectedPivots.add(expectedPivot3);

        Assert.assertArrayEquals(pivots.toArray(), expectedPivots.toArray());

    }


}
