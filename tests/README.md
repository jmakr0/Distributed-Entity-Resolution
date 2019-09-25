# Tests

Tests are using our [docker setup](../docker) to test the current DERAM approach locally in isolation. 
A test script, e.g. [test_1.sh](test_1.sh), sets up its environment by sourcing [env.sh](bin/env.sh) and executes the following scripts:

* [init.sh](bin/init.sh): Creates the docker test network, log/data directory, copies/creates dataset. If desired (`TEST_DATASET_SIZE`=100), a bigger dataset is created.
* [run.sh](bin/run.sh): Starts master and worker nodes within a docker container.
* [teardown.sh](bin/teardown.sh): Waits until all nodes are done or a certain timeout is reached. Deletes the docker test network, all nodes, and the data directory which is only used for the tests.

The log data, which are saved in `testing/log` by default, are not deleted automatically. If you want to have a total cleanup, use `bin/helpers/cleanup.sh`.

## Artificial dataset
To get a bigger dataset, which is based on the [restaurant dataset](../data/restaurant.csv), the [create_dataset.sh](bin/helper/create_dataset.sh)
was created. A dataset with a size of 10MB can hence created with:

```
./create_dataset.sh ../data/restaurant.csv ../data/restaurant_new.csv 10
```

## Testing

To setup a custom test, copy [test_template.sh](test_template.sh) and adjust it. 
The [env.sh](bin/env.sh) contains all possible settings. During development, a couple of tests have been developed:

### Test_1

* Worker nodes: 1
* Worker on each node: 1

### Test_2

* Worker nodes: 2
* Worker on each node: 3

### Test_3

* Worker nodes: 2
* Worker on each node: 3
* Dataset size: 10MB
