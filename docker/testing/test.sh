#!/bin/bash

set +e

# Initialize network for tests

RDSE_NET=$(docker network ls -q -f "name=rdse-network")

if [ -z "$RDSE_NET" ]
then
      echo "RDSE-TESTING network create ..."
      docker network create --subnet 172.16.238.0/24 rdse-network
      echo "RDSE-TESTING network created"
else
      echo "RDSE-TESTING network exists"
fi

# Setup log directory

mkdir -p $PATH_LOG_MOUNT/$PATH_LOG_DIR

echo "testing logs go to $PATH_LOG_MOUNT/$PATH_LOG_DIR"

echo "debug $PATH_LOG_MOUNT"
echo "debug $PATH_LOG_DIR"

# Run master node

docker run -d --rm \
   --shm-size $SHM_SIZE \
   --network rdse-network \
   --name rdse-master \
   --volume $PATH_LOG_MOUNT/log:/app/log \
   --volume $PATH_DATA_MOUNT:/app/data \
   -e WORKERS=$MASTER_WORKERS \
   -e PATH_DATA=$MASTER_PATH_DATA \
   -e PATH_GOLD_STANDARD=$MASTER_PATH_GOLD_STANDARD \
   -e LOG_FILE=$PATH_LOG_DIR/master.log \
   rdse/master

# Run worker nodes

docker run -d --rm \
   --shm-size $SHM_SIZE \
   --network rdse-network \
   --name rdse-worker-1 \
   --volume $PATH_LOG_MOUNT/log:/app/log \
   -e WORKERS=$WORKER_WORKERS \
   -e LOG_FILE=$PATH_LOG_DIR/worker_1.log \
   rdse/worker
