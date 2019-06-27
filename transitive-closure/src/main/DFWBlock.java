package main;

import java.util.LinkedList;
import java.util.List;

public class DFWBlock {
    private DFWPosition pivot;
    private SubMatrix target;
    private List<SubMatrix> paths;

    public DFWBlock(SubMatrix target, DFWPosition pivot) {
        this.target = target;
        this.pivot = pivot;
        this.paths = new LinkedList<>();
    }

    // TODO: remove this constructor!
    public DFWBlock(SubMatrix target) {
        this.target = target;
        this.paths = new LinkedList<>();
    }

    public SubMatrix getTarget() {
        return target;
    }

    public List<SubMatrix> getPaths() {
        return paths;
    }

    public void addPath(SubMatrix path){
        this.paths.add(path);
    }

    public void calculate() {

        int targetX = (int) target.getX();
        int targetY = (int) target.getY();

        int size = target.getMatrix().length;

        for (int k = this.pivot.getX(); k < size + this.pivot.getX(); k++) {
            for (int x = targetX; x < targetX + size; x++) {
                for (int y = targetY; y < targetY + size; y++) {
                    if ((x != k) && (y != k)) {
                        int currentValue = target.getMatrixValue(x, y);
                        // we do not know which of submatrix in "paths" has the needed value/index
                        int one = this.getValues(x, k);
                        int two = this.getValues(k, y);
                        int newPath = one + two;
//                        // check int overflow !!
                        if (one == Integer.MAX_VALUE || two == Integer.MAX_VALUE) {
                            newPath = Integer.MAX_VALUE;
                        }
                        int update = Math.min(currentValue, newPath);
                        target.setMatrixValue(x, y, update);
                    }
                }
            }
        }
    }

    private int getValues(int x, int y) {
        SubMatrix responsibleMatrix = getResponsibleMatrix(x, y);
        return responsibleMatrix.getMatrixValue(x, y);
    }

    private SubMatrix getResponsibleMatrix(int x, int y) {
        List<SubMatrix> searchSpace = new LinkedList<>();
        searchSpace.add(this.target);
        searchSpace.addAll(this.paths);

        for (int i = 0; i < searchSpace.size(); i++) {
            SubMatrix sub = searchSpace.get(i);
            if (sub.contains(x,y)) {
                return sub;
            }
        }

        // should never be reached
        assert false;
        return null;
    }
}
