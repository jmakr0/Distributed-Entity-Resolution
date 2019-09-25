# TODO
 
## plot
To analyze the logged data of the experiments we wrote a python script (`extract.py`)
that provides a method to read all the data and transform it into a structured python dictionary.
This functionality is used in two Jupyter Notebooks that were used to investigate the results of our experiments
and finally, plot the figures for our paper.
 
- [cpu-usage.ipynb](./plot/cpu-usage.ipynb) was used to investigate the CPU utilization within the single phases of our 
algorithm and additionally see how the computing power of the master and the workers develops in several different settings. 
- [scaleout.ipynb](./plot/scaleout.ipynb) was used to analyze how our approach scales concerning a rising amount of computational power.  
