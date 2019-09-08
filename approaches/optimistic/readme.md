# Optimistic

## Configuration

To make our solution easily configurable we introduced a configuration file.
Per default the [default.conf](src/main/resources/default.conf) file is used.

#### Use a custom configuration
* Copy the [default.conf](src/main/resources/default.conf) and rename it (e.g. `my-config`) and edit it.
* When starting a `master` or `worker` instance use the command line parameter `-c`(or `-config`) (e.g. `master my-config`)

#### Fields in config file

The config file defines the following fields: 

- cluster
    - master
        - **host-address**: The host address at which the master node can be reached.
		- **port**: The port at which the master node can be reached.
		- **worker-actors**: The number of worker actors that are created on the master node.
    - worker 
        - **host-address**: The host address at which the worker node can be reached.
        - **port**: The port at which the worker node can be reached.
        - **worker-actors**: The number of worker actors that are created on the worker node.
	- data
		- input
			- **path**: The (csv) file that contains the data that should be deduplicated.
			- **has-header**: A Flag indicating if the file has a header as first line. 
			- **line-separator**: A String containing at index 0 the char that is the line separator of the (csv) file.
			- **max-queue-size**: The maximum size of the queues that are used to queue data chunks of a specific size. 
		- gold-standard
			- **path**: The (csv) file that contains the gold standard.
	- hash-router
		- **number-of-buckets**: The number of buckets that are placed on the hash ring. (Note: The buckets are distributed among all available workers. So, the number of buckets should be _at least_ as high as the total number of available workers.)
	- performance-tracker
		- **time-threshold**: The maximum time that a given worker should need for parsing a chunk of data.
		- **min-workload**: A natural number indicating the minimum workload a worker should get regardless of his current performance. (The size of the workload is defined as follows: `number_of_rows_in_data_chunk = workload^2) 
	- similarity 
		- abs-comparator
			- **interval-start**: The distance between two given numbers that is minimum required so that similarity != 1.` (Note: for more details see: [AbsComparator](../shared/entity-resolution/src/main/java/de/hpi/rdse/der/similarity/numeric/AbsComparator.java)) 
			- **interval-end**: The distance between two given numbers that is so high such that we set similarity = 0.  (Note: for more details see: [AbsComparator](../shared/entity-resolution/src/main/java/de/hpi/rdse/der/similarity/numeric/AbsComparator.java))
			
			Example for interval-start and interval-end: 
			
			```intervalStart = 5, intervalEnd = 10```
			
			```compare(5,6) -> abs distance < 5 -> sim = 1```
			
			```compare(5,20) -> abs distance > 10 -> sim = 0```
			
			```compare(5,11) -> 5 <= abs distance = 6 <= 10  -> sim somewhere between 1 and 0```
                 
	- duplicate-detection
		- **similarity-threshold**: Threshold (threshold _t_ in [0,1]) that is used to decide whether a given pair of tuples having a given similarity _s_ are duplicates or not. (`if s > threshold -> duplicate`)
	- transitive-closure
		- **block-size**: The block size that is used for the distributed implementation of the Floyd Warshall algorithm.
