# Tests

A test script, e.g. [test_1.sh](test_1.sh), sets up its environment and sources it to the following scripts:

* [init.sh](../tests/bin/init.sh): Creates the docker test network, log/data directory, copies/creates dataset. If desired (`TEST_DATASET_SIZE`=100), a bigger dataset is created.
* [run.sh](../tests/bin/run.sh): Starts master and worker nodes within a docker container.
* [teardown.sh](../tests/bin/teardown.sh): Waits until all nodes are done or a certain timeout is reached. Deletes the docker test network, all nodes, and the data directory which is only used for the tests.

The log data, which are saved in `testing/log` by default, are not deleted automatically. If you want to have a total cleanup, use `./helpers/cleanup.sh`.

### Test_1

Worker nodes: 1

Worker on each node: 1

### Test_2

Worker nodes: 2

Worker on each node: 3

### Test_3

Worker nodes: 2

Worker on each node: 3

Dataset size: 10MB

### Custom test

To setup a custom test, copy [test_template.sh](test_template.sh) and adjust it.
