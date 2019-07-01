package Similarity.Tests;

import Similarity.Tokenizer.CustomStringTokenizer;
import Similarity.Tokenizer.StringSpaceTokenizer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StringSpaceTokenizerTest{

    private CustomStringTokenizer tokenizer;

    @Before
    public void initTokenizer() {
        this.tokenizer = new StringSpaceTokenizer();
    }

    @Test
    public void TestOneSpace() {
        String[] expected = {"aa","bb"};
        Assert.assertArrayEquals(expected,tokenizer.tokenize("aa bb"));
    }

    @Test
    public void TestThreeSpaces() {
        String[] expected = {"aa","bb","cc","dd"};
        Assert.assertArrayEquals(expected,tokenizer.tokenize("aa bb cc dd"));
    }

    @Test
    public void TestZeroSpaces() {
        String[] expected = {"aabb"};
        Assert.assertArrayEquals(expected,tokenizer.tokenize("aabb"));
    }

    @Test
    public void TestEmptyString() {
        String[] expected = {""};
        Assert.assertArrayEquals(expected,tokenizer.tokenize(""));
    }

//    @Test
//    public void TestTwoSpacesInARow() {
//        // TODO discuss what makes sense here
//        String[] expected = {"aa", " bb"};
//        String[] res = tokenizer.tokenize("aa  bb");
//        Assert.assertArrayEquals(expected,tokenizer.tokenize("aa  bb"));
//    }

    @Test
    public void TestThreeSpacesInARow() {
        // TODO discuss what makes sense here
//        String[] expected = {"aa", " bb"};
//        String[] res = tokenizer.tokenize("aa   bb");
//        Assert.assertArrayEquals(expected,tokenizer.tokenize("aa   bb"));
    }


}

