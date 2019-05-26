package de.hpi.ddd.partition;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;

import java.nio.charset.Charset;
import java.util.*;

import static com.google.common.hash.Hashing.consistentHash;
import static com.google.common.hash.Hashing.md5;

public class Md5HashRouter<T> extends HashRouter<T> {

    private HashFunction hashFunction;
    private Map<T, List<Integer>> mapping;
    Random r = new Random();

    public Md5HashRouter(int buckets) {
        super(buckets);
        this.hashFunction = md5();
        this.mapping = new HashMap<T, List<Integer>>();
    }

    public T getObjectForKey(String key) {
        HashCode hashCode = this.hashFunction.hashString(key, Charset.defaultCharset());
        int bucket = consistentHash(hashCode, numberOfBuckets);
        for (T obj: mapping.keySet()) {
            if(mapping.get(obj).contains(bucket)) {
                return obj;
            }
        }
        // This line should never be executed
        return null;
    }

    public void addNewObject(T object) {
        int numberOfObjects = mapping.keySet().size() + 1;
        List<Integer> newBucketList = new LinkedList<>();

        // if the object is the first that is added, we have to init the bucket list = [0,...,buckets-1]
        if (numberOfObjects == 1) {
            for (int i = 0; i < numberOfBuckets; i++) {
                newBucketList.add(i);
            }
        } else {
            int stealCount = numberOfBuckets / numberOfObjects;

            for (int i = 0; i < stealCount; i++) {
                T mostBuckets = mostBuckets();
                List<Integer> bucketList = mapping.get(mostBuckets);
                int freeBucket = bucketList.remove(0);
                newBucketList.add(freeBucket);
            }
        }
        mapping.put(object, newBucketList);
    }

    private T mostBuckets() {
        List<T> objects = new LinkedList<T>(mapping.keySet());
        Comparator<T> comparator = new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                Integer numberOfBuckets1 = mapping.get(o1).size();
                Integer numberOfBuckets2 = mapping.get(o2).size();
                return numberOfBuckets1.compareTo(numberOfBuckets2);
            }
        };

        objects.sort(comparator);
        return objects.get(objects.size()-1);
    }
}
