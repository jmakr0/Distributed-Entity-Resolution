package de.hpi.ddd.similarity.numeric;

public interface NumberComparator {

    /**
     * Compares two given doubles
     * @param d1 first double
     * @param d2 second double
     * @return A double between 0 and 1 indicating how similar the two numbers are
     */
    double compare(double d1, double d2);
}
