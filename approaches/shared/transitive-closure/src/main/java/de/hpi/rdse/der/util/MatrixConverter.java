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

    public static MappedMatrix duplicateSetToMappedMatrix(Set<Set<Integer>> duplicates) {

        int dimension = duplicates.size();

        int [][] matrix = new int[dimension][dimension];
        fillMatrix(dimension, matrix);

        MappedMatrix mappedMatrix = new MappedMatrix(matrix);

        // fill matrix
        for (Set<Integer> duplicatePair: duplicates) {
            // we know that every duplicate Set contains exactly two ints
            assert(duplicatePair.size() == 2);

            // extract record IDs from set
            int[] duplicateRecords = intSetToArray(duplicatePair);
            int elem1 = duplicateRecords[0];
            int elem2 = duplicateRecords[1];

            // TODO put this logic in Mapped Matrix
            int mappedID1 = mappedMatrix.getMappedIndexForId(elem1);
            int mappedID2 = mappedMatrix.getMappedIndexForId(elem2);

            // because duplicate relation is symmetric
            mappedMatrix.setValue(mappedID1, mappedID2, 1);
            mappedMatrix.setValue(mappedID2, mappedID1, 1);
        }

        return mappedMatrix;

    }

    private static void fillMatrix(int dimension, int[][] matrix) {
        // init matrix with infinity (using int max)
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
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
