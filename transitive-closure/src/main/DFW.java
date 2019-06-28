package main;

import java.util.*;

public class DFW {

    private List<DFWBlock> work;
    private Set<DFWPosition> calculated;
    private Set<DFWPosition> cross;

    private SubMatrix pivot;

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
        int size = this.pivotsMax;

        this.pivotIndex++;
        this.triplePending = size * size - (size * 2 - 1);

        return new SubMatrix(matrix, x, y, this.blksize);
    }

    private Set<DFWPosition> calculateCross() {

        Set<DFWPosition> positions = new HashSet<>();

        for (int x = 0; x < this.matrix.length; x += this.blksize) {
            if (x != this.pivot.getX()) {
                positions.add(new DFWPosition(x, this.pivot.getY()));
            }
        }

        for (int y = 0; y < this.matrix.length; y += this.blksize) {
            if (y != this.pivot.getY()) {
                positions.add(new DFWPosition(this.pivot.getX(), y));
            }
        }

        return positions;
    }

    private void generateTuples() {

        for (int i = 0; i < this.matrix.length; i+= this.blksize) {

            if (i != this.pivot.getX()) {
                SubMatrix target = new SubMatrix(this.matrix, i, this.pivot.getY(), this.blksize);
                DFWBlock block = new DFWBlock(target, this.pivot.getDFWPosition());
                block.addPath(this.pivot);

                this.work.add(block);
            }

            if (i != this.pivot.getY()) {
                SubMatrix target = new SubMatrix(matrix, this.pivot.getX(), i, blksize);
                DFWBlock block = new DFWBlock(target, this.pivot.getDFWPosition());
                block.addPath(this.pivot);

                this.work.add(block);
            }

        }

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

    private void generateTriple(SubMatrix block) {
        Set<DFWPosition> pairingPositions = this.pivot.getPairingPositions(block);

        for (DFWPosition pos: pairingPositions) {

            // block is available
            if(this.calculated.contains(pos)) {
                DFWPosition targetPos = this.pivot.getTarget(block.getDFWPosition(), pos);
                SubMatrix target = new SubMatrix(this.matrix, targetPos, this.blksize);
                DFWBlock work = new DFWBlock(target, this.pivot.getDFWPosition());
                SubMatrix path = new SubMatrix(this.matrix, pos, this.blksize);
                work.addPath(block);
                work.addPath(path);

                this.work.add(work);
            }
        }

    }

    private boolean isTuple(SubMatrix block) {
        DFWPosition pos = block.getDFWPosition();

        return !this.pivot.equals(pos) && this.cross.contains(pos);
    }


    private boolean isTriple(SubMatrix block) {
        DFWPosition pos = block.getDFWPosition();

        return !this.pivot.equals(pos) && !this.cross.contains(pos);
    }

    private void nextRound() {
        if(this.triplePending > 0) {
            return;
        }

        this.pivot = this.getNextPivot();
        this.work.add(new DFWBlock(this.pivot, this.pivot.getDFWPosition()));

        this.calculated.clear();
        this.cross.clear();
        this.cross.addAll(this.calculateCross());
    }

    public DFW(int[][] matrix, int blksize) {
        double pivots =  (double) matrix.length / blksize;

        this.matrix = matrix;
        this.blksize = blksize;
        this.pivotIndex = 0;
        this.work = new LinkedList<>();
        this.calculated = new HashSet<>();
        this.cross = new HashSet<>();

        this.pivotsMax = (int) Math.ceil(pivots);
        this.pivot = this.getNextPivot();

        this.cross.addAll(this.calculateCross());

        this.work.add(new DFWBlock(this.pivot, this.pivot.getDFWPosition()));
    }

    public boolean isDone() {
        return this.work.isEmpty() || this.pivotIndex > this.pivotsMax;
    }

    public int[][] getMatrix() {
        return this.matrix;
    }

    public DFWBlock getWork() {

        if(!this.work.isEmpty()) {
            DFWBlock work = this.work.remove(0);

            return work;
        }

        return null;
    }

    public void dispatch(SubMatrix block) {

        // Pivot got calculated by worker
        if (this.pivot.equals(block)) {

            this.merge(block);
            this.generateTuples();

        } else if (this.isTuple(block)) {

            this.merge(block);
            this.generateTriple(block);
            this.calculated.add(block.getDFWPosition());

        } else if (this.isTriple(block)) {

            this.merge(block);
            this.triplePending--;
            this.nextRound();

        }
    }

}
