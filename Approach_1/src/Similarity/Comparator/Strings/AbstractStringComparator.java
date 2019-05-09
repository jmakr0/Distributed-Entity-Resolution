package Similarity.Comparator.Strings;

import Similarity.Tokenizer.CustomStringTokenizer;

public abstract class AbstractStringComparator implements StringComparator {

    CustomStringTokenizer tokenizer;

    public AbstractStringComparator(CustomStringTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }
}
