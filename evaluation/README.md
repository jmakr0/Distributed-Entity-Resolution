# Evaluation

To increase repeatability, we automated our evaluation as much as possible. 
This directory contains all our code and setup of our experiments. The scripts in the root directory have the following tasks:

* [deploy.sh](deploy.sh)- Builds the current DERAM project with its dependencies, sets up the `deployment/` folder with the all experiments, and finally, deploys it to the test cluster using ansible.

* [experiments.sh](experiments.sh) - Uses ansible to run an experiment with the determined setup. It has the following parameters:
  * `$1` - master node
  * `$2` - worker nodes
  * `$3` - experiment number
  * `$4` - repeat
  * `$5` - number of workers to start with
  * `$6` - max number of workers
  * `$7` - deployment password
  * Example: `./experiments odin01 all 4 5 1 7 PASSWORD`

 * [reboot.sh](reboot.sh) - Reboots the entire cluster

## Ansible

The ansible folder contains the following playbooks:

* [deram-deployment.yaml](ansible/deram-deployment.yaml) - It compresses the deployment folder, copies and deploys the achieve to the nodes 
* [deram-experiments.yaml](ansible/deram-experiments.yaml) - First, it starts all workers asynchronously, waits till the master has completed, and pulls the results from the nodes
* [deram-reboot.yaml](ansible/deram-reboot.yaml) - Just reboots the entire cluster

## Experiments

Each experiment is a script, which is executed on each worker. The entry-point is always the [experiments script](experiments/experiment.sh).
It initializes the experiment, executes the application, and logs the CPU utilization. It also kills the process when it takes longer than 15 minutes.
Depending on the experiment number, an individual experiment script is executed, which sets up specific settings. 
The following four have been developed:

* [setup_1.sh](experiments/setup_1.sh) - Experiment 1 with one worker 
* [setup_2.sh](experiments/setup_2.sh) - Experiment 2 with 20 workers
* [setup_3.sh](experiments/setup_3.sh) - Experiment 3 with 20 workers, a bucket size of 1120, dataset size of 1MB
* [setup_4.sh](experiments/setup_4.sh) - Experiment 4 with 20 workers, a bucket size of 440, dataset size of 1.5MB

## Plot

To analyze the logged data of the experiments, we wrote a [python script](plot/extract.py) that provides a method to read all 
the data and transform it into a structured python dictionary. Its functionality is used in two Jupyter Notebooks, which were 
used to investigate the results of our experiments and finally, plot the figures.
 
* [cpu-usage.ipynb](plot/cpu-usage.ipynb) - Was used to investigate the CPU utilization within the single phases of our 
algorithm and additionally investigates how the computing power of the master and the workers evolves in several different settings. 
* [scaleout.ipynb](plot/scaleout.ipynb) - Was used to analyze how DERAM scales concerning a rising amount of computational power  
