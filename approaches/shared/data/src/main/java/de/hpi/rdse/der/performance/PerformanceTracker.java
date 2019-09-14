package de.hpi.rdse.der.performance;

import akka.actor.ActorRef;

import java.util.HashMap;
import java.util.Map;

public class PerformanceTracker {

    private long timeThreshold;
    private int minWorkload;
    private int maxWorkload;

    private Map<ActorRef, PerformanceMetric> performance;

    public PerformanceTracker(long timeThreshold, int minWorkload, int maxWorkload) {
        this.timeThreshold = timeThreshold;
        this.minWorkload = minWorkload;
        this.maxWorkload = maxWorkload;
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
                newWorkload = Math.min(currentWorkload + 1, this.maxWorkload);
            } else {
                newWorkload = Math.min(Math.max(currentWorkload - 1, this.minWorkload), this.maxWorkload);
            }
            pMetric.setWorkLoad(newWorkload);
            pMetric.setWorkTime(now);
        }

        return (int) Math.pow(2, newWorkload);
    }

}
