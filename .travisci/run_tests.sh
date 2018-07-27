#!/bin/bash

cd activejdbc

echo "mvn test -P$DB -V -B" && mvn test -P$DB -V -B