package de.hpi.rdse.der.evaluation;

import java.util.Set;

public interface GoldStandardEvaluator {

    /**
     * Evaluates the found duplicates against a given gold standard
     * @param duplicates the duplicates that were found as a set of two-dimensional sets
     * @param goldStandard the gold standard as a set of two-dimensional sets
     */
    void evaluate(Set<Set<Integer>> duplicates, Set<Set<Integer>> goldStandard);

}
