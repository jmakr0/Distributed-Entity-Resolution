#!/bin/bash

set +e

# set environment

export JAR_NAME="AMakkaN-1.0.jar"
export MASTER_HOST="rdse-master"
export PATH_DATA="./restaurant.csv"
export PATH_GOLD_STANDARD="./restaurant_gold.csv"

# get current app

mkdir tmp
cd ../approach_2
mvn clean verify
cd -
cp ../approach_2/target/$JAR_NAME tmp/
cp ../data/restaurant.csv tmp/
cp ../data/restaurant_gold.csv tmp/

# build image

docker build -t rdse/base -f base/Dockerfile .

docker build -t rdse/master \
             --build-arg JAR_NAME=$JAR_NAME \
             --build-arg MASTER_HOST=$MASTER_HOST \
             --build-arg PATH_DATA=$PATH_DATA \
             --build-arg PATH_GOLD_STANDARD=$PATH_GOLD_STANDARD \
             -f master/Dockerfile .

docker build -t rdse/worker \
             --build-arg JAR_NAME=$JAR_NAME \
             --build-arg MASTER_HOST=$MASTER_HOST \
             -f worker/Dockerfile .

# cleanup

rm -rf tmp
