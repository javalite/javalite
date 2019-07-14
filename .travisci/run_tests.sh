#!/bin/bash

set -e # Exit with nonzero exit code if anything fails

cd activejdbc

echo "mvn test -P$DB -V -B" && mvn test -P$DB -V -B