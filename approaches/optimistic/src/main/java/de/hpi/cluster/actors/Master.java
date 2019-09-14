package de.hpi.cluster.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import de.hpi.cluster.actors.TCMaster.DispatchBlockMessage;
import de.hpi.cluster.messages.interfaces.InfoObjectInterface;
import de.hpi.rdse.der.data.GoldReader;
import de.hpi.rdse.der.dfw.DFWBlock;
import de.hpi.rdse.der.evaluation.ConsoleOutputEvaluator;
import de.hpi.rdse.der.evaluation.GoldStandardEvaluator;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

public class Master extends AbstractActor {

    public static final String DEFAULT_NAME = "master";
    private int routerVersion = 0;

    public static Props props() {
        return Props.create(Master.class);
    }

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class ConfigMessage implements Serializable {
        private static final long serialVersionUID = -7330958742629706627L;
        private ConfigMessage() {}
        private Config config;
    }

    @Data @AllArgsConstructor
    public static class RegisterMessage implements Serializable {
        private static final long serialVersionUID = -7643194361868862425L;
        private RegisterMessage() {}
        protected InfoObjectInterface info;
    }

    @Data @AllArgsConstructor
    public static class PartitionMessage implements Serializable {
        private static final long serialVersionUID = -7643124361894862420L;
        private PartitionMessage() {}
        protected int routerVersion;
    }

    @Data @AllArgsConstructor
    public static class WorkRequestMessage implements Serializable {
        private static final long serialVersionUID = -7643194361868862420L;
        private WorkRequestMessage() {}
        protected int routerVersion;
    }

    @Data @AllArgsConstructor
    public static class AllDataParsedMessage implements Serializable {
        private static final long serialVersionUID = -4739494771812333325L;
    }

    @Data @AllArgsConstructor
    public static class DuplicateMessage implements Serializable {
        private static final long serialVersionUID = -1444194311112342425L;
        private DuplicateMessage() {}
        protected Set<Set<Integer>> duplicates;
    }

    @Data @AllArgsConstructor
    public static class WorkerFinishedMatchingMessage implements Serializable {
        private static final long serialVersionUID = -1031194361812333325L;
    }

    @Data @AllArgsConstructor
    public static class MatchingCompletedMessage implements Serializable {
        private static final long serialVersionUID = -1942194771812333325L;
        private MatchingCompletedMessage() {}
        private Set<Set<Integer>> duplicates;
        private Set<ActorRef> workers;
    }

    @Data @AllArgsConstructor
    public static class DFWWorkFinishedMessage implements Serializable {
        private static final long serialVersionUID = -1991194311112342421L;
        private DFWWorkFinishedMessage() {}
        protected DFWBlock block;
    }

    @Data @AllArgsConstructor
    public static class DFWDoneMessage implements Serializable {
        private static final long serialVersionUID = -1971194311112342421L;
        private DFWDoneMessage() {}
        Set<Set<Integer>> transitiveClosure;
        Queue<ActorRef> workers;
    }

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
                .match(PartitionMessage.class, this::handle)
                .match(AllDataParsedMessage.class, this::handle)
                .match(Terminated.class, this::handle)
                .match(DuplicateMessage.class, this::handle)
                .match(WorkerFinishedMatchingMessage.class, this::handle)
                .match(MatchingCompletedMessage.class, this::handle)
                .match(DFWWorkFinishedMessage.class, this::handle)
                .match(DFWDoneMessage.class, this::handle)
                .matchAny(object -> this.log.info("Received unknown message: \"{}\"", object.toString()))
                .build();
    }

    private void handle(PartitionMessage partitionMessage) {
        this.routerVersion = partitionMessage.routerVersion;
    }

    private void handle(ConfigMessage message) {
        this.config = message.config;

        this.goldPath = this.config.getString("der.data.gold-standard.path");

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
        if (this.dataAvailable) {
            this.partitionCoordinator.tell(new PartitionCoordinator.RegisterMessage(this.sender()), this.self());
        } else {
            this.log.info("data is gone! please gooooo");
        }
    }

    private void handle(WorkRequestMessage workRequestMessage) {
        ActorRef worker = this.sender();
        int routerVersion = workRequestMessage.routerVersion;

        if (routerVersion < this.routerVersion) {
            this.log.info("Repartition noticed; new router version {}.", this.routerVersion);
            this.partitionCoordinator.tell(new PartitionCoordinator.RepartitionMessage(worker), this.self());
        } else if(this.dataAvailable){
            this.sendData(worker);
        } else {
            this.sendSimilarity(worker);
        }
    }

    private void handle(AllDataParsedMessage allDataParsedMessage) {
        this.dataAvailable = false;
    }

    private void sendData(ActorRef worker) {
        this.indexingCoordinator.tell(new IndexingCoordinator.SendDataMessage(worker), this.self());
    }

    private void sendSimilarity(ActorRef worker) {
        this.matchingCoordinator.tell(new MatchingCoordinator.StartSimilarityMessage(worker), this.self());
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

    private void transitiveClosure(Set<Set<Integer>> duplicates, Set<ActorRef> workers) {
        this.log.info("Calculate Transitive Closure");

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