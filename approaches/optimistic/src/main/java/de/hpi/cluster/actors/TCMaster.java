package de.hpi.cluster.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import de.hpi.rdse.der.dfw.DFW;
import de.hpi.rdse.der.dfw.DFWBlock;
import de.hpi.rdse.der.util.CompressedMatrix;
import de.hpi.rdse.der.util.MappedMatrix;
import de.hpi.rdse.der.util.MatrixConverter;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class TCMaster extends AbstractActor {

    @Data @AllArgsConstructor
    public static class ConfigMessage implements Serializable {
        private static final long serialVersionUID = -42431888868862395L;
        private ConfigMessage() {}
        protected Config config;
    }

    @Data @AllArgsConstructor
    public static class RestartMessage implements Serializable {
        private static final long serialVersionUID = -424318883528862395L;
    }

    @Data @AllArgsConstructor
    public static class CalculateMessage implements Serializable {
        private static final long serialVersionUID = -4243198888868862395L;
        private CalculateMessage() {}
        protected Set<Set<Integer>> duplicates;
        protected Set<ActorRef> idleWorkers;
    }

    @Data @AllArgsConstructor
    public static class DispatchBlockMessage implements Serializable {
        private static final long serialVersionUID = -1277777777768862395L;
        private DispatchBlockMessage() {}
        protected DFWBlock block;
    }

    public static final String DEFAULT_NAME = "tcMaster";
    private final LoggingAdapter log = Logging.getLogger(this.context().system(), this);

    private Queue<ActorRef> idleWorkers;
    private DFW dfw;
    private Config config;
    private boolean restart = false;
    private ActorRef master;
    private Map<Integer, Integer> compressionLookup;

    public static Props props() {
        return Props.create(TCMaster.class);
    }


    @Override
    public void preStart() throws Exception {
        super.preStart();

        Reaper.watchWithDefaultReaper(this);
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();

        // Log the stop event
        this.log.debug("Stopped {}.", this.getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ConfigMessage.class, this::handle)
                .match(RestartMessage.class, this::handle)
                .match(CalculateMessage.class, this::handle)
                .match(DispatchBlockMessage.class, this::handle)
                .matchAny(object -> this.log.debug("Received unknown message: \"{}\"", object.toString()))
                .build();
    }

    private void handle(RestartMessage restartMessage) {
        if (this.dfw != null) {
            this.log.debug("Restart calculation");
            this.restart = true;
        }
    }

    private void handle(ConfigMessage configMessage) {
        this.master = this.sender();
        this.idleWorkers = new LinkedList<>();
        this.config = configMessage.config;
    }

    private void handle(CalculateMessage calculateMessage) {
        this.log.info("Start calculating the transitive closure");
        int blockSize = this.config.getInt("der.transitive-closure.block-size");

        Set<Set<Integer>> duplicates = calculateMessage.duplicates;
        CompressedMatrix compressed = MatrixConverter.duplicateSetToCompressedMatrix(duplicates);
        this.compressionLookup = compressed.getCompressionLookup();

        this.dfw = new DFW(compressed.getMatrix(), blockSize);
        this.idleWorkers.addAll(calculateMessage.idleWorkers);
        this.restart = false;

        this.progressCalculation();
    }

    private void handle(DispatchBlockMessage dispatchBlockMessage) {
        if (this.restart) {
            this.log.debug("Restarting calculation; drop message");
            return;
        }

        DFWBlock block = dispatchBlockMessage.block;
        this.dfw.dispatch(block.getTarget());
        this.idleWorkers.add(this.sender());

        if (this.dfw.isCalculated()) {
            this.sendResult();
        } else {
            this.progressCalculation();
        }
    }

    private void progressCalculation() {
        boolean hasNextBlock = this.dfw.hasNextBlock();
        boolean hasWorkers = !this.idleWorkers.isEmpty();

        while (hasWorkers &&  hasNextBlock) {
            DFWBlock block = this.dfw.getNextBlock();

            ActorRef worker = idleWorkers.poll();
            worker.tell(new Worker.DFWWorkMessage(block), this.master);

            hasWorkers = !this.idleWorkers.isEmpty();
            hasNextBlock = this.dfw.hasNextBlock();
        }
    }

    private void sendResult() {
        this.log.info("Transitive closure calculated");

        int[][] matrix = this.dfw.getMatrix();

        Set<Set<Integer>> tk = MatrixConverter.formTransitiveClosure(matrix);
        tk = MatrixConverter.translateWithCompressionLookup(tk, this.compressionLookup);

        this.master.tell(new Master.DFWDoneMessage(tk, this.idleWorkers), this.sender());
    }

}
