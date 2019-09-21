#!/usr/bin/env python
import re
from datetime import time
from glob import glob

experiment_paths = glob('results/*')

result = {}

for experiment in experiment_paths:

    result[experiment] = {}

    nodes_paths = glob(experiment + '/*')

    for node_path in nodes_paths:

        tmp = node_path.split('_')
        i = len(tmp)

        node = tmp[i - 2]
        i_round = tmp[i - 1]

        if node not in result[experiment]:
            result[experiment][node] = {}
            result[experiment][node]["finish"] = []
            result[experiment][node]["cpu-master"] = []
            result[experiment][node]["cpu-worker"] = {}

        if i_round not in result[experiment][node]["cpu-worker"]:
            result[experiment][node]["cpu-worker"][i_round] = []

        logs = glob(node_path + '/*')

        for log in logs:

            # final times
            if log.find("master") != -1 and log.find("_.log") != -1:
                indicator = "DONE: "
                f = open(log, "r")
                output = f.read()

                s = output.find(indicator) + len(indicator)
                e = output.find(' ms', s)

                result[experiment][node]["finish"].append(output[s:e])

            # cpu-master
            if log.find("master") != -1 and log.find("_CPU.log") != -1:
                f = open(log, "r")
                cpu_times = f.read()

                cpu_times_list = cpu_times.split('\n')

                for time in cpu_times_list:
                    time = time.strip()
                    if len(time) > 0:
                        result[experiment][node]["cpu-master"].append(time.strip())

            # cpu-worker
            if log.find("worker") != -1 and log.find("_CPU.log") != -1:
                f = open(log, "r")
                cpu_times = f.read()

                cpu_times_list = cpu_times.split('\n')

                for time in cpu_times_list:
                    time = time.strip()
                    if len(time) > 0:
                        result[experiment][node]["cpu-worker"][i_round].append(time.strip())

print result
