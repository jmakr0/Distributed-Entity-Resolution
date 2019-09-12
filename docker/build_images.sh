#!/bin/bash

# set environment

export JAR_NAME="AMakkaN-1.0.jar"
export MASTER_HOST="rdse-master"
export PORT_MASTER=7877
export PORT_WORKER=7879
export CONFIG_FILE="default.conf"

# get current app

mkdir build
cd ../approaches/optimistic
mvn clean verify
cd -
cp ../approaches/optimistic/target/${JAR_NAME} build/

# build image

docker build -t rdse/master \
             --build-arg JAR_NAME=${JAR_NAME} \
             --build-arg PORT=${PORT_MASTER} \
             --build-arg CONFIG_FILE=${CONFIG_FILE} \
             --build-arg ROLE=master \
             -f Dockerfile-DER .

docker build -t rdse/worker \
             --build-arg JAR_NAME=${JAR_NAME} \
             --build-arg PORT=${PORT_WORKER} \
             --build-arg CONFIG_FILE=${CONFIG_FILE} \
             --build-arg ROLE=worker \
             -f Dockerfile-DER .

# cleanup

rm -rf build
