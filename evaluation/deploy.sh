#!/usr/bin/env bash

JAR_NAME="AMakkaN-1.0.jar"

# remove test data
if ! [[ -d deployment/ ]]
then
 mkdir deployment/
fi

cd ../approaches
./build.sh --optimistic
cd -

cp ../approaches/optimistic/target/${JAR_NAME} deployment/
cp -r ../data deployment/
cp evaluation.conf deployment/

cd ansible
ansible-playbook --ask-pass der-deployment.yaml --extra-vars "target=all"
