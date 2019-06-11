package de.hpi.cluster;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import com.typesafe.config.Config;
import de.hpi.cluster.actors.Master;
import de.hpi.cluster.actors.Worker;
import de.hpi.cluster.actors.listeners.ClusterListener;

//import de.hpi.cluster.actors.Reaper;

public class ClusterMaster extends ClusterSystem {
	
	public static final String MASTER_ROLE = "master";

	public static void start(String actorSystemName, int workers, String host, int port, int slaves, String inputPath, String goldPath) {

		final Config config = createConfiguration(actorSystemName, MASTER_ROLE, host, port, host, port);
		
		final ActorSystem system = createSystem(actorSystemName, config);
		
		Cluster.get(system).registerOnMemberUp(new Runnable() {
			@Override
			public void run() {
				system.actorOf(ClusterListener.props(), ClusterListener.DEFAULT_NAME);

				ActorRef master = system.actorOf(Master.props(), Master.DEFAULT_NAME);
				master.tell(new Master.ConfigMessage(inputPath, goldPath), ActorRef.noSender());
				
				for (int i = 0; i < workers; i++)
					system.actorOf(Worker.props(), Worker.DEFAULT_NAME + i);
				
			}
		});
	}

}
