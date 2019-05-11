package DuplicateDetection;

import Similarity.Comparator.UniversalComparator;

public abstract class AbstractDuplicateDetector implements DuplicateDetector{

    UniversalComparator comparator;
    double similarityThreshold;

    public AbstractDuplicateDetector(UniversalComparator comparator, double similarityThreshold) {
        this.comparator = comparator;
        this.similarityThreshold = similarityThreshold;
    }

}
