package de.hpi.rdse.der.similarity.numeric;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class AbsComparator implements NumberComparator {

    double thresholdMin;
    double thresholdMax;

    /**
     * Initializes an abs based number comparator
     * @param thresholdMin The distance between two given numbers that is at least required so that similarity != 1.
     * @param thresholdMax The distance between two given numbers that is so high such that we set similarity = 0.
     *
     *                    e.g. thresholdMin = 5, thresholdMax = 10
     *                    compare(5,6) -> abs distance < 5 -> sim = 1
     *                    compare(5,20) -> abs distance > 10 -> sim = 0
     *                    compare(5,11) -> 5 <= abs distance = 6 <= 10  -> sim somewhere between 1 and 0
     *
     */
    public AbsComparator(double thresholdMin, double thresholdMax) {
        this.thresholdMin = thresholdMin;
        this.thresholdMax = thresholdMax;
    }

    /**
     * Initializes an abs based number comparator
     * @param config An Configuration object containing the keys: similarity.abs-comparator.threshold-min and similarity.abs-comparator.threshold-min
     */
    public AbsComparator(Config config) {
        this.thresholdMin = config.getDouble("similarity.abs-comparator.threshold-min");
        this.thresholdMax = config.getDouble("similarity.abs-comparator.threshold-min");
    }

    /**
     * Initializes an abs based number comparator using the default configuration
     */
    public AbsComparator() {
       this(ConfigFactory.load("default"));
    }

    public double compare(double n1, double n2) {
        double diff = Math.abs(n1-n2);

        if (diff < thresholdMin) {
            return 1;
        } else if (diff > thresholdMax) {
            return 0;
        }

        return scaleDiffToZeroOneInterval(diff);
    }

    private double scaleDiffToZeroOneInterval(double diff) {
        // Formula: output_start + ((output_end - output_start) / (input_end - input_start)) * (input - input_start)
        return 1 - (0 + ((1 - 0) / (thresholdMax - thresholdMin)) * (diff - thresholdMin));
    }
}
