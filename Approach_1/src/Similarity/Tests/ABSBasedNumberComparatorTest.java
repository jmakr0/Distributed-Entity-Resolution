package Similarity.Tests;

import Similarity.Comparator.Numbers.NumberComparator;
import Similarity.Comparator.Numbers.ABSBasedNumberComparator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ABSBasedNumberComparatorTest {

    NumberComparator numberComparator;

    double DELTA = 0.0001;

    @Before
    public void init(){
        numberComparator = new ABSBasedNumberComparator(5,20);
    }

    @Test
    public void testBiggerThanInterval() {
        Assert.assertEquals(0,numberComparator.compare(0,20), DELTA);
    }

    @Test
    public void testSmallerThanInterval() {
        Assert.assertEquals(1,numberComparator.compare(11,11), DELTA);
    }

    @Test
    public void testWithinInterval() {
        double result1 = numberComparator.compare(10,16);
        double result2 = numberComparator.compare(10,18);

        Assert.assertTrue(result1 > result2);
    }
}
