#!/bin/bash

set +e

# General

TEST_NAME="Test_1"
TIMEOUT_SEC=200
SHM_SIZE=512m
PATH_LOG_DIR=log/$TEST_NAME/$(date +%Y%m%d_%H%M%S)
PATH_LOG_MOUNT=$(pwd)/testing
PATH_DATA_MOUNT=$(pwd)/../data

# Master settings

MASTER_WORKERS=0
MASTER_PATH_DATA="data/restaurant.csv"
MASTER_PATH_GOLD_STANDARD="data/restaurant_gold.csv"

# Worker settings

WORKER_NODES=1
WORKER_WORKERS=3

# Run test

echo "execute $TEST_NAME"

. ./testing/test.sh

echo "test is running ..."

# Wait until timeout is done