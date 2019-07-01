#!/bin/bash

set +e

SEC=0
CLUSTER_NODES=$(($MASTER_NODES + $WORKER_NODES)) 
NODE_DONES=$(find $PATH_LOG_DIR -type f -name *.done.log | wc -l)
DOCKER_NODES=$(docker ps -aq -f "name=rdse*")
DOCKER_NETWORK=$(docker network ls -q -f "name=$DOCKER_NETWORK")

# Wait for cluster/timeout

until [ $NODE_DONES -eq $CLUSTER_NODES ] || [ $SEC -ge $TIMEOUT_SEC ]; do
   echo "...nodes that are done: $NODE_DONES | timeout in: $(($TIMEOUT_SEC - $SEC)) seconds";
   NODE_DONES=$(find $PATH_LOG_DIR -type f -name *.done.log | wc -l);
   SEC=$((SEC+5));
   sleep 5;
done

# Print reason for teardown

if [ $NODE_DONES -eq $CLUSTER_NODES ] 
then
	echo "cluster is all done";
else 
	echo "cluster timed out";
fi

# Take down docker 

if ! [ -z "$DOCKER_NODES" ] 
then
	echo "stop docker nodes";
	docker stop $DOCKER_NODES
fi

if ! [ -z "$DOCKER_NETWORK" ] 
then
	echo "stop docker RDSE network";
	docker network rm $DOCKER_NETWORK
fi

if [ -d "$PATH_DATA_MOUNT" ] 
then
  rm -rf $PATH_DATA_MOUNT
fi
