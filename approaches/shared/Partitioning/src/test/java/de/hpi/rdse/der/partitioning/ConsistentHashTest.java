package de.hpi.rdse.der.partitioning;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.hash.Hashing.consistentHash;
import static com.google.common.hash.Hashing.md5;

public class ConsistentHashTest {

    List<Integer> blockingKeys;
    List<Integer> buckets;


    @Before
    public void before() {
        blockingKeys = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            blockingKeys.add(i);
        }
        buckets = new ArrayList<>();
    }

    @Test
    public void testBuckets() {
        // in this method we check if the method consistentHash really hashes consistent
        HashFunction hasFunc = md5();
        for (int i = 0; i < 4; i++) {
            for (Integer key: blockingKeys) {
                HashCode hashCode = hasFunc.hashString(key.toString(), Charset.defaultCharset());
                int bucket = consistentHash(hashCode, 100);
                buckets.add(bucket);
            }
        }
        for (int i = 0; i < blockingKeys.size(); i++) {
            // we used the consistentHash function in 4 rounds
            // if it works as expected the results should be the same in every round
            Assert.assertEquals(buckets.get(i), buckets.get(i));
            Assert.assertEquals(buckets.get(i), buckets.get(i + 50));
            Assert.assertEquals(buckets.get(i), buckets.get(i + 100));
            Assert.assertEquals(buckets.get(i), buckets.get(i + 150));
        }

    }
}
