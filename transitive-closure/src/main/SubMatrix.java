package main;

import java.awt.*;
import java.util.Arrays;

public class SubMatrix {

    public void setMatrix(int[][] matrix) {
        this.matrix = matrix;
    }

    int [][] matrix;

    public int[][] getMatrix() {
        return matrix;
    }

    public Point getTopLeft() {
        return topLeft;
    }

    Point topLeft;

    public SubMatrix(int [][] matrix, Point topLeft, int size) {
        this.topLeft = topLeft;
        this.matrix = new int[size][size];

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int posX = x + topLeft.x;
                int posY = y + topLeft.y;

                // note: quadratic matrix
                if (posX >= matrix.length || posY >= matrix.length) {
                    this.matrix [x][y] = Integer.MAX_VALUE;
                } else {
                    this.matrix [x][y] = matrix[posX][posY];
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SubMatrix) {
            SubMatrix other = (SubMatrix) o;
            return Arrays.deepEquals(other.matrix, this.matrix);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.topLeft.hashCode();
    }
}
