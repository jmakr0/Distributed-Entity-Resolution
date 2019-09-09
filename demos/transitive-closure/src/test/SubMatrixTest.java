package test;

import main.Position;
import main.SubMatrix;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SubMatrixTest {

    private int[][] testMatrix = new int[6][6];

    @Before
    public void before() {
        //                                  y
        //                       -----------------------------
        this.testMatrix[0] = new int[]{0, 1, 2, 3, 4, 5}; // |
        this.testMatrix[1] = new int[]{1, 2, 3, 4, 5, 6}; // |
        this.testMatrix[2] = new int[]{2, 3, 4, 5, 6, 7}; // |  x
        this.testMatrix[3] = new int[]{3, 4, 5, 6, 7, 8}; // |
        this.testMatrix[4] = new int[]{4, 5, 6, 7, 8, 9}; // |
        this.testMatrix[5] = new int[]{5, 6, 7, 8, 9, 10};// |

    }

    @Test
    public void testFill() {
        Position pos = new Position(1, 1);
        SubMatrix result = new SubMatrix(testMatrix, pos, 2);

        int [][] expected = new int [2][2];
        expected[0] = new int[]{2, 3};
        expected[1] = new int[]{3, 4};

        Assert.assertArrayEquals(expected, result.getSubMatrix());
    }

    @Test
    public void testFillOverflow() {
        Position pos = new Position(4, 4);
        SubMatrix result = new SubMatrix(testMatrix, pos, 3);

        int [][] expected = new int [3][3];
        expected[0] = new int[]{8, 9, Integer.MAX_VALUE};
        expected[1] = new int[]{9, 10, Integer.MAX_VALUE};
        expected[2] = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};

        Assert.assertArrayEquals(expected, result.getSubMatrix());
    }

    @Test
    public void testGetMatrixValue() {
        Position pos = new Position(2, 4);
        SubMatrix sub = new SubMatrix(testMatrix, pos, 2);

        Assert.assertEquals(6, sub.getValue(2, 4));
        Assert.assertEquals(7, sub.getValue(2, 5));
        Assert.assertEquals(7, sub.getValue(3, 4));
        Assert.assertEquals(8, sub.getValue(3, 5));
    }

    @Test
    public void testContains() {
        Position pos = new Position(2, 4);
        SubMatrix sub = new SubMatrix(testMatrix, pos, 2);

        Assert.assertTrue(sub.contains(2, 4));
        Assert.assertTrue(sub.contains(2, 5));
        Assert.assertTrue(sub.contains(3, 4));
        Assert.assertTrue(sub.contains(3, 5));

        Assert.assertFalse(sub.contains(0, 0));
        Assert.assertFalse(sub.contains(3, 6));
        Assert.assertFalse(sub.contains(1, 4));
    }

}
