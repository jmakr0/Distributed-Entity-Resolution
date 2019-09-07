package main;

import java.util.*;

public class DFWCoordinator {

    class Process {
        DFWPosition start;
        DFWPosition end;
    }

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

    private Process process;
    private Map<DFWPosition, Block> blocks;
    private Queue<DFWPosition> pending;

    public DFWCoordinator(int matrixSize, int blksize) {
        this.pending = new LinkedList<>();

        this.blocks = this.generateBlocks(matrixSize, blksize);
        this.process = this.generateDependencies(matrixSize, blksize);

        this.pending.add(process.start);
    }


    public void calculated(DFWPosition position) {
        List<DFWPosition> positions = getNextPositions(position);
        this.pending.addAll(positions);
    }

    public DFWPosition getNext() {
        if (isNotDone()) {
            return this.pending.poll();
        }

        return null;
    }

    public boolean isNotDone() {
        Block blk = this.blocks.get(process.end);
        int round = blk.dependencies.size() -1;

        return !this.pending.isEmpty() || !blk.dependencies.get(round).isEmpty();
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

    private void setDependency(int round, DFWPosition from, DFWPosition to) {
        if (round < 0) {
            return;
        }

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

    private void setPriorDependency(int round, DFWPosition position) {
        int priorRound = round - 1;

        if (priorRound < 0) {
            return;
        }

        Block blk = this.blocks.get(position);

        if (!blk.next.containsKey(round)) {
            blk.next.put(round, new HashSet<>());
        }

        if (!blk.dependencies.containsKey(round)) {
            blk.dependencies.put(round, new HashSet<>());
        }

        // the block depends on itself to get calculated
        blk.next.get(round).add(blk);
        blk.dependencies.get(round).add(blk);
    }

    private Process generateDependencies(int matrixSize, int blksize) {
        final Process process = new Process();
        int round = 0;

        for (int k = 0; k < matrixSize; k += blksize) {

            DFWPosition pivot = new DFWPosition(k, k);

            if (round == 0) {
                process.start = pivot;
            } else if (k + blksize >= matrixSize) {
                process.end = pivot;
            }

            // tuples
            for (int i = 0; i < matrixSize; i += blksize) {

                if(i != k) {
                    DFWPosition x = new DFWPosition(i, k);
                    DFWPosition y = new DFWPosition(k, i);

                    this.setPriorDependency(round, x);
                    this.setPriorDependency(round, y);

//                    if (k == 0) {
                        this.setDependency(round, pivot, x);
                        this.setDependency(round, pivot, y);
//                    } else {
//                        this.setDependency(round - 1, pivot, x);
//                        this.setDependency(round - 1 , pivot, y);
//                    }

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

        return process;
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
                }

            }

        }

        blk.round ++;

        return result;
    }

}
