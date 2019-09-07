package main;

import java.util.*;

public class DFWCoordinator {

    class Block {
        int round = 0;
        DFWPosition position;
        Map<Integer, Set<Block>> dependencies = new HashMap<>();
        Map<Integer, Set<Block>> next = new HashMap<>();

        @Override
        public boolean equals(Object o) {
            return this.position.equals(o);
        }

        @Override
        public int hashCode() {
            return this.position.hashCode();
        }
    }

    private int maxRounds;
    private int pendingResponses = 0;
    private Queue<DFWPosition> pending;
    private Map<DFWPosition, Block> blocks;

    public DFWCoordinator(int matrixSize, int blksize) {
        DFWPosition start = new DFWPosition(0, 0);
        this.pending = new LinkedList<>();

        this.blocks = this.generateBlocks(matrixSize, blksize);

        this.generateDependencies(matrixSize, blksize);

        this.maxRounds = (matrixSize / blksize) -1;
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

    private void setDependency(int round, DFWPosition position) {
        int priorRound = round - 1;

        if (priorRound < 0) {
            return;
        }

        Block blk = this.blocks.get(position);

        if (!blk.dependencies.containsKey(round)) {
            blk.dependencies.put(round, new HashSet<>());
        }

        blk.dependencies.get(round).add(blk);
    }

    private void setDependency(int round, DFWPosition from, DFWPosition to) {
        Block blkFrom = this.blocks.get(from);
        Block blkTo = this.blocks.get(to);

        if (!blkFrom.next.containsKey(round)) {
            blkFrom.next.put(round, new HashSet<>());
        }

        if (!blkTo.dependencies.containsKey(round)) {
            blkTo.dependencies.put(round, new HashSet<>());
        }

        blkFrom.next.get(round).add(blkTo);
        blkTo.dependencies.get(round).add(blkFrom);
    }

    private void generateDependencies(int matrixSize, int blksize) {
        int round = 0;

        for (int k = 0; k < matrixSize; k += blksize) {

            DFWPosition pivot = new DFWPosition(k, k);

            // tuples
            for (int i = 0; i < matrixSize; i += blksize) {

                if(i != k) {
                    DFWPosition x = new DFWPosition(i, k);
                    DFWPosition y = new DFWPosition(k, i);

                    this.setDependency(round, x);
                    this.setDependency(round, y);

                    this.setDependency(round, pivot, x);
                    this.setDependency(round, pivot, y);

                    // triples
                    for (int j = 0; j < matrixSize; j += blksize) {

                        if (j != k) {
                            DFWPosition crossX =  new DFWPosition(k, j);
                            DFWPosition crossY =  new DFWPosition(j, k);

                            DFWPosition x1 = new DFWPosition(j, i);
                            DFWPosition y1 = new DFWPosition(i, j);

                            this.setDependency(round, x, y1);
                            this.setDependency(round, y, x1);

                            if (!x1.equals(y1)) {
                                this.setDependency(round, crossX, y1);
                                this.setDependency(round, crossY, x1);
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

        if (blk.next.containsKey(round)) {

            for (Block nextBlk : blk.next.get(round)) {
                nextBlk.dependencies.get(round).remove(blk);

                if (nextBlk.dependencies.get(round).isEmpty()) {
                    result.add(nextBlk.position);

                    if (!nextBlk.dependencies.containsKey(round + 1) && round < this.maxRounds) {
                        nextBlk.round ++;
                    }
                }

            }

        }

        blk.round ++;

        if (blk.dependencies.containsKey(blk.round)) {
            // remove itself for the next round
            blk.dependencies.get(blk.round).remove(blk);
            // ready for the next round
            if (blk.dependencies.get(blk.round).isEmpty()) {
                result.add(blk.position);
            }
        }

        return result;
    }

}
