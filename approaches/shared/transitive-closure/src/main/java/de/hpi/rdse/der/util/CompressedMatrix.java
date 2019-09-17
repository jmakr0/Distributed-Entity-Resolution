package de.hpi.rdse.der.util;

import java.util.Map;

public class CompressedMatrix {

    int[][] matrix;
    Map<Integer, Integer> compressionLookup;

    public CompressedMatrix(int[][] matrix, Map<Integer, Integer> compressionLookup) {
        this.matrix = matrix;
        this.compressionLookup = compressionLookup;
    }
}
