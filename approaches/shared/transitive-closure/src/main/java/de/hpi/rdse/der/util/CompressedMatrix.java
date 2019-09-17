package de.hpi.rdse.der.util;

import java.util.Map;

public class CompressedMatrix {

    private int[][] matrix;

    private Map<Integer, Integer> compressionLookup;

    public CompressedMatrix(int[][] matrix, Map<Integer, Integer> compressionLookup) {
        this.matrix = matrix;
        this.compressionLookup = compressionLookup;
    }

    public int[][] getMatrix() {
        return matrix;
    }

    public Map<Integer, Integer> getCompressionLookup() {
        return compressionLookup;
    }

}
