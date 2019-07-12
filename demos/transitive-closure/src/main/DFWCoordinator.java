package main;

import java.util.*;

public class DFWCoordinator {

    private Queue<DFWPosition> pending;
    private Map<DFWPosition, Queue<Set<DFWPosition>>> blocks;

    public DFWCoordinator(int matrixSize, int blksize) {
        this.pending = new LinkedList<>();
        this.blocks = this.generateBlocks(matrixSize, blksize);

        this.generateDependencies(matrixSize, blksize);
    }


    public void calculated(DFWPosition position) {
        List<DFWPosition> positions = getNextPositions(position);
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

    private void addDependency(DFWPosition target, DFWPosition position) {
        Set<DFWPosition> dependency = new HashSet<>();
        dependency.add(position);

        this.blocks.get(target).add(dependency);
    }

    private void addDependency(DFWPosition target, DFWPosition pos1, DFWPosition pos2) {
        Set<DFWPosition> dependency = new HashSet<>();
        dependency.add(pos1);
        dependency.add(pos2);

        this.blocks.get(target).add(dependency);
    }

    private void generateDependencies(int matrixSize, int blksize) {

        for (int k = 0; k < matrixSize; k += blksize) {

            DFWPosition pivot = new DFWPosition(k, k);

            // tuples
            for (int i = 0; i < matrixSize; i += blksize) {

                if(i != k) {
                    DFWPosition tupleX1 = new DFWPosition(i, k);
                    DFWPosition tupleY1 = new DFWPosition(k, i);

                    addDependency(tupleX1, pivot);
                    addDependency(tupleY1, pivot);

                    // triples
                    for (int j = 0; j < i + blksize; j += blksize) {

                        if (j != k) {
                            DFWPosition tupleX2 = new DFWPosition(j, k);
                            DFWPosition tupleY2 = new DFWPosition(k, j);

                            DFWPosition tripleX = new DFWPosition(j, i);
                            DFWPosition tripleY = new DFWPosition(i, j);

                            addDependency(tripleX, tupleX2, tupleY1);

                            if (!tripleX.equals(tripleY)) {
                                addDependency(tripleY, tupleY2, tupleX1);
                            }

                        }

                    }
                }

            }

        }

    }

    private List<DFWPosition> getNextPositions(DFWPosition position) {
        List<DFWPosition> result = new LinkedList<>();

        this.blocks.forEach((blk, dependencies) -> {
            Set<DFWPosition> next = dependencies.element();

            if (next.contains(position)) {
                next.remove(position);
            }

            if (next.isEmpty()) {
                result.add(blk);
                dependencies.remove();
            }
        });

        return result;
    }

}
