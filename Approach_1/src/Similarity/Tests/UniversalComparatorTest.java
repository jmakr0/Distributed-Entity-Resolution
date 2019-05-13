package Similarity.Tests;

import Similarity.Comparator.*;
import Similarity.Comparator.Numbers.NumberComparator;
import Similarity.Comparator.Numbers.ABSBasedNumberComparator;
import Similarity.Comparator.Strings.JaccardCoefficientStringComparator;
import Similarity.Comparator.Strings.StringComparator;
import Similarity.Tokenizer.CustomStringTokenizer;
import Similarity.Tokenizer.StringCharTokenizer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UniversalComparatorTest {

    UniversalComparator uvc;

    @Before
    public void initComparator() {
        CustomStringTokenizer tokenizer = new StringCharTokenizer();
        StringComparator sComparator = new JaccardCoefficientStringComparator(tokenizer);
        NumberComparator nComparator = new ABSBasedNumberComparator(3,15);
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
        String[] a2 = {"Maximilian", "Korschewski", "28"};

        Assert.assertTrue(0.5 > uvc.compare(a1,a2));
    }

}
