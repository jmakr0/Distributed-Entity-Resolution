package de.hpi.cluster.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.remote.RemoteActorRef;
import de.hpi.cluster.actors.Worker.DataMessage;
import de.hpi.cluster.actors.listeners.ClusterListener;
import de.hpi.cluster.messages.Blocking;
import de.hpi.cluster.messages.interfaces.InfoObjectInterface;
import de.hpi.ddd.evaluation.ConsoleOutputEvaluator;
import de.hpi.ddd.evaluation.GoldStandardEvaluator;
import de.hpi.ddd.partition.HashRouter;
import de.hpi.ddd.partition.Md5HashRouter;
import de.hpi.utils.data.CSVService;
import de.hpi.utils.data.CSVService.ReadLineResult;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.*;
import java.util.*;

public class Master extends AbstractActor {

	////////////////////////
	// Actor Construction //
	////////////////////////
	
	public static final String DEFAULT_NAME = "master";

    public static Props props() {
		return Props.create(Master.class);
	}

	////////////////////
	// Actor Messages //
	////////////////////

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
    public static class ReadyForWorkMessage implements Serializable {
        private static final long serialVersionUID = -7643194361868862420L;
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

	/////////////////
	// Actor State //
	/////////////////
	
	private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final Queue<ActorRef> idleWorkers = new LinkedList<>();
    private final Set<ActorRef> knownWorkers = new HashSet<>();
    private final Set<ActorRef> unroutedWorkers = new HashSet<>();

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

    private HashRouter<ActorRef> router = new Md5HashRouter<ActorRef>(NUMBER_OF_BUCKETS);

    // DEBUG
    private int dataMessagesSEND = 0;
    private int dataMessagesACK = 0;

    ////////////////////
	// Actor Behavior //
	////////////////////

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
                .match(ReadyForWorkMessage.class, this::handle)
                .match(RepartitionFinishedMessage.class, this::handle)
                .match(DataAckMessage.class, this::handle)
                .match(Terminated.class, this::handle)
                .match(DuplicateMessage.class, this::handle)
                .match(ComparisonFinishedMessage.class, this::handle)
				.matchAny(object -> this.log.info("Received unknown message: \"{}\"", object.toString()))
				.build();
	}

    //	##############################################################
    //  Registration and Repartitioning
    //	##############################################################
    private void handle(ConfigMessage message) {
        this.csvService = new CSVService(message.dataPath);
        this.goldPath = message.goldPath;
    }

    //	##############################################################
    //  Registration and Repartitioning
    //	##############################################################
    private void handle(RegisterMessage registerMessage) {
        this.log.info("REGISTERED with Info: \"{}\"", registerMessage.info.infoString());
        ActorRef worker = this.sender();
        this.register(worker);
        worker.tell(new Worker.RegisterAckMessage(new Blocking(), SIMILARITY_THRESHOLD, NUMBER_INTERVAL_START, NUMBER_INTERVAL_END), this.self());
        this.markActorAsBusy(worker);
    }

    private void handle(ReadyForWorkMessage readyForWorkMessage) {
        markActorAsIdle(this.sender());
        if(allWorkersIdle()) {
            repartition();
        }
    }

    private void register(ActorRef worker) {
        this.idleWorkers.add(worker);
        this.knownWorkers.add(worker);
        this.unroutedWorkers.add(worker);
    }

    private void repartition() {
        this.repartitionRunning = true;
        for (ActorRef worker: this.unroutedWorkers) {
            this.router.addNewObject(worker);
        }
        this.unroutedWorkers.clear();
        for (ActorRef worker: this.knownWorkers) {
            this.log.info("PartitionBroadcastMessage to: \"{}\"", worker);
            worker.tell(new Worker.PartitionBroadcastMessage(this.router), this.self());
            this.markActorAsBusy(worker);
        }
    }

    private void handle(RepartitionFinishedMessage repartitionFinishedMessage) {
        this.markActorAsIdle(this.sender());
        this.log.info("Repartition finished by: \"{}\"", this.sender());
        if (allWorkersIdle()) {
            repartitionRunning = false;
            this.log.info("Repartition finishreed by ALL workers: " + ((int) this.idleWorkers.size() - (int) this.unroutedWorkers.size()));
            // if new workers registered while the repartitioning process was running -> repartition again
            if (!this.unroutedWorkers.isEmpty()) {
                this.repartition();
            }
            // if repartition is done we can assign new work for the workers
            else {
                this.assignWork();
            }
        }
    }

    // ##############################################################
    // Send Data
    // ##############################################################

    private void handle(DataAckMessage dataAckMessage) {
        dataMessagesACK++;
        this.log.info("DataMessage ACK: " + dataMessagesACK + " from " + this.sender());
        this.markActorAsIdle(this.sender());
        if (this.unroutedWorkers.isEmpty()) {
            this.assignWork();
        } else if (allWorkersIdle()){
            this.repartition();
        }

    }

    // ##############################################################
    // Comparison phase
    // ##############################################################
    private void handle(ComparisonFinishedMessage comparisonFinishedMessage) {
        this.markActorAsIdle(this.sender());

        if (this.allWorkersIdle()) {
            // evaluate results
            Set<Set<Integer>> goldStandard = CSVService.readRestaurantGoldStandard(this.goldPath, ",");
            GoldStandardEvaluator evaluator = new ConsoleOutputEvaluator();
            evaluator.evaluateAgainstGoldStandard(duplicates, goldStandard);
            this.log.info("Duplicates: \"{}\"", this.duplicates);
        }
    }

    private void handle(DuplicateMessage duplicateMessage) {
        this.duplicates.addAll(duplicateMessage.duplicates);
    }

    // ##############################################################
    // assign work logic
    // ##############################################################
    private void assignWork() {
        while (!this.idleWorkers.isEmpty() && !allDataRead) {
            // get next data block
            ReadLineResult readResult = this.csvService.readNextDataBlock(this.LINE_STEPS);
            String data = readResult.getData();
            if (!data.isEmpty()) {
                // if there is data to process form a DataMessage and send it to worker
                ActorRef worker = this.idleWorkers.poll();
                worker.tell(new DataMessage(readResult.getData()), this.self());
                dataMessagesSEND++;
                this.log.info("DataMessage send: \"{}\"", dataMessagesSEND);
            }
            allDataRead = readResult.foundEndOfFile();
            if (allDataRead) {
                this.log.info("############# ALL DATA READ ################");
            }
        }

        // if all Data is read and all workers are idle -> no data flow between workers -> ready for similarity phase
        if (allDataRead && allWorkersIdle()) {
            startSimilarityPhase();
        }
    }

    private void startSimilarityPhase() {
        for (ActorRef worker: this.idleWorkers) {
            worker.tell(new Worker.StartComparingMessage(), this.self());
        }
        this.idleWorkers.clear();

    }

    // ##############################################################
    // helper functions
    // ##############################################################

    private void markActorAsBusy(ActorRef actor) {
        this.idleWorkers.remove(actor);
    }

    private void markActorAsIdle(ActorRef actor) {
        this.idleWorkers.add(actor);
    }

    private boolean allWorkersIdle() {
        return this.idleWorkers.containsAll(this.knownWorkers);
    }

    // ##############################################################
    // all other methods -> TODO needs cleanup
    // ##############################################################

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

        for (ActorRef actor:this.idleWorkers) {
            actor.tell(PoisonPill.getInstance(), this.getSelf());
        }

        // todo REAPER check if the cluster listener can be reached by parent relation or similar
        ActorSelection actorSelection = this.context().system().actorSelection("user/" + ClusterListener.DEFAULT_NAME);
        actorSelection.tell(PoisonPill.getInstance(), this.getSelf());

        this.getSelf().tell(PoisonPill.getInstance(), this.getSelf());

    }

}