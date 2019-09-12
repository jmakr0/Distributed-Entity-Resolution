#!/bin/bash

# Run master node

echo "run master-node"

docker run -d --rm \
   --shm-size ${SHM_SIZE} \
   --network ${DOCKER_NETWORK} \
   --name rdse-master \
   --volume ${PATH_LOG_DIR}:/app/log \
   --volume ${PATH_DATA_MOUNT}:/app/data \
   --volume ${PATH_CONF_MOUNT}:/app/conf \
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
      --volume ${PATH_LOG_DIR}:/app/log \
      --volume ${PATH_CONF_MOUNT}:/app/conf \
      -e CONFIG_FILE=${CONFIG_FILE} \
      rdse/worker
done
