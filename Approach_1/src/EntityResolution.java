import Similarity.Comparator.*;
import Similarity.Comparator.Numbers.NumberComparator;
import Similarity.Comparator.Numbers.SimpleNumberComparator;
import Similarity.Comparator.Strings.JaccardCoefficientStringComparator;
import Similarity.Comparator.Strings.StringComparator;
import Similarity.Tokenizer.CustomStringTokenizer;
import Similarity.Tokenizer.StringSpaceTokenizer;
import Util.CSVService;
import Util.Combinations;
import Util.SetOperations;
import javafx.util.Pair;

import java.util.*;

public class EntityResolution {

    private static final String restaurantDataPath = "../data/restaurant.csv";
    private static final String restaurantDataGold = "../data/restaurant_gold.csv";
    private static final double SimThreshold = 0.55;

    public static void main(String[] args) {
        findDuplicatesForRestaurantDataset();
    }

    private static void findDuplicatesForRestaurantDataset() {
        List<String[]> recordsRestaurant = CSVService.readDataset(restaurantDataPath, ",", false);

        // blocking
        Map<String,List<String[]>> groupedData = groupDataByPrefixOfName(recordsRestaurant);

        CustomStringTokenizer tokenizer = new StringSpaceTokenizer();
        StringComparator sComparator = new JaccardCoefficientStringComparator(tokenizer);
        NumberComparator nComparator = new SimpleNumberComparator();
        UniversalComparator comparator = new UniversalComparator(sComparator, nComparator);

        Set<Pair<Integer,Integer>> duplicates = findDuplicates(groupedData, comparator);

        Set<Pair<Integer, Integer>> goldStandard = CSVService.readRestaurantGoldStandard(restaurantDataGold, ",");

        // Evaluation
        Set<Pair<Integer, Integer>> truePositives = SetOperations.intersection(duplicates,goldStandard);
        Set<Pair<Integer, Integer>> false_positives = SetOperations.setDiff(duplicates,goldStandard);
        Set<Pair<Integer, Integer>> false_negatives = SetOperations.setDiff(goldStandard,duplicates);

        Set<Pair<Integer, Integer>> truePositivesUfalsePositives = SetOperations.union(truePositives,false_positives);

        Set<Pair<Integer, Integer>> truePositivesUfalseNegatives = SetOperations.union(truePositives,false_negatives);

        double precision = truePositives.size() * 1.0 / truePositivesUfalsePositives.size();
        double recall = truePositives.size() * 1.0 / truePositivesUfalseNegatives.size();
        double f1 = 2 * precision * recall / (precision + recall);

        System.out.println("Precision: " + precision);
        System.out.println("Recall: " + recall);
        System.out.println("F1: " + f1);
    }

    private static Map<String,List<String[]>> groupDataByPrefixOfName(List<String[]> records) {
        Map<String,List<String[]>> groupedData = new HashMap<String,List<String[]>>();

        for (String[] record: records) {
            String prefix = record[1].substring(0,Math.min(5, record[1].length() - 1));
            if(groupedData.containsKey(prefix)) {
                groupedData.get(prefix).add(record);
            } else {
                List<String[]> list = new LinkedList<String[]>();
                list.add(record);
                groupedData.put(prefix,list);
            }
        }
        return groupedData;
    }

    private static Set<Pair<Integer, Integer>> findDuplicates(Map<String, List<String[]>> groupedData, UniversalComparator comparator) {
        Set<Pair<Integer,Integer>> duplicates = new HashSet<Pair<Integer, Integer>>();

        for (String key: groupedData.keySet()) {
            duplicates.addAll(findDuplicatesForRecordList(groupedData.get(key),comparator));
        }

        return duplicates;
    }

    private static Set<Pair<Integer, Integer>> findDuplicatesForRecordList(List<String[]> records, UniversalComparator comparator) {
        Set<Pair<Integer,Integer>> duplicates = new HashSet<Pair<Integer, Integer>>();

        List<int[]> indexCombinations = Combinations.generateCombinations(records.size(), 2);

        for (int[] indexCombination: indexCombinations) {
            int index1 = indexCombination[0];
            int index2 = indexCombination[1];

            String[] rec1 = records.get(index1);
            String[] rec2 = records.get(index2);

            double similarity = comparator.compare(rec1,rec2);

            if (similarity > SimThreshold) {
                int rec1Id = Integer.parseInt(rec1[0]);
                int rec2Id = Integer.parseInt(rec2[0]);
                if(rec1Id < rec2Id) {
                    duplicates.add(new Pair<Integer, Integer>(rec1Id,rec2Id));
                } else {
                    duplicates.add(new Pair<Integer, Integer>(rec2Id,rec1Id));
                }
            }
        }

        return duplicates;
    }
}

