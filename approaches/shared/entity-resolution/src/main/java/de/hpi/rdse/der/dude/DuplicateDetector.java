package de.hpi.rdse.der.dude;

import java.util.List;
import java.util.Set;

public interface DuplicateDetector {

    /**
     * Finds all duplicates within a given list of records
     * @param records the records of a block, each record is represented as an array of Strings
     * @return A set of sets of integers representing the identified duplicates
     */
    Set<Set<Integer>> findDuplicates(List<String[]> records);

}
