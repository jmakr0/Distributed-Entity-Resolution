package main;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DFW {

    private List<DFWBlock> pendingBlocks;
    private Set<DFWPosition> calculated;
    private Set<DFWPosition> crossPositions;

    private SubMatrix pivot;
    private int pivotIndex;
    private int pivotsMax;

    private int[][] matrix;
    private int blksize;

    private int pendingTriplesCount;

    private SubMatrix getNextPivot(int pivotIndex) {
        if(this.pivotIndex > this.pivotsMax) {
            throw new IndexOutOfBoundsException("Next pivot does not exist");
        }

        // note: quadratic matrix
        int x = this.blksize * pivotIndex;
        int y = x;

        return new SubMatrix(matrix, x, y, this.blksize);
    }

    private int calculateTripleCount(int pivotSize){
        return pivotSize * pivotSize - (pivotSize * 2 - 1);
    }

    private Set<DFWPosition> calculateCrossPositions() {
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
                DFWBlock block = new DFWBlock(target, this.pivot);
                block.addSubMatrix(this.pivot);

                this.pendingBlocks.add(block);
            }

            if (i != this.pivot.getY()) {
                SubMatrix target = new SubMatrix(matrix, this.pivot.getX(), i, blksize);
                DFWBlock block = new DFWBlock(target, this.pivot);
                block.addSubMatrix(this.pivot);

                this.pendingBlocks.add(block);
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

    private void generateTriple(SubMatrix other) {
        Set<DFWPosition> pairingPositions = this.pivot.getPairingPositions(other);

        for (DFWPosition pos: pairingPositions) {

            // block is available
            if(this.calculated.contains(pos)) {
                DFWPosition targetPos = this.pivot.getTargetPosition(other.getPosition(), pos);
                SubMatrix target = new SubMatrix(this.matrix, targetPos, this.blksize);

                DFWBlock block = new DFWBlock(target, this.pivot);
                SubMatrix path = new SubMatrix(this.matrix, pos, this.blksize);

                block.addSubMatrix(other);
                block.addSubMatrix(path);

                this.pendingBlocks.add(block);
            }
        }

    }

    private boolean isPivot(SubMatrix block) {
        return this.pivot.equals(block);
    }

    private boolean isTuple(SubMatrix block) {
        DFWPosition pos = block.getPosition();

        return !this.pivot.equals(pos) && this.crossPositions.contains(pos);
    }


    private boolean isTriple(SubMatrix block) {
        DFWPosition pos = block.getPosition();

        return !this.pivot.equals(pos) && !this.crossPositions.contains(pos);
    }

    private void nextPhase() {
        if(this.pendingTriplesCount > 0) {
            return;
        }

        this.pivot = this.getNextPivot(this.pivotIndex);
        this.pivotIndex++;
        this.pendingTriplesCount = this.calculateTripleCount(this.pivotsMax);

        this.pendingBlocks.add(new DFWBlock(this.pivot, this.pivot));

        this.calculated.clear();
        this.crossPositions.clear();
        this.crossPositions.addAll(this.calculateCrossPositions());
    }

    public DFW(int[][] matrix, int blksize) {
        this.matrix = matrix;
        this.blksize = blksize;
        this.pivotIndex = 0;

        this.pendingBlocks = new LinkedList<>();
        this.calculated = new HashSet<>();
        this.crossPositions = new HashSet<>();

        this.pivotsMax = (int) Math.ceil((double) matrix.length / blksize);
        this.pivot = this.getNextPivot(this.pivotIndex);
        this.pivotIndex++;
        this.pendingTriplesCount = this.calculateTripleCount(this.pivotsMax);

        this.crossPositions.addAll(this.calculateCrossPositions());

        this.pendingBlocks.add(new DFWBlock(this.pivot, this.pivot));
    }

    public boolean calculated() {
        return this.pendingBlocks.isEmpty() || this.pivotIndex > this.pivotsMax;
    }

    public int[][] getMatrix() {
        return this.matrix;
    }

    public DFWBlock getBlock() {

        if(!this.pendingBlocks.isEmpty()) {
            DFWBlock block = this.pendingBlocks.remove(0);

            return block;
        }

        return null;
    }

    public void dispatch(SubMatrix block) {

        if (this.isPivot(block)) {

            this.merge(block);
            this.generateTuples();

        } else if (this.isTuple(block)) {

            this.merge(block);
            this.generateTriple(block);
            this.calculated.add(block.getPosition());

        } else if (this.isTriple(block)) {

            this.merge(block);
            this.pendingTriplesCount--;
            this.nextPhase();

        }
    }

}
