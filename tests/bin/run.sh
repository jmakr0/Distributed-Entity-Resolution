#!/usr/bin/env bash

# Run master node

echo "run master-node"

docker run -d --rm \
   --shm-size ${SHM_SIZE} \
   --network ${DOCKER_NETWORK} \
   --name rdse-master \
   --volume ${LOGGING_PATH}:/app/log \
   --volume ${DATA_PATH}:/app/data \
   --volume ${CONFIG_PATH}:/app/conf \
   -e CONFIG_FILE=${CONFIG_FILE} \
   rdse/master

# Run worker nodes

for I in $(seq ${TEST_WORKER_CONTAINER}); do
    echo "run worker-container $I"

    docker run -d --rm \
      --shm-size ${SHM_SIZE} \
      --network ${DOCKER_NETWORK} \
      --name rdse-worker-${I} \
      --memory ${WORKER_MEMORY} \
      --cpu-shares ${WORKER_CPU_SHARES} \
      --volume ${LOGGING_PATH}:/app/log \
      --volume ${CONFIG_PATH}:/app/conf \
      -e CONFIG_FILE=${CONFIG_FILE} \
      rdse/worker
done
