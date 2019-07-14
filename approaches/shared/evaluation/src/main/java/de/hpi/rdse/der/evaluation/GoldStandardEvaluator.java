package de.hpi.rdse.der.evaluation;

import java.util.Set;

public interface GoldStandardEvaluator {

    /**
     * Evaluates the found duplicates against a given gold standard
     * @param duplicates the duplicates that were found
     * @param goldStandard the gold standard
     */
    void evaluate(Set<Set<Integer>> duplicates, Set<Set<Integer>> goldStandard);

}
