#!/bin/bash

SEC=0
MASTER_CONTAINER=1

CLUSTER_NODES=$(($MASTER_CONTAINER + $WORKER_CONTAINER))
NODE_DONES=$(find ${TEST_LOG_PATH} -type f -name *.done.log | wc -l)
DOCKER_NODES=$(docker ps -aq -f "name=rdse*")
DOCKER_NETWORK=$(docker network ls -q -f "name=$DOCKER_NETWORK")

# Wait for cluster/timeout

until [[ ${NODE_DONES} -eq ${CLUSTER_NODES} ]] || [[ ${SEC} -ge ${TIMEOUT_SEC} ]]; do
   echo "...nodes that are done: $NODE_DONES | timeout in: $(($TIMEOUT_SEC - $SEC)) seconds";
   NODE_DONES=$(find ${TEST_LOG_PATH} -type f -name *.done.log | wc -l);
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
#if [[ -d ${TEST_DATA_PATH} ]]
#then
#  rm -rf ${TEST_DATA_PATH}
#fi
