#!/usr/bin/env bash

## This script will instrument fast without Maven
## Adjust versions on the classpath if upgrading!
export JAVALITE=2.3.1-SNAPSHOT

export M2_REPO=~/.m2/repository

export CLASSPATH=$M2_REPO/org/javalite/activejdbc-instrumentation/$JAVALITE/activejdbc-instrumentation-$JAVALITE.jar
export CLASSPATH=${CLASSPATH}:$M2_REPO/org/javassist/javassist/3.18.1-GA/javassist-3.18.1-GA.jar
export CLASSPATH=${CLASSPATH}:$M2_REPO/org/javalite/activejdbc/$JAVALITE/activejdbc-$JAVALITE.jar
export CLASSPATH=${CLASSPATH}:$M2_REPO/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar
export CLASSPATH=${CLASSPATH}:$M2_REPO/org/slf4j/slf4j-simple/1.7.5/slf4j-simple-1.7.5.jar
export CLASSPATH=${CLASSPATH}:target/test-classes

java   -classpath $CLASSPATH  -DoutputDirectory=target/test-classes org.javalite.instrumentation.Main

time java -classpath $CLASSPATH  -DoutputDirectory=target/test-classes org.javalite.instrumentation.Main