package Similarity.Comparator.Numbers;

public class ABSBasedNumberComparator implements NumberComparator {

    double intervalStart;
    double intervalEnd;

    /**
     * Initializes a abs based number comparator
     * @param intervalStart distance between two given numbers that is minimum required so that sim != 1
     * @param intervalEnd distance between two given numbers that is so high such that we set sim = 0
     *
     *                    e.g. intervalStart = 5, intervalEnd = 10
     *                    compare(5,6) -> abs distance < 5 -> sim = 1
     *                    compare(5,20) -> abs distance > 10 -> sim = 0
     *                    compare(5,11) -> 5 <= abs distance = 6 <= 10  -> sim somewhere between 1 and 0
     *
     */
    public ABSBasedNumberComparator(double intervalStart, double intervalEnd) {
        this.intervalStart = intervalStart;
        this.intervalEnd = intervalEnd;
    }

    public double compare(double n1, double n2) {
        double diff = Math.abs(n1-n2);

        if (diff < intervalStart) {
            return 1;
        } else if (diff > intervalEnd) {
            return 0;
        }

        return scaleDiffToZeroOneInterval(diff);
    }

    private double scaleDiffToZeroOneInterval(double diff) {
        // Formular: output_start + ((output_end - output_start) / (input_end - input_start)) * (input - input_start)
        return 1 - (0 + ((1 - 0) / (intervalEnd - intervalStart)) * (diff - intervalStart));
    }
}
