package de.hpi.ddd.partition;

public abstract class HashRouter<T> {
//    public abstract class HashRouter<T> implements Serializable {

//    private static final long serialVersionUID = -7641194361832342425L;

    protected int numberOfBuckets;

    public HashRouter(int buckets) {
        this.numberOfBuckets = buckets;
    }

    public abstract T getObjectForKey(String key);

    public abstract void addNewObject(T object);

    public abstract Integer getVersion();
}
