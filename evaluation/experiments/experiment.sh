#!/usr/bin/env bash

experiment_nr=$1
jar=$2
role=$3
# repeat=$4

export MASTER_HOST=$4

export DIR=experiment_${experiment_nr}

# Remove old experiment
if [[ -d ${DIR} ]]
then
    rm -rf ${DIR}
fi

mkdir -p ${DIR}/result

cp ${jar} ${DIR}
cp data/* ${DIR}
cp evaluation.conf ${DIR}

# Run custom experiment setup
./setup_${experiment_nr}.sh

cd ${DIR}

# for i in $(seq ${repeat}); do
#     echo "run experiment $experiment_nr $i / $repeat"

java -jar ${jar} ${role} -c evaluation.conf > result/${role}_${HOSTNAME}_${i}.log &

pid=$(pgrep -f "java -jar ${jar}")
while ! [[ -z ${pid} ]]; do
    ps -p ${pid} -o %cpu --noheader >> result/${role}_${HOSTNAME}_${i}_CPU.log
    ps -p ${pid} -o %mem --noheader >> result/${role}_${HOSTNAME}_${i}_MEM.log
    sleep 1
    pid=$(pgrep -f "java -jar ${jar}")
done

# done

echo "done with experiment $experiment_nr"
