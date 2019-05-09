package Util;

import java.util.HashSet;
import java.util.Set;

public class ConversionHelper {

    public static Set<String> convertStringArrayToStringSet(String[] stringArray) {
        // Create an empty Set
        Set<String> set = new HashSet<String>();

        // Iterate through the array
        for (String t : stringArray) {
            // Add each element into the set
            set.add(t);
        }

        // Return the converted Set
        return set;
    }
}
