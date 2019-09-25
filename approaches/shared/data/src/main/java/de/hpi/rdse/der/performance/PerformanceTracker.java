package de.hpi.rdse.der.performance;

import akka.actor.ActorRef;

import java.util.HashMap;
import java.util.Map;

public class PerformanceTracker {

    private long timeThreshold;
    private int minWorkload;
    private int maxWorkload;

    private Map<ActorRef, PerformanceMetric> performance;

    /**
     * Initializes an object of type PerformanceTracker that can be used to determine the optimal number of workload
     * for a specific actor based on it's current performance.
     * workload corresponds to the number of lines as follows: <number of lines> = 2^<workload>
     * @param timeThreshold The time threshold in ms that should not be exceeded by the worker
     * @param minWorkload The minimal workload that should be assigned to an actor
     * @param maxWorkload The maximum workload that should be assigned to an actor
     */
    public PerformanceTracker(long timeThreshold, int minWorkload, int maxWorkload) {
        this.timeThreshold = timeThreshold;
        this.minWorkload = minWorkload;
        this.maxWorkload = maxWorkload;
        this.performance = new HashMap<>();
    }

    /**
     * Is called to determine the optimalmnumber of lines that should be send to a worker based on his current performance
     * @param worker The worker identified by its ActorRef
     * @return The optimal number of lines
     */
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
