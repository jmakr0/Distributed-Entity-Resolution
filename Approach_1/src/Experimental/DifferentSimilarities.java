package Experimental;

import Similarity.Comparator.Strings.*;
import Similarity.Tokenizer.StringCharTokenizer;

public class DifferentSimilarities {

    public static void main(String[] args) {
        JaroWinklerComparator jw = new JaroWinklerComparator();
        StringComparator cp = new JaccardCoefficientStringComparator(new StringCharTokenizer());
        JaccardIndexComparator jc = new JaccardIndexComparator();
        NGramComparator ng = new NGramComparator();


        System.out.println(jw.compare("John Doe", "John Doe"));
        System.out.println(jw.compare("Prof. John Doe", "Dr. John Doe"));
        System.out.println("++++++++++++++++++++++");
        System.out.println(cp.compare("John Doe", "John Doe"));
        System.out.println(cp.compare("Prof. John Doe", "Dr. John Doe"));
        System.out.println("++++++++++++++++++++++");
        System.out.println(jc.compare("John Doe", "John Doe"));
        System.out.println(jc.compare("Prof. John Doe", "Dr. John Doe"));
        System.out.println("++++++++++++++++++++++");
        System.out.println(ng.compare("John Doe", "John Doe"));
        System.out.println(ng.compare("Prof. John Doe", "Dr. John Doe"));
    }

}
