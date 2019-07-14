package de.hpi.der.combinations;

import java.util.ArrayList;
import java.util.List;

// Code from: https://www.baeldung.com/java-combinations-algorithm
public class Combinations {

    /**
     * Generates all r combinations of n values
     * @return all combinations as List of int array
     */

    public static List<int[]> generateCombinations(int n, int r) {
        List<int[]> combinations = new ArrayList<int[]>();
        helper(combinations, new int[r], 0, n-1, 0);
        return combinations;
    }

    private static void helper(List<int[]> combinations, int[] data, int start, int end, int index) {
        if (index == data.length) {
            int[] combination = data.clone();
            combinations.add(combination);
        } else if (start <= end) {
            data[index] = start;
            helper(combinations, data, start + 1, end, index + 1);
            helper(combinations, data, start + 1, end, index);
        }
    }
}
