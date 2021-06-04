#!/usr/bin/env bash

# Note: execute  from top directory: ./scripts/build_no_tests.sh

mvn clean deploy  -Dmaven.test.skip=true
