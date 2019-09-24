#!/usr/bin/env python
import os
from glob import glob


def extract_master_log(path):
    times = {}
    indicator_data = "DATA-READ: "
    indicator_tc = "START-TC: "
    indicator_done = "DONE: "

    f = open(path, "r")
    log_string = f.read()

    s_data = log_string.find(indicator_data) + len(indicator_data)
    e_data = log_string.find(' ms', s_data)
    s_tc = log_string.find(indicator_tc) + len(indicator_tc)
    e_tc = log_string.find(' ms', s_tc)
    s_done = log_string.find(indicator_done) + len(indicator_done)
    e_done = log_string.find(' ms', s_done)

    times["data-read"] = log_string[s_data:e_data]
    times["start-tc"] = log_string[s_tc:e_tc]
    times["finish"] = log_string[s_done:e_done]

    return times


# used for CPU logs
def extract_list(path):
    log_values = []

    open_file = open(path, "r")
    string = open_file.read()
    lst = string.split('\n')

    for value in lst:
        value = value.strip()
        if len(value) > 0:
            log_values.append(value.strip())

    return log_values


def extract(rootpath):
    experiment_paths = glob(rootpath + '/*')

    log_results = {}

    for experiment in experiment_paths:

        experiment_name = os.path.basename(experiment)

        log_results[experiment_name] = {}

        nodes_paths = glob(experiment + '/*')

        for node_path in nodes_paths:

            tmp = node_path.split('_')
            i = len(tmp)

            node = "node-count-" + tmp[i - 2]
            i_round = "round-" + tmp[i - 1]

            if node not in log_results[experiment_name]:
                log_results[experiment_name][node] = {}

            if i_round not in log_results[experiment_name][node]:
                log_results[experiment_name][node][i_round] = {}
                log_results[experiment_name][node][i_round]["data-read"] = 0
                log_results[experiment_name][node][i_round]["start-tc"] = 0
                log_results[experiment_name][node][i_round]["finish"] = 0
                log_results[experiment_name][node][i_round]["cpu-master"] = []
                log_results[experiment_name][node][i_round]["cpu-worker"] = []

            logs = glob(node_path + '/*')

            entry = log_results[experiment_name][node][i_round]

            for log in logs:

                # final times for data, tc, finish
                if log.find("master") != -1 and log.find("_.log") != -1:
                    master_log = extract_master_log(log)

                    entry["data-read"] = master_log["data-read"]
                    entry["start-tc"] = master_log["start-tc"]
                    entry["finish"] = master_log["finish"]

                # cpu-master
                if log.find("master") != -1 and log.find("_CPU.log") != -1:
                    values = extract_list(log)
                    entry["cpu-master"] = values

                # cpu-worker
                if log.find("worker") != -1 and log.find("_CPU.log") != -1:
                    values = extract_list(log)
                    entry["cpu-worker"].append(values)

    # print log_results
    return log_results
