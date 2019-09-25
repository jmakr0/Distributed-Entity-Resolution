package de.hpi.rdse.der.dude;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import de.hpi.rdse.der.combinations.Combinations;
import de.hpi.rdse.der.similarity.UniversalComparator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleDuplicateDetector implements DuplicateDetector{

    private UniversalComparator comparator;
    private double similarityThreshold;

    /**
     * Inits a SimpleDuplicateDetector
     * @param comparator the UniversalComparator object that is used to compare two records
     * @param similarityThreshold A value between 0 and 1 that is used to decide whether a two records are classified as duplicates or not
     */
    public SimpleDuplicateDetector(UniversalComparator comparator, double similarityThreshold) {
        this.comparator = comparator;
        this.similarityThreshold = similarityThreshold;
    }

    /**
     * Inits a SimpleDuplicateDetector
     * @param comparator the UniversalComparator object that is used to compare two records
     * @param config An Configuration object containing the key: duplicate-detection.similarity-threshold
     */
    public SimpleDuplicateDetector(UniversalComparator comparator, Config config) {
        this.comparator = comparator;
        this.similarityThreshold = config.getDouble("duplicate-detection.similarity-threshold");
    }

    /**
     * Inits a SimpleDuplicateDetector using the default configuration
     * @param comparator the UniversalComparator object that is used to compare two records
     */
    public SimpleDuplicateDetector(UniversalComparator comparator) {
        this(comparator, ConfigFactory.load("default"));
    }

    public Set<Set<Integer>> findDuplicates(List<String[]> records) {
        Set<Set<Integer>> duplicates = new HashSet<Set<Integer>>();

        List<int[]> indexCombinations = Combinations.generateCombinations(records.size(), 2);

        for (int[] indexCombination: indexCombinations) {
            int index1 = indexCombination[0];
            int index2 = indexCombination[1];

            String[] rec1 = records.get(index1);
            String[] rec2 = records.get(index2);

            double similarity = this.comparator.compare(rec1,rec2);

            if (similarity > this.similarityThreshold) {
                int rec1Id = Integer.parseInt(rec1[0]);
                int rec2Id = Integer.parseInt(rec2[0]);
                    HashSet<Integer> duplicateSet = new HashSet<Integer>();
                    duplicateSet.add((rec1Id));
                    duplicateSet.add((rec2Id));
                    duplicates.add(duplicateSet);
            }
        }

        return duplicates;
    }
}
