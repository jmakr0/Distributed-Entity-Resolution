package de.hpi.partitioning;

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

// TODO clean up this test
public class HashRouterTest {

    List<Integer> blockingKeys;
    List<Integer> buckets;


    @Before
    public void initComparator() {
        blockingKeys = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            blockingKeys.add(i);
        }
        buckets = new ArrayList<>();
    }

    @Test
    public void testBuckets() {
        HashFunction hasFunc = md5();
        for (int i = 0; i < 4; i++) {
            List<Integer> tmpBuckets = new ArrayList<>();
            for (Integer key: blockingKeys) {
                HashCode hashCode = hasFunc.hashString(key.toString(), Charset.defaultCharset());
                int bucket = consistentHash(hashCode, 100);
                buckets.add(bucket);
            }
        }
        for (int i = 0; i < blockingKeys.size(); i++) {
            Assert.assertEquals(buckets.get(i), buckets.get(i + 50));
            Assert.assertEquals(buckets.get(i), buckets.get(i + 100));
            Assert.assertEquals(buckets.get(i), buckets.get(i + 150));
        }

    }
}
