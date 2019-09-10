#!/bin/bash

PARTITIONING_PATH='partitioning/'
SETOPERATIONS_PATH='../set-operations/'
EVALUATION_PATH='../evaluation/'
ENTITY_RESOLUTION_PATH='../entity-resolution/'
DATA_PATH='../data/'
TRANSITIVE_CLOSURE_PATH='../transitive-closure/'

cd $PARTITIONING_PATH
mvn clean verify

mvn install:install-file -Dfile=target/partitioning-1.0-jar-with-dependencies.jar -DpomFile=pom.xml

cd $SETOPERATIONS_PATH
mvn clean verify

mvn install:install-file -Dfile=target/setoperations-1.0.jar -DpomFile=pom.xml

pwd

cd $ENTITY_RESOLUTION_PATH
mvn clean verify

mvn install:install-file -Dfile=target/entity-resolution-1.0-jar-with-dependencies.jar -DpomFile=pom.xml

cd $EVALUATION_PATH
mvn clean verify

mvn install:install-file -Dfile=target/evaluation-1.0-jar-with-dependencies.jar -DpomFile=pom.xml

cd $DATA_PATH
mvn clean verify

mvn install:install-file -Dfile=target/data-1.0-jar-with-dependencies.jar -DpomFile=pom.xml

cd $TRANSITIVE_CLOSURE_PATH
mvn clean verify

mvn install:install-file -Dfile=target/transitive-closure-1.0-jar-with-dependencies.jar -DpomFile=pom.xml
