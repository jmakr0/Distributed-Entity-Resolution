package de.hpi.rdse.der.util;

import java.util.Map;

public class MappedMatrix {

    private int[][] matrix;
    private Map<Integer, Integer> mapping;
    private Map<Integer, Integer> reverseMapping;
    private int mappingCounter;

    public MappedMatrix(int[][] matrix) {
        this.matrix = matrix;
        this.mapping = mapping;
        this.mappingCounter = 0;
    }

    public void setValue(int x, int y, int value) {
        this.matrix[x][y] = value;
    }

    public int[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(int[][] matrix) {
        this.matrix = matrix;
    }

    public Map<Integer, Integer> getMapping() {
        return mapping;
    }

    public int getMappedIndexForId(Integer id) {
        if (mapping.keySet().contains(id)) {
            return mapping.get(id);
        } else {
            int mappedID = mappingCounter;
            this.mapping.put(id, mappedID);
            this.reverseMapping.put(mappedID, id);
            this.mappingCounter++;
            return mappedID;
        }
    }

    public int getIdForMappedId(Integer mappedId) {
        return this.reverseMapping.get(mappedId);
    }

}
