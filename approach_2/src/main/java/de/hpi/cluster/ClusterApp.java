package de.hpi.cluster;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

public class ClusterApp {

    public static final String ACTOR_SYSTEM_NAME = "ddd-approach-2";

    public static void main(String[] args) {

        MasterCommand masterCommand = new MasterCommand();
        SlaveCommand workerCommand = new SlaveCommand();
        JCommander jCommander = JCommander.newBuilder()
                .addCommand(de.hpi.cluster.ClusterMaster.MASTER_ROLE, masterCommand)
                .addCommand(ClusterWorker.WORKER_ROLE, workerCommand)
                .build();

        try {
            jCommander.parse(args);

            if (jCommander.getParsedCommand() == null) {
                throw new ParameterException("No command given.");
            }

            switch (jCommander.getParsedCommand()) {
                case ClusterMaster.MASTER_ROLE:
                    ClusterMaster.start(ACTOR_SYSTEM_NAME, masterCommand.workers, masterCommand.host, masterCommand.port, masterCommand.workers, masterCommand.inputPath, masterCommand.goldPath);
                    break;
                case ClusterWorker.WORKER_ROLE:
                    ClusterWorker.start(ACTOR_SYSTEM_NAME, workerCommand.workers, workerCommand.host, workerCommand.port, workerCommand.masterhost, workerCommand.masterport);
                    break;
                default:
                    throw new AssertionError();
            }

        } catch (ParameterException e) {
            System.out.printf("Could not parse args: %s\n", e.getMessage());
            if (jCommander.getParsedCommand() == null) {
                jCommander.usage();
            } else {
                jCommander.usage(jCommander.getParsedCommand());
            }
            System.exit(1);
        }
    }

    abstract static class CommandBase {

        public static final int DEFAULT_MASTER_PORT = 7877;
        public static final int DEFAULT_SLAVE_PORT = 7879;
        public static final int DEFAULT_WORKERS = 4;

        @Parameter(names = {"-h", "--host"}, description = "this machine's host name or IP to bind against")
        String host = getDefaultHost();

        String getDefaultHost() {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                return "localhost";
            }
        }

        @Parameter(names = {"-p", "--port"}, description = "port to bind against", required = false)
        int port = this.getDefaultPort();

        abstract int getDefaultPort();

        @Parameter(names = {"-w", "--workers"}, description = "number of workers to start locally", required = false)
        int workers = DEFAULT_WORKERS;

        @Parameter(names = {"-i", "--input"}, description = "file path to input file", required = true)
        String inputPath;

        @Parameter(names = {"-g", "--gold"}, description = "file path to gold standard file", required = true)
        String goldPath;
    }

    @Parameters(commandDescription = "start a master actor system")
    static class MasterCommand extends CommandBase {

        @Override
        int getDefaultPort() {
            return DEFAULT_MASTER_PORT;
        }
    }

    @Parameters(commandDescription = "start a slave actor system")
    static class SlaveCommand extends CommandBase {

        @Override
        int getDefaultPort() {
            return DEFAULT_SLAVE_PORT;
        }

        @Parameter(names = {"-mp", "--masterport"}, description = "port of the master", required = false)
        int masterport = DEFAULT_MASTER_PORT;

        @Parameter(names = {"-mh", "--masterhost"}, description = "host name or IP of the master", required = true)
        String masterhost;
    }
}
