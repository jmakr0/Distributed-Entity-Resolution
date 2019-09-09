package main;

import java.util.*;

public class DFWCoordinator {

    class Block {
        int round = 0;
        DFWPosition position;
        Map<Integer, Set<Block>> previous = new HashMap<>();
        Map<Integer, Set<Block>> next = new HashMap<>();
        Map<Integer, Set<Block>> dependedOn = new HashMap<>();

        void setNext(int round, Block nextBlk) {
            setDependencyNextRound();
            nextBlk.setPrevious(round, this);

            if (!this.next.containsKey(round)) {
                this.next.put(round, new HashSet<>());
            }

            this.next.get(round).add(nextBlk);
        }

        void setPrevious(int round, Block previousBlk) {
            if (!this.previous.containsKey(round)) {
                this.previous.put(round, new HashSet<>());
            }
            this.previous.get(round).add(previousBlk);
        }

        void removePrevious(int round, Block blk) {
            if (!this.previous.containsKey(round)) {
                return;
            }
            if (this.previous.get(round).contains(blk)) {
                this.previous.get(round).remove(blk);
                this.setDependedOn(round, blk);
            }
        }

        @Override
        public boolean equals(Object o) {
            return this.position.equals(o);
        }

        @Override
        public int hashCode() {
            return this.position.hashCode();
        }

        private void setDependencyNextRound() {
            if (round - 1 < 0) {
                return;
            }

            if (!this.previous.containsKey(round)) {
                this.previous.put(round, new HashSet<>());
            }

            this.previous.get(round).add(this);
        }

        private void setDependedOn(int round, Block blk) {
            if (!this.dependedOn.containsKey(round)) {
                this.dependedOn.put(round, new HashSet<>());
            }

            this.dependedOn.get(round).add(blk);
        }
    }

    private int maxRounds;
    private int blksize;
    private int pendingResponses = 0;
    private Queue<DFWPosition> pending;
    private Map<DFWPosition, Block> blocks;

    public DFWCoordinator(int matrixSize, int blksize) {
        int paddedMatrixSize = (int) Math.ceil((double) matrixSize / blksize) * blksize;
        DFWPosition start = new DFWPosition(0, 0);
        this.pending = new LinkedList<>();

        this.blocks = this.generateBlocks(paddedMatrixSize, blksize);

        this.generateDependencies(paddedMatrixSize, blksize);

        this.blksize = blksize;
        this.maxRounds = (paddedMatrixSize / blksize) -1;
        this.pending.add(start);
    }

    public void calculated(DFWPosition position) {
        List<DFWPosition> positions = getNextPositions(position);

        this.pending.addAll(positions);
        this.pendingResponses --;
    }

    public DFWPosition getNext() {
        if (isNotDone()) {
            this.pendingResponses ++;

            return this.pending.poll();
        }

        return null;
    }

    public DFWPosition getPivotFromPosition(DFWPosition position) {
        Block blk = this.blocks.get(position);
        int round = blk.round;
        int blkSize = this.blksize;

        return new DFWPosition(round * blkSize, round * blkSize);
    }

    public Set<DFWPosition> getDependenciesFromPosition(DFWPosition position) {
        Block blk = this.blocks.get(position);
        Set<DFWPosition> result = new HashSet<>();

        if (blk.dependedOn.containsKey(blk.round)) {
            blk.dependedOn.get(blk.round).forEach(dependency -> result.add(dependency.position));
        }

        return result;
    }

    public boolean isNotDone() {
        return !this.pending.isEmpty() || this.pendingResponses > 0;
    }

    private Map<DFWPosition, Block> generateBlocks(int matrixSize, int blksize) {
        final Map<DFWPosition, Block>  blocks = new HashMap<>();

        for (int x = 0; x < matrixSize; x += blksize) {
            for (int y = 0; y < matrixSize; y += blksize) {
                Block block = new Block();
                block.position = new DFWPosition(x, y);
                blocks.put(block.position, block);
            }
        }

        return blocks;
    }

    private Block getBlock(int x, int y) {
        return this.blocks.get(new DFWPosition(x, y));
    }

    private void generateDependencies(int matrixSize, int blksize) {
        int round = 0;

        for (int k = 0; k < matrixSize; k += blksize) {

            Block pivot = this.getBlock(k, k);

            // tuples
            for (int i = 0; i < matrixSize; i += blksize) {

                if(i != k) {
                    Block x = this.getBlock(i, k);
                    Block y = this.getBlock(k, i);

                    pivot.setNext(round, x);
                    pivot.setNext(round, y);

                    // triples
                    for (int j = 0; j < matrixSize; j += blksize) {

                        if (j != k) {
                            Block crossX = this.getBlock(k, j);
                            Block crossY = this.getBlock(j, k);

                            Block x1 = this.getBlock(j, i);
                            Block y1 = this.getBlock(i, j);

                            x.setNext(round, y1);
                            y.setNext(round, x1);

                            if (!x1.position.equals(y1.position)) {
                                crossX.setNext(round, y1);
                                crossY.setNext(round, x1);
                            }

                        }

                    }
                }

            }

            round ++;

        }
    }

    private List<DFWPosition> getNextPositions(DFWPosition position) {
        final List<DFWPosition> result = new LinkedList<>();
        Block blk = this.blocks.get(position);

        int round = blk.round;
        boolean hasDependenciesNextRound = blk.previous.containsKey(round + 1);
        boolean dependsOnBlk = hasDependenciesNextRound && blk.previous.get(round + 1).contains(blk);
        boolean isNextPivot = !hasDependenciesNextRound;

        if (hasDependenciesNextRound && dependsOnBlk && round < maxRounds) {
            // remove itself for the next round
            blk.removePrevious(round + 1, blk);
            // ready for the next round
            if (blk.previous.get(round + 1).isEmpty()) {
                result.add(blk.position);
            }
        }

        // next pivot element; has no previous in next round
        if (isNextPivot && round < maxRounds) {
            result.add(blk.position);
        }

        if (blk.next.containsKey(round)) {

            for (Block nextBlk : blk.next.get(round)) {
                nextBlk.removePrevious(round, blk);

                if (nextBlk.previous.get(round).isEmpty()) {
                    result.add(nextBlk.position);
                }

            }

        }

        if (blk.round < maxRounds) {
            blk.round++;
        }

        return result;
    }

}
