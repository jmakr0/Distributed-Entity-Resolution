package de.hpi.rdse.der.partitioning;

import akka.actor.ActorRef;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.*;

import static com.google.common.hash.Hashing.consistentHash;
import static com.google.common.hash.Hashing.md5;

/**
 * This object is responsible to manage a consistent hashring.
 * A constant number of buckets is placed on the hashring and i mapped to a flexible number of actors
 * represented by a ActorRef
 * As a Hash function MD5 is used
 * For the hash calculation as well as for the consistent hashing itself we use the com.google.common.hash library
 */
public class Md5HashRouter implements Serializable {
    private static final long serialVersionUID = -7643424321861862995L;

    private Map<ActorRef, List<Integer>> mapping;
    // an index to speedup the responsibility lookup
    private Map<Integer, ActorRef> mappingIndex;
    private int numberOfBuckets;
    private int version;

    Random r = new Random();

    public Md5HashRouter() {
        // This default Constructor is needed for serialization
    }

    /**
     * Inits a new MD5HashRouter dividing the keyspace of the MD5 hashring
     * @param buckets the number of buckets on the hashring
     */
    public Md5HashRouter(int buckets) {
        this.numberOfBuckets = buckets;
        this.mapping = new HashMap<>();
    }

    /**
     * Puts a new Actor on the hashring so that it becomes responsible for a number of buckets
     * @param actorRef the actor
     */
    public void putOnHashring(ActorRef actorRef) {
        // with every new ActorRef that is added to the hashRing we increment the version
        this.version ++;

        int numberOfActors = mapping.keySet().size() + 1;
        List<Integer> newBucketList = new LinkedList<>();

        // if the actor is the first that is added, we have to init the bucket list = [0,...,buckets-1]
        if (numberOfActors == 1) {
            for (int i = 0; i < numberOfBuckets; i++) {
                newBucketList.add(i);
            }
        } else {
            // to reach a nearly equal distribution we first calculate the number of buckets each actor should hold
            int stealCount = numberOfBuckets / numberOfActors;
            // then we "steal" buckets from workers that hold ne highest number of buckets
            for (int i = 0; i < stealCount; i++) {
                ActorRef mostBuckets = mostBuckets();
                List<Integer> bucketList = mapping.get(mostBuckets);
                int freeBucket = bucketList.remove(0);
                newBucketList.add(freeBucket);
            }
        }
        mapping.put(actorRef, newBucketList);

        buildIndex();
    }

    /**
     * This method is used to query which of the registered Actors is responsible for a given Key
     * @param key the key
     * @return the actor that is responsible for the given key
     */
    public ActorRef responsibleActor(String key) {
        HashFunction hashFunc = md5();
        HashCode hashCode = hashFunc.hashString(key, Charset.defaultCharset());
        int bucket = consistentHash(hashCode, this.numberOfBuckets);
        return this.mappingIndex.get(bucket);
    }

    /**
     * getter for the version property
     * @return the current version of the Router
     */
    public Integer getVersion() {
        return version;
    }

    private ActorRef mostBuckets() {
        List<ActorRef> actors = new LinkedList<ActorRef>(mapping.keySet());
        Comparator<ActorRef> comparator = new Comparator<ActorRef>() {
            @Override
            public int compare(ActorRef a1, ActorRef a2) {
                Integer numberOfBuckets1 = mapping.get(a1).size();
                Integer numberOfBuckets2 = mapping.get(a2).size();
                return numberOfBuckets1.compareTo(numberOfBuckets2);
            }
        };

        actors.sort(comparator);
        return actors.get(actors.size()-1);
    }

    private void buildIndex() {
        this.mappingIndex = new HashMap<>();
        for (ActorRef actor: mapping.keySet()) {
            List<Integer> bucketList = mapping.get(actor);
            for (Integer bucket: bucketList) {
                this.mappingIndex.put(bucket, actor);
            }
        }
    }
}
