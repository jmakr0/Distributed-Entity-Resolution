package de.hpi.utils.data;

import au.com.bytecode.opencsv.CSVReader;
import de.hpi.utils.helper.SetOperations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class CSVService {

    private boolean allDataRead = false;
    private Map<Integer, Queue<String>> data;
    private Set<Integer> queueSizes;
    private final int QUEUE_SIZE = 5;
    private CSVReader csvReader;

    public CSVService(String dataFile, boolean hasHeader, char separator, int minBlockSize) {
        this.csvReader = CSVReaderFactory.createCSVReader(dataFile, separator);
        this.data = new HashMap<>();
        this.queueSizes = new HashSet<>();
        this.queueSizes.add(minBlockSize);

        if (hasHeader) {
            skipHeader();
        }

        this.fillQueues();
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

    public String getRecords(int lines) {
        this.queueSizes.add(lines);

        if (!sizeAvailable(lines)) {
            fillQueues();
            if(!sizeAvailable(lines)) {
                return getNextFittingDataBlock(lines);
            }
        }
        return this.data.get(lines).poll();
    }

    private boolean sizeAvailable(int maxSize) {
        return this.data.get(maxSize) != null && this.data.get(maxSize).peek() != null;
    }

    private String getNextFittingDataBlock(int maxSize) {
        List<Integer> sizes = new LinkedList<>();
        sizes.addAll(this.data.keySet());
        Collections.sort(sizes);

        for (int i = sizes.size() - 1; i >= 0; i--) {
            int size = sizes.get(i);
            if(size < maxSize && this.data.get(size).peek() != null) {
                return this.data.get(size).poll();
            }
        }

        for (int i = 0; i < sizes.size(); i++) {
            int size = sizes.get(i);
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

        try {
            for (Integer numberOfLines: this.data.keySet()) {
                Queue<String> queue = this.data.get(numberOfLines);

                while(!allDataRead && queue.size() < this.QUEUE_SIZE) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < numberOfLines; i++) {
                        String[] tmpRecord = this.csvReader.readNext();
                        if (tmpRecord != null) {
                            sb.append(tmpRecord[0].replaceAll("\"", "").replaceAll("\'", ""));
                            sb.append("\n");
                        } else {
                            allDataRead = true;
                            break;
                        }
                    }
                    queue.add(sb.toString());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void skipHeader() {
        try {
            this.csvReader.readNext();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
