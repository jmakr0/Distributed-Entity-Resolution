package de.hpi.cluster;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import com.typesafe.config.Config;
import de.hpi.cluster.actors.Profiler;
//import de.hpi.cluster.actors.Reaper;
import de.hpi.cluster.actors.Worker;
import de.hpi.cluster.actors.listeners.ClusterListener;

public class ClusterMaster extends ClusterSystem {
	
	public static final String MASTER_ROLE = "master";

	public static void start(String actorSystemName, int workers, String host, int port, int slaves, String csvPath) {

		final Config config = createConfiguration(actorSystemName, MASTER_ROLE, host, port, host, port);
		
		final ActorSystem system = createSystem(actorSystemName, config);
		
		Cluster.get(system).registerOnMemberUp(new Runnable() {
			@Override
			public void run() {
				system.actorOf(ClusterListener.props(), ClusterListener.DEFAULT_NAME);
			//	system.actorOf(MetricsListener.props(), MetricsListener.DEFAULT_NAME);

				// Create the Reaper.
//				system.actorOf(Reaper.props(), Reaper.DEFAULT_NAME);

				system.actorOf(Profiler.props(), Profiler.DEFAULT_NAME);
				
				for (int i = 0; i < workers; i++)
					system.actorOf(Worker.props(), Worker.DEFAULT_NAME + i);
				
			//	int maxInstancesPerNode = workers; // TODO: Every node gets the same number of workers, so it cannot be a parameter for the slave nodes
			//	Set<String> useRoles = new HashSet<>(Arrays.asList("master", "slave"));
			//	ActorRef router = system.actorOf(
			//		new ClusterRouterPool(
			//			new AdaptiveLoadBalancingPool(SystemLoadAverageMetricsSelector.getInstance(), 0),
			//			new ClusterRouterPoolSettings(10000, workers, true, new HashSet<>(Arrays.asList("master", "slave"))))
			//		.props(Props.create(Worker.class)), "router");
			}
		});

//        List<String[]> csvData = CSVService.readCSV(csvPath);
//        Results resultsObject = new Results(csvData);

		system.actorSelection("/user/" + Profiler.DEFAULT_NAME).tell(new Profiler.ConfigMessage(slaves), ActorRef.noSender());
//		system.actorSelection("/user/" + Profiler.DEFAULT_NAME).tell(new Profiler.TaskMessages(csvTaskMessages), ActorRef.noSender());
	}

}
