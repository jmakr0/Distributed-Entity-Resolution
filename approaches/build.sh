#!/usr/bin/env bash

## Arguments

# -s, --shared
# builds dependencies/shared libs

# -m, --monolith
# builds monolith approach

# -o, --optimistic
# builds optimistic approach

MONOLITH_PATH="monolith/"
OPTIMISTIC_PATH="optimistic/"
SHARED="shared/"
SHARED_SH="update-local-dependencies.sh"

mvn_build() {
    echo "mvn build in $1"
    local dir=$1

    cd ${dir}
    mvn clean verify
    cd -
}

shared_build() {
    echo "build dependency ${1}/${2}"
    local dir=$1
    local script=$2

    cd ${dir}
    ./${script}
    cd -
}

build_all() {
    shared_build ${SHARED} ${SHARED_SH}
    mvn_build ${MONOLITH_PATH}
    mvn_build ${OPTIMISTIC_PATH}
}

# no arguments
if [[ $# == 0 ]]; then
    echo "build all dependencies and projects"
    build_all
    exit 0
else
    # loop over arguments
	for i in "$@"
    do
        # build shared
        if [[ ${i} == "--shared" ]] || [[ ${i} == "-s" ]]; then
            shared_build ${SHARED} ${SHARED_SH}
        fi

        # build monolith
        if [[ ${i} == "--monolith" ]] || [[ ${i} == "-m" ]]; then
            mvn_build ${MONOLITH_PATH}
        fi

        # build optimistic
        if [[ ${i} == "--optimistic" ]] || [[ ${i} == "-o" ]]; then
            mvn_build ${OPTIMISTIC_PATH}
        fi
    done
fi
