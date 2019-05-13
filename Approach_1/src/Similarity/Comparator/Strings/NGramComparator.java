package Similarity.Comparator.Strings;

import info.debatty.java.stringsimilarity.NGram;

public class NGramComparator implements StringComparator {

    private NGram ng = new NGram();

    public double compare(String s1, String s2) {
        return 1 - ng.distance(s1, s2);
    }
}
