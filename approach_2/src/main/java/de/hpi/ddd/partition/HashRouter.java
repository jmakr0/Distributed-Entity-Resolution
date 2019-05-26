package de.hpi.ddd.partition;

public abstract class HashRouter<T> {

    protected int numberOfBuckets;

    public HashRouter(int buckets) {
        this.numberOfBuckets = buckets;
    }

    public abstract T getObjectForKey(String key);

    public abstract void addNewObject(T object);
}
