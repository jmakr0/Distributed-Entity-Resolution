#!/usr/bin/env bash

cd ansible

#ansible-playbook  --ask-pass deram-experiments.yaml \
#                  --extra-vars "master=thor01 worker=thor02 experiment_nr=1 repeat=5"

ansible-playbook  --ask-pass deram-experiments.yaml \
                  --extra-vars "master=thor01 worker=thor-cluster experiment_nr=2 repeat=5"

