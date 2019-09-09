package main;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DFW {

    private int[][] matrix;
    private int blksize;
    private DFWCoordinator dfwCoordinator;

    public DFW(int[][] matrix, int blksize) {
        this.matrix = matrix;
        this.blksize = blksize;
        this.dfwCoordinator = new DFWCoordinator(matrix.length, blksize);
    }

    public boolean calculated() {
        return !this.dfwCoordinator.isNotDone();
    }

    public int[][] getMatrix() {
        return this.matrix;
    }

    public DFWBlock getBlock() {

        if(this.dfwCoordinator.isNotDone()) {
            Position nextPosition = this.dfwCoordinator.getNext();

            return this.getNextBlock(nextPosition);
        }

        return null;
    }

    private DFWBlock getNextBlock(Position position) {
        Position pivot = this.dfwCoordinator.getPivotFromPosition(position);
        SubMatrix target = new SubMatrix(this.matrix, position, this.blksize);
        Set<Position> dependencies = this.dfwCoordinator.getDependenciesFromPosition(position);
        List<SubMatrix> subMatrices = new LinkedList<>();

        dependencies.forEach(d -> subMatrices.add(new SubMatrix(this.matrix, d, this.blksize)));

        return new DFWBlock(target, pivot, subMatrices);
    }

    public void dispatch(SubMatrix block) {
        this.dfwCoordinator.calculated(block.getPosition());
        this.merge(block);
    }

    private void merge(SubMatrix block) {
        int x = block.getX();
        int y = block.getY();
        int [][] data = block.getSubMatrix();
        int max = this.matrix.length;
        int size = data.length;

        for (int i = x; i < x + size && i < max; i++) {
            for (int j = y; j < y + size && j < max; j++) {
                this.matrix[i][j] = block.getMatrixValue(i, j);
            }
        }
    }

}
