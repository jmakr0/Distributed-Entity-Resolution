package Util;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                String[] split = tmpRecord[0].split(splitSymbol);
                String[] record = new String[header.length];
                Arrays.fill(record,"");

                if(addIdField) {
                    // add id column
                    record[0] = "" + idCounter;
                    idCounter++;
                }

                // copy all other values
                System.arraycopy(split,0,record,destCopyPosition,split.length-1);
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
}
