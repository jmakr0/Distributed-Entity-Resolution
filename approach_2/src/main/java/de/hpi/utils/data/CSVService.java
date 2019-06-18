package de.hpi.utils.data;

import au.com.bytecode.opencsv.CSVReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CSVService {

    private int startLine = 1;
    private String dataFile;

    public CSVService(String dataFile) {
        this.dataFile = dataFile;
    }

    public static class ReadLineResult {

        private String data;
        private int lastIndexRead;
        private boolean foundEndOfFile;

        public ReadLineResult(String data, int lastIndexRead, boolean foundEndOfFile) {
            this.data = data;
            this.lastIndexRead = lastIndexRead;
            this.foundEndOfFile = foundEndOfFile;
        }

        public String getData() {
            return data;
        }

        public int getLastIndexRead() {
            return lastIndexRead;
        }

        public boolean isFoundEndOfFile() {
            return foundEndOfFile;
        }
    }

    public ReadLineResult readNextDataBlock(int maxSize) {
        ReadLineResult result = readLines(dataFile, startLine, startLine + maxSize - 1);
        startLine += maxSize;
        return result;
    }

    public static ReadLineResult readLines(String dataFile, int startLine, int endLine) {
        try {
            StringBuilder sb = new StringBuilder();
            CSVReader reader = getCsvReader(dataFile);
            boolean foundEndOfFile = false;

            // skip first lines
            for (int i = 0; i < startLine ; i++) {
                reader.readNext();
            }

            int lineNumber;
            for (lineNumber = startLine; lineNumber <= endLine && !foundEndOfFile ; lineNumber++) {
                String[] tmpRecord = reader.readNext();
                if (tmpRecord != null) {
                    sb.append(tmpRecord[0].replaceAll("\"", "").replaceAll("\'", ""));
                    sb.append("\n");
                } else {
                    foundEndOfFile = true;
                }
            }
            return new ReadLineResult(sb.toString(), lineNumber, foundEndOfFile);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static List<String[]> readDataset(String dataFile, String splitSymbol, boolean addIdField) {
        List<String[]> result = new ArrayList<String[]>();
        CSVReader reader = null;

        try {
            reader = getCsvReader(dataFile);

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

    private static CSVReader getCsvReader(String dataFile) throws FileNotFoundException {
        CSVReader reader;
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8);
        reader = new CSVReader(inputStreamReader, '\n');
        return reader;
    }

    public static Set<Set<Integer>> readRestaurantGoldStandard(String dataFile, String splitSymbol) {
        Set<Set<Integer>> goldStandard = new HashSet<Set<Integer>>();

        CSVReader reader = null;
        try {
            reader = getCsvReader(dataFile);

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
