#!/bin/bash

# Run master node

echo "run master-node"

docker run -d --rm \
   --shm-size ${SHM_SIZE} \
   --network ${DOCKER_NETWORK} \
   --name rdse-master \
   --volume ${TEST_LOG_PATH}:/app/log \
   --volume ${TEST_DATA_PATH}:/app/data \
   --volume ${TEST_CONF_PATH}:/app/conf \
   -e CONFIG_FILE=${CONFIG_FILE} \
   rdse/master

# Run worker nodes

for I in $(seq ${WORKER_CONTAINER}); do
    echo "run worker-container $I"

    docker run -d --rm \
      --shm-size ${SHM_SIZE} \
      --network ${DOCKER_NETWORK} \
      --name rdse-worker-${I} \
      --memory ${WORKER_MEMORY} \
      --cpu-shares ${WORKER_CPU_SHARES} \
      --volume ${TEST_LOG_PATH}:/app/log \
      --volume ${TEST_CONF_PATH}:/app/conf \
      -e CONFIG_FILE=${CONFIG_FILE} \
      rdse/worker
done
