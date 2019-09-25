# Evaluation

This directory contains all our code and setup for our experimental evaluation. The scripts in the root directory have the following tasks:

* `deploy.sh` - Builds the current DERAM project with its dependencies, sets up the `deployment/` folder with the all experiments, and finally, deploys it to the test cluster using ansible.

* `experiments.sh` - Uses ansible to run an experiment with the determined setup. It has the following parameters:
  * `$1` - master node
  * `$2` - worker nodes
  * `$3` - experiment number
  * `$4` - repeat
  * `$5` - number of workers to start with
  * `$6` - max number of workers
  * `$7` - deployment password
  * `Example: $./experiments odin01 all 4 5 1 7 PASSWORD`

 * `reboot.sh` - 
 

## Ansible

## Experiments

## Plot
To analyze the logged data of the experiments we wrote a python script (`extract.py`)
that provides a method to read all the data and transform it into a structured python dictionary.
This functionality is used in two Jupyter Notebooks that were used to investigate the results of our experiments
and finally, plot the figures for our paper.
 
- [cpu-usage.ipynb](./plot/cpu-usage.ipynb) was used to investigate the CPU utilization within the single phases of our 
algorithm and additionally see how the computing power of the master and the workers develops in several different settings. 
- [scaleout.ipynb](./plot/scaleout.ipynb) was used to analyze how our approach scales concerning a rising amount of computational power.  
