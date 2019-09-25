package de.hpi.cluster.messages;

public class NameBlocking implements de.hpi.cluster.messages.interfaces.Blocking {
    @Override
    public String getKey(String[] record) {
        String value = record[1];
        int prefixLength = 5;
       return value.substring(0,Math.min(prefixLength, record[1].length() - 1));
    }
}
