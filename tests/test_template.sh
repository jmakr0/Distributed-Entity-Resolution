#!/bin/bash

# General

TEST_NAME="YOUR_TEST_NAME"
TIMEOUT_SEC=60

DATASET_PATH=$(pwd)/../data/restaurant.csv
GOLD_STANDARD_PATH=$(pwd)/../data/restaurant_gold.csv
DATASET_SIZE_MB=$(du -m $DATASET_PATH | cut -f1) # takes the current size

# Docker settings

SHM_SIZE=512m
DOCKER_NETWORK="rdse-network"

TEST_DATA_PATH=$(pwd)/testing/data

PATH_LOG_TEST_DIR=$TEST_NAME/$(date +%Y%m%d_%H%M%S)/
PATH_LOG_MOUNT=$(pwd)/testing/log
TEST_LOG_PATH=$PATH_LOG_MOUNT/$PATH_LOG_TEST_DIR

WORKER_CPU_SHARES=0
WORKER_MEMORY=0

# Master settings

MASTER_NODES=1
MASTER_WORKERS=0
MASTER_NEW_DATASET_SIZE_MB=0

# Worker settings

WORKER_NODES=1
WORKER_WORKERS=1

# Run test

echo
echo "###### $TEST_NAME ######"
echo

echo "### Initialize ###"
echo

. ../docker/testing/init.sh

echo
echo "### Run ###"
echo

. ../docker/testing/run.sh

echo
echo "### Teardown ###"
echo

. ../docker/testing/teardown.sh

echo
echo "### Done ###"
