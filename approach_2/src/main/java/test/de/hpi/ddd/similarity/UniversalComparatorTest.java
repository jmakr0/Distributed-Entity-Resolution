package test.de.hpi.ddd.similarity;

import de.hpi.ddd.similarity.UniversalComparator;
import de.hpi.ddd.similarity.numeric.AbsComparator;
import de.hpi.ddd.similarity.numeric.NumberComparator;
import de.hpi.ddd.similarity.strings.JaroWinklerComparator;
import de.hpi.ddd.similarity.strings.StringComparator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UniversalComparatorTest {

    UniversalComparator uvc;

    @Before
    public void initComparator() {
        NumberComparator nComparator = new AbsComparator(3,15);
        StringComparator sComparator = new JaroWinklerComparator();
        uvc = new UniversalComparator(sComparator, nComparator);
    }

    @Test
    public void compareSameArraysTest() {
        String[] a1 = {"Nils", "Straßenburg", "23"};
        String[] a2 = {"Nils", "Straßenburg", "23"};

        Assert.assertTrue(1 == uvc.compare(a1,a2));
    }

    @Test
    public void compareSlightlyDifferenzArraysTest() {
        String[] a1 = {"Nils", "Straßenburq", "23"};
        String[] a2 = {"Nils", "Straßenburg", "23"};

        Assert.assertTrue(1 != uvc.compare(a1,a2));
        Assert.assertTrue(0.9 <= uvc.compare(a1,a2));
    }

    @Test
    public void compareDifferenzArraysTest() {
        String[] a1 = {"Nils", "Straßenburg", "23"};
        String[] a2 = {"Maximilian", "Kroschewski", "28"};

        Assert.assertTrue(0.65 > uvc.compare(a1,a2));
    }

}
