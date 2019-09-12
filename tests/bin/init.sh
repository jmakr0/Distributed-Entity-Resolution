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

mkdir -p ${TEST_LOG_PATH}
echo "logs are saved to: ${TEST_LOG_PATH}"

# Setup data directory

mkdir -p ${TEST_DATA_PATH}
echo "dataset copied to: ${TEST_DATA_PATH}/data.csv"

# Create/Copy dataset

# normalize filename to data.csv
if [[ ${MASTER_NEW_DATASET_SIZE_MB} -gt ${DATASET_SIZE_MB} ]]
then
    echo "create bigger dataset of ${MASTER_NEW_DATASET_SIZE_MB} MB"
    ./testing/helper/create_dataset.sh ${DATASET_PATH} ${TEST_DATA_PATH}/data.csv ${MASTER_NEW_DATASET_SIZE_MB}
else
    cp ${DATASET_PATH} ${TEST_DATA_PATH}/data.csv
fi

# We dont care about the gold standard when the dataset is replicated.
# Just copy it anyway.

cp ${GOLD_STANDARD_PATH} ${TEST_DATA_PATH}/data_gold.csv
echo "dataset gold standard copied to: ${TEST_DATA_PATH}/data_gold.csv"
