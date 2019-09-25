#!/usr/bin/env bash

JAR_NAME="DERAM-1.0.jar"
MASTER_HOST="rdse-master"
PORT_MASTER=7877
PORT_WORKER=7879
CONFIG_FILE="default.conf"

mkdir build
cd ../approaches/
./build.sh "$@"                                         # ATTENTION: args will be passed to the approaches/build.sh
cd -

cp ../approaches/deram/target/${JAR_NAME} build/   # Copy build

# Build image

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

# Cleanup

rm -rf build
