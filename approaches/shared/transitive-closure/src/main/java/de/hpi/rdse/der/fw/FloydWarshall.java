package de.hpi.rdse.der.fw;

public class FloydWarshall {

    public static int[][] apply(int[][] inputMatrix) {

        for (int k = 0; k < inputMatrix.length; k++) {
            for (int i = 0; i < inputMatrix.length; i++) {
                for (int j = 0; j < inputMatrix.length; j++) {
                    if ((i != k) && (j != k)) {
                        int oldPath = inputMatrix[i][j];
                        int one = inputMatrix[i][k];
                        int two = inputMatrix[k][j];
                        int newPath = one + two;

                        // check int overflow
                        if (one == Integer.MAX_VALUE || two == Integer.MAX_VALUE) {
                            newPath = Integer.MAX_VALUE;
                        }

                        inputMatrix[i][j] = Math.min(oldPath, newPath);
                    }
                }
            }
        }

        return inputMatrix;
    }

}
