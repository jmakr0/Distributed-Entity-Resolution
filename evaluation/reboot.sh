#!/usr/bin/env bash

cd ansible

ansible-playbook deram-reboot.yaml --extra-vars "target=all"
