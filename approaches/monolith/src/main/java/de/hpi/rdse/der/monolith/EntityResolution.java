package de.hpi.rdse.der.monolith;

import de.hpi.rdse.der.data.GoldReader;
import de.hpi.rdse.der.dude.DuplicateDetector;
import de.hpi.rdse.der.dude.SimpleDuplicateDetector;
import de.hpi.rdse.der.evaluation.ConsoleOutputEvaluator;
import de.hpi.rdse.der.evaluation.GoldStandardEvaluator;
import de.hpi.rdse.der.monolith.util.CSVService;
import de.hpi.rdse.der.similarity.UniversalComparator;
import de.hpi.rdse.der.similarity.numeric.AbsComparator;
import de.hpi.rdse.der.similarity.numeric.NumberComparator;
import de.hpi.rdse.der.similarity.string.JaroWinklerComparator;
import de.hpi.rdse.der.similarity.string.StringComparator;

import java.util.*;

public class EntityResolution {
    private static final String RESTAURANT_DATA_PATH = "../../data/restaurant.csv";
    private static final String RESTAURANT_DATA_GOLD = "../../data/restaurant_gold.csv";
    private static final int PREFIX_LENGTH = 5;
    private static final double SIMILARITY_THRESHOLD = 0.9;
    private static final double INTERVAL_START = 5;
    private static final double INTERVAL_END = 30;

    public static void main(String[] args) {

        findDuplicatesForRestaurantDataset();

    }

    private static void findDuplicatesForRestaurantDataset() {
        // read data
        List<String[]> recordsRestaurant = CSVService.readDataset(RESTAURANT_DATA_PATH, ",", false);

        // blocking
        Map<String,List<String[]>> groupedData = groupDataByPrefixOfName(recordsRestaurant);

        // matching
        Set<Set<Integer>> duplicates = findDuplicates(groupedData);

        // TODO transitive closure

        Set<Set<Integer>> goldStandard = GoldReader.readRestaurantGoldStandard(RESTAURANT_DATA_GOLD);
        GoldStandardEvaluator evaluator = new ConsoleOutputEvaluator();
        evaluator.evaluate(duplicates, goldStandard);
    }

    private static Map<String,List<String[]>> groupDataByPrefixOfName(List<String[]> records) {
        Map<String,List<String[]>> groupedData = new HashMap<String,List<String[]>>();

        for (String[] record: records) {
            String prefix = record[1].substring(0,Math.min(PREFIX_LENGTH, record[1].length() - 1));
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

    private static Set<Set<Integer>> findDuplicates(Map<String, List<String[]>> groupedData) {
        StringComparator sComparator = new JaroWinklerComparator();
        NumberComparator nComparator = new AbsComparator(INTERVAL_START, INTERVAL_END);
        UniversalComparator comparator = new UniversalComparator(sComparator, nComparator);

        DuplicateDetector duDetector = new SimpleDuplicateDetector(comparator, SIMILARITY_THRESHOLD);

        Set<Set<Integer>> duplicates = new HashSet<>();

        for (String key : groupedData.keySet()) {
            duplicates.addAll(duDetector.findDuplicates(groupedData.get(key)));
        }

        return duplicates;
    }
}
