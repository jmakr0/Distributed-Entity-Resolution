package de.hpi.rdse.der.similarity;

import de.hpi.rdse.der.similarity.numeric.NumberComparator;
import de.hpi.rdse.der.similarity.string.StringComparator;

public class UniversalComparator {

    private StringComparator defaultStringComparator;
    private NumberComparator defaultNumberComparator;

    /**
     * Initializes Comparator
     * @param defaultStringComparator the comparator used for Strings
     * @param defaultNumberComparator the comparator used for numbers
     */
    public UniversalComparator(StringComparator defaultStringComparator, NumberComparator defaultNumberComparator) {
        this.defaultStringComparator = defaultStringComparator;
        this.defaultNumberComparator = defaultNumberComparator;
    }

    /**
     * Compares two given Arrays of Strings representing two different records
     * @param elem1 the first record
     * @param elem2 the second record
     * @return A value between 0 and 1 (1 for high similarity)
     */
    public double compare(String[] elem1, String[] elem2) {
        assert elem1.length == elem2.length;

        double accumulator = 0;

        for (int i = 0; i < elem1.length; i++) {
            accumulator += compare(elem1[i], elem2[i]);
        }
        return accumulator/elem1.length;
    }

    private double compare(String elem1, String elem2) {
        if (canBeParsedAsDouble(elem1) && canBeParsedAsDouble(elem2)) {
            double d1 = Double.valueOf(elem1);
            double d2 = Double.valueOf(elem2);
            return compareDoubles(d1,d2);
        } else {
            return compareStrings(elem1,elem2);
        }
    }

    private double compareDoubles(double elem1, double elem2) {
        return defaultNumberComparator.compare(elem1, elem2);
    }

    private double compareStrings(String elem1, String elem2) {
        return defaultStringComparator.compare(elem1,elem2);
    }

    private boolean canBeParsedAsDouble(String s) {
        try {
            Double.valueOf(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


}
