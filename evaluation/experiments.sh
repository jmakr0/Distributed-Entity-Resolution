#!/usr/bin/env bash

master=$1
worker=$2
experiment_nr=$3
repeat=$4
start_nodes=$5 # 0-indexed!
max_worker_nodes=$6 # 0-indexed!

# master=odin01
# worker=all_worker
# experiment_nr=5
# repeat=5
# start_nodes=4 # 0-indexed!
# max_worker_nodes=11 # 0-indexed!

ansible_ssh_pass=cluster

cd ansible

# ansible-playbook  --ask-pass deram-experiments.yaml \
#                   --extra-vars "master=thor01 worker=thor02 experiment_nr=1 repeat=2"

for w in $(seq ${start_nodes} ${max_worker_nodes}); do

    dw=$((w+1))

    for r in $(seq ${repeat}); do
        echo "run experiment ${experiment_nr} with ${dw} nodes; round ${r}-${repeat}"
        
        result_dir="../results/experiment_${experiment_nr}/nodes_${dw}_${r}/"

        ansible-playbook deram-experiments.yaml \
                        --extra-vars "master=${master} \
                                    worker=${worker}[0:$w] \
                                    experiment_nr=${experiment_nr} \
                                    result_dir=${result_dir} \
                                    ansible_ssh_pass=${ansible_ssh_pass}"

        ansible-playbook deram-reboot.yaml \
                        --extra-vars "target=${worker}[0:$w]:${master}"
    done

done
