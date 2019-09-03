package de.hpi.cluster.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import de.hpi.cluster.actors.Worker.DataMessage;
import de.hpi.cluster.messages.NameBlocking;
import de.hpi.cluster.messages.interfaces.InfoObjectInterface;
import de.hpi.rdse.der.data.CSVService;
import de.hpi.rdse.der.data.GoldReader;
import de.hpi.rdse.der.evaluation.ConsoleOutputEvaluator;
import de.hpi.rdse.der.evaluation.GoldStandardEvaluator;
import de.hpi.rdse.der.partitioning.Md5HashRouter;
import de.hpi.rdse.der.performance.PerformanceTracker;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Master extends AbstractActor {

	public static final String DEFAULT_NAME = "master";

    public static Props props() {
		return Props.create(Master.class);
	}

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class ConfigMessage implements Serializable {
        private static final long serialVersionUID = -7330958742629706627L;
        private ConfigMessage() {}
        private String dataPath;
        private String goldPath;
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

	private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final int NUMBER_OF_BUCKETS = 500;
    private final double SIMILARITY_THRESHOLD = 0.9;
    private final double NUMBER_INTERVAL_START = 5;
    private final double NUMBER_INTERVAL_END = 30;
    private final long TIME_THRESHOLD = 5000;
    private final int MIN_WORKLOAD = 1;

    private Set<Set<Integer>> duplicates = new HashSet<Set<Integer>>();

    private Set<ActorRef> workers = new HashSet<>();
    // todo: see if we really need this list, currently we do
    private Set<ActorRef> registeredWorkers = new HashSet<>();

    private boolean repartitionRunning = false;

    private CSVService csvService;
    private String goldPath;
    private Md5HashRouter router = new Md5HashRouter(NUMBER_OF_BUCKETS);

    private PerformanceTracker performanceTracker = new PerformanceTracker(TIME_THRESHOLD, MIN_WORKLOAD);

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
				.matchAny(object -> this.log.info("Received unknown message: \"{}\"", object.toString()))
				.build();
	}

    private void handle(ConfigMessage message) {
        this.csvService = new CSVService(message.dataPath, true, '\n', (int) Math.pow(2,MIN_WORKLOAD));
        this.goldPath = message.goldPath;
    }

    private void handle(RegisterMessage registerMessage) {
        this.log.info("REGISTERED with Info: \"{}\"", registerMessage.info.infoString());

        ActorRef worker = this.sender();

        this.addWorker(worker);

        this.router.putOnHashring(worker);

        this.log.info("Router version: " + this.router.getVersion());

        System.out.println("Master " + System.identityHashCode(this.router));

        Md5HashRouter routerCopy = new Md5HashRouter(this.router);
        
        worker.tell(new Worker.RegisterAckMessage(
                new NameBlocking(),
                SIMILARITY_THRESHOLD,
                NUMBER_INTERVAL_START,
                NUMBER_INTERVAL_END,
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

        this.workers.remove(this.sender());

        if (this.workers.isEmpty()) {
            // evaluate results
            Set<Set<Integer>> goldStandard = GoldReader.readRestaurantGoldStandard(this.goldPath);
            GoldStandardEvaluator evaluator = new ConsoleOutputEvaluator();
            evaluator.evaluate(duplicates, goldStandard);
            this.log.info("Duplicates: \"{}\"", this.duplicates);
            this.log.info("All tasks finished, starting shutdown process.");
            this.shutdown();
        }
    }


    private void handle(DuplicateMessage duplicateMessage) {
        this.log.info("Duplicate {}", duplicateMessage.duplicates);

        this.duplicates.addAll(duplicateMessage.duplicates);
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