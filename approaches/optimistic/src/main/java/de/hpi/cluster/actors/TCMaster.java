package de.hpi.cluster.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import de.hpi.cluster.actors.Master.DFWDoneMessage;
import de.hpi.rdse.der.dfw.DFW;
import de.hpi.rdse.der.dfw.DFWBlock;
import de.hpi.rdse.der.fw.FloydWarshall;
import de.hpi.rdse.der.util.MatrixConverter;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class TCMaster extends AbstractActor {

    public static final String DEFAULT_NAME = "tcMaster";
    private int blockSize;
    private Queue<ActorRef> workers;
    private ActorRef master;

    public static Props props() {
        return Props.create(TCMaster.class);
    }

    @Data
    @AllArgsConstructor
    public static class ConfigMessage implements Serializable {
        private static final long serialVersionUID = -42431888868862395L;
        private ConfigMessage() {}
        protected Config config;
    }

    @Data
    @AllArgsConstructor
    public static class CalculateMessage implements Serializable {
        private static final long serialVersionUID = -4243198888868862395L;
        private CalculateMessage() {}
        protected Set<Set<Integer>> duplicates;
        protected Set<ActorRef> workers;
    }

    @Data
    @AllArgsConstructor
    public static class RequestWorkMessage implements Serializable {
        private static final long serialVersionUID = -4277777777768862395L;
        private RequestWorkMessage() {}
        protected ActorRef worker;
    }

    @Data
    @AllArgsConstructor
    public static class DispatchBlockMessage implements Serializable {
        private static final long serialVersionUID = -1277777777768862395L;
        private DispatchBlockMessage() {}
        protected DFWBlock block;
    }

    private final LoggingAdapter log = Logging.getLogger(this.context().system(), this);

    private DFW dfw;
    private boolean done = false;


    @Override
    public void preStart() throws Exception {
        super.preStart();

        Reaper.watchWithDefaultReaper(this);
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
                .match(CalculateMessage.class, this::handle)
                .match(DispatchBlockMessage.class, this::handle)
                .match(RequestWorkMessage.class, this::handle)
                .matchAny(object -> this.log.info("Received unknown message: \"{}\"", object.toString()))
                .build();
    }

    private void handle(ConfigMessage configMessage) {
        Config config = configMessage.config;

        this.master = this.sender();

        this.blockSize = config.getInt("der.transitive-closure.block-size");
    }

    private void handle(RequestWorkMessage requestWorkMessage) {
        boolean calculated = this.dfw.isCalculated();
        DFWBlock block = this.dfw.getBlock();
        ActorRef worker = requestWorkMessage.worker;

        if (calculated) {
            this.sendResult();
        } else if(block != null) {
            worker.tell(new Worker.DFWWorkMessage(block), this.sender());
        } else {
            this.sender().tell(new Master.IdleDFWMessage(), requestWorkMessage.worker);
        }
    }

    private void handle(CalculateMessage calculateMessage) {
        this.log.info("Received calculateMessage");
        Set<Set<Integer>> duplicates = calculateMessage.duplicates;
        int[][] matrix = MatrixConverter.duplicateSetToMatrix(duplicates);

        this.dfw = new DFW(matrix, this.blockSize);
        this.workers = new LinkedList<>();
        this.workers.addAll(calculateMessage.workers);

        this.sendWork();
    }

    private void handle(DispatchBlockMessage dispatchBlockMessage) {
//        this.log.info("Received distpatchBlockMessage");

        DFWBlock block = dispatchBlockMessage.block;
        this.dfw.dispatch(block.getTarget());
        this.workers.add(this.sender());

        if (this.dfw.isCalculated()) {
            this.sendResult();
        } else {
            this.sendWork();
        }
    }

    private void sendWork() {
        boolean isCalculated = this.dfw.isCalculated();
        boolean hasWorkers = !this.workers.isEmpty();

        while (hasWorkers && !isCalculated) {
            ActorRef worker = workers.poll();
            DFWBlock block = this.dfw.getBlock();

            worker.tell(new Worker.DFWWorkMessage(block), this.master);

            hasWorkers = !this.workers.isEmpty();
        }
    }

    private void sendResult() {
        this.log.info("sendResult");

        int[][] matrix = this.dfw.getMatrix();

        Set<Set<Integer>> tk = MatrixConverter.fromTransitiveClosure(matrix);

        this.master.tell(new Master.DFWDoneMessage(tk), this.sender());
    }



}
