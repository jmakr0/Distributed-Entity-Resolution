#!/usr/bin/env bash

## Script will be sourced from each test and sets up the test environment; mind the context!

# Test

TEST_NAME="NEED_TO_BE_SET"
TEST_TIMEOUT=60                                         # in seconds
TEST_DATASET_SIZE=0                                     # in MB
TEST_WORKER_CONTAINER=1

# Configuration

CONFIG_PATH=$(pwd)/conf
CONFIG_FILE="default.conf"

# Dataset

DATA_PATH=$(pwd)/testing                                # will be manipulated in init.sh
DATASET_PATH=$(pwd)/../data/restaurant.csv
GOLD_STANDARD_PATH=$(pwd)/../data/restaurant_gold.csv

# Logging

LOGGING_PATH=$(pwd)/testing                             # will be manipulated in init.sh

# Docker

DOCKER_NETWORK="rdse-network"
SHM_SIZE=512m
WORKER_CPU_SHARES=0                                     # keeps default
WORKER_MEMORY=0                                         # keeps default
