import DuplicateDetection.DuplicateDetector;
import DuplicateDetection.SimpleDuplicateDetector;
import Evaluation.ConsoleOutputEvaluator;
import Evaluation.GoldStandardEvaluator;
import Similarity.Comparator.Numbers.NumberComparator;
import Similarity.Comparator.Numbers.SimpleNumberComparator;
import Similarity.Comparator.Strings.JaccardCoefficientStringComparator;
import Similarity.Comparator.Strings.StringComparator;
import Similarity.Comparator.UniversalComparator;
import Similarity.Tokenizer.CustomStringTokenizer;
import Similarity.Tokenizer.StringSpaceTokenizer;
import Util.CSVService;

import java.util.*;

public class EntityResolution {

    private static final String RESTAURAND_DATA_PATH = "../data/restaurant.csv";
    private static final String RESTAURANT_DATA_GOLD = "../data/restaurant_gold.csv";
    private static final double SIMILARITY_THRESHOLD = 0.55;

    public static void main(String[] args) {
        findDuplicatesForRestaurantDataset();
    }

    private static void findDuplicatesForRestaurantDataset() {
        // read data
        List<String[]> recordsRestaurant = CSVService.readDataset(RESTAURAND_DATA_PATH, ",", false);

        // blocking
        Map<String,List<String[]>> groupedData = groupDataByPrefixOfName(recordsRestaurant);

        // find duplicates
        Set<Set<Integer>> duplicates = findDuplicates(groupedData);

        // evaluate results
        Set<Set<Integer>> goldStandard = CSVService.readRestaurantGoldStandard(RESTAURANT_DATA_GOLD, ",");
        GoldStandardEvaluator evaluator = new ConsoleOutputEvaluator();
        evaluator.evaluateAgainstGoldStandard(duplicates, goldStandard);
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

    private static Set<Set<Integer>> findDuplicates(Map<String, List<String[]>> groupedData) {
        CustomStringTokenizer tokenizer = new StringSpaceTokenizer();
        StringComparator sComparator = new JaccardCoefficientStringComparator(tokenizer);
        NumberComparator nComparator = new SimpleNumberComparator();
        UniversalComparator comparator = new UniversalComparator(sComparator, nComparator);

        DuplicateDetector duDetector= new SimpleDuplicateDetector(comparator, SIMILARITY_THRESHOLD);

        return findDuplicates(groupedData, duDetector);
    }

    private static Set<Set<Integer>> findDuplicates(Map<String, List<String[]>> groupedData, DuplicateDetector duDetector) {
        Set<Set<Integer>> duplicates = new HashSet<Set<Integer>>();

        for (String key: groupedData.keySet()) {
            duplicates.addAll(duDetector.findDuplicatesForBlock(groupedData.get(key)));
        }

        return duplicates;
    }
}
