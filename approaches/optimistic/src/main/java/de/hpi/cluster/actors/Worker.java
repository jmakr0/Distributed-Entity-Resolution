package de.hpi.cluster.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.serialization.Serialization;
import akka.serialization.SerializationExtension;
import akka.serialization.Serializer;
import de.hpi.cluster.ClusterMaster;
import de.hpi.cluster.messages.interfaces.Blocking;
import de.hpi.rdse.der.dfw.DFWBlock;
import de.hpi.rdse.der.dude.DuplicateDetector;
import de.hpi.rdse.der.dude.SimpleDuplicateDetector;
import de.hpi.rdse.der.partitioning.Md5HashRouter;
import de.hpi.rdse.der.similarity.UniversalComparator;
import de.hpi.rdse.der.similarity.numeric.AbsComparator;
import de.hpi.rdse.der.similarity.numeric.NumberComparator;
import de.hpi.rdse.der.similarity.string.JaroWinklerComparator;
import de.hpi.rdse.der.similarity.string.StringComparator;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

public class Worker extends AbstractActor {

    @Data @AllArgsConstructor
    public static class RegisterAckMessage implements Serializable {
        private static final long serialVersionUID = -4243194361868862395L;
        private RegisterAckMessage() {}
        protected Blocking blocking;
        protected byte[] serializedRouter;
    }

    @Data @AllArgsConstructor
    public static class RepartitionMessage implements Serializable {
        private static final long serialVersionUID = -7643424361868862395L;
        private RepartitionMessage() {}
        protected byte[] serializedRouter;
    }

    @Data @AllArgsConstructor
    public static class DataMessage implements Serializable {
        private static final long serialVersionUID = -7643424368868862395L;
        private DataMessage() {}
        protected String records;
    }

    @Data @AllArgsConstructor
    public static class ParsedDataMessage implements Serializable {
        private static final long serialVersionUID = -1643424368868861534L;
        private ParsedDataMessage() {}
        protected String key;
        protected List<String[]> records;
        protected byte[] serializedRouter;
    }

    @Data @AllArgsConstructor
    public static class SimilarityMessage implements Serializable {
        private static final long serialVersionUID = -5431818188868861534L;
        private SimilarityMessage() {}
        private double similarityThreshold;
        private int thresholdMin;
        private int thresholdMax;
    }

    @Data @AllArgsConstructor
    public static class DFWWorkMessage implements Serializable {
        private static final long serialVersionUID = -1943426365861861534L;
        private DFWWorkMessage() {}
        protected DFWBlock block;
    }

    public static final String DEFAULT_NAME = "worker";
    private final LoggingAdapter log = Logging.getLogger(this.context().system(), this);

    private boolean calculatingSimilarity = false;

    private final Cluster cluster = Cluster.get(this.context().system());

    private Map<String, List<String[]>> records = new HashMap<>();

    private Md5HashRouter router;
    private Blocking blocking;
    private ActorRef master;

    public static Props props() {
        return Props.create(Worker.class);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        this.cluster.subscribe(this.self(), MemberUp.class);
        Reaper.watchWithDefaultReaper(this);
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        this.cluster.unsubscribe(this.self());
        this.log.debug("Stopped {}.", this.getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CurrentClusterState.class, this::handle)
                .match(MemberUp.class, this::handle)
                .match(RegisterAckMessage.class, this::handle)
                .match(RepartitionMessage.class, this::handle)
                .match(DataMessage.class, this::handle)
                .match(ParsedDataMessage.class, this::handle)
                .match(SimilarityMessage.class, this::handle)
                .match(DFWWorkMessage.class, this::handle)
                .matchAny(object -> this.log.debug("Received unknown message: \"{}\"", object.toString()))
                .build();
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

    private void handle(RegisterAckMessage registerAckMessage) {
        this.log.debug("Registered");
        this.master = this.sender();
        this.blocking = registerAckMessage.blocking;
        Md5HashRouter router = this.deserializeRouter(registerAckMessage.serializedRouter);
        this.setRouter(router, "registration");

        this.sender().tell(new Master.WorkRequestMessage(), this.self());
    }

    private void handle(RepartitionMessage repartitionMessage) {
        Md5HashRouter router = this.deserializeRouter(repartitionMessage.serializedRouter);

        if(router.getVersion() > this.getRouterVersion()) {
            this.setRouter(router, "repartitioning");
            this.repartition();
        }

        this.sender().tell(new Master.WorkRequestMessage(), this.self());
    }

    private void handle(DataMessage dataMessage) {
        this.log.debug("Data message received");

        Map<String,List<String[]>> parsedData = new HashMap<>();

        String[] lines = this.clean(dataMessage.records);

        List<String[]> records = this.extract(lines);

        for (String[] record: records) {
            String prefix = this.blocking.getKey(record);
            if(parsedData.containsKey(prefix)) {
                parsedData.get(prefix).add(record);
            } else {
                List<String[]> list = new LinkedList<String[]>();
                list.add(record);
                parsedData.put(prefix,list);
            }
        }

        this.distributeDataToWorkers(parsedData);

        this.log.debug("Records size in total: {}",this.records.keySet().size());

        this.sender().tell(new Master.WorkRequestMessage(), this.self());
    }

    private void handle(ParsedDataMessage parsedDataMessage) {
        if (this.calculatingSimilarity) {
            this.master.tell(new Master.WorkerGotParsedData(), this.self());
        }

        Md5HashRouter router = this.deserializeRouter(parsedDataMessage.serializedRouter);
        int myRouterVersion = this.getRouterVersion();
        int peerRouterVersion = router.getVersion();

        this.setRecords(parsedDataMessage.key, parsedDataMessage.records);

        if(peerRouterVersion > myRouterVersion) {
            this.setRouter(router, "parsed records");
            this.repartition();
        }

    }

    private void handle(SimilarityMessage similarityMessage) {
        this.calculatingSimilarity = true;
        StringComparator sComparator = new JaroWinklerComparator();
        NumberComparator nComparator = new AbsComparator(similarityMessage.thresholdMin, similarityMessage.thresholdMax);
        UniversalComparator comparator = new UniversalComparator(sComparator, nComparator);

        DuplicateDetector duDetector= new SimpleDuplicateDetector(comparator, similarityMessage.similarityThreshold);

        for (String key: records.keySet()) {
            Set<Set<Integer>> duplicates = duDetector.findDuplicates(records.get(key));
            if (!duplicates.isEmpty()) {
                this.sender().tell(new Master.DuplicateMessage(duplicates), this.self());
            }
        }

        this.sender().tell(new Master.WorkerFinishedMatchingMessage(), this.self());
    }

    private void handle(DFWWorkMessage dfwWorkMessage) {
        DFWBlock block = dfwWorkMessage.block;
        block.calculate();

        this.sender().tell(new Master.DFWWorkFinishedMessage(block), this.self());
    }

    private void register(Member member) {
        if (member.hasRole(ClusterMaster.MASTER_ROLE)) {

            this.getContext()
                    .actorSelection(member.address() + "/user/" + Master.DEFAULT_NAME)
                    .tell(new Master.RegisterMessage(), this.self());
        }
    }

    private int getRouterVersion() {
        return this.router != null ? this.router.getVersion() : 0;
    }

    private void setRouter(Md5HashRouter router, String source) {
        if(this.router == null) {
            this.log.debug("Set router to version {} from {}", router.getVersion(), source);
        } else {
            this.log.debug("Set router from version {} to {} from {}", this.router.getVersion(), router.getVersion(), source);
        }
        this.router = router;
    }

    private void setRecords(String key, List<String[]> records) {
        if(this.records.containsKey(key)) {
            this.records.get(key).addAll(records);
        } else {
            List<String[]> list = new LinkedList<>();
            list.addAll(records);
            this.records.put(key,list);
        }
    }

    private byte[] serializeRouter() {
        Serialization serialization = SerializationExtension.get(this.context().system());
        Serializer serializer = serialization.findSerializerFor(Md5HashRouter.class);

        return serializer.toBinary(this.router);
    }

    private Md5HashRouter deserializeRouter(byte[] serializedRouter) {
        Serialization serialization = SerializationExtension.get(this.context().system());
        Serializer serializer = serialization.findSerializerFor(Md5HashRouter.class);

        return  (Md5HashRouter) serializer.fromBinary(serializedRouter);
    }

    private String[] clean(String input) {
        String[] lines = {};

        if (!input.isEmpty()) {
            String data = input
                    .replaceAll("\"", "")
                    .replaceAll("\'", "");
            lines = data.split("\n");
        }

        return lines;
    }

    private List<String[]> extract(String[] lines) {
        List<String[]> records = new LinkedList<>();

        for (String line: lines) {
            records.add(line.split(","));
        }

        return records;
    }

    private void repartition() {
        this.log.debug("Start repartitioning");

        Iterator<Map.Entry<String,List<String[]>>> iterator = this.records.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String,List<String[]>> entry = iterator.next();
            ActorRef peer = this.router.responsibleActor(entry.getValue().toString());

            if(peer.compareTo(this.self()) != 0) {
                byte [] serializedRouter = this.serializeRouter();
                peer.tell(new ParsedDataMessage(entry.getKey(), entry.getValue(), serializedRouter), this.self());
                iterator.remove();
            }

        }
    }

    private void distributeDataToWorkers(Map<String, List<String[]>> parsedData) {
        for(String key: parsedData.keySet()) {
            ActorRef responsibleWorker = this.router.responsibleActor(key);
            List<String[]> records = parsedData.get(key);

            if(responsibleWorker.compareTo(this.self()) == 0) {
                this.setRecords(key, records);
            } else {
                byte [] serializedRouter = this.serializeRouter();
                responsibleWorker.tell(new ParsedDataMessage(key, records, serializedRouter), this.self());
            }

        }
    }

}