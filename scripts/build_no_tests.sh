#!/usr/bin/env bash

# Note: execute  from top directory: ./scripts/build_no_tests.sh

mvn clean install -Dmaven.test.skip=true $1
