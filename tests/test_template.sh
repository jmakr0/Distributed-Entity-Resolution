#!/usr/bin/env bash

# Source environment and setup test variables

. bin/env.sh

# To create a scrip, you can simply override the variables which are defined in the env.sh

TEST_NAME="TEST_TEMPLATE_NAME"

# ...
# ... further vars here

# Run test

echo
echo "###### $TEST_NAME ######"
echo

echo "### Initialize ###"
echo

. bin/init.sh

echo
echo "### Run ###"
echo

. bin/run.sh

echo
echo "### Teardown ###"
echo

. bin/teardown.sh

echo
echo "### Done ###"
