#!/usr/bin/env bash

# Note: execute  from top directory: ./scripts/build_no_tests.sh
mvn clean install -Pskip_integration_tests
