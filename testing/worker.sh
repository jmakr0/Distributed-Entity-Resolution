#!/bin/bash

set +e

docker run -t --rm --shm-size 512m --network rdse-network --name rdse-worker-1 rdse/worker
