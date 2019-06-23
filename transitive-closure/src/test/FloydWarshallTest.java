package test;

import main.FloydWarshall;
import org.junit.Assert;
import org.junit.Test;

public class FloydWarshallTest {

    @Test
    public void testAlgorithm() {

        // Example taken from:
        // Optimierung für Studierende der Informatik (Uni Hamburg)
        // THOMAS ANDREAE
        // Wintersemester 2015/16

        int[][] input = new int[6][6];

        int [] line1 = {0                   , 6                 , 1                , 10                , Integer.MAX_VALUE  , Integer.MAX_VALUE};
        int [] line2 = {2                   , 0                 , 4                , Integer.MAX_VALUE , 9                  , Integer.MAX_VALUE};
        int [] line3 = {Integer.MAX_VALUE   , 6                 , 0                , 6                 , 3                  , Integer.MAX_VALUE};
        int [] line4 = {Integer.MAX_VALUE   , Integer.MAX_VALUE , 4                , 0                 , 4                  , 2};
        int [] line5 = {Integer.MAX_VALUE   , Integer.MAX_VALUE , Integer.MAX_VALUE, 2                 , 0                  , Integer.MAX_VALUE};
        int [] line6 = {Integer.MAX_VALUE   , Integer.MAX_VALUE , Integer.MAX_VALUE, Integer.MAX_VALUE , 1                  , 0};

        input[0] = line1;
        input[1] = line2;
        input[2] = line3;
        input[3] = line4;
        input[4] = line5;
        input[5] = line6;


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

        int[][] result = FloydWarshall.floydWarshall(input);

        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result.length; j++) {
                Assert.assertTrue(result[i][j] == expectedOutput[i][j]);
            }
        }
    }
}
