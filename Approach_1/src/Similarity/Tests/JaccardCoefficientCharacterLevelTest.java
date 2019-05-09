package Similarity.Tests;

import Similarity.Tokenizer.CustomStringTokenizer;
import Similarity.Comparator.Strings.JaccardCoefficientStringComparator;
import Similarity.Tokenizer.StringCharTokenizer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JaccardCoefficientCharacterLevelTest {

    private JaccardCoefficientStringComparator comperator;

    @Before
    public void initFileds() {
        CustomStringTokenizer tokenizer = new StringCharTokenizer();
        comperator = new JaccardCoefficientStringComparator(tokenizer);
    }

    @Test
    public void TestTwoSimpleStrings() {
        String s1 = "nils";
        String s2 = "nilz";

        double expected = 3/5.0;
        double result = comperator.compare(s1,s2);

        Assert.assertTrue(expected == result);
    }

    @Test
    public void TestOneEmptyString() {
        String s1 = "";
        String s2 = "nils";

        double expected = 0;
        double result = comperator.compare(s1,s2);

        Assert.assertTrue(expected == result);
    }

    @Test
    public void TestTwoEmptyStrings() {
        String s1 = "";
        String s2 = "";

        double expected = 1;
        double result = comperator.compare(s1,s2);

        Assert.assertTrue(expected == result);
    }
}
