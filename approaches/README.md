# Approaches

The [build.sh](build.sh) is used to build all approaches and offers some parameters to determine which build should be 
 performed: `-a --all, -s --shared, -d --deram, -m --monolith`

## Distributed Entity Resolution using the Actor Model (DERAM)
This approach is extensively discussed in our [paper](../documentation/RDSE-DERAM-kroschewski-strassenburg.pdf).

### Configuration
To make DERAM configurable and test it with different parameters, e.g. number of buckets for the consistent hashing, 
we introduced a configuration file that can be customized. 

#### Apply configuration

Whenever a `master` or `worker` instance should be started, the path to the config file that should be applied must be provided with: `-c --config`.
The [default.conf](src/main/resources/default.conf) can be used as a default configuration.

#### Fields in the configuration

The config file defines the following fields:

- logging
    - **level**: The log level that is specified by one of the following strings: OFF, ERROR, WARNING, INFO, DEBUG.  
- cluster
    - master
        - **host-address**: The host address on which the master node can be reached.
		- **port**: The port on which the master node can be reached
		- **worker-actors**: The number of worker actors that are created on the master node
    - worker 
        - **host-address**: The host address on which the worker node can be reached.
        - **port**: The port on which the worker node can be reached.
        - **worker-actors**: The number of worker actors that are created on the worker node.
	- data
		- input
			- **path**: The (csv) file that contains the data that should be deduplicated
			- **has-header**: A flag indicating if the file has a header as first line
			- **line-separator**: A string containing at index 0 the char that is the line separator of the (csv) file
			- **max-queue-size**: The maximum size of the queues that are used to queue data chunks of a specific size
		- gold-standard
			- **path**: The (csv) file that contains the gold standard
	- hash-router
		- **number-of-buckets**: The number of buckets that are placed on the hash ring. The buckets are distributed among all available workers. Therefore, the number of buckets should be _at least_ as high as the total number of available workers.
	- performance-tracker
		- **time-threshold**: The maximum time that a given worker should need for parsing a chunk of data
		- **min-workload**: A natural number indicating the minimum workload a worker should get regardless of his current performance. The size of the workload is defined as follows: `number_of_rows_in_data_chunk = workload^2`
		- **max-workload**: A natural number indicating the maximum workload a worker should get regardless of his current performance. The size of the workload is defined as follows: `number_of_rows_in_data_chunk = workload^2`
	- similarity 
		- abs-comparator
			- **threshold_min**: The distance between two given numbers that is at least required so that similarity != 1. For more details, we refer to the [AbsComparator](../shared/entity-resolution/src/main/java/de/hpi/rdse/der/similarity/numeric/AbsComparator.java).
			- **threshold_max**: The distance between two given numbers that is so high such that we set similarity = 0. For more details, we refer to the [AbsComparator](../shared/entity-resolution/src/main/java/de/hpi/rdse/der/similarity/numeric/AbsComparator.java).
			
			Example for threshold_min and threshold_max: 
			
			```threshold_min = 5, threshold_max = 10```
			
			```compare(5,6) -> abs distance < 5 -> sim = 1```
			
			```compare(5,20) -> abs distance > 10 -> sim = 0```
			
			```compare(5,11) -> 5 <= abs distance = 6 <= 10  -> sim somewhere between 1 and 0```
                 
	- duplicate-detection
		- **similarity-threshold**: A threshold _t_ [0,1] that is used to decide whether a given pair of tuples having a given similarity _s_ are duplicates or not. In detail, if `s > threshold -> duplicate`.
	- transitive-closure
		- **block-size**: The block size that is used for the distributed implementation of the Floyd Warshall algorithm.


## Monolith
Before we started to develop a distributed approach, we developed a monolithic approach using most of our shared libraries 
to show that our suggested pipeline performs properly. 
 
It consists of the class `EntityResolution` that contains all the entity resolution logic and a second class called `CSVService`, 
which is used to read the restaurant dataset and its gold standard. Because we do not use queues but instead want to read 
the whole dataset at once for the monolithic approach, we cannot use the CSV reading functionality from our shared libraries. 

## Shared 

To make our code reusable in different approaches, we implemented parts in different projects. These can be locally 
imported as `maven` dependency. For more details, please read the provided `Javadoc`.

### Available projects
* **data**: Contains the functionality of parsing the input and gold standard data.
* **entity-resolution**: Provides the similarity calculation for numbers, strings, and records.
* **evaluation**: Evaluates results against the corresponding *gold standard* by using _accuracy_, _recall_, and _F1-score_ as metrics.
* **partitioning**: Provides the logic for the hash partitioning.
* **set-operations**: Provides simple set operation functionalities that are used, for example, for the calculation of the F1-score.
* **transitive-closure**: Provides all the logic for the distributed calculation of the transitive closure using the Floyd Warshall Algorithm.

### Usage
The projects are imported into the `monolith` and `optimistic` approach using `maven`. Since we don't wanted to publish 
the projects as publicly available `maven` dependencies, they have to be build and imported before they can be used. 

We provide the [update-local-dependencies](update-local-dependencies.sh) script. It first builds the projects using 
`mvn clean verify`, and afterward, imports the project locally using the `mvn install:install-file` command.

After running the script the libraries can be imported and used like normal `maven` libraries.
For example, to include the `evaluation` project add the following dependency to your maven dependencies.

```xml
<dependency>
	<groupId>de.hpi.rdse.der</groupId>
	<artifactId>evaluation</artifactId>
	<version>1.0</version>
</dependency>
```