package main;

import java.awt.Point;
import java.util.*;

public class DFW {

    private List<List<SubMatrix>> work;
    private Set<Point> calculated;
    private boolean hasData = false;
    private int[][] matrix;
    private int blockSize;
    private int pivotIndex;
    private SubMatrix pivot;

    public DFW(int[][] matrix, int blockSize) {
        this.matrix = matrix;
        this.pivotIndex = 0;
        this.blockSize = blockSize;
        this.pivot = this.getPivot(this.pivotIndex);
        this.work = this.generateTuples(pivot);
        this.calculated = new HashSet<>();
    }

    public List<SubMatrix> getWork() {

        if(!this.work.isEmpty()) {
            List<SubMatrix> tuple = this.work.remove(0);
            this.hasData = this.work.isEmpty();

            return tuple;
        }

        this.hasData = false;
        return null;
    }

    public void receiveBlock(SubMatrix block) {
        this.calculated.add(block.getTopLeft());
        this.merge(block);
        this.calculateTriple(block);
        this.nextRound();
        //todo: check if done with pivotMax
    }

    private void nextRound() {
        if(this.hasData) {
            return;
        }

        this.pivotIndex++;
        this.pivot = this.getPivot(this.pivotIndex);
        this.work = this.generateTuples(this.pivot);
    }

    //todo refactor
    private void calculateTriple(SubMatrix block) {
        Point posPivot = this.pivot.getTopLeft();
        Point posBlock = block.getTopLeft();

        if(posPivot.getX() == posBlock.getX()) {
            int d = (int) Math.abs(posPivot.getY() - posBlock.getY());
            Point posPivotTop = new Point((int) posPivot.getX(), (int) posPivot.getY() + d);
            Point posPivotDown = new Point((int) posPivot.getX(), (int) posPivot.getY() - d);

            // top
            if(this.calculated.contains(posPivotTop)){
                List<SubMatrix> triple = new LinkedList<>();
                SubMatrix block2 = new SubMatrix(this.matrix, posPivotTop, this.blockSize);
                Point posBlock3 = new Point((int) posPivot.getX() - this.blockSize , (int) posPivot.getY() - this.blockSize);
                SubMatrix block3 = new SubMatrix(this.matrix, posBlock3, this.blockSize);

                triple.add(block);
                triple.add(block2);
                triple.add(block3);

                this.work.add(triple);
            }

            // down
            if(this.calculated.contains(posPivotDown)){
                List<SubMatrix> triple = new LinkedList<>();
                SubMatrix block2 = new SubMatrix(this.matrix, posPivotDown, this.blockSize);
                Point posBlock3 = new Point((int) posPivot.getX() - this.blockSize , (int) posPivot.getY() + this.blockSize);
                SubMatrix block3 = new SubMatrix(this.matrix, posBlock3, this.blockSize);

                triple.add(block);
                triple.add(block2);
                triple.add(block3);

                this.work.add(triple);
            }

        } else {
            int d = (int) Math.abs(posPivot.getX() - posBlock.getX());
            Point posPivotLeft = new Point((int) posPivot.getX() - d, (int) posPivot.getY());
            Point posPivotRight = new Point((int) posPivot.getX() + d, (int) posPivot.getY());

            // left
            if(this.calculated.contains(posPivotLeft)){
                List<SubMatrix> triple = new LinkedList<>();
                SubMatrix block2 = new SubMatrix(this.matrix, posPivotLeft, this.blockSize);
                Point posBlock3 = new Point((int) posPivot.getX() - this.blockSize , (int) posPivot.getY() + this.blockSize);
                SubMatrix block3 = new SubMatrix(this.matrix, posBlock3, this.blockSize);

                triple.add(block);
                triple.add(block2);
                triple.add(block3);

                this.work.add(triple);
            }

            // right
            if(this.calculated.contains(posPivotRight)){
                List<SubMatrix> triple = new LinkedList<>();
                SubMatrix block2 = new SubMatrix(this.matrix, posPivotRight, this.blockSize);
                Point posBlock3 = new Point((int) posPivot.getX() + this.blockSize , (int) posPivot.getY() + this.blockSize);
                SubMatrix block3 = new SubMatrix(this.matrix, posBlock3, this.blockSize);

                triple.add(block);
                triple.add(block2);
                triple.add(block3);

                this.work.add(triple);
            }
        }
    }

    private void merge(SubMatrix block) {
        int x = (int) block.getTopLeft().getX();
        int y = (int) block.getTopLeft().getY();
        int [][] data = block.getMatrix();
        int size = data.length;

        for (int i = x; i < x + size; i++) {
            for (int j = y; j < y + size; j++) {
                this.matrix[i][j] = data[i][j];
            }
        }
    }

    private SubMatrix getPivot(int index) {
        List<int[][]> pivots = new LinkedList<>();

        int x = blockSize * index, y = blockSize * index;

        return new SubMatrix(matrix, new Point(x, y), blockSize);
    }

    public List<List<SubMatrix>> generateTuples(SubMatrix pivot) {

        List<List<SubMatrix>> tuples = new LinkedList<>();
        for (int i = 0; i < matrix.length; i+=blockSize) {
            if (i != pivot.getTopLeft().getX()) {
                SubMatrix block = new SubMatrix(matrix, new Point(i, (int) pivot.getTopLeft().getY()), blockSize);

                List<SubMatrix> tuple = new LinkedList<>();
                tuple.add(pivot);
                tuple.add(block);

                tuples.add(tuple);
            }

            if (i != pivot.getTopLeft().getY()) {
                SubMatrix block = new SubMatrix(matrix, new Point((int) pivot.getTopLeft().getX(), i), blockSize);

                List<SubMatrix> tuple = new LinkedList<>();
                tuple.add(pivot);
                tuple.add(block);

                tuples.add(tuple);
            }

        }

        return tuples;
    }


//
//            // phase 2
//            List<SubMatrix> subMatrices = new LinkedList<>();
//
//            for (int i = 0; i < matrix.length; i+=blockSize) {
//                if (i != x) {
//                    SubMatrix s = new SubMatrix(matrix, new Point(x, i), blockSize);
//                    subMatrices.add(s);
//                }
//
//            }
//
//            for (int j = 0; j < matrix.length; j+=blockSize) {
//                if (j != y) {
//                    SubMatrix s = new SubMatrix(matrix, new Point(j, y), blockSize);
//                    subMatrices.add(s);
//                }
//            }
//
//
//
//        }


    }

//}
