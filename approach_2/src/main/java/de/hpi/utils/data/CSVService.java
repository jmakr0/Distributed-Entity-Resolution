package de.hpi.utils.data;

import akka.stream.impl.fusing.Collect;
import au.com.bytecode.opencsv.CSVReader;
import de.hpi.utils.helper.SetOperations;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CSVService {

    private int startLine = 1;
    private String dataFile;
    private boolean allDataRead = false;
    private Map<Integer, Queue<String>> data;
    private Set<Integer> queueSizes;
    private final int QUEUE_SIZE = 5;

    public CSVService(String dataFile) {
        this.dataFile = dataFile;
        this.data = new HashMap<>();
        this.queueSizes = new HashSet<>();
        queueSizes.add(2);
        fillQueues();
    }

    public boolean dataAvailable() {
        if (allQueuesEmpty()) {
            fillQueues();
            return !allQueuesEmpty();
        } else {
            return true;
        }
    }

    private boolean allQueuesEmpty() {
        for (Queue<String> dataQueue: this.data.values()) {
            if (dataQueue.size() > 0) {
                return false;
            }
        }
        return true;
    }

    public String readNextDataBlock(int size) {
        this.queueSizes.add(size);

        if (!sizeAvailable(size)) {
            fillQueues();
            if(!sizeAvailable(size)) {
                return getNextFittingDataBlock(size);
            }
        }
        return this.data.get(size).poll();
    }

    private boolean sizeAvailable(int maxSize) {
        return this.data.get(maxSize) != null && this.data.get(maxSize).peek() != null;
    }

    private String getNextFittingDataBlock(int maxSize) {
        List<Integer> allKeys = new LinkedList<>();
        allKeys.addAll(this.data.keySet());
        Collections.sort(allKeys, Collections.reverseOrder());

        // first search for the next smaller block
        for (int size: allKeys) {
            if(size < maxSize && this.data.get(size).peek() != null) {
                return this.data.get(size).poll();
            }
        }
        // if all smaller queues are empty, search for bigger blocks
        Collections.sort(allKeys);
        for (int size: allKeys) {
            if(size > maxSize && this.data.get(size).peek() != null) {
                return this.data.get(size).poll();
            }
        }

        return null;

    }


    public void fillQueues() {
        // create new queues
        Set<Integer> currentQueueSizes = this.data.keySet();
        Set<Integer> missingSizes = SetOperations.setDiff(this.queueSizes, currentQueueSizes);
        for (int queueSize: missingSizes) {
            this.data.put(queueSize, new LinkedList<>());
        }

        CSVReader reader = null;
        try {
            reader = getCsvReader(dataFile);

            // skip first lines
            for (int i = 0; i < startLine ; i++) {
                reader.readNext();
            }

            // fill up queues
            for (Integer numberOfLines: this.data.keySet()) {
                Queue<String> queue = this.data.get(numberOfLines);

                while(!allDataRead && queue.size() < this.QUEUE_SIZE) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = startLine; i < startLine + numberOfLines; i++) {
                        String[] tmpRecord = reader.readNext();
                        if (tmpRecord != null) {
                            System.out.println(tmpRecord[0]);
                            sb.append(tmpRecord[0].replaceAll("\"", "").replaceAll("\'", ""));
                            sb.append("\n");
                        } else {
                            allDataRead = true;
                            break;
                        }
                    }
                    queue.add(sb.toString());
                    startLine += numberOfLines;
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
