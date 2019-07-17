package main;

import java.util.*;

public class DFWCoordinator {

    class Block {
        int round = 0;
        DFWPosition position;
        Map<Integer, Set<Block>> dependsOn = new HashMap<>();
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

    private Queue<DFWPosition> pending;

    private Map<DFWPosition, Block> blocks;

    public DFWCoordinator(int matrixSize, int blksize) {
        this.pending = new LinkedList<>();
        this.blocks = this.generateBlocks(matrixSize, blksize);

        Block start = this.generateDependencies(matrixSize, blksize);
        this.pending.add(start.position);
        this.blocks.put(start.position, start);
    }


    public void calculated(DFWPosition position) {
        Set<DFWPosition> positions = getNextPositions(position);
        this.pending.addAll(positions);
    }

    public DFWPosition getNext() {
        if (!isDone()) {
            return this.pending.poll();
        }

        return null;
    }

    public boolean isDone() {
        return this.pending.isEmpty() && this.blocks.isEmpty();
    }

    private Map<DFWPosition, Block> generateBlocks(int matrixSize, int blksize) {
        Map<DFWPosition, Block>  blocks = new HashMap<>();

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

    private Block getBlock(DFWPosition pos) {
        return this.getBlock(pos.getX(), pos.getY());
    }

    private void setDependency(int round, DFWPosition from, DFWPosition to) {
        Block blkFrom = this.getBlock(from);
        Block blkTo = this.getBlock(to);

        if (!blkFrom.next.containsKey(round)) {
            blkFrom.next.put(round, new HashSet<>());
        }

        if (!blkTo.dependsOn.containsKey(round)) {
            blkTo.dependsOn.put(round, new HashSet<>());
        }

        blkFrom.next.get(round).add(blkTo);
        blkTo.dependsOn.get(round).add(blkFrom);
    }

    private Block generateDependencies(int matrixSize, int blksize) {

        Block tail = null;
        int round = 0;

        for (int k = 0; k < matrixSize; k += blksize) {

            DFWPosition pivot = new DFWPosition(k, k);

            if (tail == null) {
                tail = this.getBlock(pivot);
            }

            // tuples
            for (int i = 0; i < matrixSize; i += blksize) {

                if(i != k) {
                    DFWPosition x = new DFWPosition(i, k);
                    DFWPosition y = new DFWPosition(k, i);

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

                                this.setDependency(round, crossY, x1);
                                this.setDependency(round, crossX, y1);

                            }

                        }

                    }
                }

            }

            round ++;

        }

        return tail;

    }

    private Set<DFWPosition> getNextPositions(DFWPosition position) {
        Set<DFWPosition> result = new HashSet<>();
//        Set<Block> nextBlocks = new HashSet<>();
//
//        Iterator blkItr = this.blocks.iterator();
//
//        while (blkItr.hasNext()) {
//            Block blk = (Block)blkItr.next();
//
//            Iterator nextItr = blk.next.iterator();
//
//            while (nextItr.hasNext()) {
//                Block nextBlk = (Block)nextItr.next();
//                nextBlk.dependsOn.remove(position);
//
//                if (nextBlk.dependsOn.isEmpty()) {
//                    nextBlocks.add(nextBlk);
//                    nextItr.remove();
//                }
//            }
//
//            if (blk.next.isEmpty()) {
//                blkItr.remove();
//            }
//
//        }
//
//        this.blocks.addAll(nextBlocks);
//
//        nextBlocks.forEach(block -> result.add(block.position));

        return result;
    }

}
