import Util.CSVService;

import java.util.List;

public class EntityResolution {

    public static void main(String[] args) {
        List<String[]> recordsRestaurant = CSVService.readDataset("../data/restaurant.csv", ",", false);
        List<String[]> recordsCora = CSVService.readDataset("../data/cora.tsv", "\t", true);


    }

}