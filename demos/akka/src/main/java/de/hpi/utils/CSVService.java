package de.hpi.utils;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

    public static List<String[]> readCSV(String dataFile) {
        List<String[]> result = new ArrayList<>();
        CSVReader reader = null;

        try {
            reader = new CSVReader(
                    new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8), ',', '\"');
            String[] record = null;
            while ((record = reader.readNext()) != null) {
                result.add(record[0].split(";"));
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
