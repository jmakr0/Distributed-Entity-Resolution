import Util.CSVService;

import java.util.List;

public class EntityResolution {

    public static void main(String[] args) {
        String s = ";a;b;;;e;;;";
        String[] test = s.split("\t");
        List<String[]> records = CSVService.readCoraCSV("../data/cora.tsv", "\t");
    }

}