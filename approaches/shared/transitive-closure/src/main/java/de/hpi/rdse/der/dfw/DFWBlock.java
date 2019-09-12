package de.hpi.rdse.der.dfw;

import de.hpi.rdse.der.util.Position;
import de.hpi.rdse.der.util.SubMatrix;

import java.util.LinkedList;
import java.util.List;

public class DFWBlock {

    private Position pivot;
    private SubMatrix target;
    private List<SubMatrix> subMatrices;

    // default constructor for serialization
    public DFWBlock() {}

    public DFWBlock(SubMatrix target, Position pivot, List<SubMatrix> subMatrices) {
        this.target = target;
        this.pivot = pivot;
        this.subMatrices = subMatrices;
    }

    public SubMatrix getTarget() {
        return target;
    }

    public void calculate() {
        int pivotX = this.pivot.getX();
        int targetX = target.getX();
        int targetY = target.getY();
        int size = target.getSubMatrixSize();

        for (int k = pivotX; k < size + pivotX; k++) {
            for (int x = targetX; x < targetX + size; x++) {
                for (int y = targetY; y < targetY + size; y++) {
                    if ((x != k) && (y != k)) {
                        int currentValue = target.getValue(x, y);
                        int value1 = this.getValue(x, k);
                        int value2 = this.getValue(k, y);
                        int newValue = value1 + value2;

                        // check int overflow
                        if (value1 == Integer.MAX_VALUE || value2 == Integer.MAX_VALUE) {
                            newValue = Integer.MAX_VALUE;
                        }

                        target.setValue(x, y, Math.min(currentValue, newValue));
                    }
                }
            }
        }
    }

    private SubMatrix getResponsibleMatrix(int x, int y) {
        List<SubMatrix> searchSpace = new LinkedList<>();
        searchSpace.add(this.target);
        searchSpace.addAll(this.subMatrices);

        for (SubMatrix sub : searchSpace) {
            if (sub.contains(x, y)) {
                return sub;
            }
        }

        // should never be reached
        assert false;
        return null;
    }

    private int getValue(int x, int y) {
        SubMatrix responsibleMatrix = getResponsibleMatrix(x, y);

        assert responsibleMatrix != null;
        return responsibleMatrix.getValue(x, y);
    }

}
