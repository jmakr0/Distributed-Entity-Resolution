package main;

import java.util.*;

public class DFWCoordinator {

    class Block {
        DFWPosition position;
        Set<DFWPosition> dependsOn = new HashSet<>();
        Set<Block> next = new HashSet<>();

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
    private List<Block> blocks;

    public DFWCoordinator(int matrixSize, int blksize) {
        this.pending = new LinkedList<>();
        this.blocks = new LinkedList<>();

        Block start = this.generateDependencies(matrixSize, blksize);
        this.pending.add(start.position);
        this.blocks.add(start);
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

    private Map<DFWPosition, Queue<Set<DFWPosition>>> generateBlocks(int matrixSize, int blksize) {
        Map<DFWPosition, Queue<Set<DFWPosition>>>  blocks = new HashMap<>();

        for (int x = 0; x < matrixSize; x += blksize) {
            for (int y = 0; y < matrixSize; y += blksize) {
                Queue<Set<DFWPosition>> dependencies = new LinkedList<>();
                DFWPosition position = new DFWPosition(x, y);
                blocks.put(position, dependencies);
            }
        }

        return blocks;
    }

    private Block generateDependencies(int matrixSize, int blksize) {

        Block tail = new Block();
        tail.position = new DFWPosition(0, 0);

        Block pivot = tail;
        Block nextPivot = null;

        for (int k = 0; k < matrixSize; k += blksize) {

            DFWPosition pivotPosition = new DFWPosition(k, k);

            // tuples
            for (int i = 0; i < matrixSize; i += blksize) {

                if(i != k) {
                    Block x = new Block();
                    Block y = new Block();

                    x.position = new DFWPosition(i, k);
                    y.position = new DFWPosition(k, i);

                    x.dependsOn.add(pivotPosition);
                    y.dependsOn.add(pivotPosition);

                    pivot.next.add(x);
                    pivot.next.add(y);

                    // triples
                    for (int j = 0; j < matrixSize; j += blksize) {

                        if (j != k) {
                            Block xy = new Block();
                            Block yx = new Block();

                            DFWPosition posX =  new DFWPosition(k, j);
                            DFWPosition posY =  new DFWPosition(i, k);

                            xy.position = new DFWPosition(j, i);
                            yx.position = new DFWPosition(i, j);

                            xy.dependsOn.add(posX);
                            xy.dependsOn.add(posY);

                            // same position is next pivot
                            if (xy.position.equals(yx.position)) {
                                x.next.add(xy);
                                y.next.add(xy);

                                if (nextPivot == null && j > k) {
                                    nextPivot = xy;
                                }

                            } else {
                                yx.dependsOn.add(posX);
                                yx.dependsOn.add(posY);

                                x.next.add(yx);
                                y.next.add(xy);
                            }

                        }

                    }
                }

            }

            pivot = nextPivot;
            nextPivot = null;

        }

        return tail;

    }

    private Set<DFWPosition> getNextPositions(DFWPosition position) {
        Set<DFWPosition> result = new HashSet<>();
        Set<Block> nextBlocks = new HashSet<>();

        Iterator blkItr = this.blocks.iterator();

        while (blkItr.hasNext()) {
            Block blk = (Block)blkItr.next();

            Iterator nextItr = blk.next.iterator();

            while (nextItr.hasNext()) {
                Block nextBlk = (Block)nextItr.next();
                nextBlk.dependsOn.remove(position);

                if (nextBlk.dependsOn.isEmpty()) {
                    nextBlocks.add(nextBlk);
                    nextItr.remove();
                }
            }

            if (blk.next.isEmpty()) {
                blkItr.remove();
            }

        }

        this.blocks.addAll(nextBlocks);

        nextBlocks.forEach(block -> result.add(block.position));

        return result;
    }

}
