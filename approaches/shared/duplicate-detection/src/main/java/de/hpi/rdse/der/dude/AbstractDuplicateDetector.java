package de.hpi.rdse.der.dude;

import de.hpi.rdse.der.similarity.UniversalComparator;

public abstract class AbstractDuplicateDetector implements DuplicateDetector {

    UniversalComparator comparator;
    double similarityThreshold;

    public AbstractDuplicateDetector(UniversalComparator comparator, double similarityThreshold) {
        this.comparator = comparator;
        this.similarityThreshold = similarityThreshold;
    }

}
