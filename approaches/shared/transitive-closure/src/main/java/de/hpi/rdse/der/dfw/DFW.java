package de.hpi.rdse.der.dfw;

import de.hpi.rdse.der.util.Position;
import de.hpi.rdse.der.util.SubMatrix;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DFW {

    private int[][] matrix;
    private int blksize;
    private DFWCoordinator dfwCoordinator;

    /**
     * @param matrix Adjacent matrix of the directed graph
     * @param blksize Size of the blocks
     */
    public DFW(int[][] matrix, int blksize) {
        this.matrix = matrix;
        this.blksize = blksize;
        this.dfwCoordinator = new DFWCoordinator(matrix.length, blksize);
    }

    /**
     * @return Returns if the calculation is done
     */
    public boolean isCalculated() {
        return this.dfwCoordinator.isDone();
    }

    public int[][] getMatrix() {
        return this.matrix;
    }

    /**
     * @return Returns if there are still blocks to calculate
     */
    public boolean hasNextBlock() {
        return this.dfwCoordinator.hasNext();
    }

    /**
     * @return Returns the next block or null when this is not possible, e.g. calculation is done
     */
    public DFWBlock getNextBlock() {
        Position position = this.dfwCoordinator.getNextPosition();

        if(position != null) {

            return this.getNextBlock(position);
        }

        return null;
    }

    /**
     * @param block Resolves a block that was calculated
     */
    public void dispatch(SubMatrix block) {
        this.dfwCoordinator.calculated(block.getPosition());
        this.merge(block);
    }

    private DFWBlock getNextBlock(Position position) {
        Position pivot = this.dfwCoordinator.getPivot(position);
        SubMatrix target = new SubMatrix(this.matrix, position, this.blksize);
        Set<Position> dependencies = this.dfwCoordinator.getDependencies(position);
        List<SubMatrix> subMatrices = new LinkedList<>();

        dependencies.forEach(d -> subMatrices.add(new SubMatrix(this.matrix, d, this.blksize)));

        return new DFWBlock(target, pivot, subMatrices);
    }

    private void merge(SubMatrix subMatrix) {
        int x = subMatrix.getX();
        int y = subMatrix.getY();
        int [][] data = subMatrix.getSubMatrix();
        int max = this.matrix.length;
        int size = data.length;

        for (int i = x; i < x + size && i < max; i++) {
            for (int j = y; j < y + size && j < max; j++) {
                this.matrix[i][j] = subMatrix.getValue(i, j);
            }
        }
    }

}
