#!/bin/bash

set +e

docker stop $(docker ps -aq -f "name=rdse*")
docker network rm rdse-network

rm -rf log
