package de.hpi.rdse.der.setoperations;

import java.util.HashSet;
import java.util.Set;

public class SetOperations {

    /**
     * Calculates and returns the union of two given sets
     * @param s1 the first set
     * @param s2 the second set
     * @param <T> the type of values within the set
     * @return A new set that is the union of the two given sets
     */
    public static <T> Set<T> union(Set<T> s1, Set<T> s2) {
        Set<T> union = new HashSet<T>();

        union.addAll(s1);
        union.addAll(s2);

        return union;
    }

    /**
     * Calculates and returns the intersection of two given sets
     * @param s1 the first set
     * @param s2 the second set
     * @param <T> the type of values within the set
     * @return A new set that is the intersection of the two given sets
     */
    public static <T> Set<T> intersection(Set<T> s1, Set<T> s2) {
        Set<T> intersection = new HashSet<T>();

        intersection.addAll(s1);
        intersection.retainAll(s2);

        return intersection;
    }

    /**
     * Calculates and returns the set diff s1 \ s2
     * @param s1 the first set
     * @param s2 the second set
     * @param <T> the type of values within the set
     * @return A new set that is the set diff s1 \ s2
     */
    public static <T> Set<T> setDiff(Set<T> s1, Set<T> s2) {
        Set<T> setDiff = new HashSet<T>();

        setDiff.addAll(s1);
        setDiff.removeAll(s2);

        return setDiff;
    }
}
