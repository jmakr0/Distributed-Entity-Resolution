#!/bin/bash

set +e

# get current app

mkdir tmp
cd ../approach_2
mvn clean verify
cd -
cp ../approach_2/target/AMakkaN-1.0.jar tmp/

# build image

docker build --no-cache -t rdse/${ROLE} .

# cleanup

rm -rf tmp
