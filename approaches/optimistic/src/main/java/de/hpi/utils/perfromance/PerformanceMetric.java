package de.hpi.utils.perfromance;

public class PerformanceMetric {

    private Long workTime;
    private int workLoad;

    public void setWorkTime(Long workTime) {
        this.workTime = workTime;
    }

    public void setWorkLoad(int workLoad) {
        this.workLoad = workLoad;
    }

    public Long getWorkTime() {
        return workTime;
    }

    public int getWorkLoad() {
        return workLoad;
    }
}
