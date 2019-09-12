package de.hpi.cluster.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import de.hpi.cluster.messages.NameBlocking;
import de.hpi.rdse.der.partitioning.Md5HashRouter;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class PartitionCoordinator extends AbstractActor {

    public static final String DEFAULT_NAME = "partitioning-coordinator";

    public static Props props() {
        return Props.create(PartitionCoordinator.class);
    }

    private final LoggingAdapter log = Logging.getLogger(this.context().system(), this);

    @Data
    @AllArgsConstructor
    public static class ConfigMessage implements Serializable {
        private static final long serialVersionUID = 7354361868862425L;
        private ConfigMessage() {}
        protected Config config;
    }

    @Data
    @AllArgsConstructor
    public static class RegisterMessage implements Serializable {
        private static final long serialVersionUID = -737354361868862425L;
        private RegisterMessage() {}
        protected ActorRef worker;
    }

    @Data
    @AllArgsConstructor
    public static class TestMessage implements Serializable {
        private static final long serialVersionUID = -7373543618688625L;
    }

    private ActorRef master;
    private Set<ActorRef> worker = new HashSet<>();
    private Md5HashRouter router;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ConfigMessage.class, this::handle)
                .match(RegisterMessage.class, this::handle)
                .matchAny(object -> this.log.info("Received unknown message: \"{}\"", object.toString()))
                .build();
    }

    private void handle(ConfigMessage configMessage) {
        this.master = this.sender();

        Config config = configMessage.config;

        int numberOfBuckets = config.getInt("der.hash-router.number-of-buckets");
        this.router = new Md5HashRouter(numberOfBuckets);
    }

    private void handle(RegisterMessage registerMessage) {
        ActorRef worker = registerMessage.worker;

        this.addWorker(worker);

        this.router.putOnHashring(worker);

        this.log.info("Router version: " + this.router.getVersion());

        Md5HashRouter routerCopy = new Md5HashRouter(this.router);

        worker.tell(new Worker.RegisterAckMessage(
                        new NameBlocking(),
                        routerCopy),
                    this.master
        );
    }

    private void addWorker(ActorRef worker) {
        this.worker.add(worker);
    }
}
