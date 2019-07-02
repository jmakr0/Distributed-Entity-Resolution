package Similarity.Tests;

import Similarity.Tokenizer.CustomStringTokenizer;
import Similarity.Tokenizer.StringCharTokenizer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StringCharTokenizerTest {

    private CustomStringTokenizer tokenizer;

    @Before
    public void initTokenizer() {
        this.tokenizer = new StringCharTokenizer();
    }

    @Test
    public void TestNormalString() {
        String[] expected = {"a","a","b","b"};
        Assert.assertArrayEquals(expected,tokenizer.tokenize("aabb"));
    }

    @Test
    public void TestStringLengthOne() {
        String[] expected = {"a"};
        Assert.assertArrayEquals(expected,tokenizer.tokenize("a"));
    }

    @Test
    public void TestEmptyString() {
        String[] expected = {""};
        Assert.assertArrayEquals(expected,tokenizer.tokenize(""));
    }

    @Test
    public void TestStringWithSpaces() {
        // TODO discuss what makes sense here
        String[] expected = {"a","a"," "," "," ","b","b"};
        Assert.assertArrayEquals(expected,tokenizer.tokenize("aa   bb"));
    }
}
