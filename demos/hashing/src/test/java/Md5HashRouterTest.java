import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.*;

import static java.util.Collections.max;
import static java.util.Collections.min;

public class Md5HashRouterTest {

    HashRouter<String> router;
    List<String> keyList = new LinkedList<>();
    int loopCount = 3;
    private static Random rand = new Random(1);

    static String getAlphaNumericString(int n)
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

    @Before
    public void setUp() {
        router = new Md5HashRouter<String>(100);
        for (int i = 0; i < 100000; i++) {
            String randomString = getAlphaNumericString(4);
            keyList.add(randomString);
        }
    }

    @Test
    public void TestWithOnePartitions() {
        router.addNewObject("Nils");

        for (String key: keyList) {
            String responsible = router.getObjectForKey(key);
            for (int i = 0; i < loopCount; i++) {
                Assert.assertEquals(responsible, router.getObjectForKey(key));
            }
        }

    }

    @Test
    public void TestWithTwoPartitions() {
        router.addNewObject("Nils");
        router.addNewObject("Max");

        for (String key: keyList) {
            String responsible = router.getObjectForKey(key);

            for (int i = 0; i < loopCount; i++) {
                Assert.assertEquals(responsible, router.getObjectForKey(key));
            }
        }

    }

    @Test
    public void TestWithMultiplePartitions() {
        router.addNewObject("Max");
        router.addNewObject("Nils");
        router.addNewObject("Thorsten");
        router.addNewObject("Felix");
        router.addNewObject("Sebi");

        for (String key: keyList) {
            String responsible = router.getObjectForKey(key);
            for (int i = 0; i < loopCount; i++) {
                Assert.assertEquals(responsible, router.getObjectForKey(key));
            }
        }

    }

    @Test
    public void testDistribution() {
        router.addNewObject("Max");
        router.addNewObject("Nils");
        router.addNewObject("Thorsten");
        router.addNewObject("Felix");
        router.addNewObject("Sebi");

        Map<String,Integer> mapping = new HashMap<>();

        for (String key: keyList) {
            String responsible = router.getObjectForKey(key);
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
}
