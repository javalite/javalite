#!/bin/bash

set -e # Exit with nonzero exit code if anything fails

source .travisci/setup.sh

echo "mvn deploy -V -B -DskipTests"
mvn deploy -V -B -DskipTests