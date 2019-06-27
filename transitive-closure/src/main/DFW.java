package main;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DFW {

    private List<DFWBlock> work;
    private Set<DFWPosition> calculated;

    private SubMatrix pivot;

    private boolean hasData = false;
    private int[][] matrix;
    private int blksize;
    private int pivotsMax;
    private int pivotIndex;
    private int triplePending;

    private SubMatrix getNextPivot() {
        if(this.pivotIndex > this.pivotsMax) {
            throw new IndexOutOfBoundsException("Next pivot does not exist");
        }

        // note: quadratic matrix
        int x = this.blksize * this.pivotIndex;
        int y = x;
        int size = this.matrix.length;

        this.pivotIndex++;
        this.triplePending = size * size - (size * 2 - 1);

        return new SubMatrix(matrix, x, y, this.blksize);
    }

    private void generateTuples() {

        for (int i = 0; i < this.matrix.length; i+= this.blksize) {

            if (i != this.pivot.getX()) {
                SubMatrix target = new SubMatrix(this.matrix, i, this.pivot.getY(), this.blksize);
                DFWBlock block = new DFWBlock(target);
                block.addPath(this.pivot);

                this.work.add(block);
            }

            if (i != this.pivot.getY()) {
                SubMatrix target = new SubMatrix(matrix, this.pivot.getX(), i, blksize);
                DFWBlock block = new DFWBlock(target);
                block.addPath(this.pivot);

                this.work.add(block);
            }

        }

    }

    private void merge(SubMatrix block) {
        int x = block.getX();
        int y = block.getY();
        int [][] data = block.getMatrix();
        int size = data.length;

        for (int i = x; i < x + size; i++) {
            for (int j = y; j < y + size; j++) {
                this.matrix[i][j] = data[i][j];
            }
        }

        this.calculated.add(block.getDFWPosition());
    }

    private void generateTriple(SubMatrix block) {
        Set<DFWPosition> pairingPositions = this.pivot.getPairingPositions(block);

        for (DFWPosition pos: pairingPositions) {

            // block is available
            if(this.calculated.contains(pos)) {
                DFWBlock work = new DFWBlock(block);
                SubMatrix path = new SubMatrix(this.matrix, pos, this.blksize);
                work.addPath(path);

                this.work.add(work);
            }
        }

    }

    private boolean isTuple(SubMatrix block) {
        DFWPosition pos = block.getDFWPosition();

        return !this.pivot.equals(pos) && this.pivot.getPairingPositions(block).contains(pos);
    }


    private boolean isTriple(SubMatrix block) {
        DFWPosition pos = block.getDFWPosition();

        return !this.pivot.equals(pos) && !this.pivot.getPairingPositions(block).contains(pos);
    }

    private void nextRound() {
        if(this.hasData && this.triplePending > 0) {
            return;
        }

        this.pivotIndex++;
        this.pivot = this.getNextPivot();
        this.work.add(new DFWBlock(this.pivot));
        this.calculated.clear();
    }

    public DFW(int[][] matrix, int blksize) {
        double pivots =  (double) matrix.length / blksize;

        this.matrix = matrix;
        this.blksize = blksize;
        this.pivotIndex = 0;
        this.work = new LinkedList<>();
        this.calculated = new HashSet<>();

        this.pivotsMax = (int) Math.ceil(pivots);
        this.pivot = this.getNextPivot();

        this.work.add(new DFWBlock(this.pivot));
    }

    public DFWBlock getWork() {

        if(!this.work.isEmpty()) {
            DFWBlock work = this.work.remove(0);
            this.hasData = this.work.isEmpty();

            return work;
        }

        this.hasData = false;
        return null;
    }

    public void dispatch(SubMatrix block) {
        // todo: maybe apply builder pattern

        // Pivot got calculated by worker
        if (this.pivot.equals(block)) {

            this.generateTuples();

        } else if (this.isTuple(block)) {

            this.merge(block);
            this.generateTriple(block);

        } else if (this.isTriple(block)) {

            this.merge(block);
            this.triplePending--;
            this.nextRound();

        }
    }

}
