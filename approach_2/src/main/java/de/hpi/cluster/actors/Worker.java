package de.hpi.cluster.actors;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import de.hpi.cluster.ClusterMaster;
import de.hpi.cluster.actors.Master.DataAckMessage;
import de.hpi.cluster.actors.listeners.MetricsListener;
import de.hpi.cluster.messages.InfoObject;
import de.hpi.cluster.messages.interfaces.BlockingInterface;
import de.hpi.ddd.dd.DuplicateDetector;
import de.hpi.ddd.dd.SimpleDuplicateDetector;
import de.hpi.ddd.partition.HashRouter;
import de.hpi.ddd.similarity.UniversalComparator;
import de.hpi.ddd.similarity.numeric.AbsComparator;
import de.hpi.ddd.similarity.numeric.NumberComparator;
import de.hpi.ddd.similarity.strings.JaroWinklerComparator;
import de.hpi.ddd.similarity.strings.StringComparator;
import de.hpi.utils.helper.TokenGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

public class Worker extends AbstractActor {

    private enum Phase {
        PARSING, REPARTITIONING, UNDEFINED
    }

    ////////////////////////
    // Actor Construction //
    ////////////////////////

    public static final String DEFAULT_NAME = "worker";

    public static Props props() {
        return Props.create(Worker.class);
    }

    ////////////////////
    // Actor Messages //
    ////////////////////

    @Data @AllArgsConstructor
    public static class RegisterAckMessage implements Serializable {
        private static final long serialVersionUID = -4243194361868862395L;
        private RegisterAckMessage() {}
        protected BlockingInterface blocking;
        protected double similarityThreshold;
        protected double numberComparisonIntervalStart;
        protected double numberComparisonIntervalEnd;
    }

    @Data @AllArgsConstructor
    public static class PartitionBroadcastMessage implements Serializable {
        private static final long serialVersionUID = -7643424361868862395L;
        private PartitionBroadcastMessage() {}
        protected HashRouter<ActorRef> router;
    }

    @Data @AllArgsConstructor
    public static class DataMessage implements Serializable {
        private static final long serialVersionUID = -7643424368868862395L;
        private DataMessage() {}
        protected String data;
    }

    @Data @AllArgsConstructor
    public static class ParsedDataMessage implements Serializable {
        private static final long serialVersionUID = -1643424368868861534L;
        private ParsedDataMessage() {}
        protected String key;
        protected List<String[]> data;
        protected String id;
    }

    @Data @AllArgsConstructor
    public static class ParsedDataAckMessage implements Serializable {
        private static final long serialVersionUID = -5432124368868861534L;
        private ParsedDataAckMessage() {}
        protected String id;
    }

    @Data @AllArgsConstructor
    public static class StartComparingMessage implements Serializable {
        private static final long serialVersionUID = -5431818188868861534L;
    }

    /////////////////
    // Actor State //
    /////////////////

    private final int TOKEN_SIZE = 10;

    private final LoggingAdapter log = Logging.getLogger(this.context().system(), this);
    private final Cluster cluster = Cluster.get(this.context().system());
    private double similarityThreshold;
    private double numberComparisonIntervalStart;
    private double numberComparisonIntervalEnd;
    private HashRouter<ActorRef> router;
    private ActorRef masterActor;
    private Map<String,List<String[]>> groupedData = new HashMap<>();
    private List<String> waitingFor = new LinkedList<>();
    private Phase phase = Phase.UNDEFINED;


    /////////////////////
    // Actor Lifecycle //
    /////////////////////

    @Override
    public void preStart() throws Exception {
        super.preStart();

        this.cluster.subscribe(this.self(), MemberUp.class);

        // Register at this actor system's reaper
//        Reaper.watchWithDefaultReaper(this);
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();

        // todo REAPER check if the metrics listener can be reached by parent relation or similar
        ActorSelection actorSelection = this.context().system().actorSelection("user/" + MetricsListener.DEFAULT_NAME);
        actorSelection.tell(PoisonPill.getInstance(), this.getSelf());

        this.getSelf().tell(PoisonPill.getInstance(), this.getSelf());

        this.cluster.unsubscribe(this.self());

        // Log the stop event
        this.log.info("Stopped {}.", this.getSelf());
    }

    ////////////////////
    // Actor Behavior //
    ////////////////////

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CurrentClusterState.class, this::handle)
                .match(MemberUp.class, this::handle)
                .match(RegisterAckMessage.class, this::handle)
                .match(PartitionBroadcastMessage.class, this::handle)
                .match(DataMessage.class, this::handle)
                .match(ParsedDataMessage.class, this::handle)
                .match(ParsedDataAckMessage.class, this::handle)
                .match(StartComparingMessage.class, this::handle)
                .matchAny(object -> this.log.info("Received unknown message: \"{}\"", object.toString()))
                .build();
    }

    //	##############################################################
    //  Registration
    //	##############################################################

    private void register(Member member) {
        if (member.hasRole(ClusterMaster.MASTER_ROLE)) {
//            InfoObject infoObject = new de.hpi.cluster.messages.InfoObject();
            InfoObject infoObj = new InfoObject(this.self().toString());
            this.getContext()
                    .actorSelection(member.address() + "/user/" + Master.DEFAULT_NAME)
                    .tell(new Master.RegisterMessage(infoObj), this.self());
        }
    }

    private void handle(RegisterAckMessage registerAckMessage) {
        this.log.info("Received RegisterAck");
        masterActor = this.sender();
        // TODO use blocking method
//        this.blocking = registerAckMessage.blocking;
        this.similarityThreshold = registerAckMessage.similarityThreshold;
        this.numberComparisonIntervalStart = registerAckMessage.numberComparisonIntervalStart;
        this.numberComparisonIntervalEnd = registerAckMessage.numberComparisonIntervalEnd;

        this.sender().tell(new Master.ReadyForWorkMessage(), this.self());
    }

    //	##############################################################
    //  partitioning
    //	##############################################################

    private void handle(PartitionBroadcastMessage partitionBroadcastMessage) {
        this.log.info("received PartitionBroadcastMessage");
        this.router = partitionBroadcastMessage.router;
        repartition();
    }

    private void repartition() {
//        this.masterActor.tell(new Master.RepartitionFinishedMessage(), this.self());
        this.phase = Phase.REPARTITIONING;
        if (this.groupedData.isEmpty()) {
            this.masterActor.tell(new Master.RepartitionFinishedMessage(), this.self());
        } else {
            this.distributeDataToWorkers(this.groupedData);
        }
    }

    //	##############################################################
    //  send Data
    //	##############################################################

    private void handle(DataMessage dataMessage) {
        Map<String,List<String[]>> parsedData = new HashMap<>();

        String[] lines = dataMessage.data.split("\n");
        List<String[]> records = new LinkedList<>();

        for (String line: lines) {
            records.add(line.split(","));
        }

        // TODO use blocking function that was received via message before
        for (String[] record: records) {
            String prefix = record[1].substring(0,Math.min(5, record[1].length() - 1));
            if(parsedData.containsKey(prefix)) {
                parsedData.get(prefix).add(record);
            } else {
                List<String[]> list = new LinkedList<String[]>();
                list.add(record);
                parsedData.put(prefix,list);
            }
        }

        // after the data is parsed it has be send to the responsible workers
        this.phase = Phase.PARSING;
        distributeDataToWorkers(parsedData);

    }

    private void distributeDataToWorkers(Map<String, List<String[]>> parsedData) {
        for(String key: parsedData.keySet()) {
            ActorRef responsibleWorker = this.router.getObjectForKey(key);
            List<String[]> pd = parsedData.get(key);
            String id = TokenGenerator.getRandomAlphaNumericString(this.TOKEN_SIZE);
            responsibleWorker.tell(new ParsedDataMessage(key, pd, id), this.self());
            waitingFor.add(id);
        }
    }

    private void handle(ParsedDataMessage parsedDataMessage) {
        String key = parsedDataMessage.key;
        if(groupedData.containsKey(key)) {
            groupedData.get(key).addAll(parsedDataMessage.data);
        } else {
            List<String[]> list = new LinkedList<String[]>();
            list.addAll(parsedDataMessage.data);
            groupedData.put(key,list);
        }
        this.sender().tell(new ParsedDataAckMessage(parsedDataMessage.id), this.self());
    }

    private void handle(ParsedDataAckMessage parsedDataAckMessage) {
        this.waitingFor.remove(parsedDataAckMessage.id);
        if(this.waitingFor.isEmpty()) {
            switch (phase) {
                case PARSING:
                    this.masterActor.tell(new DataAckMessage(), this.self());
                    break;
                case REPARTITIONING:
                    this.masterActor.tell(new Master.RepartitionFinishedMessage(), this.self());
                    break;
                default:
                    this.log.warning("Reached undefined state");
            }

        }
    }

    //	##############################################################
    //  similarity
    //	##############################################################

    private void handle(StartComparingMessage startComparingMessage) {
        this.log.info("number of data keys: {}", this.groupedData.keySet().size());
        StringComparator sComparator = new JaroWinklerComparator();
        NumberComparator nComparator = new AbsComparator(this.numberComparisonIntervalStart,this.numberComparisonIntervalEnd);
        UniversalComparator comparator = new UniversalComparator(sComparator, nComparator);

        DuplicateDetector duDetector= new SimpleDuplicateDetector(comparator, this.similarityThreshold);

        for (String key: groupedData.keySet()) {
            Set<Set<Integer>> duplicates = duDetector.findDuplicatesForBlock(groupedData.get(key));
            if (!duplicates.isEmpty()) {
                this.sender().tell(new Master.DuplicateMessage(duplicates), this.self());
            }
        }
        this.sender().tell(new Master.ComparisonFinishedMessage(), this.self());
    }

    private void handle(CurrentClusterState message) {
        message.getMembers().forEach(member -> {
            if (member.status().equals(MemberStatus.up()))
                this.register(member);
        });
    }

    private void handle(MemberUp message) {
        this.register(message.member());
    }
}