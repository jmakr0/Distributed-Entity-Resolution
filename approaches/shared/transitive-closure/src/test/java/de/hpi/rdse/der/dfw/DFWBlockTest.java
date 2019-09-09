package de.hpi.rdse.der.dfw;

import de.hpi.rdse.der.util.Position;
import de.hpi.rdse.der.util.SubMatrix;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
        Position pos = new Position(6, 2);
        Position pivot = new Position(2,2);

        SubMatrix target = new SubMatrix(testMatrix, pos, 2);
        List<SubMatrix> subMatrices = new LinkedList<>();

        // in this case the only path is the pivot element
        SubMatrix path = new SubMatrix(this.testMatrix, pivot, 2);
        subMatrices.add(path);

        DFWBlock dfwBlock = new DFWBlock(target, pivot, subMatrices);

        dfwBlock.calculate();

        int[][] targetMatrix = dfwBlock.getTarget().getSubMatrix();

        int[][] expected = new int[2][2];
        expected[0] = new int[]{2, 1};
        expected[1] = new int[]{2, 4};

        Assert.assertTrue(Arrays.deepEquals(expected, targetMatrix));
    }

    @Test
    public void testCalculateTargetPlusTwoInCross() {
        Position pivot = new Position(2,2);
        Position pos = new Position(6, 6);
        Position pos1 = new Position(2, 6);
        Position pos2 = new Position(6, 2);
        List<SubMatrix> subMatrices = new LinkedList<>();

        SubMatrix target = new SubMatrix(testMatrix, pos, 2);

        SubMatrix sub1 = new SubMatrix(this.testMatrix, pos1, 2);
        SubMatrix sub2 = new SubMatrix(this.testMatrix, pos2, 2);

        subMatrices.add(sub1);
        subMatrices.add(sub2);

        DFWBlock dfwBlock = new DFWBlock(target, pivot, subMatrices);

        dfwBlock.calculate();

        int[][] targetMatrix = dfwBlock.getTarget().getSubMatrix();

        int[][] expected = new int[2][2];
        expected[0] = new int[]{0, 5};
        expected[1] = new int[]{3, 0};

        Assert.assertTrue(Arrays.deepEquals(expected, targetMatrix));
    }

    @Test
    public void testCalculateTargetPlusTwoOutOfCross() {
        Position pivot = new Position(2,2);
        Position pos = new Position(0, 6);
        Position pos1 = new Position(0, 2);
        Position pos2 = new Position(2, 6);
        List<SubMatrix> subMatrices = new LinkedList<>();

        SubMatrix target = new SubMatrix(testMatrix, pos, 2);
        SubMatrix sub1 = new SubMatrix(this.testMatrix, pos1, 2);
        SubMatrix sub2 = new SubMatrix(this.testMatrix, pos2, 2);

        subMatrices.add(sub1);
        subMatrices.add(sub2);

        DFWBlock dfwBlock = new DFWBlock(target, pivot, subMatrices);

        dfwBlock.calculate();

        int[][] targetMatrix = dfwBlock.getTarget().getSubMatrix();

        int[][] expected = new int[2][2];
        expected[0] = new int[]{2, 2};
        expected[1] = new int[]{3, 5};

        Assert.assertTrue(Arrays.deepEquals(expected, targetMatrix));
    }

}
