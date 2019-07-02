#!/bin/bash

set +e

# Run master node

echo "run master-node with $MASTER_WORKERS workers"

docker run -d --rm \
   --shm-size $SHM_SIZE \
   --network rdse-network \
   --name rdse-master \
   --volume $PATH_LOG_MOUNT:/app/log \
   --volume $PATH_DATA_MOUNT:/app/data \
   -e WORKERS=$MASTER_WORKERS \
   -e PATH_DATA=$MASTER_PATH_DATA \
   -e PATH_GOLD_STANDARD=$MASTER_PATH_GOLD_STANDARD \
   -e LOG_SUB_DIR=$PATH_LOG_TEST_DIR \
   rdse/master

# Run worker nodes

for I in $(seq $WORKER_NODES); do
    echo "run worker-node $I with $WORKER_WORKERS workers"

    docker run -d --rm \
      --shm-size $SHM_SIZE \
      --network rdse-network \
      --name rdse-worker-$I \
      --memory $WORKER_MEMORY \
      --cpu-shares $WORKER_CPU_SHARES \
      --volume $PATH_LOG_MOUNT:/app/log \
      -e WORKERS=$WORKER_WORKERS \
      -e LOG_SUB_DIR=$PATH_LOG_TEST_DIR \
      rdse/worker
done
