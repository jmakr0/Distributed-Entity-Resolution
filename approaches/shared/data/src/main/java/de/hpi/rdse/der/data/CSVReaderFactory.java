package de.hpi.rdse.der.data;

import au.com.bytecode.opencsv.CSVReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CSVReaderFactory {

    /**
     * A Method that generates a CSVService and returns it
     * @param dataFile The path to the file that should be read by the CSVReader
     * @param separator The separator that is used for the CSVReader
     * @return A CSVReader that was initialized with the values that were given as parameters
     */
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
