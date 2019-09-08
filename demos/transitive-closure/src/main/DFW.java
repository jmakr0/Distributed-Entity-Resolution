package main;

import java.util.Set;

public class DFW {

    private DFWCoordinator dfwCoordinator;

//    private List<DFWBlock> pendingBlocks;
//    private Set<DFWPosition> dependedOn;
//    private Set<DFWPosition> crossPositions;
//
//    private SubMatrix pivot;
//    private int pivotIndex;
//    private int pivotsMax;

    private int[][] matrix;
    private int blksize;
//
//    private int pendingTriplesCount;

    public DFW(int[][] matrix, int blksize) {
        this.matrix = matrix;
        this.blksize = blksize;

        this.dfwCoordinator = new DFWCoordinator(matrix.length, blksize);

//        this.pivotIndex = 0;
//
//        this.pendingBlocks = new LinkedList<>();
//        this.dependedOn = new HashSet<>();
//        this.crossPositions = new HashSet<>();
//
//        this.pivotsMax = (int) Math.ceil((double) matrix.length / blksize);
//        this.pivot = this.getNextPivot(this.pivotIndex);
//        this.pivotIndex++;
//        this.pendingTriplesCount = this.calculateTripleCount(this.pivotsMax);

//        this.crossPositions.addAll(this.calculateCrossPositions());
//
//        this.pendingBlocks.add(new DFWBlock(this.pivot, this.pivot));
    }

    public boolean calculated() {
        return !this.dfwCoordinator.isNotDone();
//        return this.pivotIndex > this.pivotsMax;
    }

    public int[][] getMatrix() {
        return this.matrix;
    }

    public DFWBlock getBlock() {

        if(this.dfwCoordinator.isNotDone()) {
            DFWPosition nextPosition = this.dfwCoordinator.getNext();
            DFWPosition pivotPosition = this.dfwCoordinator.getPivotFromPosition(nextPosition);
            Set<DFWPosition> dependencies = this.dfwCoordinator.getDependenciesFromPosition(nextPosition);

            SubMatrix target = new SubMatrix(this.matrix, nextPosition, this.blksize);

            System.out.println(dependencies);

            return new DFWBlock(target, pivotPosition);
        }

        return null;
    }

    public void dispatch(SubMatrix block) {
        this.dfwCoordinator.calculated(block.getPosition());
        this.merge(block);

//        this.merge(block);
//
//        if (this.isPivot(block)) {
//
//            this.generateTuples();
//
//        } else if (this.isTuple(block)) {
//
//            this.generateTriple(block);
//            this.dependedOn.add(block.getPosition());
//
//        } else if (this.isTriple(block)) {
//
//            this.pendingTriplesCount--;
//
//        }
//
//        this.nextPhase();
    }

//    private SubMatrix getNextPivot(int pivotIndex) {
//        if(pivotIndex > this.pivotsMax) {
//            throw new IndexOutOfBoundsException("Next pivot does not exist");
//        }
//
//        // note: quadratic matrix
//        int x = this.blksize * pivotIndex;
//        int y = x;
//
//        return new SubMatrix(matrix, x, y, this.blksize);
//    }

//    private int calculateTripleCount(int pivotSize){
//        return pivotSize * pivotSize - (pivotSize * 2 - 1);
//    }

//    private Set<DFWPosition> calculateCrossPositions() {
//        Set<DFWPosition> positions = new HashSet<>();
//
//        for (int x = 0; x < this.matrix.length; x += this.blksize) {
//            if (x != this.pivot.getX()) {
//                positions.add(new DFWPosition(x, this.pivot.getY()));
//            }
//        }
//
//        for (int y = 0; y < this.matrix.length; y += this.blksize) {
//            if (y != this.pivot.getY()) {
//                positions.add(new DFWPosition(this.pivot.getX(), y));
//            }
//        }
//
//        return positions;
//    }

//    private void generateTuples() {
//
//        for (int i = 0; i < this.matrix.length; i+= this.blksize) {
//
//            if (i != this.pivot.getX()) {
//                SubMatrix target = new SubMatrix(this.matrix, i, this.pivot.getY(), this.blksize);
//                DFWBlock block = new DFWBlock(target, this.pivot);
//                block.addSubMatrix(this.pivot);
//
//                this.pendingBlocks.add(block);
//            }
//
//            if (i != this.pivot.getY()) {
//                SubMatrix target = new SubMatrix(matrix, this.pivot.getX(), i, blksize);
//                DFWBlock block = new DFWBlock(target, this.pivot);
//                block.addSubMatrix(this.pivot);
//
//                this.pendingBlocks.add(block);
//            }
//
//        }
//
//    }

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

//    private void generateTriple(SubMatrix other) {
//        Set<DFWPosition> pairingPositions = this.pivot.getPairingPositions(other);
//
//        for (DFWPosition pos: pairingPositions) {
//
//            // block is available
//            if(this.dependedOn.contains(pos)) {
//                DFWPosition targetPos = this.pivot.getTargetPosition(other.getPosition(), pos);
//                SubMatrix target = new SubMatrix(this.matrix, targetPos, this.blksize);
//
//                DFWBlock block = new DFWBlock(target, this.pivot);
//                SubMatrix path = new SubMatrix(this.matrix, pos, this.blksize);
//
//                block.addSubMatrix(other);
//                block.addSubMatrix(path);
//
//                this.pendingBlocks.add(block);
//            }
//        }
//
//    }

//    private boolean isPivot(SubMatrix block) {
//        return this.pivot.equals(block);
//    }
//
//    private boolean isTuple(SubMatrix block) {
//        DFWPosition pos = block.getPosition();
//
//        return !this.pivot.equals(pos) && this.crossPositions.contains(pos);
//    }
//
//
//    private boolean isTriple(SubMatrix block) {
//        DFWPosition pos = block.getPosition();
//
//        return !this.pivot.equals(pos) && !this.crossPositions.contains(pos);
//    }

//    private void nextPhase() {
//        if(this.pendingTriplesCount > 0) {
//            return;
//        }
//
//        this.dependedOn.clear();
//        this.crossPositions.clear();
//
//        this.pivot = this.getNextPivot(this.pivotIndex);
//        this.pivotIndex++;
//        this.pendingTriplesCount = this.calculateTripleCount(this.pivotsMax);
//
//        if(!this.dependedOn()) {
//            this.pendingBlocks.add(new DFWBlock(this.pivot, this.pivot));
//            this.crossPositions.addAll(this.calculateCrossPositions());
//        }
//    }

}
