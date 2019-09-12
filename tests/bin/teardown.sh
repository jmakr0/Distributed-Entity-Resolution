#!/usr/bin/env bash

SEC=0
MASTER_CONTAINER=1

CLUSTER_NODES=$(($MASTER_CONTAINER + $TEST_WORKER_CONTAINER))
NODE_DONES=$(find ${LOGGING_PATH} -type f -name *.done.log | wc -l)
DOCKER_NODES=$(docker ps -aq -f "name=rdse*")
DOCKER_NETWORK=$(docker network ls -q -f "name=$DOCKER_NETWORK")

# Wait for cluster/timeout

until [[ ${NODE_DONES} -eq ${CLUSTER_NODES} ]] || [[ ${SEC} -ge ${TEST_TIMEOUT} ]]; do
   echo "...nodes that are done: $NODE_DONES | timeout in: $(($TEST_TIMEOUT - $SEC)) seconds";
   NODE_DONES=$(find ${LOGGING_PATH} -type f -name *.done.log | wc -l);
   SEC=$((SEC+5));
   sleep 5;
done

# Print reason for teardown

if [[ ${NODE_DONES} -eq ${CLUSTER_NODES} ]]
then
	echo "cluster is all done";
else 
	echo "cluster timed out";
fi

# Take down docker 

if ! [[ -z ${DOCKER_NODES} ]]
then
	echo "stop docker nodes";
	docker stop ${DOCKER_NODES}
fi

if ! [[ -z ${DOCKER_NETWORK} ]]
then
	echo "stop docker RDSE network";
	docker network rm ${DOCKER_NETWORK}
fi

# remove test data
#if [[ -d ${DATA_PATH} ]]
#then
#  rm -rf ${DATA_PATH}
#fi
