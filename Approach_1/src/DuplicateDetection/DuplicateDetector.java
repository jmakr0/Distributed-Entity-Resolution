package DuplicateDetection;

import java.util.List;
import java.util.Set;

public interface DuplicateDetector {

    /**
     * Finds all duplicates within a given block
     * @param records the records of a block
     * @return a set of sets representing the identified duplicates grouped in classes
     */
    Set<Set<Integer>> findDuplicatesForBlock(List<String[]> records);

}
