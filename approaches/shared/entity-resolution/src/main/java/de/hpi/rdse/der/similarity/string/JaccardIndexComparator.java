package de.hpi.rdse.der.similarity.string;


import info.debatty.java.stringsimilarity.Jaccard;

public class JaccardIndexComparator implements StringComparator {

    private Jaccard jc = new Jaccard();

    public double compare(String s1, String s2) {
        return 1 - jc.distance(s1, s2);
    }
}
