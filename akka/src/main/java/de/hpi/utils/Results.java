package de.hpi.utils;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.max;

public class Results {

    private Map<Integer, String[]> results;
    private Map<Integer, String> uncrackedPasswords;
    private List<String> geneSequences;

    public Results(List<String[]> csvData) {
        results = new HashMap<>();
        uncrackedPasswords = new HashMap<>();
        geneSequences = new ArrayList<>();

        for (int i = 1; i < csvData.size(); i++) {
            String[] line = csvData.get(i);

            String username = line[1];
            String[] resultsArray = new String[6];
            resultsArray[1] = username;

            int userID = Integer.parseInt(line[0]);
            resultsArray[0] = line[0];

            results.put(userID,resultsArray);

            uncrackedPasswords.put(userID, line[2]);
            geneSequences.add(line[3]);

        }

    }

    public int[] getAllPasswordsSortedByID() {
        int[] passwords = new int[42];
        for (int i = 0; i < max(this.results.keySet()); i++) {
            String[] currentResultEntry = this.results.get(i+1);
            passwords[i] = Integer.parseInt(currentResultEntry[2]);
        }
        return passwords;
    }

    public void setPasswords(Map<String,Integer> rainbowTable) {
        for (int userID: uncrackedPasswords.keySet()) {
            String hash = uncrackedPasswords.get(userID);
            Integer password = rainbowTable.get(hash);

            String[] currentResult = results.get(userID);
            currentResult[2] = "" + password;

            results.put(userID,currentResult);
        }
        System.out.println("Passwords have been set");
    }

    public void setPrefixes(int[] linearCombnation) {
        for (int i = 1; i <= max(this.results.keySet()); i++) {
            int prefix = linearCombnation[i-1];

            String[] currentResult = results.get(i);
            currentResult[3] = "" + prefix;
        }
        System.out.println("Prefixes have been set");
    }

    public void setGenePartner(int searchedFor, int partner) {
        String[] currentResult = this.results.get(searchedFor);
        currentResult[4] = "" + partner;
    }

    public void setPrefixHash(int personID, String hash) {
        String[] currentResult = this.results.get(personID);
        currentResult[5] = hash;
    }

    public List<String> getAllGeneSequences() {
        return geneSequences;
    }

    public int getNumberOfResults() {
        return this.results.size();
    }

    public boolean everyoneHasAPartner() {
        for (String[] currentResult: this.results.values()) {
            if (currentResult[4] == null) {
                return false;
            }
        }
        return true;
    }

    public List<Integer> getAllPrefixes() {
        List<Integer> prefixes = new ArrayList<>();
        for (int i = 0; i < max(this.results.keySet()); i++) {
            String[] currentResultEntry = this.results.get(i+1);
            prefixes.add(i, Integer.parseInt(currentResultEntry[3]));
        }
        return prefixes;
    }

    public List<Integer> getAllPartners() {
        List<Integer> partners = new ArrayList<>();
        for (int i = 0; i < max(this.results.keySet()); i++) {
            String[] currentResultEntry = this.results.get(i+1);
            partners.add(i, Integer.parseInt(currentResultEntry[4]));
        }
        return partners;
    }

    public boolean allPrefixHashesCalculated() {
        for (String[] currentResult: this.results.values()) {
            if (currentResult[5] == null) {
                return false;
            }
        }
        return true;
    }

    public void writeResultsToCsv() {
        List<String[]> records = new LinkedList<>();
        for (int key = 1; key <= max(this.results.keySet()); key++) {
            records.add(results.get(key));
        }
        CSVService.writeCSV("results.csv", records);

    }
}
