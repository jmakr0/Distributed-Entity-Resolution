#!/usr/bin/env bash

# Initialize network for tests

RDSE_NET=$(docker network ls -q -f "name=${DOCKER_NETWORK}")

if [[ -z "$RDSE_NET" ]]
then
    echo "testing network create"
    docker network create ${DOCKER_NETWORK}
else
    echo "testing network exists"
fi

# Setup log directory

LOGGING_PATH=${LOGGING_PATH}/${TEST_NAME}/log/$(date +%Y%m%d_%H%M%S)
mkdir -p ${LOGGING_PATH}
echo "logs are saved to: ${LOGGING_PATH}"

# Setup data directory

DATA_PATH=${DATA_PATH}/${TEST_NAME}/data
mkdir -p ${DATA_PATH}
echo "dataset copied to: ${DATA_PATH}/data.csv"

# Create/copy dataset

DATASET_SIZE_MB=$(du -m ${DATASET_PATH} | cut -f1) # takes the current size in MB

# Normalize filename to data.csv
if [[ ${TEST_DATASET_SIZE} -gt ${DATASET_SIZE_MB} ]]
then
    echo "create bigger dataset of ${TEST_DATASET_SIZE} MB"
    ./testing/helper/create_dataset.sh ${DATASET_PATH} ${DATA_PATH}/data.csv ${TEST_DATASET_SIZE}
else
    cp ${DATASET_PATH} ${DATA_PATH}/data.csv
fi

# We dont care about the gold standard when the dataset is replicated.
# Just copy it anyway.

cp ${GOLD_STANDARD_PATH} ${DATA_PATH}/data_gold.csv
echo "dataset gold standard copied to: ${DATA_PATH}/data_gold.csv"
