package de.hpi.cluster.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MatchingCoordinator extends AbstractActor {

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class ConfigMessage implements Serializable {
        private static final long serialVersionUID = -7332428742629706627L;
        private ConfigMessage() {}
        private Config config;
    }

    @Data
    @AllArgsConstructor
    public static class StartSimilarityMessage implements Serializable {
        private static final long serialVersionUID = -4243198881468862195L;
        private StartSimilarityMessage() {}
        protected ActorRef worker;
    }

    @Data @AllArgsConstructor
    public static class DuplicateMessage implements Serializable {
        private static final long serialVersionUID = -1111194311112342425L;
        private DuplicateMessage() {}
        protected Set<Set<Integer>> duplicates;
    }

    @Data @AllArgsConstructor
    public static class WorkerFinishedMatchingMessage implements Serializable {
        private static final long serialVersionUID = -1111194361812333325L;
    }

    public static final String DEFAULT_NAME = "matching-coordinator";
    private final LoggingAdapter log = Logging.getLogger(this.context().system(), this);

    private Set<Set<Integer>> duplicates = new HashSet<>();
    private List<ActorRef> busyWorkers = new LinkedList<>();
    private Set<ActorRef> idleWorkers = new HashSet<>();
    private ActorRef master;
    private Config config;

    public static Props props() {
        return Props.create(MatchingCoordinator.class);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();

        Reaper.watchWithDefaultReaper(this);
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();

        // Log the stop event
        this.log.debug("Stopped {}.", this.getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ConfigMessage.class, this::handle)
                .match(StartSimilarityMessage.class, this::handle)
                .match(DuplicateMessage.class, this::handle)
                .match(WorkerFinishedMatchingMessage.class, this::handle)
                .matchAny(object -> this.log.debug("Received unknown message: \"{}\"", object.toString()))
                .build();
    }

    private void handle(ConfigMessage configMessage) {
        this.master = this.sender();
        this.config = configMessage.config;
    }

    private void handle(StartSimilarityMessage startSimilarityMessage) {
        ActorRef worker = startSimilarityMessage.worker;
        double similarityThreshold = this.config.getDouble("der.duplicate-detection.similarity-threshold");
        int thresholdMin = this.config.getInt("der.similarity.abs-comparator.threshold-min");
        int thresholdMax = this.config.getInt("der.similarity.abs-comparator.threshold-max");

        this.busyWorkers.add(worker);
        this.idleWorkers.remove(worker);

        this.log.debug("Similarity message to {}", worker.path().name());
        worker.tell(new Worker.SimilarityMessage(similarityThreshold, thresholdMin, thresholdMax), this.master);
    }

    private void handle(DuplicateMessage duplicateMessage) {
        this.duplicates.addAll(duplicateMessage.duplicates);
    }

    private void handle(WorkerFinishedMatchingMessage workerFinishedMatchingMessage) {
        ActorRef worker = this.sender();

        this.busyWorkers.remove(worker);

        if (!this.busyWorkers.contains(worker)) {
            this.idleWorkers.add(this.sender());
        }

        if (busyWorkers.isEmpty()) {
            this.log.info("Matching phase completed; duplicates found: {}", this.duplicates.size());
            this.master.tell(new Master.MatchingCompletedMessage(this.duplicates, this.idleWorkers), this.self());
        }
    }

}
