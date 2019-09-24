#!/usr/bin/env bash

# This setup will be executed by experiment.sh

# Set master host
sed -i -e "s/MASTER_IP_SET_BY_EXPERIMENT/${MASTER_HOST}/g" ${DIR}/evaluation.conf
# Set worker count
sed -i -e "s/WORKER_ACTORS_SET_BY_EXPERIMENT/20/g" ${DIR}/evaluation.conf
# Set bucket size, NODES*WORKER*2
sed -i -e "s/BUCKET_SIZE_SET_BY_EXPERIMENT/280/g" ${DIR}/evaluation.conf

# Increase dataset by 512KB
./create_dataset.sh data/restaurant.csv ${DIR}/restaurant.csv 0.5
