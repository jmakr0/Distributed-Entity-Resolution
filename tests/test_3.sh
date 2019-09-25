#!/usr/bin/env bash

# Source environment and setup test variables

. bin/env.sh

# Override environment settings from env.sh; make the test individual

TEST_NAME="Test_3"
TEST_TIMEOUT=70
CONFIG_FILE="test_2.conf"
SHM_SIZE=1024m
TEST_WORKER_CONTAINER=2
TEST_DATASET_SIZE=10

# Run test

echo
echo "###### $TEST_NAME ######"
echo

echo "### Initialize ###"
echo

. bin/init.sh

echo
echo "### Run ###"
echo
	
. bin/run.sh

echo
echo "### Teardown ###"
echo

. bin/teardown.sh

echo
echo "### Done ###"
