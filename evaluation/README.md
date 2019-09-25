# Evaluation

This directory contains all our code and setup for our experimental evaluation. The scripts in the root directory have the following tasks:
* `deploy.sh` - Builds the current DERAM project with its dependencies, sets up the `deployment/` folder with the all experiments, and finally, deploys it to the test cluster using ansible.
* `experiments.sh` - 
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
