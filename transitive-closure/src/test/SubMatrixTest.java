package test;

import main.SubMatrix;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;

public class SubMatrixTest {

    int[][] testMatrix = new int[6][6];

    @Before
    public void before() {

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
    public void testFill() {
        SubMatrix result = new SubMatrix(testMatrix, new Point(0,0), 2);

        int [][] expected = new int [2][2];

        int [] line1 = {0, 6};
        int [] line2 = {2, 0};

        expected[0] = line1;
        expected[1] = line2;

        Assert.assertArrayEquals(expected, result.getMatrix());
    }

    @Test
    public void testFillOverflow() {
        SubMatrix result = new SubMatrix(testMatrix, new Point(4,4), 3);

        int [][] expected = new int [3][3];

        int [] line1 = {0, Integer.MAX_VALUE, Integer.MAX_VALUE};
        int [] line2 = {1, 0, Integer.MAX_VALUE};
        int [] line3 = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};

        expected[0] = line1;
        expected[1] = line2;
        expected[2] = line3;

        Assert.assertArrayEquals(expected, result.getMatrix());
    }

}
