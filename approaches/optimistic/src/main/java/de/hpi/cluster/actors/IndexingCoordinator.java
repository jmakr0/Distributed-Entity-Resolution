package de.hpi.cluster.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import de.hpi.rdse.der.data.CSVService;
import de.hpi.rdse.der.performance.PerformanceTracker;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

public class IndexingCoordinator extends AbstractActor {

    @Data
    @AllArgsConstructor
    public static class ConfigMessage implements Serializable {
        private static final long serialVersionUID = -8298428742629706627L;
        private ConfigMessage() {}
        private Config config;
    }

    @Data
    @AllArgsConstructor
    public static class SendDataMessage implements Serializable {
        private static final long serialVersionUID = -4243198881482982195L;
        private SendDataMessage() {}
        protected ActorRef worker;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ConfigMessage.class, this::handle)
                .match(SendDataMessage.class, this::handle)
                .matchAny(object -> this.log.info("Received unknown message: \"{}\"", object.toString()))
                .build();
    }

    public static final String DEFAULT_NAME = "indexing-coordinator";

    private final LoggingAdapter log = Logging.getLogger(this.context().system(), this);

    private ActorRef master;
    private PerformanceTracker performanceTracker;
    private CSVService csvService;

    public static Props props() {
        return Props.create(IndexingCoordinator.class);
    }

    private void handle(ConfigMessage configMessage) {
        this.master = this.sender();

        Config config = configMessage.config;

        int timeThreshold = config.getInt("der.performance-tracker.time-threshold");
        int minWorkload = config.getInt("der.performance-tracker.min-workload");

        this.performanceTracker = new PerformanceTracker(timeThreshold, minWorkload);

        String data = config.getString("der.records.input.path");
        boolean hasHeader = config.getBoolean("der.records.input.has-header");
        char separator = config.getString("der.records.input.line-separator").charAt(0);
        int maxQueueSize = config.getInt("der.records.input.max-queue-size");

        this.csvService = new CSVService(data, hasHeader, separator, (int) Math.pow(2,minWorkload), maxQueueSize);
    }

    private void handle(SendDataMessage sendDataMessage) {
        ActorRef worker = sendDataMessage.worker;
        int numberOfLines = this.performanceTracker.getNumberOfLines(worker);
        this.log.info("numberOfLines: {}", numberOfLines);

        if (this.csvService.dataAvailable()) {
            String data = this.csvService.getRecords(numberOfLines);
            assert data != null;
            worker.tell(new Worker.DataMessage(data), this.master);
        } else {
            this.master.tell(new Master.AllDataParsedMessage(), this.self());
            this.master.tell(new Master.WorkRequestMessage(), worker);
        }

    }

}
