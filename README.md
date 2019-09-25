# Abstract

Entity resolution is the process of matching records from multiple data sources that refer to the same objects. When 
applied to a single dataset, this process is also referred to as deduplication. As the amount of data increases, this 
process requires more computing power, and it is worth exploring how distributed computing environments can be used to 
reduce computing time. This paper introduces a distributed approach for entity resolution called DERAM that uses 
the actor model to highly parallelize the process, which involves a sophisticated approach to determining transitive 
dependencies between entities. Scalability and resource utilization are analyzed and evaluated in an experimental 
evaluation showing that distribution can significantly decrease computing time by up to 93% on the evaluated dataset.

## Distributed Entity Resolution

This repository contains the code of our approach, which we developed during the course 
[Reliable Distributed Systems Engineering](https://hpi.de/naumann/teaching/teaching/ss-19/reliable-distributed-systems-engineering.html)
at HPI. 

In the following folders, we put all information regarding our: 
* [Approaches](approaches/README.md) - Contains all the code
* [Docker](docker/README.md) - Our docker setup enabled us to test our implementation on a local cluster
* [Documentation](documentation/README.md) - Includes our [paper](documentation/RDSE-DERAM-kroschewski-strassenburg.pdf) 
in which we describe our approach and the conducted experiments                                                                                                                          
* [Evaluation](evaluation/README.md) - All information regarding our evaluation pipeline
* [Tests](tests/README.md) - Setup of our local tests which were conducted on our docker setup
