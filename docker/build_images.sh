#!/bin/bash

set +e

# set environment

export JAR_NAME="AMakkaN-1.0.jar"
export MASTER_HOST="rdse-master"

# get current app

mkdir tmp
cd ../approach_2
mvn clean verify
cd -
cp ../approach_2/target/$JAR_NAME tmp/

# build image

docker build -t rdse/base -f images/Dockerfile-base .

docker build -t rdse/master \
             --build-arg JAR_NAME=$JAR_NAME \
             --build-arg MASTER_HOST=$MASTER_HOST \
             -f images/Dockerfile-master .

docker build -t rdse/worker \
             --build-arg JAR_NAME=$JAR_NAME \
             --build-arg MASTER_HOST=$MASTER_HOST \
             -f images/Dockerfile-worker .

# cleanup

rm -rf tmp
