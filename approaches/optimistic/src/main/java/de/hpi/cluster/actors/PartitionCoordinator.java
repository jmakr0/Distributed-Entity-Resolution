package de.hpi.cluster.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.serialization.Serialization;
import akka.serialization.SerializationExtension;
import akka.serialization.Serializer;
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
    public static class RepartitionMessage implements Serializable {
        private static final long serialVersionUID = -7373541236186862425L;
        private RepartitionMessage() {}
        protected ActorRef worker;
    }

    private ActorRef master;
    private Md5HashRouter router;
    private Serializer serializer;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ConfigMessage.class, this::handle)
                .match(RegisterMessage.class, this::handle)
                .match(RepartitionMessage.class, this::handle)
                .matchAny(object -> this.log.info("Received unknown message: \"{}\"", object.toString()))
                .build();
    }

    private void handle(RepartitionMessage repartitionMessage) {
        ActorRef worker = repartitionMessage.worker;

        byte[] serializedRouter = serializer.toBinary(this.router);

        worker.tell(new Worker.RepartitionMessage(serializedRouter), this.master);
    }

    private void handle(ConfigMessage configMessage) {
        this.master = this.sender();

        Config config = configMessage.config;

        int numberOfBuckets = config.getInt("der.hash-router.number-of-buckets");
        this.router = new Md5HashRouter(numberOfBuckets);

        // determine serializer

        ActorSystem system = context().system();
        Serialization serialization = SerializationExtension.get(system);
        this.serializer = serialization.findSerializerFor(Md5HashRouter.class);
    }

    private void handle(RegisterMessage registerMessage) {
        ActorRef worker = registerMessage.worker;

        this.router.putOnHashring(worker);

        this.log.info("Router version: " + this.router.getVersion());

        byte[] serializedRouter = serializer.toBinary(this.router);

        this.master.tell(new Master.PartitionMessage(this.router.getVersion()), this.master);

        worker.tell(new Worker.RegisterAckMessage(
                        new NameBlocking(),
                        serializedRouter),
                    this.master
        );
    }

}
