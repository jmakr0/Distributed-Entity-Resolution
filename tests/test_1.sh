#!/bin/bash

# General

TEST_NAME="Test_1"
CONFIG_FILE="default.conf"
TIMEOUT_SEC=60

PATH_DATASET=$(pwd)/../data/restaurant.csv
PATH_DATASET_GOLD=$(pwd)/../data/restaurant_gold.csv
DATASET_SIZE_MB=$(du -m ${PATH_DATASET} | cut -f1) # takes the current size

# Docker settings

SHM_SIZE=512m
DOCKER_NETWORK="rdse-network"

PATH_DATA_MOUNT=$(pwd)/testing/data
PATH_CONF_MOUNT=$(pwd)/conf
PATH_LOG_DIR=$(pwd)/testing/log/${TEST_NAME}/$(date +%Y%m%d_%H%M%S)

WORKER_CPU_SHARES=0
WORKER_MEMORY=0

# Worker settings

WORKER_CONTAINER=1

# Master settings

MASTER_NEW_DATASET_SIZE_MB=0 # if 0, test keeps DATASET_SIZE_MB

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
