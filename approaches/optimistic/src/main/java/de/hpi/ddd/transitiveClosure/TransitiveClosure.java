package de.hpi.ddd.transitiveClosure;

import java.util.*;

public class TransitiveClosure {

    public static Set<Set<Integer>> calculate(Set<Set<Integer>> duplicates) {

//        boolean[][] matrix = MatrixConverter.duplicateSetToBooleanMatrix(duplicates);
        int[][] matrix = MatrixConverter.duplicateSetToMatrix(duplicates);

        FloydWarshall.apply(matrix);

        return formTransitiveClosure(matrix);
    }

    public static Set<Set<Integer>> formTransitiveClosure(int[][] matrix) {

        boolean[][] boolMatrix = toBoolMatrix(matrix);

        return formTransitiveClosure(boolMatrix);
    }

    public static Set<Set<Integer>> formTransitiveClosure(boolean[][] matrix) {
        Set<Set<Integer>> transitiveClosure = new HashSet<>();

        int maxID = matrix.length - 1;

        Queue<Integer> work = new LinkedList<>();

        // put all IDs in work queue
        for (int i = 0; i <= maxID; i++) {
            work.add(i);
        }

        while (!work.isEmpty()) {
            int currentID = work.poll();

            Set<Integer> duplicates = new HashSet<>();
            duplicates.add(currentID);

            for (int id = 0; id <= maxID; id++) {
                boolean hasConnection = matrix[currentID][id];

                if (hasConnection) {
                    duplicates.add(id);
                    work.remove(id);
                }
            }

            transitiveClosure.add(duplicates);
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