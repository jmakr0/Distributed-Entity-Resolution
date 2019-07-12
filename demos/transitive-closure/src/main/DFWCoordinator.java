package main;

import javafx.scene.input.KeyCode;

import java.util.*;

public class DFWCoordinator {

    private Queue<DFWPosition> pending;
    private Map<DFWPosition, Queue<List<DFWPosition>>> blocks;

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

    private Map<DFWPosition, Queue<List<DFWPosition>>> generateBlocks(int matrixSize, int blksize) {
        Map<DFWPosition, Queue<List<DFWPosition>>>  blocks = new HashMap<>();

        for (int x = 0; x < matrixSize; x += blksize) {
            for (int y = 0; y < matrixSize; y += blksize) {
                Queue<List<DFWPosition>> dependencies = new LinkedList<>();
                DFWPosition position = new DFWPosition(x, y);
                blocks.put(position, dependencies);
            }
        }

        return blocks;
    }

    private void addDependency(DFWPosition target, DFWPosition position) {
        List<DFWPosition> dependencies = new LinkedList<>();
        Queue<List<DFWPosition>> queue = new LinkedList<>(this.blocks.get(position));

        queue.forEach(set -> dependencies.addAll(set));

        dependencies.add(position);

        this.blocks.get(target).add(dependencies);
    }

//    private void addDependency(DFWPosition target, List<DFWPosition> positions) {
//        Set<DFWPosition> dependency = new HashSet<>();
//        positions.forEach(position -> dependency.add(position));
//
//        this.blocks.get(target).add(dependency);
//    }

//    private void addDependency(DFWPosition target, DFWPosition pos1, DFWPosition pos2) {
//        Set<DFWPosition> dependency = new HashSet<>();
//
//        Queue<Set<DFWPosition>> queue1 = new LinkedList<>(this.blocks.get(pos1));
//        Queue<Set<DFWPosition>> queue2 = new LinkedList<>(this.blocks.get(pos2));
//
//        queue1.forEach(set -> dependency.addAll(set));
//        queue2.forEach(set -> dependency.addAll(set));
//
//        dependency.add(pos1);
//        dependency.add(pos2);
//
//        this.blocks.get(target).add(dependency);
//    }

//    private void addDependency(DFWPosition target, DFWPosition pos1, DFWPosition pos2, DFWPosition pos3) {
//        Set<DFWPosition> dependency = new HashSet<>();
//        dependency.add(pos1);
//        dependency.add(pos2);
//        dependency.add(pos3);
//
//        this.blocks.get(target).add(dependency);
//    }

    private void generateDependencies(int matrixSize, int blksize) {
        List<DFWPosition> dependencies = new LinkedList<>();

        for (int k = 0; k < matrixSize; k += blksize) {

            DFWPosition pivot = new DFWPosition(k, k);

            dependencies.add(pivot);

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

                            addDependency(tripleX, pivot);
                            addDependency(tripleX, tupleX2);
                            addDependency(tripleX, tupleY1);
//                            addDependency(tripleX, tupleX2, tupleY1);

                            if (!tripleX.equals(tripleY)) {
                                addDependency(tripleY, pivot);
                                addDependency(tripleY, tupleY2);
                                addDependency(tripleY, tupleX1);
//                                addDependency(tripleY, tupleY2, tupleX1);
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
            List<DFWPosition> next = dependencies.element();

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
