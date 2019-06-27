package main;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DFW {

    private List<DFWBlock> work;
    private Set<DFWPosition> calculated;

    private SubMatrix pivot;

    private boolean hasData = false;
    private int[][] matrix;
    private int blksize;
    private int pivotsMax;
    private int pivotIndex;

    public DFW(int[][] matrix, int blksize) {
        double pivots =  (double) matrix.length / blksize;

        this.matrix = matrix;
        this.blksize = blksize;
        this.pivotIndex = 0;
        this.work = new LinkedList<>();
        this.calculated = new HashSet<>();

        this.pivotsMax = (int) Math.ceil(pivots);
        this.pivot = this.getNextPivot();

        this.work.add(new DFWBlock(this.pivot));
    }

    public List<SubMatrix> getWork() {

//        if(!this.work.isEmpty()) {
////            List<SubMatrix> tuple = this.work.remove(0);
//            this.hasData = this.work.isEmpty();
//
//            return tuple;
//        }
//
//        this.hasData = false;
        return null;
    }

    public void dispatch(SubMatrix block) {
        // todo: maybe apply buildern pattern

        // Pivot got calculated by worker
        if(this.pivot.equals(block)) {
            this.generateTuples();
        } else {
            this.merge(block);
            this.calculateTriple(block);
            this.calculated.add(block.getDFWPosition());
            this.nextRound();
        }

        //todo: check if done with pivotMax
    }

    private void nextRound() {
        if(this.hasData) {
            return;
        }

        this.pivotIndex++;
        this.pivot = this.getNextPivot();
//        this.work = this.generateTuples();
    }

    //todo refactor
    private void calculateTriple(SubMatrix block) {
//        SubMatrix posPivot = this.pivot;
//        SubMatrix posBlock = block;
//
//        if(posPivot.getX() == posBlock.getX()) {
//            int d = (int) Math.abs(posPivot.getY() - posBlock.getY());
//            Point posPivotTop = new Point((int) posPivot.getX(), (int) posPivot.getY() + d);
//            Point posPivotDown = new Point((int) posPivot.getX(), (int) posPivot.getY() - d);
//
//            // top
//            if(this.calculated.contains(posPivotTop)){
//                List<SubMatrix> triple = new LinkedList<>();
//                SubMatrix block2 = new SubMatrix(this.matrix, posPivotTop, this.blksize);
//                Point posBlock3 = new Point((int) posPivot.getX() - this.blksize , (int) posPivot.getY() - this.blksize);
//                SubMatrix block3 = new SubMatrix(this.matrix, posBlock3, this.blksize);
//
//                triple.add(block);
//                triple.add(block2);
//                triple.add(block3);
//
//                this.work.add(triple);
//            }
//
//            // down
//            if(this.calculated.contains(posPivotDown)){
//                List<SubMatrix> triple = new LinkedList<>();
//                SubMatrix block2 = new SubMatrix(this.matrix, posPivotDown, this.blksize);
//                Point posBlock3 = new Point((int) posPivot.getX() - this.blksize , (int) posPivot.getY() + this.blksize);
//                SubMatrix block3 = new SubMatrix(this.matrix, posBlock3, this.blksize);
//
//                triple.add(block);
//                triple.add(block2);
//                triple.add(block3);
//
//                this.work.add(triple);
//            }
//
//        } else {
//            int d = (int) Math.abs(posPivot.getX() - posBlock.getX());
//            Point posPivotLeft = new Point((int) posPivot.getX() - d, (int) posPivot.getY());
//            Point posPivotRight = new Point((int) posPivot.getX() + d, (int) posPivot.getY());
//
//            // left
//            if(this.calculated.contains(posPivotLeft)){
//                List<SubMatrix> triple = new LinkedList<>();
//                SubMatrix block2 = new SubMatrix(this.matrix, posPivotLeft, this.blksize);
//                Point posBlock3 = new Point((int) posPivot.getX() - this.blksize , (int) posPivot.getY() + this.blksize);
//                SubMatrix block3 = new SubMatrix(this.matrix, posBlock3, this.blksize);
//
//                triple.add(block);
//                triple.add(block2);
//                triple.add(block3);
//
//                this.work.add(triple);
//            }
//
//            // right
//            if(this.calculated.contains(posPivotRight)){
//                List<SubMatrix> triple = new LinkedList<>();
//                SubMatrix block2 = new SubMatrix(this.matrix, posPivotRight, this.blksize);
//                Point posBlock3 = new Point((int) posPivot.getX() + this.blksize , (int) posPivot.getY() + this.blksize);
//                SubMatrix block3 = new SubMatrix(this.matrix, posBlock3, this.blksize);
//
//                triple.add(block);
//                triple.add(block2);
//                triple.add(block3);
//
//                this.work.add(triple);
//            }
//        }
    }

    private void merge(SubMatrix block) {
        int x = block.getX();
        int y = block.getY();
        int [][] data = block.getMatrix();
        int size = data.length;

        for (int i = x; i < x + size; i++) {
            for (int j = y; j < y + size; j++) {
                this.matrix[i][j] = data[i][j];
            }
        }
    }

    private SubMatrix getNextPivot() {
        if(this.pivotIndex > this.pivotsMax) {
            throw new IndexOutOfBoundsException("Next pivot does not exist");
        }

        // note: quadratic matrix
        int x = this.blksize * this.pivotIndex;
        int y = x;

        this.pivotIndex++;

        return new SubMatrix(matrix, x, y, this.blksize);
    }

    private void generateTuples() {

        for (int i = 0; i < this.matrix.length; i+= this.blksize) {
            if (i != this.pivot.getX()) {
                SubMatrix target = new SubMatrix(this.matrix, i, this.pivot.getY(), this.blksize);
                DFWBlock block = new DFWBlock(target);
                block.addPath(this.pivot);

                this.work.add(block);
            }

            if (i != this.pivot.getY()) {
                SubMatrix target = new SubMatrix(matrix, this.pivot.getX(), i, blksize);
                DFWBlock block = new DFWBlock(target);
                block.addPath(this.pivot);

                this.work.add(block);
            }

        }

    }


//
//            // phase 2
//            List<SubMatrix> subMatrices = new LinkedList<>();
//
//            for (int i = 0; i < matrix.length; i+=blksize) {
//                if (i != x) {
//                    SubMatrix s = new SubMatrix(matrix, new Point(x, i), blksize);
//                    subMatrices.add(s);
//                }
//
//            }
//
//            for (int j = 0; j < matrix.length; j+=blksize) {
//                if (j != y) {
//                    SubMatrix s = new SubMatrix(matrix, new Point(j, y), blksize);
//                    subMatrices.add(s);
//                }
//            }
//
//
//
//        }


    }

//}
