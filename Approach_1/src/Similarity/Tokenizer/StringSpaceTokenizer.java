package Similarity.Tokenizer;

public class StringSpaceTokenizer implements CustomStringTokenizer {

    public String[] tokenize(String s) {
        return s.split(" ");
    }
}
