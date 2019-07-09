package de.hpi.partitioning;

import akka.actor.ActorRef;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;

import java.nio.charset.Charset;
import java.util.*;

import static com.google.common.hash.Hashing.consistentHash;
import static com.google.common.hash.Hashing.md5;

public class Md5HashRouter {

    private Map<ActorRef, List<Integer>> mapping;
    private int numberOfBuckets;
    private int version;

    Random r = new Random();

    public Md5HashRouter(Md5HashRouter router) {
        this.mapping = new HashMap<ActorRef, List<Integer>>();

        for (ActorRef key:router.mapping.keySet()) {
            this.mapping.put(key, router.mapping.get(key));
        }

        this.numberOfBuckets = router.numberOfBuckets;
        this.version = router.version;
    }

    public Md5HashRouter() {

    }

    public Md5HashRouter(int buckets) {
        this.numberOfBuckets = buckets;
        this.mapping = new HashMap<>();
    }

    public ActorRef getObjectForKey(String key) {
        HashFunction hashFunc = md5();
        HashCode hashCode = hashFunc.hashString(key, Charset.defaultCharset());
        int bucket = consistentHash(hashCode, this.numberOfBuckets);
        for (ActorRef obj: mapping.keySet()) {
            if(mapping.get(obj).contains(bucket)) {
                return obj;
            }
        }
        // This line should never be executed
        return null;
    }

    public void addNewObject(ActorRef object) {
        int numberOfObjects = mapping.keySet().size() + 1;
        List<Integer> newBucketList = new LinkedList<>();
        this.version ++;

        // if the object is the first that is added, we have to init the bucket list = [0,...,buckets-1]
        if (numberOfObjects == 1) {
            for (int i = 0; i < numberOfBuckets; i++) {
                newBucketList.add(i);
            }
        } else {
            int stealCount = numberOfBuckets / numberOfObjects;

            for (int i = 0; i < stealCount; i++) {
                ActorRef mostBuckets = mostBuckets();
                List<Integer> bucketList = mapping.get(mostBuckets);
                int freeBucket = bucketList.remove(0);
                newBucketList.add(freeBucket);
            }
        }
        mapping.put(object, newBucketList);
    }

    private ActorRef mostBuckets() {
        List<ActorRef> objects = new LinkedList<ActorRef>(mapping.keySet());
        Comparator<ActorRef> comparator = new Comparator<ActorRef>() {
            @Override
            public int compare(ActorRef o1, ActorRef o2) {
                Integer numberOfBuckets1 = mapping.get(o1).size();
                Integer numberOfBuckets2 = mapping.get(o2).size();
                return numberOfBuckets1.compareTo(numberOfBuckets2);
            }
        };

        objects.sort(comparator);
        return objects.get(objects.size()-1);
    }

    public Integer getVersion() {
        return version;
    }
}
