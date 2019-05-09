package Similarity.Comparator.Numbers;

public class SimpleNumberComparator implements NumberComparator {

    public double compare(double n1, double n2) {
        return Math.abs(n1-n2);
    }
}
