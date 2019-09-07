package de.hpi.cluster;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import com.typesafe.config.Config;
import de.hpi.cluster.actors.Master;
import de.hpi.cluster.actors.Reaper;
import de.hpi.cluster.actors.Worker;
import de.hpi.cluster.actors.listeners.ClusterListener;

//import de.hpi.cluster.actors.Reaper;

public class ClusterMaster extends ClusterSystem {
	
	public static final String MASTER_ROLE = "master";

	public static void start(String actorSystemName, Config config) {
		int workers = config.getInt("der.cluster.master.worker-actors");
		String host = config.getString("der.cluster.master.default-host-address");
		int port = config.getInt("der.cluster.master.port");
		String inputPath = config.getString("der.data.input.path");
		String goldPath = config.getString("der.data.gold-standard.path");

		final Config actorSystemConfig = createConfiguration(actorSystemName, MASTER_ROLE, host, port, host, port);
		
		final ActorSystem system = createSystem(actorSystemName, actorSystemConfig);
		
		Cluster.get(system).registerOnMemberUp(new Runnable() {
			@Override
			public void run() {
				system.actorOf(Reaper.props(), Reaper.DEFAULT_NAME);

				system.actorOf(ClusterListener.props(), ClusterListener.DEFAULT_NAME);

				ActorRef master = system.actorOf(Master.props(), Master.DEFAULT_NAME);
				master.tell(new Master.ConfigMessage(config), ActorRef.noSender());
				
				for (int i = 0; i < workers; i++)
					system.actorOf(Worker.props(), Worker.DEFAULT_NAME + i);
				
			}
		});
	}
}
