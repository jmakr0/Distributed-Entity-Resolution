package test.de.hpi.utils.helper;

import de.hpi.utils.helper.SetOperations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class SetOperationsTest {

    Set<String> emptySet;
    Set<String> sizeOneSet;
    Set<String> normalSet1;
    Set<String> normalSet2;

    @Before
    public void initSets() {
        emptySet = new HashSet<String>();

        sizeOneSet = new HashSet<String>();
        sizeOneSet.add("test");

        normalSet1 = new HashSet<String>();
        normalSet1.add("a");
        normalSet1.add("b");
        normalSet1.add("c");
        normalSet1.add("d");

        normalSet2 = new HashSet<String>();
        normalSet2.add("a");
        normalSet2.add("b");
        normalSet2.add("c");
        normalSet2.add("e");
    }

    @Test
    public void testUnionWithEmptySet() {
        HashSet<String> expected = new HashSet<String>();
        expected.add("test");

        Assert.assertEquals(expected, SetOperations.union(sizeOneSet,emptySet));
    }

    @Test
    public void testUnionWithNonEmptySets() {
        HashSet<String> expected = new HashSet<String>();
        expected.add("test");
        expected.add("a");
        expected.add("b");
        expected.add("c");
        expected.add("d");

        Assert.assertEquals(expected,SetOperations.union(sizeOneSet,normalSet1));
    }

    @Test
    public void testIntersectionWithEmptySet() {
        HashSet<String> expected = new HashSet<String>();

        Assert.assertEquals(expected,SetOperations.intersection(sizeOneSet,emptySet));
    }

    @Test
    public void testIntersectionWithDistinctSets() {
        HashSet<String> expected = new HashSet<String>();

        Assert.assertEquals(expected,SetOperations.intersection(sizeOneSet,normalSet1));
    }

    @Test
    public void testIntersectionWithNonEmptySets() {
        HashSet<String> expected = new HashSet<String>();
        expected.add("a");
        expected.add("b");
        expected.add("c");

        Assert.assertEquals(expected,SetOperations.intersection(normalSet2,normalSet1));
    }

    @Test
    public void testSetDiffWithEmptySet() {
        Assert.assertEquals(normalSet2,SetOperations.setDiff(normalSet2,emptySet));
    }

    @Test
    public void testSetDiffWithNormalSet() {
        HashSet<String> expected = new HashSet<String>();
        expected.add("d");
        Assert.assertEquals(expected,SetOperations.setDiff(normalSet1,normalSet2));
    }
}
