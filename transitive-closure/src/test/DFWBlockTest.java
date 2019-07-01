package test;

import main.DFWBlock;
import main.DFWPosition;
import main.SubMatrix;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class DFWBlockTest {

    private int[][] testMatrix = new int[10][10];

    @Before
    public void before() {
        //                                  y
        //                       ------------------------------------
        this.testMatrix[0] = new int[]{0, 1, 1, 3, 4, 8, 8, 2, 2, 4}; // |
        this.testMatrix[1] = new int[]{9, 0, 2, 4, 4, 7, 9, 9, 3, 4}; // |
        this.testMatrix[2] = new int[]{7, 1, 0, 2, 1, 3, 1, 3, 4, 4}; // |  x
        this.testMatrix[3] = new int[]{6, 4, 1, 0, 5, 7, 2, 4, 4, 1}; // |
        this.testMatrix[4] = new int[]{8, 8, 6, 7, 0, 3, 9, 5, 2, 3}; // |
        this.testMatrix[5] = new int[]{5, 8, 3, 7, 6, 0, 7, 8, 7, 4}; // |
        this.testMatrix[6] = new int[]{8, 2, 5, 1, 5, 5, 0, 7, 5, 3}; // |
        this.testMatrix[7] = new int[]{6, 8, 2, 9, 1, 6, 9, 0, 5, 9}; // |
        this.testMatrix[8] = new int[]{3, 8, 5, 8, 4, 9, 6, 8, 0, 4}; // |
        this.testMatrix[9] = new int[]{8, 7, 7, 6, 2, 7, 7, 1, 6, 0}; // |
    }

    @Test
    public void testCalculateTargetPlusOne() {
        SubMatrix target = new SubMatrix(testMatrix, 6,2, 2);
        DFWPosition pivot = new DFWPosition(2,2);

        // in this case the only path is the pivot element
        SubMatrix path = new SubMatrix(this.testMatrix, 2, 2, 2);

        DFWBlock dfwBlock = new DFWBlock(target, pivot);
        dfwBlock.addSubMatrix(path);

        dfwBlock.calculate();

        int[][] targetMatrix = dfwBlock.getTarget().getSubMatrix();

        int[][] expected = new int[2][2];
        expected[0] = new int[]{2, 1};
        expected[1] = new int[]{2, 4};

        Assert.assertTrue(Arrays.deepEquals(expected, targetMatrix));
    }

    @Test
    public void testCalculateTargetPlusTwoInCross() {
        SubMatrix target = new SubMatrix(testMatrix, 6,6, 2);
        DFWPosition pivot = new DFWPosition(2,2);

        SubMatrix p1 = new SubMatrix(this.testMatrix, 2, 6, 2);
        SubMatrix p2 = new SubMatrix(this.testMatrix, 6, 2, 2);

        DFWBlock dfwBlock = new DFWBlock(target, pivot);
        dfwBlock.addSubMatrix(p1);
        dfwBlock.addSubMatrix(p2);

        dfwBlock.calculate();

        int[][] targetMatrix = dfwBlock.getTarget().getSubMatrix();

        int[][] expected = new int[2][2];
        expected[0] = new int[]{0, 5};
        expected[1] = new int[]{3, 0};

        Assert.assertTrue(Arrays.deepEquals(expected, targetMatrix));
    }

    @Test
    public void testCalculateTargetPlusTwoOutOfCross() {
        SubMatrix target = new SubMatrix(testMatrix, 0,6, 2);
        DFWPosition pivot = new DFWPosition(2,2);

        SubMatrix p1 = new SubMatrix(this.testMatrix, 0, 2, 2);
        SubMatrix p2 = new SubMatrix(this.testMatrix, 2, 6, 2);

        DFWBlock dfwBlock = new DFWBlock(target, pivot);
        dfwBlock.addSubMatrix(p1);
        dfwBlock.addSubMatrix(p2);

        dfwBlock.calculate();

        int[][] targetMatrix = dfwBlock.getTarget().getSubMatrix();

        int[][] expected = new int[2][2];
        expected[0] = new int[]{2, 2};
        expected[1] = new int[]{3, 5};

        Assert.assertTrue(Arrays.deepEquals(expected, targetMatrix));
    }

}
