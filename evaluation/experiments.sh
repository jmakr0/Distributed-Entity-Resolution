#!/usr/bin/env bash

master=odin01
worker=odin-cluster
experiment_nr=2
repeat=3
max_worker_nodes=7

ansible_ssh_pass=cluster

cd ansible

# ansible-playbook  --ask-pass deram-experiments.yaml \
#                   --extra-vars "master=thor01 worker=thor02 experiment_nr=1 repeat=2"


for w in $(seq ${max_worker_nodes}); do

    for r in $(seq ${repeat}); do
    
        result_dir="../results/experiment_${experiment_nr}/nodes_${w}_${r}/"

        ansible-playbook deram-experiments.yaml \
                          --extra-vars "master=${master} \
                                        worker=${worker}[1:$w] \
                                        experiment_nr=${experiment_nr} \
                                        result_dir=${result_dir} \
                                        ansible_ssh_pass=${ansible_ssh_pass}"

        ansible-playbook deram-reboot.yaml --extra-vars "target=odin-cluster[0:$w]"
    done

done
