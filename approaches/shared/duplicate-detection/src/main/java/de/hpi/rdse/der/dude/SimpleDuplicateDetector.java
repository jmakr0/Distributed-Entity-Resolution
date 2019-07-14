package de.hpi.rdse.der.dude;

import de.hpi.rdse.der.combinations.Combinations;
import de.hpi.rdse.der.similarity.UniversalComparator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleDuplicateDetector extends AbstractDuplicateDetector {

    public SimpleDuplicateDetector(UniversalComparator comparator, double similarityThreshold) {
        super(comparator, similarityThreshold);
    }

    public Set<Set<Integer>> findDuplicates(List<String[]> records) {
        Set<Set<Integer>> duplicates = new HashSet<Set<Integer>>();

        List<int[]> indexCombinations = Combinations.generateCombinations(records.size(), 2);

        for (int[] indexCombination: indexCombinations) {
            int index1 = indexCombination[0];
            int index2 = indexCombination[1];

            String[] rec1 = records.get(index1);
            String[] rec2 = records.get(index2);

            double similarity = super.comparator.compare(rec1,rec2);

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
