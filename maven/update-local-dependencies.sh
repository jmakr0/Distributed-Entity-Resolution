#!/bin/sh

set +e

PARTITIONING_PATH='../approaches/shared/partitioning'
SETOPERATIONS_PATH='../set-operations'
EVALUATION_PATH='../evaluation'
DUPLICATEDETECTION_PATH='../duplicate-detection'


cd $PARTITIONING_PATH
mvn clean verify

mvn install:install-file -Dfile=target/Partitioning-1.0-jar-with-dependencies.jar -DpomFile=pom.xml




cd $SETOPERATIONS_PATH
mvn clean verify

mvn install:install-file -Dfile=target/setoperations-1.0.jar -DpomFile=pom.xml




cd $DUPLICATEDETECTION_PATH
mvn clean verify

mvn install:install-file -Dfile=target/duplicate-detection-1.0-jar-with-dependencies.jar -DpomFile=pom.xml




cd $EVALUATION_PATH
mvn clean verify

mvn install:install-file -Dfile=target/evaluation-1.0-jar-with-dependencies.jar -DpomFile=pom.xml

