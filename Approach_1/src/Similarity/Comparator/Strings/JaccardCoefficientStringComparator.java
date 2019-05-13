package Similarity.Comparator.Strings;

import Similarity.Tokenizer.CustomStringTokenizer;
import Util.ConversionHelper;
import Util.SetOperations;

import java.util.Set;

public class JaccardCoefficientStringComparator implements StringComparator {

    private CustomStringTokenizer tokenizer;

    /**
     * Initializes an Object of type JaccardCoefficientStringComparator
     * @param tokenizer A CustomStringTokenizer that is responsible for cutting the given string into parts for comparison.
     */
    public JaccardCoefficientStringComparator(CustomStringTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public double compare(String s1, String s2) {
        String[] tokenizedString1 = tokenizer.tokenize(s1);
        String[] tokenizedString2 = tokenizer.tokenize(s2);
        return computeJaccardCoefficient(tokenizedString1,tokenizedString2);
    }

    private double computeJaccardCoefficient(String[] stringArray1, String[] stringArray2) {
        Set<String> stringSet1 = ConversionHelper.convertStringArrayToStringSet(stringArray1);
        Set<String> stringSet2 = ConversionHelper.convertStringArrayToStringSet(stringArray2);

        Set<String> intersection = SetOperations.intersection(stringSet1,stringSet2);
        Set<String> union = SetOperations.union(stringSet1,stringSet2);

        return intersection.size() / (double) union.size();
    }
}
