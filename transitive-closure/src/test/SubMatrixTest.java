package test;

import main.DFWPosition;
import main.SubMatrix;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class SubMatrixTest {

    private int[][] testMatrix = new int[6][6];

    @Before
    public void before() {
        this.testMatrix[0] = new int[]{0, 1, 2, 3, 4, 5};
        this.testMatrix[1] = new int[]{1, 2, 3, 4, 5, 6};
        this.testMatrix[2] = new int[]{2, 3, 4, 5, 6, 7};
        this.testMatrix[3] = new int[]{3, 4, 5, 6, 7, 8};
        this.testMatrix[4] = new int[]{4, 5, 6, 7, 8, 9};
        this.testMatrix[5] = new int[]{5, 6, 7, 8, 9, 10};
    }

    @Test
    public void testFill() {
        SubMatrix result = new SubMatrix(testMatrix, 1, 1, 2);

        int [][] expected = new int [2][2];
        expected[0] = new int[]{2, 3};
        expected[1] = new int[]{3, 4};

        Assert.assertArrayEquals(expected, result.getMatrix());
    }

    @Test
    public void testFillOverflow() {
        SubMatrix result = new SubMatrix(testMatrix, 4,4, 3);

        int [][] expected = new int [3][3];
        expected[0] = new int[]{8, 9, Integer.MAX_VALUE};
        expected[1] = new int[]{9, 10, Integer.MAX_VALUE};
        expected[2] = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};

        Assert.assertArrayEquals(expected, result.getMatrix());
    }

    @Test
    public void testNeighbors() {
        Set<DFWPosition> result = new SubMatrix(testMatrix, 2, 2, 2).getNeighborPositions();

        Set<DFWPosition> expected = new HashSet<>();
        expected.add(new DFWPosition(2,0));
        expected.add(new DFWPosition(0,2));
        expected.add(new DFWPosition(4,2));
        expected.add(new DFWPosition(2,4));

        Assert.assertTrue(result.containsAll(expected) && result.size() == expected.size());
    }

    @Test
    public void testEdgeNeighbors() {
        Set<DFWPosition> result = new SubMatrix(testMatrix, 0, 0, 2).getNeighborPositions();

        Set<DFWPosition> expected = new HashSet<>();
        expected.add(new DFWPosition(2,0));
        expected.add(new DFWPosition(0,2));

        Assert.assertTrue(result.containsAll(expected) && result.size() == expected.size());
    }

}
