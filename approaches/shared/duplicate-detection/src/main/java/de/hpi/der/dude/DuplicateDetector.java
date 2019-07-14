package de.hpi.der.dude;

import java.util.List;
import java.util.Set;

public interface DuplicateDetector {

    /**
     * Finds all duplicates within a given list of records
     * @param records the records of a block, each record is represented as a array of Strings
     * @return A set of sets representing the identified duplicates grouped in classes
     */
    Set<Set<Integer>> findDuplicates(List<String[]> records);

}
