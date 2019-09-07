package de.hpi.cluster;

import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import com.typesafe.config.Config;
import de.hpi.cluster.actors.Reaper;
import de.hpi.cluster.actors.Worker;
import de.hpi.cluster.actors.listeners.MetricsListener;

//import de.hpi.cluster.actors.Reaper;

public class ClusterWorker extends ClusterSystem {

	public static final String WORKER_ROLE = "worker";

	public static void start(String actorSystemName, Config config) {
		int workers = config.getInt("der.cluster.worker.worker-actors");
		String host = config.getString("der.cluster.worker.default-host-address");
		int port = config.getInt("der.cluster.worker.port");
		int masterPort = config.getInt("der.cluster.master.port");
		String masterHost = config.getString("der.cluster.master.default-host-address");

		start(actorSystemName, workers, host, port, masterHost, masterPort);
	}

	private static void start(String actorSystemName, int workers, String host, int port, String masterHost, int masterPort) {
		
		final Config config = createConfiguration(actorSystemName, WORKER_ROLE, host, port, masterHost, masterPort);
		
		final ActorSystem system = createSystem(actorSystemName, config);
		
		Cluster.get(system).registerOnMemberUp(new Runnable() {
			@Override
			public void run() {
				system.actorOf(Reaper.props(), Reaper.DEFAULT_NAME);

				system.actorOf(MetricsListener.props(), MetricsListener.DEFAULT_NAME);

				for (int i = 0; i < workers; i++)
					system.actorOf(Worker.props(), Worker.DEFAULT_NAME + i);

			}
		});

	}
}
