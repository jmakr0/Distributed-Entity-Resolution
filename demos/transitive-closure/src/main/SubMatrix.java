package main;

import java.util.Arrays;

public class SubMatrix {

    private Position position;
    private int subMatrixSize;
    private int [][] subMatrix;

    public SubMatrix(int [][] matrix, Position position, int size) {
        this.position = position;
        this.subMatrixSize = size;
        this.subMatrix = new int[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int subX = i + position.getX();
                int subY = j + position.getY();

                // note: quadratic subMatrix
                if (subX >= matrix.length || subY >= matrix.length) {
                    this.subMatrix[i][j] = Integer.MAX_VALUE;
                } else {
                    this.subMatrix[i][j] = matrix[subX][subY];
                }
            }
        }
    }

    public int[][] getSubMatrix() {
        return subMatrix;
    }

    public Position getPosition() {
        return this.position;
    }

    public int getX() {
        return this.position.getX();
    }

    public int getY() {
        return this.position.getY();
    }

    public int getSubMatrixSize() {
        return this.subMatrixSize;
    }

    public boolean contains(int x, int y) {
        boolean xRange = (this.getX() + this.subMatrixSize) > x && (x >= this.getX());
        boolean yRange = (this.getY() + this.subMatrixSize) > y && (y >= this.getY());

        return xRange && yRange;
    }

    public void setValue(int x, int y, int update) {
        int internX = this.getInternX(x);
        int internY = this.getInternY(y);

        subMatrix[internX][internY] = update;
    }

    public int getValue(int x, int y) {
        int internX = getInternX(x);
        int internY = getInternY(y);

        return subMatrix[internX][internY];
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SubMatrix) {
            SubMatrix other = (SubMatrix) o;
            return this.getX() == other.getX() && this.getY() == other.getY() && Arrays.deepEquals(other.subMatrix, this.subMatrix);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.position.hashCode();
    }

    private int getInternX(int x) {
        return x - this.getX();
    }

    private int getInternY(int y) {
        return y - this.getY();
    }

}
