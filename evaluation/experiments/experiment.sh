#!/usr/bin/env bash

experiment_nr=$1
jar=$2
role=$3
export MASTER_HOST=$4
export DIR=experiment_${experiment_nr}

# Kill experiment after X seconds
timeout=900

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

java -jar ${jar} ${role} -c evaluation.conf > result/${role}_${HOSTNAME}_${i}.log &

pid=$(pgrep -f "java -jar ${jar}")
sec=0
while ! [[ -z ${pid} ]] || [[ ${sec} -ge ${timeout} ]]; do
    ps -p ${pid} -o %cpu --noheader >> result/${role}_${HOSTNAME}_${i}_CPU.log
    ps -p ${pid} -o %mem --noheader >> result/${role}_${HOSTNAME}_${i}_MEM.log
    sleep 1
    pid=$(pgrep -f "java -jar ${jar}")
    
    sec=$((sec+1));
    if [[ ${sec} -ge ${timeout} ]]
    then
        kill -9 ${pid}
        mv result/${role}_${HOSTNAME}_${i}.log result/${role}_${HOSTNAME}_${i}_TIMEOUT.log
    fi
done

echo "done with experiment $experiment_nr"
