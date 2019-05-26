package de.hpi.cluster.messages;

import de.hpi.cluster.messages.interfaces.BlockingInterface;

public class Blocking implements BlockingInterface {
    @Override
    public String getKey(String value) {
        int prefixLength = 4;
        if (value.length() >= prefixLength) {
            return value.substring(0,prefixLength);
        } else {
            return value.substring(0,value.length()-1);
        }
    }
}
