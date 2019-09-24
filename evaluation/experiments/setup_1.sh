#!/usr/bin/env bash

# This setup will be executed by experiment.sh

# Set master host
sed -i -e "s/MASTER_IP_SET_BY_EXPERIMENT/${MASTER_HOST}/g" ${DIR}/evaluation.conf
# Set worker count
sed -i -e "s/WORKER_ACTORS_SET_BY_EXPERIMENT/1/g" ${DIR}/evaluation.conf
# Set bucket size
sed -i -e "s/BUCKET_SIZE_SET_BY_EXPERIMENT/500/g" ${DIR}/evaluation.conf