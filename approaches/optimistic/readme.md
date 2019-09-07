# Optimistic

## Configuration

To make our solution easily configurable we introduced a configuration file.
Per default the [default.conf](/src/main/resources/default.conf) file is used.

#### Use a custom configuration
* copy the [default.conf](/src/main/resources/default.conf) and rename it (e.g. `my-config`) and edit it
* when starting a `master` or `worker` instance use the command line parameter `-c`(or `-config`) (e.g. `master my-config`)


- cluster
    - master
        - **default-host-address** = "localhost"
		- **port** = 7877
		- **worker-actors** = 0
    - worker 
        - **default-host-address** = "localhost"
        - **port** = 7879
        - **worker-actors** = 1
	- data
		- input
			- **path** = restaurant.csv
			- **has-header** = true
			- **line-separator** = "\n"
			- **max-queue-size** = 5
		- gold-standard
			- **path** = restaurant_gold.csv
	- hash-router
		- **number-of-buckets** = 500
	- performance-tracker
		- **time-threshold** = 5000
		- **min-workload** = 1
	- similarity 
		- abs-comparator
			- **interval-start** = 5
			- **interval-end** = 30
	- duplicate-detection
		- **similarity-threshold** = 0.9
	- transitive-closure
		- **block-size** = 100
