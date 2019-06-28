package test;

import main.DFW;
import main.DFWBlock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class DFWTest {


    int[][] testMatrix = new int[6][6];
    int[][] testMatrixExpected = new int[6][6];

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

        testMatrixExpected[0] = new int[]{0,  6,  1, 6, 4, 8};
        testMatrixExpected[1] = new int[]{2,  0,  3, 8, 6, 10};
        testMatrixExpected[2] = new int[]{8,  6,  0, 5, 3, 7};
        testMatrixExpected[3] = new int[]{12, 10, 4, 0, 3, 2};
        testMatrixExpected[4] = new int[]{14, 12, 6, 2, 0, 4};
        testMatrixExpected[5] = new int[]{15, 13, 7, 3, 1, 0};
    }

    private int[][] calculate(int blksize) {
        DFW dfw = new DFW(testMatrix, blksize);

        while(!dfw.calculated()) {
            DFWBlock block = dfw.getBlock();
            block.calculate();
            dfw.dispatch(block.getTarget());
        }

        return dfw.getMatrix();
    }

    @Test
    public void blocksize1Test() {
        int[][] result = this.calculate(1);

        Assert.assertTrue(Arrays.deepEquals(testMatrixExpected, result));
    }

    @Test
    public void blocksize2Test() {
        int[][] result = this.calculate(2);

        Assert.assertTrue(Arrays.deepEquals(testMatrixExpected, result));
    }

    @Test
    public void blocksize3Test() {
        int[][] result = this.calculate(3);

        Assert.assertTrue(Arrays.deepEquals(testMatrixExpected, result));
    }

    @Test
    public void blocksize4Test() {
        int[][] result = this.calculate(4);

        Assert.assertTrue(Arrays.deepEquals(testMatrixExpected, result));
    }

    @Test
    public void blocksize5Test() {
        int[][] result = this.calculate(5);

        Assert.assertTrue(Arrays.deepEquals(testMatrixExpected, result));
    }

    @Test
    public void blocksize8Test() {
        int[][] result = this.calculate(8);

        Assert.assertTrue(Arrays.deepEquals(testMatrixExpected, result));
    }

}
