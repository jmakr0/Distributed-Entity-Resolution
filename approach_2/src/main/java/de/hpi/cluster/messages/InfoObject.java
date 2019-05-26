package de.hpi.cluster.messages;

import de.hpi.cluster.messages.interfaces.InfoObjectInterface;

public class InfoObject implements InfoObjectInterface {

    private String info;

    public InfoObject(String info) {
        this.info = info;
    }

    @Override
    public String infoString() {
        return info;
    }
}
