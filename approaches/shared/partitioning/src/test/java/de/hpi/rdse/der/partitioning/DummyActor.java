package de.hpi.rdse.der.partitioning;

import akka.actor.AbstractActor;
import akka.actor.Props;

import akka.event.Logging;
import akka.event.LoggingAdapter;

public class DummyActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props() {
        return Props.create(DummyActor.class, DummyActor::new);
    }

    @Override
    public void preStart() {
        log.info("IoT Application started");
    }

    @Override
    public void postStop() {
        log.info("IoT Application stopped");
    }

    // No need to handle any messages
    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }
}