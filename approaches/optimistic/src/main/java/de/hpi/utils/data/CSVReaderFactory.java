package de.hpi.utils.data;

import au.com.bytecode.opencsv.CSVReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CSVReaderFactory {

    public static CSVReader createCSVReader(String dataFile, char separator) {
        CSVReader reader = null;
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8);
            reader = new CSVReader(inputStreamReader, separator);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return reader;
    }
}
