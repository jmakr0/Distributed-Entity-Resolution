package Similarity.Tokenizer;

public class StringCharTokenizer implements CustomStringTokenizer {

    public String[] tokenize(String s) {
        return s.split("");
    }
}
