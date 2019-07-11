package main;

import java.util.*;

public class DFWCoordinator {

    class Block {
        DFWPosition position;
        Queue<Block> dependencies = new LinkedList<>();

        @Override
        public boolean equals(Object o) {
            return this.position.equals(o);
        }

        @Override
        public int hashCode() {
            return this.position.hashCode();
        }
    }

    private List<DFWPosition> pending;

    private List<Block> blocks;

    private Map<DFWPosition, > blocks2;

    public DFWCoordinator(int matrixSize, int blksize) {
        this.pending = new LinkedList<>();
        this.blocks = new LinkedList<>();

        this.blocks2 = this.generateBlocks(matrixSize, blksize);

        this.generateDependencies(matrixSize, blksize);
    }


    public void calculated(DFWPosition position) {
//        DFWPosition pivot = this.pending.get(position);

//        if (this.blocks.containsKey(pivot)) {
//            List<Block> blocks = this.blocks.get(pivot);
//
//            blocks.forEach(block -> {
//
//                if (block.dependencies.contains(position)) {
//                    block.dependencies.remove(position);
//                }
//
//                if (block.dependencies.size() == 0) {
//
//                    this.blocks.put(block.position, getDependencies(blocks, block));
//                    this.pending.add(block.position);
//
//                }
//            });
//        }

    }

    public DFWPosition getNext() {
//        if (!isDone()) {
//            return this.pending.poll();
//        }

        return null;
    }

    public boolean isDone() {
        return this.pending.isEmpty() && this.blocks.isEmpty();
    }

    private Set<Block> generateBlocks(int matrixSize, int blksize) {
        Set<Block> blocks = new HashSet<>();

        for (int x = 0; x < matrixSize; x += blksize) {
            for (int y = 0; y < matrixSize; y += blksize) {
                Block block = new Block();
                block.position = new DFWPosition(x, y);
                blocks.add(block);
            }
        }

        return blocks;
    }

    private void generateDependencies(int matrixSize, int blksize) {

        for (int k = 0; k < matrixSize; k += blksize) {

            DFWPosition pivot = new DFWPosition(k, k);

            Block block = this.blocks2.
//            List<Block> tuples = generateTuples(pivot, matrixSize, blksize);
//            List<Block> triples = generateTriples(k, matrixSize, blksize, tuples);
//
//            tuples.addAll(triples);
//
//            this.blocks.put(pivot, tuples);
        }

    }

    private List<Block> generateTuples(Block pivot, int matrixSize, int blksize){
        List<Block> tuples = new LinkedList<>();
        int k = pivot.position.getX();

        for (int i = 0; i < matrixSize; i += blksize) {

            if(i != k) {
                Block x = new Block();
                x.position = new DFWPosition(i, k);
                x.dependencies.add(pivot);

                Block y = new Block();
                y.position = new DFWPosition(k, i);
                y.dependencies.add(pivot);

                tuples.add(x);
                tuples.add(y);
            }

        }

        return tuples;
    }


    private List<Block> generateTriples(Block pivot, int matrixSize, int blksize, List<Block> tuples){
        List<Block> triples = new LinkedList<>();
        int k = pivot.position.getX();

        for (Block tuple: tuples) {

            for (int i = 0; i < matrixSize; i += blksize) {

                if (i != k) {
                    Block triple = new Block();
                    triple.dependencies.add(tuple);

//                    if (tuple.position.sameX(k)) {
//                        triple.dependencies.add(new DFWPosition(i, k));
//                        triple.position = new DFWPosition(i, tuple.position.getY());
//                    } else {
//                        triple.dependencies.add(new DFWPosition(k, i));
//                        triple.position = new DFWPosition(tuple.position.getX(), i);
//                    }

                    triples.add(triple);
                }

            }

        }

        return triples;
    }

    private List<Block> getDependencies(List<Block> blocks, Block block) {
        List<Block> result = new LinkedList<>();

        blocks.forEach(blk -> {
            if (blk.dependencies.contains(block.position)) {
                result.add(blk);
            }
        });

        return result;
    }

}
