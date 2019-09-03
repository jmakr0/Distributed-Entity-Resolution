package de.hpi.rdse.der.monolith.util;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CSVService {

    public static void writeCSV(String dataFile, List<String[]> records) {
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(
                    new OutputStreamWriter(new FileOutputStream(dataFile, true), StandardCharsets.UTF_8), ',', '\"', '\\');
            for (String[] record : records) {
                writer.writeNext(record);
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String[]> readDataset(String dataFile, String splitSymbol, boolean addIdField) {
        List<String[]> result = new ArrayList<String[]>();
        CSVReader reader = null;

        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8);
            reader = new CSVReader(inputStreamReader, '\n');

            // create header
            String[] originalHeader = reader.readNext()[0].split(splitSymbol);
            String[] header = null;
            if(addIdField) {
                // header = originalHeader + id column
                 header = new String[originalHeader.length + 1];
                header[0] = "id";
                System.arraycopy(originalHeader, 0, header, 1, originalHeader.length);
            } else {
                header = originalHeader;
            }

            String[] tmpRecord = null;
            int idCounter = 0;
            int destCopyPosition = addIdField ? 1 : 0;
            while ((tmpRecord = reader.readNext()) != null) {
                String line = tmpRecord[0].replaceAll("\"", "").replaceAll("\'", "");
                String[] split = line.split(splitSymbol);
                String[] record = new String[header.length];
                Arrays.fill(record,"");

                if(addIdField) {
                    // add id column
                    record[0] = "" + idCounter;
                    idCounter++;
                }

                // copy all other values
                System.arraycopy(split,0,record,destCopyPosition,split.length);
                result.add(record);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Set<Set<Integer>> readRestaurantGoldStandard(String dataFile, String splitSymbol) {
        Set<Set<Integer>> goldStandard = new HashSet<Set<Integer>>();

        CSVReader reader = null;
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8);
            reader = new CSVReader(inputStreamReader, '\n');

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
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return  goldStandard;
    }

    private static String removeWhitespaces(String s) {
        return s.replaceAll("\\s+","");
    }
}
