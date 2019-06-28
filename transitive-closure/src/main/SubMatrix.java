package main;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SubMatrix {

    private int matrixSize;
    private int blksize;
    private int [][] subMatrix;
    private DFWPosition position;

    private Set<DFWPosition> pairingPositionsX;
    private Set<DFWPosition> pairingPositionsY;

    private boolean validPosition(DFWPosition p) {
        return p.getX() >= 0 && p.getX() < this.matrixSize && p.getY() >= 0 && p.getY() < this.matrixSize;
    }

    private Set<DFWPosition> calculatePairingPositions(SubMatrix submatrix) {
        int dX = (this.sameY(submatrix) ? 1 : 0);
        int dY = (this.sameX(submatrix) ? 1 : 0);
        int posX = this.getX() * dX;
        int posY = this.getY() * dY;

        Set<DFWPosition> positions = new HashSet<>();

        for (int i = 0; i < this.matrixSize; i += this.blksize) {

            if(this.sameX(submatrix)) {
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

    private Set<DFWPosition> getPairingPositionsX(SubMatrix submatrix) {
        if(this.pairingPositionsX == null) {
            this.pairingPositionsX = this.calculatePairingPositions(submatrix);
        }

        return this.pairingPositionsX;
    }

    private Set<DFWPosition> getPairingPositionsY(SubMatrix submatrix) {
        if(this.pairingPositionsY == null) {
            this.pairingPositionsY = this.calculatePairingPositions(submatrix);
        }

        return this.pairingPositionsY;
    }

    private boolean sameX(SubMatrix subMatrix) {
        return this.getX() == subMatrix.getX();
    }

    private boolean sameY(SubMatrix subMatrix) {
        return this.getY() == subMatrix.getY();
    }

    public SubMatrix(int [][] matrix, int x, int y, int size) {
        this(matrix, new DFWPosition(x, y), size);
    }

    public SubMatrix(int [][] matrix, DFWPosition position, int size) {
        this.position = position;
        this.matrixSize = matrix.length;
        this.blksize = size;
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

    public DFWPosition getTarget(DFWPosition p1, DFWPosition p2){
        int x = 0;
        int y = 0;

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

    public int getBlksize() {
        return blksize;
    }

    public DFWPosition getDFWPosition() {
        return this.position;
    }

    public int getX() {
        return this.position.getX();
    }

    public int getY() {
        return this.position.getY();
    }

    public Set<DFWPosition> getPairingPositions(SubMatrix submatrix) {
        boolean sameBlksize = this.blksize == submatrix.blksize;
        Set<DFWPosition> positions = new HashSet<>();

        if(sameBlksize && this.sameX(submatrix)) {
            positions = this.getPairingPositionsY(submatrix);
        } else if (sameBlksize && this.sameY(submatrix)) {
            positions = this.getPairingPositionsX(submatrix);
        }

        return positions;
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
        return x - this.getX();
    }

    private int getInternY(int y) {
        return y - this.getY();
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
