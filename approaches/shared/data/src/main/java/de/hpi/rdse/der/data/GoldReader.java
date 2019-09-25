package de.hpi.rdse.der.data;

import au.com.bytecode.opencsv.CSVReader;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GoldReader {

    /**
     * Reads the restaurant gold standard and parses it into a set of two-dimensional sets of integers
     * @param dataFile The path to the gold standard as a String
     * @return a set of two-dimensional sets of integers representing the duplicates of the gold standard
     */
    public static Set<Set<Integer>> readRestaurantGoldStandard(String dataFile) {
        char separator = '\n';
        String splitSymbol = ",";

        Set<Set<Integer>> goldStandard = new HashSet<Set<Integer>>();

        CSVReader reader = null;
        try {
            reader = CSVReaderFactory.createCSVReader(dataFile, separator);

            // first line is header
            String[] tmpRecord = reader.readNext();
            while ((tmpRecord = reader.readNext()) != null) {
                String[] split = tmpRecord[0].split(splitSymbol);
                Integer id1 = Integer.parseInt(removeWhitespaces(split[1]));
                Integer id2 = Integer.parseInt(removeWhitespaces(split[2]));
                HashSet<Integer> set = new HashSet<Integer>();
                set.add(id1);
                set.add(id2);
                goldStandard.add(set);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();

        }

        return  goldStandard;
    }

    private static String removeWhitespaces(String s) {
        return s.replaceAll("\\s+","");
    }

}
