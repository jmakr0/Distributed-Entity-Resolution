package de.hpi.partitioning;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.*;

import static java.util.Collections.max;
import static java.util.Collections.min;

public class MD5HashRouterTest {

    Md5HashRouter router;
    List<String> keyList;
    int loopCount = 3;
    private static Random rand = new Random(1);

    ActorSystem system;
    ActorRef nils;
    ActorRef max;
    ActorRef thorsten;
    ActorRef felix;
    ActorRef sebastian;

    @Before
    public void setUp() {
        // create a new HashRouter and a server keys
        router = new Md5HashRouter(100);

        keyList = new LinkedList<>();
        for (int i = 0; i < 100000; i++) {
            String randomString = getAlphaNumericString(4);
            keyList.add(randomString);
        }

        system = ActorSystem.create("system");
        nils = system.actorOf(DummyActor.props(), "nils");
        max = system.actorOf(DummyActor.props(), "max");
        thorsten = system.actorOf(DummyActor.props(), "thorsten");
        felix = system.actorOf(DummyActor.props(), "felix");
        sebastian = system.actorOf(DummyActor.props(), "sebastian");
    }

    @After
    public void after() {
        this.system.terminate();
    }

    @Test
    public void TestWithOnePartition() {

        router.putOnHashring(nils);

        for (String key: keyList) {
            ActorRef responsible = router.responsibleActor(key);
            for (int i = 0; i < loopCount; i++) {
                Assert.assertEquals(responsible, router.responsibleActor(key));
            }
        }

    }

    @Test
    public void TestWithTwoPartitions() {
        router.putOnHashring(nils);
        router.putOnHashring(max);

        for (String key: keyList) {
            ActorRef responsible = router.responsibleActor(key);

            for (int i = 0; i < loopCount; i++) {
                Assert.assertEquals(responsible, router.responsibleActor(key));
            }
        }

    }

    @Test
    public void TestWithMultiplePartitions() {
        router.putOnHashring(nils);
        router.putOnHashring(max);
        router.putOnHashring(thorsten);
        router.putOnHashring(felix);
        router.putOnHashring(sebastian);

        for (String key: keyList) {
            ActorRef responsible = router.responsibleActor(key);
            for (int i = 0; i < loopCount; i++) {
                Assert.assertEquals(responsible, router.responsibleActor(key));
            }
        }

    }

    @Test
    public void testDistribution() {
        router.putOnHashring(max);
        router.putOnHashring(nils);
        router.putOnHashring(thorsten);
        router.putOnHashring(felix);
        router.putOnHashring(sebastian);

        Map<ActorRef,Integer> mapping = new HashMap<>();

        for (String key: keyList) {
            ActorRef responsible = router.responsibleActor(key);
            if (!mapping.containsKey(responsible)) {
                mapping.put(responsible,1);
            } else {
                mapping.put(responsible, mapping.get(responsible) + 1);
            }
        }

        Integer minCount = min(mapping.values());
        Integer maxCount = max(mapping.values());

        double percentageThreshold = 0.02;

        // Test if the biggest partition is may 2% bigger than the smallest partition
        Assert.assertTrue(maxCount - minCount < percentageThreshold * maxCount);
    }

    private static String getAlphaNumericString(int n)
    {
        // length is bounded by 256 Character
        byte[] array = new byte[256];
        rand.nextBytes(array);

        String randomString = new String(array, Charset.forName("UTF-8"));

        // Create a StringBuffer to store the result
        StringBuffer r = new StringBuffer();

        // Append first 20 alphanumeric characters
        // from the generated random String into the result
        for (int k = 0; k < randomString.length(); k++) {

            char ch = randomString.charAt(k);

            if (((ch >= 'a' && ch <= 'z')
                    || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9'))
                    && (n > 0)) {

                r.append(ch);
                n--;
            }
        }

        // return the resultant string
        return r.toString();
    }
}
