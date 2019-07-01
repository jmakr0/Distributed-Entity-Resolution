package main;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SubMatrix {

    private int matrixSize;
    private int subMatrixSize;
    private int [][] subMatrix;

    private DFWPosition position;
    private Set<DFWPosition> tuplePositionsXAxis;
    private Set<DFWPosition> tuplePositionsYAxis;

    private boolean validPosition(DFWPosition p) {
        return p.getX() >= 0 && p.getX() < this.matrixSize && p.getY() >= 0 && p.getY() < this.matrixSize;
    }

    private Set<DFWPosition> calculateTuplePositions(SubMatrix other) {
        int posX = this.getX();
        int posY = this.getY();

        Set<DFWPosition> positions = new HashSet<>();

        for (int i = 0; i < this.matrixSize; i += this.subMatrixSize) {

            if(this.position.sameX(other.getX())) {
                posX = i;
            } else {
                posY = i;
            }

            DFWPosition pos = new DFWPosition(posX, posY);

            if(this.validPosition(pos) && !this.position.equals(pos)) {
                positions.add(pos);
            }
        }

        return positions;
    }

    private Set<DFWPosition> getTuplePositionsXAxis(SubMatrix submatrix) {
        if(this.tuplePositionsXAxis == null) {
            this.tuplePositionsXAxis = this.calculateTuplePositions(submatrix);
        }

        return this.tuplePositionsXAxis;
    }

    private Set<DFWPosition> getTuplePositionsYAxis(SubMatrix submatrix) {
        if(this.tuplePositionsYAxis == null) {
            this.tuplePositionsYAxis = this.calculateTuplePositions(submatrix);
        }

        return this.tuplePositionsYAxis;
    }

    private int getInternX(int x) {
        return x - this.getX();
    }

    private int getInternY(int y) {
        return y - this.getY();
    }

    public SubMatrix(int [][] matrix, int x, int y, int size) {
        this(matrix, new DFWPosition(x, y), size);
    }

    public SubMatrix(int [][] matrix, DFWPosition position, int size) {
        this.position = position;
        this.matrixSize = matrix.length;
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

    public DFWPosition getTargetPosition(DFWPosition p1, DFWPosition p2){
        int x;
        int y;

        // same x
        if(this.getX() == p1.getX()) {
            y = p1.getY();
            int dx = Math.abs(this.getX() - p2.getX());

            if (p2.getX() > this.getX()) {
                x = p1.getX() + dx;
            } else {
                x = p1.getX() - dx;
            }
        }
        // same y
        else {
            x = p1.getX();
            int dy = Math.abs(this.getY() - p2.getY());

            if (p2.getY() > this.getY()) {
                y = p1.getY() + dy;
            } else {
                y = p1.getY() - dy;
            }
        }

        return new DFWPosition(x, y);
    }

    public int[][] getSubMatrix() {
        return subMatrix;
    }

    public DFWPosition getPosition() {
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

    public Set<DFWPosition> getPairingPositions(SubMatrix other) {
        boolean sameSize = this.subMatrixSize == other.subMatrixSize;
        boolean sameX = this.position.sameX(other.getX());
        boolean sameY = this.position.sameY(other.getY());
        Set<DFWPosition> positions = new HashSet<>();

        if(sameSize && sameX) {
            positions = this.getTuplePositionsYAxis(other);
        } else if (sameSize && sameY) {
            positions = this.getTuplePositionsXAxis(other);
        }

        return positions;
    }

    public boolean contains(int x, int y) {
        boolean xRange = (this.getX() + this.subMatrixSize) > x && (x >= this.getX());
        boolean yRange = (this.getY() + this.subMatrixSize) > y && (y >= this.getY());

        return xRange && yRange;
    }

    public void setMatrixValue(int x, int y, int update) {
        int internX = this.getInternX(x);
        int internY = this.getInternY(y);

        subMatrix[internX][internY] = update;
    }

    public int getMatrixValue(int x, int y) {
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

}
