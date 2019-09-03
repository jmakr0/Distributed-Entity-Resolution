package de.hpi.rdse.der.performance;

import akka.actor.ActorRef;

import java.util.HashMap;
import java.util.Map;

public class PerformanceTracker {

    private long timeThreshold;
    private int minWorkload;

    private Map<ActorRef, PerformanceMetric> performance;

    public PerformanceTracker(long timeThreshold, int minWorkload) {
        this.timeThreshold = timeThreshold;
        this.minWorkload = minWorkload;
        this.performance = new HashMap<>();
    }

    public int getNumberOfLines(ActorRef worker) {
        long now = System.currentTimeMillis();
        int newWorkload = this.minWorkload;

        if (this.performance.get(worker) == null) {
            PerformanceMetric pMetric = new PerformanceMetric();
            pMetric.setWorkLoad(this.minWorkload);
            pMetric.setWorkTime(now);
            this.performance.put(worker, pMetric);
        } else {
            PerformanceMetric pMetric = this.performance.get(worker);
            long timeDiff = now - pMetric.getWorkTime();
            int currentWorkload = pMetric.getWorkLoad();

            if (timeDiff < this.timeThreshold) {
                newWorkload = currentWorkload + 1;
            } else {
                newWorkload = Math.max(currentWorkload - 1, this.minWorkload);
            }
            pMetric.setWorkLoad(newWorkload);
            pMetric.setWorkTime(now);
        }

        return (int) Math.pow(2, newWorkload);
    }

}
