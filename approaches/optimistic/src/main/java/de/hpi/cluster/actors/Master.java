package de.hpi.cluster.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import de.hpi.cluster.actors.TCMaster.DispatchBlockMessage;
import de.hpi.rdse.der.data.GoldReader;
import de.hpi.rdse.der.dfw.DFWBlock;
import de.hpi.rdse.der.evaluation.ConsoleOutputEvaluator;
import de.hpi.rdse.der.evaluation.GoldStandardEvaluator;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class Master extends AbstractActor {

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class ConfigMessage implements Serializable {
        private static final long serialVersionUID = -7330958742629706627L;
        private ConfigMessage() {}
        private Config config;
    }

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class RegisterMessage implements Serializable {
        private static final long serialVersionUID = -7643194361868862425L;
    }

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class WorkRequestMessage implements Serializable {
        private static final long serialVersionUID = -7643194361868862420L;
    }

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class AllDataParsedMessage implements Serializable {
        private static final long serialVersionUID = -4739494771812333325L;
    }

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class DuplicateMessage implements Serializable {
        private static final long serialVersionUID = -1444194311112342425L;
        private DuplicateMessage() {}
        protected Set<Set<Integer>> duplicates;
    }

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class WorkerFinishedMatchingMessage implements Serializable {
        private static final long serialVersionUID = -1031194361812333325L;
    }

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class MatchingCompletedMessage implements Serializable {
        private static final long serialVersionUID = -1942194771812333325L;
        private MatchingCompletedMessage() {}
        private Set<Set<Integer>> duplicates;
        private Set<ActorRef> workers;
    }

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class DFWWorkFinishedMessage implements Serializable {
        private static final long serialVersionUID = -1991194311112342421L;
        private DFWWorkFinishedMessage() {}
        protected DFWBlock block;
    }

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class DFWDoneMessage implements Serializable {
        private static final long serialVersionUID = -1971194311112342421L;
        private DFWDoneMessage() {}
        Set<Set<Integer>> transitiveClosure;
        Queue<ActorRef> workers;
    }

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class WorkerGotParsedData implements Serializable {
        private static final long serialVersionUID = -1971194313192342421L;
    }

    public static final String DEFAULT_NAME = "master";

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private Config config;
    private Queue<ActorRef> registeredWorkers = new LinkedList<>();
    private boolean dataAvailable = true;
    private String goldPath;

    // Coordinators
    private ActorRef partitionCoordinator;
    private ActorRef tcMaster;
    private ActorRef matchingCoordinator;
    private ActorRef indexingCoordinator;

    public static Props props() {
        return Props.create(Master.class);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();

        Reaper.watchWithDefaultReaper(this);
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();

        for (ActorRef worker : this.registeredWorkers) {
            worker.tell(PoisonPill.getInstance(), this.self());
        }

        // Log the stop event
        this.log.info("Stopped {}.", this.getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ConfigMessage.class, this::handle)
                .match(RegisterMessage.class, this::handle)
                .match(WorkRequestMessage.class, this::handle)
                .match(AllDataParsedMessage.class, this::handle)
                .match(Terminated.class, this::handle)
                .match(DuplicateMessage.class, this::handle)
                .match(WorkerFinishedMatchingMessage.class, this::handle)
                .match(MatchingCompletedMessage.class, this::handle)
                .match(DFWWorkFinishedMessage.class, this::handle)
                .match(DFWDoneMessage.class, this::handle)
                .match(WorkerGotParsedData.class, this::handle)
                .matchAny(object -> this.log.info("Received unknown message: \"{}\"", object.toString()))
                .build();
    }

    private void handle(ConfigMessage message) {
        this.config = message.config;

        this.goldPath = this.config.getString("der.records.gold-standard.path");

        // create coordinator actors
        this.partitionCoordinator = context().actorOf(PartitionCoordinator.props(), PartitionCoordinator.DEFAULT_NAME);
        this.partitionCoordinator.tell(new PartitionCoordinator.ConfigMessage(config), this.self());

        this.indexingCoordinator = context().actorOf(IndexingCoordinator.props(), IndexingCoordinator.DEFAULT_NAME);
        this.indexingCoordinator.tell(new IndexingCoordinator.ConfigMessage(config), this.self());

        this.matchingCoordinator = context().actorOf(MatchingCoordinator.props(), MatchingCoordinator.DEFAULT_NAME);
        this.matchingCoordinator.tell(new MatchingCoordinator.ConfigMessage(config), this.self());

        this.tcMaster = context().actorOf(TCMaster.props(), TCMaster.DEFAULT_NAME);
        this.tcMaster.tell(new TCMaster.ConfigMessage(config), this.self());
    }

    private void handle(RegisterMessage registerMessage) {
        this.partitionCoordinator.tell(new PartitionCoordinator.RegisterMessage(this.sender()), this.self());

        if (!this.dataAvailable) {
            this.log.info("Register after records has been sent to the cluster");
        }
    }

    private void handle(WorkRequestMessage workRequestMessage) {
        ActorRef worker = this.sender();

        if(this.dataAvailable){
            this.sendData(worker);
        } else {
            this.sendSimilarity(worker);
        }
    }

    private void handle(AllDataParsedMessage allDataParsedMessage) {
        this.dataAvailable = false;
    }

    private void handle(DuplicateMessage duplicateMessage) {
        this.matchingCoordinator.tell(new MatchingCoordinator.DuplicateMessage(duplicateMessage.duplicates), this.sender());
    }

    private void handle(WorkerFinishedMatchingMessage workerFinishedMatchingMessage) {
        this.matchingCoordinator.tell(new MatchingCoordinator.WorkerFinishedMatchingMessage(), this.sender());
    }

    private void handle(MatchingCompletedMessage matchingCompletedMessage) {
        Set<ActorRef> workers = matchingCompletedMessage.workers;
        this.transitiveClosure(matchingCompletedMessage.duplicates, workers);
    }

    private void handle(WorkerGotParsedData workerGotParsedData) {
        ActorRef worker = this.sender();
        this.log.info("{} has new records in similarity phase", worker.path().name());

        this.sendSimilarity(worker);
        this.tcMaster.tell(new TCMaster.RestartMessage(), this.self());
    }

    private void sendData(ActorRef worker) {
        this.indexingCoordinator.tell(new IndexingCoordinator.SendDataMessage(worker), this.self());
    }

    private void sendSimilarity(ActorRef worker) {
        this.matchingCoordinator.tell(new MatchingCoordinator.StartSimilarityMessage(worker), this.self());
    }

    private void transitiveClosure(Set<Set<Integer>> duplicates, Set<ActorRef> workers) {
        tcMaster.tell(new TCMaster.CalculateMessage(duplicates, workers), this.self());
    }

    private void handle(DFWWorkFinishedMessage dfwWorkFinishedMessage) {
        DFWBlock block = dfwWorkFinishedMessage.block;

        this.tcMaster.tell(new DispatchBlockMessage(block), this.sender());
    }

    private void handle(DFWDoneMessage dfwDoneMessage) {
        Set<Set<Integer>> tk = dfwDoneMessage.transitiveClosure;
        logTransitiveClosure(tk);

        Set<Set<Integer>> goldStandard = GoldReader.readRestaurantGoldStandard(this.goldPath);
        GoldStandardEvaluator evaluator = new ConsoleOutputEvaluator();
        System.out.println("tk-distributed:" + tk);
        evaluator.evaluate(tk, goldStandard);

        this.registeredWorkers = dfwDoneMessage.workers;
        this.shutdown();
    }

    private void logTransitiveClosure(Set<Set<Integer>> tk) {
        StringBuilder sb = new StringBuilder();
        for (Set<Integer> duplicateClass: tk) {
            Object[] result = duplicateClass.toArray();
            Arrays.sort(result);
            sb.append(Arrays.toString(result));
            sb.append("\n");
        }

        this.log.info("TransitiveClosure: {}", sb.toString());
    }

    private void handle(Terminated message) {
        this.context().unwatch(message.getActor());

        this.log.info("Unregistered {}", message.getActor());
    }

    private void shutdown() {
        this.getSelf().tell(PoisonPill.getInstance(), this.getSelf());
    }

}