package de.hpi.cluster.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.remote.RemoteActorRef;
import de.hpi.cluster.actors.listeners.ClusterListener;
import de.hpi.cluster.messages.NameBlocking;
import de.hpi.cluster.messages.interfaces.InfoObjectInterface;
import de.hpi.ddd.partition.Md5HashRouter;
import de.hpi.utils.data.CSVService;
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
    public static class RepartitionFinishedMessage implements Serializable {
        private static final long serialVersionUID = -7643194361812342425L;
    }

    @Data @AllArgsConstructor
    public static class DataAckMessage implements Serializable {
        private static final long serialVersionUID = -4243194361812342425L;
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

//    private final Queue<ActorRef> idleWorkers = new LinkedList<>();
//    private final Set<ActorRef> knownWorkers = new HashSet<>();
//    private final Set<ActorRef> unroutedWorkers = new HashSet<>();

    private final int LINE_STEPS = 2;
    private final int NUMBER_OF_BUCKETS = 100;
    private final double SIMILARITY_THRESHOLD = 0.9;
    private final double NUMBER_INTERVAL_START = 5;
    private final double NUMBER_INTERVAL_END = 30;


    private Boolean isWorking = false;
    private boolean allDataRead = false;

    private Set<Set<Integer>> duplicates = new HashSet<Set<Integer>>();

    private boolean repartitionRunning = false;

    private CSVService csvService;
    private String goldPath;

    private Md5HashRouter router = new Md5HashRouter(NUMBER_OF_BUCKETS);

    // DEBUG
    private int dataMessagesSEND = 0;
    private int dataMessagesACK = 0;


    @Override
    public void preStart() throws Exception {
        super.preStart();
        // Register at this actor system's reaper
//        Reaper.watchWithDefaultReaper(this);
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();

        // Log the stop event
        this.log.info("Stopped {}.", this.getSelf());
    }

	@Override
	public Receive createReceive() {
		return receiveBuilder()
                .match(ConfigMessage.class, this::handle)
                .match(RegisterMessage.class, this::handle)
                .match(WorkRequestMessage.class, this::handle)
                .match(RepartitionFinishedMessage.class, this::handle)
                .match(DataAckMessage.class, this::handle)
                .match(Terminated.class, this::handle)
                .match(DuplicateMessage.class, this::handle)
                .match(ComparisonFinishedMessage.class, this::handle)
				.matchAny(object -> this.log.info("Received unknown message: \"{}\"", object.toString()))
				.build();
	}

    private void handle(ConfigMessage message) {
        this.csvService = new CSVService(message.dataPath);
        this.goldPath = message.goldPath;
    }

    private void handle(RegisterMessage registerMessage) {
        this.log.info("REGISTERED with Info: \"{}\"", registerMessage.info.infoString());

        ActorRef worker = this.sender();

        this.router.addNewObject(worker);

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
            this.sendData(worker);
            // todo: similarity call here
        } else {
            this.log.error("Reached undefined state");
        }

    }

    private void sendRepartition(ActorRef worker) {
        this.log.info("Repartitioning message");

        Md5HashRouter routerCopy = new Md5HashRouter(this.router);

        worker.tell(new Worker.RepartitionMessage(routerCopy), this.self());
    }

    private void sendData(ActorRef worker) {
        // todo: adjust load to worker performance
        String data = this.csvService.readNextDataBlock(this.LINE_STEPS).getData();

        if(!data.isEmpty()) {
            worker.tell(new Worker.DataMessage(data), this.self());
        } else {
            this.log.info("done");
           // todo: similarity
        }

    }

    private void handle(RepartitionFinishedMessage repartitionFinishedMessage) {
//        this.markActorAsIdle(this.sender());
//        this.log.info("Repartition finished by: \"{}\"", this.sender());
//        if (allWorkersIdle()) {
//            repartitionRunning = false;
//            this.log.info("Repartition finishreed by ALL workers: " + ((int) this.idleWorkers.size() - (int) this.unroutedWorkers.size()));
//            // if new workers registered while the repartitioning process was running -> sendRepartition again
//            if (!this.unroutedWorkers.isEmpty()) {
//                this.sendRepartition();
//            }
//            // if sendRepartition is done we can assign new work for the workers
//            else {
//                this.assignWork();
//            }
//        }
    }

    private void handle(DataAckMessage dataAckMessage) {
//        dataMessagesACK++;
//        this.log.info("DataMessage ACK: " + dataMessagesACK + " from " + this.sender());
//        this.markActorAsIdle(this.sender());
//        if (this.unroutedWorkers.isEmpty()) {
//            this.assignWork();
//        } else if (allWorkersIdle()){
//            this.sendRepartition();
//        }

    }

    private void handle(ComparisonFinishedMessage comparisonFinishedMessage) {
//        this.markActorAsIdle(this.sender());
//
//        if (this.allWorkersIdle()) {
//            // evaluate results
//            Set<Set<Integer>> goldStandard = CSVService.readRestaurantGoldStandard(this.goldPath, ",");
//            GoldStandardEvaluator evaluator = new ConsoleOutputEvaluator();
//            evaluator.evaluateAgainstGoldStandard(duplicates, goldStandard);
//            this.log.info("Duplicates: \"{}\"", this.duplicates);
//        }
    }

    private void handle(DuplicateMessage duplicateMessage) {
        this.duplicates.addAll(duplicateMessage.duplicates);
    }

    private void assignWork() {
//        while (!this.idleWorkers.isEmpty() && !allDataRead) {
//            // get next data block
//            ReadLineResult readResult = this.csvService.readNextDataBlock(this.LINE_STEPS);
//            String data = readResult.getData();
//            if (!data.isEmpty()) {
//                // if there is data to process form a DataMessage and send it to worker
//                ActorRef worker = this.idleWorkers.poll();
//                worker.tell(new DataMessage(readResult.getData()), this.self());
//                dataMessagesSEND++;
//                this.log.info("DataMessage send: \"{}\"", dataMessagesSEND);
//            }
//            allDataRead = readResult.foundEndOfFile();
//            if (allDataRead) {
//                this.log.info("############# ALL DATA READ ################");
//            }
//        }
//
//        // if all Data is read and all workers are idle -> no data flow between workers -> ready for similarity phase
//        if (allDataRead && allWorkersIdle()) {
//            startSimilarityPhase();
//        }
    }

    private void startSimilarityPhase() {
//        for (ActorRef worker: this.idleWorkers) {
//            worker.tell(new Worker.StartComparingMessage(), this.self());
//        }
//        this.idleWorkers.clear();
    }

    private void markActorAsBusy(ActorRef actor) {
//        this.idleWorkers.remove(actor);
    }

    private void markActorAsIdle(ActorRef actor) {
//        this.idleWorkers.add(actor);
    }

//    private boolean allWorkersIdle() {
//        return this.idleWorkers.containsAll(this.knownWorkers);
//    }

    private void handle(Terminated message) {
		this.context().unwatch(message.getActor());
		
//		if (!this.idleWorkers.remove(message.getActor())) {
//			WorkMessage work = this.busyWorkers.remove(message.getActor());
//			if (work != null) {
//				this.register(work);
//			}
//		}
		this.log.info("Unregistered {}", message.getActor());
	}

	private void start(){
	    if(isWorking)
	        return;

        this.isWorking = true;
    }

    private void monitorSlaves(ActorRef sender) {
	    if(!(sender instanceof RemoteActorRef))
	        return;

	    String key = sender.path().address().toString();

//	    if(!this.slaves.contains(key)){
//	        this.slaves.add(key);
//	        this.waitSlavesCount -= 1;
//        }
    }

    private void terminateAll() {

//        for (ActorRef actor:this.busyWorkers.keySet()) {
//            actor.tell(PoisonPill.getInstance(), this.getSelf());
//        }

//        for (ActorRef actor:this.idleWorkers) {
//            actor.tell(PoisonPill.getInstance(), this.getSelf());
//        }

        // todo REAPER check if the cluster listener can be reached by parent relation or similar
        ActorSelection actorSelection = this.context().system().actorSelection("user/" + ClusterListener.DEFAULT_NAME);
        actorSelection.tell(PoisonPill.getInstance(), this.getSelf());

        this.getSelf().tell(PoisonPill.getInstance(), this.getSelf());

    }

}