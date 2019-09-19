#!/usr/bin/env bash

experiment=$1

times=$(grep "Time:" results/experiment_${experiment}/* | sed 's/.*: //' | sed 's/ ms//' | tr '\n' ' \0')

echo ${times}
