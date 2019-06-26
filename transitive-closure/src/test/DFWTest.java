package test;

import main.DFW;
import main.SubMatrix;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DFWTest {


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
    public void phaseTwoBlocksTest() {

        int blksize = 2;

        SubMatrix pivot = new SubMatrix(testMatrix, new Point(2,2), blksize);
        Set<List<SubMatrix>> tuples = DFW.generateTuples(testMatrix, pivot, blksize);

        SubMatrix tmpBlock;
        List<SubMatrix> tmpTuple;

        tmpBlock = new SubMatrix(testMatrix, new Point(0, 2), blksize);
        tmpTuple = new LinkedList<>();

        tmpTuple.add(pivot);
        tmpTuple.add(tmpBlock);

        Assert.assertTrue(tuples.contains(tmpTuple));

        tmpBlock = new SubMatrix(testMatrix, new Point(4, 2), blksize);
        tmpTuple = new LinkedList<>();

        tmpTuple.add(pivot);
        tmpTuple.add(tmpBlock);

        Assert.assertTrue(tuples.contains(tmpTuple));

        tmpBlock = new SubMatrix(testMatrix, new Point(2, 0), blksize);
        tmpTuple = new LinkedList<>();

        tmpTuple.add(pivot);
        tmpTuple.add(tmpBlock);

        Assert.assertTrue(tuples.contains(tmpTuple));

        tmpBlock = new SubMatrix(testMatrix, new Point(2, 4), blksize);
        tmpTuple = new LinkedList<>();

        tmpTuple.add(pivot);
        tmpTuple.add(tmpBlock);

        Assert.assertTrue(tuples.contains(tmpTuple));
    }

}
