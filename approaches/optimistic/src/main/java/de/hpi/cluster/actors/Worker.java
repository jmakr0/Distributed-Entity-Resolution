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
import de.hpi.cluster.ClusterMaster;
import de.hpi.cluster.actors.MatchingCoordinator.WorkerFinishedMatchingMessage;
import de.hpi.cluster.messages.InfoObject;
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

    private enum Phase {
        PARSING, REPARTITIONING, UNDEFINED
    }

    public static final String DEFAULT_NAME = "worker";

    public static Props props() {
        return Props.create(Worker.class);
    }

    @Data @AllArgsConstructor
    public static class RegisterAckMessage implements Serializable {
        private static final long serialVersionUID = -4243194361868862395L;
        private RegisterAckMessage() {}
        protected Blocking blocking;
        protected Md5HashRouter router;
    }

    @Data @AllArgsConstructor
    public static class RepartitionMessage implements Serializable {
        private static final long serialVersionUID = -7643424361868862395L;
        private RepartitionMessage() {}
        protected Md5HashRouter router;
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
        protected Md5HashRouter router;
    }

    @Data @AllArgsConstructor
    public static class SimilarityMessage implements Serializable {
        private static final long serialVersionUID = -5431818188868861534L;
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

    private final LoggingAdapter log = Logging.getLogger(this.context().system(), this);
    private final Cluster cluster = Cluster.get(this.context().system());

    private Md5HashRouter router;
    private Map<String, List<String[]>> data = new HashMap<>();

    private Blocking blocking;


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

        // Log the stop event
        this.log.info("Stopped {}.", this.getSelf());
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
                .matchAny(object -> this.log.info("Received unknown message: \"{}\"", object.toString()))
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
        this.log.info("RegisterAckMessage received");
        this.log.info("Router version: " + registerAckMessage.router.getVersion());

        this.blocking = registerAckMessage.blocking;
        this.setRouter(registerAckMessage.router, "RegisterAckMessage");

//        this.sender().tell(new Master.WorkRequestMessage(0), this.self());
        this.sender().tell(new Master.WorkRequestMessage(this.getRouterVersion()), this.self());
    }

    private void handle(RepartitionMessage repartitionMessage) {
        this.log.info("Repartitioning with worker router {}, master router {}", this.getRouterVersion(), repartitionMessage.router.getVersion());

        if(repartitionMessage.router.getVersion() > this.getRouterVersion()) {
            this.setRouter(repartitionMessage.router, "RepartitionMessage");
            this.repartition();
        }

        this.sender().tell(new Master.WorkRequestMessage(this.getRouterVersion()), this.self());
    }

//    private void handle(RouterVersionRequestMessage routerVersionRequestMessage) {
//        ActorRef peer = this.sender();
//
//        peer.tell(new RouterVersionResponseMessage(this.router.getVersion()), this.self());
//    }

//    private void handle(RouterVersionResponseMessage routerVersionResponseMessage) {
//        ActorRef peer = this.sender();
//        int myRouterVersion = this.router.getVersion();
//        int peerRouterVersion = routerVersionResponseMessage.version;
//
//        if(myRouterVersion == peerRouterVersion) {
//            this.sendData(peer);
//        } else if (myRouterVersion > peerRouterVersion) {
//            this.sendRouter(peer);
//        } else {
//            this.requestRouter(peer);
//        }
//    }

//    private void handle(RouterRequestMessage routerRequestMessage) {
//        ActorRef peer = this.sender();
//
//        peer.tell(new RepartitionMessage(this.router), this.self());
//    }

//    private void sendData(ActorRef peer) {

    // todo:
    // get data for peer
    // delete peer from sendQueue
    // send data to peer

//        Iterator<Map.Entry<String,List<String[]>>> iterator = this.data.entrySet().iterator();
//
//        while (iterator.hasNext()) {
//            Map.Entry<String,List<String[]>> entry = iterator.next();
//            ActorRef responsibleWorker = this.router.getObjectForKey(entry.getValue().toString());
//
//        }
////            ActorRef responsibleWorker = router.getObjectForKey(key);
////            List<String[]> pd = this.data.get(key);
////            responsibleWorker.tell(new ParsedDataMessage(key, pd), this.self());
////        }
//    }

//    private void sendRouter(ActorRef peer) {
//        peer.tell(new RepartitionMessage(this.router), this.self());
//    }

//    private void requestRouter(ActorRef peer) {
//        peer.tell(new RouterRequestMessage(), this.self());
//    }

    private void handle(DataMessage dataMessage) {
        this.log.info("data massage received");

        Map<String,List<String[]>> parsedData = new HashMap<>();

        String[] lines = {};

        if (!dataMessage.data.isEmpty()) {
            String data = cleanData(dataMessage.data);
            lines = data.split("\n");
        }
        List<String[]> records = new LinkedList<>();

        for (String line: lines) {
            records.add(line.split(","));
        }

        // TODO use blocking function that was received via message before
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

        this.log.info("data size: {}",this.data.keySet().size());

        this.sender().tell(new Master.WorkRequestMessage(this.getRouterVersion()), this.self());
    }

    private String cleanData(String data) {
        return data.replaceAll("\"", "")
            .replaceAll("\'", "");
    }

    private void repartition() {
        this.log.info("Repartition");

        Iterator<Map.Entry<String,List<String[]>>> iterator = this.data.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String,List<String[]>> entry = iterator.next();
            ActorRef peer = this.router.responsibleActor(entry.getValue().toString());

            if(peer.compareTo(this.self()) != 0) {
                peer.tell(new ParsedDataMessage(entry.getKey(), entry.getValue(), this.router), this.self());
                iterator.remove();
            }

        }
    }

    private void distributeDataToWorkers(Map<String, List<String[]>> parsedData) {
        for(String key: parsedData.keySet()) {
            ActorRef responsibleWorker = this.router.responsibleActor(key);
            List<String[]> pd = parsedData.get(key);

            if(responsibleWorker.compareTo(this.self()) == 0) {

                // todo: put in method
                if(this.data.containsKey(key)) {
                    this.data.get(key).addAll(pd);
                } else {
                    List<String[]> list = new LinkedList<String[]>();
                    list.addAll(pd);
                    this.data.put(key, list);
                }

            } else {
                responsibleWorker.tell(new ParsedDataMessage(key, pd, this.router), this.self());
            }

        }
    }

    private void handle(ParsedDataMessage parsedDataMessage) {
        int myRouterVersion = this.getRouterVersion();
        int peerRouterVersion = parsedDataMessage.router.getVersion();

        String key = parsedDataMessage.key;

        if(this.data.containsKey(key)) {
            this.data.get(key).addAll(parsedDataMessage.data);
        } else {
            List<String[]> list = new LinkedList<String[]>();
            list.addAll(parsedDataMessage.data);
            this.data.put(key,list);
        }

        if(peerRouterVersion > myRouterVersion) {
            this.setRouter(parsedDataMessage.router, "ParsedDataMessage");
            this.repartition();
        }

    }

    private void handle(SimilarityMessage similarityMessage) {
        this.log.info("number of data keys: {}", this.data.keySet().size());

        StringComparator sComparator = new JaroWinklerComparator();
        NumberComparator nComparator = new AbsComparator(similarityMessage.thresholdMin, similarityMessage.thresholdMax);
        UniversalComparator comparator = new UniversalComparator(sComparator, nComparator);

        DuplicateDetector duDetector= new SimpleDuplicateDetector(comparator, similarityMessage.similarityThreshold);

        for (String key: data.keySet()) {
            Set<Set<Integer>> duplicates = duDetector.findDuplicates(data.get(key));
            if (!duplicates.isEmpty()) {
                this.sender().tell(new Master.DuplicateMessage(duplicates), this.self());
            }
        }

        this.sender().tell(new Master.WorkerFinishedMatchingMessage(), this.self());
    }

    private void handle(DFWWorkMessage dfwWorkMessage) {
        DFWBlock block = dfwWorkMessage.block;
        block.calculate();

//        this.log.info("tell DFWWorkFinishedMessage");
        this.sender().tell(new Master.DFWWorkFinishedMessage(block), this.self());
    }

    private void register(Member member) {
        if (member.hasRole(ClusterMaster.MASTER_ROLE)) {
            InfoObject infoObj = new InfoObject(this.self().toString());
            this.getContext()
                    .actorSelection(member.address() + "/user/" + Master.DEFAULT_NAME)
                    .tell(new Master.RegisterMessage(infoObj), this.self());
        }
    }

    private int getRouterVersion() {
        return this.router != null ? this.router.getVersion() : 0;
    }

    private void setRouter(Md5HashRouter router, String source) {
        if(this.router == null) {
            this.log.info("Set router to version {} from {}", router.getVersion(), source);
        } else {
            this.log.info("Set router from version {} to {} from {}", this.router.getVersion(), router.getVersion(), source);
        }
        this.router = router;
    }

}