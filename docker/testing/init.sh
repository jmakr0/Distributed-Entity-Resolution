#!/bin/bash

set +e

# Initialize network for tests

RDSE_NET=$(docker network ls -q -f "name=$DOCKER_NETWORK")

if [ -z "$RDSE_NET" ] 
then
    echo "testing network create"
    docker network create $DOCKER_NETWORK
else
    echo "testing network exists"
fi

# Setup log directory

mkdir -p $PATH_LOG_DIR
echo "logs are saved to: $PATH_LOG_DIR"

# Setup data directory

mkdir -p $PATH_DATA_MOUNT
echo "dataset copied to: $PATH_DATA_MOUNT/data.csv"

# Create/Copy dataset

if [ $MASTER_NEW_DATASET_SIZE_MB -gt $DATASET_SIZE_MB ] 
then
    echo "create bigger dataset of $MASTER_NEW_DATASET_SIZE_MB MB"
    ./testing/helper/create_dataset.sh $PATH_DATASET $PATH_DATA_MOUNT/data.csv $MASTER_NEW_DATASET_SIZE_MB
else
    cp $PATH_DATASET $PATH_DATA_MOUNT/data.csv
fi

# We dont care about the gold standard when the dataset is replicated.
# Just copy it anyway.

cp $PATH_DATASET_GOLD $PATH_DATA_MOUNT/data_gold.csv
echo "dataset gold standard copied to: $PATH_DATA_MOUNT/data_gold.csv"
