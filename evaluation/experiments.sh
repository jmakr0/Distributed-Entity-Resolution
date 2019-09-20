#!/usr/bin/env bash

cd ansible

ansible-playbook  --ask-pass deram-experiments.yaml \
                  --extra-vars "master=thor01 worker=thor02 experiment_nr=1 repeat=2"

# ansible-playbook  --ask-pass deram-experiments.yaml \
#                   --extra-vars "master=odin01 worker=odin-cluster[0:6] experiment_nr=2 repeat=1"
