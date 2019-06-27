package main;

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

//    public static int[][] apply(SubMatrix block, List<SubMatrix> blocks) {
//        int x = (int) block.getTopLeft().getX();
//        int y = (int) block.getTopLeft().getY();
//
//        for (int k = 0; k < block.length; k++) {
//            for (int i = x; i < x + block.length; i++) {
//                for (int j = y; j < y + block.length; j++) {
//                    if ((i != k) && (j != k)) {
//                        int currentValue = block[i][j];
//                        int one = block[i][k];
//                        int two = block[k][j];
//                        int newPath = one + two;
//                        // check int overflow !!
//                        if (one == Integer.MAX_VALUE || two == Integer.MAX_VALUE) {
//                            newPath = Integer.MAX_VALUE;
//                        }
//                        block[i][j] = Math.min(currentValue, newPath);
//                    }
//                }
//            }
//        }
//
//        return block;
//    }
//
//    public static List<int [][]> getPivots(int[][] matrix, int blockSize) {
//        List<int[][]> pivots = new LinkedList<>();
//
//        for (int x = 0, y = 0; x < matrix.length && y < matrix.length; x += blockSize, y += blockSize) {
//
//            SubMatrix pivot = new SubMatrix(matrix, new Point(x, y), blockSize);
//
//            pivots.add(pivot.getMatrix());
//        }
//
//        return pivots;
//    }


}
