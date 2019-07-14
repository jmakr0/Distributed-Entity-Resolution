package de.hpi.der.similarity.string;

public interface StringComparator {

    /**
     * Compares two given Strings
     * @param s1
     * @param s2
     * @return a double between 0 and 1.
     * If the two Strings are equal the result is 1.
     * If the two Strings have nothing in common the result is 0.
     */
    double compare(String s1, String s2);

}
