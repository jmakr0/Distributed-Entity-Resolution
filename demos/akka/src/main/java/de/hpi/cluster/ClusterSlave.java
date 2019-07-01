package de.hpi.cluster;

import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import com.typesafe.config.Config;
//import de.hpi.cluster.actors.Reaper;
import de.hpi.cluster.actors.Worker;
import de.hpi.cluster.actors.listeners.MetricsListener;

public class ClusterSlave extends ClusterSystem {

	public static final String SLAVE_ROLE = "slave";
	
	public static void start(String actorSystemName, int workers, String host, int port, String masterhost, int masterport) {
		
		final Config config = createConfiguration(actorSystemName, SLAVE_ROLE, host, port, masterhost, masterport);
		
		final ActorSystem system = createSystem(actorSystemName, config);
		
		Cluster.get(system).registerOnMemberUp(new Runnable() {
			@Override
			public void run() {
				system.actorOf(MetricsListener.props(), MetricsListener.DEFAULT_NAME);

				// Create the Reaper.
//				system.actorOf(Reaper.props(), Reaper.DEFAULT_NAME);

				for (int i = 0; i < workers; i++)
					system.actorOf(Worker.props(), Worker.DEFAULT_NAME + i);

			}
		});

	}
}
