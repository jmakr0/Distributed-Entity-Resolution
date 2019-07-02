package Evaluation;

import Util.SetOperations;

import java.util.Set;

public class ConsoleOutputEvaluator implements GoldStandardEvaluator {

    public void evaluateAgainstGoldStandard(Set<Set<Integer>> duplicates, Set<Set<Integer>> goldStandard) {
        Set<Set<Integer>> truePositives = SetOperations.intersection(duplicates,goldStandard);
        Set<Set<Integer>> false_positives = SetOperations.setDiff(duplicates,goldStandard);
        Set<Set<Integer>> false_negatives = SetOperations.setDiff(goldStandard,duplicates);

        Set<Set<Integer>> truePositivesUfalsePositives = SetOperations.union(truePositives,false_positives);

        Set<Set<Integer>> truePositivesUfalseNegatives = SetOperations.union(truePositives,false_negatives);

        double precision = truePositives.size() * 1.0 / truePositivesUfalsePositives.size();
        double recall = truePositives.size() * 1.0 / truePositivesUfalseNegatives.size();
        double f1 = 2 * precision * recall / (precision + recall);

        System.out.println("Precision: " + precision);
        System.out.println("Recall: " + recall);
        System.out.println("F1: " + f1);
    }
}
