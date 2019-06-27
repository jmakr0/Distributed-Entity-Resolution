package main;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SubMatrix {

    private int matrixSize;
    private int [][] subMatrix;
    private Set<DFWPosition> neighborPositions;
    private DFWPosition pos;

    private boolean validPosition(int x, int y) {
        return x >= 0 && x < this.matrixSize && y >= 0 && y < this.matrixSize;
    }

    public SubMatrix(int [][] matrix, int x, int y, int size) {
        this(matrix, new DFWPosition(x, y), size);
    }

    public SubMatrix(int [][] matrix, DFWPosition pos, int size) {
        this.pos = pos;
        this.matrixSize = matrix.length;
        this.subMatrix = new int[size][size];
        this.neighborPositions = this.calculateNeighborPositions();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int subX = i + pos.getX();
                int subY = j + pos.getY();

                // note: quadratic subMatrix
                if (subX >= matrix.length || subY >= matrix.length) {
                    this.subMatrix[i][j] = Integer.MAX_VALUE;
                } else {
                    this.subMatrix[i][j] = matrix[subX][subY];
                }
            }
        }
    }

    public boolean contains(int x, int y) {
        int size = this.subMatrix.length;

        boolean xRange = ((this.getX() + size)) > x && (x >= this.getX());
        boolean yRange = ((this.getY() + size)) > y && (y >= this.getY());
        return xRange && yRange;
    }

    public void setMatrixValue(int x, int y, int update) {
        int internX = getInternX(x);
        int internY = getInternY(y);

        subMatrix[internX][internY] = update;
    }

    public int getMatrixValue(int x, int y) {
        int internX = getInternX(x);
        int internY = getInternY(y);

        return subMatrix[internX][internY];
    }

    private int getInternX(int x) {
        return x - this.pos.getX();
    }

    private int getInternY(int y) {
        return y - this.pos.getY();
    }

    private Set<DFWPosition> calculateNeighborPositions() {
        Set<DFWPosition> positions = new HashSet<>();
        int x = this.getX();
        int y = this.getY();
        int blksize = this.subMatrix.length;

        // down
        if(this.validPosition(x + blksize, y)) {
            positions.add(new DFWPosition(x + blksize, y));
        }

        // top
        if(this.validPosition(x - blksize, y)) {
            positions.add(new DFWPosition(x - blksize, y));
        }

        // right
        if(this.validPosition(x, y + blksize)) {
            positions.add(new DFWPosition(x, y + blksize));
        }

        // left
        if(this.validPosition(x, y - blksize)) {
            positions.add(new DFWPosition(x, y - blksize));
        }

        return positions;
    }

    public int[][] getMatrix() {
        return subMatrix;
    }

    public Set<DFWPosition> getNeighborPositions() {
        return neighborPositions;
    }

    public DFWPosition getDFWPosition() {
        return this.pos;
    }

    public int getX() {
        return this.pos.getX();
    }

    public int getY() {
        return this.pos.getY();
    }

    public boolean sameXTo(SubMatrix subMatrix) {
        return this.getX() == subMatrix.getX();
    }

    public boolean sameYTo(SubMatrix subMatrix) {
        return this.getY() == subMatrix.getY();
    }

    public int getDistanceX(SubMatrix subMatrix) {
        return Math.abs(this.getX() - subMatrix.getX());
    }

    public int getDistanceY(SubMatrix subMatrix) {
        return Math.abs(this.getY() - subMatrix.getY());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SubMatrix) {
            SubMatrix other = (SubMatrix) o;
            return Arrays.deepEquals(other.subMatrix, this.subMatrix);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.pos.hashCode();
    }

}
