package de.hpi.rdse.der.util;

import java.util.*;

public class MatrixConverter {

    public static int[][] duplicateSetToMatrix(Set<Set<Integer>> duplicates) {

        // determine matrix size
        int maxID = getMaxNumber(duplicates);

        // we need length maxID + 1 (e.g. maxID = 3 -> need index 0,1,2, and 3)
        int [][] matrix = new int[maxID+1][maxID+1];

        // init matrix with infinity (using int max)
        // because the matrix is quadratic we can simply use matrix.length for both loops
        fillMatrix(matrix.length, matrix);

        // fill matrix
        for (Set<Integer> duplicatePair: duplicates) {
            // we know that every duplicate Set contains exactly two ints
            assert(duplicatePair.size() == 2);

            // extract record IDs from set
            int[] duplicateRecords = intSetToArray(duplicatePair);
            int elem1 = duplicateRecords[0];
            int elem2 = duplicateRecords[1];

            // because duplicate relation is symmetric
            matrix[elem1][elem2] = 1;
            matrix[elem2][elem1] = 1;

        }

        return matrix;

    }

    public static CompressedMatrix duplicateSetToCompressedMatrix(Set<Set<Integer>> duplicates) {
        Set<Integer> ids = new HashSet();
        // actual id -> mappedID
        Map<Integer,Integer> compressionLookup = new HashMap<>();
        // mappedID -> actual id
        Map<Integer,Integer> reverseLookup = new HashMap<>();


        // adding all original ids to set
        for (Set<Integer>  duplicateTuple: duplicates) {
            ids.addAll(duplicateTuple);
        }

        // create lookup maps
        int mappedID = 0;
        for (Integer actualID: ids) {
            compressionLookup.put(actualID, mappedID);
            reverseLookup.put(mappedID, actualID);
            mappedID++;
        }

        int maxID = ids.size();

        // we need length maxID + 1 (e.g. maxID = 3 -> need index 0,1,2, and 3)
        int [][] matrix = new int[maxID][maxID];

        // init matrix with infinity (using int max)
        // because the matrix is quadratic we can simply use matrix.length for both loops
        fillMatrix(matrix);

        // fill matrix
        for (Set<Integer> duplicatePair: duplicates) {
            // we know that every duplicate Set contains exactly two ints
            assert(duplicatePair.size() == 2);

            // extract record IDs from set
            int[] duplicateRecords = intSetToArray(duplicatePair);

            int actualID1 = duplicateRecords[0];
            int actualID2 = duplicateRecords[1];

            int mappedID1 = compressionLookup.get(actualID1);
            int mappedID2 = compressionLookup.get(actualID2);

            matrix[mappedID1][mappedID2] = 1;
            matrix[mappedID2][mappedID1] = 1;

        }

        return new CompressedMatrix(matrix, reverseLookup);
    }

    public Set<Set<Integer>> translateWithCompressionLookup(Set<Set<Integer>> duplicates, Map<Integer, Integer> lookup) {
        Set<Set<Integer>> result = new HashSet<>();

        for (Set<Integer> duplicatePair: duplicates) {
            for (Integer compressionID: duplicatePair) {
                Set<Integer> translated = new HashSet<>();
                translated.add(lookup.get(compressionID));
                result.add(translated);
            }
        }

        return result;
    }


//
//    public static MappedMatrix duplicateSetToMappedMatrix(Set<Set<Integer>> duplicates) {
//
//        int dimension = duplicates.size();
//
//        int [][] matrix = new int[dimension][dimension];
//        fillMatrix(dimension, matrix);
//
//        MappedMatrix mappedMatrix = new MappedMatrix(matrix);
//
//        // fill matrix
//        for (Set<Integer> duplicatePair: duplicates) {
//            // we know that every duplicate Set contains exactly two ints
//            assert(duplicatePair.size() == 2);
//
//            // extract record IDs from set
//            int[] duplicateRecords = intSetToArray(duplicatePair);
//            int elem1 = duplicateRecords[0];
//            int elem2 = duplicateRecords[1];
//
//            // TODO put this logic in Mapped Matrix
//            int mappedID1 = mappedMatrix.getMappedIndexForId(elem1);
//            int mappedID2 = mappedMatrix.getMappedIndexForId(elem2);
//
//            // because duplicate relation is symmetric
//            mappedMatrix.setValue(mappedID1, mappedID2, 1);
//            mappedMatrix.setValue(mappedID2, mappedID1, 1);
//        }
//
//        return mappedMatrix;
//
//    }

    private static void fillMatrix(int[][] matrix) {
        // init matrix with infinity (using int max)
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                if (i == j) {
                    // from position node to itself the distance is 0
                    matrix[i][j] = 0;
                } else {
                    matrix[i][j] = Integer.MAX_VALUE;
                }
            }
        }
    }

    private static int getMaxNumber(Set<Set<Integer>> duplicates) {
        int max = Integer.MIN_VALUE;
        for (Set<Integer> duplicatePair: duplicates) {
            int localMax = Collections.max(duplicatePair);
            if (localMax > max) {
                max = localMax;
            }
        }

        return max;
    }

    private static int[] intSetToArray(Set<Integer> duplicatePair) {
        // we know that each duplicateSet has size 2
        int[] result = new int[2];
        int index = 0;
        for (int recordId: duplicatePair) {
            result[index] = recordId;
            index++;
        }

        return result;
    }

    public static Set<Set<Integer>> formTransitiveClosure(int[][] matrix) {

        boolean[][] boolMatrix = toBoolMatrix(matrix);

        return formTransitiveClosure(boolMatrix);

    }

    public static Set<Set<Integer>> formTransitiveClosure(boolean[][] matrix) {
        Set<Set<Integer>> transitiveClosure = new HashSet<>();

        int maxID = matrix.length - 1;

        for (int i = 0; i <= maxID; i++) {
            boolean[] row = matrix[i];
            for (int j = 0; j <= maxID; j++) {
                if (j != i && row[j]) {
                    HashSet<Integer> duplicatePair = new HashSet<Integer>();
                    duplicatePair.add(i);
                    duplicatePair.add(j);
                    transitiveClosure.add(duplicatePair);
                }
            }
        }

        return transitiveClosure;
    }

    private static boolean[][] toBoolMatrix(int[][] matrix) {
        boolean[][] result = new boolean[matrix.length][matrix.length];

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                result[i][j] = matrix[i][j] < Integer.MAX_VALUE;
            }
        }

        return result;
    }
}
