package de.hpi.ddd.transitiveClosure;

public class FloydWarshall {

    public static int[][] apply(int[][] inputMatrix) {

        for (int k = 0; k < inputMatrix.length; k++) {
            for (int i = 0; i < inputMatrix.length; i++) {
                for (int j = 0; j < inputMatrix.length; j++) {
                    if ((i != k) && (j != k)) {
                        int oldPath = inputMatrix[i][j];
                        int one = inputMatrix[i][k];
                        int two = inputMatrix[k][j];

                        inputMatrix[i][j] = update(oldPath, one, two);
                    }
                }
            }
        }

        return inputMatrix;
    }

    public static boolean[][] apply(boolean[][] inputMatrix) {

        for (int k = 0; k < inputMatrix.length; k++) {
            for (int i = 0; i < inputMatrix.length; i++) {
                for (int j = 0; j < inputMatrix.length; j++) {
                    if ((i != k) && (j != k)) {
                        boolean oldPath = inputMatrix[i][j];
                        boolean one = inputMatrix[i][k];
                        boolean two = inputMatrix[k][j];

                        inputMatrix[i][j] = update(oldPath, one, two);
                    }
                }
            }
        }

        return inputMatrix;
    }

    private static boolean update(boolean old, boolean one, boolean two) {
        return old || (one && two);
    }

    private static int update(int old, int one, int two) {
        int newPath = one + two;

        // check int overflow
        if (one == Integer.MAX_VALUE || two == Integer.MAX_VALUE) {
            newPath = Integer.MAX_VALUE;
        }

        return Math.min(old, newPath);
    }

}