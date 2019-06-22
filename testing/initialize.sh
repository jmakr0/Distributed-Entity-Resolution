#!/bin/bash

set +e

docker network create --subnet 172.16.238.0/24 rdse-network
