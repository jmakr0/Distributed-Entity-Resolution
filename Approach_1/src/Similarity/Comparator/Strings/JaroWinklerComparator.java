package Similarity.Comparator.Strings;

import info.debatty.java.stringsimilarity.JaroWinkler;

public class JaroWinklerComparator implements StringComparator {

    private JaroWinkler jw = new JaroWinkler();

    public double compare(String s1, String s2) {
        return jw.similarity(s1, s2);
    }
}
