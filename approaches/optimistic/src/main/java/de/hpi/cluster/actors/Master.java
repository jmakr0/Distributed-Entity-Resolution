package de.hpi.cluster.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import de.hpi.cluster.actors.TCMaster.DispatchBlockMessage;
import de.hpi.cluster.actors.Worker.DataMessage;
import de.hpi.cluster.messages.NameBlocking;
import de.hpi.cluster.messages.interfaces.InfoObjectInterface;
import de.hpi.ddd.transitiveClosure.DFWBlock;
import de.hpi.ddd.transitiveClosure.TransitiveClosure;
import de.hpi.rdse.der.data.CSVService;
import de.hpi.rdse.der.data.GoldReader;
import de.hpi.rdse.der.evaluation.ConsoleOutputEvaluator;
import de.hpi.rdse.der.evaluation.GoldStandardEvaluator;
import de.hpi.rdse.der.partitioning.Md5HashRouter;
import de.hpi.rdse.der.performance.PerformanceTracker;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.typesafe.config.Config;

import java.io.Serializable;
import java.util.*;

public class Master extends AbstractActor {

    public static final String DEFAULT_NAME = "master";

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
    public static class WorkRequestMessage implements Serializable {
        private static final long serialVersionUID = -7643194361868862420L;
        private WorkRequestMessage() {}
        private int routerVersion;
    }

    @Data @AllArgsConstructor
    public static class DuplicateMessage implements Serializable {
        private static final long serialVersionUID = -1111194311112342425L;
        private DuplicateMessage() {}
        protected Set<Set<Integer>> duplicates;
    }

    @Data @AllArgsConstructor
    public static class ComparisonFinishedMessage implements Serializable {
        private static final long serialVersionUID = -1111194361812333325L;
    }

    @Data @AllArgsConstructor
    public static class ReadyDFWMessage implements Serializable {
        private static final long serialVersionUID = -1111194771812333325L;
    }

    @Data @AllArgsConstructor
    public static class IdleDFWMessage implements Serializable {
        private static final long serialVersionUID = -1111194771812333325L;
    }

    @Data @AllArgsConstructor
    public static class DFWWorkMessage implements Serializable {
        private static final long serialVersionUID = -1111194311112342421L;
        private DFWWorkMessage() {}
        protected DFWBlock block;
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
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private Config config;

    private Set<Set<Integer>> duplicates = new HashSet<Set<Integer>>();

    private Set<ActorRef> workers = new HashSet<>();
    // todo: see if we really need this list, currently we do
    private Set<ActorRef> registeredWorkers = new HashSet<>();
    private Queue<ActorRef> readyForDFWWork = new LinkedList<>();
    private Queue<DFWWorkMessage> pendingDFWWork = new LinkedList<>();

    private boolean repartitionRunning = false;

    private CSVService csvService;
    private String goldPath;
    private Md5HashRouter router;

    private ActorRef tcMaster;

    private PerformanceTracker performanceTracker;

    private int fwBlockSize;

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
                .match(Terminated.class, this::handle)
                .match(DuplicateMessage.class, this::handle)
                .match(ComparisonFinishedMessage.class, this::handle)
                .match(ReadyDFWMessage.class, this::handle)
                .match(IdleDFWMessage.class, this::handle)
                .match(DFWWorkMessage.class, this::handle)
                .match(DFWWorkFinishedMessage.class, this::handle)
                .match(DFWDoneMessage.class, this::handle)
                .matchAny(object -> this.log.info("Received unknown message: \"{}\"", object.toString()))
                .build();
    }

    private void handle(ConfigMessage message) {
        this.config = message.config;

        this.goldPath = this.config.getString("der.data.gold-standard.path");
        String data = this.config.getString("der.data.input.path");
        boolean hasHeader = this.config.getBoolean("der.data.input.has-header");
        // TODO get separator from config
        char separator = '\n';
        int minWorkload = this.config.getInt("der.performance-tracker.min-workload");
        int maxQueueSize = this.config.getInt("der.data.input.max-queue-size");

        this.csvService = new CSVService(data, hasHeader, separator, (int) Math.pow(2,minWorkload), maxQueueSize);

        int numberOfBuckets = this.config.getInt("der.hash-router.number-of-buckets");
        this.router = new Md5HashRouter(numberOfBuckets);

        int timeThreshold = this.config.getInt("der.performance-tracker.time-threshold");
        this.performanceTracker = new PerformanceTracker(timeThreshold, minWorkload);

        this.fwBlockSize = this.config.getInt("der.transitive-closure.block-size");
    }

    private void handle(RegisterMessage registerMessage) {
        this.log.info("REGISTERED with Info: \"{}\"", registerMessage.info.infoString());

        ActorRef worker = this.sender();

        this.addWorker(worker);

        this.router.putOnHashring(worker);

        this.log.info("Router version: " + this.router.getVersion());

        System.out.println("Master " + System.identityHashCode(this.router));

        Md5HashRouter routerCopy = new Md5HashRouter(this.router);

        double similarityThreshold = config.getDouble("der.duplicate-detection.similarity-threshold");
        int intervalStart = config.getInt("der.similarity.abs-comparator.interval-start");
        int intervalEnd = config.getInt("der.similarity.abs-comparator.interval-end");

        worker.tell(new Worker.RegisterAckMessage(
                        new NameBlocking(),
                        similarityThreshold,
                        intervalStart,
                        intervalEnd,
                        routerCopy),
                this.self()
        );
    }

    private void handle(WorkRequestMessage workRequestMessage) {
        ActorRef worker = this.sender();
        int masterVersion = this.router.getVersion();
        int workerVersion = workRequestMessage.routerVersion;

        this.log.info("Work request {}, master router {}, worker router {}", worker.path().name(), masterVersion, workerVersion);

        if (masterVersion > workerVersion) {
            this.sendRepartition(worker);
        } else if (masterVersion == workerVersion) {

            if(this.csvService.dataAvailable()){
                this.sendData(worker);
            } else {
                this.sendSimilarity(worker);
            }
        } else {
            this.log.error("Reached undefined state");
        }

    }

    private void sendRepartition(ActorRef worker) {
        this.log.info("Repartitioning message to {}", worker.path().name());

        Md5HashRouter routerCopy = new Md5HashRouter(this.router);

        worker.tell(new Worker.RepartitionMessage(routerCopy), this.self());
    }

    private void sendSimilarity(ActorRef worker) {
        this.log.info("Similarity message to {}", worker.path().name());

        worker.tell(new Worker.SimilarityMessage(), this.self());
    }

    private void sendData(ActorRef worker) {
        int numberOfLines = this.performanceTracker.getNumberOfLines(worker);
        this.log.info("numberOfLines: {}", numberOfLines);
        String data = this.csvService.getRecords(numberOfLines);

        DataMessage dataMessage = new DataMessage(data);
        worker.tell(dataMessage, this.self());
    }

    private void handle(ComparisonFinishedMessage comparisonFinishedMessage) {

        ActorRef worker = this.sender();
        this.workers.remove(worker);

        if (!this.readyForDFWWork.contains(worker)) {
            this.readyForDFWWork.add(worker);
        }

        if (this.workers.isEmpty()) {
            evaluateDuplicates();

            this.logTransitiveClosure(TransitiveClosure.calculate(this.duplicates));
//            this.log.info("start transitive closure");
            transitiveClosure();

            // todo move shutdown
//            this.log.info("All tasks finished, starting shutdown process.");
//            this.shutdown();
        }
    }

    private void transitiveClosure() {
        this.log.info("Calculate Transitive Closure");
        this.tcMaster = context().actorOf(TCMaster.props(), TCMaster.DEFAULT_NAME);

        tcMaster.tell(new TCMaster.CalculateMessage(this.duplicates, this.fwBlockSize), this.self());
    }

    private void evaluateDuplicates() {
        this.log.info("Duplicates: \"{}\"", this.duplicates);
        Set<Set<Integer>> goldStandard = GoldReader.readRestaurantGoldStandard(this.goldPath);
        GoldStandardEvaluator evaluator = new ConsoleOutputEvaluator();
        evaluator.evaluate(duplicates, goldStandard);
    }

    private void handle(DuplicateMessage duplicateMessage) {
        this.log.info("Duplicate {}", duplicateMessage.duplicates);

        this.duplicates.addAll(duplicateMessage.duplicates);
    }

    private void handle(IdleDFWMessage idleDFWMessage) {
        this.readyForDFWWork.add(this.sender());
    }

    private void handle(ReadyDFWMessage readyDFWMessage) {

        while (!this.readyForDFWWork.isEmpty()) {
            ActorRef worker = this.readyForDFWWork.poll();

//            this.tcMaster.tell(new TCMaster.DispatchBlockMessage(null), this.self());
            this.tcMaster.tell(new TCMaster.RequestWorkMessage(worker), this.self());
        }

    }

    private void handle(DFWWorkMessage dfwWorkMessage) {
        ActorRef worker = this.sender();

        worker.tell(new Worker.DFWWorkMessage(dfwWorkMessage.block), this.self());

//        this.pendingDFWWork.add(dfwWorkMessage);
//
//        this.keepWorkersBusy();
    }

    private void keepWorkersBusy() {
        while (!(this.readyForDFWWork.isEmpty() || this.pendingDFWWork.isEmpty())) {
            ActorRef worker = this.readyForDFWWork.poll();
            DFWWorkMessage work = this.pendingDFWWork.poll();

            worker.tell(new Worker.DFWWorkMessage(work.block), this.self());
        }

    }

    private void handle(DFWWorkFinishedMessage dfwWorkFinishedMessage) {
        DFWBlock block = dfwWorkFinishedMessage.block;

//        this.log.info("tell DispatchBlockMessage");

        this.tcMaster.tell(new DispatchBlockMessage(block), this.self());

        tcMaster.tell(new TCMaster.RequestWorkMessage(this.sender()), this.self());

//        this.readyForDFWWork.add(this.sender());
//
//        this.keepWorkersBusy();
    }

    private void handle(DFWDoneMessage dfwDoneMessage) {
        Set<Set<Integer>> tk = dfwDoneMessage.transitiveClosure;
        logTransitiveClosure(tk);
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

    private void addWorker(ActorRef actor) {
        this.workers.add(actor);
        this.registeredWorkers.add(actor);
    }

    private void shutdown() {
        this.getSelf().tell(PoisonPill.getInstance(), this.getSelf());
    }

}