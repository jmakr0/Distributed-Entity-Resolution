package de.hpi.cluster.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import de.hpi.cluster.ClusterMaster;
import de.hpi.cluster.actors.Profiler.RegistrationMessage;
import de.hpi.cluster.actors.listeners.MetricsListener;
import de.hpi.utils.SerialAnalyzer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

public class Worker extends AbstractActor {

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

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class WorkMessage implements Serializable {
        private static final long serialVersionUID = -7643194361868862395L;
        private WorkMessage() {}
        protected int id;
    }

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class RainbowWorkMessage extends WorkMessage {
        private RainbowWorkMessage() {}
        private int min;
        private int max;
    }

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class LinearCombinationWorkMessage extends WorkMessage {
        private LinearCombinationWorkMessage() {}
        private long intervalStart;
        private long intervalStop;
        private int[] passwords;
    }

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class GenePartnerWorkMessage extends WorkMessage {
        private GenePartnerWorkMessage() {}
        private List<String> geneSequences;
        // The person for whom we are looking for a partner
        private int partner;
    }

    @Data @AllArgsConstructor @SuppressWarnings("unused")
    public static class FindHashWithPrefixWorkMessage extends WorkMessage {
        private FindHashWithPrefixWorkMessage() {}
        private int personID;
        private int partnerNumber;
        private int prefix;
    }

    /////////////////
    // Actor State //
    /////////////////

    private final LoggingAdapter log = Logging.getLogger(this.context().system(), this);
    private final Cluster cluster = Cluster.get(this.context().system());

    /////////////////////
    // Actor Lifecycle //
    /////////////////////

    @Override
    public void preStart() throws Exception {
        super.preStart();

        this.cluster.subscribe(this.self(), MemberUp.class);

        // Register at this actor system's reaper
        Reaper.watchWithDefaultReaper(this);
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();

        // todo check if the metrics listener can be reached by parent relation or similar
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
                .match(RainbowWorkMessage.class, this::handle)
                .match(LinearCombinationWorkMessage.class, this::handle)
                .match(GenePartnerWorkMessage.class, this::handle)
                .match(FindHashWithPrefixWorkMessage.class, this::handle)
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

    private void register(Member member) {
        if (member.hasRole(ClusterMaster.MASTER_ROLE))
            this.getContext()
                    .actorSelection(member.address() + "/user/" + Profiler.DEFAULT_NAME)
                    .tell(new RegistrationMessage(), this.self());
    }

    private void handle(RainbowWorkMessage message) {
        Map<String,Integer> hashes = calculateHashes(message.min, message.max);
		this.sender().tell(new Profiler.CompletionRainbowTaskMessage(hashes), this.self());
    }

    private void handle(LinearCombinationWorkMessage message) {
        int[] linearCombination = checkLinearCombinationsInterval(message.intervalStart, message.intervalStop, message.passwords);
        this.sender().tell(new Profiler.CheckedLinearCombinationIntervalMessage(linearCombination), this.self());
    }

    private void handle(GenePartnerWorkMessage genePartnerWorkMessage) {
        // -1 to map from person id to List index
        int searchingForPartner = genePartnerWorkMessage.partner - 1;
        int partner = SerialAnalyzer.longestOverlapPartner(searchingForPartner, genePartnerWorkMessage.geneSequences);
        // map back from list index to person id
        partner += 1;

        this.sender().tell(new Profiler.GenePartnerFoundMessage(genePartnerWorkMessage.partner, partner), this.self());
    }

    private void handle(FindHashWithPrefixWorkMessage findHashWithPrefixWorkMessage) {
        int personID = findHashWithPrefixWorkMessage.personID;
        int partnerNumber = findHashWithPrefixWorkMessage.partnerNumber;
        String prefix = findHashWithPrefixWorkMessage.prefix == 1 ? "1" : "0";

        String hash = SerialAnalyzer.findHash(partnerNumber, prefix, 5);

        this.sender().tell(new Profiler.HashWithPrefixFoundMessage(personID, hash), this.self());
    }

    private int[] checkLinearCombinationsInterval(long start, long stop, int[] numbers) {
        for (long a = start; a < stop; a++) {
            String binary = Long.toBinaryString(a);

            int[] prefixes = new int[62];
            for (int i = 0; i < prefixes.length; i++)
                prefixes[i] = 1;

            int i = 0;
            for (int j = binary.length() - 1; j >= 0; j--) {
                if (binary.charAt(j) == '1')
                    prefixes[i] = -1;
                i++;
            }

            int sum = this.sum(numbers, prefixes);

            if (sum == 0)
                return prefixes;
        }

        return null;
    }

    private int sum(int[] numbers, int[] prefixes) {
        int sum = 0;
        for (int i = 0; i < numbers.length; i++)
            sum += numbers[i] * prefixes[i];
        return sum;
    }

    private Map<String,Integer> calculateHashes(int min, int max) {
        Map<String,Integer> hashes = new HashMap<>();

        for (int i = min; i <= max; i++) {
            hashes.put(SerialAnalyzer.hash(i),i);
        }

        return hashes;
    }

}