package de.hpi.cluster.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.remote.RemoteActorRef;
import de.hpi.cluster.actors.Worker.WorkMessage;
import de.hpi.cluster.actors.listeners.ClusterListener;
import de.hpi.utils.Results;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

public class Profiler extends AbstractActor {

	////////////////////////
	// Actor Construction //
	////////////////////////
	
	public static final String DEFAULT_NAME = "profiler";

    public static Props props() {
		return Props.create(Profiler.class);
	}

	////////////////////
	// Actor Messages //
	////////////////////
	
	@Data @AllArgsConstructor
	public static class RegistrationMessage implements Serializable {
		private static final long serialVersionUID = 4545299661052078209L;
	}

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class ConfigMessage implements Serializable {
        private static final long serialVersionUID = -7330958742629706627L;
        private ConfigMessage() {}
        private int slavesCount;
        private Results results;
    }

	@Data @AllArgsConstructor @SuppressWarnings("unused")
	public static class TaskMessage implements Serializable {
		private static final long serialVersionUID = -8330958742629706627L;
		private TaskMessage() {}
		protected int id;
	}

	@Data @AllArgsConstructor @SuppressWarnings("unused")
	public static class CompletionRainbowTaskMessage implements Serializable {
		private static final long serialVersionUID = -6823011111281387872L;
		private CompletionRainbowTaskMessage() {}
        private Map<String,Integer> hashes;
	}

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class CheckedLinearCombinationIntervalMessage implements Serializable {
        private static final long serialVersionUID = -7123011111281387872L;
        private CheckedLinearCombinationIntervalMessage() {}
        private int[] linearCombination;
    }

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class GenePartnerFoundMessage implements Serializable {
        private static final long serialVersionUID = -7123011111281381111L;
        private GenePartnerFoundMessage() {}
        // todo check if searchedFor is needed
        private int searchedFor;
        private int foundPartner;
    }

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class HashWithPrefixFoundMessage implements Serializable {
        private static final long serialVersionUID = -7123012345281381111L;
        private HashWithPrefixFoundMessage() {}
        private int personID;
        private String hash;
    }

	/////////////////
	// Actor State //
	/////////////////
	
	private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private final Queue<WorkMessage> unassignedWork = new LinkedList<>();
	private final Queue<ActorRef> idleWorkers = new LinkedList<>();
	private final Map<ActorRef, WorkMessage> busyWorkers = new HashMap<>();

    private final HashSet<String> slaves = new HashSet<>();
    private int waitSlavesCount = -1;
    private Boolean isWorking = false;

    private final int MAX_PASSWORD = 1000000;

    private long currentStartIndex = 0;
    private long currentStopIndex = 0;
    private final long LINEAR_COMBINATION_INTERVAL_SIZE = 1000000;
    private int[] allPasswords;

    private boolean prefixFound = false;

    private Results results;
    private Map<String,Integer> rainbowTable = new HashMap<>();

    private List<String> geneSequences;
    private int numberOfUsers;
    private int currentGeneSequenceUser;

    private List<Integer> prefixes;
    private List<Integer> partners;
    private int currentHashPrefixUser;

    private long startTime;



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
                .match(RegistrationMessage.class, this::handle)
				.match(Terminated.class, this::handle)
                .match(ConfigMessage.class, this::handle)
				.match(CompletionRainbowTaskMessage.class, this::handle)
                .match(CheckedLinearCombinationIntervalMessage.class, this::handle)
                .match(GenePartnerFoundMessage.class, this::handle)
                .match(HashWithPrefixFoundMessage.class, this::handle)
				.matchAny(object -> this.log.info("Received unknown message: \"{}\"", object.toString()))
				.build();
	}

    private void handle(RegistrationMessage message) {
		this.context().watch(this.sender());
		
		this.assign(this.sender());
		this.log.info("Registered {}", this.sender());

        this.monitorSlaves(this.sender());
	    if(this.waitSlavesCount == 0)
	        this.start();
	}

    private void handle(Terminated message) {
		this.context().unwatch(message.getActor());
		
		if (!this.idleWorkers.remove(message.getActor())) {
			WorkMessage work = this.busyWorkers.remove(message.getActor());
			if (work != null) {
				this.assign(work);
			}
		}		
		this.log.info("Unregistered {}", message.getActor());
	}

    private void handle(ConfigMessage message) {
        this.log.info("Received slaves count: {}", message.slavesCount);
	    this.waitSlavesCount = message.slavesCount;
	    this.results = message.results;
    }

	private void start(){
	    if(isWorking)
	        return;

        this.isWorking = true;

        this.startTime = System.currentTimeMillis();

        calculateRainbowTable();
    }

    private void calculateRainbowTable() {
	    int workers = this.idleWorkers.size();
//        int step = maxPassword/workers;
        int step = 2500;

        for (int i = 0; i <=  MAX_PASSWORD - step; i+=step) {
            this.log.info("Calculate rainbow table for: [{},{}]", i, i+step);
            this.assign(new Worker.RainbowWorkMessage(i, i+step));
        }

        // todo: last worker, missing interval?
    }

    private void handle(CompletionRainbowTaskMessage message) {
		ActorRef worker = this.sender();
		Worker.RainbowWorkMessage work = (Worker.RainbowWorkMessage) this.busyWorkers.remove(worker);

        this.log.info("Completed Rainbow table for: [{},{}]", work.getMin(), work.getMax());

        this.rainbowTable.putAll(message.hashes);

        this.assign(worker);

        if(isTableComlpete(rainbowTable)) {
            this.results.setPasswords(rainbowTable);

            startSearchingforLinearCombinations();
        }
    }

    private void handle(CheckedLinearCombinationIntervalMessage message) {
        ActorRef worker = this.sender();
        this.busyWorkers.remove(worker);
        if(!prefixFound) {
            if (message.linearCombination == null) {
                this.assignNextLinearCombinationInterval();
            } else {
                this.unassignedWork.clear();

                int[] linearCombination = message.linearCombination;
                this.log.info("Found linear combination: {}", Arrays.toString(linearCombination));
                this.results.setPrefixes(linearCombination);
                prefixFound = true;

                startFindingPartners();
            }
        }

        this.assign(worker);

    }

    private void handle(GenePartnerFoundMessage genePartnerFoundMessage) {
        ActorRef worker = this.sender();
        this.busyWorkers.remove(worker);

	    int searchedFor = genePartnerFoundMessage.searchedFor;
	    int partner = genePartnerFoundMessage.foundPartner;

	    this.results.setGenePartner(searchedFor, partner);

        this.assign(worker);

	    if(!this.results.everyoneHasAPartner()) {
	        assignNextFindingPartnerTask();
        } else {
            this.log.info("Every person has an associated partner");

           startFindingHashesWithPrefix();

        }
    }

    private void handle(HashWithPrefixFoundMessage hashWithPrefixFoundMessage) {
        ActorRef worker = this.sender();
        this.busyWorkers.remove(worker);

        int personID = hashWithPrefixFoundMessage.personID;
        String hash = hashWithPrefixFoundMessage.hash;

        this.results.setPrefixHash(personID, hash);

        this.assign(worker);

        if(!this.results.allPrefixHashesCalculated()) {
            assignNextFindingHashesWithPrefixTask();
        } else {
            this.log.info("All prefix hashes calculated");

            long stop = System.currentTimeMillis();
            System.out.println("Time: " + (stop - this.startTime));

            this.results.writeResultsToCsv();

            this.log.info("Wrote to CSV file");

            // todo terminate program
            terminateAll();

        }
    }

    private void startSearchingforLinearCombinations() {
        this.currentStartIndex = 0;
        this.currentStopIndex = this.LINEAR_COMBINATION_INTERVAL_SIZE;
        this.allPasswords = this.results.getAllPasswordsSortedByID();

        for (int i = 0; i < this.idleWorkers.size(); i++) {
            assignNextLinearCombinationInterval();
        }

    }

    private void assignNextLinearCombinationInterval() {
        this.assign(new Worker.LinearCombinationWorkMessage(this.currentStartIndex, this.currentStopIndex, this.allPasswords));
        this.currentStartIndex += this.LINEAR_COMBINATION_INTERVAL_SIZE;
        this.currentStopIndex += this.LINEAR_COMBINATION_INTERVAL_SIZE;
    }

    private void startFindingPartners() {
	    this.geneSequences = this.results.getAllGeneSequences();
	    this.numberOfUsers = this.results.getNumberOfResults();
	    this.currentGeneSequenceUser = 1;

        for (int i = 0; i < this.idleWorkers.size(); i++) {
            assignNextFindingPartnerTask();
        }
    }

    private void assignNextFindingPartnerTask() {
	    if(this.currentGeneSequenceUser <= this.numberOfUsers) {
            this.assign(new Worker.GenePartnerWorkMessage(this.geneSequences, this.currentGeneSequenceUser));
            this.currentGeneSequenceUser++;
        }
    }

    private void startFindingHashesWithPrefix() {
	    this.prefixes = this.results.getAllPrefixes();
	    this.partners = this.results.getAllPartners();

	    this.numberOfUsers = this.results.getNumberOfResults();
	    this.currentHashPrefixUser = 1;

        for (int i = 0; i < this.idleWorkers.size(); i++) {
            assignNextFindingHashesWithPrefixTask();
        }

    }

    private void assignNextFindingHashesWithPrefixTask() {
        if(this.currentHashPrefixUser <= this.numberOfUsers) {
            int partnerNumber = this.partners.get(this.currentHashPrefixUser - 1);
            int prefix = this.prefixes.get(this.currentHashPrefixUser - 1);

            this.assign(new Worker.FindHashWithPrefixWorkMessage(this.currentHashPrefixUser, partnerNumber, prefix));
            this.currentHashPrefixUser++;
        }
    }

    private boolean isTableComlpete(Map<String,Integer> rainbowTable) {
        // todo can be more efficient: just check one elem per interval: be careful because offset of one
        return this.rainbowTable.size() >= this.MAX_PASSWORD;
    }
	
	private void assign(WorkMessage work) {
		ActorRef worker = this.idleWorkers.poll();
		
		if (worker == null) {
			this.unassignedWork.add(work);
			return;
		}
		
		this.busyWorkers.put(worker, work);
		worker.tell(work, this.self());
	}
	
	private void assign(ActorRef worker) {
		WorkMessage work = this.unassignedWork.poll();

		if (work == null) {
			this.idleWorkers.add(worker);
			return;
		}
		
		this.busyWorkers.put(worker, work);
		worker.tell(work, this.self());
	}

    private void monitorSlaves(ActorRef sender) {
	    if(!(sender instanceof RemoteActorRef))
	        return;

	    String key = sender.path().address().toString();

	    if(!this.slaves.contains(key)){
	        this.slaves.add(key);
	        this.waitSlavesCount -= 1;
        }
    }

    private void terminateAll() {

        for (ActorRef actor:this.busyWorkers.keySet()) {
            actor.tell(PoisonPill.getInstance(), this.getSelf());
        }

        for (ActorRef actor:this.idleWorkers) {
            actor.tell(PoisonPill.getInstance(), this.getSelf());
        }

        // todo check if the cluster listener can be reached by parent relation or similar
        ActorSelection actorSelection = this.context().system().actorSelection("user/" + ClusterListener.DEFAULT_NAME);
        actorSelection.tell(PoisonPill.getInstance(), this.getSelf());

        this.getSelf().tell(PoisonPill.getInstance(), this.getSelf());

    }

}